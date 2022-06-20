package sn.sn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static sn.sn.Sn.*;

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
*       list:
*           - <name>
*       <name>:
*           owner: <Player name>
*           range:
*               0:
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

        if(args.length == 0)return help();
        if(args[0].equals("help")||args[0].equals("?")||args[0].equals("？"))return help();

        if(args[0].equals("create")){
            if(!(args.length==3 && args[2].equals("-g") || args.length!=2))return help();

            Player commander = (Player) sender;
            if(!startpoint.containsKey(commander)||!endpoint.containsKey(commander)){
                commander.sendMessage(ChatColor.GREEN+"请先选择两个点哦~");
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
            Location s = startpoint.get(commander), e = endpoint.get(commander);
            if(s.distance(e) > range_perm){
                sender.sendMessage(ChatColor.RED+"你选区的对角线长度（"+s.distance(e)+"）大于你的权限（"+range_perm+"）");
                return true;
            }

            if(collectors.getOrDefault(commander,new ArrayList<>()).size()+1 > amount_perm){
                sender.sendMessage(ChatColor.RED+"你拥有（"+collectors.getOrDefault(commander,new ArrayList<>()).size()+"）个收集器，而你的权限（"+amount_perm+"）不足让你继续创建一个新的收集器！");
                return true;
            }

            Collector temp = new Collector();
            temp.setOwner(commander.getUniqueId());
            temp.setName(args[1]);

            Range tr = new Range(s.getX(),s.getY(),s.getZ(),e.getX(),e.getY(),e.getZ());
            if(args.length==3)
            tr.setWorld(commander.getWorld());




        }


        return true;
    }

    private boolean help() {
        return true;
    }

    public static class Collector {

        private String name;
        private UUID owner;
        private List<Range> ranges = new ArrayList<>();

        public UUID getOwner() {
            return owner;
        }

        public void setOwner(UUID owner) {
            this.owner = owner;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Range> getRanges() {
            return ranges;
        }

        public void setRanges(List<Range> ranges) {
            this.ranges = ranges;
        }

        public void addRange(Range range) {
            this.ranges.add(range);
        }

        public void saveCollectorToYml(YamlConfiguration ymlfile,@Nullable String path){
            if(path == null) path = name;
            else path = path + "." + name;
            ymlfile.set(path+".owner",owner);
            int cnt = 1;
            for (Range range : ranges) {
                range.saveRangeToYml(ymlfile,path + "." + cnt++);
            }
            sendDebug("collector类存储完成，cnt="+cnt);
        }
    }
}
