package org.modernbeta.admintoolbox.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;
import org.modernbeta.admintoolbox.managers.admin.AdminManager;
import org.modernbeta.admintoolbox.managers.admin.AdminState;

import java.util.List;
import java.util.Objects;

public class GoForwardCommand implements CommandExecutor, TabCompleter {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	private static final String GO_FORWARD_COMMAND_PERMISSION = "admintoolbox.target";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.hasPermission(GO_FORWARD_COMMAND_PERMISSION)) return false;
		if (args.length > 0) return false;

		if (!(sender instanceof Player player)) {
			sender.sendRichMessage("<red>You must be a player to run this command!");
			return true;
		}

		AdminManager adminManager = plugin.getAdminManager();

		if (!adminManager.isActiveAdmin(player)) {
			sender.sendRichMessage("<red>You are not in admin mode!");
			return true;
		}

		AdminState adminState = adminManager.getAdminState(player).orElseThrow();
		Location nextLocation = adminState.getTeleportHistory().goForward();

		if (nextLocation == null) {
			player.sendRichMessage("<red>You are as far forward as you can go!");
		} else {
			adminManager.target(player, nextLocation, false);
		}

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return List.of();
	}
}
