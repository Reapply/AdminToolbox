package org.modernbeta.admintoolbox.managers;

import net.kyori.adventure.text.minimessage.MiniMessage;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.modernbeta.admintoolbox.AdminToolboxPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeManager implements Listener {
	AdminToolboxPlugin plugin = AdminToolboxPlugin.getInstance();

	Set<UUID> frozenPlayers = new HashSet<>();

	public void freeze(Player player) {
		// TODO: maybe show a freeze effect on screen?
		frozenPlayers.add(player.getUniqueId());
	}

	public void unfreeze(OfflinePlayer player) {
		frozenPlayers.remove(player.getUniqueId());
	}

	public boolean isFrozen(OfflinePlayer player) {
		return frozenPlayers.contains(player.getUniqueId());
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
