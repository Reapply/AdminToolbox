package org.modernbeta.admintoolbox.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;
import org.modernbeta.admintoolbox.PermissionAudience;
import org.modernbeta.admintoolbox.utils.LocationUtils;

import java.util.List;

public class SpawnCommand implements CommandExecutor, TabCompleter {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	public static final String TARGET_SPAWN_PERMISSION = "admintoolbox.target.spawn";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendRichMessage("<red>Error: You must be a player to use this command.");
			return false;
		}

		if (!player.hasPermission(TARGET_SPAWN_PERMISSION)) {
			sendNoPermissionMessage(sender);
			return true;
		}

		World targetWorld;
		if (args.length == 0) {
			targetWorld = player.getWorld();
		} else if (args.length == 1) {
			targetWorld = LocationUtils.resolveWorld(args[0]);
			if (targetWorld == null) {
				sender.sendRichMessage("<red>Error: Could not find world '" + args[0] + "'.");
				return true;
			}
		} else {
			return false;
		}

		Location spawnLocation = targetWorld.getSpawnLocation().toCenterLocation();
		String worldLabel = LocationUtils.getShortWorldName(targetWorld);

		// Ensure chunk is loaded before teleporting
		targetWorld.getChunkAtAsync(spawnLocation).thenAccept(chunk -> {
			plugin.getAdminManager().target(player, spawnLocation);

			player.sendRichMessage("<gold>Spectating at <target> spawn",
				Placeholder.unparsed("target", worldLabel));

			if (!player.hasPermission(AdminToolboxPlugin.BROADCAST_EXEMPT_PERMISSION)) {
				PermissionAudience adminAudience = plugin.getAdminAudience()
					.excluding(player);
				adminAudience.sendMessage(MiniMessage.miniMessage().deserialize(
					"<gold><admin> is spectating at <target> spawn",
					Placeholder.unparsed("admin", sender.getName()),
					Placeholder.unparsed("target", worldLabel)
				));
			}
		});

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(!sender.hasPermission(TARGET_SPAWN_PERMISSION)) return List.of();

		String partialName = args[0].toLowerCase();

		return Bukkit.getOnlinePlayers().stream()
			.map(Player::getName)
			.filter(name -> name.toLowerCase().startsWith(partialName) && !name.equals(sender.getName()))
			.toList();
	}

	private void sendNoPermissionMessage(CommandSender sender) {
		sender.sendRichMessage("<red>You do not have permission!");
	}
}
