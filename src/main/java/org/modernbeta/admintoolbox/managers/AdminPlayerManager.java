package org.modernbeta.admintoolbox.managers;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminPlayerManager implements Listener {
	private final AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	Map<UUID, ActiveAdminState> activeAdmins = new HashMap<>();

	Map<UUID, ItemStack[]> savedInventories = new HashMap<>();
	Map<UUID, TeleportHistory> teleportHistories = new HashMap<>();

	public void target(Player admin, Location location) {
		if (isActiveAdmin(admin)) {
			teleportHistories.get(admin.getUniqueId()).add(admin.getLocation().clone());
		} else {
			captureSnapshot(admin);
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

	public void reveal(Player admin) {
		if (!isActiveAdmin(admin))
			throw new RuntimeException("Tried to reveal \"" + admin.getName() + "\", who was not spectating! This is a bug.");

		// TODO: get safe location below player
		// TODO: teleport to safe location and switch to Survival mode
	}

	public void restore(Player admin) {
		if (!isActiveAdmin(admin))
			throw new RuntimeException("Tried to restore \"" + admin.getName() + "\", who was not in an admin state! This is a bug.");

		restoreSnapshot(admin);
		admin.setGameMode(GameMode.SURVIVAL);
		activeAdmins.remove(admin.getUniqueId());
	}

	private void captureSnapshot(Player admin) {
		savedInventories.put(admin.getUniqueId(), admin.getInventory().getContents());
		teleportHistories.put(admin.getUniqueId(), new TeleportHistory(admin.getLocation().clone()));

		admin.getInventory().clear();
	}

	private void restoreSnapshot(Player admin) {
		UUID uuid = admin.getUniqueId();
		TeleportHistory teleportHistory = teleportHistories.get(uuid);

		ItemStack[] originalInventory = savedInventories.get(uuid);
		Location originalLocation = teleportHistory.getOriginalLocation();

		admin.teleportAsync(originalLocation).thenAccept((didTeleport) -> {
			if (!didTeleport) {
				admin.sendRichMessage("Error: Could not teleport you back to your original location.");
				throw new RuntimeException("Did not teleport \"" + admin.getName() + "\" to their original location. This is a bug!");
			}

			admin.getInventory().setContents(originalInventory);
		});

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

	public enum ActiveAdminState {
		SPECTATING, REVEALED;
	}

	public static class TeleportHistory {
		ArrayList<Location> locationHistory = new ArrayList<>();
		int cursor = 0;

		public TeleportHistory(Location originalLocation) {
			add(originalLocation);
		}

		@Nullable
		public Location goBack() {
			if (cursor > 0) {
				cursor -= 1;
				return locationHistory.get(cursor);
			}
			return null;
		}

		@Nullable
		public Location goForward() {
			if (cursor < (locationHistory.size() - 1)) {
				cursor += 1;
				return locationHistory.get(cursor);
			}
			return null;
		}

		@NotNull
		Location getOriginalLocation() {
			return locationHistory.getFirst();
		}

		void add(Location location) {
			if (cursor < locationHistory.size()) {
				locationHistory.subList(cursor, locationHistory.size()).clear();
			}

			locationHistory.add(location);
			cursor = locationHistory.size() - 1;
		}
	}
}
