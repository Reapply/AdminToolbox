package org.modernbeta.admintoolbox.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;
import org.modernbeta.admintoolbox.managers.AdminManager;
import org.modernbeta.admintoolbox.managers.AdminManager.TeleportHistory;

import java.util.Objects;

public class GoBackCommand implements CommandExecutor {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	private static final String GO_BACK_COMMAND_PERMISSION = "admintoolbox.target";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.hasPermission(GO_BACK_COMMAND_PERMISSION)) return false;
		if (args.length > 0) return false;

		if (!(sender instanceof Player player)) {
			sender.sendRichMessage("<red>Error: You must be a player to run this command!");
			return true;
		}

		AdminManager adminManager = plugin.getAdminManager();

		if (!adminManager.isActiveAdmin(player)) {
			sender.sendRichMessage("<red>Error: You are not in an active admin state!");
			return true;
		}

		TeleportHistory<Location> history = Objects.requireNonNull(adminManager.getTeleportHistory(player));
		Location previousLocation = history.goBack();

		if (previousLocation == null) {
			adminManager.restore(player);
		} else {
			adminManager.target(player, previousLocation, false);
		}

		return true;
	}
}
