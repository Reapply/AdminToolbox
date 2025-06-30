package org.modernbeta.admintoolbox.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;

import java.util.List;

public class StreamerModeCommand implements CommandExecutor, TabCompleter {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	private static final String STREAMER_MODE_COMMAND_PERMISSION = "admintoolbox.streamermode";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.hasPermission(STREAMER_MODE_COMMAND_PERMISSION))
			return false; // Bukkit should handle this for us, just a sanity-check
		if (!(sender instanceof Player player)) {
			sender.sendRichMessage("<red>Error: You must be a player to use this command.");
			return false;
		}

		// - toggle Streamer Mode
		// - if enabling, parse provided duration
		// - use LuckPerms API to add negated/'false' versions of permissions from config.yml to user for duration
		// - if possible, use meta tag with expiry as well to denote streamer mode presence so we don't have to track it ourselves

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		return List.of();
	}
}
