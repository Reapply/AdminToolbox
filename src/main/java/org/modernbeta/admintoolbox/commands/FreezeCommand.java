package org.modernbeta.admintoolbox.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;
import org.modernbeta.admintoolbox.PermissionAudience;
import org.modernbeta.admintoolbox.managers.PlayerFreezeManager;

public class FreezeCommand implements CommandExecutor {
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
				Placeholder.unparsed("name", (target != null) ? target.getName() : args[0])
			);
			return true;
		}

		PlayerFreezeManager freezeManager = plugin.getFreezeManager();

		if (freezeManager.isFrozen(target)) {
			sender.sendRichMessage(
				"<red>Error: Player <gray><name></gray> is already frozen!",
				Placeholder.unparsed("name", target.getName())
			);
			return true;
		}
		freezeManager.freeze(target);

		target.sendRichMessage("<red>You have been frozen!");

		PermissionAudience adminAudience = plugin.getAdminAudience();
		if(sender instanceof Player adminPlayer) adminAudience = adminAudience.excluding(adminPlayer);
		adminAudience
			.sendMessage(MiniMessage.miniMessage().deserialize(
				"<gold><admin> has frozen <target>!",
				Placeholder.unparsed("admin", sender.getName()),
				Placeholder.unparsed("target", target.getName())
			));

		return true;
	}
}
