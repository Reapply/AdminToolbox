package org.modernbeta.admintoolbox;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public class PermissionAudience implements ForwardingAudience.Single {
	private final Predicate<Player> filter;

	public PermissionAudience(Predicate<Player> filter) {
		this.filter = filter;
	}

	public PermissionAudience(String permission) {
		this(player -> player.hasPermission(permission));
	}

	@Override
	public @NotNull Audience audience() {
		return Audience.audience(
			Bukkit.getOnlinePlayers().stream()
				.filter(filter)
				.toList()
		);
	}

	@Override
	public void sendMessage(Component message) {
		audience().sendMessage(message);
	}

	public @NotNull PermissionAudience excluding(Player... excludedPlayers) {
		Set<UUID> excludedIds = new HashSet<>(excludedPlayers.length);
		for (Player excluded : excludedPlayers) {
			excludedIds.add(excluded.getUniqueId());
		}

		Predicate<Player> excludeFilter = player ->
			!excludedIds.contains(player.getUniqueId());

		return new PermissionAudience(filter.and(excludeFilter));
	}
}
