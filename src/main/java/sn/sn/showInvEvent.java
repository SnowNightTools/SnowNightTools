package sn.sn;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

import static sn.sn.Sn.*;
import static sn.sn.quest.getQuest;


/*


    express 和 quest 的监听器类

    实现了 express show 面板打开后的点击物品处理 和关闭面板时保存文件的处理
    实现了 quest create 面板操作

    作者：LtSeed


*/



public class showInvEvent implements Listener {

    public static Player commander;
    public static int showInv_nmax = 0;
    Plugin plugin = Sn.getPlugin(Sn.class);


    private static final ItemStack cancel = new ItemStack(Material.BARRIER);
    private static final ItemStack confirm = new ItemStack(Material.EMERALD);
    private static final ItemStack pgup = new ItemStack(Material.WRITABLE_BOOK);
    private static final ItemStack pgdn = new ItemStack(Material.WRITABLE_BOOK);

    showInvEvent(){
        ItemMeta confirmmeta = confirm.getItemMeta();
        assert confirmmeta != null;
        confirmmeta.setDisplayName(ChatColor.GREEN+"确认");
        confirm.setItemMeta(confirmmeta);
        ItemMeta cancelmeta = cancel.getItemMeta();
        assert cancelmeta != null;
        cancelmeta.setDisplayName(ChatColor.RED+"取消");
        cancel.setItemMeta(cancelmeta);

        ItemMeta pgupmeta = pgup.getItemMeta();
        assert pgupmeta != null;
        pgupmeta.setDisplayName(ChatColor.WHITE+"上一页");
        pgup.setItemMeta(pgupmeta);
        ItemMeta pgdnmeta = pgdn.getItemMeta();
        assert pgdnmeta != null;
        pgdnmeta.setDisplayName(ChatColor.WHITE+"下一页");
        pgdn.setItemMeta(pgdnmeta);
    }



    @EventHandler(priority = EventPriority.HIGH)
    public void InvCloseEvent(InventoryCloseEvent Invclose) {

        commander = Bukkit.getPlayer(Invclose.getPlayer().getUniqueId());
        if(Invclose.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"雪花速递")){

            saveInvToYml(share_yml,share_file,commander.getName(),showInv.get(commander));

            //保存文件
            express.setstatefalse(commander);
            // 名字 - 背包
            showInv.remove(commander);
            commander.sendMessage(commander.getName()+"面板已关闭，信息已同步");
        }

        if(Invclose.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"正在创建一个新任务，请填写以下信息")){
            try {
                quest_yml.save(quest_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    static void openQuestSettingUI(Player questPlayer){
        openQuestSettingUI(questPlayer,questseting.get(questPlayer).getQuestname());
    }

    static void openQuestSettingUI(Player questPlayer, String questname){
        try {
            setingstate.remove(commander);
        } catch (NullPointerException ignored) {
        }
        Inventory questcreate = Bukkit.createInventory(questPlayer,9,ChatColor.BLUE+"正在创建一个新任务，请填写以下信息");


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


        ItemStack positionicon = new ItemStack(quest.SettingType.QUESTPOSITION.getSymbol());
        ItemMeta positioniconmeta = positionicon.getItemMeta();
        assert positioniconmeta != null;
        positioniconmeta.setDisplayName("任务位置");
        List<String> positionlore = new ArrayList<>();
        positionlore.add(ChatColor.GREEN+"点我设置任务位置");
        if(questseting.get(questPlayer).isPositionSet()) {
            positionlore.add(ChatColor.GREEN+"父任务"+questseting.get(questPlayer).getQuestposition().getParentquest());
            positionlore.add(ChatColor.GREEN+"子任务"+questseting.get(questPlayer).getQuestposition().getChildquest());
            positionlore.add(ChatColor.GREEN+"任务等级"+questseting.get(questPlayer).getQuestposition().getQuestlevel());
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
        if(questseting.get(questPlayer).isTypeSet()) {
            typelore.add(ChatColor.GREEN+questseting.get(questPlayer).getQuestType().getKey().getKey());
            typeiconmeta.addEnchant(Enchantment.LUCK,1,false);
            typeicon.setType(questseting.get(questPlayer).getQuesttype().getSymbol());
        }
        typeiconmeta.setLore(typelore);
        typeicon.setItemMeta(typeiconmeta);
        questcreate.setItem(3, typeicon);


        ItemStack targeticon = new ItemStack(quest.SettingType.QUESTACTION.getSymbol());
        ItemMeta targeticonmeta = targeticon.getItemMeta();
        assert targeticonmeta != null;
        targeticonmeta.setDisplayName("任务目标");
        List<String> targetlore = new ArrayList<>();
        targetlore.add(ChatColor.GREEN+"点我设置任务目标");
        if(questseting.get(questPlayer).isTargetSet()) {
            for (quest.QuestAction action : questseting.get(questPlayer).getQuesttarget()) {
                targetlore.addAll(toStrList(action.serialize()));
            }
            targeticonmeta.addEnchant(Enchantment.ARROW_DAMAGE,1,false);
        }
        targeticonmeta.setLore(targetlore);
        targeticon.setItemMeta(targeticonmeta);
        questcreate.setItem(6, targeticon);


        ItemStack acccondtnicon = new ItemStack(quest.SettingType.QUESTACTION.getSymbol());
        ItemMeta acccondtniconmeta = acccondtnicon.getItemMeta();
        assert acccondtniconmeta != null;
        acccondtniconmeta.setDisplayName("任务接受条件");
        List<String> acccondtnlore = new ArrayList<>();
        acccondtnlore.add(ChatColor.GREEN+"点我设置任务接受条件");
        acccondtnlore.add("如果没有特别的任务接受条件，");
        acccondtnlore.add("也请将其设置为“默认“！");
        if(questseting.get(questPlayer).isAcceptconditionSet()) {
            for (quest.QuestAction action : questseting.get(questPlayer).getQustAccptCndtn()) {
                acccondtnlore.addAll(toStrList(action.serialize()));
            }
            acccondtniconmeta.addEnchant(Enchantment.ARROW_DAMAGE,1,false);
        }
        acccondtniconmeta.setLore(acccondtnlore);
        acccondtnicon.setItemMeta(acccondtniconmeta);
        questcreate.setItem(2, acccondtnicon);


        ItemStack rewardicon = new ItemStack(quest.SettingType.QUESTREWARD.getSymbol());
        ItemMeta rewardiconmeta = rewardicon.getItemMeta();
        assert rewardiconmeta != null;
        rewardiconmeta.setDisplayName("任务奖励");
        List<String> rewardlore = new ArrayList<>();
        rewardlore.add(ChatColor.GREEN+"点我设置任务奖励");
        if(questseting.get(questPlayer).isRewardset()) {
            rewardlore.addAll(toStrList(questseting.get(questPlayer).getQuestreward().serialize()));
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

        if(questseting.get(questPlayer).isOn()){
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

    static void openActionCreateUI(Player commander) {

        Inventory tmpacui = Bukkit.createInventory(commander,18,ChatColor.GREEN+"创建一个任务条件");
        int i = 0;
        for (quest.QuestActionType value : quest.QuestActionType.values()) {
            tmpacui.setItem(i++,getItem("PAPER",value.getKey().getKey(),null,questactionseting.get(commander).getQuestactiontype().getKey() == (value).getKey()));
        }
        List<String> a = new ArrayList<>();
        if(!questactionseting.get(commander).getQuestactiondata().getQuesttargetitem().isEmpty())
            for (ItemStack itemStack : questactionseting.get(commander).getQuestactiondata().getQuesttargetitem()) {
                a.add(itemStack.serialize().toString());
            }
        tmpacui.setItem(9,getItem("GRASS","设置物品信息",a,!questactionseting.get(commander).getQuestactiondata().getQuesttargetitem().isEmpty()));
        a.clear();
        if(!questactionseting.get(commander).getQuestactiondata().getQuesttargetentity().isEmpty())
            for (EntityType key : questactionseting.get(commander).getQuestactiondata().getQuesttargetentity().keySet()) {
                a.add(key.name()+' '+questactionseting.get(commander).getQuestactiondata().getQuesttargetentity().get(key));
            }
        tmpacui.setItem(10,getItem("SPAWNER","设置实体信息",a,!questactionseting.get(commander).getQuestactiondata().getQuesttargetentity().isEmpty()));

        tmpacui.setItem(11,getItem("VILLAGER_SPAWN_EGG","设置npc信息",null,questactionseting.get(commander).getQuestactiondata().getQuesttargetnpc()!=null));

        a.clear();
        if(questactionseting.get(commander).getQuestactiondata().getTargetlocation()!=null)
        a.add(questactionseting.get(commander).getQuestactiondata().getTargetlocation().toString());
        tmpacui.setItem(12,getItem("COMPASS","设置位置信息",a,!questactionseting.get(commander).getQuestactiondata().isLocSet()));
        a.clear();
        a.add(String.valueOf(questactionseting.get(commander).getQuestactiondata().getQuesttimelimit()));
        tmpacui.setItem(13,getItem("CLOCK","设置时间限制",a,questactionseting.get(commander).getQuestactiondata().getQuesttimelimit()!=-1));
        tmpacui.setItem(17,confirm);
        commander.openInventory(tmpacui);
    }

    static void openActionSettingUI(Player commander) {
        String viewname;
        if(isSetTorC.get(commander)) viewname = ChatColor.GREEN+"任务达成的条件列表";
        else viewname = ChatColor.GREEN+"任务触发或接受的条件列表";
        Inventory tmptarget = Bukkit.createInventory(commander,18, viewname);
        List<quest.QuestAction> target;
        if(isSetTorC.get(commander)) {
            target = questseting.get(commander).getQuesttarget();
        } else target = questseting.get(commander).getQuestacceptcondition();
        if(target.size()!=0)
            for (int i = 0; i < target.size(); i++) {
                ItemStack tmpis = new ItemStack(Material.REDSTONE,1);
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

        tmptarget.setItem(9,getItem("DIAMOND_PICKAXE","添加一个条件",null));
        tmptarget.setItem(10,getItem("DIAMOND_SHOVEL","删除一个条件",null));
        tmptarget.setItem(17,confirm);
        commander.openInventory(tmptarget);
    }

    static void openPositionChooseMultiPageUI(Player commander,int pgindex) {
        Inventory positionset = Bukkit.createInventory(commander,54,ChatColor.GREEN+"父任务设置：请选择父任务(第"+pgindex+"页)" );
        int nowindex =45 * (pgindex - 1);

        for (int i = 0; i < 45; i++) {
            quest.Quest quest = quests.get(i);
            positionset.setItem(nowindex + i, getItem("BOOK", quest.getQuestname(), quest.getQuestdescription()));
        }
        if(pgindex != 1)positionset.setItem(45,pgup);
        if(quests.size() > nowindex + 45)positionset.setItem(53,pgdn);
        commander.openInventory(positionset);
    }

    static void openRewardSettingUI(Player commander1) {
        Inventory rewardset = Bukkit.createInventory(commander1,9,ChatColor.GREEN+"任务奖励设置" );
        List<String> a = new ArrayList<>();
        a.add("设置为系统任务");
        a.add("系统任务在发放奖励时不会收取创建人物品");

        rewardset.setItem(0,getItem("REDSTONE_BLOCK","系统任务",a,questseting.get(commander1).getQuestreward().isAdmin()));
        a = new ArrayList<>();
        a.add("设置经济奖励");
        a.add(String.valueOf(questseting.get(commander1).getQuestreward().getRewardmoney()));
        rewardset.setItem(3,getItem("EMERALD_BLOCK","经济奖励",a,questseting.get(commander1).getQuestreward().getRewardmoney() != 0));
        a = new ArrayList<>();
        a.add("设置物品奖励");
        for (ItemStack rewarditem : questseting.get(commander1).getQuestreward().getRewarditems()) {
            a.add(rewarditem.serialize().toString());
        }
        rewardset.setItem(5,getItem("DIAMOND_BLOCK","物品奖励",a,!questseting.get(commander1).getQuestreward().getRewarditems().isEmpty()));
        rewardset.setItem(8,confirm);
        commander1.openInventory(rewardset);
    }

    static void openEntityTypeSettingUI(Player commander) {
        Inventory tmpetsui = Bukkit.createInventory(commander,54,ChatColor.GREEN+"选择实体类型");
        int i = 1, j = 0;
        List<String> tmplore = new ArrayList<>();
        tmplore.add("这一组有下列实体：");

        for (EntityType entitytype : EntityType.values()) {
            tmplore.add(entitytype.name());
            if(i == 8){
                tmpetsui.setItem(j,getItem("CHEST","第"+(j+1)+"组实体类型",tmplore));
                tmplore.clear();
                tmplore.add("这一组有下列实体：");
                i=1;
                j++;
            } else i++;
        }
        if(entitytypeseting.get(commander)!=null){
            ItemStack tmpcon = confirm.clone();
            List<String> tmplist = new ArrayList<>();
            tmplist.add("已经设置的实体类型：");
            tmplist.add(entitytypeseting.get(commander).name());
            ItemMeta tmpim = tmpcon.getItemMeta();
            assert tmpim != null;
            tmpim.setLore(tmplist);
            tmpcon.setItemMeta(tmpim);
            tmpetsui.setItem(53,confirm);
        } else tmpetsui.setItem(53,cancel);

        commander.openInventory(tmpetsui);

    }

    static void openActionDeleteUI(Player commander) {
        Inventory tmpacui = Bukkit.createInventory(commander,18,ChatColor.RED+"删除一个任务条件");
        List<quest.QuestAction> target = questseting.get(commander).getQuestacceptcondition();
        if(isSetTorC.get(commander)) target = questseting.get(commander).getQuesttarget();
        if(target.size()!=0)
            for (int i = 0; i < target.size(); i++) {
                ItemStack tmpis = new ItemStack(Material.BARRIER,1);
                ItemMeta tmpitm = tmpis.getItemMeta();
                assert tmpitm != null;
                tmpitm.setDisplayName(target.get(i).getQuestactionname());
                List<String> a = new ArrayList<>();
                for (String s : target.get(i).serialize().keySet()) {
                    a.add(s+"->"+target.get(i).serialize().get(s).toString());
                }
                tmpitm.setLore(a);
                tmpis.setItemMeta(tmpitm);
                tmpacui.setItem(i,tmpis);
            }
        tmpacui.setItem(17,cancel);
        commander.openInventory(tmpacui);
    }

    private static @NotNull ItemStack getItem(String typname, String dpname, List<String> lore, boolean isench){
        ItemStack a = new ItemStack(Material.valueOf(typname),1);
        ItemMeta b = a.getItemMeta();
        if(b != null) b.setLore(lore);
        assert b != null;
        b.setDisplayName(dpname);
        if(isench) b.addEnchant(Enchantment.LUCK,1,true);
        a.setItemMeta(b);

        return a;
    }

    private static @NotNull ItemStack getItem(String typname, String dpname, List<String> lore){
        return getItem(typname,dpname,lore,false);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void breakEvent(InventoryClickEvent Invclick) {



        commander = Bukkit.getPlayer(Invclick.getView().getPlayer().getUniqueId());
        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"雪花速递")){
            if (uiINIT(Invclick)) return;

            ClickType cli = Invclick.getClick();//获得clickType 类型
            if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
                //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
                commander.closeInventory();
                return;
            }

            if(cli.isLeftClick()){
                HashMap<Integer, ItemStack> remains;
                //if(Invclick.getClickedInventory() == null||Invclick.getCurrentItem() == null) return ;
                //点击showInv物品:取回该项
                if(Objects.requireNonNull(Invclick.getClickedInventory()).equals(showInv.get(commander))) {
                    //玩家点击的是showInv

                    remains = commander.getInventory().addItem(Invclick.getCurrentItem());
                    if(remains.isEmpty())
                        showInv.get(commander).clear(Invclick.getSlot());
                    else showInv.get(commander).setItem(Invclick.getSlot(),remains.get(0));
                } else {
                    //玩家点击的是背包
                    remains = showInv.get(commander).addItem(Invclick.getCurrentItem());
                    if(remains.isEmpty())
                        commander.getInventory().clear(Invclick.getSlot());
                    else commander.getInventory().setItem(Invclick.getSlot(),remains.get(0));

                }

            }
        }


        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"正在创建一个新任务，请填写以下信息")){
            if (uiINIT(Invclick)) return;
            ClickType cli = Invclick.getClick();//获得clickType 类型
            if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
                //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
                commander.closeInventory();
            }

            if(Objects.requireNonNull(Invclick.getClickedInventory()).getSize()==9) {
                //玩家点击的是上面的设置内容
                String name = Objects.requireNonNull(Objects.requireNonNull(Invclick.getClickedInventory().getItem(4)).getItemMeta()).getDisplayName();
                if(Invclick.getSlot() == 8){//isOn
                    if(questseting.get(commander).isOn()) {
                        if(!questseting.get(commander).turnOff()) return;
                        say("打开了任务"+questseting.get(commander).getQuestname());
                        ItemStack temp = Invclick.getClickedInventory().getItem(2);
                        assert temp != null;
                        temp.setType(Material.BARRIER);
                        Invclick.getClickedInventory().setItem(2,temp);
                    } else {
                        if(!questseting.get(commander).turnOn()) return;
                        ItemStack temp = Invclick.getClickedInventory().getItem(2);
                        assert temp != null;
                        temp.setType(Material.EMERALD);
                        Invclick.getClickedInventory().setItem(2,temp);
                    }
                    return;
                }
                if(Invclick.getSlot() == 3){
                    Inventory typeset = Bukkit.createInventory(commander,9,ChatColor.GREEN+"选择任务类型");
                    typeset.setItem(1, getItem_QuestTypeSetting("MAIN","主线任务",null));
                    typeset.setItem(2, getItem_QuestTypeSetting("SIDE","支线任务",null));
                    typeset.setItem(3, getItem_QuestTypeSetting("TRIGGER","触发任务",null));
                    typeset.setItem(5, getItem_QuestTypeSetting("DAILY","日常任务",null));
                    typeset.setItem(6, getItem_QuestTypeSetting("REWARD","悬赏任务",null));
                    typeset.setItem(7, getItem_QuestTypeSetting("DIY","自编任务",null));
                    setingstate.put(commander, quest.SettingType.QUESTTYPE);
                    commander.openInventory(typeset);
                    return;
                }

                if(Invclick.getSlot() == 5){//quest position
                    Inventory positionset = Bukkit.createInventory(commander,54,ChatColor.GREEN+"父任务设置：请选择父任务" );
                    setingstate.put(commander, quest.SettingType.QUESTPOSITION);
                    if(quests.size()<=53){
                        for (int i = 0; i < quests.size(); i++) {
                            quest.Quest quest = quests.get(i);
                            positionset.setItem(i+1, getItem("BOOK", quest.getQuestname(), quest.getQuestdescription()));
                        }
                        positionset.setItem(0,nonp());
                        commander.openInventory(positionset);
                    } else {
                        openPositionChooseMultiPageUI(commander,1);
                    }
                    return;
                }
                if(Invclick.getSlot() == 7){//questreward
                    setingstate.put(commander, quest.SettingType.QUESTREWARD);
                    openRewardSettingUI(commander);
                    return;
                }
                if(Invclick.getSlot() == 6){//位置目标
                    setingstate.put(commander, quest.SettingType.QUESTACTION);
                    isSetTorC.put(commander,true);
                    openActionSettingUI(commander);
                    return;
                }
                if(Invclick.getSlot() == 2){//位置目标
                    setingstate.put(commander, quest.SettingType.QUESTACTION);
                    isSetTorC.put(commander,false);
                    openActionSettingUI(commander);
                    return;
                }
                if(Invclick.getSlot() == 0){//删除
                    setingstate.remove(commander);
                    questseting.remove(commander);
                    commander.closeInventory();
                    commander.sendMessage("已经删除你新建的任务！");
                    return;
                }
                if(Invclick.getSlot() == 1){//新建

                    commander.closeInventory();
                    commander.sendMessage("已经新建任务！");
                    questseting.get(commander).saveQuestToYml();
                    try {
                        quest_yml.save(quest_file);
                    } catch (IOException ignored) {
                    }
                    quests.add(questseting.get(commander));
                    questamount++;
                    List<String> a = new ArrayList<>();
                    a.add("任务信息如下:");
                    try {
                        a.addAll(toStrList(questseting.get(commander).serialize()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    a.add("end.");
                    for (String s : a) {
                        commander.sendMessage(s);
                    }
                    questseting.remove(commander);
                    setingstate.remove(commander);


                    return;
                }

            }
        }

        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"创建一个任务条件")){
            //open by openActionCreateUI();
            if (uiINIT(Invclick)) return;
            ClickType cli = Invclick.getClick();//获得clickType 类型
            if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
                //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
                commander.closeInventory();
            }

            if(!questactionseting.containsKey(commander))
                questactionseting.put(commander,new quest.QuestAction());

            switch (Invclick.getSlot()){

                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    questactionseting.get(commander).setQuestactiontype(quest.QuestActionType.getFromInt(Invclick.getSlot()+1));
                    openActionCreateUI(commander);
                    return;
                case 9:
                    List<ItemStack> questtargetitem = questactionseting.get(commander).getQuestactiondata().getQuesttargetitem();
                    Inventory qtisetting = Bukkit.createInventory(commander, 18,ChatColor.GREEN+"请设置物品条件");
                    qtisetting.setContents(questtargetitem.toArray(new ItemStack[9]));
                    qtisetting.setItem(17,confirm);
                    commander.openInventory(qtisetting);
                    return;
                case 10:
                    openEntityTypeSettingUI(commander);
                    return;
                case 11:
                    openNPCSettingUI(commander);
                    return;
                case 12:
                    seting.put(commander, loc -> questactionseting.get(commander).getQuestactiondata().setTargetlocation((Location) loc));
                    uiopener.put(commander, showInvEvent::openActionCreateUI);
                    openLocSettingUI(commander);
                    return;
                case 13:
                    seting.put(commander, set -> questactionseting.get(commander).getQuestactiondata().setQuesttimelimit((Integer) set));
                    uiopener.put(commander, showInvEvent::openActionCreateUI);
                    openIntSettingUI(commander);
                    return;
                case 17:
                    if(isSetTorC.get(commander)) {
                        questseting.get(commander).addQuesttarget(questactionseting.get(commander));
                        questactionseting.remove(commander);
                        openActionSettingUI(commander);
                    } else {
                        questseting.get(commander).addQuestacceptcondition(questactionseting.get(commander));
                        questactionseting.remove(commander);
                        openActionSettingUI(commander);
                    }
                    return;
                default:
                    return;
            }
        }

        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"任务达成的条件列表")){
            isSetTorC.put(commander,true);
            if (workActionSettingView(Invclick)) return;
        }
        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"任务触发或接受的条件列表")){
            isSetTorC.put(commander,false);
            if (workActionSettingView(Invclick)) return;
        }

        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"选择任务类型")){
            if (uiINIT(Invclick)) return;
            ClickType cli = Invclick.getClick();//获得clickType 类型
            if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
                //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
                commander.closeInventory();
            }

            if(Objects.requireNonNull(Invclick.getClickedInventory()).getSize()==9) {
                //玩家点击的是上面的设置内容
                switch (Invclick.getSlot()){
                    case 1:
                        questseting.get(commander).setQuesttype(quest.QuestType.MAIN);
                        break;
                    case 2:
                        questseting.get(commander).setQuesttype(quest.QuestType.SIDE);
                        break;
                    case 3:
                        questseting.get(commander).setQuesttype(quest.QuestType.TRIGGER);
                        break;
                    case 5:
                        questseting.get(commander).setQuesttype(quest.QuestType.DAILY);
                        break;
                    case 6:
                        questseting.get(commander).setQuesttype(quest.QuestType.REWARD);
                        break;
                    case 7:
                        questseting.get(commander).setQuesttype(quest.QuestType.DIY);
                        break;
                    default:
                        return;
                }

                openQuestSettingUI(commander);
                return;

            }



        }


        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"任务奖励设置")){
            if (uiINIT(Invclick)) return;
            ClickType cli = Invclick.getClick();//获得clickType 类型
            if(cli.isKeyboardClick()||cli.equals(ClickType.WINDOW_BORDER_LEFT)||cli==ClickType.WINDOW_BORDER_RIGHT){
                //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
                commander.closeInventory();
            }

            if(Objects.requireNonNull(Invclick.getClickedInventory()).getSize()==9) {
                //玩家点击的是上面的设置内容

                quest.QuestReward tmpqr = questseting.get(commander).getQuestreward();
                if(tmpqr==null)
                    tmpqr = new quest.QuestReward();
                switch (Invclick.getSlot()){
                    case 0:
                        //if(commander.hasPermission("quest.reward.admin")){

                       // }
                        tmpqr.setAdmin(true);
                        openRewardSettingUI(commander);
                        questseting.get(commander).setQuestreward(tmpqr);
                        return;
                    case 3:
                        openIntSettingUI(commander);
                        uiopener.put(commander,showInvEvent::openRewardSettingUI);
                        return;
                    case 5:
                        Inventory rwrditmstng = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请放入物品奖励");
                        rwrditmstng.setContents(tmpqr.getRewarditems().toArray(new ItemStack[9]));
                        rwrditmstng.setItem(17,confirm);
                        commander.openInventory(rwrditmstng);
                        return;
                    case 8:
                        openQuestSettingUI(commander);
                        return;
                    default:
                        return;
                }
            }
        }


        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"请放入物品奖励")){
            if (uiINIT(Invclick)) return;
            ClickType cli = Invclick.getClick();//获得clickType 类型
            if(cli.isKeyboardClick()||cli.equals(ClickType.WINDOW_BORDER_LEFT)||cli==ClickType.WINDOW_BORDER_RIGHT){
                //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
                commander.closeInventory();
            }

            if(Invclick.getSlot()==17){
                openRewardSettingUI(commander);
                return;
            }

            quest.QuestReward tmpqr = questseting.get(commander).getQuestreward();
            if(tmpqr==null)
                tmpqr = new quest.QuestReward();

            if(cli.isLeftClick()){
                HashMap<Integer, ItemStack> remains;
                if(Invclick.getClickedInventory() == null||Invclick.getCurrentItem() == null) return ;
                //点击showInv物品:取回该项
                if(Objects.requireNonNull(Invclick.getClickedInventory()).getSize()==18) {
                    if(Invclick.getSlot()>=9)
                        return;
                    tmpqr.getRewarditems().remove(Invclick.getSlot());
                    Inventory rwrditmstng = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请放入物品奖励");
                    rwrditmstng.setContents(tmpqr.getRewarditems().toArray(new ItemStack[9]));
                    rwrditmstng.setItem(17,confirm);
                    commander.openInventory(rwrditmstng);

                } else {
                    //玩家点击的是背包
                    if(tmpqr.getRewarditems().size()>=9)return;
                    tmpqr.getRewarditems().add(Invclick.getCurrentItem());
                    Inventory rwrditmstng = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请放入物品奖励");
                    rwrditmstng.setContents(tmpqr.getRewarditems().toArray(new ItemStack[9]));
                    rwrditmstng.setItem(17,confirm);
                    commander.openInventory(rwrditmstng);
                }

            }
            questseting.get(commander).setQuestreward(tmpqr);
            return;
        }


        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"请设置物品条件")){
            if (uiINIT(Invclick)) return;
            ClickType cli = Invclick.getClick();//获得clickType 类型
            if(cli.isKeyboardClick()||cli.equals(ClickType.WINDOW_BORDER_LEFT)||cli==ClickType.WINDOW_BORDER_RIGHT){
                //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
                commander.closeInventory();
            }

            if(Invclick.getSlot()==17){
                openActionCreateUI(commander);
                return;
            }

            List<ItemStack> list = questactionseting.get(commander).getQuestactiondata().getQuesttargetitem();

            if(cli.isLeftClick()){
                if(Invclick.getClickedInventory() == null||Invclick.getCurrentItem() == null) return ;
                //点击showInv物品:取回该项
                if(Objects.requireNonNull(Invclick.getClickedInventory()).getSize()==18) {
                    if(Invclick.getSlot()>=9)
                        return;
                    list.remove(Invclick.getSlot());
                    Inventory rwrditmstng = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请设置物品条件");
                    rwrditmstng.setContents(list.toArray(new ItemStack[9]));
                    rwrditmstng.setItem(17,confirm);
                    commander.openInventory(rwrditmstng);

                } else {
                    //玩家点击的是背包
                    if(list.size()>=9)return;
                    list.add(Invclick.getCurrentItem());
                    Inventory rwrditmstng = Bukkit.createInventory(commander,18,ChatColor.GREEN+"请设置物品条件");
                    rwrditmstng.setContents(list.toArray(new ItemStack[9]));
                    rwrditmstng.setItem(17,confirm);
                    commander.openInventory(rwrditmstng);
                }

            }
            questactionseting.get(commander).getQuestactiondata().setQuesttargetitem(list);
            return;
        }


        if(Invclick.getView().getTitle().contains("父任务设置：请选择父任务")) {
            if (uiINIT(Invclick)) return;
            ClickType cli = Invclick.getClick();//获得clickType 类型
            if (cli.isKeyboardClick() || cli == ClickType.WINDOW_BORDER_LEFT || cli == ClickType.WINDOW_BORDER_RIGHT) {
                //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
                commander.closeInventory();
            }

            if(Objects.requireNonNull(Invclick.getClickedInventory()).contains(nonp())){
                String name;
                try {
                    name = Objects.requireNonNull(Objects.requireNonNull(Invclick.getCurrentItem()).getItemMeta()).getDisplayName();
                } catch (NullPointerException e){
                    return;
                }
                if (workPositionSet(name)) return;
                openQuestSettingUI(commander);
                return;

            }

            if (Objects.requireNonNull(Invclick.getClickedInventory()).contains(pgup)||Objects.requireNonNull(Invclick.getClickedInventory()).contains(pgdn)) {
                //玩家点击的是上面的设置内容
                int pgindex = Integer.parseInt(Invclick.getView().getTitle(),Invclick.getView().getTitle().indexOf("第")+1,Invclick.getView().getTitle().indexOf("页"),10);
                String name;
                try {
                    name = Objects.requireNonNull(Objects.requireNonNull(Invclick.getCurrentItem()).getItemMeta()).getDisplayName();
                } catch (NullPointerException e){
                    return;
                }

                if(name.equals("上一页")){
                    openPositionChooseMultiPageUI(commander,pgindex + 1);
                    return;
                }
                if(name.equals("下一页")){
                    openPositionChooseMultiPageUI(commander,pgindex - 1);
                    return;
                }
                if (workPositionSet(name)) return;
                openQuestSettingUI(commander);
                return;
            }
        }

        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"选择实体类型")){
            if (uiINIT(Invclick)) return;

            if(Invclick.getSlot()==53) {
                openActionCreateUI(commander);
                return;
            }

            List<String> tmplore = null;
            try {
                tmplore = Objects.requireNonNull(Objects.requireNonNull(Invclick.getCurrentItem()).getItemMeta()).getLore();
            } catch (NullPointerException ignore){}
            if(tmplore == null) return;

            Inventory tmpcet = Bukkit.createInventory(commander,9,ChatColor.GREEN+"选择一种实体");

            for (int i = 1; i < tmplore.size(); i++) {
                tmpcet.setItem(i-1,getItem("SPAWNER",tmplore.get(i),null));
            }
            tmpcet.setItem(8,cancel);
            commander.openInventory(tmpcet);
            return;
        }

        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"选择一种实体")){
            if (uiINIT(Invclick)) return;

            try {
                entitytypeseting.put(commander,EntityType.valueOf(Objects.requireNonNull(Objects.requireNonNull(Invclick.getCurrentItem()).getItemMeta()).getDisplayName()));
            } catch (IllegalArgumentException | NullPointerException ignore) {
            }

            uiopener.put(commander,showInvEvent::openActionCreateUI);
            if(seting.containsKey(commander)) seting.put(commander,p -> putEntitySet(commander,p));
            else seting.put(commander,p -> putEntitySet(commander,p));
            openIntSettingUI(commander);

            return;
        }
        if(Invclick.getView().getTitle().equalsIgnoreCase("位置变量设置")){
            if (uiINIT(Invclick)) return;
            //反转 重设
            int opr = 0;
            String name = Objects.requireNonNull(Objects.requireNonNull(Invclick.getCurrentItem()).getItemMeta()).getDisplayName();

            if (name.contains("X")){
                if(name.contains("反转")){
                    locseting.get(commander).revXm();
                    openLocSettingUI(commander);
                    return;
                }
                if(name.contains("重设")){
                    locseting.get(commander).setX(0);
                    openLocSettingUI(commander);
                    return;
                }
                name = name.substring(1);
                try {
                    opr = Integer.parseInt(name);
                } catch (NumberFormatException ignored) {
                }
                if(opr != 0){
                    locseting.get(commander).addX(opr);
                    openLocSettingUI(commander);
                    return;
                }
            }
            if (name.contains("Y")){
                if(name.contains("反转")){
                    locseting.get(commander).revYm();
                    openLocSettingUI(commander);
                    return;
                }
                if(name.contains("重设")){
                    locseting.get(commander).setY(0);
                    openLocSettingUI(commander);
                    return;
                }
                name = name.substring(1);
                try {
                    opr = Integer.parseInt(name);
                } catch (NumberFormatException ignored) {
                }
                if(opr != 0){
                    locseting.get(commander).addY(opr);
                    openLocSettingUI(commander);
                    return;
                }
            }
            if (name.contains("Z")){
                if(name.contains("反转")){
                    locseting.get(commander).revZm();
                    openLocSettingUI(commander);
                    return;
                }
                if(name.contains("重设")){
                    locseting.get(commander).setZ(0);
                    openLocSettingUI(commander);
                    return;
                }
                name = name.substring(1);
                try {
                    opr = Integer.parseInt(name);
                } catch (NumberFormatException ignored) {
                }
                if(opr != 0){
                    locseting.get(commander).addZ(opr);
                    openLocSettingUI(commander);
                    return;
                }
            }
            List<World> tmplw = Bukkit.getWorlds();
            for (World world : tmplw) {
                if(name.equalsIgnoreCase(world.getName())){
                    locseting.get(commander).setWorld(world);
                    openLocSettingUI(commander);
                    return;
                }
            }
            try {
                if(Invclick.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.RED+"取消")){
                    seting.remove(commander);
                    uiopener.get(commander).accept(commander);
                    uiopener.remove(commander);
                    return;
                }
                if(Invclick.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN+"确认")){
                    seting.get(commander).accept(locseting.get(commander).getLoc());
                    seting.remove(commander);
                    uiopener.get(commander).accept(commander);
                    uiopener.remove(commander);
                    return;
                }
            } catch (Exception ignored) {
            }
            if(Invclick.getCurrentItem().getItemMeta().getDisplayName().equals("下一页")){
                Inventory tmpdn = Bukkit.createInventory(commander,54,"世界设置");
                if(tmplw.size()>=53){
                    commander.closeInventory();
                    commander.sendMessage("大哥你服务器是怎么装下52个以上的世界的，能不能教教我？");
                    commander.sendMessage("帮您把刚刚设置的东西都删了哦！");
                    seting.remove(commander);
                    setingstate.remove(commander);
                    isSetTorC.remove(commander);
                    questseting.remove(commander);
                    questactionseting.remove(commander);
                    return;
                }
                for (int i = 0, tmplwSize = tmplw.size(); i < tmplwSize; i++) {
                    World world = tmplw.get(i);

                    tmpdn.setItem(i, getItem("GRASS_BLOCK",world.getName(), Collections.singletonList(world.getUID().toString())));
                }
                tmpdn.setItem(52,cancel);
                tmpdn.setItem(53,confirm);
                commander.openInventory(tmpdn);
                return;
            }
        }

        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.RED+"删除一个任务条件")){
            if (uiINIT(Invclick)) return;
            List<quest.QuestAction> target = questseting.get(commander).getQuestacceptcondition();
            if(isSetTorC.get(commander)) target = questseting.get(commander).getQuesttarget();
            if(Invclick.getSlot()==17){
                openActionSettingUI(commander);
                return;
            }
            try {
                target.remove(Invclick.getSlot());
                if(isSetTorC.get(commander)) questseting.get(commander).setQuesttarget(target);
                questseting.get(commander).setQuestacceptcondition(target);
            } catch (UnsupportedOperationException|IndexOutOfBoundsException|NullPointerException ignore) {
            }
            openActionDeleteUI(commander);
        }


        if(Invclick.getView().getTitle().equalsIgnoreCase("整数型变量设置")){
            if (uiINIT(Invclick)) return;
            int ind = Invclick.getSlot();
            int ord = 0;
            try {
                ord = intseting.get(commander);
            } catch (NullPointerException ignored) {
            }
            boolean neg = false;
            if(ord<0){
                neg = true;
                ord *= -1;
            }
            if(ind<9){//0-8 -> 1-9
                intseting.replace(commander,ord-ord%10+ind+1);
            } else if(ind<18){ // 9-17 -> 1-9 *10
                intseting.replace(commander,ord-ord%100+ord%10 + (ind%9+1)*10);
            } else if(ind<27){
                intseting.replace(commander,ord-ord%1000+ord%100 + (ind%9+1)*100);
            } else if(ind<36){
                intseting.replace(commander,ord-ord%10000+ord%1000 + (ind%9+1)*1000);
            } else if(ind<45){
                intseting.replace(commander,ord-ord%100000+ord%10000 + (ind%9+1)*10000);
            } else switch (ind) {
                case 45:
                    intseting.replace(commander, 0);
                    break;
                case 46:
                    if(neg) intseting.replace(commander, ord);
                    else intseting.replace(commander, -1*ord);
                    break;
                case 53:
                    if(neg) intseting.replace(commander, -1*ord);
                    else intseting.replace(commander, ord);
                case 52:
                    if(ind==52)intseting.replace(commander, 0);
                    if(seting.containsKey(commander)){
                        if (neg) seting.get(commander).accept(-1 * ord);
                        else seting.get(commander).accept(ord);
                        seting.remove(commander);
                    }
                    if(uiopener.containsKey(commander)) {
                        uiopener.get(commander).accept(commander);
                        uiopener.remove(commander);
                    }

                    intseting.remove(commander);
                    return;
                default:
                    return;
            }
            openIntSettingUI(commander);
        }


    }

    static class LocSet{
        int x;
        int y;
        int z;
        boolean xm = true;
        boolean ym = true;
        boolean zm = true;
        World world = null;

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


        public void setXm(boolean xm) {
            this.xm = xm;
        }

        public void setYm(boolean ym) {
            this.ym = ym;
        }

        public void setZm(boolean zm) {
            this.zm = zm;
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

        private LocSet(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
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

    private void openLocSettingUI(Player commander) {
        if(!locseting.containsKey(commander)){
            locseting.put(commander, new LocSet(Bukkit.getWorlds().get(0).getSpawnLocation()));
        }
        LocSet ls = locseting.get(commander);
        Inventory tmpls = Bukkit.createInventory(commander, 54 ,"位置变量设置");
        List<String> a =new ArrayList<>();
        a.add("现在的位置信息:");
        a.add("世界:"+ls.getWorld());
        a.add("x:"+ls.getX());
        a.add("y:"+ls.getY());
        a.add("z:"+ls.getZ());

        tmpls.setItem(0,getItem("COMPASS","反转X设置方向",a,ls.isXm()));
        tmpls.setItem(2,getItem("COMPASS","反转Y设置方向",a,ls.isYm()));
        tmpls.setItem(4,getItem("COMPASS","反转Z设置方向",a,ls.isZm()));

        tmpls.setItem(1,getItem("BARRIER","重设X(归零)",a));
        tmpls.setItem(3,getItem("BARRIER","重设Y(归零)",a));
        tmpls.setItem(5,getItem("BARRIER","重设Z(归零)",a));

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
            tmpls.setItem((i+1) * 9,getItem("PAPER","X"+mX+ Tpower(i),a));
            tmpls.setItem((i+1)*9+1,getItem("PAPER","X"+mX+5* Tpower(i),a));
            tmpls.setItem((i+1)*9+2,getItem("PAPER","Y"+mY+ Tpower(i),a));
            tmpls.setItem((i+1)*9+3,getItem("PAPER","Y"+mY+5* Tpower(i),a));
            tmpls.setItem((i+1)*9+4,getItem("PAPER","Z"+mZ+ Tpower(i),a));
            tmpls.setItem((i+1)*9+5,getItem("PAPER","Z"+mZ+5* Tpower(i),a));
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
            tmpls.setItem(51,pgdn);
        }
        tmpls.setItem(52,cancel);
        tmpls.setItem(53,confirm);
        commander.openInventory(tmpls);
    }

    private long Tpower(int mi) {
        long res = 10;
        for (int i = 0; i < mi; i++) {
            res *= 10;
        }
        return res;
    }

    private void openNPCSettingUI(Player commander) {
        say("系统建设中");
    }

    private static void putEntitySet(Player commander, Object ignored){
        if(entitytypeseting.containsKey(commander)&&intseting.containsKey(commander)) {
            questactionseting.get(commander).getQuestactiondata().addQuesttargetentity(entitytypeseting.get(commander), intseting.get(commander));
        }
    }

    private static void openIntSettingUI(Player commander) {

        Inventory intset = Bukkit.createInventory(commander,54,"整数型变量设置");


        List<String> a = new ArrayList<>();
        a.add("每一位不点就是0");
        a.add("要是点过了就回不到0了");
        a.add("只能重新设置！");
        intset.setItem(45,getItem("BARRIER","重新设置",a));

        a.clear();
        int ord = 0;
        try {
            ord = intseting.get(commander);
        } catch (NullPointerException e) {
            intseting.put(commander,0);
        }
        a.add("现在的取值："+ord);

        if(ord<0) {
            intset.setItem(46, getItem("REDSTONE_TORCH", "负数", a, true));
            ord *= -1;
        } else intset.setItem(46, getItem("REDSTONE_TORCH", "负数", a));

        for (int i = 1; i < 10; i++) {
            intset.setItem(i-1,getItem("PAPER", i +"(个位)",null,i%10 == ord%10));
        }
        for (int i = 11; i < 20; i++) {
            intset.setItem(i-2,getItem("PAPER", i%10 +"(十位)",null,i%10 == (ord%100)/10));
        }
        for (int i = 21; i < 30; i++) {
            intset.setItem(i-3,getItem("PAPER", i%10 +"(百位)",null,i%10 == (ord%1000)/100));
        }
        for (int i = 31; i < 40; i++) {
            intset.setItem(i-4,getItem("PAPER", i%10 +"(千位)",null,i%10 == (ord%10000)/1000));
        }
        for (int i = 41; i < 50; i++) {
            intset.setItem(i-5,getItem("PAPER", i%10 +"(万位)",null,i%10 == (ord%100000)/10000));
        }
        intset.setItem(52,cancel);
        intset.setItem(53,confirm);

        commander.openInventory(intset);
    }

    private boolean workActionSettingView(InventoryClickEvent Invclick) {
        if (uiINIT(Invclick)) return true;
        ClickType cli = Invclick.getClick();//获得clickType 类型
        if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
            //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
            commander.closeInventory();
        }
        if(Invclick.getSlot()==17){
            openQuestSettingUI(commander);
            isSetTorC.remove(commander);
            return true;
        }
        if(Invclick.getSlot()==9){
            questactionseting.put(commander, new quest.QuestAction());
            openActionCreateUI(commander);
            return true;
        }
        if(Invclick.getSlot()==10){
            openActionDeleteUI(commander);
            return true;
        }
        return false;
    }

    private boolean uiINIT(InventoryClickEvent Invclick) {
        Invclick.setCancelled(true);
        return Invclick.getClickedInventory() == null || Invclick.getCurrentItem() == null;
    }

    private boolean workPositionSet(@NotNull String name) {
        if(name.equals("无父任务")){
            quest.QuestPosition tmp = new quest.QuestPosition();
            tmp.setParentquest(null);
            tmp.setQuestlevel(1);
            questseting.get(commander).setQuestposition(tmp);
            openQuestSettingUI(commander);

            return true;
        }

        quest.QuestPosition tmp = new quest.QuestPosition();
        tmp.setQuestpositionname(questseting.get(commander).getQuestname()+"position");
        tmp.setParentquest(name);
        if(questseting.get(commander).isPositionSet()){
            tmp.setChildquest(questseting.get(commander).getQuestposition().getChildquest());
            tmp.setChildquestother1(questseting.get(commander).getQuestposition().getChildquestother1());
            tmp.setChildquestother2(questseting.get(commander).getQuestposition().getChildquestother2());
            tmp.setChildquestother3(questseting.get(commander).getQuestposition().getChildquestother3());
            tmp.setQuestpositionname(questseting.get(commander).getQuestposition().getQuestpositionname());
        }
        questseting.get(commander).setQuestposition(tmp);
        if(getQuest(name).getQuestposition().getChildquest() == null){
            getQuest(name).getQuestposition().setChildquest(questseting.get(commander).getQuestname());
            questseting.get(commander).getQuestposition().setQuestlevel(getQuest(name).getQuestposition().getQuestlevel());
            openQuestSettingUI(commander);
            return true;
        }
        if(getQuest(name).getQuestposition().getChildquestother1() == null){
            getQuest(name).getQuestposition().setChildquestother1(questseting.get(commander).getQuestname());
            questseting.get(commander).getQuestposition().setQuestlevel(getQuest(name).getQuestposition().getQuestlevel()+1);
            openQuestSettingUI(commander);
            return true;
        }
        if(getQuest(name).getQuestposition().getChildquestother2() == null){
            getQuest(name).getQuestposition().setChildquestother2(questseting.get(commander).getQuestname());
            questseting.get(commander).getQuestposition().setQuestlevel(getQuest(name).getQuestposition().getQuestlevel()+1);
            openQuestSettingUI(commander);
            return true;
        }
        if(getQuest(name).getQuestposition().getChildquestother3() == null){
            getQuest(name).getQuestposition().setChildquestother3(questseting.get(commander).getQuestname());
            questseting.get(commander).getQuestposition().setQuestlevel(getQuest(name).getQuestposition().getQuestlevel()+1);
            openQuestSettingUI(commander);
            return true;
        }

        return false;
    }

    private @NotNull ItemStack nonp() {
        ItemStack nonp = new ItemStack(Material.BARRIER,1);
        ItemMeta a = nonp.getItemMeta();
        assert a != null;
        a.setDisplayName("无父任务");
        nonp.setItemMeta(a);
        return nonp;
    }

    private @NotNull ItemStack getItem_QuestTypeSetting(String typname, String dpname, List<String> lore){
        ItemStack a = new ItemStack(quest.QuestType.valueOf(typname).getSymbol(),1);
        ItemMeta b = a.getItemMeta();
        if(b != null) b.setLore(lore);
        assert b != null;
        b.setDisplayName(dpname);
        a.setItemMeta(b);
        return a;
    }



}

