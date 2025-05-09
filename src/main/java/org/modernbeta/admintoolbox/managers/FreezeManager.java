package org.modernbeta.admintoolbox.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;

import java.util.*;

public class FreezeManager implements Listener {
	AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	Set<UUID> frozenPlayers = new HashSet<>();

	public void freeze(Player player) {
		frozenPlayers.add(player.getUniqueId());
	}

	public void unfreeze(OfflinePlayer player) {
		frozenPlayers.remove(player.getUniqueId());
	}

	public boolean isFrozen(OfflinePlayer player) {
		return frozenPlayers.contains(player.getUniqueId());
	}

	public List<OfflinePlayer> getFrozenPlayers() {
		// fetch all offline players by their UUID key
		return frozenPlayers.stream().map(Bukkit::getOfflinePlayer).toList();
	}

	@EventHandler
	public void onFrozenPlayerJoin(PlayerJoinEvent joinEvent) {
		Player player = joinEvent.getPlayer();
		if (!isFrozen(player)) return;

		Component adminBroadcast =
			MiniMessage.miniMessage().deserialize("<aqua><player> is still frozen!",
				Placeholder.unparsed("player", player.getName()));
		plugin.getAdminAudience().sendMessage(adminBroadcast);

		Component playerMessage =
			MiniMessage.miniMessage().deserialize("<red>You are still frozen!");
		player.sendActionBar(playerMessage);
		player.sendMessage(playerMessage);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onFrozenPlayerMove(PlayerMoveEvent moveEvent) {
		if (!isFrozen(moveEvent.getPlayer())) return;

		Location from = moveEvent.getFrom();
		Location to = moveEvent.getTo();
		boolean didMove =
			// X/Z axis movement counts as a move
			(to.getX() != from.getX()) || (to.getZ() != from.getZ())
				// No upward movement on the Y axis, you can only fall
				// (prevents glitchy state if in midair when frozen)
				|| (to.getY() > from.getY());
		if (!didMove) return;

		moveEvent.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFrozenPlayerDamage(EntityDamageByEntityEvent damageEvent) {
		boolean isDamagedPlayerFrozen = (damageEvent.getEntity() instanceof Player player) && isFrozen(player);
		boolean isDamagedByFrozenPlayer = (damageEvent.getDamager() instanceof Player player) && isFrozen(player);

		if (isDamagedPlayerFrozen || isDamagedByFrozenPlayer)
			damageEvent.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFrozenPlayerTarget(EntityTargetLivingEntityEvent targetEvent) {
		if (!(targetEvent.getTarget() instanceof Player player)) return;
		if (!isFrozen(player)) return;

		targetEvent.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFrozenPlayerPlaceBlock(BlockPlaceEvent blockEvent) {
		Player player = blockEvent.getPlayer();
		if (!isFrozen(player)) return;

		sendFrozenAlert(player);
		blockEvent.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFrozenPlayerBreakBlock(BlockBreakEvent blockEvent) {
		Player player = blockEvent.getPlayer();
		if (!isFrozen(player)) return;

		sendFrozenAlert(player);
		blockEvent.setCancelled(true);
	}

	private void sendFrozenAlert(Player player) {
		player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>You are frozen"));
	}
}
