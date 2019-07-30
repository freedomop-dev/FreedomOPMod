package me.totalfreedom.totalfreedommod.command;

import java.util.Date;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Manage admins in game.", usage = "/<command> <add | remove> <username>", aliases = "system")
public class Command_sys extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String label, String[] args, boolean senderIsConsole)
    {
        if (!ConfigEntry.SERVER_SYSTEM_ADMINS.getList().contains(sender.getName()) || plugin.al.isAdminImpostor(playerSender))
        {
            sender.sendMessage(ChatColor.RED + "You have no permission to use this command.");
            return true;
        }

        if (args.length != 2)
        {
            return false;
        }

        final Player player = getPlayer(args[0]);

        if (player == null)
        {
            msg(PLAYER_NOT_FOUND);
            return true;
        }

        if (args[0].equalsIgnoreCase("add"))
        {
            if (plugin.al.isAdmin(player))
            {
                sender.sendMessage("That player is currently an administrator.");
                return true;
            }

            FUtil.adminAction(sender.getName(), "Adding " + player.getName() + " to the admin list", true);

            plugin.al.addAdmin(new Admin(player));
            Admin admin = plugin.al.getAdmin(player);

            admin.setActive(true);
            admin.setLastLogin(new Date());
            plugin.rm.updateDisplay(player);
            plugin.al.save();
            plugin.al.updateTables();
            return true;
        }

        if (args[0].equalsIgnoreCase("remove"))
        {
            Admin admin = plugin.al.getAdmin(player);

            if (admin == null)
            {
                sender.sendMessage(ChatColor.RED + "Player is not even an administrator.");
                return true;
            }

            FUtil.adminAction(sender.getName(), "Removing " + player.getName() + " from the admin list", true);
            admin.setActive(false);
            plugin.rm.updateDisplay(player);
            plugin.al.save();
            plugin.al.updateTables();
            return true;
        }

        return true;
    }
}
