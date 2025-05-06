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

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class TargetCommand implements CommandExecutor {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	private static final String TARGET_COMMAND_PERMISSION = "admintoolbox.target";

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!sender.hasPermission(TARGET_COMMAND_PERMISSION))
			return false; // Bukkit should handle this for us, just a sanity-check
		if (!(sender instanceof Player player)) {
			sender.sendRichMessage("<red>Error: You must be a player to use this command.");
			return false;
		}

		AtomicReference<String> targetLabel = new AtomicReference<>();
		CompletableFuture<Location> targetLocationFuture = new CompletableFuture<>();

		switch (args.length) {
			case 0 -> {
				if (plugin.getAdminManager().isSpectating(player)) {
					plugin.getAdminManager().restore(player);
				} else {
					plugin.getAdminManager().target(player, player.getLocation());
				}
			}
			case 1 -> {
				String targetPlayerName = args[0];

				Bukkit.getAsyncScheduler().runNow(plugin, (task) -> {
					try {
						OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetPlayerName);
						targetLabel.set(offlineTarget.getName());
						if (!offlineTarget.isOnline()) {
							targetLabel.set(targetLabel.get() + " (offline player)");
						}
						targetLocationFuture.complete(offlineTarget.getLocation());
					} catch (Exception e) {
						targetLocationFuture.completeExceptionally(e);
					}
				});
			}
			case 2 -> {
				try {
					int x = Integer.parseInt(args[0]);
					int z = Integer.parseInt(args[1]);

					CompletableFuture<Location> highestLocationFuture = getHighestLocation(player.getWorld(), x, z);
					highestLocationFuture.thenAccept((highestLocation) -> {
						targetLabel.set(prettifyCoordinates(highestLocation));
						targetLocationFuture.complete(highestLocation);
					});
				} catch (Exception e) {
					targetLocationFuture.completeExceptionally(e);
				}
			}
			case 3 -> {
				try {
					int x = Integer.parseInt(args[0]);
					int y = Integer.parseInt(args[1]);
					int z = Integer.parseInt(args[2]);

					Location targetLocation = new Location(
						player.getWorld(), x, y, z,
						player.getYaw(), player.getPitch()
					);
					targetLabel.set(prettifyCoordinates(targetLocation));
					targetLocationFuture.complete(targetLocation);
				} catch (NumberFormatException e) {
					// try getting highest x/z at provided world if we couldn't parse all the coords
					try {
						int x = Integer.parseInt(args[0]);
						int z = Integer.parseInt(args[1]);

						CompletableFuture<Location> highestLocationFuture = getHighestLocation(Objects.requireNonNull(resolveWorld(args[2])), x, z);
						highestLocationFuture.thenAccept((highestLocation) -> {
							targetLabel.set(prettifyCoordinates(highestLocation));
							targetLocationFuture.complete(highestLocation);
						});
					} catch (Exception ex) {
						targetLocationFuture.completeExceptionally(ex);
					}
				}
			}
			case 4 -> {
				try {
					int x = Integer.parseInt(args[0]);
					int y = Integer.parseInt(args[1]);
					int z = Integer.parseInt(args[2]);
					World world = Objects.requireNonNull(resolveWorld(args[3]));

					Location targetLocation = new Location(world, x, y, z);
					targetLabel.set(prettifyCoordinates(targetLocation));
					targetLocationFuture.complete(targetLocation);
				} catch (Exception e) {
					targetLocationFuture.completeExceptionally(e);
				}
			}
			default -> {
				return false;
			}
		}

		targetLocationFuture.thenAccept((location -> {
			plugin.getAdminManager().target(player, location);
			sender.sendRichMessage(
				"<gold>Spectating at <target>",
				Placeholder.unparsed("target", targetLabel.get())
			);

			if (!sender.hasPermission(AdminToolboxPlugin.BROADCAST_EXEMPT_PERMISSION)) {
				PermissionAudience adminAudience = plugin.getAdminAudience()
					.excluding(player);
				adminAudience
					.sendMessage(MiniMessage.miniMessage().deserialize(
						"<gold><admin> is spectating <target>",
						Placeholder.unparsed("admin", sender.getName()),
						Placeholder.unparsed("target", targetLabel.get())
					));
			}
		})).exceptionally(ex -> {
			sender.sendRichMessage("<red>Error: Couldn't use that location.");
			return null;
		});

		return true;
	}

	private @NotNull CompletableFuture<Location> getHighestLocation(World world, int x, int z) {
		CompletableFuture<Location> locationFuture = new CompletableFuture<>();
		Location taskLocation = new Location(world, x, 0, z);

		Bukkit.getRegionScheduler().run(plugin, taskLocation, (task) -> {
			Block highestYBlock = world.getHighestBlockAt(x, z);
			locationFuture.complete(highestYBlock.getLocation().add(0, 1.1, 0));
		});

		return locationFuture;
	}

	private World resolveWorld(@NotNull String worldName) {
		@Nullable World exactWorld = Bukkit.getWorld(worldName);
		if (exactWorld != null) {
			return exactWorld;
		}

		World defaultWorld = Bukkit.getWorlds().getFirst();
		// backwards-compatibility: resolve "overworld" to default world
		// TODO: could replace with a more robust first-world-with-type method
		// TODO: 	but on a large majority of servers this will suffice.
		if (worldName.equalsIgnoreCase("overworld")) return defaultWorld;

		// look up world by short name (like 'nether' for `world_nether`)
		for (World world : Bukkit.getWorlds()) {
			if (getShortWorldName(world).equalsIgnoreCase(worldName)) return world;
		}

		return null;
	}

	private String getShortWorldName(World world) {
		World defaultWorld = Bukkit.getWorlds().getFirst();

		if (world.getName().equals(defaultWorld.getName())) return world.getName();

		// get name without `world_` (+1 accounts for the underscore)
		return world.getName().substring(defaultWorld.getName().length() + 1);
	}

	private String prettifyCoordinates(Location location) {
		return String.format(
			"%s, %s, %s (%s)",
			location.getBlockX(),
			location.getBlockY(),
			location.getBlockZ(),
			getShortWorldName(location.getWorld())
		);
	}
}
