package sn.sn;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static sn.sn.InvOperateEvent.*;
import static sn.sn.Sn.*;
import static sn.sn.SnFileIO.getSkull;

@SuppressWarnings("SpellCheckingInspection")
public class OpenUI {


    public static void openQuestSettingUI(Player questPlayer, String questname){
        try {
            setting_state.remove(InvOperateEvent.commander);
        } catch (NullPointerException ignored) {
        }
        Inventory questcreate = Bukkit.createInventory(questPlayer,9, ChatColor.BLUE+"正在创建一个新任务，请填写以下信息");


        ItemStack nameicon = new ItemStack(Material.BOOK);
        ItemMeta nameiconmeta = nameicon.getItemMeta();
        assert nameiconmeta != null;
        nameiconmeta.setDisplayName(questname);
        List<String> namelore = new ArrayList<>();
        namelore.add(ChatColor.RED+"任务名应该不含空格、\\、");
        namelore.add(ChatColor.RED+"单双引号、英文句点等特殊字符");
        namelore.add(ChatColor.RED+"任务名应该不以数字开头");
        namelore.add(ChatColor.RED+"任务名应该独一无二");
        namelore.add(ChatColor.YELLOW+"任务名最好不含中文字符");
        namelore.add(ChatColor.YELLOW+"否则可能会出现意料之外的编码错误");
        namelore.add(ChatColor.GREEN+"任务名最好与它的内容相对应");
        namelore.add("例子：FirstQuestofSnowNightTown");
        nameiconmeta.addEnchant(Enchantment.DIG_SPEED,1,false);
        nameiconmeta.setLore(namelore);
        nameicon.setItemMeta(nameiconmeta);
        questcreate.setItem(4,nameicon);


        ItemStack positionicon = new ItemStack(Quest_CE.SettingType.QUESTPOSITION.getSymbol());
        ItemMeta positioniconmeta = positionicon.getItemMeta();
        assert positioniconmeta != null;
        positioniconmeta.setDisplayName("任务位置");
        List<String> positionlore = new ArrayList<>();
        positionlore.add(ChatColor.GREEN+"点我设置任务位置");
        if(quest_setting.get(questPlayer).isPositionSet()) {
            positionlore.add(ChatColor.GREEN+"父任务"+ quest_setting.get(questPlayer).getQuestposition().getParentquest());
            positionlore.add(ChatColor.GREEN+"子任务"+ quest_setting.get(questPlayer).getQuestposition().getChildquest());
            positionlore.add(ChatColor.GREEN+"任务等级"+ quest_setting.get(questPlayer).getQuestposition().getQuestlevel());
            positioniconmeta.addEnchant(Enchantment.LUCK,1,false);
        }
        positioniconmeta.setLore(positionlore);
        positionicon.setItemMeta(positioniconmeta);
        questcreate.setItem(5, positionicon);


        ItemStack typeicon = new ItemStack(Material.ARROW);
        ItemMeta typeiconmeta = typeicon.getItemMeta();
        assert typeiconmeta != null;
        typeiconmeta.setDisplayName("任务类型");
        List<String> typelore = new ArrayList<>();
        typelore.add(ChatColor.GREEN+"点我设置任务类型");
        if(quest_setting.get(questPlayer).isTypeSet()) {
            typelore.add(ChatColor.GREEN+ quest_setting.get(questPlayer).getQuestType().getKey().getKey());
            typeiconmeta.addEnchant(Enchantment.LUCK,1,false);
            typeicon.setType(quest_setting.get(questPlayer).getQuesttype().getSymbol());
        }
        typeiconmeta.setLore(typelore);
        typeicon.setItemMeta(typeiconmeta);
        questcreate.setItem(3, typeicon);


        ItemStack targeticon = new ItemStack(Quest_CE.SettingType.QUESTACTION.getSymbol());
        ItemMeta targeticonmeta = targeticon.getItemMeta();
        assert targeticonmeta != null;
        targeticonmeta.setDisplayName("任务目标");
        List<String> targetlore = new ArrayList<>();
        targetlore.add(ChatColor.GREEN+"点我设置任务目标");
        if(quest_setting.get(questPlayer).isTargetSet()) {
            for (Quest_CE.QuestAction action : quest_setting.get(questPlayer).getQuesttarget()) {
                targetlore.addAll(toStrList(action.serialize()));
            }
            targeticonmeta.addEnchant(Enchantment.ARROW_DAMAGE,1,false);
        }
        targeticonmeta.setLore(targetlore);
        targeticon.setItemMeta(targeticonmeta);
        questcreate.setItem(6, targeticon);


        ItemStack acccondtnicon = new ItemStack(Quest_CE.SettingType.QUESTACTION.getSymbol());
        ItemMeta acccondtniconmeta = acccondtnicon.getItemMeta();
        assert acccondtniconmeta != null;
        acccondtniconmeta.setDisplayName("任务接受条件");
        List<String> acccondtnlore = new ArrayList<>();
        acccondtnlore.add(ChatColor.GREEN+"点我设置任务接受条件");
        acccondtnlore.add("如果没有特别的任务接受条件，");
        acccondtnlore.add("也请将其设置为“默认“！");
        if(quest_setting.get(questPlayer).isAcceptconditionSet()) {
            for (Quest_CE.QuestAction action : quest_setting.get(questPlayer).getQustAccptCndtn()) {
                acccondtnlore.addAll(toStrList(action.serialize()));
            }
            acccondtniconmeta.addEnchant(Enchantment.ARROW_DAMAGE,1,false);
        }
        acccondtniconmeta.setLore(acccondtnlore);
        acccondtnicon.setItemMeta(acccondtniconmeta);
        questcreate.setItem(2, acccondtnicon);


        ItemStack rewardicon = new ItemStack(Quest_CE.SettingType.QUESTREWARD.getSymbol());
        ItemMeta rewardiconmeta = rewardicon.getItemMeta();
        assert rewardiconmeta != null;
        rewardiconmeta.setDisplayName("任务奖励");
        List<String> rewardlore = new ArrayList<>();
        rewardlore.add(ChatColor.GREEN+"点我设置任务奖励");
        if(quest_setting.get(questPlayer).isRewardset()) {
            rewardlore.addAll(toStrList(quest_setting.get(questPlayer).getQuestreward().serialize()));
            rewardiconmeta.addEnchant(Enchantment.CHANNELING,1,false);
        }
        rewardiconmeta.setLore(rewardlore);
        rewardicon.setItemMeta(rewardiconmeta);
        questcreate.setItem(7, rewardicon);


        ItemStack onicon = new ItemStack(Material.BARRIER);
        ItemMeta oniconmeta = onicon.getItemMeta();
        assert oniconmeta != null;
        oniconmeta.setDisplayName("任务试运行");
        List<String> onlore = new ArrayList<>();

        if(quest_setting.get(questPlayer).isOn()){
            oniconmeta.addEnchant(Enchantment.CHANNELING,1,false);
            onicon.setType(Material.EMERALD);
            onlore.add(ChatColor.GREEN+"任务现在正在运行！");
            onlore.add(ChatColor.RED+"左键关闭任务！");
        } else {
            onlore.add(ChatColor.GREEN+"任务现在没有运行！");
            onlore.add(ChatColor.RED+"左键打开任务！");
            onlore.add(ChatColor.RED+"请在打开任务前确定所有参数已经被充分设置！");
        }
        oniconmeta.setLore(onlore);
        onicon.setItemMeta(oniconmeta);
        questcreate.setItem(8, onicon);


        ItemStack cficon = new ItemStack(Material.EMERALD);
        ItemMeta cficonmeta = cficon.getItemMeta();
        assert cficonmeta != null;
        cficonmeta.setDisplayName("确认并创建任务");
        List<String> cflore = new ArrayList<>();
        cflore.add(ChatColor.GREEN+"点我确认并创建任务");
        cflore.add("请确认所有状态准确地、充分地被设置！");
        cflore.add("建议先试运行任务");
        cflore.add("新建的任务的运行状态继承试运行状态");
        cficonmeta.setLore(cflore);
        cficon.setItemMeta(cficonmeta);
        questcreate.setItem(1, cficon);


        ItemStack cicon = new ItemStack(Material.BARRIER);
        ItemMeta ciconmeta = cicon.getItemMeta();
        assert ciconmeta != null;
        ciconmeta.setDisplayName("任务接受条件");
        List<String> clore = new ArrayList<>();
        clore.add(ChatColor.RED+"取消新建这个任务！");
        clore.add(ChatColor.RED+"这会导致你刚才所完成的工作全部被删除！");
        ciconmeta.setLore(clore);
        cicon.setItemMeta(ciconmeta);
        questcreate.setItem(0, cicon);


        questPlayer.openInventory(questcreate);
    }

    public static void openQuestSettingUI(Player questPlayer){
        openQuestSettingUI(questPlayer, quest_setting.get(questPlayer).getQuestname());
    }

    public static void openActionCreateUI(Player commander) {

        Inventory tmpacui = Bukkit.createInventory(commander,18,ChatColor.GREEN+"创建一个任务条件");
        int i = 0;
        for (Quest_CE.QuestActionType value : Quest_CE.QuestActionType.values()) {
            tmpacui.setItem(i++, getItem("PAPER",value.getKey().getKey(),null, quest_action_setting.get(commander).getQuestactiontype().getKey() == (value).getKey()));
        }
        List<String> a = new ArrayList<>();
        if(!quest_action_setting.get(commander).getQuestactiondata().getQuesttargetitem().isEmpty())
            for (ItemStack itemStack : quest_action_setting.get(commander).getQuestactiondata().getQuesttargetitem()) {
                a.add(itemStack.serialize().toString());
            }
        tmpacui.setItem(9, getItem("GRASS","设置物品信息",a,!quest_action_setting.get(commander).getQuestactiondata().getQuesttargetitem().isEmpty()));
        a.clear();
        if(!quest_action_setting.get(commander).getQuestactiondata().getQuesttargetentity().isEmpty())
            for (EntityType key : quest_action_setting.get(commander).getQuestactiondata().getQuesttargetentity().keySet()) {
                a.add(key.name()+' '+ quest_action_setting.get(commander).getQuestactiondata().getQuesttargetentity().get(key));
            }
        tmpacui.setItem(10, getItem("SPAWNER","设置实体信息",a,!quest_action_setting.get(commander).getQuestactiondata().getQuesttargetentity().isEmpty()));

        tmpacui.setItem(11, getItem("VILLAGER_SPAWN_EGG","设置npc信息",null, quest_action_setting.get(commander).getQuestactiondata().getQuesttargetnpc()!=null));

        a.clear();
        if(quest_action_setting.get(commander).getQuestactiondata().getTargetlocation()!=null)
        a.add(quest_action_setting.get(commander).getQuestactiondata().getTargetlocation().toString());
        tmpacui.setItem(12, getItem("COMPASS","设置位置信息",a,!quest_action_setting.get(commander).getQuestactiondata().isLocSet()));
        a.clear();
        a.add(String.valueOf(quest_action_setting.get(commander).getQuestactiondata().getQuesttimelimit()));
        tmpacui.setItem(13, getItem("CLOCK","设置时间限制",a, quest_action_setting.get(commander).getQuestactiondata().getQuesttimelimit()!=-1));
        tmpacui.setItem(17, InvOperateEvent.confirm);
        commander.openInventory(tmpacui);
    }

    public static void openActionSettingUI(Player commander) {
        String viewname;
        if(isSetTorC.get(commander)) viewname = ChatColor.GREEN+"任务达成的条件列表";
        else viewname = ChatColor.GREEN+"任务触发或接受的条件列表";
        Inventory tmptarget = Bukkit.createInventory(commander,18, viewname);
        List<Quest_CE.QuestAction> target;
        if(isSetTorC.get(commander)) {
            target = quest_setting.get(commander).getQuesttarget();
        } else target = quest_setting.get(commander).getQuestacceptcondition();
        if(target.size()!=0)
            for (int i = 0; i < target.size(); i++) {
                ItemStack tmpis = new ItemStack(Material.REDSTONE,1);
                setTempItemMeta(tmptarget, target, i, tmpis);
            }

        tmptarget.setItem(9, getItem("DIAMOND_PICKAXE","添加一个条件",null));
        tmptarget.setItem(10, getItem("DIAMOND_SHOVEL","删除一个条件",null));
        tmptarget.setItem(17, InvOperateEvent.confirm);
        commander.openInventory(tmptarget);
    }

    private static void setTempItemMeta(Inventory tmptarget, List<Quest_CE.QuestAction> target, int i, ItemStack tmpis) {
        ItemMeta tmpitm = tmpis.getItemMeta();
        assert tmpitm != null;
        tmpitm.setDisplayName(target.get(i).getQuestactionname());
        List<String> a = new ArrayList<>();
        for (String s : target.get(i).serialize().keySet()) {
            a.add(s+"->"+target.get(i).serialize().get(s).toString());
        }
        tmpitm.setLore(a);
        tmpis.setItemMeta(tmpitm);
        tmptarget.setItem(i,tmpis);
    }

    public static void openPositionChooseMultiPageUI(Player commander, int pgindex) {
        Inventory positionset = Bukkit.createInventory(commander,54,ChatColor.GREEN+"父任务设置：请选择父任务(第"+pgindex+"页)" );
        int nowindex =45 * (pgindex - 1);

        for (int i = 0; i < 45; i++) {
            Quest_CE.Quest quest = quests.get(i);
            positionset.setItem(nowindex + i, getItem("BOOK", quest.getQuestname(), quest.getQuestdescription()));
        }
        if(pgindex != 1)positionset.setItem(45, pg_up);
        if(quests.size() > nowindex + 45)positionset.setItem(53, InvOperateEvent.pg_dn);
        commander.openInventory(positionset);
    }

    public static void openActionDeleteUI(Player commander) {
        Inventory tmpacui = Bukkit.createInventory(commander,18,ChatColor.RED+"删除一个任务条件");
        List<Quest_CE.QuestAction> target = quest_setting.get(commander).getQuestacceptcondition();
        if(isSetTorC.get(commander)) target = quest_setting.get(commander).getQuesttarget();
        if(target.size()!=0)
            for (int i = 0; i < target.size(); i++) {
                ItemStack tmpis = new ItemStack(Material.BARRIER,1);
                setTempItemMeta(tmpacui, target, i, tmpis);
            }
        tmpacui.setItem(17, InvOperateEvent.cancel);
        commander.openInventory(tmpacui);
    }

    public static void openRewardSettingUI(Player commander1) {
        Inventory rewardset = Bukkit.createInventory(commander1,9,ChatColor.GREEN+"任务奖励设置" );
        List<String> a = new ArrayList<>();
        a.add("设置为系统任务");
        a.add("系统任务在发放奖励时不会收取创建人物品");

        rewardset.setItem(0, getItem("REDSTONE_BLOCK","系统任务",a, quest_setting.get(commander1).getQuestreward().isAdmin()));
        a = new ArrayList<>();
        a.add("设置经济奖励");
        a.add(String.valueOf(quest_setting.get(commander1).getQuestreward().getRewardmoney()));
        rewardset.setItem(3, getItem("EMERALD_BLOCK","经济奖励",a, quest_setting.get(commander1).getQuestreward().getRewardmoney() != 0));
        a = new ArrayList<>();
        a.add("设置物品奖励");
        for (ItemStack rewarditem : quest_setting.get(commander1).getQuestreward().getRewarditems()) {
            a.add(rewarditem.serialize().toString());
        }
        rewardset.setItem(5, getItem("DIAMOND_BLOCK","物品奖励",a,!quest_setting.get(commander1).getQuestreward().getRewarditems().isEmpty()));
        rewardset.setItem(8, InvOperateEvent.confirm);
        commander1.openInventory(rewardset);
    }

    public static void openEntityTypeSettingUI(Player commander) {
        Inventory tmpetsui = Bukkit.createInventory(commander,54,ChatColor.GREEN+"选择实体类型");
        int i = 1, j = 0;
        List<String> tmplore = new ArrayList<>();
        tmplore.add("这一组有下列实体：");

        for (EntityType entitytype : EntityType.values()) {
            tmplore.add(entitytype.name());
            if(i == 8){
                tmpetsui.setItem(j, getItem("CHEST","第"+(j+1)+"组实体类型",tmplore));
                tmplore.clear();
                tmplore.add("这一组有下列实体：");
                i=1;
                j++;
            } else i++;
        }
        if(entity_type_setting.get(commander)!=null){
            ItemStack tmpcon = InvOperateEvent.confirm.clone();
            List<String> tmplist = new ArrayList<>();
            tmplist.add("已经设置的实体类型：");
            tmplist.add(entity_type_setting.get(commander).name());
            ItemMeta tmpim = tmpcon.getItemMeta();
            assert tmpim != null;
            tmpim.setLore(tmplist);
            tmpcon.setItemMeta(tmpim);
            tmpetsui.setItem(53, InvOperateEvent.confirm);
        } else tmpetsui.setItem(53, InvOperateEvent.cancel);

        commander.openInventory(tmpetsui);

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
            loc_setting.put(commander, new InvOperateEvent.LocSet(Bukkit.getWorlds().get(0).getSpawnLocation()));
        }
        InvOperateEvent.LocSet ls = loc_setting.get(commander);
        Inventory tmpls = Bukkit.createInventory(commander, 54 ,"位置变量设置");
        List<String> a =new ArrayList<>();
        a.add("现在的位置信息:");
        a.add("世界:"+ls.getWorld());
        a.add("x:"+ls.getX());
        a.add("y:"+ls.getY());
        a.add("z:"+ls.getZ());

        tmpls.setItem(0, getItem("COMPASS","反转X设置方向",a,ls.isXm()));
        tmpls.setItem(2, getItem("COMPASS","反转Y设置方向",a,ls.isYm()));
        tmpls.setItem(4, getItem("COMPASS","反转Z设置方向",a,ls.isZm()));

        tmpls.setItem(1, getItem("BARRIER","重设X(归零)",a));
        tmpls.setItem(3, getItem("BARRIER","重设Y(归零)",a));
        tmpls.setItem(5, getItem("BARRIER","重设Z(归零)",a));

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
            tmpls.setItem((i+1) * 9, getItem("PAPER","X"+mX+ powerTen(i),a));
            tmpls.setItem((i+1)*9+1, getItem("PAPER","X"+mX+5* powerTen(i),a));
            tmpls.setItem((i+1)*9+2, getItem("PAPER","Y"+mY+ powerTen(i),a));
            tmpls.setItem((i+1)*9+3, getItem("PAPER","Y"+mY+5* powerTen(i),a));
            tmpls.setItem((i+1)*9+4, getItem("PAPER","Z"+mZ+ powerTen(i),a));
            tmpls.setItem((i+1)*9+5, getItem("PAPER","Z"+mZ+5* powerTen(i),a));
        }
        List<World> lw = Bukkit.getWorlds();
        int worldsize = Bukkit.getWorlds().size();
        if(worldsize <= 12){
            int now = 6,line = 0;
            for (int i = 0, lwSize = lw.size(); i < lwSize; i++,now++) {
                if(now == 9) {
                    now = 6;
                    line ++;
                }
                World world = lw.get(i);
                tmpls.setItem(now+9*line, getItem("GRASS", world.getName(), a, ls.getWorld() == world));
            }
        } else {
            int now = 6,line = 0;
            for (int i = 0; i < 12; i++,now++) {
                if(now == 9) {
                    now = 6;
                    line ++;
                }
                World world = lw.get(i);
                tmpls.setItem(now+9*line, getItem("GRASS", world.getName(), a, ls.getWorld() == world));
            }
            tmpls.setItem(51, InvOperateEvent.pg_dn);
        }
        tmpls.setItem(52, InvOperateEvent.cancel);
        tmpls.setItem(53, InvOperateEvent.confirm);
        commander.openInventory(tmpls);
    }

    public static void openNPCSettingUI(@SuppressWarnings("unused") Player commander) {
        sendInfo("系统建设中");
    }

    public static void openIntSettingUI(Player commander) {

        Inventory intset = Bukkit.createInventory(commander,54,"整数型变量设置");


        List<String> a = new ArrayList<>();
        a.add("每一位不点就是0");
        a.add("要是点过了就回不到0了");
        a.add("只能重新设置！");
        intset.setItem(45, getItem("BARRIER","重新设置",a));

        a.clear();
        int ord = 0;
        try {
            ord = int_setting.get(commander);
        } catch (NullPointerException e) {
            int_setting.put(commander,0);
        }
        a.add("现在的取值："+ord);

        if(ord<0) {
            intset.setItem(46, getItem("REDSTONE_TORCH", "负数", a, true));
            ord *= -1;
        } else intset.setItem(46, getItem("REDSTONE_TORCH", "负数", a));

        for (int i = 1; i < 10; i++) {
            intset.setItem(i-1, getItem("PAPER", i +"(个位)",null,i%10 == ord%10));
        }
        for (int i = 11; i < 20; i++) {
            intset.setItem(i-2, getItem("PAPER", i%10 +"(十位)",null,i%10 == (ord%100)/10));
        }
        for (int i = 21; i < 30; i++) {
            intset.setItem(i-3, getItem("PAPER", i%10 +"(百位)",null,i%10 == (ord%1000)/100));
        }
        for (int i = 31; i < 40; i++) {
            intset.setItem(i-4, getItem("PAPER", i%10 +"(千位)",null,i%10 == (ord%10000)/1000));
        }
        for (int i = 41; i < 50; i++) {
            intset.setItem(i-5, getItem("PAPER", i%10 +"(万位)",null,i%10 == (ord%100000)/10000));
        }
        intset.setItem(52, InvOperateEvent.cancel);
        intset.setItem(53, InvOperateEvent.confirm);

        commander.openInventory(intset);
    }

    public static boolean uiINIT(InventoryClickEvent Invclick) {
        Invclick.setCancelled(true);
        return Invclick.getClickedInventory() == null || Invclick.getCurrentItem() == null;
    }

    public static @NotNull
    ItemStack getItem(String typname, String dpname, List<String> lore, boolean isench){
        ItemStack a = new ItemStack(Material.valueOf(typname),1);
        ItemMeta b = a.getItemMeta();
        if(b != null) b.setLore(lore);
        assert b != null;
        b.setDisplayName(dpname);
        if(isench) b.addEnchant(Enchantment.LUCK,1,true);
        a.setItemMeta(b);

        return a;
    }

    public static @NotNull ItemStack getItem(String typname, String dpname, List<String> lore){
        return getItem(typname,dpname,lore,false);
    }

    public static void openCityResidentsListUI(Player player,int now_page){
        City_CE.City city = City_CE.City.getCity(player);
        if(city == null){
            player.sendMessage("找不到你的小镇哦~");
            return;
        }
        int page_amount = (city.getResidents().size()-1)/45 + 1;
        Inventory temp = Bukkit.createInventory(player,54,"Residents List: "+city.getName()+" Page "+now_page+" of "+page_amount);
        int now = (now_page-1) * 45;
        List<UUID> residents = city.getResidents();
        for (int i = now, residentsSize = residents.size(); (i < residentsSize)&&(i < now+45); i++) {
            UUID resident = residents.get(i);
            if (resident.equals(player.getUniqueId())) continue;
            temp.addItem(getSkull(resident, null));
        }
        if(now_page != 1)temp.setItem(45,pg_up);
        if(now_page != page_amount)temp.setItem(53,pg_dn);
        temp.setItem(52,getItem("PAPER","共有"+(city.getResidents().size()+1)+"人",null));
        player.openInventory(temp);
    }

    public static void openCityWarpListUI(Player player,int now_page){
        City_CE.City city = City_CE.City.getCity(player);
        if(city == null){
            player.sendMessage("找不到你的小镇哦~");
            return;
        }
        int page_amount = (city.getWarps().size()-1)/45 + 1;
        Inventory temp = Bukkit.createInventory(player,54,"Warp List: "+city.getName()+" Page "+now_page+" of "+page_amount);
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
                temp.addItem(getItem("PAPER",s,lore));
            }
        }
        if(now_page != 1)temp.setItem(45,pg_up);
        if(now_page != page_amount)temp.setItem(53,pg_dn);
        temp.setItem(52,getItem("PAPER","共有"+(city.getWarps().size())+"个Warp",null));
        player.openInventory(temp);
    }

    public static boolean openMyCityUI(Player player){
        City_CE.City city = City_CE.City.getCity(player);
        if(city == null){
            player.sendMessage("找不到你的小镇哦~");
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

    public static boolean openCityApplicationAcceptUI(City_CE.City city, Player player){
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

    public static void openCityApplicationAcceptUI(City_CE.City city, Player player, int page){
        List<UUID> applications = city.getApplications();
        int page_total = applications.size()/45 + 1;
        Inventory temp = Bukkit.createInventory(player,54,ChatColor.GREEN+city.getName()+"城市加入申请管理面板 Page "+page+" of "+page_total);
        int now = (page-1)*45;
        for (int i = now; (i < now+45)&&(i < applications.size()); i++) {
            temp.addItem(getSkull(applications.get(i),null));
        }
        if(page!=1) temp.setItem(45,pg_up);
        if(page!=page_total) temp.setItem(53,pg_dn);
        player.openInventory(temp);
    }

    public static void openCityPermGroupChooseUI(Player commander) {
        City_CE.City city = City_CE.City.checkMayorAndGetCity(commander);
        if(city == null) return;
        Inventory temp = Bukkit.createInventory(commander,54,"权限组选择界面: "+city.getName());
        for (String s : city.getPermGroup().keySet()) {
            temp.addItem(getItem("PAPER",s,null));
        }
        commander.openInventory(temp);
    }
}
