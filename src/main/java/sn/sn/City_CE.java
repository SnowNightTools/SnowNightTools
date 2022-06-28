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

import static java.lang.Math.pow;
import static sn.sn.AskSet.askSetAsync;
import static sn.sn.City_CE.City.getCity;
import static sn.sn.Collector_CE.getRange;
import static sn.sn.OpenUI.*;
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

        // /city perm add <perm group name> <player name> 将一个特定的人添加进该权限组，需要玩家在线。
        if (args[0].equals("perm"))
            if(args.length > 1) {
                if (args[1].equals("add")) {
                    return workPermAddPlayerCE(args, commander);
                }
            } else return help();

        // /city perm set [perm group name] 反转城市对特定权限组的权限，不填写则设置城市对居民的权限（打开面板）。
        if(args[0].equals("perm")&&args[1].equals("set")) {
            return workPermGroupSetCE(sender, args, commander);
        }

        // /city range add 向小镇中添加区域 体积上限与人数（用单独的枚举类CITY_TYPE来实现）有关。
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

        // /city manage 打开小镇管理面板（踢人之类的操作）


        // Only For OP:
        // /city admin remove <name> 删除一个城市，需要op操作。
        // /city admin add <perm> 添加一个可以被城主设置的权限。
        // /city admin 打开小镇系统管理面板



        return false;
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
        if(args.length < 2) return help();
        return workCitySetWarpCE(commander, args[1]);
    }

    private boolean workCityChunkForceLoadCE(Player commander) {
        City city = City.checkMayorAndGetCity(commander);
        if(city == null) return true;
        if (city.addChunks(commander.getLocation().getChunk())) {
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
            help();
            return true;
        }
        if(args[1].equals("add")){
            Range tr = getRange(commander);
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
            help();
            return true;
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
        City city = City.checkMayorAndGetCity(commander);
        if(city == null) return true;
        Boolean p = city.getPerm().getOrDefault(args[2], false);
        city.setPerm(args[2],!p);
        sender.sendMessage("权限"+ args[2]+"已经被设置为"+!p);
        return true;
    }

    private boolean workPermAddPlayerCE( @NotNull String[] args, Player commander) {
        if(args.length!=4) return help();
        Player ad = Bukkit.getPlayer(args[3]);
        if(ad == null){
            commander.sendMessage("玩家未在线！");
            return true;
        }
        City city = City.checkMayorAndGetCity(commander);
        if(city == null) return true;
        city.addPermGroup(args[2],ad.getUniqueId());
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
                    sendError(e.getLocalizedMessage());
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

    public enum CITY_TYPE {
        METROPOLIS("FIRST-TIER_CITY",5,5000,36,50,120),
        CITY("SECOND-TIER_CITY",4,3000,30,40,80),
        TOWN("THIRD-TIER_CITY",3,1000,24,30,50),
        TOWNSHIP("FOURTH-TIER_CITY",2,500,18,20,30),
        VILLAGE("FIFTH-TIER_CITY",1,300,12,10,10),
        NOT_ACTIVE("NOT_ACTIVE",0,100,6,2,2),
        ADMIN("ADMIN",6,0x7FFFFFFF,0x7FFFFFFF,0x7FFFFFFF,0x7FFFFFFF);

        private final String name;
        private final int range_perm;
        private final int perm_level;
        private final int warp_amount_perm;
        private final int max_resident;
        private final int perm_group_amount;

        CITY_TYPE(String name, int perm_level, int range_perm, int warp_amount_perm, int perm_group_amount, int max_resident){
            this.max_resident = max_resident;
            this.name = name;
            this.warp_amount_perm = warp_amount_perm;
            this.perm_group_amount = perm_group_amount;
            this.perm_level = perm_level;
            this.range_perm = range_perm;
        }

        @Nullable
        public static CITY_TYPE getCityTypeByLevel(int level){
            for (CITY_TYPE value : CITY_TYPE.values()) {
                if(value.getPermLevel() == level) return value;
            }
            return null;
        }

        public int getPermGroupAmount() {
            return perm_group_amount;
        }

        public int getPermLevel() {
            return perm_level;
        }

        public int getRangePerm() {
            return range_perm;
        }

        public String getName() {
            return name;
        }

        public int getWarpAmountPerm() {
            return warp_amount_perm;
        }

        public int getMaxResident(){
            return max_resident;
        }

        public int getChunkPerm() {
            //强加载区块权限与warp权限公用一个数据
            return warp_amount_perm;
        }
    }

    public static class City {

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
        private CITY_TYPE type = CITY_TYPE.NOT_ACTIVE;

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

        @Nullable
        public static City checkMayorAndGetCity(Player commander) {
            City city;
            city = getCity(commander);
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


        public ItemStack getIcon() {
            return icon;
        }

        public List<String> getDescription() {
            return description;
        }

        public Map<String, Location> getWarps() {
            return warps;
        }

        public Location getWarp(String warp) {
            return warps.get(warp);
        }

        public boolean addWarp(String name,Location loc) {
            boolean in = false;
            for (Range range : this.territorial) {
                if (range.isInRange(loc)) in = true;
            }
            if(!in) {
                return false;
            }
            for (String s : warps.keySet()) {
                if(s.equals(name)) {
                    this.warps.put(name, loc);
                    return true;
                }
            }
            if(warps.size() < type.getWarpAmountPerm()){
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

        public boolean addChunks(Chunk chunk) {
            if (chunk.isForceLoaded()) return false;
            for (Chunk c : chunks) {
                if(c.equals(chunk)) return false;
            }
            Range chunk_range = new Range(chunk);
            if(!chunk_range.isInRange(territorial)) return false;
            if(chunks.size()<type.getChunkPerm()) {
                this.chunks.add(chunk);
                chunk.setForceLoaded(true);
                return true;
            }
            return false;
        }

        public Map<String, Boolean> getPerm() {
            return perm;
        }

        public void setPerm(String perm_name, boolean perm) {
            this.perm.put(perm_name,perm);
        }

        public UUID getMayor() {
            return mayor;
        }

        public void setMayor(UUID mayor) {
            this.mayor = mayor;
        }

        public Map<String, List<UUID>> getPermGroup() {
            return perm_group;
        }

        public void addPermGroup(String perm_name, UUID player) {
            List<UUID> list = this.perm_group.getOrDefault(perm_name, new ArrayList<>());
            list.add(player);
            perm_group.put(perm_name, list);
        }

        public void addResident(UUID resident) {
            this.residents.add(resident);
            city_joined.put(Bukkit.getOfflinePlayer(resident),true);
            this.checkType();
        }

        private void checkType() {
            while (residents.size()>type.getMaxResident()) {
                if (!this.upgrade()) {
                    break;
                }
            }
            while ((type.getPermLevel() >= 2) && (residents.size() <= Objects.requireNonNull(CITY_TYPE.getCityTypeByLevel(type.getPermLevel() - 1)).getMaxResident())) {
                if (!this.downgrade()) {
                    break;
                }
            }
        }

        public boolean upgrade(){
            CITY_TYPE new_type = CITY_TYPE.getCityTypeByLevel(type.getPermLevel()+1);
            if(new_type == null) return false;
            activated = new_type.getPermLevel() >= 1;
            this.type = new_type;
            return true;
        }

        public boolean downgrade(){
            CITY_TYPE new_type = CITY_TYPE.getCityTypeByLevel(type.getPermLevel()-1);
            if(new_type == null) return false;
            activated = new_type.getPermLevel() >= 1;
            this.type = new_type;
            return true;
        }

        public boolean addTerritorial(Range territorial) {
            List<Range> temp = new ArrayList<>(this.territorial);
            double ori_a = Range.countUnionAreaFromDifferentWorld(temp);
            temp.add(territorial);
            double new_a = Range.countUnionAreaFromDifferentWorld(temp);
            if(ori_a == new_a) return false;
            if (new_a <= pow(type.getRangePerm(),3)) {
                this.territorial = temp;
                return true;
            } return false;
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

        public boolean addPerm_group(String name, List<UUID> perm_group) {
            if(perm.size() < type.getPermGroupAmount()) {
                this.perm_group.put(name, perm_group);
                return true;
            } return false;
        }

        public List<Range> getTerritorial() {
            return territorial;
        }

        public void removeResident(Player commander) {
            residents.remove(commander.getUniqueId());
        }

        public void removeTerritorial(int index) {
            territorial.remove(index);
        }
    }

}
