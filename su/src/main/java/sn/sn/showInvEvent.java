package sn.sn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static sn.sn.Sn.*;
import static sn.sn.quest.getQuest;
import static sn.sn.quest.openQuestSettingUI;

/*


  雪花速递 express 的监听器类

   实现了 express show 面板打开后的点击物品处理 和关闭面板时保存文件的处理

   作者：LtSeed


*/



public class showInvEvent implements Listener {

    public static Player commander;
    public static int showInv_nmax = 0;
    Plugin plugin = Sn.getPlugin(Sn.class);


    private final ItemStack cancel = new ItemStack(Material.BARRIER);
    private final ItemStack confirm = new ItemStack(Material.EMERALD);
    private final ItemStack pgup = new ItemStack(Material.WRITABLE_BOOK);
    private final ItemStack pgdn = new ItemStack(Material.WRITABLE_BOOK);

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

    public String ask(Player player,String message) {
        AtomicReference<String> answer = new AtomicReference<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                player.sendMessage(message);
                answer.set(getAnswer(player).get(15, TimeUnit.SECONDS));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return answer.get();
    }

    public Future<String> getAnswer(Player player) {

        CompletableFuture<String> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTask(plugin, () -> {

            AskLifeExperience listener = new AskLifeExperience(player.getUniqueId(), future);

            Bukkit.getPluginManager().registerEvents(listener, plugin);

        });

        return future;

    }


    @EventHandler(priority = EventPriority.HIGHEST)
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


    @EventHandler(priority = EventPriority.HIGHEST)
    public void breakEvent(InventoryClickEvent Invclick) {


        commander = Bukkit.getPlayer(Invclick.getView().getPlayer().getUniqueId());
        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.BLUE+"雪花速递")){
            Invclick.setCancelled(true);
            ClickType cli = Invclick.getClick();//获得clickType 类型
            if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
                //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
                commander.closeInventory();
            }
            if(cli.isLeftClick()){
                HashMap<Integer, ItemStack> remains;
                if(Invclick.getClickedInventory() == null||Invclick.getCurrentItem() == null) return ;
                //点击showInv物品:取回该项
                if(Invclick.getClickedInventory().equals(showInv.get(commander))) {
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
            Invclick.setCancelled(true);
            ClickType cli = Invclick.getClick();//获得clickType 类型
            if(cli.isKeyboardClick()||cli==ClickType.WINDOW_BORDER_LEFT||cli==ClickType.WINDOW_BORDER_RIGHT){
                //键盘操作、点窗口外面，退出界面*(代码没有发挥作用？)
                commander.closeInventory();
            }

            if(Objects.requireNonNull(Invclick.getClickedInventory()).getSize()==9) {
                //玩家点击的是上面的设置内容
                String name = Objects.requireNonNull(Objects.requireNonNull(Invclick.getClickedInventory().getItem(4)).getItemMeta()).getDisplayName();
                if(Invclick.getSlot() == 2){//isOn
                    if(questseting.get(commander).isOn()) {
                        if(!questseting.get(commander).turnOff()) return;
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
                }
                if(Invclick.getSlot() == 3){
                    Inventory typeset = Bukkit.createInventory(commander,9,ChatColor.GREEN+"选择任务类型");
                    typeset.setItem(1, getItem_QuestTypeSetting("MAIN","主线任务",null));
                    typeset.setItem(2, getItem_QuestTypeSetting("SIDE","支线任务",null));
                    typeset.setItem(3, getItem_QuestTypeSetting("TRIGGER","触发任务",null));
                    typeset.setItem(5, getItem_QuestTypeSetting("DAILY","日常任务",null));
                    typeset.setItem(6, getItem_QuestTypeSetting("REWARD","悬赏任务",null));
                    typeset.setItem(7, getItem_QuestTypeSetting("DIY","自编任务",null));
                    commander.openInventory(typeset);
                    return;
                }

                if(Invclick.getSlot() == 5){//quest position
                    Inventory positionset = Bukkit.createInventory(commander,54,ChatColor.GREEN+"父任务设置：请选择父任务" );
                    if(quests.length<=53){
                        for (int i = 0; i < quests.length; i++) {
                            quest.Quest quest = quests[i];
                            positionset.setItem(i+1, getItem("BOOK", quest.getQuestname(), List.of(quest.getQuestdescription())));
                        }
                        positionset.setItem(0,nonp());
                        commander.openInventory(positionset);
                    } else {
                        openPositionChooseMultiPageUI(commander,1);
                    }
                    return;
                }
                if(Invclick.getSlot() == 7){//questreward
                    Inventory rewardset = Bukkit.createInventory(commander,9,ChatColor.GREEN+"任务奖励设置" );
                    List<String> a = new ArrayList<>();
                    a.add("设置为系统任务");
                    a.add("系统任务在发放奖励时不会收取创建人物品");
                    rewardset.setItem(0,getItem("REDSTONE_BLOCK","系统任务",a));
                    a = new ArrayList<>();
                    a.add("设置");
                    //rewardset.setItem();
                }


            }
        }
        if(Invclick.getView().getTitle().equalsIgnoreCase(ChatColor.GREEN+"选择任务类型")){
            Invclick.setCancelled(true);
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


            }



        }
        if(Invclick.getView().getTitle().contains("父任务设置：请选择父任务")) {
            Invclick.setCancelled(true);
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



    }

    private boolean workPositionSet(String name) {
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

    private ItemStack nonp() {
        ItemStack nonp = new ItemStack(Material.BARRIER,1);
        ItemMeta a = nonp.getItemMeta();
        assert a != null;
        a.setDisplayName("无父任务");
        nonp.setItemMeta(a);
        return nonp;
    }

    private void openPositionChooseMultiPageUI(Player commander,int pgindex) {
        Inventory positionset = Bukkit.createInventory(commander,54,ChatColor.GREEN+"父任务设置：请选择父任务(第"+pgindex+"页)" );
        int nowindex =45 * (pgindex - 1);

        for (int i = 0; i < 45; i++) {
            quest.Quest quest = quests[i];
            positionset.setItem(nowindex + i, getItem("BOOK", quest.getQuestname(), List.of(quest.getQuestdescription())));
        }
        if(pgindex != 1)positionset.setItem(45,pgup);
        if(quests.length > nowindex + 45)positionset.setItem(53,pgdn);
        commander.openInventory(positionset);
    }

    private ItemStack getItem_QuestTypeSetting(String typname, String dpname, List<String> lore){
        ItemStack a = new ItemStack(quest.QuestType.valueOf(typname).getSymbol(),1);
        ItemMeta b = a.getItemMeta();
        if(b != null) b.setLore(lore);
        assert b != null;
        b.setDisplayName(dpname);
        a.setItemMeta(b);
        return a;
    }

    private ItemStack getItem(String typname, String dpname, List<String> lore){
        ItemStack a = new ItemStack(Material.valueOf(typname),1);
        ItemMeta b = a.getItemMeta();
        if(b != null) b.setLore(lore);
        assert b != null;
        b.setDisplayName(dpname);
        a.setItemMeta(b);
        return a;
    }



}

