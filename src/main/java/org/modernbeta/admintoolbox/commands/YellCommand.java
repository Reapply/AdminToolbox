package org.modernbeta.admintoolbox.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class YellCommand implements CommandExecutor, TabCompleter {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	private static final String YELL_COMMAND_PERMISSION = "admintoolbox.yell";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.hasPermission(YELL_COMMAND_PERMISSION))
			return false; // Bukkit should handle this for us, just a sanity-check
		if (args.length < 2) return false;

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null || !target.isOnline()) {
			sender.sendRichMessage("<red>Error: Player <gray><name></gray> is not online.",
				Placeholder.unparsed("name", Optional.ofNullable(target)
					.map(Player::getName)
					.orElse(args[0])
				));
			return true;
		}

		String fullMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

		Component titleComponent;
		Component subtitleComponent = null;

		// Prepare the title text components
		{
			LegacyComponentSerializer ampersandSerializer = LegacyComponentSerializer.legacyAmpersand();

			int separatorIndex = fullMessage.indexOf('|');
			if (separatorIndex != -1) {
				String titleText = fullMessage.substring(0, separatorIndex).trim();
				String subtitleText = fullMessage.substring(separatorIndex + 1).trim();

				titleComponent = Component.empty()
					.color(NamedTextColor.RED)
					.append(ampersandSerializer.deserialize(titleText));
				if (!subtitleText.isEmpty())
					subtitleComponent = Component.empty()
						.color(NamedTextColor.RED)
						.append(ampersandSerializer.deserialize(subtitleText));
			} else {
				titleComponent = Component.empty()
					.color(NamedTextColor.RED)
					.append(ampersandSerializer.deserialize(fullMessage));
			}
		}

		// Send command feedback & broadcast to other admins
		{
			Component feedbackTitle = titleComponent;

			if (subtitleComponent != null) {
				feedbackTitle = feedbackTitle
					.appendNewline()
					.append(subtitleComponent);
			}

			Component broadcastMessage = MiniMessage.miniMessage().deserialize("<gold><admin> yelled at <target>: <title>",
				Placeholder.unparsed("admin", sender.getName()),
				Placeholder.unparsed("target", target.getName()),
				Placeholder.component("title", feedbackTitle)
			);
			plugin.getAdminAudience()
				.excluding(sender, target)
				.sendMessage(broadcastMessage);

			sender.sendRichMessage("<gold>Yelled at <target>: <title>",
				Placeholder.unparsed("target", target.getName()),
				Placeholder.component("title", feedbackTitle)
			);
		}

		Title targetTitle = Title.title(
			titleComponent,
			Optional.ofNullable(subtitleComponent).orElse(Component.empty())
		);

		target.showTitle(targetTitle);

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length == 1) {
			String partialName = args[0].toLowerCase();

			return Bukkit.getOnlinePlayers().stream()
				.map(OfflinePlayer::getName)
				.filter((name) -> name.toLowerCase().startsWith(partialName))
				.toList();
		}

		return List.of();
	}
}
