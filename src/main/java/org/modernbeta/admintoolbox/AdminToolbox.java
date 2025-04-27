package org.modernbeta.admintoolbox;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.modernbeta.admintoolbox.admins.AdminManager;
import org.modernbeta.admintoolbox.commands.*;
import org.modernbeta.admintoolbox.tools.Freeze;

public final class AdminToolbox extends JavaPlugin implements Listener {

    static AdminToolbox instance;
    AdminManager adminManager = new AdminManager();

    Freeze freeze = new Freeze();

    @Override
    public void onEnable() {
        instance = this;
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
    public void onDisable()
    {
        adminManager.clearAdmins();
    }

    public static AdminToolbox getInstance()
    {
        return instance;
    }

    public AdminManager getAdminManager() {
        return adminManager;
    }
}
