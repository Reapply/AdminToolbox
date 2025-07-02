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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		if (args.length != 1) return List.of();
		if (plugin.getLuckPermsAPI().isEmpty()) return List.of();
		if (!(sender instanceof Player player)) return List.of();

		// if player is in Streamer Mode, do not validate/suggest duration since this is an 'off' toggle!
		{
			LuckPerms luckPerms = plugin.getLuckPermsAPI().get();
			if (isStreamerModeActive(luckPerms, player)) return List.of();
		}

		String partialArgument = args[0];

		// if arg is int-parseable, suggest time suffixes m/h (for minutes/hours)
		try {
			Integer.parseUnsignedInt(partialArgument);
		} catch (NumberFormatException e) {
			return List.of();
		}

		List<String> supportedUnits = List.of("h", "m");

		return supportedUnits.stream()
			.map((unit) -> partialArgument + unit) // suggests typed number with units at end - i.e. 15 -> 15m, 15h
			.toList();
	}

	/// Rudimentary regex-based parser for durations.
	///
	/// ## Examples
	/// - `5h` -> 5 hours
	/// - `15m` -> 15 minutes
	///
	/// ## Note
	/// <strong>Only one duration segment is supported.</strong> That means durations such as
	/// '1h15m' will fail to parse.
	private Optional<Duration> parseDuration(String input) {
		Pattern durationPattern = Pattern.compile("^\\s*(?<num>\\d{1,3})(?<unit>[mh])\\s*$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = durationPattern.matcher(input);

		if (!matcher.matches())
			return Optional.empty();

		String inputNumber = matcher.group("num");
		String inputUnit = matcher.group("unit");

		// skipping try/catch on NumberFormatException here because this capture group can only
		// contain digits (\d)!
		int durationNumber = Integer.parseInt(inputNumber);
		TemporalUnit unit;

		switch (inputUnit.toLowerCase()) {
			case "h" -> unit = ChronoUnit.HOURS;
			case "m" -> unit = ChronoUnit.MINUTES;
			default -> { // unit is invalid!
				return Optional.empty();
			}
		}

		return Optional.of(Duration.of(durationNumber, unit));
	}

	private boolean isStreamerModeActive(LuckPerms luckPerms, Player player) {
		return luckPerms.getPlayerAdapter(Player.class)
			.getMetaData(player)
			.getMetaValue(STREAMER_MODE_META_KEY, Boolean::valueOf)
			.orElse(false);
	}
}
