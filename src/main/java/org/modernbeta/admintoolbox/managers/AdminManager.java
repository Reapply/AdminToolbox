package org.modernbeta.admintoolbox.managers;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;

import javax.annotation.Nullable;
import java.util.*;

public class AdminManager implements Listener {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	Map<UUID, ActiveAdminState> activeAdmins = new HashMap<>();

	Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
	Map<UUID, TeleportHistory<Location>> teleportHistories = new HashMap<>();

	public void target(Player admin, Location location, boolean appending) {
		if (!isActiveAdmin(admin)) {
			savedInventories.put(admin.getUniqueId(), admin.getInventory().getContents());
			admin.getInventory().clear();

			TeleportHistory<Location> history = new TeleportHistory<>();
			history.add(admin.getLocation().clone());
			teleportHistories.put(admin.getUniqueId(), history);
		} else if (appending) {
			TeleportHistory<Location> history = getTeleportHistory(admin);
			if (history != null) {
				history.add(admin.getLocation().clone());
			}
		}

		admin.teleportAsync(location, PlayerTeleportEvent.TeleportCause.COMMAND).thenAccept((didTeleport) -> {
			if (!didTeleport) {
				admin.sendRichMessage("<red>Error: You were not teleported!");
				return;
			}

			admin.setGameMode(GameMode.SPECTATOR);
			activeAdmins.put(admin.getUniqueId(), ActiveAdminState.SPECTATING);
		});
	}

	public void target(Player admin, Location location) {
		target(admin, location, true);
	}

	public void reveal(Player admin) {
		if (!isActiveAdmin(admin))
			throw new RuntimeException("Tried to reveal \"" + admin.getName() + "\", who was not spectating! This is a bug.");

		// TODO: get safe location below player
		// TODO: teleport to safe location and switch to Survival mode
	}

	public void restore(Player admin) {
		if (!isActiveAdmin(admin))
			throw new RuntimeException("Tried to restore \"" + admin.getName() + "\", who was not in an admin state! This is a bug.");

		UUID uuid = admin.getUniqueId();

		ItemStack[] originalInventory = savedInventories.get(uuid);

		TeleportHistory<Location> history = Objects.requireNonNull(getTeleportHistory(admin));
		Location originalLocation = history.getOriginalLocation();

		admin.teleportAsync(originalLocation).thenAccept((didTeleport) -> {
			if (!didTeleport) {
				admin.sendRichMessage("<red>Error: Could not teleport you back to your original location.");
				return;
			}

			admin.getInventory().setContents(originalInventory);
		});

		admin.setGameMode(GameMode.SURVIVAL);
		activeAdmins.remove(uuid);
		teleportHistories.remove(uuid);
		savedInventories.remove(uuid);
	}

	public boolean isSpectating(OfflinePlayer player) {
		return activeAdmins.get(player.getUniqueId()) == ActiveAdminState.SPECTATING;
	}

	public boolean isRevealed(OfflinePlayer player) {
		return activeAdmins.get(player.getUniqueId()) == ActiveAdminState.REVEALED;
	}

	public boolean isActiveAdmin(OfflinePlayer player) {
		return activeAdmins.containsKey(player.getUniqueId());
	}

	@Nullable
	public TeleportHistory<Location> getTeleportHistory(Player admin) {
		return teleportHistories.get(admin.getUniqueId());
	}

	public enum ActiveAdminState {
		SPECTATING, REVEALED;
	}

	public static class TeleportHistory<T extends Location> {
		private final List<T> backLocations = new ArrayList<>();
		private final List<T> forwardLocations = new ArrayList<>();
		private T originalLocation = null;

		public void add(T location) {
			if (originalLocation == null) {
				originalLocation = location;
			} else {
				backLocations.add(location);
				forwardLocations.clear();
			}
		}

		@Nullable
		public T goBack() {
			if (backLocations.isEmpty())
				return null;

			T previousLocation = backLocations.removeLast();
			forwardLocations.add(previousLocation);
			return previousLocation;
		}

		@Nullable
		public T goForward() {
			if (forwardLocations.isEmpty())
				return null;

			T nextLocation = forwardLocations.removeLast();
			backLocations.add(nextLocation);
			return nextLocation;
		}

		public T getOriginalLocation() {
			return originalLocation;
		}

		public boolean canGoBack() {
			return !backLocations.isEmpty();
		}

		public boolean canGoForward() {
			return !forwardLocations.isEmpty();
		}
	}
}
