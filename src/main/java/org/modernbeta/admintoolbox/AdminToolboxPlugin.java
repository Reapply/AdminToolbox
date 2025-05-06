package org.modernbeta.admintoolbox;

import org.bukkit.plugin.java.JavaPlugin;
import org.modernbeta.admintoolbox.commands.*;
import org.modernbeta.admintoolbox.managers.AdminManager;
import org.modernbeta.admintoolbox.managers.FreezeManager;

@SuppressWarnings("UnstableApiUsage")
public class AdminToolboxPlugin extends JavaPlugin {
    static AdminToolboxPlugin instance;

	AdminManager adminManager;
    FreezeManager freezeManager;

	PermissionAudience broadcastAudience;

	private static final String BROADCAST_AUDIENCE_PERMISSION = "admintoolbox.broadcast.receive";
	public static final String BROADCAST_EXEMPT_PERMISSION = "admintoolbox.broadcast.exempt";

    @Override
    public void onEnable() {
        instance = this;

		this.adminManager = new AdminManager();
        this.freezeManager = new FreezeManager();

		this.broadcastAudience = new PermissionAudience(BROADCAST_AUDIENCE_PERMISSION);

        getServer().getPluginManager().registerEvents(adminManager, this);
        getServer().getPluginManager().registerEvents(freezeManager, this);

		getCommand("target").setExecutor(new TargetCommand());
		getCommand("reveal").setExecutor(new RevealCommand());
		getCommand("back").setExecutor(new GoBackCommand());
		getCommand("forward").setExecutor(new GoForwardCommand());
		getCommand("freeze").setExecutor(new FreezeCommand());
		getCommand("unfreeze").setExecutor(new UnfreezeCommand());
		getCommand("yell").setExecutor(new YellCommand());

        getLogger().info(String.format("Enabled %s", getPluginMeta().getDisplayName()));
    }

    @Override
    public void onDisable() {
		adminManager.restoreAll();
		// TODO: unfreeze all frozen players

        getLogger().info(String.format("Disabled %s", getPluginMeta().getDisplayName()));
    }

	public static AdminToolboxPlugin getInstance() {
		return instance;
	}

	public AdminManager getAdminManager() {
		return adminManager;
	}

	public FreezeManager getFreezeManager() {
		return freezeManager;
	}

	public PermissionAudience getAdminAudience() {
		return broadcastAudience;
	}
}
