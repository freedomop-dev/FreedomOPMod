package me.totalfreedom.totalfreedommod.command;

import java.util.Date;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.banning.Ban;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.punishments.Punishment;
import me.totalfreedom.totalfreedommod.punishments.PunishmentType;
import me.totalfreedom.totalfreedommod.rank.Rank;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.pravian.aero.util.Ips;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@CommandPermissions(level = Rank.OP, source = SourceType.ONLY_IN_GAME)
@CommandParameters(description = "Manage admins in game.", usage = "/<command> <add | remove> <username>", aliases = "system")
public class Command_sys extends FreedomCommand
{
    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String label, String[] args, boolean senderIsConsole)
    {
        if (!ConfigEntry.SERVER_SYSTEM_ADMINS.getList().contains(sender.getName()) || !ConfigEntry.SERVER_OWNERS.getList().contains(sender.getName()) || plugin.al.isAdminImpostor(playerSender))
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
        
        if (args[0].equalsIgnoreCase("doom"))
        {
         if (args.length != 1)
        {
            return false;
        }

        if (player == null)
        {
            sender.sendMessage(FreedomCommand.PLAYER_NOT_FOUND);
            return true;
        }

        FUtil.adminAction(sender.getName(), "Casting oblivion over " + player.getName(), true);
        FUtil.bcastMsg(player.getName() + " will be completely obliviated!", ChatColor.RED);

        final String ip = player.getAddress().getAddress().getHostAddress().trim();

        // Remove from admin
        Admin admin = getAdmin(player);
        if (admin != null)
        {
            FUtil.adminAction(sender.getName(), "Removing " + player.getName() + " from the admin list", true);
            admin.setActive(false);
            plugin.al.save();
            plugin.al.updateTables();
            if (plugin.dc.enabled && ConfigEntry.DISCORD_ROLE_SYNC.getBoolean())
            {
                plugin.dc.syncRoles(admin);
            }
        }

        // Remove from whitelist
        player.setWhitelisted(false);

        // Deop
        player.setOp(false);

        // Ban player
        Ban ban = Ban.forPlayer(player, sender);
        ban.setReason("&cFUCKOFF");
        for (String playerIp : plugin.pl.getData(player).getIps())
        {
            ban.addIp(playerIp);
        }
        plugin.bm.addBan(ban);

        // Set gamemode to survival
        player.setGameMode(GameMode.SURVIVAL);

        // Clear inventory
        player.closeInventory();
        player.getInventory().clear();

        // Ignite player
        player.setFireTicks(10000);

        // Generate explosion
        player.getWorld().createExplosion(player.getLocation(), 0F, false);

        // Shoot the player in the sky
        player.setVelocity(player.getVelocity().clone().add(new Vector(0, 20, 0)));

        // Log doom
        plugin.pul.logPunishment(new Punishment(player.getName(), Ips.getIp(player), sender.getName(), PunishmentType.DOOM, null));

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                // strike lightning
                player.getWorld().strikeLightningEffect(player.getLocation());

                // kill (if not done already)
                player.setHealth(0.0);
            }
        }.runTaskLater(plugin, 2L * 20L);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                // message
                FUtil.adminAction(sender.getName(), "Banning " + player.getName() + ", IP: " + ip, true);

                // generate explosion
                player.getWorld().createExplosion(player.getLocation(), 0F, false);

                // kick player
                player.kickPlayer(ChatColor.RED + "FUCKOFF, and get your shit together!");
            }
        }.runTaskLater(plugin, 3L * 20L);

            return true;
        }

        return true;
    }
}
