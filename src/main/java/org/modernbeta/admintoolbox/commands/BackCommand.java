package org.modernbeta.admintoolbox.commands;

import org.modernbeta.admintoolbox.admins.Admin;
import org.modernbeta.admintoolbox.admins.AdminManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BackCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args)
    {
        if (!(commandSender instanceof Player))
            return false;

        Player player = (Player) commandSender;
        if (!player.hasPermission("AdminToolbox.admin"))
            return false;

        Admin admin = AdminManager.getOnlineAdmin(player);
        if (admin == null) return false;

        admin.teleportBackwardInHistory();
        return true;
    }
}
