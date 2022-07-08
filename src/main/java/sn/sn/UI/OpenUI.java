package sn.sn.UI;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import sn.sn.Basic.LocSet;
import sn.sn.Basic.Other;
import sn.sn.City.CITY_TYPE;
import sn.sn.City.City;
import sn.sn.City.CityPermissionItemStack;
import sn.sn.Quest.Quest;
import sn.sn.Quest.QuestAction;
import sn.sn.Quest.QuestActionType;
import sn.sn.Quest.QuestSettingType;

import java.util.*;

import static com.google.common.primitives.Ints.min;
import static sn.sn.Basic.SnFileIO.getSkull;
import static sn.sn.Sn.*;
import static sn.sn.UI.InvOperateEvent.*;

//@SuppressWarnings("SpellCheckingInspection")
public class OpenUI {


    public static void openQuestSettingUI(Player questPlayer, String quest_name) {
        try {
            setting_state.remove(questPlayer);
        } catch (NullPointerException ignored) {
        }
        Inventory quest_create = Bukkit.createInventory(questPlayer, 9, ChatColor.BLUE + "正在创建一个新任务，请填写以下信息");


        ItemStack name_icon = new ItemStack(Material.BOOK);
        ItemMeta name_icon_meta = name_icon.getItemMeta();
        assert name_icon_meta != null;
        name_icon_meta.setDisplayName(quest_name);
        List<String> name_lore = new ArrayList<>();
        name_lore.add(ChatColor.RED + "任务名应该不含空格、\\、");
        name_lore.add(ChatColor.RED + "单双引号、英文句点等特殊字符");
        name_lore.add(ChatColor.RED + "任务名应该不以数字开头");
        name_lore.add(ChatColor.RED + "任务名应该独一无二");
        name_lore.add(ChatColor.YELLOW + "任务名最好不含中文字符");
        name_lore.add(ChatColor.YELLOW + "否则可能会出现意料之外的编码错误");
        name_lore.add(ChatColor.GREEN + "任务名最好与它的内容相对应");
        name_lore.add("例子：FirstQuestOfSnowNightTown");
        name_icon_meta.addEnchant(Enchantment.DIG_SPEED, 1, false);
        name_icon_meta.setLore(name_lore);
        name_icon.setItemMeta(name_icon_meta);
        quest_create.setItem(4, name_icon);


        ItemStack position_icon = new ItemStack(QuestSettingType.QUESTPOSITION.getSymbol());
        ItemMeta position_icon_meta = position_icon.getItemMeta();
        assert position_icon_meta != null;
        position_icon_meta.setDisplayName("任务位置");
        List<String> position_lore = new ArrayList<>();
        position_lore.add(ChatColor.GREEN + "点我设置任务位置");
        if (quest_setting.get(questPlayer).isPositionSet()) {
            position_lore.add(ChatColor.GREEN + "父任务" + quest_setting.get(questPlayer).getQuestPosition().getParentquest());
            position_lore.add(ChatColor.GREEN + "子任务" + quest_setting.get(questPlayer).getQuestPosition().getChildquest());
            position_lore.add(ChatColor.GREEN + "任务等级" + quest_setting.get(questPlayer).getQuestPosition().getQuestlevel());
            position_icon_meta.addEnchant(Enchantment.LUCK, 1, false);
        }
        position_icon_meta.setLore(position_lore);
        position_icon.setItemMeta(position_icon_meta);
        quest_create.setItem(5, position_icon);


        ItemStack type_icon = new ItemStack(Material.ARROW);
        ItemMeta type_icon_meta = type_icon.getItemMeta();
        assert type_icon_meta != null;
        type_icon_meta.setDisplayName("任务类型");
        List<String> type_lore = new ArrayList<>();
        type_lore.add(ChatColor.GREEN + "点我设置任务类型");
        if (quest_setting.get(questPlayer).isTypeSet()) {
            type_lore.add(ChatColor.GREEN + quest_setting.get(questPlayer).getQuestType().getKey().getKey());
            type_icon_meta.addEnchant(Enchantment.LUCK, 1, false);
            type_icon.setType(quest_setting.get(questPlayer).getQuestType().getSymbol());
        }
        type_icon_meta.setLore(type_lore);
        type_icon.setItemMeta(type_icon_meta);
        quest_create.setItem(3, type_icon);


        ItemStack target_icon = new ItemStack(QuestSettingType.QUESTACTION.getSymbol());
        ItemMeta target_icon_meta = target_icon.getItemMeta();
        assert target_icon_meta != null;
        target_icon_meta.setDisplayName("任务目标");
        List<String> target_lore = new ArrayList<>();
        target_lore.add(ChatColor.GREEN + "点我设置任务目标");
        if (quest_setting.get(questPlayer).isTargetSet()) {
            for (QuestAction action : quest_setting.get(questPlayer).getQuest_target()) {
                target_lore.addAll(Other.toStrList(action.serialize()));
            }
            target_icon_meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, false);
        }
        target_icon_meta.setLore(target_lore);
        target_icon.setItemMeta(target_icon_meta);
        quest_create.setItem(6, target_icon);


        ItemStack icon = new ItemStack(QuestSettingType.QUESTACTION.getSymbol());
        ItemMeta icon_meta = icon.getItemMeta();
        assert icon_meta != null;
        icon_meta.setDisplayName("任务接受条件");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "点我设置任务接受条件");
        lore.add("如果没有特别的任务接受条件，");
        lore.add("也请将其设置为“默认“！");
        if (quest_setting.get(questPlayer).isAcceptConditionSet()) {
            for (QuestAction action : quest_setting.get(questPlayer).getQuestAcceptCondition()) {
                lore.addAll(Other.toStrList(action.serialize()));
            }
            icon_meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, false);
        }
        icon_meta.setLore(lore);
        icon.setItemMeta(icon_meta);
        quest_create.setItem(2, icon);


        ItemStack reward_icon = new ItemStack(QuestSettingType.QUESTREWARD.getSymbol());
        ItemMeta reward_icon_meta = reward_icon.getItemMeta();
        assert reward_icon_meta != null;
        reward_icon_meta.setDisplayName("任务奖励");
        List<String> reward_lore = new ArrayList<>();
        reward_lore.add(ChatColor.GREEN + "点我设置任务奖励");
        if (quest_setting.get(questPlayer).isRewardSet()) {
            reward_lore.addAll(Other.toStrList(quest_setting.get(questPlayer).getQuestReward().serialize()));
            reward_icon_meta.addEnchant(Enchantment.CHANNELING, 1, false);
        }
        reward_icon_meta.setLore(reward_lore);
        reward_icon.setItemMeta(reward_icon_meta);
        quest_create.setItem(7, reward_icon);


        ItemStack on_icon = new ItemStack(Material.BARRIER);
        ItemMeta on_icon_meta = on_icon.getItemMeta();
        assert on_icon_meta != null;
        on_icon_meta.setDisplayName("任务试运行");
        List<String> on_lore = new ArrayList<>();

        if (quest_setting.get(questPlayer).isOn()) {
            on_icon_meta.addEnchant(Enchantment.CHANNELING, 1, false);
            on_icon.setType(Material.EMERALD);
            on_lore.add(ChatColor.GREEN + "任务现在正在运行！");
            on_lore.add(ChatColor.RED + "左键关闭任务！");
        } else {
            on_lore.add(ChatColor.GREEN + "任务现在没有运行！");
            on_lore.add(ChatColor.RED + "左键打开任务！");
            on_lore.add(ChatColor.RED + "请在打开任务前确定所有参数已经被充分设置！");
        }
        on_icon_meta.setLore(on_lore);
        on_icon.setItemMeta(on_icon_meta);
        quest_create.setItem(8, on_icon);


        ItemStack cf_icon = new ItemStack(Material.EMERALD);
        ItemMeta cf_icon_meta = cf_icon.getItemMeta();
        assert cf_icon_meta != null;
        cf_icon_meta.setDisplayName("确认并创建任务");
        List<String> cf_lore = new ArrayList<>();
        cf_lore.add(ChatColor.GREEN + "点我确认并创建任务");
        cf_lore.add("请确认所有状态准确地、充分地被设置！");
        cf_lore.add("建议先试运行任务");
        cf_lore.add("新建的任务的运行状态继承试运行状态");
        cf_icon_meta.setLore(cf_lore);
        cf_icon.setItemMeta(cf_icon_meta);
        quest_create.setItem(1, cf_icon);


        ItemStack c_icon = new ItemStack(Material.BARRIER);
        ItemMeta c_icon_meta = c_icon.getItemMeta();
        assert c_icon_meta != null;
        c_icon_meta.setDisplayName("任务接受条件");
        List<String> c_lore = new ArrayList<>();
        c_lore.add(ChatColor.RED + "取消新建这个任务！");
        c_lore.add(ChatColor.RED + "这会导致你刚才所完成的工作全部被删除！");
        c_icon_meta.setLore(c_lore);
        c_icon.setItemMeta(c_icon_meta);
        quest_create.setItem(0, c_icon);


        questPlayer.openInventory(quest_create);
    }

    public static void openQuestSettingUI(Player questPlayer){
        openQuestSettingUI(questPlayer, quest_setting.get(questPlayer).getQuestName());
    }

    public static void openActionCreateUI(Player commander) {

        Inventory tmp_ac_ui = Bukkit.createInventory(commander, 18, ChatColor.GREEN + "创建一个任务条件");
        int i = 0;
        for (QuestActionType value : QuestActionType.values()) {
            tmp_ac_ui.setItem(i++, getItem("PAPER", value.getKey().getKey(), null, quest_action_setting.get(commander).getQuest_action_type().getKey() == (value).getKey()));
        }
        List<String> a = new ArrayList<>();
        if(!quest_action_setting.get(commander).getQuest_action_data().getQuesttargetitem().isEmpty())
            for (ItemStack itemStack : quest_action_setting.get(commander).getQuest_action_data().getQuesttargetitem()) {
                a.add(itemStack.serialize().toString());
            }
        tmp_ac_ui.setItem(9, getItem("GRASS", "设置物品信息", a, !quest_action_setting.get(commander).getQuest_action_data().getQuesttargetitem().isEmpty()));
        a.clear();
        if(!quest_action_setting.get(commander).getQuest_action_data().getQuesttargetentity().isEmpty())
            for (EntityType key : quest_action_setting.get(commander).getQuest_action_data().getQuesttargetentity().keySet()) {
                a.add(key.name()+' '+ quest_action_setting.get(commander).getQuest_action_data().getQuesttargetentity().get(key));
            }
        tmp_ac_ui.setItem(10, getItem("SPAWNER", "设置实体信息", a, !quest_action_setting.get(commander).getQuest_action_data().getQuesttargetentity().isEmpty()));

        tmp_ac_ui.setItem(11, getItem("VILLAGER_SPAWN_EGG", "设置npc信息", null, quest_action_setting.get(commander).getQuest_action_data().getQuesttargetnpc() != null));

        a.clear();
        if (quest_action_setting.get(commander).getQuest_action_data().getTargetlocation() != null)
            a.add(quest_action_setting.get(commander).getQuest_action_data().getTargetlocation().toString());
        tmp_ac_ui.setItem(12, getItem("COMPASS", "设置位置信息", a, !quest_action_setting.get(commander).getQuest_action_data().isLocSet()));
        a.clear();
        a.add(String.valueOf(quest_action_setting.get(commander).getQuest_action_data().getQuesttimelimit()));
        tmp_ac_ui.setItem(13, getItem("CLOCK", "设置时间限制", a, quest_action_setting.get(commander).getQuest_action_data().getQuesttimelimit() != -1));
        tmp_ac_ui.setItem(17, InvOperateEvent.confirm);
        commander.openInventory(tmp_ac_ui);
    }

    public static void openActionSettingUI(Player commander) {
        String view_name;
        if (isSetTorC.get(commander)) view_name = ChatColor.GREEN + "任务达成的条件列表";
        else view_name = ChatColor.GREEN + "任务触发或接受的条件列表";
        Inventory tmp_target = Bukkit.createInventory(commander, 18, view_name);
        List<QuestAction> target;
        if (isSetTorC.get(commander)) {
            target = quest_setting.get(commander).getQuest_target();
        } else target = quest_setting.get(commander).getQuestAcceptCondition();
        if (target.size() != 0)
            for (int i = 0; i < target.size(); i++) {
                ItemStack tmp_is = new ItemStack(Material.REDSTONE, 1);
                setTempItemMeta(tmp_target, target, i, tmp_is);
            }

        tmp_target.setItem(9, getItem("DIAMOND_PICKAXE", "添加一个条件", null));
        tmp_target.setItem(10, getItem("DIAMOND_SHOVEL", "删除一个条件", null));
        tmp_target.setItem(17, InvOperateEvent.confirm);
        commander.openInventory(tmp_target);
    }

    private static void setTempItemMeta(Inventory tmp_target, List<QuestAction> target, int i, ItemStack tmp_is) {
        ItemMeta tmp_itm = tmp_is.getItemMeta();
        assert tmp_itm != null;
        tmp_itm.setDisplayName(target.get(i).getQuest_action_name());
        List<String> a = new ArrayList<>();
        for (String s : target.get(i).serialize().keySet()) {
            a.add(s + "->" + target.get(i).serialize().get(s).toString());
        }
        tmp_itm.setLore(a);
        tmp_is.setItemMeta(tmp_itm);
        tmp_target.setItem(i, tmp_is);
    }

    public static void openPositionChooseMultiPageUI(Player commander, int page) {
        Inventory position_set = Bukkit.createInventory(commander, 54, ChatColor.GREEN + "父任务设置：请选择父任务(第" + page + "页)");
        int now = 45 * (page - 1);

        for (int i = 0; i < 45; i++) {
            Quest quest = quests.get(i);
            position_set.setItem(now + i, getItem("BOOK", quest.getQuestName(), quest.getQuestDescription()));
        }
        if (page != 1) position_set.setItem(45, front_pg);
        if (quests.size() > now + 45) position_set.setItem(53, InvOperateEvent.next_pg);
        commander.openInventory(position_set);
    }

    public static void openActionDeleteUI(Player commander) {
        Inventory tmp_ac_ui = Bukkit.createInventory(commander, 18, ChatColor.RED + "删除一个任务条件");
        List<QuestAction> target = quest_setting.get(commander).getQuestAcceptCondition();
        if (isSetTorC.get(commander)) target = quest_setting.get(commander).getQuest_target();
        if (target.size() != 0)
            for (int i = 0; i < target.size(); i++) {
                ItemStack tmp_is = new ItemStack(Material.BARRIER, 1);
                setTempItemMeta(tmp_ac_ui, target, i, tmp_is);
            }
        tmp_ac_ui.setItem(17, InvOperateEvent.cancel);
        commander.openInventory(tmp_ac_ui);
    }

    public static void openRewardSettingUI(Player commander1) {
        Inventory reward_set = Bukkit.createInventory(commander1, 9, ChatColor.GREEN + "任务奖励设置");
        List<String> a = new ArrayList<>();
        a.add("设置为系统任务");
        a.add("系统任务在发放奖励时不会收取创建人物品");

        reward_set.setItem(0, getItem("REDSTONE_BLOCK", "系统任务", a, quest_setting.get(commander1).getQuestReward().isAdmin()));
        a = new ArrayList<>();
        a.add("设置经济奖励");
        a.add(String.valueOf(quest_setting.get(commander1).getQuestReward().getRewardmoney()));
        reward_set.setItem(3, getItem("EMERALD_BLOCK", "经济奖励", a, quest_setting.get(commander1).getQuestReward().getRewardmoney() != 0));
        a = new ArrayList<>();
        a.add("设置物品奖励");
        for (ItemStack reward_item : quest_setting.get(commander1).getQuestReward().getRewarditems()) {
            a.add(reward_item.serialize().toString());
        }
        reward_set.setItem(5, getItem("DIAMOND_BLOCK", "物品奖励", a, !quest_setting.get(commander1).getQuestReward().getRewarditems().isEmpty()));
        reward_set.setItem(8, InvOperateEvent.confirm);
        commander1.openInventory(reward_set);
    }

    public static void openEntityTypeSettingUI(Player commander) {
        Inventory tmp_ets_ui = Bukkit.createInventory(commander, 54, ChatColor.GREEN + "选择实体类型");
        int i = 1, j = 0;
        List<String> tmp_lore = new ArrayList<>();
        tmp_lore.add("这一组有下列实体：");

        for (EntityType entitytype : EntityType.values()) {
            tmp_lore.add(entitytype.name());
            if (i == 8) {
                tmp_ets_ui.setItem(j, getItem("CHEST", "第" + (j + 1) + "组实体类型", tmp_lore));
                tmp_lore.clear();
                tmp_lore.add("这一组有下列实体：");
                i = 1;
                j++;
            } else i++;
        }
        if (entity_type_setting.get(commander) != null) {
            ItemStack tmp_con = InvOperateEvent.confirm.clone();
            List<String> tmp_list = new ArrayList<>();
            tmp_list.add("已经设置的实体类型：");
            tmp_list.add(entity_type_setting.get(commander).name());
            ItemMeta tmp_im = tmp_con.getItemMeta();
            assert tmp_im != null;
            tmp_im.setLore(tmp_list);
            tmp_con.setItemMeta(tmp_im);
            tmp_ets_ui.setItem(53, InvOperateEvent.confirm);
        } else tmp_ets_ui.setItem(53, InvOperateEvent.cancel);

        commander.openInventory(tmp_ets_ui);

    }

    public static long powerTen(int mi) {
        long res = 10;
        for (int i = 0; i < mi; i++) {
            res *= 10;
        }
        return res;
    }

    public static void openLocSettingUI(Player commander) {
        if(!loc_setting.containsKey(commander)){
            loc_setting.put(commander, new LocSet(Bukkit.getWorlds().get(0).getSpawnLocation()));
        }
        LocSet ls = loc_setting.get(commander);
        Inventory tmp_ls = Bukkit.createInventory(commander, 54, "位置变量设置");
        List<String> a = new ArrayList<>();
        a.add("现在的位置信息:");
        a.add("世界:" + ls.getWorld());
        a.add("x:" + ls.getX());
        a.add("y:" + ls.getY());
        a.add("z:" + ls.getZ());

        tmp_ls.setItem(0, getItem("COMPASS", "反转X设置方向", a, ls.isXm()));
        tmp_ls.setItem(2, getItem("COMPASS", "反转Y设置方向", a, ls.isYm()));
        tmp_ls.setItem(4, getItem("COMPASS", "反转Z设置方向", a, ls.isZm()));

        tmp_ls.setItem(1, getItem("BARRIER", "重设X(归零)", a));
        tmp_ls.setItem(3, getItem("BARRIER", "重设Y(归零)", a));
        tmp_ls.setItem(5, getItem("BARRIER", "重设Z(归零)", a));

        char mX;
        if (ls.isXm()) mX = '+';
        else mX = '-';
        char mY;
        if (ls.isYm()) mY = '+';
        else mY = '-';
        char mZ;
        if (ls.isZm()) mZ = '+';
        else mZ = '-';

        for (int i = 0; i < 5; i++) {
            tmp_ls.setItem((i + 1) * 9, getItem("PAPER", "X" + mX + powerTen(i), a));
            tmp_ls.setItem((i + 1) * 9 + 1, getItem("PAPER", "X" + mX + 5 * powerTen(i), a));
            tmp_ls.setItem((i + 1) * 9 + 2, getItem("PAPER", "Y" + mY + powerTen(i), a));
            tmp_ls.setItem((i + 1) * 9 + 3, getItem("PAPER", "Y" + mY + 5 * powerTen(i), a));
            tmp_ls.setItem((i + 1) * 9 + 4, getItem("PAPER", "Z" + mZ + powerTen(i), a));
            tmp_ls.setItem((i + 1) * 9 + 5, getItem("PAPER", "Z" + mZ + 5 * powerTen(i), a));
        }
        List<World> lw = Bukkit.getWorlds();
        int world_size = Bukkit.getWorlds().size();
        if (world_size <= 12) {
            int now = 6, line = 0;
            for (int i = 0, lwSize = lw.size(); i < lwSize; i++, now++) {
                if (now == 9) {
                    now = 6;
                    line++;
                }
                World world = lw.get(i);
                tmp_ls.setItem(now + 9 * line, getItem("GRASS", world.getName(), a, ls.getWorld() == world));
            }
        } else {
            int now = 6,line = 0;
            for (int i = 0; i < 12; i++, now++) {
                if (now == 9) {
                    now = 6;
                    line++;
                }
                World world = lw.get(i);
                tmp_ls.setItem(now + 9 * line, getItem("GRASS", world.getName(), a, ls.getWorld() == world));
            }
            tmp_ls.setItem(51, InvOperateEvent.next_pg);
        }
        tmp_ls.setItem(52, InvOperateEvent.cancel);
        tmp_ls.setItem(53, InvOperateEvent.confirm);
        commander.openInventory(tmp_ls);
    }

    public static void openNPCSettingUI(@SuppressWarnings("unused") Player commander) {
        Other.sendInfo("系统建设中");
    }

    public static void openIntSettingUI(Player commander) {

        Inventory int_set = Bukkit.createInventory(commander, 54, "整数型变量设置");


        List<String> a = new ArrayList<>();
        a.add("每一位不点就是0");
        a.add("要是点过了就回不到0了");
        a.add("只能重新设置！");
        int_set.setItem(45, getItem("BARRIER", "重新设置", a));

        a.clear();
        int ord = 0;
        try {
            ord = int_setting.get(commander);
        } catch (NullPointerException e) {
            int_setting.put(commander,0);
        }
        a.add("现在的取值："+ord);

        if (ord < 0) {
            int_set.setItem(46, getItem("REDSTONE_TORCH", "负数", a, true));
            ord *= -1;
        } else int_set.setItem(46, getItem("REDSTONE_TORCH", "负数", a));

        for (int i = 1; i < 10; i++) {
            int_set.setItem(i - 1, getItem("PAPER", i + "(个位)", null, i % 10 == ord % 10));
        }
        for (int i = 11; i < 20; i++) {
            int_set.setItem(i - 2, getItem("PAPER", i % 10 + "(十位)", null, i % 10 == (ord % 100) / 10));
        }
        for (int i = 21; i < 30; i++) {
            int_set.setItem(i - 3, getItem("PAPER", i % 10 + "(百位)", null, i % 10 == (ord % 1000) / 100));
        }
        for (int i = 31; i < 40; i++) {
            int_set.setItem(i - 4, getItem("PAPER", i % 10 + "(千位)", null, i % 10 == (ord % 10000) / 1000));
        }
        for (int i = 41; i < 50; i++) {
            int_set.setItem(i - 5, getItem("PAPER", i % 10 + "(万位)", null, i % 10 == (ord % 100000) / 10000));
        }
        int_set.setItem(52, InvOperateEvent.cancel);
        int_set.setItem(53, InvOperateEvent.confirm);

        commander.openInventory(int_set);
    }

    public static boolean uiINIT(InventoryClickEvent Inv_click) {
        Inv_click.setCancelled(true);
        return Inv_click.getClickedInventory() == null || Inv_click.getCurrentItem() == null;
    }

    public static @NotNull
    ItemStack getItem(String typ_name, String dp_name, List<String> lore, boolean is_en) {
        ItemStack a = new ItemStack(Material.valueOf(typ_name), 1);
        ItemMeta b = a.getItemMeta();
        if (b != null) b.setLore(lore);
        assert b != null;
        b.setDisplayName(dp_name);
        if (is_en) b.addEnchant(Enchantment.LUCK, 1, true);
        a.setItemMeta(b);

        return a;
    }

    public static @NotNull
    ItemStack getItem(String typ_name, String dp_name, List<String> lore) {
        return getItem(typ_name, dp_name, lore, false);
    }


    public static void openCityResidentsListUI(Player player, int now_page) {
        City city = City.getCity(player);
        openCityResidentsListUI(city, player, now_page, false);
    }

    public static void openCityResidentsListUI(City city, Player player, int now_page, boolean edit) {
        if(city == null){
            player.sendMessage("找不到你的小镇哦~");
            return;
        }
        int page_amount = (city.getResidents().size()-1)/45 + 1;
        Inventory temp;
        if (edit) {
            temp = Bukkit.createInventory(player,54,"Residents List(Edit): "+city.getName()+" Page "+now_page+" of "+page_amount);
        } else temp = Bukkit.createInventory(player,54,"Residents List: "+city.getName()+" Page "+now_page+" of "+page_amount);
        int now = (now_page-1) * 45;
        int in_page = 0;
        List<String> lore = new ArrayList<>();
        if(edit) lore.add("点击将该玩家踢出城市！");
        List<UUID> residents = city.getResidents();
        for (int i = now, residentsSize = residents.size(); (i < residentsSize)&&(i < now+45+in_page);i++) {
            UUID resident = residents.get(i);
            if (resident.equals(player.getUniqueId())) {
                in_page = 1;
                continue;
            }
            temp.addItem(getSkull(resident, lore));
        }
        if(now_page != 1)temp.setItem(45, front_pg);
        if(now_page != page_amount)temp.setItem(53, next_pg);
        temp.setItem(52,getItem("PAPER","共有"+(city.getResidents().size()+1)+"人",null));
        player.openInventory(temp);
    }

    public static void openCityWarpListUI(Player player,int now_page){
        City city = City.getCity(player);
        openCityWarpListUI(city,player,now_page,false);
    }

    public static void openCityWarpListUI(City city,Player player,int now_page, boolean edit){
        if(city == null){
            player.sendMessage("找不到你的小镇哦~");
            return;
        }
        int page_amount = (city.getWarps().size()-1)/45 + 1;
        Inventory temp;
        if (edit) {
            temp = Bukkit.createInventory(player,54,"Warp List(Edit): "+city.getName()+" Page "+now_page+" of "+page_amount);
        } else temp = Bukkit.createInventory(player,54,"Warp List: "+city.getName()+" Page "+now_page+" of "+page_amount);
        int now = (now_page-1) * 45;
        Map<String, Location> warps = city.getWarps();
        int i = 0;
        for (String s : warps.keySet()) {
            i++;
            if((i < now+45)&&(i >= now)){
                List<String> lore = new ArrayList<>();
                lore.add("X= " + warps.get(s).getX());
                lore.add("Y= " + warps.get(s).getY());
                lore.add("Z= " + warps.get(s).getZ());
                lore.add("World= "+ Objects.requireNonNull(warps.get(s).getWorld()).getName());
                if(edit) lore.add("点击进行重新设置！");
                temp.addItem(getItem("PAPER",s,lore));
            }
        }
        if(now_page != 1)temp.setItem(45, front_pg);
        if(now_page != page_amount)temp.setItem(53, next_pg);
        temp.setItem(52,getItem("PAPER","共有"+(city.getWarps().size())+"个Warp",null));
        player.openInventory(temp);
    }

    public static boolean openCityManageUI(City city, Player player,boolean edit){
        if(city == null){
            return true;
        }
        List<String> lore = new ArrayList<>(city.getDescription());
        Inventory temp;
        if (edit) {
            temp = Bukkit.createInventory(player,54,"CityManage(Edit): "+city.getName());
        } else temp = Bukkit.createInventory(player,54,"CityManage: "+city.getName());

        ItemStack is = city.getIcon();
        if(edit){
            lore.add("点我设置城市的标志和描述");
        }
        lore.addAll(city.getDescription());
        if(is==null) {
            is = getItem("SNOWBALL", city.getName(), lore);
        }
        else {
            Objects.requireNonNull(is.getItemMeta()).setLore(lore);
        }
        temp.setItem(4,is);

        temp.setItem(5,city.getType().getSymbolItemStack());

        lore = new ArrayList<>();
        lore.add("欢迎语：");
        lore.add(city.getWelcomeMessage());
        if(edit) lore.add("点我重新设置");
        temp.setItem(14,getItem("SPRUCE_SIGN","欢迎语",lore));

        if(city.getWarp("spawn")!=null){
            lore = new ArrayList<>();
            if (edit) {
                lore.add("点击此处设置出生点");
            } else lore.add("点击此处传送回出生点");
            temp.setItem(31,getItem("OAK_DOOR","SPAWN",lore));
        }

        lore = new ArrayList<>();
        lore.add("传送点列表");
        if (edit) {
            lore.add(ChatColor.GREEN+"点击进入传送点管理界面");
        }
        temp.setItem(40,getItem("PAPER","Warp",lore));

        lore = new ArrayList<>();
        lore.add("市长: "+Bukkit.getOfflinePlayer(city.getMayor()).getName());
        temp.setItem(27,getSkull(city.getMayor(),lore));

        lore = new ArrayList<>();
        lore.add("城市居民列表");
        lore.add(ChatColor.GREEN+"点击进入城市居民管理界面");
        temp.setItem(36,getItem("PLAYER_HEAD","城市居民",lore));

        lore = new ArrayList<>();
        lore.add("城市权限组列表");
        lore.add(ChatColor.GREEN+"点击进入城市权限组管理界面");
        temp.setItem(44,getItem("PAPER","城市权限组",lore));

        lore = new ArrayList<>();
        lore.add("点我进入或退出编辑状态");
        temp.setItem(45,getItem("WOODEN_HOE","编辑状态",lore,edit));

        if(player.isOp()||player.hasPermission("sn.city.admin")){
            lore = new ArrayList<>();
            lore.add("删除这个城市！");
            temp.setItem(53,getItem("BARRIER",ChatColor.RED+"删除这个城市",lore));

            lore = new ArrayList<>();
            lore.add("给与这个城市ADMIN权限");
            temp.setItem(3,getItem("BARRIER",ChatColor.RED+"设为ADMIN",lore,city.getType().equals(CITY_TYPE.ADMIN)));
        }

        player.openInventory(temp);
        return true;
    }

    public static boolean openCityManageUI(Player player,boolean edit){
        City city = City.checkMayorAndGetCity(player);
        return openCityManageUI(city,player,edit);
    }


    public static boolean openMyCityUI(Player player){
        City city = City.getCity(player);
        return openMyCityUI(city,player);
    }

    public static boolean openMyCityUI(City city,Player player){
        if(city == null){
            return true;
        }
        Inventory temp = Bukkit.createInventory(player,54,"MyCity: "+city.getName());
        ItemStack is = city.getIcon();
        if(is==null) is = getItem("SNOWBALL",city.getName(),city.getDescription());
        temp.setItem(4,is);
        if(city.getWarp("spawn")!=null){
            List<String> lore = new ArrayList<>();
            lore.add("点击此处传送回出生点");
            temp.setItem(31,getItem("OAK_DOOR","SPAWN",lore));
        }
        List<String> lore = new ArrayList<>();
        lore.add("传送点列表");
        temp.setItem(40,getItem("PAPER","Warp",lore));

        lore = new ArrayList<>();
        lore.add("市长: "+Bukkit.getOfflinePlayer(city.getMayor()).getName());
        temp.setItem(27,getSkull(city.getMayor(),lore));

        lore = new ArrayList<>();
        lore.add("城市居民列表");
        temp.setItem(36,getItem("PLAYER_HEAD","城市居民列表",lore));

        player.openInventory(temp);
        return true;
    }

    public static boolean openCityApplicationAcceptUI(City city, Player player){
        List<UUID> applications = city.getApplications();
        if(applications.size()<=54){
            Inventory temp = Bukkit.createInventory(player,54,ChatColor.GREEN+city.getName()+"城市加入申请管理面板");
            for (UUID application : applications) {
                temp.addItem(getSkull(application,null));
            }
            player.openInventory(temp);
            return true;
        }
        openCityApplicationAcceptUI(city,player,1);
        return true;
    }

    public static void openCityApplicationAcceptUI(City city, Player player, int page){
        List<UUID> applications = city.getApplications();
        int page_total = applications.size()/45 + 1;
        Inventory temp = Bukkit.createInventory(player,54,ChatColor.GREEN+city.getName()+"城市加入申请管理面板 Page "+page+" of "+page_total);
        int now = (page-1)*45;
        for (int i = now; (i < now+45)&&(i < applications.size()); i++) {
            temp.addItem(getSkull(applications.get(i),null));
        }
        if(page!=1) temp.setItem(45, front_pg);
        if(page!=page_total) temp.setItem(53, next_pg);
        player.openInventory(temp);
    }

    public static void openCityPermGroupChooseUI(City city,Player commander) {
        if(city == null) return;
        Inventory temp = Bukkit.createInventory(commander,54,"权限组选择界面: "+city.getName());
        int cnt = 0;
        for (String s : city.getPermGroupList().keySet()) {
            temp.addItem(getItem("PAPER",s,null));
            if(cnt++ == 45){
                commander.sendMessage("可能有部分权限组未列出，请使用指令查看！");
                break;
            }
        }

        List<String> lore = new ArrayList<>();
        lore.add("点我返回主页");
        temp.setItem(45,getItem("PAPER","返回",lore));

        lore = new ArrayList<>();
        lore.add("点我添加一个权限组");
        temp.setItem(46,getItem("PAPER","添加",lore));

        commander.openInventory(temp);
    }

    public static void openCityPermGroupChooseUI(Player commander) {
        City city = City.checkMayorAndGetCity(commander);
        openCityPermGroupChooseUI(city,commander);
    }

    public static void openCityAdminUI(Player commander,int page) {

        int tot_page = cities.size() / 45 + 1;
        Inventory temp = Bukkit.createInventory(commander, 54, "City Admin Panel: Page " + page + " of " + tot_page);

        int index = (page - 1) * 45;
        for (int i = 0; (i < 45) && (index + i < city_names.size()); i++) {
            String name = city_names.get(i + index);
            List<String> lore = new ArrayList<>();
            try {
                lore.add("Mayor: " + Bukkit.getOfflinePlayer(cities.get(name).getMayor()).getName());
            } catch (Exception ignored) {
            }
            lore.addAll(cities.get(name).getDescription());
            temp.setItem(i, getItem("PAPER", name, lore));
        }
        if (page != 1) temp.setItem(45, front_pg);
        if (page != tot_page) temp.setItem(53, next_pg);
        List<String> lore = new ArrayList<>();
        lore.add("debug状态："+debug);
        lore.add("开启debug会让后台接受更多信息");
        temp.setItem(46,getItem("IRON_AXE","debug",lore,debug));
        commander.openInventory(temp);
    }

    public static void openCityIconSetUI(City city, Player commander, int page) {
        if (city == null) {
            return;
        }
        int tot_page = Material.values().length / 45 + 1;
        Inventory temp = Bukkit.createInventory(commander, 54, "CityIconSet: " + city.getName() + " Page " + page + " of " + tot_page);
        int index = (page - 1) * 45;
        for (int i = 0; (i < 45) && (index + i < Material.values().length); i++) {
            Material material = Material.values()[i + index];
            temp.setItem(i, new ItemStack(material));
        }
        if (page != 1) temp.setItem(45, front_pg);
        if (page != tot_page) temp.setItem(53, next_pg);
        List<String> lore = new ArrayList<>();
        lore.add("手持可以容纳文字的物品");
        lore.add("比如成书或纸笔");
        lore.add("然后点这里可以设置城市描述");
        temp.setItem(46,getItem("BOOK","导入城市描述",lore));
        commander.openInventory(temp);
    }

    public static void openCityPermGroupSetUI(City city, Player commander, String name, int perm_page, int player_page) {
        if (city == null) return;
        Map<String, Boolean> perm_list = city.getPermList().get(name);
        List<String> perm_group = new ArrayList<>(perm_city_settable);
        List<UUID> player_list = city.getPermGroupList().get(name);
        int perm_tot_page = perm_list.size() / 27 + 1, player_tot_page = player_list.size() / 18 + 1;
        Inventory temp = Bukkit.createInventory(commander, 54, "City权限组设置: " + name + " 权限组P" + perm_page
                + "/" + perm_tot_page + " 玩家P" + player_page + "/" + perm_tot_page);
        perm_page = min(perm_page, perm_tot_page);
        player_page = min(player_page, player_tot_page);
        int ori = (perm_page - 1) * 27;
        for (int i = 0; i < 27 && i + ori < perm_group.size(); i++) {
            String perm_name = perm_group.get(i + ori);
            temp.setItem(i, new CityPermissionItemStack(perm_name, perm_list.getOrDefault(perm_name, false)));
        }

        ori = (player_page - 1) * 18;
        for (int i = 0; i < 18 && i + ori < player_list.size(); i++) {
            temp.setItem(i + 27, getSkull(player_list.get(i), null));
        }

        ItemStack player_front_page = new ItemStack(Material.WRITABLE_BOOK);
        ItemStack player_next_page = new ItemStack(Material.WRITABLE_BOOK);
        ItemStack perm_front_page = new ItemStack(Material.WRITABLE_BOOK);
        ItemStack perm_next_page = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta player_fp_meta = player_front_page.getItemMeta();
        ItemMeta player_np_meta = player_next_page.getItemMeta();
        ItemMeta perm_fp_meta = perm_front_page.getItemMeta();
        ItemMeta perm_np_meta = perm_next_page.getItemMeta();
        if(perm_fp_meta==null||perm_np_meta==null||player_fp_meta==null||player_np_meta==null){
            return;
        }
        player_fp_meta.setDisplayName("上一页");
        player_np_meta.setDisplayName("下一页");
        perm_fp_meta.setDisplayName("上一页");
        perm_np_meta.setDisplayName("下一页");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW+"对玩家翻页！");
        player_fp_meta.setLore(lore);
        player_np_meta.setLore(lore);
        lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "对权限翻页！");
        perm_np_meta.setLore(lore);
        perm_fp_meta.setLore(lore);
        player_front_page.setItemMeta(player_fp_meta);
        player_next_page.setItemMeta(player_np_meta);
        perm_front_page.setItemMeta(perm_fp_meta);
        perm_next_page.setItemMeta(perm_np_meta);

        if (player_page != 1) temp.setItem(46, player_front_page);
        if (perm_page != 1) temp.setItem(48, perm_front_page);
        if (player_page != player_tot_page) temp.setItem(47, player_next_page);
        if (perm_page != perm_tot_page) temp.setItem(49, perm_next_page);
        if (!(name.equals("residents") || name.equals("mayor")))
            temp.setItem(50, getItem("EMERALD", "向这个权限组添加玩家", null));
        temp.setItem(52, getItem("PAPER", city.getName(), null));
        temp.setItem(53, confirm);
        commander.openInventory(temp);
    }

    public static void openIconSetUI(Player commander, int page) {

        int tot_page = 23;
        Inventory temp = Bukkit.createInventory(commander, 54, "IconChoose: Page " + page + " of " + tot_page);
        int index = (page - 1) * 45;
        for (int i = 0; (i < 45); i++) {
            Material material = Material.values()[i + index];
            temp.setItem(i, new ItemStack(material));
        }
        if (page != 1) temp.setItem(45, front_pg);
        if (page != tot_page) temp.setItem(53, next_pg);
        List<String> lore = new ArrayList<>();
        lore.add("放弃设置该权限的对应方块，将会默认为纸");
        temp.setItem(46, getItem("BARRIER", "放弃设置", lore));
        commander.openInventory(temp);

    }

    public static void openCityPermGroupAddPlayerUI(City city, Player commander, String name, int page) {
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
        page = min(page, tot_page);
        Inventory temp = Bukkit.createInventory(commander, 54, "PGPlayerAdd: " + name
                + " Page " + page + " of " + tot_page);
        int now = (page - 1) * 45;
        List<String> lore = new ArrayList<>();
        lore.add("点击将其加入权限组！");
        for (int i = 0; i < 45 && (i + now) < not_set.size(); i++) {
            temp.addItem(getSkull(not_set.get(i + now),lore));
        }
        if(page!=1) temp.setItem(45, front_pg);
        if(page!=tot_page) temp.setItem(53, next_pg);
        temp.setItem(46,getItem("EMERALD","将整页玩家都加入",null));
        temp.setItem(47,getItem("EMERALD","将能加入的玩家都加入",null));
        temp.setItem(51,confirm);
        temp.setItem(52,getItem("PAPER",city.getName(),null));
        commander.openInventory(temp);
    }
}
