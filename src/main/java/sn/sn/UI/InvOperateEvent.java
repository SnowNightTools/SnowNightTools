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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sn.sn.Ask.AskSet;
import sn.sn.City.City;
import sn.sn.Express.Express_CE;
import sn.sn.Quest.*;
import sn.sn.Basic.SnFileIO;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static sn.sn.City.City_CE.workCityWarp;
import static sn.sn.Quest.Quest_CE.getQuest;
import static sn.sn.Sn.*;


/*


    express 、 quest 和 collector 的监听器类

    实现了 express show 面板打开后的点击物品处理 和关闭面板时保存文件的处理
    实现了 quest create 面板操作
    实现了 collector bins 面板操作

    作者：LtSeed


*/



public class InvOperateEvent implements Listener {

    public static Player commander;
    public static final ItemStack pg_up = new ItemStack(Material.WRITABLE_BOOK);
    public static final ItemStack cancel = new ItemStack(Material.BARRIER);
    public static final ItemStack confirm = new ItemStack(Material.EMERALD);
    public static final ItemStack pg_dn = new ItemStack(Material.WRITABLE_BOOK);
    public static int show_inv_n_max = 0;

    public InvOperateEvent(){
        ItemMeta confirm_meta = confirm.getItemMeta();
        assert confirm_meta != null;
        confirm_meta.setDisplayName(ChatColor.GREEN+"确认");
        confirm.setItemMeta(confirm_meta);
        ItemMeta cancel_meta = cancel.getItemMeta();
        assert cancel_meta != null;
        cancel_meta.setDisplayName(ChatColor.RED+"取消");
        cancel.setItemMeta(cancel_meta);

        ItemMeta pg_up_meta = pg_up.getItemMeta();
        assert pg_up_meta != null;
        pg_up_meta.setDisplayName(ChatColor.WHITE+"上一页");
        pg_up.setItemMeta(pg_up_meta);
        ItemMeta pg_dn_meta = pg_dn.getItemMeta();
        assert pg_dn_meta != null;
        pg_dn_meta.setDisplayName(ChatColor.WHITE+"下一页");
        pg_dn.setItemMeta(pg_dn_meta);
    }

    private static void putEntitySet(Player commander, Object ignored){
        if(entity_type_setting.containsKey(commander)&& int_setting.containsKey(commander)) {
            quest_action_setting.get(commander).getQuestactiondata().addQuesttargetentity(entity_type_setting.get(commander), int_setting.get(commander));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void InvCloseEvent(InventoryCloseEvent inv_close) {

        commander = Bukkit.getPlayer(inv_close.getPlayer().getUniqueId());
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

        commander = Bukkit.getPlayer(inv_click.getView().getPlayer().getUniqueId());
        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"雪花速递")){
            if (workExpressIO(inv_click)) return;
        }

        if(inv_click.getView().getTitle().contains("Bin")){
            workCollectorIO(inv_click);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"正在创建一个新任务，请填写以下信息")){
            if (workQuestSetIO(inv_click)) return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"创建一个任务条件")){
            workQuestActionSetIO(inv_click);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"任务达成的条件列表")){
            isSetTorC.put(commander,true);
            if (workActionSettingViewIO(inv_click)) return;
        }
        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"任务触发或接受的条件列表")){
            isSetTorC.put(commander,false);
            if (workActionSettingViewIO(inv_click)) return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"选择任务类型")){
            if (workQuestTypeSetIO(inv_click)) return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"任务奖励设置")){
            if (workQuestRewardSetIO(inv_click)) return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"请放入物品奖励")){
            workQuestRewardItemSetIO(inv_click);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"请设置物品条件")){
            workQuestItemConditionSetIO(inv_click);
            return;
        }

        if(inv_click.getView().getTitle().contains("父任务设置：请选择父任务")) {
            if (workParentQuestSetIO(inv_click)) return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"选择实体类型")){
            workQuestEntityTypeSetIO(inv_click);
            return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"选择一种实体")){
            workQuestEntitySetIO(inv_click);
            return;
        }
        if(inv_click.getView().getTitle().equalsIgnoreCase("位置变量设置")){
            if (workLocVarSetIO(inv_click)) return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase(ChatColor.RED+"删除一个任务条件")){
            if (workQuestConditionRemoveIO(inv_click)) return;
        }

        if(inv_click.getView().getTitle().equalsIgnoreCase("整数型变量设置")){
            workIntVarSetIO(inv_click);
            return;
        }

        if(inv_click.getView().getTitle().contains("城市加入申请管理面板")) {
            workCityApplicationAcceptIO(inv_click);
            return;
        }

        if(inv_click.getView().getTitle().contains("Warps List: ")){
            workWarpListIO(inv_click);
            return;
        }

        if(inv_click.getView().getTitle().contains("Residents List: ")){
            workResidentsListIO(inv_click);
            return;
        }

        if(inv_click.getView().getTitle().contains("MyCity")) {
            workMyCityIO(inv_click);
        }
    }

    private void workWarpListIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return;
        City city = City.getCity(commander);
        if (city == null) return;
        String page_str = getPage(inv_click);
        if(page_str == null) return;
        int page = Integer.parseInt(page_str);
        int page_amount = (city.getResidents().size()-1)/45 +1;
        switch (inv_click.getSlot()){
            case 53:
                if(page != page_amount){
                    OpenUI.openCityWarpListUI(commander,page+1);
                }
                return;
            case 45:
                if(page != 1){
                    OpenUI.openCityWarpListUI(commander,page-1);
                }
                return;
            default:
                ItemStack item = inv_click.getCurrentItem();
                if(item==null) return;
                String warp_name = Objects.requireNonNull(item.getItemMeta()).getDisplayName();
                if(warp_name.contains("共有")) return;
                workCityWarp(commander,warp_name,"发送了未知的错误。");
        }
    }

    private void workResidentsListIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return;
        City city = City.getCity(commander);
        if (city == null) return;
        String page_str = getPage(inv_click);
        if(page_str == null) return;
        int page = Integer.parseInt(page_str);
        int page_amount = (city.getResidents().size()-1)/45 +1;
        switch (inv_click.getSlot()){
            case 53:
                if(page != page_amount){
                    OpenUI.openCityResidentsListUI(commander,page+1);
                }
                return;
            case 45:
                if(page != 1){
                    OpenUI.openCityResidentsListUI(commander,page-1);
                }
                return;
            default:
                ItemStack item = inv_click.getCurrentItem();
                if(item==null) return;
                if(item.getItemMeta() instanceof SkullMeta){
                    OfflinePlayer player = ((SkullMeta) item.getItemMeta()).getOwningPlayer();
                    if(player==null) return;
                    if(!player.isOnline()) return;
                    commander.performCommand("/cmi tpa "+ player.getName());
                }
        }
    }

    private void workMyCityIO(InventoryClickEvent inv_click) {
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

    private void workCityApplicationAcceptIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return;
        OfflinePlayer p;
        try {
            ItemMeta itemMeta = Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta();
            p = ((SkullMeta) Objects.requireNonNull(itemMeta)).getOwningPlayer();
        } catch (Exception e) {
            sendError(e.getLocalizedMessage());
            return;
        }
        if (p == null) {
            commander.sendMessage("发生了错误：无法获得玩家信息！");
            return;
        }
        City city = City.checkMayorAndGetCity(commander);
        if (city == null) return;
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
        AskSet.askSetAsync(commander, list,
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

    private void workIntVarSetIO(InventoryClickEvent inv_click) {
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

    private boolean workQuestConditionRemoveIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return true;
        List<QuestAction> target = quest_setting.get(commander).getQuest_accept_condition();
        if(isSetTorC.get(commander)) target = quest_setting.get(commander).getQuest_target();
        if(inv_click.getSlot()==17){
            OpenUI.openActionSettingUI(commander);
            return true;
        }
        try {
            target.remove(inv_click.getSlot());
            if(isSetTorC.get(commander)) quest_setting.get(commander).setQuest_target(target);
            quest_setting.get(commander).setQuest_accept_condition(target);
        } catch (UnsupportedOperationException|IndexOutOfBoundsException|NullPointerException ignore) {
        }
        OpenUI.openActionDeleteUI(commander);
        return false;
    }

    private boolean workLocVarSetIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return true;
        //反转 重设
        int opr = 0;
        String name = Objects.requireNonNull(Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta()).getDisplayName();

        if (name.contains("X")){
            if(name.contains("反转")){
                loc_setting.get(commander).revXm();
                OpenUI.openLocSettingUI(commander);
                return true;
            }
            if(name.contains("重设")){
                loc_setting.get(commander).setX(0);
                OpenUI.openLocSettingUI(commander);
                return true;
            }
            name = name.substring(1);
            try {
                opr = Integer.parseInt(name);
            } catch (NumberFormatException ignored) {
            }
            if(opr != 0){
                loc_setting.get(commander).addX(opr);
                OpenUI.openLocSettingUI(commander);
                return true;
            }
        }
        if (name.contains("Y")){
            if(name.contains("反转")){
                loc_setting.get(commander).revYm();
                OpenUI.openLocSettingUI(commander);
                return true;
            }
            if(name.contains("重设")){
                loc_setting.get(commander).setY(0);
                OpenUI.openLocSettingUI(commander);
                return true;
            }
            name = name.substring(1);
            try {
                opr = Integer.parseInt(name);
            } catch (NumberFormatException ignored) {
            }
            if(opr != 0){
                loc_setting.get(commander).addY(opr);
                OpenUI.openLocSettingUI(commander);
                return true;
            }
        }
        if (name.contains("Z")){
            if(name.contains("反转")){
                loc_setting.get(commander).revZm();
                OpenUI.openLocSettingUI(commander);
                return true;
            }
            if(name.contains("重设")){
                loc_setting.get(commander).setZ(0);
                OpenUI.openLocSettingUI(commander);
                return true;
            }
            name = name.substring(1);
            try {
                opr = Integer.parseInt(name);
            } catch (NumberFormatException ignored) {
            }
            if(opr != 0){
                loc_setting.get(commander).addZ(opr);
                OpenUI.openLocSettingUI(commander);
                return true;
            }
        }
        List<World> tmp_lw = Bukkit.getWorlds();
        for (World world : tmp_lw) {
            if(name.equalsIgnoreCase(world.getName())){
                loc_setting.get(commander).setWorld(world);
                OpenUI.openLocSettingUI(commander);
                return true;
            }
        }
        try {
            if(inv_click.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.RED+"取消")){
                setting.remove(commander);
                ui_opener.get(commander).accept(commander);
                ui_opener.remove(commander);
                return true;
            }
            if(inv_click.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN+"确认")){
                setting.get(commander).accept(loc_setting.get(commander).getLoc());
                setting.remove(commander);
                ui_opener.get(commander).accept(commander);
                ui_opener.remove(commander);
                return true;
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
                return true;
            }
            for (int i = 0, tmp_lw_size = tmp_lw.size(); i < tmp_lw_size; i++) {
                World world = tmp_lw.get(i);

                tmp_dn.setItem(i, OpenUI.getItem("GRASS_BLOCK",world.getName(), Collections.singletonList(world.getUID().toString())));
            }
            tmp_dn.setItem(52,cancel);
            tmp_dn.setItem(53,confirm);
            commander.openInventory(tmp_dn);
            return true;
        }
        return false;
    }

    private void workQuestEntitySetIO(InventoryClickEvent inv_click) {
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

    private void workQuestEntityTypeSetIO(InventoryClickEvent inv_click) {
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

    private boolean workParentQuestSetIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return true;
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
                return true;
            }
            if (workPositionSet(name)) return true;
            OpenUI.openQuestSettingUI(commander);
            return true;

        }

        if (Objects.requireNonNull(inv_click.getClickedInventory()).contains(pg_up)||Objects.requireNonNull(inv_click.getClickedInventory()).contains(pg_dn)) {
            //玩家点击的是上面的设置内容
            int pg_index = Integer.parseInt(inv_click.getView().getTitle(), inv_click.getView().getTitle().indexOf("第")+1, inv_click.getView().getTitle().indexOf("页"),10);
            String name;
            try {
                name = Objects.requireNonNull(Objects.requireNonNull(inv_click.getCurrentItem()).getItemMeta()).getDisplayName();
            } catch (NullPointerException e){
                return true;
            }

            if(name.equals("上一页")){
                OpenUI.openPositionChooseMultiPageUI(commander, pg_index + 1);
                return true;
            }
            if(name.equals("下一页")){
                OpenUI.openPositionChooseMultiPageUI(commander, pg_index - 1);
                return true;
            }
            if (workPositionSet(name)) return true;
            OpenUI.openQuestSettingUI(commander);
            return true;
        }
        return false;
    }

    private void workQuestItemConditionSetIO(InventoryClickEvent inv_click) {
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

        List<ItemStack> list = quest_action_setting.get(commander).getQuestactiondata().getQuesttargetitem();

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
        quest_action_setting.get(commander).getQuestactiondata().setQuesttargetitem(list);
    }

    private void workQuestRewardItemSetIO(InventoryClickEvent inv_click) {
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

        QuestReward tmp_qr = quest_setting.get(commander).getQuestreward();
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
        quest_setting.get(commander).setQuestreward(tmp_qr);
    }

    private boolean workQuestRewardSetIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return true;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli.equals(ClickType.WINDOW_BORDER_LEFT)||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }

        if(Objects.requireNonNull(inv_click.getClickedInventory()).getSize()==9) {
            //玩家点击的是上面的设置内容

            QuestReward tmp_qr = quest_setting.get(commander).getQuestreward();
            if(tmp_qr ==null)
                tmp_qr = new QuestReward();
            switch (inv_click.getSlot()){
                case 0:
                    tmp_qr.setAdmin(true);
                    OpenUI.openRewardSettingUI(commander);
                    quest_setting.get(commander).setQuestreward(tmp_qr);
                    return true;
                case 3:
                    OpenUI.openIntSettingUI(commander);
                    ui_opener.put(commander, OpenUI::openRewardSettingUI);
                    return true;
                case 5:
                    Inventory reward_item_setting = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请放入物品奖励");
                    reward_item_setting.setContents(tmp_qr.getRewarditems().toArray(new ItemStack[9]));
                    reward_item_setting.setItem(17,confirm);
                    commander.openInventory(reward_item_setting);
                    return true;
                case 8:
                    OpenUI.openQuestSettingUI(commander);
                    return true;
                default:
                    return true;
            }
        }
        return false;
    }

    private boolean workQuestTypeSetIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return true;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }

        if(Objects.requireNonNull(inv_click.getClickedInventory()).getSize()==9) {
            //玩家点击的是上面的设置内容
            switch (inv_click.getSlot()){
                case 1:
                    quest_setting.get(commander).setQuesttype(QuestType.MAIN);
                    break;
                case 2:
                    quest_setting.get(commander).setQuesttype(QuestType.SIDE);
                    break;
                case 3:
                    quest_setting.get(commander).setQuesttype(QuestType.TRIGGER);
                    break;
                case 5:
                    quest_setting.get(commander).setQuesttype(QuestType.DAILY);
                    break;
                case 6:
                    quest_setting.get(commander).setQuesttype(QuestType.REWARD);
                    break;
                case 7:
                    quest_setting.get(commander).setQuesttype(QuestType.DIY);
                    break;
                default:
                    return true;
            }

            OpenUI.openQuestSettingUI(commander);
            return true;

        }
        return false;
    }

    private void workQuestActionSetIO(InventoryClickEvent inv_click) {
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
                quest_action_setting.get(commander).setQuestactiontype(QuestActionType.getFromInt(inv_click.getSlot()+1));
                OpenUI.openActionCreateUI(commander);
                return;
            case 9:
                List<ItemStack> quest_target_item = quest_action_setting.get(commander).getQuestactiondata().getQuesttargetitem();
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
                setting.put(commander, loc -> quest_action_setting.get(commander).getQuestactiondata().setTargetlocation((Location) loc));
                ui_opener.put(commander, OpenUI::openActionCreateUI);
                OpenUI.openLocSettingUI(commander);
                return;
            case 13:
                setting.put(commander, set -> quest_action_setting.get(commander).getQuestactiondata().setQuesttimelimit((Integer) set));
                ui_opener.put(commander, OpenUI::openActionCreateUI);
                OpenUI.openIntSettingUI(commander);
                return;
            case 17:
                if(isSetTorC.get(commander)) {
                    quest_setting.get(commander).addQuesttarget(quest_action_setting.get(commander));
                } else {
                    quest_setting.get(commander).addQuestacceptcondition(quest_action_setting.get(commander));
                }
                quest_action_setting.remove(commander);
                OpenUI.openActionSettingUI(commander);
                return;
            default:
        }
    }

    private boolean workQuestSetIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return true;
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
                    if(!quest_setting.get(commander).turnOff()) return true;
                    sendInfo("打开了任务"+ quest_setting.get(commander).getQuest_name());
                    ItemStack temp = inv_click.getClickedInventory().getItem(2);
                    assert temp != null;
                    temp.setType(Material.BARRIER);
                    inv_click.getClickedInventory().setItem(2,temp);
                } else {
                    if(!quest_setting.get(commander).turnOn()) return true;
                    ItemStack temp = inv_click.getClickedInventory().getItem(2);
                    assert temp != null;
                    temp.setType(Material.EMERALD);
                    inv_click.getClickedInventory().setItem(2,temp);
                }
                return true;
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
                return true;
            }

            if(inv_click.getSlot() == 5){//quest position
                Inventory position_set = Bukkit.createInventory(commander,54,ChatColor.GREEN+"父任务设置：请选择父任务" );
                setting_state.put(commander, QuestSettingType.QUESTPOSITION);
                if(quests.size()<=53){
                    for (int i = 0; i < quests.size(); i++) {
                        Quest quest = quests.get(i);
                        position_set.setItem(i+1, OpenUI.getItem("BOOK", quest.getQuest_name(), quest.getQuest_description()));
                    }
                    position_set.setItem(0, non_p());
                    commander.openInventory(position_set);
                } else {
                    OpenUI.openPositionChooseMultiPageUI(commander,1);
                }
                return true;
            }
            if(inv_click.getSlot() == 7){//quest_reward
                setting_state.put(commander, QuestSettingType.QUESTREWARD);
                OpenUI.openRewardSettingUI(commander);
                return true;
            }
            if(inv_click.getSlot() == 6){//位置目标
                setting_state.put(commander, QuestSettingType.QUESTACTION);
                isSetTorC.put(commander,true);
                OpenUI.openActionSettingUI(commander);
                return true;
            }
            if(inv_click.getSlot() == 2){//位置目标
                setting_state.put(commander, QuestSettingType.QUESTACTION);
                isSetTorC.put(commander,false);
                OpenUI.openActionSettingUI(commander);
                return true;
            }
            if(inv_click.getSlot() == 0){//删除
                setting_state.remove(commander);
                quest_setting.remove(commander);
                commander.closeInventory();
                commander.sendMessage("已经删除你新建的任务！");
                return true;
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
                    a.addAll(toStrList(quest_setting.get(commander).serialize()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                a.add("end.");
                for (String s : a) {
                    commander.sendMessage(s);
                }
                quest_setting.remove(commander);
                setting_state.remove(commander);


                return true;
            }

        }
        return false;
    }

    private void workCollectorIO(InventoryClickEvent inv_click) {
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
            if(now_page != 1) temp.setItem(45, pg_up);
            if(now_page != pages) temp.setItem(53, pg_dn);
            commander.openInventory(temp);
        }
    }

    private boolean workExpressIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return true;

        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
            return true;
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
        return false;
    }
    
    private boolean workActionSettingViewIO(InventoryClickEvent inv_click) {
        if (OpenUI.uiINIT(inv_click)) return true;
        ClickType cli = inv_click.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }
        if(inv_click.getSlot()==17){
            OpenUI.openQuestSettingUI(commander);
            isSetTorC.remove(commander);
            return true;
        }
        if(inv_click.getSlot()==9){
            quest_action_setting.put(commander, new QuestAction());
            OpenUI.openActionCreateUI(commander);
            return true;
        }
        if(inv_click.getSlot()==10){
            OpenUI.openActionDeleteUI(commander);
            return true;
        }
        return false;
    }

    private boolean workPositionSet(@NotNull String name) {
        if(name.equals("无父任务")){
            QuestPosition tmp = new QuestPosition();
            tmp.setParentquest(null);
            tmp.setQuestlevel(1);
            quest_setting.get(commander).setQuestposition(tmp);
            OpenUI.openQuestSettingUI(commander);

            return true;
        }

        QuestPosition tmp = new QuestPosition();
        tmp.setQuestpositionname(quest_setting.get(commander).getQuest_name()+"position");
        tmp.setParentquest(name);
        if(quest_setting.get(commander).isPositionSet()){
            tmp.setChildquest(quest_setting.get(commander).getQuestposition().getChildquest());
            tmp.setChildquestother1(quest_setting.get(commander).getQuestposition().getChildquestother1());
            tmp.setChildquestother2(quest_setting.get(commander).getQuestposition().getChildquestother2());
            tmp.setChildquestother3(quest_setting.get(commander).getQuestposition().getChildquestother3());
            tmp.setQuestpositionname(quest_setting.get(commander).getQuestposition().getQuestpositionname());
        }
        quest_setting.get(commander).setQuestposition(tmp);
        if(getQuest(name).getQuestposition().getChildquest() == null){
            getQuest(name).getQuestposition().setChildquest(quest_setting.get(commander).getQuest_name());
            quest_setting.get(commander).getQuestposition().setQuestlevel(getQuest(name).getQuestposition().getQuestlevel());
            OpenUI.openQuestSettingUI(commander);
            return true;
        }
        if(getQuest(name).getQuestposition().getChildquestother1() == null){
            getQuest(name).getQuestposition().setChildquestother1(quest_setting.get(commander).getQuest_name());
            quest_setting.get(commander).getQuestposition().setQuestlevel(getQuest(name).getQuestposition().getQuestlevel()+1);
            OpenUI.openQuestSettingUI(commander);
            return true;
        }
        if(getQuest(name).getQuestposition().getChildquestother2() == null){
            getQuest(name).getQuestposition().setChildquestother2(quest_setting.get(commander).getQuest_name());
            quest_setting.get(commander).getQuestposition().setQuestlevel(getQuest(name).getQuestposition().getQuestlevel()+1);
            OpenUI.openQuestSettingUI(commander);
            return true;
        }
        if(getQuest(name).getQuestposition().getChildquestother3() == null){
            getQuest(name).getQuestposition().setChildquestother3(quest_setting.get(commander).getQuest_name());
            quest_setting.get(commander).getQuestposition().setQuestlevel(getQuest(name).getQuestposition().getQuestlevel()+1);
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

    public static class LocSet{
        private int x;
        private int y;
        private int z;
        private boolean xm = true;
        private boolean ym = true;
        private boolean zm = true;
        private World world;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public boolean isXm() {
            return xm;
        }

        public boolean isYm() {
            return ym;
        }

        public boolean isZm() {
            return zm;
        }

        public void setWorld(World world) {
            this.world = world;
        }
        public void revXm(){
            this.xm = !this.xm;
        }
        public void revYm(){
            this.ym = !this.ym;
        }
        public void revZm(){
            this.zm = !this.zm;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }

        public void setZ(int z) {
            this.z = z;
        }

        public void addX(int x) {
            this.x += x;
        }

        public void addY(int y) {
            this.y += y;
        }

        public void addZ(int z) {
            this.z += z;
        }

        public World getWorld() {
            return world;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LocSet)) return false;
            LocSet locSet = (LocSet) o;
            return getX() == locSet.getX() && getY() == locSet.getY() && getZ() == locSet.getZ() && isXm() == locSet.isXm() && isYm() == locSet.isYm() && isZm() == locSet.isZm() && Objects.equals(getWorld(), locSet.getWorld());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getX(), getY(), getZ(), isXm(), isYm(), isZm(), getWorld());
        }

        @Override
        public String toString() {
            return "LocSet{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    ", xm=" + xm +
                    ", ym=" + ym +
                    ", zm=" + zm +
                    ", world=" + world +
                    '}';
        }

        public LocSet(Location l){
            this.x = (int) l.getX();
            this.y = (int) l.getY();
            this.z = (int) l.getZ();
            this.world = l.getWorld();
        }

        public Location getLoc() {
            return new Location(world,x,y,z);
        }
    }

}

