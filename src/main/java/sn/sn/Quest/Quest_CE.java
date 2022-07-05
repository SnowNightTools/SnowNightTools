package sn.sn.Quest;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import sn.sn.Basic.Other;

import java.util.*;

import static sn.sn.UI.OpenUI.openQuestSettingUI;
import static sn.sn.Sn.*;

/*



枚举格式
    代码：英文名 值
指令
    quest：

        help\?\？\-：帮助

        now：打印现在正在进行的任务信息

        done：打印完成过的所有任务列表，包括完成时间

        trace：开始追踪任务目标点  给目标点高亮 （？）

        list：打印现在你可以进行的任务列表

        select：选择你现在想要进行的任务，进行切换（如果你有多个可以进行的任务）

        getreward:当任务完成时无法领取奖励，可以使用这个指令重新领取，并跳到下一个任务

        create [TYPE] [name]： quest create Quest firstquest  quest create QuestPosition qp1
            新建一个类型：
            sn.Quest
            TYPE：
                1:Quest
                2:QusetPosition
                3:QusetAction
                4:QusetActionData
                5:QusetReward
                6：int（直接值）
                7：String（直接值）
                8：String[]（直接值）
                9：char（直接值）
                10：ItemStack
                11:ItemStack[]
                12:Entity
                13:Entity[]
                14：double（直接值）
                15：block

        setvalue [TYPE] [name] [value]： setvalue QuestPosition firstquest qp1
            设置某个量的值（仅限于6.7.8.9.10.11.12.14.15）

            setvalue ItemStack [name] hand 可以设置一个物品堆量为手上的物品堆
            setvalue ItemStack[] [name] inv 可以设置一个物品堆组量为背包里的所有物品堆构成的组

            setvalue Entity [name] now 可以设置一个实体量为你下一个交互的实体

        addvalue [TYPE] [name] [value]：
            向某个量中添加值（仅限于11.13）

            addvalue ItemStack[] [name] hand 可以为一个物品堆量添加手上的物品堆
            addvalue ItemStack[] [name] inv 可以为一个物品堆量添加背包里的所有物品堆

            addvalue Entity[] [name] now 可以添加一个实体量为你下一个交互的实体


        set [parentname] [property] [childname]：
            设置某类型的某属性继承（仅限于1.2.3.4.5.13）
            property：

                Quest
                    QuestPosition questposition;
                    int questtype;（直接值）
                    QuestAction[] questacceptcondition;
                    QuestAction[] questtarget;
                    QuestReward questreward;
                    String[] questdescription;（直接值）
                    boolean isSync;

                QuestPosition
                    int questlevel;（直接值）
                    Quest parentquest;
                    Quest[] childquest;

                QuestAction
                    int questactiontype;（直接值）
                    QuestActionData questactiondata;

                QuestActionData
                    ItemStack[] questtargetitem;
                    int questtimelimit;（直接值）
                    Entity questtargetmonster;
                    Entity questtargetentity;
                    ItemStack[] foundquesttargetitem;
                    int targetpositionx,targetpositiony,targetpositionz;（直接值）
                    Entity questtargetanimal;
                    Block questtargetblock;
                    double defaultdistance

                QuestReward
                    double rewardmoney;（直接值）
                    ItemStack[] rewarditems;
                    String[] rewardpermission;（直接值）

                ItemStack
                    特殊方法：使用 hand 获取手上的方块堆

        info [name]：
            打印名为name的量的信息


信息
    1.玩家任务执行信息（存储在Playerquest.yml）每个玩家都有自己的任务信息，它们形如：
    Player:
        on: false
        process:
            action1: true
            ……
        nowtaskid: id
        nowquest: name(一个quest类的名字)
        starttime: time
        enableamount: amount（能够执行的任务数）
        questenable:（能够执行的任务）
            quest1: name1
            quest2: name2
            ……
        done:(完成过的任务 完成时间 用时)
            quest1:
                name：1
                time：2022.2.2_17:50 //完成事件
                usedtime：81min //用时
            quest2:
            ……

     2.类和量信息（存储在Quest.yml中）每个被create创建的量都有自己的信息。
     Name：
        type：1
        property-inherit：(继承的属性)
            propertyname1: name1
            propertyname2: name2
            ……
        property-set:(直接设置的属性)
            propertyname1: value1
            propertyname2: value2
            ……

类：
    任务
        执行者（Player)，任务位置（New 任务位置），任务种类，任务接取条件（New 操作），任务目标（操作），任务报酬，任务描述文本（string[]，  int 行数）

    任务位置：
        任务等级：（int）
            主线（1）
            主线的支线（2）
            主线的支线的支线（3）
            ....

        父任务：（New 任务）

        子任务集：(New 任务[]）



    任务报酬：
        1、货币（int）
        2、物品奖励（ItemStack[])
        3、权限（String)

    任务指引方式:
        1、粒子指引
        2、文本指引

    任务需要玩家进行的操作：（int  序号）
        1、收集类（ItemStack[]  目标物品属性组， int  时间)COLLECT
            检查背包中的目标物品数量（+时限）
        2、讨伐类（New  怪物种类,int 讨伐数量)CRUSADE
            接受任务开始计数（+时限）
        3、寻找类（生物）（New  目标生物数据）FIND_NPC
            检测与目标NPC的距离
        4、寻找类（物）（ItemStack[]  目标物品数据）FIND_ITEM
            检测背包内是否有目标物品
        5、寻找类（目标点）（New  目标点数据）FIND_POSITION
            检测玩家与目标点的距离
        6、建筑类（需人工检查）BUILD
            人工
        7、成就系统类（待定）ACCOMPLISHMENT
        8、养成类（动物）（New  动物数据）HUSBANDRY
            检查范围内生物数量
        9、养成类（植物）（New  植物数据）AGRICULTURE
            检查范围内植物数量

    任务类型：（int  序号, String key）
        1、主线 MAIN
        2、支线 SIDE
        3、触发事件 TRIGGER
        4、日常 DAILY
        5、悬赏 REWARD
        6、自编 DIY

        */
@SuppressWarnings({"unused", "SpellCheckingInspection", "UnusedReturnValue"})
public class Quest_CE implements CommandExecutor {


    public Player questPlayer;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label,@NotNull String[] args) {

        if(!label.equalsIgnoreCase("quest")){
            return true;
        }

        if(args.length == 0) return help();

        //获取玩家背包信息
        if(sender instanceof Player){
            questPlayer = (Player)sender;
        } else {
            Other.sendInfo("玩家信息异常，请联系管理员。");
            return true;
        }

        if(args[0].equals("test")){
            new Thread(() -> {
                List<Block> blockList = QuestRuntime.getBlockList(questPlayer.getWorld(), questPlayer.getLocation(), 10);
                int index = 0;
                for (Block block : blockList) {
                    questPlayer.sendMessage("--------------------------------");
                    questPlayer.sendMessage("index:" +index++);
                    questPlayer.sendMessage("block:" +block.getType());
                    questPlayer.sendMessage("state:" +block.getState());
                    questPlayer.sendMessage("loc:" +block.getLocation());
                    questPlayer.sendMessage("tem:" +block.getTemperature());
                    questPlayer.sendMessage("--------------------------------");
                }
            }).start();
            return true;
        }

        //quest show now：打印现在正在进行的任务信息
        //quest show other [questname]打印特定任务信息。
        if(args[0].equalsIgnoreCase("show")){
            workQuestShowCE(args);
        }

        //quest done打印以前已经完成过的任务信息和时间
        if(args[0].equalsIgnoreCase("done")){
            return workQuestDoneCE(args);
        }

        //quest list：打印现在你可以进行的任务列表
        //          enableamount: amount（能够执行的任务数）
        //          questenable:（能够执行的任务）
        //            0: name1
        //            1: name2
        //            ……
        if(args[0].equalsIgnoreCase("list")){
            return workQuestListCE();
        }

        //quest select [index]：选择你现在想要进行的任务，进行切换（如果你有多个可以进行的任务）
        if(args[0].equalsIgnoreCase("select")){
            return workQuestSelectCE(args);

        }

        //getreward:当任务完成时无法领取奖励，可以使用这个指令重新领取，并跳到下一个任务
        if(args[0].equalsIgnoreCase("getreward")){
            return workQuestGetRewardCE();
        }

        //create [questname]：
        if(args[0].equalsIgnoreCase("create")){
            if (workQuestCreateCE(args)) return false;

        }

        //set
        if(args[0].equalsIgnoreCase("set")){
            if(args.length==1){
                if(!quest_setting.containsKey(questPlayer)){
                    questPlayer.sendMessage("请先新建一个任务！");
                    return false;
                }
                openQuestSettingUI(questPlayer);
                return true;
            }
        }

        return true;
    }

    private boolean workQuestCreateCE(String[] args) {
        if(args.length==2){

            if(args[1].contains("\\")|| args[1].contains("'")|| args[1].contains("\"")|| args[1].contains(" ")|| args[1].contains(".")){
                questPlayer.sendMessage(ChatColor.RED+"非法的任务名！");
                return true;
            }
            String name = args[1];
            if(quest_yml.contains(name)){
                questPlayer.sendMessage(ChatColor.YELLOW+"任务名被占用！");
                return true;
            }

            if(quest_setting.containsKey(questPlayer)){
                questPlayer.sendMessage("请继续处理你未完成的任务！");
            } else quest_setting.put(questPlayer,new Quest(name));
            openQuestSettingUI(questPlayer,name);

        }
        return false;
    }

    private boolean workQuestGetRewardCE() {
        if(playerquest_yml.getBoolean(questPlayer.getName()+".rewarding",false)){
            playerquest_yml.set(questPlayer.getName()+".rewarding",null);
            String name = playerquest_yml.getString(questPlayer.getName()+".nowquest");
            Quest.getQuest(name).succeed(questPlayer);
        }else questPlayer.sendMessage("你没有需要领取的奖励！");
        return true;
    }

    private boolean workQuestSelectCE(String[] args) {
        int amount = playerquest_yml.getInt(questPlayer.getName()+".enableamount");
        if(amount == 0){
            questPlayer.sendMessage("你现在没有可以选择的任务~");
            return true;
        }
        int index = Integer.parseInt(args[1]);
        if(index>=0&&index<=amount){
            String questname = playerquest_yml.getString(questPlayer.getName() + ".questenable." + index);
            if(!Quest.loadQuest(questPlayer,questname)){
                questPlayer.sendMessage("任务选择失败！");
                return false;
            } else return true;
        } else {
            questPlayer.sendMessage("请输入正确的序号！");
            return false;
        }
    }

    private boolean workQuestListCE() {
        int amount = playerquest_yml.getInt(questPlayer.getName()+".enableamount");
        if(amount == 0){
            questPlayer.sendMessage("你现在没有可以选择的任务~");
            return true;
        }
        questPlayer.sendMessage("你现在可以选择以下任务：");
        for (int i = 0; i < amount; i++) {
            questPlayer.sendMessage(Objects.requireNonNull(playerquest_yml.getString(questPlayer.getName() + ".questenable." + i)));
        }
        return true;
    }

    private boolean workQuestDoneCE(String[] args) {
        if(!args[1].isEmpty()){
            int i = Integer.parseInt(args[1]);
            String tmpname = playerquest_yml.getString(questPlayer.getName()+".donelist."+i);
            questPlayer.sendMessage("第"+i+"个任务，任务名："+tmpname);
            questPlayer.sendMessage("开始时间："+playerquest_yml.getString(questPlayer.getName()+".done."+tmpname+".starttime"));
            questPlayer.sendMessage("完成时间："+playerquest_yml.getString(questPlayer.getName()+".done."+tmpname+".endtime"));
            questPlayer.sendMessage("用时："+playerquest_yml.getString(questPlayer.getName()+".done."+tmpname+".usedtime"));
            ++i;
            return true;
        }
        if(playerquest_yml.contains(questPlayer.getName()+".done")){
            int i=0;
            while(i<playerquest_yml.getInt(questPlayer.getName()+".doneamount")){
                String tmpname = playerquest_yml.getString(questPlayer.getName()+".donelist."+i);
                questPlayer.sendMessage("第"+i+"个任务，任务名："+tmpname);
                questPlayer.sendMessage("开始时间："+playerquest_yml.getString(questPlayer.getName()+".done."+tmpname+".starttime"));
                questPlayer.sendMessage("完成时间："+playerquest_yml.getString(questPlayer.getName()+".done."+tmpname+".endtime"));
                questPlayer.sendMessage("用时："+playerquest_yml.getString(questPlayer.getName()+".done."+tmpname+".usedtime"));
                ++i;
            }
            questPlayer.sendMessage("以上~");
        } else questPlayer.sendMessage("你好像还没有完成过任务哦~");
        return true;
    }

    private void workQuestShowCE(String[] args) {
        if(args[1].equalsIgnoreCase("now")) {
            int taskid = playerquest_yml.getInt(questPlayer.getName()+".nowtaskid");

            String nowquestname;
            nowquestname = playerquest_yml.getString(questPlayer.getName() + ".nowquest");
            show(nowquestname, questPlayer);
        }
        if(args[1].equalsIgnoreCase("other")) {
            if(Quest.isQuestExist(args[2])) show(args[2], questPlayer);
            else questPlayer.sendMessage("任务不存在！");
        }
    }

    private boolean help() {
        if(questPlayer.isOp()) {
            Other.sendInfo("/quest create [任务名] 打开任务新建界面");
            return true;
        } else return false;
    }

    private void show(String name,Player player) {
        show(quest_yml,name,player);
    }

    //简要显示信息
    private void show(YamlConfiguration ymlfile, String name, Player player){
        switch (ymlfile.getInt(name+".type")){
            case 1:
                if(Quest.isQuestExist(name)){
                    Quest a = Quest.getQuest(name);
                    player.sendMessage(ChatColor.GREEN+"任务名："+a.getQuestName());
                    player.sendMessage(ChatColor.GREEN+"任务编号："+a.getQuestNumber());
                    player.sendMessage(ChatColor.GREEN+"父任务："+a.getQuestPosition().getParentquest());
                    player.sendMessage(ChatColor.GREEN+"子任务："+a.getQuestPosition().getChildquest());
                    player.sendMessage(ChatColor.GREEN+"任务描述：");
                    List<String> tempstr = a.getQuestDescription();
                    for (int i = 0; i < a.getQuestDescriptionLine(); i++) {
                        player.sendMessage(ChatColor.WHITE + tempstr.get(i));
                    }
                    player.sendMessage(ChatColor.GREEN+"任务目标：");
                    List<QuestAction> tempqa = a.getQuestAcceptCondition();
                    for (int i = 0; i < a.getQuestAcceptConditionAmount(); i++) {
                        player.sendMessage(ChatColor.WHITE + "操作"+(i+1)+"：");
                        show(ymlfile, tempqa.get(i).getQuestactionname(),player);
                    }
                } else player.sendMessage("查询失败~");
                break;
            case 3:
                QuestAction a = new QuestAction();
                a.readQaFromYml(ymlfile, name);
                switch (a.getQuestactiontype()){
                    case BUILD:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个建筑操作，请按照任务描述进行建筑~");
                        ymlfile.set(player.getName()+".check",false);
                        break;
                    case COLLECT:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个收集操作，你需要收集以下物品：");
                        List<ItemStack> b = a.getQuestactiondata().getQuesttargetitem();
                        for (ItemStack stack : b) {
                            player.sendMessage(ChatColor.WHITE + stack.toString());
                        }
                        ymlfile.set(player.getName()+".check",false);
                        break;
                    case CRUSADE:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个讨伐操作，你需要讨伐以下怪物：");
                        Map<EntityType,Integer> c = a.getQuestactiondata().getQuesttargetentity();
                        for (EntityType key:c.keySet()) {
                            player.sendMessage(ChatColor.WHITE+key.getKey().getKey().toLowerCase()+" 数量："+c.get(key));
                        }
                        ymlfile.set(player.getName()+".check",true);
                        break;
                    case FIND_NPC:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个寻找操作，你需要找到以下NPC：");
                        Entity d= Bukkit.getEntity(a.getQuestactiondata().getQuesttargetnpc());
                        assert d != null;
                        player.sendMessage(ChatColor.WHITE+"它叫"+d.getName());
                        ymlfile.set(player.getName()+".check",true);
                        break;
                    case FIND_ITEM:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个寻找操作，你需要找到这个物品：");
                        ItemStack f = a.getQuestactiondata().getQuesttargetitem().get(0);
                        player.sendMessage(ChatColor.WHITE+f.toString());
                        ymlfile.set(player.getName()+".check",true);
                        break;
                    case HUSBANDRY:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个养小动物的操作，你的身边需要有以下小动物：");
                        Map<EntityType,Integer> e = a.getQuestactiondata().getQuesttargetentity();
                        for (EntityType key:e.keySet()) {
                            player.sendMessage(ChatColor.WHITE+key.getKey().getKey().toLowerCase()+" 数量："+e.get(key));
                        }
                        ymlfile.set(player.getName()+".check",true);
                        break;
                    case AGRICULTURE:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个种植操作，你的身边需要出现下列作物：");
                        List<ItemStack> g = a.getQuestactiondata().getQuesttargetitem();
                        for (ItemStack itemStack : g) {
                            player.sendMessage(ChatColor.WHITE + itemStack.toString());
                        }
                        ymlfile.set(player.getName()+".check",true);
                        break;
                    case FIND_POSITION:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个寻找操作，你需要找到这个坐标：");
                        player.sendMessage(ChatColor.WHITE+"["+a.getQuestactiondata().getTargetpositionx()+','+a.getQuestactiondata().getTargetpositiony()+','+a.getQuestactiondata().getTargetpositionz()+']');
                        ymlfile.set(player.getName()+".check",true);
                        break;
                    case ACCOMPLISHMENT:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个成就系统操作，请阅读任务描述~");
                        ymlfile.set(player.getName()+".check",false);
                        break;

                }

                break;
            default:
                Other.sendInfo("要查询这个信息，你可能需要换个指令（或者换个号QWQ）！");
                break;
        }
    }


}
