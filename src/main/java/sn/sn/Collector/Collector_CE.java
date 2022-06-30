package sn.sn.Collector;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sn.sn.Basic.AutoSave;
import sn.sn.Range.Range;
import sn.sn.Basic.SnFileIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.pow;
import static sn.sn.Collector.CollectorThrowThread.rubbishes_folder;
import static sn.sn.Sn.*;
import static sn.sn.UI.InvOperateEvent.pg_dn;

/*
* Collector_CE 扫地机器人 与 简单收集系统 类
* 作者 LtSeed
*
* 指令：
*   使用木锄进行选区
*   /collector create <name>
*   /collector delete <name>
*   /collector list
*   /collector admin list
*   /collector set <name>
*
* 权限:
*   sn.collector.admin
*   管理员权限
*   sn.collector.amount.<amount>
*   能够设置的数量
*   sn.collector.range.<range>
*   每个的最大面积
*
* 存储：collectors.yml文件
*
*   格式：
*       amount: 1
*       list:
*           - <name>

*       <name>:
*           owner: <Player uuid>
            range_amount:
*           range:
*               1:
*                   start: <X,Y,Z>
*                   end: <X,Y,Z>

*
*
* */
public class Collector_CE implements CommandExecutor {
    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!label.equals("collector"))return false;

        if(args.length == 0)return help(sender);
        if(args[0].equals("help")||args[0].equals("?")||args[0].equals("？"))return help(sender);
        Player commander = null;
        try {
            commander = (Player) sender;
        } catch (Exception e) {
            sendError(e.getLocalizedMessage());
        }

        if(args[0].equals("create")){
            return workCollectorCreateCE(sender, args, commander);
        }

        if(args[0].equals("save")&&sender.isOp()){
            new AutoSave().start();
            return true;
        }

        if(args[0].equals("debug")&&sender.isOp()){
            return workCollectorDebugStateChangeCE();
        }

        if(args[0].equals("add")){
            if(args.length<=1)return help(sender);
            if (workCollectorAddCE(sender, args, commander)) return true;
        }

        if(args[0].equals("list")){
            return workCollectorNormalListCE(commander);
        }

        if(args[0].equals("remove")){
            if(!commander.hasPermission("sn.collector.remove"))noPermission(commander,"sn.collector.remove");
            if(args.length == 1)return help(sender);
            if (workCollectorRemoveCE(args, commander)) return true;
        }

        if (!sender.hasPermission("sn.collector.admin") && !sender.isOp()) {return help(sender);}

        if(args.length < 2) return help(sender);

        if(!args[0].equals("admin")) return help(sender);

        if(args[1].equals("list")){
            return workCollectorAdminListCE(commander);
        }

        if(args[1].equals("now")){
            return runCollectorNow(sender);
        }

        if(args.length < 3) return help(sender);

        if(args[1].equals("bins")&&args[2].equals("list")){
            return workBinListCE(sender);
        }

        if(args[1].equals("bins")&&args[2].equals("remove")){
            return workBinRemoveCE(args, commander);
        }

        if(args[1].equals("bins")&&args[2].equals("clean_file")){
            new CollectorFileCleanThread().start();
            return true;
        }

        if(args[1].equals("bins")){
            return workBinOpenCE(sender, args);
        }

        return help(sender);
    }

    private boolean workBinRemoveCE( @NotNull String[] args, Player commander) {
        if(args.length != 4) return help(commander);
        int rem_amt;
        try {
            rem_amt = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            commander.sendMessage("请输入正确的数字格式");
            return true;
        }
        bins = new ArrayList<>(bins.subList(rem_amt,bins.size()));
        new AutoSave().start();
        return true;
    }

    private boolean workBinOpenCE(@NotNull CommandSender sender,@NotNull String[] args) {
        if(bins.contains(args[2])){
            foundRubbishes(args[2], sender);
            return true;
        }
        int index;
        try {
            index = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("未能找到你描述的垃圾箱");
            return true;
        }
        foundRubbishes(bins.get(index), sender);
        return true;
    }

    private boolean workBinListCE(@NotNull CommandSender sender) {
        for (int i = 0, binSize = bins.size(); i < binSize; i++) {
            String s = bins.get(i);
            sender.sendMessage(i + ": " + s);
        }
        return true;
    }

    private boolean runCollectorNow(@NotNull CommandSender sender) {
        runCollector(true);
        sender.sendMessage("开始准备扫地！");
        return true;
    }

    private boolean workCollectorAdminListCE(Player commander) {
        for (OfflinePlayer player : collectors.keySet()) {
            commander.sendMessage(ChatColor.GREEN+"Player: "+player);
            printCollectors(commander, (Player) player);
        }
        return true;
    }

    private boolean workCollectorNormalListCE(Player commander) {
        if(!collectors.containsKey(commander)){
            commander.sendMessage("没有找到你的收集器哦~");
            return true;
        }
        commander.sendMessage("即将打印你的所有收集器的信息~");
        printCollectors(commander, commander);
        return true;
    }

    private boolean workCollectorRemoveCE(@NotNull String[] args, Player commander) {
        if(args.length == 2) {
            if (!collectors.containsKey(commander)) {
                commander.sendMessage("没有找到你的收集器哦~");
                return true;
            }
            int i;
            boolean found = false;
            for (i = 0; i < collectors.get(commander).size(); i++) {
                if (collectors.get(commander).get(i).getName().equals(args[1])) {
                    found = true;
                    break;
                }
            }
            if (found) {
                collectors.get(commander).remove(i);
                collector_names.remove(args[1]);
                commander.sendMessage(ChatColor.GREEN+"你的收集器"+ args[1]+"被删除了哦~");
            } else {
                commander.sendMessage("没有找到你的收集器哦~");
            }
            return true;
        }
        if(args.length == 3){
            if (!collectors.containsKey(commander)) {
                commander.sendMessage("没有找到你的收集器哦~");
                return true;
            }
            int i;
            boolean found = false;
            for (i = 0; i < collectors.get(commander).size(); i++) {
                if (collectors.get(commander).get(i).getName().equals(args[1])) {
                    found = true;
                    break;
                }
            }
            if (found) {
                List<Range> r = new ArrayList<>(collectors.get(commander).get(i).getRanges());
                try {
                    r.remove(Integer.parseInt(args[2])+1);
                } catch (NumberFormatException e) {
                    commander.sendMessage("请输入正确的数字格式哦~");
                }
                commander.sendMessage(ChatColor.GREEN+"你的收集器中第"+ args[2]+"个范围被删除了哦~");
                collectors.get(commander).get(i).setRanges(r);
            } else {
                commander.sendMessage("没有找到你的收集器哦~");
            }
        }
        return false;
    }

    @Nullable
    public static Range getRange(Player commander) {
        if(!start_point.containsKey(commander)){
            commander.sendMessage("你还没有设置第一个点！");
            return null;
        }
        if(!end_point.containsKey(commander)){
            commander.sendMessage("你还没有设置第二个点！");
            return null;
        }
        Location s = start_point.get(commander), e = end_point.get(commander);
        return new Range(s.getX(), s.getY(), s.getZ(), e.getX(), e.getY(), e.getZ());
    }

    private boolean workCollectorAddCE(@NotNull CommandSender sender,@NotNull String[] args, Player commander) {
        if (checkRange(commander)) return true;
        List<Collector> get = collectors.get(commander);
        for (int i = 0, getSize = get.size(); i < getSize; i++) {
            Collector collector = get.get(i);
            if (collector.getName().equals(args[1])) {
                int range_perm = 0;
                while (true) {
                    if (sender.hasPermission("sn.collector.range." + range_perm)) break;
                    if (range_perm > 15000) {
                        range_perm = 0;
                        break;
                    }
                    range_perm++;
                }
                if (sender.hasPermission("sn.collector.admin")) {
                    range_perm = 0x7FFFFFFF;
                }
                Range tr = getRange(commander);
                List<Range> temp_range = new ArrayList<>(collector.getRanges());
                temp_range.add(tr);
                double area = Range.countUnionAreaFromDifferentWorld(temp_range);
                if (area >= pow(range_perm, 3)) {
                    sender.sendMessage(ChatColor.RED + "添加区域后的体积（" + area + "）大于你的权限（" + pow(range_perm, 3) + "）");
                    return true;
                }
                collectors.get(commander).get(i).setRanges(temp_range);
                return true;
            }
        }
        return false;
    }

    private boolean workCollectorDebugStateChangeCE() {
        debug = !debug;
        config_yml.set("debug",debug);
        try {
            config_yml.save(config_file);
        } catch (IOException e) {
            sendError(e.getLocalizedMessage());
        }
        return true;
    }

    private boolean workCollectorCreateCE(@NotNull CommandSender sender, @NotNull String[] args, Player commander) {
        if(args.length!=2) return help(sender);
        if (checkRange(commander)) return true;
        if (collector_names.contains(args[1])){
            sender.sendMessage("换个名字吧，这个名字有人用过了……");
            return true;
        }

        Chest chest;
        Block target_block = commander.getTargetBlock(null,10);
        if(target_block.getType()== Material.CHEST){
            chest = (Chest) target_block.getState();
        } else {
            commander.sendMessage("请指向一个箱子！");
            return true;
        }
        int amount_perm = 0,range_perm = 0;
        while(true){
            if(sender.hasPermission("sn.collector.amount." + amount_perm))break;
            if(amount_perm >150){
                amount_perm = 0;
                break;
            }
            amount_perm++;
        }
        while(true){
            if(sender.hasPermission("sn.collector.range." + range_perm))break;
            if(range_perm > 15000){
                range_perm = 0;
                break;
            }
            range_perm++;
        }
        if(sender.hasPermission("sn.collector.admin")) {
            amount_perm = 0x7FFFFFFF;
            range_perm = 0x7FFFFFFF;
        }

        Range tr = getRange(commander);
        if(tr.getArea() > range_perm){
            sender.sendMessage(ChatColor.RED+"你选区的大小（"+tr.getArea()+"）大于你的权限（"+range_perm*range_perm*range_perm+"）");
            return true;
        }

        if(collectors.getOrDefault(commander,new ArrayList<>()).size()+1 > amount_perm){
            sender.sendMessage(ChatColor.RED+"你拥有（"+collectors.getOrDefault(commander,new ArrayList<>()).size()+"）个收集器，而你的权限（"+amount_perm+"）不足让你继续创建一个新的收集器！");
            return true;
        }

        Collector temp = new Collector();
        temp.setOwner(commander.getUniqueId());
        temp.setName(args[1]);

        tr.setWorld(commander.getWorld());
        temp.addRange(tr);

        temp.setBox(chest.getLocation());
        List<Collector> g = collectors.getOrDefault(commander, new ArrayList<>());
        g.add(temp);
        collectors.put(commander,g);
        collector_names.add(args[1]);
        sender.sendMessage("添加成功！");
        return true;
    }

    private void foundRubbishes(String time, CommandSender sender) {
        Player commander;
        try {
            commander = (Player)sender;
        } catch (Exception e) {
            return;
        }
        sendDebug(time);
        File file = new File(rubbishes_folder,time);
        YamlConfiguration ymlfile = YamlConfiguration.loadConfiguration(file);
        int amount = ymlfile.getInt("amount");
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            items.add(SnFileIO.readItemStackFromYml(ymlfile, String.valueOf(i)));
        }
        if(items.size()<=54){
            Inventory temp = Bukkit.createInventory(commander,54,"Bin-"+time);
            for (ItemStack item : items) {
                temp.addItem(item);
            }
            commander.openInventory(temp);
            return;
        }
        Inventory temp = Bukkit.createInventory(commander,54,"Bin-"+time+"-Page 1 of"+(items.size()/45 +1)+" ");
        item_temp.put(commander,items);
        for (int i = 0; i < 45; i++) {
            temp.addItem(items.get(i));
        }
        temp.setItem(53, pg_dn);
        commander.openInventory(temp);
    }

    private void printCollectors(Player commander, Player player) {
        for (Collector collector : collectors.get(player)) {
            commander.sendMessage("Name: "+collector.getName());
            commander.sendMessage("Ranges: ");
            for (Range range : collector.getRanges()) {
                commander.sendMessage("("+range.getStartX()+","+range.getStartY()+","+range.getStartZ()+")->("+range.getEndX()+","+range.getEndY()+","+range.getEndZ()+")");
            }
            commander.sendMessage("Volume: "+collector.getRangeArea());
        }
    }

    private boolean checkRange(Player commander) {
        if(!start_point.containsKey(commander)||!end_point.containsKey(commander)){
            commander.sendMessage(ChatColor.GREEN+"请先选择两个点哦~");
            return true;
        }
        if(!Objects.equals(start_point.get(commander).getWorld(), end_point.get(commander).getWorld())){
            commander.sendMessage(ChatColor.GREEN+"不要跨世界选区哦~");
            return true;
        }
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private void noPermission(Player p, String s) {
        p.sendMessage("你缺少必要的权限："+s);
    }

    private boolean help(CommandSender player) {
        player.sendMessage("help page");
        return true;
    }

}
