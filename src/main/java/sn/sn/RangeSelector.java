package sn.sn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static sn.sn.Sn.*;

public class RangeSelector implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void selectEvent(PlayerInteractEvent event){
        if(event.getItem()==null)return;
        ItemStack hoe = new ItemStack(Material.WOODEN_HOE);
        if(!Objects.equals(event.getItem(), hoe))return;
        try {
            if(event.getAction()== Action.LEFT_CLICK_BLOCK){
                Location t = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                start_point.put(event.getPlayer(), t);
                if(t.getWorld()!=null)
                event.getPlayer().sendMessage(ChatColor.GREEN+"[Sn]你的第一个点设置为"+t.getWorld().getName()+",("+t.getX()+","+t.getY()+","+t.getZ()+")");
            } else if(event.getAction()==Action.RIGHT_CLICK_BLOCK){
                Location t = Objects.requireNonNull(event.getClickedBlock()).getLocation();
                end_point.put(event.getPlayer(), t);
                if(t.getWorld()!=null)
                event.getPlayer().sendMessage(ChatColor.GREEN+"[Sn]你的第二个点设置为"+t.getWorld().getName()+",("+t.getX()+","+t.getY()+","+t.getZ()+")");
            }
        } catch (Exception e) {
            sendError(e.getLocalizedMessage());
        }
    }


}
