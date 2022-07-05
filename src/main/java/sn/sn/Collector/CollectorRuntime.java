package sn.sn.Collector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sn.sn.Basic.Other;
import sn.sn.Basic.SayToEveryoneThread;
import sn.sn.Range.Range;

import java.util.*;

import static sn.sn.Sn.*;

public class CollectorRuntime implements Runnable {

    final Entity entity;
    public CollectorRuntime (Entity entity){
        this.entity = entity;
    }

    public static void runCollector(boolean once) {
        Runnable clean = () -> {
            Other.sendInfo("开始收集物品！");
            rubbishes = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    CollectorRuntime cr = new CollectorRuntime(entity);
                    Bukkit.getScheduler().runTask(sn,cr);
                }
            }
            new CollectorThrowThread().start();
            Other.sendInfo("物品收集结束！");
        };
        if(once){
            Bukkit.getScheduler().runTask(sn,()-> new SayToEveryoneThread("扫地即将开始！").start());
            Bukkit.getScheduler().runTaskLater(sn, clean,200);
        } else {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(sn,()-> new SayToEveryoneThread("扫地即将开始！").start(),0,36000);
            Bukkit.getScheduler().scheduleSyncRepeatingTask(sn, clean,200,36000);
        }
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
            lore.add(ChatColor.GRAY+"从垃圾堆里捡回来的……");
            lore.add(String.valueOf(new Date().getTime()));
            lore.add("World: "+ Objects.requireNonNull(entity.getLocation().getWorld()).getName());
            lore.add("Location: ("+ (int)entity.getLocation().getX()+","+(int)entity.getLocation().getY()+","+(int)entity.getLocation().getZ()+")");
            im.setLore(lore);
            item.setItemMeta(im);
            rubbishes.add(item);
            Other.sendDebug("Collector 6");
            entity.remove();
        }
    }


}
