package sn.sn.UI;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sn.sn.Ask.AskSetEvent;
import sn.sn.Basic.Other;
import sn.sn.Basic.PlayerOperation;
import sn.sn.Basic.SnFileIO;
import sn.sn.City.City;
import sn.sn.City.CityPermissionItemStack;
import sn.sn.Express.Express_CE;
import sn.sn.Quest.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.Math.min;
import static sn.sn.City.City_CE.workCityWarp;
import static sn.sn.Quest.Quest.getQuest;
import static sn.sn.Sn.*;


/*


    express 、 quest 和 collector 的监听器类

    实现了 express show 面板打开后的点击物品处理 和关闭面板时保存文件的处理
    实现了 quest create 面板操作
    实现了 collector bins 面板操作

    作者：LtSeed


*/



public class InvOperateEvent implements Listener {


    public static final ItemStack front_pg = new ItemStack(Material.WRITABLE_BOOK);
    public static final ItemStack cancel = new ItemStack(Material.BARRIER);
    public static final ItemStack confirm = new ItemStack(Material.EMERALD);
    public static final ItemStack next_pg = new ItemStack(Material.WRITABLE_BOOK);

    public InvOperateEvent(){
        ItemMeta confirm_meta = confirm.getItemMeta();
        assert confirm_meta != null;
        confirm_meta.setDisplayName(ChatColor.GREEN+"确认");
        confirm.setItemMeta(confirm_meta);
        ItemMeta cancel_meta = cancel.getItemMeta();
        assert cancel_meta != null;
        cancel_meta.setDisplayName(ChatColor.RED+"取消");
        cancel.setItemMeta(cancel_meta);

        ItemMeta pg_up_meta = front_pg.getItemMeta();
        assert pg_up_meta != null;
        pg_up_meta.setDisplayName(ChatColor.WHITE+"上一页");
        front_pg.setItemMeta(pg_up_meta);
        ItemMeta pg_dn_meta = next_pg.getItemMeta();
        assert pg_dn_meta != null;
        pg_dn_meta.setDisplayName(ChatColor.WHITE+"下一页");
        next_pg.setItemMeta(pg_dn_meta);
    }

    private static void putEntitySet(Player commander, Object ignored){
        if(entity_type_setting.containsKey(commander)&& int_setting.containsKey(commander)) {
            quest_action_setting.get(commander).getQuest_action_data().addQuesttargetentity(entity_type_setting.get(commander), int_setting.get(commander));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void InvCloseEvent(InventoryCloseEvent inv_close) {

        Player commander;
        commander = Bukkit.getPlayer(inv_close.getPlayer().getUniqueId());
        if(commander==null)return;
        if(inv_close.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"雪花速递")){
            SnFileIO.saveInvToYml(share_yml,share_file,commander.getName(), show_inv.get(commander));
            //保存文件
            Express_CE.setStateFalse(commander);
            // 名字 - 背包
            show_inv.remove(commander);
            commander.sendMessage(commander.getName()+"面板已关闭，信息已同步");
        }

        if(inv_close.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"正在创建一个新任务，请填写以下信息")){
            try {
                quest_yml.save(quest_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @EventHandler(priority = EventPriority.LOW)
    public void allIOEvent(InventoryClickEvent inv_click) {
        Player commander;
        commander = Bukkit.getPlayer(inv_click.getView().getPlayer().getUniqueId());
        if(commander==null)return;

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"雪花速递")){
            workExpressIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("Bin")){
            workCollectorIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"正在创建一个新任务，请填写以下信息")){
            workQuestSetIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"创建一个任务条件")){
            workQuestActionSetIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"任务达成的条件列表")){
            isSetTorC.put(commander,true);
            workActionSettingViewIO(inv_click,commander);
            return;
        }
        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"任务触发或接受的条件列表")){
            isSetTorC.put(commander,false);
            workActionSettingViewIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"选择任务类型")){
            workQuestTypeSetIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"任务奖励设置")){
            workQuestRewardSetIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"请放入物品奖励")){
            workQuestRewardItemSetIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"请设置物品条件")){
            workQuestItemConditionSetIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("父任务设置：请选择父任务")) {
            workParentQuestSetIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"选择实体类型")){
            workQuestEntityTypeSetIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"选择一种实体")){
            workQuestEntitySetIO(inv_click,commander);
            return;
        }
        if(inv_click.getView().getTitle().equalsIgnoreCase("位置变量设置")){
            workLocVarSetIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.RED+"删除一个任务条件")){
            workQuestConditionRemoveIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase("整数型变量设置")){
            workIntVarSetIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase("IconChoose")){
            workIconChooseIO(inv_click, commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("城市加入申请管理面板")) {
            workCityApplicationAcceptIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("Warp List")){
            workCityWarpListIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("Residents List")){
            workCityResidentsListIO(inv_click,commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("CityIconSet")){
            workCityIconSetIO(inv_click, commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("City Admin Panel")){
            workCityAdminIO(inv_click, commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("CityManage")){
            workCityManageIO(inv_click, commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("权限组选择界面")){
            workCityPermChooseIO(inv_click, commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("City权限组设置")){
            workCityPermGroupSetIO(inv_click, commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("PGPlayerAdd")){
            workCityPermGroupAddPlayerIO(inv_click, commander);
            return;
        }

        if(inv_click.getView().getTitle().contains("MyCity")) {
            workMyCityIO(inv_click,commander);
        }
    }

    private void workCityPermGroupAddPlayerIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        String city_name;
        try {
            city_name = Objects.requireNonNull(Objects.requireNonNull(inv_click.getInventory().getItem(52)).getItemMeta()).getDisplayName();
        } catch (Exception e) {
            Other.sendError(e.getLocalizedMessage());
            return;
        }
        City city = cities.get(city_name);
        if(city == null) return;
        List<OfflinePlayer> not_set_player = new ArrayList<>(List.of(Bukkit.getOfflinePlayers()));
        List<UUID> not_set = new ArrayList<>();
        not_set_player.forEach(p -> not_set.add(p.getUniqueId()));
        for (Map.Entry<String, List<UUID>> entry : city.getPermGroupList().entrySet()) {
            not_set.removeAll(entry.getValue());
        }
        not_set.remove(city.getMayor());
        not_set.removeAll(city.getResidents());
        int tot_page = not_set.size() / 45 + 1;
        int page = Integer.parseInt(Objects.requireNonNull(getPage(inv_click)));
        String name = getCity(inv_click);

        switch (inv_click.getSlot()) {
            case 53:
                if(page != tot_page){
                    OpenUI.openCityPermGroupAddPlayerUI(city, commander,name,page+1);
                }
                return;
            case 45:
                if(page != 1){
                    OpenUI.openCityPermGroupAddPlayerUI(city, commander,name,page-1);
                }
                return;
            case 46:
                int now = (page - 1) * 45;
                List<UUID> addList = not_set.subList(now, min(now + 45, not_set.size()));
                addList.forEach(u -> city.addPlayerToPermGroup(name,u));
                OpenUI.openCityPermGroupAddPlayerUI(city, commander,name,page);
                return;
            case 47:
                not_set.forEach(u -> city.addPlayerToPermGroup(name,u));
                OpenUI.openCityPermGroupSetUI(city, commander,name,1,1);
                return;
            case 51:
                OpenUI.openCityPermGroupSetUI(city, commander,name,1,1);
                return;
            default:
                if(inv_click.getSlot() >= 45) return;
                ItemMeta im = Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta();
                if(!(im instanceof SkullMeta)){
                    Other.sendError("发现非法物品存在于UI界面");
                    return;
                }
                city.addPlayerToPermGroup(name, Objects.requireNonNull(((SkullMeta) im).getOwningPlayer()).getUniqueId());
                OpenUI.openCityPermGroupAddPlayerUI(city, commander,name,page);
        }
    }

    private void workCityPermGroupSetIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        String city_name;
        try {
            city_name = Objects.requireNonNull(Objects.requireNonNull(inv_click.getInventory().getItem(52)).getItemMeta()).getDisplayName();
        } catch (Exception e) {
            Other.sendError(e.getLocalizedMessage());
            return;
        }
        City city = cities.get(city_name);
        if(city == null) return;
        int perm_page, player_page;
        String name;
        try {
            String title = inv_click.getView().getTitle();
            String perm_page_str = title.split("/")[0];
            perm_page_str = perm_page_str.split("组P")[1];
            String player_page_str = title.split("/")[1];
            player_page_str = player_page_str.split("家P")[1];
            perm_page = Integer.parseInt(perm_page_str);
            player_page = Integer.parseInt(player_page_str);
            name = title.split(": ")[1];
            name = name.split(" 权限")[0];
        } catch (Exception e) {
            Other.sendError(e.getLocalizedMessage());
            return;
        }
        Map<String, Boolean> perm_list = city.getPermList().get(name);
        List<UUID> player_list = city.getPermGroupList().get(name);
        int perm_tot_page = perm_list.size()/27 + 1, player_tot_page = player_list.size()/18 + 1;
        int slot = inv_click.getSlot();
        switch (slot){
            case 46:
                if (player_page != 1)
                    OpenUI.openCityPermGroupSetUI(city, commander,name,perm_page,player_page - 1);
                return;
            case 47:
                if (player_page != player_tot_page)
                    OpenUI.openCityPermGroupSetUI(city, commander,name,perm_page,player_page + 1);
                return;
            case 48:
                if (perm_page != 1)
                    OpenUI.openCityPermGroupSetUI(city, commander,name,perm_page - 1,player_page);
                return;
            case 49:
                if (perm_page != perm_tot_page)
                    OpenUI.openCityPermGroupSetUI(city, commander,name,perm_page + 1,player_page);
                return;
            case 50:
                if(!(name.equals("residents")||name.equals("mayor")))
                    OpenUI.openCityPermGroupAddPlayerUI(city, commander,name,1);
                return;
            case 53:
                OpenUI.openCityPermGroupChooseUI(city, commander);
                return;
            default:
                if(slot < 27){
                    ItemStack item = inv_click.getCurrentItem();
                    if(!(item instanceof CityPermissionItemStack)) {
                        Other.sendError("在操作城镇权限时发现非法物品存在于UI界面");
                        return;
                    }
                    CityPermissionItemStack c = (CityPermissionItemStack) item;
                    String perm_name = c.getPermName();
                    city.setPermToPermGroup(name,perm_name,!perm_list.getOrDefault(perm_name,false));
                    OpenUI.openCityPermGroupSetUI(city, commander,name,perm_page,player_page);
                } else if(slot < 45) {
                    ItemStack item = inv_click.getCurrentItem();
                    ItemMeta im = Objects.requireNonNull(item).getItemMeta();
                    if(!(im instanceof SkullMeta)) {
                        Other.sendError("在操作城镇权限时发现非法物品存在于UI界面");
                        return;
                    }
                    try {
                        city.removePlayerFromPermGroup(name, Objects.requireNonNull(((SkullMeta) im).getOwningPlayer()).getUniqueId());
                    } catch (Exception e) {
                        commander.sendMessage("可能出现错误，建议删除该权限组并重新创建，或联系管理员!");
                        commander.closeInventory();
                        return;
                    }
                    OpenUI.openCityPermGroupSetUI(city, commander,name,perm_page,player_page);
                }
        }
    }

    private void workIconChooseIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        int page = Integer.parseInt(Objects.requireNonNull(getPage(inv_click)));
        final int page_amount = 23;
        switch (inv_click.getSlot()){
            case 53:
                if(page != page_amount){
                    OpenUI.openCityAdminUI(commander,page+1);
                }
                return;
            case 45:
                if(page != 1){
                    OpenUI.openCityAdminUI(commander,page-1);
                }
                return;
            case 46:
                if(setting.containsKey(commander)){
                    setting.get(commander).accept(Material.PAPER);
                    commander.sendMessage("你的设置被放弃，默认设置为"+Material.PAPER);
                } else {
                    commander.sendMessage("你的设置被放弃");
                }
                return;
            default:
                Material c;
                try {
                    c = Objects.requireNonNull(inv_click.getCurrentItem()).getType();
                } catch (Exception ignored) {
                    return;
                }
                if(setting.containsKey(commander)){
                    setting.get(commander).accept(c);
                    commander.sendMessage("设置成功");
                } else {
                    commander.sendMessage("无法找到你的设置项目，这可能是一个bug，请联系管理员。");
                }

        }
    }

    private void workCityPermChooseIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        String s = getCity(inv_click);
        City city = cities.get(s);
        if (city == null) return;
        switch (inv_click.getSlot()){
            case 45:
                OpenUI.openCityManageUI(city, commander,false);
                return;
            case 46:
                List<String> q = new ArrayList<>();
                q.add("请输入权限组名");
                List<Consumer<String>> c = new ArrayList<>();
                c.add((str)->{
                    if (city.getPermGroupList().containsKey(str)||str.equalsIgnoreCase("all")) {
                        commander.sendMessage("该名已经被使用。");
                        return;
                    }
                    city.setPermToPermGroup(str,"all",false);
                    commander.sendMessage("已经添加新的权限组！");
                    OpenUI.openCityPermGroupChooseUI(city, commander);
                });
                AskSetEvent.askSetAsync(commander,q,c,null,null);
                commander.closeInventory();
                return;
            default:
                if(Objects.requireNonNull(inv_click.getCurrentItem()).hasItemMeta()){
                    String name = Objects.requireNonNull(inv_click.getCurrentItem().getItemMeta()).getDisplayName();
                    OpenUI.openCityPermGroupSetUI( city, commander, name,1 , 1);
                }
        }
    }

    private void workCityAdminIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        String page_str = getPage(inv_click);
        if(page_str == null) return;
        int page = Integer.parseInt(page_str);
        int page_amount = (cities.size())/45 +1;
        switch (inv_click.getSlot()){
            case 53:
                if(page != page_amount){
                    OpenUI.openCityAdminUI(commander,page+1);
                }
                return;
            case 45:
                if(page != 1){
                    OpenUI.openCityAdminUI(commander,page-1);
                }
                return;
            case 46:
                debug = !debug;
                OpenUI.openCityAdminUI(commander,page);
                return;
            default:
                String city_name;
                try {
                    city_name = Objects.requireNonNull(Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta()).getDisplayName();
                } catch (Exception e) {
                    return;
                }
                OpenUI.openCityManageUI(cities.get(city_name), commander,false);
        }
    }

    private void workCityIconSetIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        String s = getCity(inv_click);
        City city = cities.get(s);
        if (city == null) return;
        String page_str = getPage(inv_click);
        if(page_str == null) return;
        int page = Integer.parseInt(page_str);
        int page_amount = (Material.values().length)/45 +1;
        switch (inv_click.getSlot()){
            case 53:
                if(page != page_amount){
                    OpenUI.openCityIconSetUI(city, commander,page+1);
                }
                return;
            case 45:
                if(page != 1){
                    OpenUI.openCityIconSetUI(city, commander,page-1);
                }
                return;
            case 46:
                commander.closeInventory();
                ItemStack hand = commander.getItemOnCursor();
                if(hand.getItemMeta() instanceof BookMeta){
                    BookMeta bm = (BookMeta) hand.getItemMeta();
                    List<String> dsp = new ArrayList<>(bm.getPages());
                    city.setDescription(dsp);
                }
                return;
            default:
                ItemStack a = inv_click.getCurrentItem();
                if(a==null) return;
                city.setIcon(a);
        }
    }

    private void workCityManageIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        String s = getCity(inv_click);
        City city = cities.get(s);
        if (city == null) return;
        boolean edit = inv_click.getView().getTitle().contains("(Edit)");
        switch (inv_click.getSlot()){
            case 4:
                if(edit) OpenUI.openCityIconSetUI(city,commander,1);
                return;
            case 5:
                return;
            case 31:
                if(edit) {
                    if (city.resetWarp("spawn",commander.getLocation())) {
                        commander.sendMessage("Spawn设置成功");
                    } else commander.sendMessage("Spawn设置失败");
                    return;
                }
                workCityWarp(commander,"spawn","");
                commander.closeInventory();
                return;
            case 40:
                OpenUI.openCityWarpListUI(city,commander,1,edit);
                return;
            case 27:
                PlayerOperation.tryCMITpa(commander,city.getMayor());
                commander.closeInventory();
                return;
            case 36:
                OpenUI.openCityResidentsListUI(city,commander,1,edit);
                return;
            case 44:
                OpenUI.openCityPermGroupChooseUI(city,commander);
                return;
            case 45:
                edit = !edit;
                OpenUI.openCityManageUI(city,commander,edit);
                return;
            case 14:
                commander.closeInventory();
                List<String> q = new ArrayList<>();
                q.add("请输入新的城市欢迎语：");
                List<Consumer<String>> c = new ArrayList<>();
                c.add(s1 -> {
                    city.setWelcomeMessage(s1);
                    commander.sendMessage("已经将城市欢迎语设置为："+s1);
                });
                AskSetEvent.askSetAsync(commander,q,c,null, player -> player.sendMessage("操作取消！"));
                commander.closeInventory();
                return;
            case 3:
                if(commander.isOp()||commander.hasPermission("sn.city.admin")){
                    city.setAdmin();
                    OpenUI.openCityManageUI(city,commander,edit);
                }
                return;
            case 53:
                if(commander.isOp()||commander.hasPermission("sn.city.admin")){
                    String ans = "confirm remove "+city.getName();
                    List<String> qes = new ArrayList<>();
                    qes.add("确认删除吗？请输入" + ans + "来确认!");
                    List<Consumer<String>> lc = new ArrayList<>();
                    Consumer<String> con = (str)->{
                        if(str.equals(ans)) {
                            cities.remove(city.getName());
                            city_names.remove(city.getName());
                            city_joined.remove(Bukkit.getOfflinePlayer(city.getMayor()));
                            for (Map.Entry<Player, City> entry : city_in.entrySet()) {
                                if (entry.getValue().equals(city)) {
                                    city_in.remove(entry.getKey());
                                }
                            }
                            city.getResidents().forEach(u -> city_joined.remove(Bukkit.getOfflinePlayer(u)));
                            commander.sendMessage("城市已经删除！");
                        }
                    };
                    lc.add(con);
                    AskSetEvent.askSetAsync(commander,qes,lc,null,null);
                    commander.closeInventory();
                }
        }
    }

    private String getCity(InventoryClickEvent inv_click) {
        String s = inv_click.getView().getTitle().split(": ")[1];
        if (s.contains(" Page ")) {
            s = s.split(" Page ")[0];
        }
        return s;
    }

    private void workCityWarpListIO(InventoryClickEvent inv_click,Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        String s = getCity(inv_click);
        City city = cities.get(s);
        if (city == null) return;
        String page_str = getPage(inv_click);
        if(page_str == null) return;
        int page = Integer.parseInt(page_str);
        int page_amount = (city.getWarps().size()-1)/45 +1;
        boolean edit = inv_click.getView().getTitle().contains("(Edit)");
        switch (inv_click.getSlot()){
            case 53:
                if(page != page_amount){
                    OpenUI.openCityWarpListUI(city,commander,page+1,edit);
                }
                return;
            case 45:
                if(page != 1){
                    OpenUI.openCityWarpListUI(city,commander,page-1,edit);
                }
                return;
            default:
                ItemStack item = inv_click.getCurrentItem();
                if(item==null) return;
                String warp_name = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
                if(warp_name.contains("共有")) return;
                if(edit){
                    city.resetWarp(warp_name,commander.getLocation());
                    commander.sendMessage("Warp："+warp_name+"已经重新设置！");
                    OpenUI.openCityManageUI(city,commander,true);
                } else workCityWarp(commander,warp_name,"发送了未知的错误。");
        }
    }

    private void workCityResidentsListIO(InventoryClickEvent inv_click,Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        String s = getCity(inv_click);
        City city = cities.get(s);
        if (city == null) return;
        String page_str = getPage(inv_click);
        if(page_str == null) return;
        int page = Integer.parseInt(page_str);
        int page_amount = (city.getResidents().size()-1)/45 +1;
        boolean edit = inv_click.getView().getTitle().contains("(Edit)");
        switch (inv_click.getSlot()){
            case 53:
                if(page != page_amount){
                    OpenUI.openCityResidentsListUI(city,commander,page+1,edit);
                }
                return;
            case 45:
                if(page != 1){
                    OpenUI.openCityResidentsListUI(city,commander,page-1,edit);
                }
                return;
            default:
                ItemStack item = inv_click.getCurrentItem();
                if(item==null) return;
                if(item.getItemMeta() instanceof SkullMeta){
                    OfflinePlayer player = ((SkullMeta) item.getItemMeta()).getOwningPlayer();
                    if(player==null) return;
                    if(edit) {
                        List<String> q = new ArrayList<>();
                        q.add("若确认，请直接输入：\"confirm tick "+player.getName()+"\"");
                        String ans = "confirm tick "+player.getName();
                        Consumer<String> react = (str) -> {
                            if(str.equals(ans))city.removeResident(player);
                        };
                        List<Consumer<String>> list = new ArrayList<>();
                        list.add(react);
                        AskSetEvent.askSetAsync(commander,
                                q,
                                list,
                                (player1)-> player1.sendMessage("操作成功"),
                                (player1)-> player1.sendMessage("操作失败"));
                        return;
                    }
                    if(!player.isOnline()) return;
                    commander.performCommand("/cmi tpa "+ player.getName());
                }
        }
    }

    private void workMyCityIO(InventoryClickEvent inv_click,Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        City city = City.getCity(commander);
        if (city == null) return;
        switch (inv_click.getSlot()) {
            case 36:
                OpenUI.openCityResidentsListUI(commander, 1);
                return;
            case 27:
                Player mayor = Bukkit.getPlayer(city.getMayor());
                if(mayor == null){
                    commander.closeInventory();
                    commander.sendMessage("你的市长不在线，无法进行传送。");
                    return;
                }
                commander.performCommand("/cmi tpa "+ mayor.getName());
                return;
            case 40:
                OpenUI.openCityWarpListUI(commander,1);
                return;
            case 31:
                workCityWarp(commander, "spawn", "你的小镇没有设置spawn传送点！");
                return;
            default:
        }
    }

    private void workCityApplicationAcceptIO(InventoryClickEvent inv_click,Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        OfflinePlayer p;
        try {
            ItemMeta itemMeta = Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta();
            p = ((SkullMeta) Objects.requireNonNull(itemMeta)).getOwningPlayer();
        } catch (Exception e) {
            Other.sendError(e.getLocalizedMessage());
            return;
        }
        if (p == null) {
            commander.sendMessage("发生了错误：无法获得玩家信息！");
            return;
        }
        City city = City.checkMayorAndGetCity(commander);
        if (city == null) return;
        if (inv_click.getClick().equals(ClickType.RIGHT)) {
            city.shelveApplication(p.getUniqueId());
            commander.sendMessage("搁置了"+p.getName()+"的请求");
            String page_str =  getPage(inv_click);
            if (page_str == null) {
                OpenUI.openCityApplicationAcceptUI(city,commander);
            } else {
                OpenUI.openCityApplicationAcceptUI(city,commander, Integer.parseInt(page_str));
            }
            return;
        }
        List<String> conf = new ArrayList<>();
        conf.add(ChatColor.GREEN + "你确认要添加" + p.getName() + "到城市中吗？");
        conf.add(ChatColor.LIGHT_PURPLE + "请注意，这代表着城市需要对该玩家的行为负责！");
        conf.add(ChatColor.GREEN + "若确定，请在60s内直接输入confirm add " + p.getName());
        conf.add(ChatColor.GREEN + "严格区分大小写，请不要添加其他任何符号，不要删减空格");
        for (String s : conf) {
            commander.sendMessage(s);
        }
        String ans = "confirm add " + p.getName();
        List<Consumer<String>> list = new ArrayList<>();
        Consumer<String> consumer = (str) -> {
            if (str.equals(ans)) {
                city.acceptApplication(p.getUniqueId());
                commander.sendMessage(p.getName() + "已经添加到小镇中！");
            } else {
                commander.sendMessage(p.getName() + "添加失败！");
            }
        };
        list.add(consumer);
        String finalSp = getPage(inv_click);
        AskSetEvent.askSetAsync(commander, list,
                (player) -> {
                    if (finalSp != null)
                    OpenUI.openCityApplicationAcceptUI(
                            Objects.requireNonNull(City.getCity(player)), player, Integer.parseInt(finalSp));
                    else OpenUI.openCityApplicationAcceptUI(
                            Objects.requireNonNull(City.getCity(player)), player);
                },
                (player) -> player.sendMessage("操作被打断或终止。"));
    }

    @Nullable
    private String getPage(InventoryClickEvent inv_click) {
        String sp = null;
        if (inv_click.getView().getTitle().contains("Page ")) {
            sp = inv_click.getView().getTitle().split("Page ")[1];
            sp = sp.split(" ")[0];
        }
        return sp;
    }

    private void workIntVarSetIO(InventoryClickEvent inv_click,Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        int ind = inv_click.getSlot();
        int ord = 0;
        try {
            ord = int_setting.get(commander);
        } catch (NullPointerException ignored) {
        }
        boolean neg = false;
        if(ord<0){
            neg = true;
            ord *= -1;
        }
        if(ind<9){//0-8 -> 1-9
            int_setting.replace(commander,ord-ord%10+ind+1);
        } else if(ind<18){ // 9-17 -> 1-9 *10
            int_setting.replace(commander,ord-ord%100+ord%10 + (ind%9+1)*10);
        } else if(ind<27){
            int_setting.replace(commander,ord-ord%1000+ord%100 + (ind%9+1)*100);
        } else if(ind<36){
            int_setting.replace(commander,ord-ord%10000+ord%1000 + (ind%9+1)*1000);
        } else if(ind<45){
            int_setting.replace(commander,ord-ord%100000+ord%10000 + (ind%9+1)*10000);
        } else switch (ind) {
            case 45:
                int_setting.replace(commander, 0);
                break;
            case 46:
                if(neg) int_setting.replace(commander, ord);
                else int_setting.replace(commander, -1*ord);
                break;
            case 53:
                if(neg) int_setting.replace(commander, -1*ord);
                else int_setting.replace(commander, ord);
            case 52:
                if(ind==52) int_setting.replace(commander, 0);
                if(setting.containsKey(commander)){
                    if (neg) setting.get(commander).accept(-1 * ord);
                    else setting.get(commander).accept(ord);
                    setting.remove(commander);
                }
                if(ui_opener.containsKey(commander)) {
                    ui_opener.get(commander).accept(commander);
                    ui_opener.remove(commander);
                }

                int_setting.remove(commander);
                return;
            default:
                return;
        }
        OpenUI.openIntSettingUI(commander);
    }

    private void workQuestConditionRemoveIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        List<QuestAction> target = quest_setting.get(commander).getQuestAcceptCondition();
        if(isSetTorC.get(commander)) target = quest_setting.get(commander).getQuest_target();
        if(inv_click.getSlot()==17){
            OpenUI.openActionSettingUI(commander);
            return;
        }
        try {
            target.remove(inv_click.getSlot());
            if(isSetTorC.get(commander)) quest_setting.get(commander).setQuestTarget(target);
            quest_setting.get(commander).setQuestAcceptCondition(target);
        } catch (UnsupportedOperationException|IndexOutOfBoundsException|NullPointerException ignore) {
        }
        OpenUI.openActionDeleteUI(commander);
    }

    private void workLocVarSetIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        //反转 重设
        int opr = 0;
        String name = Objects.requireNonNull(Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta()).getDisplayName();

        if (name.contains("X")){
            if(name.contains("反转")){
                loc_setting.get(commander).revXm();
                OpenUI.openLocSettingUI(commander);
                return;
            }
            if(name.contains("重设")){
                loc_setting.get(commander).setX(0);
                OpenUI.openLocSettingUI(commander);
                return;
            }
            name = name.substring(1);
            try {
                opr = Integer.parseInt(name);
            } catch (NumberFormatException ignored) {
            }
            if(opr != 0){
                loc_setting.get(commander).addX(opr);
                OpenUI.openLocSettingUI(commander);
                return;
            }
        }
        if (name.contains("Y")){
            if(name.contains("反转")){
                loc_setting.get(commander).revYm();
                OpenUI.openLocSettingUI(commander);
                return;
            }
            if(name.contains("重设")){
                loc_setting.get(commander).setY(0);
                OpenUI.openLocSettingUI(commander);
                return;
            }
            name = name.substring(1);
            try {
                opr = Integer.parseInt(name);
            } catch (NumberFormatException ignored) {
            }
            if(opr != 0){
                loc_setting.get(commander).addY(opr);
                OpenUI.openLocSettingUI(commander);
                return;
            }
        }
        if (name.contains("Z")){
            if(name.contains("反转")){
                loc_setting.get(commander).revZm();
                OpenUI.openLocSettingUI(commander);
                return;
            }
            if(name.contains("重设")){
                loc_setting.get(commander).setZ(0);
                OpenUI.openLocSettingUI(commander);
                return;
            }
            name = name.substring(1);
            try {
                opr = Integer.parseInt(name);
            } catch (NumberFormatException ignored) {
            }
            if(opr != 0){
                loc_setting.get(commander).addZ(opr);
                OpenUI.openLocSettingUI(commander);
                return;
            }
        }
        List<World> tmp_lw = Bukkit.getWorlds();
        for (World world : tmp_lw) {
            if(name.equalsIgnoreCase(world.getName())){
                loc_setting.get(commander).setWorld(world);
                OpenUI.openLocSettingUI(commander);
                return;
            }
        }
        try {
            if(inv_click.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.RED+"取消")){
                setting.remove(commander);
                ui_opener.get(commander).accept(commander);
                ui_opener.remove(commander);
                return;
            }
            if(inv_click.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN+"确认")){
                setting.get(commander).accept(loc_setting.get(commander).getLoc());
                setting.remove(commander);
                ui_opener.get(commander).accept(commander);
                ui_opener.remove(commander);
                return;
            }
        } catch (Exception ignored) {
        }
        if(inv_click.getCurrentItem().getItemMeta().getDisplayName().equals("下一页")){
            Inventory tmp_dn = Bukkit.createInventory(commander,54,"世界设置");
            if(tmp_lw.size()>=53){
                commander.closeInventory();
                commander.sendMessage("大哥你服务器是怎么装下52个以上的世界的，能不能教教我？");
                commander.sendMessage("帮您把刚刚设置的东西都删了哦！");
                setting.remove(commander);
                setting_state.remove(commander);
                isSetTorC.remove(commander);
                quest_setting.remove(commander);
                quest_action_setting.remove(commander);
                return;
            }
            for (int i = 0, tmp_lw_size = tmp_lw.size(); i < tmp_lw_size; i++) {
                World world = tmp_lw.get(i);

                tmp_dn.setItem(i, OpenUI.getItem("GRASS_BLOCK",world.getName(), Collections.singletonList(world.getUID().toString())));
            }
            tmp_dn.setItem(52,cancel);
            tmp_dn.setItem(53,confirm);
            commander.openInventory(tmp_dn);
        }
    }

    private void workQuestEntitySetIO(InventoryClickEvent inv_click,Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;

        try {
            entity_type_setting.put(commander,EntityType.valueOf(Objects.requireNonNull(Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta()).getDisplayName()));
        } catch (IllegalArgumentException | NullPointerException ignore) {
        }

        ui_opener.put(commander, OpenUI::openActionCreateUI);
        if(setting.containsKey(commander)) setting.put(commander, p -> putEntitySet(commander,p));
        else setting.put(commander, p -> putEntitySet(commander,p));
        OpenUI.openIntSettingUI(commander);

    }

    private void workQuestEntityTypeSetIO(InventoryClickEvent inv_click,Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;

        if(inv_click.getSlot()==53) {
            OpenUI.openActionCreateUI(commander);
            return;
        }

        List<String> tmp_lore = null;
        try {
            tmp_lore = Objects.requireNonNull(Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta()).getLore();
        } catch (NullPointerException ignore){}
        if(tmp_lore == null) return;

        Inventory tmp_cet = Bukkit.createInventory(commander,9,ChatColor.GREEN+"选择一种实体");

        for (int i = 1; i < tmp_lore.size(); i++) {
            tmp_cet.setItem(i-1, OpenUI.getItem("SPAWNER", tmp_lore.get(i),null));
        }
        tmp_cet.setItem(8,cancel);
        commander.openInventory(tmp_cet);
    }

    private void workParentQuestSetIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if (cli.isKeyboardClick() || cli == ClickType.WINDOW_BORDER_LEFT || cli == ClickType.WINDOW_BORDER_RIGHT) {
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }

        if(Objects.requireNonNull(inv_click.getClickedInventory()).contains(non_p())){
            String name;
            try {
                name = Objects.requireNonNull(Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta()).getDisplayName();
            } catch (NullPointerException e){
                return;
            }
            if (workPositionSet(name,commander)) return;
            OpenUI.openQuestSettingUI(commander);
            return;

        }

        if (Objects.requireNonNull(inv_click.getClickedInventory()).contains(front_pg)||Objects.requireNonNull(inv_click.getClickedInventory()).contains(next_pg)) {
            //玩家点击的是上面的设置内容
            int pg_index = Integer.parseInt(inv_click.getView().getTitle(), inv_click.getView().getTitle().indexOf("第")+1, inv_click.getView().getTitle().indexOf("页"),10);
            String name;
            try {
                name = Objects.requireNonNull(Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta()).getDisplayName();
            } catch (NullPointerException e){
                return;
            }

            if(name.equals("上一页")){
                OpenUI.openPositionChooseMultiPageUI(commander, pg_index + 1);
                return;
            }
            if(name.equals("下一页")){
                OpenUI.openPositionChooseMultiPageUI(commander, pg_index - 1);
                return;
            }
            if (workPositionSet(name,commander)) return;
            OpenUI.openQuestSettingUI(commander);
        }
    }

    private void workQuestItemConditionSetIO(InventoryClickEvent inv_click,Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli.equals(ClickType.WINDOW_BORDER_LEFT)||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }

        if(inv_click.getSlot()==17){
            OpenUI.openActionCreateUI(commander);
            return;
        }

        List<ItemStack> list = quest_action_setting.get(commander).getQuest_action_data().getQuesttargetitem();

        if(cli.isLeftClick()){
            if(inv_click.getClickedInventory() == null|| inv_click.getCurrentItem() == null) return;
            //点击showInv物品:取回该项
            if(Objects.requireNonNull(inv_click.getClickedInventory()).getSize()==18) {
                if(inv_click.getSlot()>=9)
                    return;
                list.remove(inv_click.getSlot());
                Inventory reward_item_setting = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请设置物品条件");
                reward_item_setting.setContents(list.toArray(new ItemStack[9]));
                reward_item_setting.setItem(17,confirm);
                commander.openInventory(reward_item_setting);

            } else {
                //玩家点击的是背包
                if(list.size()>=9) return;
                list.add(inv_click.getCurrentItem());
                Inventory reward_item_setting = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请设置物品条件");
                reward_item_setting.setContents(list.toArray(new ItemStack[9]));
                reward_item_setting.setItem(17,confirm);
                commander.openInventory(reward_item_setting);
            }

        }
        quest_action_setting.get(commander).getQuest_action_data().setQuesttargetitem(list);
    }

    private void workQuestRewardItemSetIO(InventoryClickEvent inv_click,Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli.equals(ClickType.WINDOW_BORDER_LEFT)||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }

        if(inv_click.getSlot()==17){
            OpenUI.openRewardSettingUI(commander);
            return;
        }

        QuestReward tmp_qr = quest_setting.get(commander).getQuestReward();
        if(tmp_qr ==null)
            tmp_qr = new QuestReward();

        if(cli.isLeftClick()){
            if(inv_click.getClickedInventory() == null|| inv_click.getCurrentItem() == null) return;
            //点击showInv物品:取回该项
            if(Objects.requireNonNull(inv_click.getClickedInventory()).getSize()==18) {
                if(inv_click.getSlot()>=9)
                    return;
                tmp_qr.getRewarditems().remove(inv_click.getSlot());
                Inventory reward_item_setting = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请放入物品奖励");
                reward_item_setting.setContents(tmp_qr.getRewarditems().toArray(new ItemStack[9]));
                reward_item_setting.setItem(17,confirm);
                commander.openInventory(reward_item_setting);

            } else {
                //玩家点击的是背包
                if(tmp_qr.getRewarditems().size()>=9) return;
                tmp_qr.getRewarditems().add(inv_click.getCurrentItem());
                Inventory reward_item_setting = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请放入物品奖励");
                reward_item_setting.setContents(tmp_qr.getRewarditems().toArray(new ItemStack[9]));
                reward_item_setting.setItem(17,confirm);
                commander.openInventory(reward_item_setting);
            }

        }
        quest_setting.get(commander).setQuestReward(tmp_qr);
    }

    private void workQuestRewardSetIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli.equals(ClickType.WINDOW_BORDER_LEFT)||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }

        if(Objects.requireNonNull(inv_click.getClickedInventory()).getSize()==9) {
            //玩家点击的是上面的设置内容

            QuestReward tmp_qr = quest_setting.get(commander).getQuestReward();
            if(tmp_qr ==null)
                tmp_qr = new QuestReward();
            switch (inv_click.getSlot()){
                case 0:
                    tmp_qr.setAdmin(true);
                    OpenUI.openRewardSettingUI(commander);
                    quest_setting.get(commander).setQuestReward(tmp_qr);
                    return;
                case 3:
                    OpenUI.openIntSettingUI(commander);
                    ui_opener.put(commander, OpenUI::openRewardSettingUI);
                    return;
                case 5:
                    Inventory reward_item_setting = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请放入物品奖励");
                    reward_item_setting.setContents(tmp_qr.getRewarditems().toArray(new ItemStack[9]));
                    reward_item_setting.setItem(17,confirm);
                    commander.openInventory(reward_item_setting);
                    return;
                case 8:
                    OpenUI.openQuestSettingUI(commander);
                    return;
                default:
            }
        }
    }

    private void workQuestTypeSetIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }

        if(Objects.requireNonNull(inv_click.getClickedInventory()).getSize()==9) {
            //玩家点击的是上面的设置内容
            switch (inv_click.getSlot()){
                case 1:
                    quest_setting.get(commander).setQuestType(QuestType.MAIN);
                    break;
                case 2:
                    quest_setting.get(commander).setQuestType(QuestType.SIDE);
                    break;
                case 3:
                    quest_setting.get(commander).setQuestType(QuestType.TRIGGER);
                    break;
                case 5:
                    quest_setting.get(commander).setQuestType(QuestType.DAILY);
                    break;
                case 6:
                    quest_setting.get(commander).setQuestType(QuestType.REWARD);
                    break;
                case 7:
                    quest_setting.get(commander).setQuestType(QuestType.DIY);
                    break;
                default:
                    return;
            }

            OpenUI.openQuestSettingUI(commander);

        }
    }

    private void workQuestActionSetIO(InventoryClickEvent inv_click,Player commander) {
        //open by openActionCreateUI();
        if (OpenUI.uiINIT(inv_click)) return;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }

        if(!quest_action_setting.containsKey(commander))
            quest_action_setting.put(commander,new QuestAction());

        switch (inv_click.getSlot()){

            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                quest_action_setting.get(commander).setQuest_action_type(QuestActionType.getFromInt(inv_click.getSlot()+1));
                OpenUI.openActionCreateUI(commander);
                return;
            case 9:
                List<ItemStack> quest_target_item = quest_action_setting.get(commander).getQuest_action_data().getQuesttargetitem();
                Inventory qt_i_setting = Bukkit.createInventory(commander, 18,ChatColor.GREEN+"请设置物品条件");
                qt_i_setting.setContents(quest_target_item.toArray(new ItemStack[9]));
                qt_i_setting.setItem(17,confirm);
                commander.openInventory(qt_i_setting);
                return;
            case 10:
                OpenUI.openEntityTypeSettingUI(commander);
                return;
            case 11:
                OpenUI.openNPCSettingUI(commander);
                return;
            case 12:
                setting.put(commander, loc -> quest_action_setting.get(commander).getQuest_action_data().setTargetlocation((Location) loc));
                ui_opener.put(commander, OpenUI::openActionCreateUI);
                OpenUI.openLocSettingUI(commander);
                return;
            case 13:
                setting.put(commander, set -> quest_action_setting.get(commander).getQuest_action_data().setQuesttimelimit((Integer) set));
                ui_opener.put(commander, OpenUI::openActionCreateUI);
                OpenUI.openIntSettingUI(commander);
                return;
            case 17:
                if(isSetTorC.get(commander)) {
                    quest_setting.get(commander).addQuestTarget(quest_action_setting.get(commander));
                } else {
                    quest_setting.get(commander).addQuestAcceptCondition(quest_action_setting.get(commander));
                }
                quest_action_setting.remove(commander);
                OpenUI.openActionSettingUI(commander);
                return;
            default:
        }
    }

    private void workQuestSetIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }

        if(Objects.requireNonNull(inv_click.getClickedInventory()).getSize()==9) {
            //玩家点击的是上面的设置内容
            Objects.requireNonNull(Objects.requireNonNull(inv_click.getClickedInventory().getItem(4)).getItemMeta()).getDisplayName();
            if(inv_click.getSlot() == 8){//isOn
                if(quest_setting.get(commander).isOn()) {
                    if(!quest_setting.get(commander).turnOff()) return;
                    Other.sendInfo("打开了任务"+ quest_setting.get(commander).getQuestName());
                    ItemStack temp = inv_click.getClickedInventory().getItem(2);
                    assert temp != null;
                    temp.setType(Material.BARRIER);
                    inv_click.getClickedInventory().setItem(2,temp);
                } else {
                    if(!quest_setting.get(commander).turnOn()) return;
                    ItemStack temp = inv_click.getClickedInventory().getItem(2);
                    assert temp != null;
                    temp.setType(Material.EMERALD);
                    inv_click.getClickedInventory().setItem(2,temp);
                }
                return;
            }
            if(inv_click.getSlot() == 3){
                Inventory typeset = Bukkit.createInventory(commander,9,ChatColor.GREEN+"选择任务类型");
                typeset.setItem(1, getItem_QuestTypeSetting("MAIN","主线任务",null));
                typeset.setItem(2, getItem_QuestTypeSetting("SIDE","支线任务",null));
                typeset.setItem(3, getItem_QuestTypeSetting("TRIGGER","触发任务",null));
                typeset.setItem(5, getItem_QuestTypeSetting("DAILY","日常任务",null));
                typeset.setItem(6, getItem_QuestTypeSetting("REWARD","悬赏任务",null));
                typeset.setItem(7, getItem_QuestTypeSetting("DIY","自编任务",null));
                setting_state.put(commander, QuestSettingType.QUESTTYPE);
                commander.openInventory(typeset);
                return;
            }

            if(inv_click.getSlot() == 5){//quest position
                Inventory position_set = Bukkit.createInventory(commander,54,ChatColor.GREEN+"父任务设置：请选择父任务" );
                setting_state.put(commander, QuestSettingType.QUESTPOSITION);
                if(quests.size()<=53){
                    for (int i = 0; i < quests.size(); i++) {
                        Quest quest = quests.get(i);
                        position_set.setItem(i+1, OpenUI.getItem("BOOK", quest.getQuestName(), quest.getQuestDescription()));
                    }
                    position_set.setItem(0, non_p());
                    commander.openInventory(position_set);
                } else {
                    OpenUI.openPositionChooseMultiPageUI(commander,1);
                }
                return;
            }
            if(inv_click.getSlot() == 7){//quest_reward
                setting_state.put(commander, QuestSettingType.QUESTREWARD);
                OpenUI.openRewardSettingUI(commander);
                return;
            }
            if(inv_click.getSlot() == 6){//位置目标
                setting_state.put(commander, QuestSettingType.QUESTACTION);
                isSetTorC.put(commander,true);
                OpenUI.openActionSettingUI(commander);
                return;
            }
            if(inv_click.getSlot() == 2){//位置目标
                setting_state.put(commander, QuestSettingType.QUESTACTION);
                isSetTorC.put(commander,false);
                OpenUI.openActionSettingUI(commander);
                return;
            }
            if(inv_click.getSlot() == 0){//删除
                setting_state.remove(commander);
                quest_setting.remove(commander);
                commander.closeInventory();
                commander.sendMessage("已经删除你新建的任务！");
                return;
            }
            if(inv_click.getSlot() == 1){//新建

                commander.closeInventory();
                commander.sendMessage("已经新建任务！");
                quest_setting.get(commander).saveQuestToYml();
                try {
                    quest_yml.save(quest_file);
                } catch (IOException ignored) {
                }
                quests.add(quest_setting.get(commander));
                List<String> a = new ArrayList<>();
                a.add("任务信息如下:");
                try {
                    a.addAll(Other.toStrList(quest_setting.get(commander).serialize()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                a.add("end.");
                for (String s : a) {
                    commander.sendMessage(s);
                }
                quest_setting.remove(commander);
                setting_state.remove(commander);


            }

        }
    }

    private void workCollectorIO(InventoryClickEvent inv_click,Player commander) {
        if(inv_click.getView().getTitle().contains("Page")){
            List<ItemStack> items = item_temp.get(commander);
            int pages = items.size()/45 +1;
            String now_page_str = inv_click.getView().getTitle().split("Page")[1];
            String time = inv_click.getView().getTitle().split("Page")[0];
            now_page_str = now_page_str.split("of")[0];
            now_page_str = now_page_str.split(" ")[1];
            int now_page = Integer.parseInt(now_page_str);
            if(inv_click.getSlot()==45&&now_page!=1){
                OpenUI.uiINIT(inv_click);
                now_page--;
            }
            if(inv_click.getSlot()==53&&now_page!=pages){
                OpenUI.uiINIT(inv_click);
                now_page++;
            }
            if(inv_click.getSlot()!=53&& inv_click.getSlot()!=45) return;
            Inventory temp = Bukkit.createInventory(commander,54,time + "Page "+(now_page)+" of "+pages);
            int now = (now_page-1)*45;
            for (int i = now; (i < now+45)&&(i < items.size()); i++) {
                temp.addItem(items.get(i));
            }
            if(now_page != 1) temp.setItem(45, front_pg);
            if(now_page != pages) temp.setItem(53, next_pg);
            commander.openInventory(temp);
        }
    }

    private void workExpressIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;

        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
            return;
        }

        if(cli.isLeftClick()){
            HashMap<Integer, ItemStack> remains;
            //if(inv_click.getClickedInventory() == null||inv_click.getCurrentItem() == null) return ;
            //点击showInv物品:取回该项
            if(Objects.requireNonNull(inv_click.getClickedInventory()).equals(show_inv.get(commander))) {
                //玩家点击的是showInv

                remains = commander.getInventory().addItem(inv_click.getCurrentItem());
                if(remains.isEmpty())
                    show_inv.get(commander).clear(inv_click.getSlot());
                else show_inv.get(commander).setItem(inv_click.getSlot(),remains.get(0));
            } else {
                //玩家点击的是背包
                remains = show_inv.get(commander).addItem(inv_click.getCurrentItem());
                if(remains.isEmpty())
                    commander.getInventory().clear(inv_click.getSlot());
                else commander.getInventory().setItem(inv_click.getSlot(),remains.get(0));

            }

        }
    }
    
    private void workActionSettingViewIO(InventoryClickEvent inv_click, Player commander) {
        if (OpenUI.uiINIT(inv_click)) return;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }
        if(inv_click.getSlot()==17){
            OpenUI.openQuestSettingUI(commander);
            isSetTorC.remove(commander);
            return;
        }
        if(inv_click.getSlot()==9){
            quest_action_setting.put(commander, new QuestAction());
            OpenUI.openActionCreateUI(commander);
            return;
        }
        if(inv_click.getSlot()==10){
            OpenUI.openActionDeleteUI(commander);
        }
    }

    private boolean workPositionSet(@NotNull String name,Player commander) {
        if(name.equals("无父任务")){
            QuestPosition tmp = new QuestPosition();
            tmp.setParentquest(null);
            tmp.setQuestlevel(1);
            quest_setting.get(commander).setQuestPosition(tmp);
            OpenUI.openQuestSettingUI(commander);

            return true;
        }

        QuestPosition tmp = new QuestPosition();
        tmp.setQuestpositionname(quest_setting.get(commander).getQuestName()+"position");
        tmp.setParentquest(name);
        if(quest_setting.get(commander).isPositionSet()){
            tmp.setChildquest(quest_setting.get(commander).getQuestPosition().getChildquest());
            tmp.setChildquestother1(quest_setting.get(commander).getQuestPosition().getChildquestother1());
            tmp.setChildquestother2(quest_setting.get(commander).getQuestPosition().getChildquestother2());
            tmp.setChildquestother3(quest_setting.get(commander).getQuestPosition().getChildquestother3());
            tmp.setQuestpositionname(quest_setting.get(commander).getQuestPosition().getQuestpositionname());
        }
        quest_setting.get(commander).setQuestPosition(tmp);
        if(getQuest(name).getQuestPosition().getChildquest() == null){
            getQuest(name).getQuestPosition().setChildquest(quest_setting.get(commander).getQuestName());
            quest_setting.get(commander).getQuestPosition().setQuestlevel(getQuest(name).getQuestPosition().getQuestlevel());
            OpenUI.openQuestSettingUI(commander);
            return true;
        }
        if(getQuest(name).getQuestPosition().getChildquestother1() == null){
            getQuest(name).getQuestPosition().setChildquestother1(quest_setting.get(commander).getQuestName());
            quest_setting.get(commander).getQuestPosition().setQuestlevel(getQuest(name).getQuestPosition().getQuestlevel()+1);
            OpenUI.openQuestSettingUI(commander);
            return true;
        }
        if(getQuest(name).getQuestPosition().getChildquestother2() == null){
            getQuest(name).getQuestPosition().setChildquestother2(quest_setting.get(commander).getQuestName());
            quest_setting.get(commander).getQuestPosition().setQuestlevel(getQuest(name).getQuestPosition().getQuestlevel()+1);
            OpenUI.openQuestSettingUI(commander);
            return true;
        }
        if(getQuest(name).getQuestPosition().getChildquestother3() == null){
            getQuest(name).getQuestPosition().setChildquestother3(quest_setting.get(commander).getQuestName());
            quest_setting.get(commander).getQuestPosition().setQuestlevel(getQuest(name).getQuestPosition().getQuestlevel()+1);
            OpenUI.openQuestSettingUI(commander);
            return true;
        }

        return false;
    }

    private @NotNull ItemStack non_p() {
        ItemStack non_p = new ItemStack(Material.BARRIER,1);
        ItemMeta a = non_p.getItemMeta();
        assert a != null;
        a.setDisplayName("无父任务");
        non_p.setItemMeta(a);
        return non_p;
    }

    @SuppressWarnings("SameParameterValue")
    private @NotNull ItemStack getItem_QuestTypeSetting(String typ_name, String dp_name, List<String> lore){
        ItemStack a = new ItemStack(QuestType.valueOf(typ_name).getSymbol(),1);
        ItemMeta b = a.getItemMeta();
        if(b != null) b.setLore(lore);
        assert b != null;
        b.setDisplayName(dp_name);
        a.setItemMeta(b);
        return a;
    }

}

