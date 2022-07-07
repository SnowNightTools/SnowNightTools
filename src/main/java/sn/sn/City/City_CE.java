package sn.sn.City;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import sn.sn.Basic.Other;
import sn.sn.UI.OpenUI;
import sn.sn.Range.Range;

import java.util.*;
import java.util.function.Consumer;

import static sn.sn.Ask.AskSetEvent.askSetAsync;
import static sn.sn.City.City.getCity;
import static sn.sn.Collector.Collector_CE.getRange;
import static sn.sn.UI.OpenUI.*;
import static sn.sn.Sn.*;

/*
*
*
* City 城市系统
* 作者: LtSeed
*
*
* 指令：
* /city create <name> 发起一个城市的新建，在达到三个人时正式成立城市，使用这个指令的人会成为市长，拥有管理权限。
* /city join <name> 加入一个城市，一个人只能加入一个城市，但是可以同时发很多请求，不储存请求，每次重启请求刷新。
* /city spawn 回到自己小镇的出生点。
* /city quit 退出小镇，小镇人数少于4人时不能退出小镇
* /city my 打开小镇菜单（传送点，出生点，各个成员，点击可以tpa或者warp之类的）
*
* Only For Mayor:
* /city setwarp <name> 在小镇的领土中设置传送点
* /city accept [player name] 同意一个人的加入请求，不填写[player name]，将会打开所有申请人的面板，可以在面板上处理请求。
* /city add <perm group name> <player name> 将一个特定的人添加进该权限组。
* /city set [perm group name] 设置城市对特定权限组的权限，不填写则设置城市对居民的权限（打开面板）。
* /city setspawn 设置小镇出生点
* /city loadchunk 让插件常加载脚下的方块
* /city manage 打开小镇管理面板（踢人之类的操作）
*
* Only For OP:
* /city admin remove <name> 删除一个城市，需要op操作。
* /city admin add <perm> 添加一个可以被城主设置的权限。
* /city admin 打开小镇系统管理面板
*
*
*
*
*
* */
public class City_CE implements CommandExecutor {


    public static boolean workCityWarp(Player commander, String warp, String message) {
        City temp = getCity(commander);
        if (temp!=null) {
            if(temp.getWarp(warp)!=null) {
                commander.teleport(temp.getWarp(warp));
                return true;
            }
            commander.sendMessage(message);
            return true;
        }
        commander.sendMessage("你……你的小镇呢？");
        return true;
    }

    public static boolean wordCityWarpCE( @NotNull String[] args, Player commander) {
        if(args.length==1){
            commander.sendMessage("请输入warp名！");
            return true;
        }
        return workCityWarp(commander, args[1], "你的小镇没有设置" + args[1] + "传送点！");
    }

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

        if(!label.equals("city"))return help(sender);
        if(args.length==0) return help(sender);
        if(args[0].equals("help")||args[0].equals("?")||args[0].equals("？")) return help(sender);

        if(!(sender instanceof Player)){
            return true;
        }
        Player commander = (Player) sender;


        // /city create <name> 发起一个城市的新建，在达到三个人时正式成立城市，使用这个指令的人会成为市长，拥有管理权限。
        if(args[0].equals("create")){
            return workCityCreateCE(sender, args, commander);
        }

        // /city create-give-up 放弃一个城市的新建。
        if(args[0].equals("create-give-up")){
            return workCityCreateGiveUpCE(commander);
        }

        // /city join <name> 加入一个城市，一个人只能加入一个城市，但是可以同时发很多请求，不储存请求，每次重启请求刷新。
        if(args[0].equals("join")){
            return workCityJoinCE(sender, args, commander);
        }

        // /city spawn 回到自己小镇的出生点。
        if(args[0].equals("spawn")){
            return workCityWarp(commander, "spawn", "你的小镇没有设置spawn传送点！");
        }

        // /city warp <warp> 回到自己小镇的出生点。
        if(args[0].equals("warp")){
            return wordCityWarpCE(args, commander);
        }

        // /city quit 退出小镇，小镇人数少于4人时不能退出小镇
        if(args[0].equals("quit")){
            return workCityQuitCE(commander);
        }

        // /city my 打开小镇菜单（传送点，出生点，各个成员，点击可以tpa或者warp之类的）
        // 若是Mayor打开小镇菜单， 则打开小镇管理面板
        if(args[0].equals("my")){
            City city = City.getCity(commander);
            if(city == null) return true;
            if (city.getMayor().equals(commander.getUniqueId())) {
                return openCityManageUI(commander,false);
            }
            return openMyCityUI(commander);
        }

        // Only For Mayor:
        // /city setwarp <name> 在小镇的领土中设置传送点
        if(args[0].equals("setwarp")){
            return workCityAddWarpCE(args, commander);
        }

        // /city accept [player name/Offline player uuid] 同意一个人的加入请求，不填写[player name]，将会打开所有申请人的面板，可以在面板上处理请求。
        if(args[0].equals("accept")){
            return workCityAcceptCE(sender, args, commander);
        }

        // /city perm add <perm group name> <player name> 将一个特定的人添加进该权限组，需要玩家在线。
        if (args[0].equals("perm"))
            if(args.length > 1) {
                if (args[1].equals("add")) {
                    return workPermAddPlayerCE(args, commander);
                }
            } else return help(commander);

        // /city perm set [perm group name] [perm name]反转城市对特定权限组的权限，不填写则设置城市对居民的权限（打开面板）。
        if(args[0].equals("perm")&&args[1].equals("set")) {
            return workPermGroupSetCE(sender, args, commander);
        }

        // /city range add 向小镇中添加区域 体积上限与人数（用单独的枚举类CITY_TYPE来实现）有关。
        // /city range add chunk 把身下的区块区域添加到城镇领土中。
        // /city range list 列出所有区域。
        // /city range remove <index> 向小镇中删除区域。
        if(args[0].equals("range")){
            return workCityRangeOperationCE(args, commander);
        }

        // /city setspawn 设置小镇出生点
        if(args[0].equals("setspawn")){
            return workCitySetWarpCE(commander, "spawn");
        }

        // /city loadchunk 让插件常加载脚下的方块
        if(args[0].equals("loadchunk")){
            return workCityChunkForceLoadCE(commander);
        }


        if(!args[0].equals("admin")) return help(commander);
        // Only For OP:
        if(!commander.hasPermission("sn.city.admin") || !commander.isOp()) return help(commander);


        // /city admin perm add/remove <perm> 添加/删除一个可以被城主设置的权限。
        // /city admin perm add/remove index <perm_index> 添加/删除一个可以被城主设置的权限。
        // /city admin perm list 查看可以被城主设置的权限列表。
        // /city admin 打开小镇系统管理面板
        if(args.length == 1){
            OpenUI.openCityAdminUI(commander,1);
        }

        if(args.length >= 3){
            return workCityAdminPermSetCE(args, commander);
        }

        help(commander);
        return true;
    }

    private boolean workCityAdminPermSetCE( @NotNull String[] args, Player commander) {
        if(!args[1].equals("perm")) {
            help(commander);
            return true;
        }
        if(args[2].equals("add")){
            if(args.length != 4) {

                return true;
            }
            perm_city_settable.add(args[3]);
            commander.sendMessage("添加了可设置权限: "+ args[3]);
            OpenUI.openIconSetUI(commander,1);
            setting.put(commander,(mat) ->{
                CityPermissionItemStack.addCorrespondingMaterials(args[3],(Material) mat);
                commander.sendMessage("成功添加了该权限的对应方块");
            });
            return true;
        }
        if(args[2].equals("remove")){
            if(args.length == 4) {
                if (perm_city_settable.remove(args[3])) {
                    commander.sendMessage("删除了可设置权限: " + args[3]);
                } else commander.sendMessage("未找到可设置权限: " + args[3]);
                return true;
            } else {
                if(args.length == 5){
                    int index;
                    try {
                        index = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        commander.sendMessage("请输入正确的数字格式");
                        return true;
                    }
                    if(index < 0 || index >= perm_city_settable.size()){
                        commander.sendMessage("请输入正确的序号，请先输入/city admin perm list 查看可以被城主设置的权限列表。");
                        return true;
                    }
                    String t = perm_city_settable.get(index);
                    perm_city_settable.remove(index);
                    commander.sendMessage(t + "已经被删除。");
                    return true;
                }

            }
        }
        if(args[2].equals("list")){
            int index = 0;
            for (String s : perm_city_settable) {
                commander.sendMessage(index++ + ": " + s);
            }
            commander.sendMessage("打印结束。");
        }
        return true;
    }

    private boolean workCityAcceptCE(@NotNull CommandSender sender, @NotNull String[] args, Player commander) {
        City city = City.checkMayorAndGetCity(commander);
        if (city==null) {
            return true;
        }
        if(args.length==1){
            return openCityApplicationAcceptUI(city, commander);
        }
        UUID uuid;
        OfflinePlayer player = Bukkit.getPlayer(args[1]);
        if(player!=null){
            uuid = player.getUniqueId();
        } else uuid = UUID.fromString(args[1]);
        if (city.getApplications().contains(uuid)) {
            city.acceptApplication(uuid);
            sender.sendMessage("已经接受了该玩家的请求！");
        } else {
            sender.sendMessage("没有找到该玩家的请求！");
        }
        return true;
    }

    private boolean workCityAddWarpCE( @NotNull String[] args, Player commander) {
        if(args.length < 2) return help(commander);
        return workCitySetWarpCE(commander, args[1]);
    }

    private boolean workCityChunkForceLoadCE(Player commander) {
        City city = City.checkMayorAndGetCity(commander);
        if(city == null) return true;
        if (city.addChunk(commander.getLocation().getChunk())) {
            commander.sendMessage("添加成功！");
        } else {
            commander.sendMessage("添加失败！");
        }
        return true;
    }

    private boolean workCitySetWarpCE(Player commander, String spawn) {
        City city;
        city = City.checkMayorAndGetCity(commander);
        if (city == null) return true;
        if(city.addWarp(spawn, commander.getLocation())){
            commander.sendMessage("成功添加传送点"+ spawn +"！");
        } else {
            commander.sendMessage("传送点"+ spawn +"添加失败了，可能的原因有：");
            commander.sendMessage("1.权限不足，或已经添加了太多warp");
            commander.sendMessage("2.该位置未在城市领土范围内");
        }
        return true;
    }

    private boolean workCityRangeOperationCE( @NotNull String[] args, Player commander) {
        if(args.length==1) {
            return help(commander);
        }
        if(args[1].equals("add")){
            Range tr;
            if(args.length == 3){
                tr = new Range(commander.getLocation().getChunk());
            } else tr = getRange(commander);
            City city = City.checkMayorAndGetCity(commander);
            if(tr == null||city==null) return true;
            if (city.addTerritorial(tr)) {
                commander.sendMessage("添加成功！");
            } else {
                commander.sendMessage("添加失败！");
            }
            return true;
        }
        if(args[1].equals("list")){
            City city = getCity(commander);
            if(city==null) return true;
            commander.sendMessage("========================================");
            commander.sendMessage("下面是你的城市的所有区域：");
            int index = 0;
            for (Range range : city.getTerritorial()) {
                commander.sendMessage("--------------------------");
                commander.sendMessage("Index: " + index++);
                commander.sendMessage("World: " + range.getWorld().getName());
                commander.sendMessage("Start: " + range.toString_StartPoint());
                commander.sendMessage("End: " + range.toString_EndPoint());
                commander.sendMessage("Area: " + range.getArea());
                commander.sendMessage("--------------------------");
            }
            commander.sendMessage("TotalArea: " + Range.countUnionAreaFromDifferentWorld(city.getTerritorial()));
            commander.sendMessage("========================================");
            return true;
        }
        if(args.length == 2) {
            return help(commander);
        }

        if(args[1].equals("remove")){
            City city = City.checkMayorAndGetCity(commander);
            if(city==null) return true;
            int index;
            try {
                index = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                commander.sendMessage("请输入正确的整数格式，用以表示序号");
                return true;
            }
            if(index < 0 || index >= city.getTerritorial().size()){
                commander.sendMessage("整数越界！");
                return true;
            }
            city.removeTerritorial(index);
            commander.sendMessage("已经删除Index = " + index + "的区域！");
            return true;
        }
        return false;
    }

    private boolean workPermGroupSetCE(@NotNull CommandSender sender, @NotNull String[] args, Player commander) {
        if(args.length == 2) {
            OpenUI.openCityPermGroupChooseUI(commander);
            return true;
        }
        if(args.length != 5)return help(commander);
        City city = City.checkMayorAndGetCity(commander);
        if(city == null) return true;
        city.setPermToPermGroup(args[2],args[3],Boolean.getBoolean(args[4]));
        sender.sendMessage("权限组"+args[2]+"的权限"+ args[3]+"已经被设置为"+args[4]);
        return true;
    }

    private boolean workPermAddPlayerCE( @NotNull String[] args, Player commander) {
        if(args.length!=4) return help(commander);
        Player ad = Bukkit.getPlayer(args[3]);
        if(ad == null){
            commander.sendMessage("玩家未在线！");
            return true;
        }
        City city = City.checkMayorAndGetCity(commander);
        if(city == null) return true;
        city.addPlayerToPermGroup(args[2],ad.getUniqueId());
        commander.sendMessage("已经将"+ad.getName()+"添加到权限组"+ args[2]);
        return true;
    }

    private boolean workCityQuitCE(Player commander) {
        City temp = getCity(commander);
        if (temp==null){
            commander.sendMessage("你……你的小镇呢？");
            return true;
        }
        if (temp.getResidents().size()<3) {
            commander.sendMessage("你的小镇已经达到活跃最低限度，无法再退出了！");
            return true;
        }
        List<String> q = new ArrayList<>();
        q.add("你确定要退出你的城市吗？");
        q.add("如果确认，请在60s内在聊天框直接输入“confirm to quit "+temp.getName()+"”，不需要引号，句末也不空格");
        for (String s : q) {
            commander.sendMessage(s);
        }
        String ans = "confirm to quit "+temp.getName();
        List<Consumer<String>> check = new ArrayList<>();
        check.add((str)->{
            if(str.equals(ans)){
                try {
                    Objects.requireNonNull(getCity(commander)).removeResident(commander);
                } catch (Exception e) {
                    Other.sendError(e.getLocalizedMessage());
                }
                city_joined.put(commander,false);
                commander.sendMessage("你离开了你的城市……");
            } else {
                commander.sendMessage("放弃离开城市！");
            }
        });
        askSetAsync(commander,check,null,null);
        return false;
    }

    private boolean workCityJoinCE(@NotNull CommandSender sender, @NotNull String [] args, Player commander) {
        if(city_joined.getOrDefault(commander,false)){
            sender.sendMessage("一个人只能属于一个城市哦~");
            return true;
        }
        if(args.length==1){
            sender.sendMessage("请输入城市名！");
            return true;
        }
        if(!city_names.contains(args[1])){
            sender.sendMessage("没有找到你说的城市呢……");
            return true;
        }
        City temp = cities.get(args[1]);
        temp.addApplication(commander.getUniqueId());
        commander.sendMessage(ChatColor.GREEN+"你已经向这个城市发送了加入申请，请等待市长的处理。");
        return true;
    }

    private boolean workCityCreateGiveUpCE(Player commander) {
        String name = "&&NotFound%%";
        boolean a = true;
        for (String s : cities.keySet()) {
            if (cities.get(s).getMayor()== commander.getUniqueId()) {
                name = s;
                a = cities.get(s).isActivated();
            }
        }
        if(name.equals("&&NotFound%%")){
            commander.sendMessage("你确定你曾经想要创建过城市吗？");
            return true;
        }
        if(a){
            commander.sendMessage("你的城市已经被激活，请联系OP对城市进行整体删除！");
            return true;
        }
        city_joined.put(commander,false);
        cities.remove(name);
        city_names.remove(name);
        commander.sendMessage("已经取消了新城市的创建~");
        return true;
    }

    private boolean workCityCreateCE(@NotNull CommandSender sender, @NotNull String [] args, Player commander) {
        if(city_joined.getOrDefault(commander,false)){
            sender.sendMessage("一个人只能属于一个城市哦~");
            return true;
        }
        if(args.length==1){
            sender.sendMessage("请输入城市名！");
            return true;
        }
        if(city_names.contains(args[1])){
            sender.sendMessage("已经有人用过这个名字了……换一个吧！");
            return true;
        }
        if(args[1].contains("City")||args[1].contains("List")||
                args[1].contains(":")||args[1].contains(" ")||
                args[1].contains("Group")||args[1].contains("Page")||
                args[1].contains("Set")||args[1].contains("of")||
                args[1].contains("Perm")||args[1].contains("：")||
                args[1].contains("面板")||args[1].contains("界面")||
                args[1].contains("设置")||args[1].contains("Add")||
                args[1].contains("My")||args[1].contains("Bin")||
                args[1].contains("\\")||args[1].contains("'")||args[1].contains("\"")||
                args[1].contains("\n")||args[1].contains("\t")||args[1].contains("\0")){
            sender.sendMessage("你取的这个名字容易引起系统bug，换一个吧！");
            return true;
        }
        city_names.add(args[1]);
        City city = new City();
        city.setName(args[1]);
        city.setMayor(commander.getUniqueId());
        city_joined.put(commander,true);
        cities.put(args[1],city);
        commander.sendMessage(ChatColor.GREEN+"城市已经登记了！快去找两个小伙伴来基活这个城市吧！");
        return true;
    }

    private boolean help(CommandSender sender) {

        sender.sendMessage(ChatColor.GREEN+"City Help Page");
        if(sender instanceof Player){
            Player commander = (Player) sender;
            if(!city_joined.getOrDefault(commander,false)){
                sender.sendMessage("/city create <name> 发起一个城市的新建，在达到三个人时正式成立城市，使用这个指令的人会成为市长，拥有管理权限。");
                sender.sendMessage("/city join <name> 加入一个城市，一个人只能加入一个城市，但是可以同时发很多请求。");
            } else {
                City city = City.getCity(commander);
                if(city == null){
                    sender.sendMessage("你的城市信息出现异常，请联系管理员！");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN+"你的城市: " + city.getName());
                sender.sendMessage("/city spawn 回到自己小镇的出生点.");
                sender.sendMessage("/city warp <warp> 去往特定传送点.");
                sender.sendMessage("/city quit 退出小镇.");
                sender.sendMessage("/city my 打开小镇菜单.");
                if(city.getMayor().equals(commander.getUniqueId())){
                    sender.sendMessage(ChatColor.GREEN+"接下来是您作为市长可以执行的指令：");
                    sender.sendMessage("/city setwarp <name> 在小镇的领土中设置传送点.");
                    sender.sendMessage("/city setspawn 设置小镇出生点.");
                    sender.sendMessage("/city accept [player name/Offline player uuid] 同意一个人的加入请求，不填写[player name]，将会打开申请管理面板");
                    sender.sendMessage("/city perm add <perm group name> <player name> 将一个特定的人添加进该权限组，需要玩家在线.");
                    sender.sendMessage("/city perm set [perm group name] [perm name] 反转城市对特定权限组的权限，不填写则设置城市对居民的权限（打开面板）.");
                    sender.sendMessage("/city range add 向城镇中添加区域（木锄选区）重叠部分不重复计算体积.");
                    sender.sendMessage("/city range add chunk 把身下的区块区域添加到城镇领土中.");
                    sender.sendMessage("/city range list 列出所有区域.");
                    sender.sendMessage("/city range remove <index> 向小镇中删除区域.");
                    sender.sendMessage("/city loadchunk 让插件常加载脚下的方块,需要整个区块都在城镇区域内.");
                }
                if(commander.isOp()||commander.hasPermission("Sn.city.admin")){
                    sender.sendMessage("/city admin perm add/remove <perm> 添加/删除一个可以被城主设置的权限");
                    sender.sendMessage("/city admin perm add/remove index <perm_index> 添加/删除一个可以被城主设置的权限.");
                    sender.sendMessage("/city admin perm list 查看可以被城主设置的权限列表.");
                    sender.sendMessage("/city admin 打开小镇系统管理面板.");

                }
            }
        }
        Other.sendDebug("City Help Page Called");
        return true;
    }

}
