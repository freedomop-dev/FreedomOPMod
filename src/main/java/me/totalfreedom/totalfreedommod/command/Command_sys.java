package me.totalfreedom.totalfreedommod.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import me.totalfreedom.totalfreedommod.admin.Admin;
import me.totalfreedom.totalfreedommod.banning.Ban;
import static me.totalfreedom.totalfreedommod.command.FreedomCommand.YOU_ARE_OP;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.masterbuilder.MasterBuilder;
import me.totalfreedom.totalfreedommod.player.FPlayer;
import me.totalfreedom.totalfreedommod.playerverification.VPlayer;
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
@CommandParameters(description = "Manage admins in game.", usage = "/<command> <setrank <username> <rank> | <add | remove | doom> <username>>", aliases = "system")
public class Command_sys extends FreedomCommand {

    @Override
    public boolean run(CommandSender sender, Player playerSender, Command cmd, String commandLabel, String[] args, boolean senderIsConsole) {
        if (!ConfigEntry.SERVER_SYSTEM_ADMINS.getList().contains(sender.getName()) && !ConfigEntry.SERVER_OWNERS.getList().contains(sender.getName()) || plugin.al.isAdminImpostor(playerSender)) {
            sender.sendMessage(ChatColor.RED + "You have no permission to use this command.");
            return true;
        }
        if (args.length < 1) {
            return false;
        }

        switch (args[0]) {
            case "setrank": {
                checkRank(Rank.SENIOR_ADMIN);

                if (args.length < 3) {
                    return false;
                }

                Rank rank = Rank.findRank(args[2]);
                if (rank == null) {
                    msg("Unknown rank: " + rank);
                    return true;
                }

                if (rank.isConsole()) {
                    msg("You cannot set players to a console rank");
                    return true;
                }

                if (!rank.isAtLeast(Rank.SUPER_ADMIN)) {
                    msg("Rank must be Super Admin or higher.", ChatColor.RED);
                    return true;
                }

                Admin admin = plugin.al.getEntryByName(args[1]);
                if (admin == null) {
                    msg("Unknown admin: " + args[1]);
                    return true;
                }

                FUtil.adminAction(sender.getName(), "Setting " + admin.getName() + "'s rank to " + rank.getName(), true);

                admin.setRank(rank);
                plugin.al.save();

                Player player = getPlayer(admin.getName());
                if (player != null) {
                    plugin.rm.updateDisplay(player);
                }

                if (plugin.dc.enabled && ConfigEntry.DISCORD_ROLE_SYNC.getBoolean()) {
                    plugin.dc.syncRoles(admin);
                }

                msg("Set " + admin.getName() + "'s rank to " + rank.getName());
                return true;
            }

            case "add": {
                if (args.length < 2) {
                    return false;
                }

                checkRank(Rank.SENIOR_ADMIN);

                // Player already an admin?
                final Player player = getPlayer(args[1]);

                if (player == null) {
                    msg(FreedomCommand.PLAYER_NOT_FOUND);
                    return true;
                }

                if (player != null && plugin.al.isAdmin(player)) {
                    msg("That player is already admin.");
                    return true;
                }

                // Find the old admin entry
                String name = player != null ? player.getName() : args[1];
                Admin admin = null;
                for (Admin loopAdmin : plugin.al.getAllAdmins().values()) {
                    if (loopAdmin.getName().equalsIgnoreCase(name)) {
                        admin = loopAdmin;
                        break;
                    }
                }

                if (plugin.pv.isPlayerImpostor(player)) {
                    msg("This player was labeled as a Player impostor and is not an admin, therefore they cannot be added to the admin list.", ChatColor.RED);
                    return true;
                }

                if (admin == null) // New admin
                {
                    if (plugin.mbl.isMasterBuilderImpostor(player)) {
                        msg("This player was labeled as a Master Builder impostor and is not an admin, therefore they cannot be added to the admin list.", ChatColor.RED);
                        return true;
                    }
                    if (player == null) {
                        msg(FreedomCommand.PLAYER_NOT_FOUND);
                        return true;
                    }

                    FUtil.adminAction(sender.getName(), "Adding " + player.getName() + " to the admin list", true);
                    plugin.al.addAdmin(new Admin(player));
                    if (player != null) {
                        plugin.rm.updateDisplay(player);
                    }

                    // Attempt to find discord account
                    if (plugin.mbl.isMasterBuilder(player)) {
                        MasterBuilder masterBuilder = plugin.mbl.getMasterBuilder(player);
                        admin.setDiscordID(plugin.mbl.getMasterBuilder(player).getDiscordID());
                    } else if (plugin.pv.getVerificationPlayer(player.getName()) != null) {
                        VPlayer vPlayer = plugin.pv.getVerificationPlayer(player.getName());
                        if (vPlayer.getDiscordId() != null) {
                            admin.setDiscordID(vPlayer.getDiscordId());
                        }
                    }
                } else // Existing admin
                {
                    FUtil.adminAction(sender.getName(), "Re-adding " + admin.getName() + " to the admin list", true);

                    if (player != null) {
                        admin.setName(player.getName());
                        admin.addIp(Ips.getIp(player));
                    }

                    // Handle master builders
                    if (!plugin.mbl.isMasterBuilder(player)) {
                        MasterBuilder masterBuilder = null;
                        for (MasterBuilder loopMasterBuilder : plugin.mbl.getAllMasterBuilders().values()) {
                            if (loopMasterBuilder.getName().equalsIgnoreCase(name)) {
                                masterBuilder = loopMasterBuilder;
                                break;
                            }
                        }

                        if (masterBuilder != null) {
                            if (player != null) {
                                masterBuilder.setName(player.getName());
                                masterBuilder.addIp(Ips.getIp(player));
                            }

                            masterBuilder.setLastLogin(new Date());

                            plugin.mbl.save();
                            plugin.mbl.updateTables();
                        }
                    }

                    admin.setActive(true);
                    admin.setLastLogin(new Date());

                    // Attempt to find discord account
                    if (plugin.mbl.isMasterBuilder(player)) {
                        MasterBuilder masterBuilder = plugin.mbl.getMasterBuilder(player);
                        admin.setDiscordID(plugin.mbl.getMasterBuilder(player).getDiscordID());
                    } else if (plugin.pv.getVerificationPlayer(admin.getName()) != null) {
                        VPlayer vPlayer = plugin.pv.getVerificationPlayer(admin.getName());
                        if (vPlayer.getDiscordId() != null) {
                            admin.setDiscordID(vPlayer.getDiscordId());
                        }
                    }

                    plugin.al.save();
                    plugin.al.updateTables();
                    if (player != null) {
                        plugin.rm.updateDisplay(player);
                    }

                    if (plugin.dc.enabled && ConfigEntry.DISCORD_ROLE_SYNC.getBoolean()) {
                        plugin.dc.syncRoles(admin);
                    }
                }

                if (player != null) {
                    final FPlayer fPlayer = plugin.pl.getPlayer(player);
                    if (fPlayer.getFreezeData().isFrozen()) {
                        fPlayer.getFreezeData().setFrozen(false);
                        msg(player.getPlayer(), "You have been unfrozen.");
                    }

                    if (!player.isOp()) {
                        player.setOp(true);
                        player.sendMessage(YOU_ARE_OP);
                    }
                    plugin.pv.removeEntry(player.getName()); // admins can't have player verification entries
                }
                return true;
            }

            case "remove": {
                if (args.length < 2) {
                    return false;
                }
                checkRank(Rank.SENIOR_ADMIN);

                Player player = getPlayer(args[1]);
                Admin admin = player != null ? plugin.al.getAdmin(player) : plugin.al.getEntryByName(args[1]);

                if (admin == null) {
                    msg("Admin not found: " + args[1]);
                    return true;
                }

                FUtil.adminAction(sender.getName(), "Removing " + admin.getName() + " from the admin list", true);
                admin.setActive(false);
                plugin.al.save();
                plugin.al.updateTables();
                if (player != null) {
                    plugin.rm.updateDisplay(player);
                }

                if (plugin.dc.enabled && ConfigEntry.DISCORD_ROLE_SYNC.getBoolean()) {
                    plugin.dc.syncRoles(admin);
                }

                return true;
            }
            case "doom": {
                if (args.length < 2) {
                    return false;
                }
                checkRank(Rank.SENIOR_ADMIN);

                Player player = getPlayer(args[1]);
                Admin admin = player != null ? plugin.al.getAdmin(player) : plugin.al.getEntryByName(args[1]);

                if (admin == null) {
                    msg("Admin not found: " + args[1]);
                    return true;
                }
                FUtil.adminAction(sender.getName(), "Casting oblivion over " + player.getName(), true);
                FUtil.bcastMsg(player.getName() + " will be completely obliviated!", ChatColor.RED);

                final String ip = player.getAddress().getAddress().getHostAddress().trim();

                // Remove from admin
                if (admin != null) {
                    FUtil.adminAction(sender.getName(), "Removing " + player.getName() + " from the admin list", true);
                    admin.setActive(false);
                    plugin.al.save();
                    plugin.al.updateTables();
                    if (plugin.dc.enabled && ConfigEntry.DISCORD_ROLE_SYNC.getBoolean()) {
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
                for (String playerIp : plugin.pl.getData(player).getIps()) {
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

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // strike lightning
                        player.getWorld().strikeLightningEffect(player.getLocation());

                        // kill (if not done already)
                        player.setHealth(0.0);
                    }
                }.runTaskLater(plugin, 2L * 20L);

                new BukkitRunnable() {
                    @Override
                    public void run() {
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

            default: {
                return false;
            }
        }
    }

    @Override
    public List<String> getTabCompleteOptions(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "setrank");
        } else if (args.length == 2) {
            if (args[0].equals("add") || args[0].equals("remove") || args[0].equals("setrank")) {
                return FUtil.getPlayerList();
            }
        } else if (args.length == 3 && args[0].equals("setrank")) {
            return Arrays.asList("super_admin", "telnet_admin", "senior_admin");
        }
        return Collections.emptyList();
    }
}
