package me.totalfreedom.totalfreedommod.blocking;

import me.totalfreedom.totalfreedommod.FreedomService;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SignBlocker extends FreedomService
{

    public SignBlocker(TotalFreedomMod plugin)
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPlaceBlock(BlockPlaceEvent event)
    {

        final Player player = event.getPlayer();
        if (event.getBlock().getType().equals(Tag.SIGNS.getValues()))
        {

            ItemStack sign = event.getItemInHand();
            net.minecraft.server.v1_14_R1.ItemStack nmsSign = CraftItemStack.asNMSCopy(sign);
            NBTTagCompound compound = (nmsSign.hasTag()) ? nmsSign.getTag() : new NBTTagCompound();
            NBTTagCompound bet = compound.getCompound("BlockEntityTag");
            String line1 = bet.getString("Text1");
            String line2 = bet.getString("Text2");
            String line3 = bet.getString("Text3");
            String line4 = bet.getString("Text4");
            if (line1.contains("run_command") || line2.contains("run_command") || line3.contains("run_command") || line4.contains("run_command"))
            {
                player.sendMessage(ChatColor.GRAY + "You are not allowed to place command signs.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractSign(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }

        if (event.getClickedBlock() != null && Tag.SIGNS.getValues().contains(event.getClickedBlock().getType()))
        {
            event.setCancelled(true);
        }
    }
}
