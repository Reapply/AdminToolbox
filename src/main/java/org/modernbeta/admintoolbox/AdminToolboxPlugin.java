package org.modernbeta.admintoolbox;

import org.bukkit.plugin.java.JavaPlugin;
import org.modernbeta.admintoolbox.commands.FreezeCommand;
import org.modernbeta.admintoolbox.commands.TargetCommand;
import org.modernbeta.admintoolbox.commands.UnfreezeCommand;
import org.modernbeta.admintoolbox.managers.AdminPlayerManager;
import org.modernbeta.admintoolbox.managers.PlayerFreezeManager;

@SuppressWarnings("UnstableApiUsage")
public class AdminToolboxPlugin extends JavaPlugin {
    static AdminToolboxPlugin instance;

	AdminPlayerManager adminManager;
    PlayerFreezeManager freezeManager;

	PermissionAudience broadcastAudience;

	private static final String BROADCAST_AUDIENCE_PERMISSION = "admintoolbox.broadcast.receive";

    @Override
    public void onEnable() {
        instance = this;

		this.adminManager = new AdminPlayerManager();
        this.freezeManager = new PlayerFreezeManager();

		this.broadcastAudience = new PermissionAudience(BROADCAST_AUDIENCE_PERMISSION);

        getServer().getPluginManager().registerEvents(freezeManager, this);

		getCommand("freeze").setExecutor(new FreezeCommand());
		getCommand("unfreeze").setExecutor(new UnfreezeCommand());
		getCommand("target").setExecutor(new TargetCommand());

        getLogger().info(String.format("Enabled %s", getPluginMeta().getDisplayName()));
    }

    @Override
    public void onDisable() {
        // TODO: reset all active admins to regular gameplay
		// TODO: unfreeze all frozen players

        getLogger().info(String.format("Disabled %s", getPluginMeta().getDisplayName()));
    }

	public static AdminToolboxPlugin getInstance() {
		return instance;
	}

	public AdminPlayerManager getAdminManager() {
		return adminManager;
	}

	public PlayerFreezeManager getFreezeManager() {
		return freezeManager;
	}

	public PermissionAudience getAdminAudience() {
		return broadcastAudience;
	}
}
