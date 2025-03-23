package org.modernbeta.admintoolbox.commands;

import org.bukkit.*;
import org.modernbeta.admintoolbox.admins.Admin;
import org.modernbeta.admintoolbox.admins.AdminManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TargetCommand implements CommandExecutor
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


        if (args.length == 1)
        {
            OfflinePlayer target = Bukkit.getServer().getOfflinePlayer(args[0]);
            admin.toggleAdminMode(target, null);
        }
        else if (args.length == 3)
        {
            Location tpLocation = new Location(player.getWorld(), Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            admin.toggleAdminMode(null, tpLocation);
        }
        else if (args.length == 4)
        {
            // standardize name
            String worldName = args[3];
            if (args[3].equalsIgnoreCase("nether") || args[3].equalsIgnoreCase("hell")) {
                worldName = "world_nether";
            } else if (args[3].equalsIgnoreCase("overworld")) {
                worldName = "world";
            }

            // ensure world is found
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage(ChatColor.RED + "This world does not exist.");
                return true;
            }

            Location tpLocation = new Location(Bukkit.getWorld(worldName), Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            admin.toggleAdminMode(null, tpLocation);
        }
        else if (args.length > 4)
        {
            player.sendMessage(ChatColor.RED + "Usage: /admin <player> or /admin <x> <y> <z> <world_name(optional)>");
        }
        else
        {
            admin.toggleAdminMode(null, null);
        }

        return true;
    }
}
