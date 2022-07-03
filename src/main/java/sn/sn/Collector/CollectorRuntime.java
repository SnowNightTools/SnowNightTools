package sn.sn.Collector;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sn.sn.Basic.Other;
import sn.sn.Range.Range;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static sn.sn.Sn.*;

public class CollectorRuntime implements Runnable {

    final Entity entity;
    public CollectorRuntime (Entity entity){
        this.entity = entity;
    }

    @Override
    public void run() {
        if (entity instanceof Item) {
            Other.sendDebug("Collector 1");
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Other.sendDebug("Collector 2");
                if (collectors.containsKey(onlinePlayer)) {
                    Other.sendDebug("Collector 3");
                    for (Collector collector : collectors.get(onlinePlayer)) {
                        Other.sendDebug("Collector 4");
                        for (Range range : collector.getRanges()) {
                            if (range.getWorld() != null) {
                                if (range.isInRange(entity.getLocation())) {
                                    Other.sendDebug("Collector 5");
                                    Chest chest;
                                    BlockState bs;
                                    try {
                                        Block block = range.getWorld().getBlockAt(collector.getBox());
                                        bs = block.getState();
                                        chest = (Chest) bs;
                                    } catch (Exception e) {
                                        Other.sendError(e.getLocalizedMessage());
                                        Other.sendError("collector在运行时无法找到箱子:");
                                        Other.sendError("player:" + onlinePlayer.getName());
                                        Other.sendError("collector:" + collector.getName());
                                        Other.sendError("range:" + range);
                                        return;
                                    }
                                    HashMap<Integer, ItemStack> item_left = chest.getBlockInventory().addItem(((Item) entity).getItemStack());
                                    for (Integer i : item_left.keySet()) {
                                        range.getWorld().dropItemNaturally(entity.getLocation(), item_left.get(i));
                                    }
                                    entity.remove();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            ItemStack item = ((Item) entity).getItemStack();
            ItemMeta im = item.getItemMeta();
            assert im != null;
            List<String> lore = new ArrayList<>();
            if (im.hasLore()) {
                lore = im.getLore();
            }
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(String.valueOf(new Date().getTime()));
            lore.add(entity.getLocation().toString());
            im.setLore(lore);
            item.setItemMeta(im);
            rubbishes.add(item);
            Other.sendDebug("Collector 6");
            entity.remove();
        }
    }


}