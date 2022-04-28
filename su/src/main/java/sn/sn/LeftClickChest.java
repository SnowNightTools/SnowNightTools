package sn.sn;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class LeftClickChest implements Listener {


    @EventHandler
    public void LeftClickChest(PlayerInteractEvent leftc) {
        Player Clicker = leftc.getPlayer();
        if(leftc.getClickedBlock().getType()== Material.CHEST && leftc.getAction()== Action.LEFT_CLICK_BLOCK){
            Clicker.sendMessage("已选好箱子");

        }

    }

}