package org.modernbeta.admintoolbox.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;
import org.modernbeta.admintoolbox.PermissionAudience;
import org.modernbeta.admintoolbox.managers.FreezeManager;

import java.util.List;
import java.util.Optional;

public class FreezeCommand implements CommandExecutor, TabCompleter {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	private static final String FREEZE_COMMAND_PERMISSION = "admintoolbox.freeze";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(!sender.hasPermission(FREEZE_COMMAND_PERMISSION)) return false; // Bukkit should handle this for us, just a sanity-check
		if(args.length != 1) return false;

		Player target = Bukkit.getPlayer(args[0]);
		if(target == null || !target.isOnline()) {
			sender.sendRichMessage(
				"<red>Error: Player <gray><name></gray> is not online.",
				Placeholder.unparsed("name", Optional.ofNullable(target)
					.map(Player::getName)
					.orElse(args[0]))
			);
			return true;
		}

		FreezeManager freezeManager = plugin.getFreezeManager();

		if (freezeManager.isFrozen(target)) {
			sender.sendRichMessage(
				"<red>Error: Player <gray><name></gray> is already frozen!",
				Placeholder.unparsed("name", target.getName())
			);
			return true;
		}
		freezeManager.freeze(target);

		target.sendRichMessage("<red>You have been frozen!");

		if (!sender.hasPermission(AdminToolboxPlugin.BROADCAST_EXEMPT_PERMISSION)) {
			PermissionAudience adminAudience = plugin.getAdminAudience().excluding(sender);
			adminAudience
				.sendMessage(MiniMessage.miniMessage().deserialize(
					"<gold><admin> froze <target>.",
					Placeholder.unparsed("admin", sender.getName()),
					Placeholder.unparsed("target", target.getName())
				));
		}

		sender.sendRichMessage(
			"<gold>You froze <target>.",
			Placeholder.unparsed("target", target.getName())
		);

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1) {
			String partialName = args[0].toLowerCase();

			return Bukkit.getOnlinePlayers().stream()
				.filter((player) -> !plugin.getFreezeManager().isFrozen(player))
				.map(OfflinePlayer::getName)
				.filter((name) -> name.toLowerCase().startsWith(partialName))
				.toList();
		}

		return List.of();
	}
}
