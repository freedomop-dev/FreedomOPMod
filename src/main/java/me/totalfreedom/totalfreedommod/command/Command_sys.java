package me.totalfreedom.totalfreedommod.command;

import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.rank.Title;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sun.security.krb5.Config;

import java.util.Date;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Manage admins in game.", usage = "/<command> <add | remove> <username>", aliases = "system")
public class Command_sys extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String label, String[] args, boolean senderIsConsole)
    {
        if (args.length == 0 || args.length > 2)
        {
            sender.sendMessage(ChatColor.RED + "Correct usage: /sys <add | remove> <player");
            return true;
        }

        final Player player = Bukkit.getServer().getPlayer(args[0]);
        if (!ConfigEntry.SYSTEM_ADMINS.getList().contains(sender.getName()))
        {
            sender.sendMessage(ChatColor.RED + "You have no permission to use this command.");
            return true;
        }
        if (isConsole())
        {
            sender.sendMessage("Please use /saconfig instead of this command, thank you.");
            return true;
        }

          if (args[0].equalsIgnoreCase("add"))
          {
              if (player == null)
              {
                  sender.sendMessage(PLAYER_NOT_FOUND);
                  return true;
              }
              if (plugin.al.isAdmin(player))
              {
                  sender.sendMessage("That player is currently an administrator.");
                  return true;
              }

              FUtil.adminAction(sender.getName(), "Adding " + player.getName() + " to the admin list", true);
              plugin.al.addAdmin(new Admin(player));

              Admin a = plugin.al.getAdmin(player);

              a.setActive(true);
              a.setLastLogin(new Date());

              plugin.rm.updateDisplay(player);

              plugin.al.save();
              plugin.al.updateTables();
              return true;
          }
          if (args[0].equalsIgnoreCase("remove"))
          {
              if (player == null)
              {
                  sender.sendMessage(PLAYER_NOT_FOUND);
                  return true;
              }
              Admin a = plugin.al.getAdmin(player);
              if (a == null)
              {
                  sender.sendMessage(ChatColor.RED + "Player is not even an administrator.");
                  return true;
              }

              FUtil.adminAction(sender.getName(), "Removing " + player.getName() + " from the admin list", true);
              plugin.al.removeAdmin(a);
              a.setActive(false);
              plugin.rm.updateDisplay(player);
              plugin.al.save();
              plugin.al.updateTables();
              return true;
          }
        //
        return true;
    }

}
