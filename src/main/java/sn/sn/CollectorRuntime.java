package sn.sn;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

import static sn.sn.Sn.*;

public class CollectorRuntime implements Runnable {

    Entity entity;
    CollectorRuntime (Entity entity){
        this.entity = entity;
    }

    @Override
    public void run() {
        sendInfo("开始收集物品！");
        sendDebug("Collector 1");
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            sendDebug("Collector 2");
            if (collectors.containsKey(onlinePlayer)) {
                sendDebug("Collector 3");
                for (Collector_CE.Collector collector : collectors.get(onlinePlayer)) {
                    sendDebug("Collector 4");
                    for (Range range : collector.getRanges()) {
                        if (range.getWorld() != null) {
                            if (entity instanceof Item) {
                                if (range.isInRange(entity.getLocation())) {
                                    sendDebug("Collector 5");
                                    Chest chest = null;
                                    BlockState bs;
                                    try {
                                        Block block = range.getWorld().getBlockAt(collector.getBox());
                                        bs = block.getState();
                                        chest = (Chest) bs;
                                    } catch (Exception e) {
                                        sendError(e.getLocalizedMessage());
                                        sendError("collector在运行时无法找到箱子:");
                                        sendError("player:" + onlinePlayer.getName());
                                        sendError("collector:" + collector.getName());
                                        sendError("range:" + range);
                                        return;
                                    }
                                    HashMap<Integer, ItemStack> item_left = chest.getBlockInventory().addItem(((Item) entity).getItemStack());
                                    for (Integer i : item_left.keySet()) {
                                        range.getWorld().dropItemNaturally(entity.getLocation(), item_left.get(i));
                                    }
                                    entity.remove();
                                }
                            }
                        }
                    }
                }
            }
        }
        sendInfo("物品收集结束！");
    }


}