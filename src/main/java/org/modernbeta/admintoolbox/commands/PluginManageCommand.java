package org.modernbeta.admintoolbox.commands;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;

import java.util.ArrayList;
import java.util.List;

public class PluginManageCommand implements CommandExecutor, TabCompleter {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	private static final String MANAGE_COMMAND_PERMISSION = "admintoolbox.manage";
	private static final String RELOAD_CONFIG_PERMISSION = "admintoolbox.manage.reload";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.hasPermission(MANAGE_COMMAND_PERMISSION))
			return false; // Bukkit should handle this for us, just a sanity-check

		switch (args.length) {
			case 0 -> {
				//noinspection UnstableApiUsage
				sender.sendRichMessage(
					"<gold>Hello from AdminToolbox version <yellow><version></yellow>!",
					Placeholder.unparsed("version", plugin.getPluginMeta().getVersion())
				);
			}
			case 1 -> {
				if (args[0].equalsIgnoreCase("reload")) {
					if (!sender.hasPermission(RELOAD_CONFIG_PERMISSION)) {
						sender.sendRichMessage("<red>You don't have permission to do that!");
						return true;
					}

					plugin.reloadConfig();
					plugin.saveConfig();
					sender.sendRichMessage("<gold>AdminToolbox configuration has been reloaded.");
				} else {
					sender.sendRichMessage("<red>Unknown action '<action>'!",
						Placeholder.unparsed("action", args[0].toLowerCase()));
				}
			}
		}

		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		switch (args.length) {
			case 1 -> {
				String partialInput = args[0].toLowerCase();
				List<String> supportedActions = new ArrayList<>();

				if (sender.hasPermission(RELOAD_CONFIG_PERMISSION)) supportedActions.add("reload");

				return supportedActions.stream()
					.filter((action) -> action.startsWith(partialInput))
					.toList();
			}
			default -> {
				return List.of();
			}
		}
	}
}
