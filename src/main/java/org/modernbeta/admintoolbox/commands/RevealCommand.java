package org.modernbeta.admintoolbox.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;
import org.modernbeta.admintoolbox.PermissionAudience;
import org.modernbeta.admintoolbox.managers.AdminManager;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class RevealCommand implements CommandExecutor {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	private static final String REVEAL_COMMAND_PERMISSION = "admintoolbox.reveal";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.hasPermission(REVEAL_COMMAND_PERMISSION))
			return false; // Bukkit should handle this for us, just a sanity-check
		if (!(sender instanceof Player player)) {
			sender.sendRichMessage("<red>Error: You must be a player to use this command.");
			return false;
		}

		AdminManager adminManager = plugin.getAdminManager();

		if(!adminManager.isSpectating(player)) {
			player.sendRichMessage("<red>Error: You cannot reveal if you are not spectating!");
			return true;
		}

		adminManager.reveal(player);

		return true;
	}
}
