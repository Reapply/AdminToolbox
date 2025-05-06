package org.modernbeta.admintoolbox.managers.admin;

import de.bluecolored.bluemap.api.BlueMapAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AdminState {
	final UUID playerId;

	Status status = Status.SPECTATING;

	private TeleportHistory<Location> teleportHistory;
	private ItemStack[] savedInventory;

	@Nullable
	Boolean savedMapVisibility;

	private AdminState(UUID playerId, TeleportHistory<Location> teleportHistory, ItemStack[] inventory, CompletableFuture<Boolean> mapVisibilityFuture) {
		this.playerId = playerId;
		this.teleportHistory = teleportHistory;
		this.savedInventory = inventory;
		mapVisibilityFuture.thenAccept((mapVisibility) -> {
			this.savedMapVisibility = mapVisibility;
		});
	}

	@SuppressWarnings("UnstableApiUsage")
	static AdminState forPlayer(Player player) {
		CompletableFuture<Boolean> mapVisibilityFuture = new CompletableFuture<>();

		BlueMapAPI.getInstance().ifPresent((blueMap) -> {
			mapVisibilityFuture.complete(blueMap.getWebApp().getPlayerVisibility(player.getUniqueId()));
			blueMap.getWebApp().setPlayerVisibility(player.getUniqueId(), false);
		});

		return new AdminState(
			player.getUniqueId(),
			new TeleportHistory<>(player.getLocation().clone()),
			player.getInventory().getContents(),
			mapVisibilityFuture
		);
	}

	public Status getStatus() {
		return this.status;
	}

	protected Status setStatus(Status newStatus) {
		return this.status = newStatus;
	}

	ItemStack[] getSavedInventory() {
		return this.savedInventory;
	}

	public TeleportHistory<Location> getTeleportHistory() {
		return this.teleportHistory;
	}

	Optional<Boolean> getSavedMapVisibility() {
		return Optional.ofNullable(this.savedMapVisibility);
	}

	public enum Status {
		SPECTATING, REVEALED;
	}

	public static class TeleportHistory<T extends Location> {
		private final List<T> backLocations = new ArrayList<>();
		private final List<T> forwardLocations = new ArrayList<>();
		private T originalLocation = null;

		public TeleportHistory(T originalLocation) {
			this.originalLocation = originalLocation;
		}

		public void add(T location) {
			backLocations.add(location);
			forwardLocations.clear();
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
	}
}
