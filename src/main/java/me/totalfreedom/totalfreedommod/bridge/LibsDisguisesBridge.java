package me.totalfreedom.totalfreedommod.bridge;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.totalfreedom.disguise.DisguiseBlocker;
import me.totalfreedom.totalfreedommod.FreedomService;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import me.totalfreedom.totalfreedommod.util.FLog;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LibsDisguisesBridge extends FreedomService
{

    private LibsDisguises libsDisguisesPlugin = null;

    public LibsDisguisesBridge(TotalFreedomMod plugin)
    {
        super(plugin);
    }

    @Override
    protected void onStart()
    {
    }

    @Override
    protected void onStop()
    {
    }

    public LibsDisguises getLibsDisguisesPlugin()
    {
        if (libsDisguisesPlugin == null)
        {
            try
            {
                final Plugin libsDisguises = server.getPluginManager().getPlugin("LibsDisguises");
                if (libsDisguises != null)
                {
                    if (libsDisguises instanceof LibsDisguises)
                    {
                        libsDisguisesPlugin = (LibsDisguises) libsDisguises;
                    }
                }
            }
            catch (Exception ex)
            {
                FLog.severe(ex);
            }
        }

        return libsDisguisesPlugin;
    }

    public Boolean isDisguised(Player player)
    {
        try
        {
            final LibsDisguises libsDisguises = getLibsDisguisesPlugin();
            if (libsDisguises != null)
            {
                return DisguiseAPI.isDisguised(player);
            }
        }
        catch (Exception ex)
        {
            FLog.severe(ex);
        }
        return null;
    }

    public void undisguiseAll(boolean admins)
    {
        try
        {
            final LibsDisguises libsDisguises = getLibsDisguisesPlugin();

            if (libsDisguises == null)
            {
                return;
            }

            for (Player player : server.getOnlinePlayers())
            {
                if (DisguiseAPI.isDisguised(player))
                {
                    if (!admins && plugin.al.isAdmin(player))
                    {
                        continue;
                    }
                    DisguiseAPI.undisguiseToAll(player);
                }
            }
        }
        catch (Exception ex)
        {
            FLog.severe(ex);
        }
    }

    public boolean isDisguisesEnabled()
    {
        return DisguiseBlocker.enabled;
    }

    public void setDisguisesEnabled(boolean state)
    {
        final LibsDisguises libsDisguises = getLibsDisguisesPlugin();

        if (libsDisguises == null)
        {
            return;
        }

        libsDisguises.toggleUsability(state);
    }

    public boolean isEnabled()
    {
        final LibsDisguises libsDisguises = getLibsDisguisesPlugin();

        return libsDisguises != null && libsDisguises.isEnabled();
    }
}