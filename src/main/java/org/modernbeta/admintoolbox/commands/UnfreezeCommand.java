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

import static org.modernbeta.admintoolbox.AdminToolboxPlugin.BROADCAST_EXEMPT_PERMISSION;

public class UnfreezeCommand implements CommandExecutor, TabCompleter {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	private static final String UNFREEZE_COMMAND_PERMISSION = "admintoolbox.unfreeze";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(!sender.hasPermission(UNFREEZE_COMMAND_PERMISSION)) return false; // Bukkit should handle this for us, just a sanity-check
		if(args.length != 1) return false;

		OfflinePlayer target = Bukkit.getOfflinePlayerIfCached(args[0]);
		if(target == null || !plugin.getFreezeManager().isFrozen(target)) {
			sender.sendRichMessage("<red>Error: Player <gray><name></gray> is not frozen.",
				Placeholder.unparsed("name", args[0]));
			return true;
		}

		FreezeManager freezeManager = plugin.getFreezeManager();
		freezeManager.unfreeze(target);

		if(target.isOnline())
			((Player) target).sendRichMessage("<green>You have been unfrozen!");

		if (!sender.hasPermission(BROADCAST_EXEMPT_PERMISSION)) {
			PermissionAudience adminAudience = plugin.getAdminAudience().excluding(sender);
			adminAudience
				.sendMessage(MiniMessage.miniMessage().deserialize(
					"<gold><admin> unfroze <target>.",
					Placeholder.unparsed("admin", sender.getName()),
					Placeholder.unparsed("target", target.getName())
				));
		}

		sender.sendRichMessage("<gold>You released <target>.",
			Placeholder.unparsed("target", target.getName()));

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1) {
			String partialName = args[0].toLowerCase();

			return Bukkit.getOnlinePlayers().stream()
				.filter((player) -> plugin.getFreezeManager().isFrozen(player))
				.map(OfflinePlayer::getName)
				.filter((name) -> name.toLowerCase().startsWith(partialName))
				.toList();
		}

		return List.of();
	}
}
