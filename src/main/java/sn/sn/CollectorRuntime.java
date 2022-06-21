package sn.sn;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

import static sn.sn.Sn.collectors;
import static sn.sn.Sn.sendError;

public class CollectorRuntime extends Thread {

    @Override
    public void run() {
        while(true){
            for (World world : Bukkit.getWorlds())
                for (Entity entity : world.getEntities()) {
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        if(collectors.containsKey(onlinePlayer)){
                            for (Collector_CE.Collector collector : collectors.get(onlinePlayer)) {
                                for (Range range : collector.getRanges()) {
                                    if(range.getWorld()!=null){
                                        if(entity instanceof Item){
                                            if(range.isInRange(entity.getLocation())){
                                                Chest chest = null;
                                                try {
                                                    chest = (Chest) range.getWorld().getBlockAt(collector.getBox());
                                                } catch (Exception e) {
                                                    sendError("collector在运行时无法找到箱子:");
                                                    sendError("player:"+onlinePlayer.getName());
                                                    sendError("collector:"+collector.getName());
                                                    sendError("range:"+range);
                                                }
                                                assert chest != null;
                                                HashMap<Integer, ItemStack> item_left = chest.getBlockInventory().addItem(((Item) entity).getItemStack());
                                                for (Integer i : item_left.keySet()) {
                                                    range.getWorld().dropItemNaturally(entity.getLocation(),item_left.get(i));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            try {
                sleep(5000);
            } catch (InterruptedException e) {
                sendError(e.getLocalizedMessage());
            }
        }
    }
}
