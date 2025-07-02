package org.modernbeta.admintoolbox.commands;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class StreamerModeCommand implements CommandExecutor, TabCompleter {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	private static final String STREAMER_MODE_COMMAND_PERMISSION = "admintoolbox.streamermode";
	private static final String STREAMER_MODE_META_KEY = "admintoolbox-streamermode";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.hasPermission(STREAMER_MODE_COMMAND_PERMISSION))
			return false; // Bukkit should handle this for us, just a sanity-check
		if (!(sender instanceof Player player)) {
			sender.sendRichMessage("<red>Only players may use Streamer Mode.");
			return true;
		}

		if (plugin.getLuckPermsAPI().isEmpty()) {
			sender.sendRichMessage("<red>LuckPerms is required to use Streamer Mode. Is it enabled?");
			return true;
		}
		LuckPerms luckPerms = plugin.getLuckPermsAPI().get();

		if (isStreamerModeActive(luckPerms, player)) {
			if (args.length > 0) return false;

			// TODO: disable Streamer Mode
			return true;
		}

		if(args.length != 1) return false;

		// TODO: parse duration arg

		User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
		MetaNode metaNode = MetaNode.builder()
			.key(STREAMER_MODE_META_KEY)
			.value(Boolean.toString(true))
			.expiry(1, TimeUnit.HOURS) // TODO: use parsed duration here
			.build();

		user.data().clear(NodeType.META.predicate((node) -> node.getMetaKey().equals(STREAMER_MODE_META_KEY)));
		user.data().add(metaNode);

		// TODO: use LuckPerms API to add negated/'false' versions of permissions from config.yml to user for duration

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		// if first arg is int-parseable, suggest time suffixes m/h (for minutes/hours)
		// if player is in Streamer Mode, do not validate/suggest duration

		return List.of();
	}

	private boolean isStreamerModeActive(LuckPerms luckPerms, Player player) {
		return luckPerms.getPlayerAdapter(Player.class)
			.getMetaData(player)
			.getMetaValue(STREAMER_MODE_META_KEY, Boolean::valueOf)
			.orElse(false);
	}
}
