package org.modernbeta.admintoolbox;

import de.bluecolored.bluemap.api.BlueMapAPI;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.modernbeta.admintoolbox.admins.AdminManager;
import org.modernbeta.admintoolbox.commands.*;
import org.modernbeta.admintoolbox.tools.Freeze;

import javax.annotation.Nullable;

public final class AdminToolboxPlugin extends JavaPlugin implements Listener {
    private static AdminToolboxPlugin instance;

    @NotNull
    AdminManager adminManager = new AdminManager();
    @NotNull
    Freeze freeze = new Freeze();

    @Nullable
    public BlueMapAPI blueMap = null;

    @Override
    public void onEnable() {
        instance = this;

        BlueMapAPI.onEnable(mapAPI -> {
            getLogger().fine("BlueMap API is enabled, storing its instance");
            this.blueMap = mapAPI;
        });

        getServer().getPluginManager().registerEvents(adminManager, this);
        getServer().getPluginManager().registerEvents(freeze, this);

        getCommand("target").setExecutor(new TargetCommand());
        getCommand("reveal").setExecutor(new RevealCommand());
        getCommand("back").setExecutor(new BackCommand());
        getCommand("forward").setExecutor(new ForwardCommand());
        getCommand("freeze").setExecutor(new FreezeCommand());
        getCommand("release").setExecutor(new ReleaseCommand());
        getCommand("yell").setExecutor(new YellCommand());
    }

    @Override
    public void onDisable() {
        adminManager.clearAdmins();
        this.blueMap = null;
    }

    public static AdminToolboxPlugin getInstance() {
        return instance;
    }

    public AdminManager getAdminManager() {
        return adminManager;
    }
}
