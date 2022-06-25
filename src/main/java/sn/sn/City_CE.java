package sn.sn;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static sn.sn.AskSet.askSetAsync;
import static sn.sn.OpenUI.*;
import static sn.sn.Sn.*;

/*
*
*
* City 城市系统
* 作者:
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
    @Nullable
    public static City checkMayorAndGetCity(Player commander) {
        City city;
        city = City.getCity(commander);
        try {
            if(!Objects.requireNonNull(city).getMayor().equals(commander.getUniqueId())){
                commander.sendMessage("这个操作只能由市长完成！");
                return null;
            }
        } catch (Exception e) {
            commander.sendMessage("你好像还没有加入城市！");
            return null;
        }
        return city;
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

        if(!label.equals("city"))return help();
        if(args.length==0) return help();
        if(args[0].equals("help")||args[0].equals("?")||args[0].equals("？")) return help();

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
            return workCitySpawnCE(commander, "spawn", "你的小镇没有设置spawn传送点！");
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
        if(args[0].equals("my")){
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

        // /city add <perm group name> <player name> 将一个特定的人添加进该权限组。
        // /city set [perm group name] 设置城市对特定权限组的权限，不填写则设置城市对居民的权限（打开面板）。
        // /city range add 向小镇中添加区域 体积上限与人数（用单独的枚举类CITY_TYPE来实现）有关。
        // /city range list 列出所有区域。
        // /city range remove <index> 向小镇中删除区域。
        // /city setspawn 设置小镇出生点
        // /city loadchunk 让插件常加载脚下的方块
        // /city manage 打开小镇管理面板（踢人之类的操作）
        //
        // Only For OP:
        // /city admin remove <name> 删除一个城市，需要op操作。
        // /city admin add <perm> 添加一个可以被城主设置的权限。
        // /city admin 打开小镇系统管理面板



        return false;
    }

    private boolean workCityAcceptCE(@NotNull CommandSender sender, @NotNull String[] args, Player commander) {
        City city = checkMayorAndGetCity(commander);
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
        if(args.length < 2) return help();
        City city;
        city = checkMayorAndGetCity(commander);
        if (city == null) return true;
        if(city.addWarp(args[1], commander.getLocation())){
            commander.sendMessage("成功添加传送点"+ args[1]+"！");
        } else {
            commander.sendMessage("传送点"+ args[1]+"添加失败了，可能的原因有：");
            commander.sendMessage("1.权限不足，或已经添加了太多warp");
            commander.sendMessage("2.该位置未在城市领土范围内");
            commander.sendMessage("3.该warp名已经被占用");
        }
        return true;
    }

    private boolean workCityQuitCE(Player commander) {
        City temp = City.getCity(commander);
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
        String ans = "confirm to quit "+temp.getName();
        List<Consumer<String>> check = new ArrayList<>();
        check.add((str)->{
            if(str.equals(ans)){
                try {
                    Objects.requireNonNull(City.getCity(commander)).removeResident(commander);
                } catch (Exception e) {
                    sendError(e.getLocalizedMessage());
                }
                city_joined.put(commander,false);
                commander.sendMessage("你离开了你的城市……");
            } else {
                commander.sendMessage("放弃离开城市！");
            }
        });
        askSetAsync(commander,q,check,60,null,null);
        return false;
    }

    private boolean workCitySpawnCE(Player commander, String spawn, String message) {
        City temp = City.getCity(commander);
        if (temp!=null) {
            if(temp.getWarp(spawn)!=null) {
                commander.teleport(temp.getWarp(spawn));
                return true;
            }
            commander.sendMessage(message);
            return true;
        }
        commander.sendMessage("你……你的小镇呢？");
        return true;
    }

    private boolean wordCityWarpCE( @NotNull String[] args, Player commander) {
        if(args.length==1){
            commander.sendMessage("请输入warp名！");
            return true;
        }
        return workCitySpawnCE(commander, args[1], "你的小镇没有设置" + args[1] + "传送点！");
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
        city_names.add(args[1]);
        City city = new City();
        city.setName(args[1]);
        city.setMayor(commander.getUniqueId());
        city_joined.put(commander,true);
        cities.put(args[1],city);
        commander.sendMessage(ChatColor.GREEN+"城市已经登记了！快去找两个小伙伴来激活这个城市吧！");
        return true;
    }

    private boolean help() {
        return true;
    }

    public static class City {

        private final int warp_amount_perm = 3;
        private ItemStack icon;
        private String name;
        private List<String> description;
        private List<UUID> residents = new ArrayList<>(), application = new ArrayList<>();
        private Map<String, List<UUID>> perm_group;
        private UUID mayor;
        private List<Range> territorial;
        private Map<String, Boolean> perm;
        private List<Chunk> chunks;
        private boolean activated = false;
        private Map<String, Location> warps = new HashMap<>();

        @Nullable
        public static City getCity(OfflinePlayer player){
            City temp;
            for (String s : cities.keySet()) {
                temp = cities.get(s);
                if(temp.getMayor().equals(player.getUniqueId())){
                    return temp;
                }
                for (UUID resident : temp.getResidents()) {
                    if(resident.equals(player.getUniqueId())){
                        return temp;
                    }
                }
            }
            return null;
        }


        public ItemStack getIcon() {
            return icon;
        }

        public List<String> getDescription() {
            return description;
        }

        public Map<String, Location> getWarps() {
            return warps;
        }

        public void setWarps(Map<String, Location> warps) {
            this.warps = warps;
        }

        public Location getWarp(String warp) {
            return warps.get(warp);
        }

        public boolean addWarp(String name,Location loc) {
            boolean in = false;
            for (Range range : this.territorial) {
                if (range.isInRange(loc)) in = true;
            }
            if(!in)return false;
            for (String s : warps.keySet()) {
                if(s.equals(name))return false;
            }
            if(warps.size() + 1 <= warp_amount_perm){
                this.warps.put(name, loc);
                return true;
            } else return false;
        }

        public void addApplication(UUID applier){
            if(!application.contains(applier)) application.add(applier);
        }

        public void acceptApplication(UUID applier){
            application.remove(applier);
            this.addResident(applier);
        }

        public void shelveApplication(UUID applier){
            application.remove(applier);
        }

        public boolean isActivated() {
            return activated;
        }

        public List<UUID> getApplications() {
            return application;
        }

        public void setApplication(List<UUID> application) {
            this.application = application;
        }

        public List<UUID> getResidents() {
            return residents;
        }

        public void setResidents(List<UUID> residents) {
            for (UUID resident : residents) {
                this.addResident(resident);
            }
        }

        public List<Chunk> getChunks() {
            return chunks;
        }

        public void setChunks(List<Chunk> chunks) {
            this.chunks = chunks;
        }

        public Map<String, Boolean> getPerm() {
            return perm;
        }

        public void setPerm(Map<String, Boolean> perm) {
            this.perm = perm;
        }

        public UUID getMayor() {
            return mayor;
        }

        public void setMayor(UUID mayor) {
            this.mayor = mayor;
        }

        public Map<String, List<UUID>> getPerm_group() {
            return perm_group;
        }

        public void setPerm_group(Map<String, List<UUID>> perm_group) {
            this.perm_group = perm_group;
        }

        public void addResident(UUID resident) {
            this.residents.add(resident);
            city_joined.put(Bukkit.getOfflinePlayer(resident),true);
            this.checkActivation();
        }

        private void checkActivation() {
            activated = residents.size() >= 2;
        }

        public void setTerritorial(List<Range> territorial) {
            this.territorial = territorial;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, List<UUID>> getPerm_groups() {
            return perm_group;
        }

        public List<UUID> getPerm_group(String perm_name) {
            return perm_group.get(perm_name);
        }

        public void addPerm_group(String name, List<UUID> perm_group) {
            this.perm_group.put(name,perm_group);
        }

        public List<Range> getTerritorial() {
            return territorial;
        }

        public void removeResident(Player commander) {
            residents.remove(commander.getUniqueId());
        }
    }

}
