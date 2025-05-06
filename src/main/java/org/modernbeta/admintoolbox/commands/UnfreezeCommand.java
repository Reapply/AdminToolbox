package org.modernbeta.admintoolbox.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;
import org.modernbeta.admintoolbox.PermissionAudience;

public class UnfreezeCommand implements CommandExecutor {
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

		plugin.getFreezeManager().unfreeze(target);

		if(target.isOnline())
			((Player) target).sendRichMessage("<green>You were unfrozen!");

		if (!sender.hasPermission(AdminToolboxPlugin.BROADCAST_EXEMPT_PERMISSION)) {
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
}
