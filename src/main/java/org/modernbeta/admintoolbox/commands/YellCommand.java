package org.modernbeta.admintoolbox.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;

import java.util.Arrays;
import java.util.Optional;

public class YellCommand implements CommandExecutor {
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

		int separatorIndex = fullMessage.indexOf('|');
		if (separatorIndex != -1) {
			String titleText = fullMessage.substring(0, separatorIndex).trim();
			String subtitleText = fullMessage.substring(separatorIndex + 1).trim();

			titleComponent = Component.text(titleText, NamedTextColor.RED);
			if (!subtitleText.isEmpty())
				subtitleComponent = Component.text(subtitleText, NamedTextColor.RED);
		} else {
			titleComponent = Component.text(fullMessage, NamedTextColor.RED);
		}

		Component feedbackSubtitle = Component.empty();

		if (subtitleComponent != null) {
			feedbackSubtitle = feedbackSubtitle
				.appendNewline()
				.append(subtitleComponent);
		}

		sender.sendRichMessage("<gold>Yelled at <target>:\n<red><title><subtitle>",
			Placeholder.unparsed("target", target.getName()),
			Placeholder.component("title", titleComponent),
			Placeholder.component("subtitle", feedbackSubtitle)
		);

		Title targetTitle = Title.title(
			titleComponent,
			subtitleComponent == null ? Component.empty() : subtitleComponent
		);

		target.showTitle(targetTitle);

		return true;
	}
}
