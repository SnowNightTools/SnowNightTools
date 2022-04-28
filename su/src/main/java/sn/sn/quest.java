package sn.sn;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

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


public class quest implements CommandExecutor {




    public static Player questPlayer;
    static Plugin Snplugin = Sn.getPlugin(Sn.class);

    public static Boolean loadQuest(Player player, String name) {

        if(!isQuestExist(name)){
            player.sendMessage("任务无法找到，请联系管理员！");
            return false;
        }

        if(!getQuest(name).isOn()){
            if(playerquest_yml.contains(player.getName()+".questenable")){
                player.sendMessage("任务未启动，尝试启动其他任务！");
                addQuest(player,name);
                return loadQuest(player,playerquest_yml.getString(player.getName()+".questenable.0"));
            }
            player.sendMessage("任务启动失败！");
            return false;
        }
        if(!Objects.requireNonNull(playerquest_yml.getString(player.getName() + ".nowquest")).equalsIgnoreCase(name)) {
            int amount = playerquest_yml.getInt(player.getName() + "amount");
            boolean found = false;
            for (int i = 0; i < amount; i++) {
                if (Objects.equals(playerquest_yml.getString(player.getName() + ".questenable." + i), name)) {
                    playerquest_yml.set(player.getName() + ".questenable." + i, playerquest_yml.getString(player.getName() + ".nowquest"));
                    playerquest_yml.set(player.getName() + ".nowquest", name);
                    found = true;
                    break;
                }
            }
            if(!found){
                player.sendMessage("无法找到任务，请联系管理员！");
                return false;
            }
        }

        questRuntime a = new questRuntime();
        a.runTaskAsynchronously(Snplugin);
        playerquest_yml.set(player.getName()+".nowtaskid",a.getTaskId());

        try {
            playerquest_yml.save(playerquest_file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static Quest getQuest(int id){
        return quests[id];
    }



    private static void addQuest(Player player, String name) {
        if(isQuestExist(name)){
            int amount = playerquest_yml.getInt(player.getName()+".enableamount");
            playerquest_yml.set(player.getName()+".questenable."+amount,name);
            playerquest_yml.set(player.getName()+".enableamount",amount+1);
        }
    }

    public static Boolean isQuestExist(int id){
        return isQuestExist(getQuest(id).getQuestname());
    }

    public static Boolean isQuestExist(String name){
        return isQuestExist(quest_yml,name);
    }

    public static Boolean isQuestExist(YamlConfiguration ymlfile,int id){
        return isQuestExist(quest_yml,getQuest(id).getQuestname());
    }

    public static Boolean isQuestExist(YamlConfiguration ymlfile, String name){
        return ymlfile.contains(name);
    }

    public static Quest getQuest(String name){
        int id = quest_yml.getInt("inside."+name);
        return quests[id];
    }

    public static void openQuestSettingUI(Player questPlayer){
        openQuestSettingUI(questPlayer,questseting.get(questPlayer).getQuestname());
    }

    public static void openQuestSettingUI(Player questPlayer,String questname){
        Inventory questcreate = Bukkit.createInventory(questPlayer,9,ChatColor.BLUE+"正在创建一个新任务，请填写以下信息");


        ItemStack nameicon = new ItemStack(Material.BOOK);
        ItemMeta nameiconmeta = nameicon.getItemMeta();
        assert nameiconmeta != null;
        nameiconmeta.setDisplayName(questname);
        List<String> namelore = new ArrayList<>();
        namelore.add(ChatColor.RED+"任务名应该不含空格、\\（右斜杠）、单/双引号、英文句点等特殊字符");
        namelore.add(ChatColor.RED+"任务名应该不以数字开头");
        namelore.add(ChatColor.RED+"任务名应该独一无二");
        namelore.add(ChatColor.YELLOW+"任务名最好不含中文字符，否则可能会出现意料之外的编码错误");
        namelore.add(ChatColor.GREEN+"任务名最好与它的内容相对应");
        namelore.add("例子：FirstQuestofSnowNightTown");
        nameiconmeta.addEnchant(Enchantment.DIG_SPEED,1,false);
        nameiconmeta.setLore(namelore);
        nameicon.setItemMeta(nameiconmeta);
        questcreate.setItem(4,nameicon);


        ItemStack positionicon = new ItemStack(SettingType.QUESTPOSITION.getSymbol());
        ItemMeta positioniconmeta = positionicon.getItemMeta();
        assert positioniconmeta != null;
        positioniconmeta.setDisplayName("任务位置");
        List<String> positionlore = new ArrayList<>();
        positionlore.add(ChatColor.GREEN+"点我设置任务位置");
        if(questseting.get(questPlayer).isPositionSet()) {
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
            typeiconmeta.addEnchant(Enchantment.LUCK,1,false);
            typeicon.setType(questseting.get(questPlayer).getQuesttype().getSymbol());
        }
        typeiconmeta.setLore(typelore);
        typeicon.setItemMeta(typeiconmeta);
        questcreate.setItem(3, typeicon);


        ItemStack targeticon = new ItemStack(SettingType.QUESTACTION.getSymbol());
        ItemMeta targeticonmeta = targeticon.getItemMeta();
        assert targeticonmeta != null;
        targeticonmeta.setDisplayName("任务目标");
        List<String> targetlore = new ArrayList<>();
        targetlore.add(ChatColor.GREEN+"点我设置任务目标");
        if(questseting.get(questPlayer).isTargetSet()) {
            targeticonmeta.addEnchant(Enchantment.ARROW_DAMAGE,1,false);
        }
        targeticonmeta.setLore(targetlore);
        targeticon.setItemMeta(targeticonmeta);
        questcreate.setItem(6, targeticon);


        ItemStack acccondtnicon = new ItemStack(SettingType.QUESTACTION.getSymbol());
        ItemMeta acccondtniconmeta = acccondtnicon.getItemMeta();
        assert acccondtniconmeta != null;
        acccondtniconmeta.setDisplayName("任务接受条件");
        List<String> acccondtnlore = new ArrayList<>();
        acccondtnlore.add(ChatColor.GREEN+"点我设置任务接受条件");
        acccondtnlore.add("如果没有特别的任务接受条件，");
        acccondtnlore.add("也请将其设置为“默认“！");
        if(questseting.get(questPlayer).isAcceptconditionSet()) {
            acccondtniconmeta.addEnchant(Enchantment.ARROW_DAMAGE,1,false);
        }
        acccondtniconmeta.setLore(acccondtnlore);
        acccondtnicon.setItemMeta(acccondtniconmeta);
        questcreate.setItem(2, acccondtnicon);


        ItemStack rewardicon = new ItemStack(SettingType.QUESTREWARD.getSymbol());
        ItemMeta rewardiconmeta = rewardicon.getItemMeta();
        assert rewardiconmeta != null;
        rewardiconmeta.setDisplayName("任务奖励");
        List<String> rewardlore = new ArrayList<>();
        rewardlore.add(ChatColor.GREEN+"点我设置任务奖励");
        if(questseting.get(questPlayer).isAcceptconditionSet()) {
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

        if(questseting.get(questPlayer).isAcceptconditionSet()) {
            oniconmeta.addEnchant(Enchantment.CHANNELING,1,false);
        }
        if(questseting.get(questPlayer).isOn()){
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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!label.equalsIgnoreCase("quest")){
            return true;
        }


        //获取玩家背包信息
        if(sender instanceof Player){
            questPlayer = (Player)sender;
        } else {
            say("玩家信息异常，请联系管理员。");
            return true;
        }

        //quest show now：打印现在正在进行的任务信息
        //quest show other [questname]打印特定任务信息。
        if(args[0].equalsIgnoreCase("show")){
            if(args[1].equalsIgnoreCase("now")) {
                int taskid = playerquest_yml.getInt(questPlayer.getName()+".nowtaskid");

                String nowquestname;
                nowquestname = playerquest_yml.getString(questPlayer.getName() + ".nowquest");
                show(nowquestname, questPlayer);
            }
            if(args[1].equalsIgnoreCase("other")) {
                if(isQuestExist(args[2])) show(args[2], questPlayer);
                else questPlayer.sendMessage("任务不存在！");
            }
        }

        //quest done打印以前已经完成过的任务信息和时间
        if(args[0].equalsIgnoreCase("done")){
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

        //quest list：打印现在你可以进行的任务列表
        //          enableamount: amount（能够执行的任务数）
        //          questenable:（能够执行的任务）
        //            0: name1
        //            1: name2
        //            ……
        if(args[0].equalsIgnoreCase("list")){
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


        //quest select [index]：选择你现在想要进行的任务，进行切换（如果你有多个可以进行的任务）
        if(args[0].equalsIgnoreCase("select")){
            int amount = playerquest_yml.getInt(questPlayer.getName()+".enableamount");
            if(amount == 0){
                questPlayer.sendMessage("你现在没有可以选择的任务~");
                return true;
            }
            int index = Integer.parseInt(args[1]);
            if(index>=0&&index<=amount){
                String questname = playerquest_yml.getString(questPlayer.getName() + ".questenable." + index);
                if(!loadQuest(questPlayer,questname)){
                    questPlayer.sendMessage("任务选择失败！");
                    return false;
                } else return true;
            } else {
                questPlayer.sendMessage("请输入正确的序号！");
                return false;
            }

        }

        //getreward:当任务完成时无法领取奖励，可以使用这个指令重新领取，并跳到下一个任务
        if(args[0].equalsIgnoreCase("getreward")){
            if(playerquest_yml.getBoolean(questPlayer.getName()+".rewarding",false)){
                playerquest_yml.set(questPlayer.getName()+".rewarding",null);
                String name = playerquest_yml.getString(questPlayer.getName()+".nowquest");
                getQuest(name).succeed(questPlayer);
            }else questPlayer.sendMessage("你没有需要领取的奖励！");
            return true;
        }


        //create [questname]：
        if(args[0].equalsIgnoreCase("create")){
            if(args.length==2){

                if(args[1].contains("\\")||args[1].contains("\'")||args[1].contains("\"")||args[1].contains(" ")||args[1].contains(".")){
                    questPlayer.sendMessage(ChatColor.RED+"非法的任务名！");
                    return false;
                }
                String name = args[1];
                if(quest_yml.contains(name)){
                    questPlayer.sendMessage(ChatColor.YELLOW+"任务名被占用！");
                    return false;
                }

                //int questindex = questamount;
                //quests[questamount++] = new Quest(name);
                if(questseting.containsKey(questPlayer)){
                    questPlayer.sendMessage("请继续处理你未完成的任务！");
                } else questseting.put(questPlayer,new Quest(name));


                openQuestSettingUI(questPlayer,name);


            }

        }
        //set
        if(args[0].equalsIgnoreCase("set")){
            if(args.length==2){
                if(!questseting.containsKey(questPlayer)){
                    questPlayer.sendMessage("请先新建一个任务！");
                    return false;
                }
                openQuestSettingUI(questPlayer);
                return true;
            }
        }


        return true;
    }

    private void show(String name,Player player) {
        show(quest_yml,name,player);
    }

    //简要显示信息
    private void show(YamlConfiguration ymlfile, String name, Player player){
        switch (ymlfile.getInt(name+".type")){
            case 1:
                if(isQuestExist(name)){
                    Quest a = getQuest(name);
                    player.sendMessage(ChatColor.GREEN+"任务名："+a.getQuestname());
                    player.sendMessage(ChatColor.GREEN+"任务编号："+a.getQuestnumber());
                    player.sendMessage(ChatColor.GREEN+"父任务："+a.getQuestposition().getParentquest());
                    player.sendMessage(ChatColor.GREEN+"子任务："+a.getQuestposition().getChildquest());
                    player.sendMessage(ChatColor.GREEN+"任务描述：");
                    String[] tempstr = a.getQuestdescription();
                    for (int i = 0; i < a.getQuestdescriptionline(); i++) {
                        player.sendMessage(ChatColor.WHITE + tempstr[i]);
                    }
                    player.sendMessage(ChatColor.GREEN+"任务目标：");
                    QuestAction[] tempqa = a.getQuestacceptcondition();
                    for (int i = 0; i < a.getQuestacceptconditionamount(); i++) {
                        player.sendMessage(ChatColor.WHITE + "操作"+(i+1)+"：");
                        show(ymlfile,tempqa[i].getQuestactionname(),player);
                    }
                } else player.sendMessage("查询失败~");
                break;
            case 3:
                QuestAction a = new QuestAction();
                a.readQaFromYml(ymlfile, name);
                switch (a.questactiontype){
                    case BUILD:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个建筑操作，请按照任务描述进行建筑~");
                        ymlfile.set(player.getName()+".check",false);
                        break;
                    case COLLECT:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个收集操作，你需要收集以下物品：");
                        ItemStack[] b = a.getQuestactiondata().getQuesttargetitem();
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
                        player.sendMessage(ChatColor.WHITE+"它叫"+d.getName());
                        ymlfile.set(player.getName()+".check",true);
                        break;
                    case FIND_ITEM:
                        player.sendMessage(ChatColor.GREEN+"这个操作是一个寻找操作，你需要找到这个物品：");
                        ItemStack f = a.getQuestactiondata().getQuesttargetitem()[0];
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
                        ItemStack[] g = a.getQuestactiondata().getQuesttargetitem();
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
                say("要查询这个信息，你可能需要换个指令（或者换个号QWQ）！");
                break;
        }
    }

    public enum SettingType implements Keyed{
        QUEST(1,"quest", Material.BOOK),
        QUESTPOSITION(2,"qusetposition", Material.PINK_GLAZED_TERRACOTTA),
        QUESTACTION(3,"qusetaction", Material.WRITABLE_BOOK),
        QUESTACTIONDATA(4,"qusetactiondata", Material.WRITTEN_BOOK),
        QUESTREWARD(5,"qusetreward", Material.EMERALD),
        QUESTTYPE(6,"questtype", Material.ARROW);

        final int number;
        final NamespacedKey key;
        final Material symbol;
        SettingType(int number, String key, Material symbol) {
            this.number = number;
            this.key = NamespacedKey.fromString(key);
            this.symbol = symbol;
        }

        @Override
        public NamespacedKey getKey() {
            return key;
        }

        public int getNumber() {
            return number;
        }

        public Material getSymbol() {
            return symbol;
        }
    }
    public enum QuestType implements Keyed {
        MAIN(1,"main", Material.RAIL),
        SIDE(2,"side", Material.POWERED_RAIL),
        TRIGGER(3,"trigger", Material.ACTIVATOR_RAIL),
        DAILY(4,"daily", Material.DETECTOR_RAIL),
        REWARD(5,"reward", Material.EMERALD),
        DIY(6,"diy", Material.WOODEN_PICKAXE);

        final int number;
        final NamespacedKey key;
        final Material symbol;
        QuestType(int number, String key, Material symbol) {
            this.number = number;
            this.key = NamespacedKey.fromString(key);
            this.symbol = symbol;
        }

        @Override
        public NamespacedKey getKey() {
            return key;
        }

        public int getNumber() {
            return number;
        }

        public Material getSymbol() {
            return symbol;
        }
    }
    public enum QuestActionType implements Keyed {
        COLLECT(1,"collect"),
        CRUSADE(2,"crusade"),
        FIND_NPC(3,"find_npc"),
        FIND_ITEM(4,"find_item"),
        FIND_POSITION(5,"find_position"),
        BUILD(6,"build"),
        ACCOMPLISHMENT(7,"accomplishment"),
        HUSBANDRY(8,"husbandry"),
        AGRICULTURE(9,"agriculture");

        final int number;
        final NamespacedKey key;

        QuestActionType(int number,String key) {
            this.number = number;
            this.key = NamespacedKey.fromString(key);
        }

        @Override
        public NamespacedKey getKey() {
            return key;
        }

        public int getNumber() {
            return number;
        }
    }

    public static class Quest implements Serializable {
        private int questnumber;
        private String questname;
        private QuestPosition questposition;
        private QuestType questtype;
        private QuestAction[] questacceptcondition;
        private int questacceptconditionamount;
        private QuestAction[] questtarget;
        private int questtargetamount;
        private QuestReward questreward;
        private String[] questdescription;
        private int questdescriptionline;
        private boolean issync = false;
        private boolean on = false;

        public boolean isOn() {
            return on;
        }

        public Boolean turnOn() {

            if(isTypeSet()&&isTargetSet()&&isAcceptconditionSet()||isDescriptionSet()||isPositionSet()){
                this.on = true;
                return true;
            }
            return false;
        }

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.writeObject(questnumber);
            out.writeObject(questname);
            out.writeObject(questposition);
            out.writeObject(questtype);
            out.writeObject(questacceptcondition);
            out.writeObject(questacceptconditionamount);
            out.writeObject(questtarget);
            out.writeObject(questtargetamount);
            out.writeObject(questreward);
            out.writeObject(questdescription);
            out.writeObject(questdescriptionline);
            out.writeObject(issync);
            out.writeObject(on);
        }
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
            questnumber = (int) in.readObject();
            questname = (String) in.readObject();
            questposition = (QuestPosition) in.readObject();
            questtype = (QuestType) in.readObject();
            questacceptcondition = (QuestAction[]) in.readObject();
            questacceptconditionamount = (int) in.readObject();
            questtarget = (QuestAction[]) in.readObject();
            questtargetamount = (int) in.readObject();
            questreward = (QuestReward) in.readObject();
            questdescription = (String[]) in.readObject();
            questdescriptionline = (int) in.readObject();
            issync = (boolean) in.readObject();
            on = (boolean) in.readObject();
        }
        private void readObjectNoData() throws ObjectStreamException{
            String name = "NewQuest";
            questname = name;
            questnumber = quest_yml.getInt("Amount");//amount比序号大1，所以不用+1
            quest_yml.set("Amount",questnumber+1);//计数
            quest_yml.set("inside."+name,questnumber);
            quest_yml.set("inside."+questnumber,name);
        }

        public Boolean turnOff() {

            Player[] tmp1 = (Player[]) Bukkit.getOfflinePlayers();
            Collection<? extends Player> tmp2 = Bukkit.getOnlinePlayers();
            for (Player tmpplayer : tmp1) {
                if(playerquest_yml.getString(tmpplayer.getName()+".nowquest","").equals(questname)){
                    return false;
                }
            }
            for (Player tmpplayer : tmp2) {
                if(playerquest_yml.getString(tmpplayer.getName()+".nowquest","").equals(questname)){
                    return false;
                }
            }
            this.on = false;
            return true;
        }

        public void forceOff() {

            Player[] tmp1 = (Player[]) Bukkit.getOfflinePlayers();
            Collection<? extends Player> tmp2 = Bukkit.getOnlinePlayers();
            for (Player tmpplayer : tmp1) {
                if(playerquest_yml.getString(tmpplayer.getName()+".nowquest","").equals(questname)){
                    addQuest(tmpplayer,playerquest_yml.getString(tmpplayer.getName()+".nowquest"));
                    playerquest_yml.set(tmpplayer.getName()+".nowquest",null);
                    playerquest_yml.set(tmpplayer.getName()+".process",null);
                }
            }
            for (Player tmpplayer : tmp2) {
                if(playerquest_yml.getString(tmpplayer.getName()+".nowquest","").equals(questname)){
                    addQuest(tmpplayer,playerquest_yml.getString(tmpplayer.getName()+".nowquest"));
                    playerquest_yml.set(tmpplayer.getName()+".nowquest",null);
                    playerquest_yml.set(tmpplayer.getName()+".process",null);
                }
            }
            this.on = false;
        }



        public boolean isSync() {
            return issync;
        }

        public void setSync(boolean issync) {
            this.issync = issync;
        }

        public Quest(String name){
            questname = name;
            questnumber = quest_yml.getInt("Amount");//amount比序号大1，所以不用+1
            quest_yml.set("Amount",questnumber+1);//计数
            quest_yml.set("inside."+name,questnumber);
            quest_yml.set("inside."+questnumber,name);
        }

        public Quest(YamlConfiguration ymlfile, String name){
            questname = name;
            questnumber = ymlfile.getInt("Amount");//amount比序号大1，所以不用+1
            ymlfile.set("Amount",questnumber+1);//计数
            ymlfile.set("inside."+name,questnumber);
            ymlfile.set("inside."+questnumber,name);
        }

        public void succeed(Player winner){

            if(!this.getQuestreward().give(winner)){
                return;
            }

            if(this.getQuestposition().getChildquest()!=null){
                addQuest(winner,this.getQuestposition().getChildquest());
                addQuest(winner,this.getQuestposition().getChildquestother1());
                addQuest(winner,this.getQuestposition().getChildquestother2());
                addQuest(winner,this.getQuestposition().getChildquestother3());
                loadQuest(winner,this.getQuestposition().getChildquest());
            }
            addDoneQuest(winner,this);

            resetQuestProcess(winner);
        }

        private void addDoneQuest(Player winner, Quest quest) {
            addDoneQuest(playerquest_yml,winner,quest);
        }

        public void addDoneQuest(YamlConfiguration ymlfile,Player winner, Quest quest) {

            ymlfile.set(winner.getName()+".doneamount",ymlfile.getInt(winner.getName()+".doneamount",0)+1);
            ymlfile.set(winner.getName()+".donelist."+(ymlfile.getInt(winner.getName()+".doneamount")-1),quest.getQuestnumber());

            ymlfile.set(winner.getName()+".done."+quest.getQuestname()+".id",quest.getQuestnumber());
            ymlfile.set(winner.getName()+".done."+quest.getQuestname()+".starttime",ymlfile.getString(winner.getName()+".starttime"));
            Date now = new Date();
            Calendar nowc = null,starttime = readCalendarFromString(ymlfile.getString(winner.getName()+".starttime"));
            nowc.setTime(now);
            String time = recordCalendarToString(nowc);
            double usedtime = starttime.compareTo(nowc) /1000.0 /60.0;
            ymlfile.set(winner.getName()+".done."+quest.getQuestname()+".endtime",time);
            ymlfile.set(winner.getName()+".done."+quest.getQuestname()+".usedtime",usedtime);

        }

        private String recordCalendarToString(Calendar nowc){
            return nowc.get(Calendar.YEAR)+'.'+nowc.get(Calendar.MONTH)+'.'+'.'+nowc.get(Calendar.DATE)+'_'+nowc.get(Calendar.HOUR_OF_DAY)+':'+nowc.get(Calendar.MINUTE)+"::"+nowc.get(Calendar.SECOND);
        }

        private Calendar readCalendarFromString(String str){
            Calendar time = null;
            //str = "year.mon..day_hour:min::sec"

            int year = Integer.parseInt(str,0,str.indexOf("."),10);
            int mon = Integer.parseInt(str,str.indexOf(".")+1,str.indexOf(".."),10);
            int day = Integer.parseInt(str,str.indexOf("..")+2,str.indexOf("_"),10);
            int hour = Integer.parseInt(str,str.indexOf("_")+1,str.indexOf(":"),10);
            int min = Integer.parseInt(str,str.indexOf(":")+1,str.indexOf("::"),10);
            int sec = Integer.parseInt(str,str.indexOf("::")+2,str.length(),10);

            assert false;
            time.set(year,(mon-1),day,hour,min,sec);

            return time;
        }

        private void resetQuestProcess(Player winner) {
            resetQuestProcess(playerquest_yml,winner);
        }

        public void resetQuestProcess(YamlConfiguration ymlfile,Player winner) {
            if(ymlfile.contains(winner.getName()+".process"))
                ymlfile.set(winner.getName()+".process",null);
        }

        public Boolean readQuestFromYml(String name){
            return readQuestFromYml(quest_yml,name);
        }

        public Boolean readQuestFromYml(int id){
            return readQuestFromYml(quest_yml,quest_yml.getString("inside."+id));
        }

        public Boolean readQuestFromYml(YamlConfiguration ymlfile,int id){
            return readQuestFromYml(ymlfile,ymlfile.getString("inside."+id));
        }

        public Boolean readQuestFromYml(YamlConfiguration ymlfile,String name){
            if(!ymlfile.contains(name)){
                say("[WARNING]读取Quest数据错误，数据不存在");
                return false;
            }

            if(ymlfile.getInt(name+".tpye")!=1){
                say("[WARNING]读取Quest数据错误，该名的类型不正确");
                return false;
            }
            //读取quest position信息
            questposition.readQpFromYml(ymlfile.getString(questname+".property-inherit.questposition"));

            //读取quest type信息
            questtype = QuestType.valueOf(Objects.requireNonNull(ymlfile.getString("property-set.questtype")).toUpperCase(Locale.ROOT));

            //读取quest accept condition 和quest target
            if(ymlfile.contains(questname + "property-set.questacceptconditionamount"))
                if(ymlfile.getInt(questname + "property-set.questacceptconditionamount")!=0) {
                    questacceptconditionamount = ymlfile.getInt(questname + "property-set.questacceptconditionamount");
                    for (int j = 0; j < questacceptconditionamount; j++) {
                        if(!ymlfile.contains(questname + ".property-inherit.questacceptcondition." + j)){
                            say("[WARNING]读取Quest时错误，信息文件可能损坏！");
                            break;
                        }
                        String tname = ymlfile.getString(questname + ".property-inherit.questacceptcondition." + j);
                        questacceptcondition[j].readQaFromYml(ymlfile, tname);
                    }
                }

            if(ymlfile.contains(questname + "property-set.questtargetamount"))
                if(ymlfile.getInt(questname + "property-set.questtargetamount")!=0){
                    questtargetamount = ymlfile.getInt(questname + "property-set.questtargetamount");
                    for(int j=0;j<questtargetamount;j++){
                        if(!ymlfile.contains(questname + ".property-inherit.questtarget." + j)){
                            say("[WARNING]读取Quest时错误，信息文件可能损坏！");
                            break;
                        }
                        String tname =ymlfile.getString(questname + ".property-inherit.questtarget." + j);
                        questtarget[j].readQaFromYml(ymlfile,tname);
                    }
                }

            //quest description
            questdescriptionline=ymlfile.getInt("property-set.questdescriptionline");
            for(int j = 0; j<this.getQuestdescriptionline(); j++){
                questdescription[j] =ymlfile.getString(questname + ".property-set.questdescription." + j);
            }
            //questreward
            questreward.readQrFromYml(ymlfile,ymlfile.getString(questname + ".property-inherit.questreward"));

            //isSync
            if(ymlfile.contains(questname+".property-set.issync"))
                issync = ymlfile.getBoolean(questname+".property-set.issync");

            issync = ymlfile.getBoolean(questname+".property-set.on",false);
            return true;
        }

        public Boolean saveQuestToYml(){
            return saveQuestToYml(quest_yml);
        }

        public Boolean saveQuestToYml(YamlConfiguration ymlfile){
            /*
            Quest[1]
                    int questnumber
                    String questname
                    QuestPosition questposition;
                    int questtype;（直接值）
                    QuestAction[] questacceptcondition;
                    QuestAction[] questtarget;
                    QuestReward questreward;
                    String[] questdescription;（直接值）
            * */
            ymlfile.set(questname+".type",1);
            ymlfile.set(questname+".property-inherit.questposition",questposition.getQuestpositionname());
            ymlfile.set(questname+".property-inherit.questreward",questreward.getQuestrewardname());

            ymlfile.set(questname+".property-set.questnumber",questnumber);
            ymlfile.set(questname+".property-set.questname",questname);
            ymlfile.set(questname+".property-set.questtype",questtype.getKey().getKey());
            //ymlfile.set(questname+".property-inherit","QUEST");
            ymlfile.set(questname+".property-set.questdescriptionline",questdescriptionline);
            ymlfile.set(questname+".property-set.questacceptconditionamount",questacceptconditionamount);
            ymlfile.set(questname+".property-set.questtargetamount",questtargetamount);
            ymlfile.set(questname+".property-set.issync",issync);
            ymlfile.set(questname+".property-set.on",on);

            for (int i = 0; i < questdescriptionline; i++) {
                ymlfile.set(questname+".property-set.questdescription."+i, questdescription[i]);
            }
            for (int i = 0; i < questacceptconditionamount; i++) {
                ymlfile.set(questname+".property-inherit.questacceptcondition."+i,questacceptcondition[i].getQuestactionname());
                questacceptcondition[i].saveQaToYml(ymlfile);
            }

            for (int i = 0; i < questtargetamount; i++) {
                ymlfile.set(questname+".property-inherit.questtarget."+i,questtarget[i].getQuestactionname());
                questtarget[i].saveQaToYml(ymlfile);
            }
            return true;
        }

        public void addQuesttarget(QuestAction a){
            QuestAction[] tempstr = questtarget.clone();
            questtarget = new QuestAction[questtargetamount+1];
            if (questtargetamount >= 0) System.arraycopy(tempstr, 0, questtarget, 0, questtargetamount);
            questtarget[questtargetamount++] = a;
        }

        public void addQuestacceptcondition(QuestAction a){
            QuestAction[] tempstr = questacceptcondition.clone();
            questacceptcondition = new QuestAction[questacceptconditionamount+1];
            if (questacceptconditionamount >= 0)
                System.arraycopy(tempstr, 0, questacceptcondition, 0, questacceptconditionamount);
            questacceptcondition[questacceptconditionamount++] = a;
        }

        public void addQuestdescription(String a){
            String[] tempstr = questdescription.clone();
            questdescription = new String[questdescriptionline+1];
            if (questdescriptionline >= 0) System.arraycopy(tempstr, 0, questdescription, 0, questdescriptionline);
            questdescription[questdescriptionline++] = a;
        }

        public void removeQuesttarget(int index){
            QuestAction[] tempstr = questtarget.clone();
            questtarget = new QuestAction[questtargetamount-1];
            for (int i = 0; i < questtargetamount; i++) {
                if(i>index)questtarget[i-1]=tempstr[i];
                else questtarget[i]=tempstr[i];
            }
            questtargetamount--;
        }

        public void removeQuestacceptcondition(int index){
            QuestAction[] tempstr = questacceptcondition.clone();
            questacceptcondition = new QuestAction[questacceptconditionamount-1];
            for (int i = 0; i < questacceptconditionamount; i++) {
                if(i>index)questacceptcondition[i-1]=tempstr[i];
                else questacceptcondition[i]=tempstr[i];
            }
            questacceptconditionamount--;
        }

        public void removeQuestdescription(int index){
            String[] tempstr = questdescription.clone();
            questdescription = new String[questdescriptionline-1];
            for (int i = 0; i < questdescriptionline; i++) {
                if(i>index)questdescription[i-1]=tempstr[i];
                else questdescription[i]=tempstr[i];
            }
            questdescriptionline--;
        }

        public int getQuestnumber() {
            return questnumber;
        }

        public void setQuestnumber(int questnumber) {
            this.questnumber = questnumber;
        }

        public String getQuestname() {
            return questname;
        }

        public void setQuestname(String questname) {
            this.questname = questname;
        }

        public void setQuesttarget(QuestAction[] questtarget) {
            questtargetamount = questtarget.length;
            this.questtarget = questtarget;
        }

        @Override
        public String toString() {
            return "Quest{" +
                    "questposition=" + questposition +
                    ", questtype=" + questtype +
                    ", questacceptcondition=" + Arrays.toString(questacceptcondition) +
                    ", questacceptconditionamount=" + questacceptconditionamount +
                    ", questtarget=" + Arrays.toString(questtarget) +
                    ", questtargetamount=" + questtargetamount +
                    ", questreward=" + questreward +
                    ", questdescription=" + Arrays.toString(questdescription) +
                    ", questdescriptionline=" + questdescriptionline +
                    '}';
        }


        public void setQuestposition(QuestPosition questposition) {
            this.questposition = questposition;
        }

        public void setQuestreward(QuestReward questreward) {
            this.questreward = questreward;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Quest)) return false;
            Quest quest = (Quest) o;
            return questtype == quest.questtype && questacceptconditionamount == quest.questacceptconditionamount && questtargetamount == quest.questtargetamount && getQuestdescriptionline() == quest.getQuestdescriptionline() && getQuestposition().equals(quest.getQuestposition()) && Arrays.equals(questacceptcondition, quest.questacceptcondition) && Arrays.equals(getQuesttarget(), quest.getQuesttarget()) && getQuestreward().equals(quest.getQuestreward()) && Arrays.equals(getQuestdescription(), quest.getQuestdescription());
        }

        @Override
        public int hashCode() {
            int result = Objects.hash( getQuestposition(), questtype, questacceptconditionamount, questtargetamount, getQuestreward(), getQuestdescriptionline());
            result = 31 * result + Arrays.hashCode(questacceptcondition);
            result = 31 * result + Arrays.hashCode(getQuesttarget());
            result = 31 * result + Arrays.hashCode(getQuestdescription());
            return result;
        }

        public QuestType getQuestType(){
            return questtype;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public QuestAction[] getQustAccptCndtn(){
            return questacceptcondition;
        }

        public int getQuestacceptconditionamount() {
            return questacceptconditionamount;
        }

        private void setQuestacceptconditionamount(int questacceptconditionamount) {
            this.questacceptconditionamount = questacceptconditionamount;
        }

        public int getQuesttargetamount() {
            return questtargetamount;
        }


        public QuestPosition getQuestposition(){
            return questposition;
        }

        private void setQuesttargetamount(int questtargetamount) {
            this.questtargetamount = questtargetamount;
        }

        public QuestAction[] getQuesttarget(){
            return questtarget;
        }

        public QuestReward getQuestreward(){
            return questreward;
        }

        public String[] getQuestdescription() {
            return questdescription;
        }

        public void setQuestdescription(String[] questdescription) {
            questdescriptionline = questdescription.length;
            this.questdescription = questdescription;
        }

        public QuestType getQuesttype() {
            return questtype;
        }

        public void setQuesttype(QuestType questtype) {
            this.questtype = questtype;
        }

        public QuestAction[] getQuestacceptcondition() {
            return questacceptcondition;
        }

        public int getQuestAcceptConditionAmount(){
            return questacceptconditionamount;
        }

        public int getQuestTargetAmount(){
            return questtargetamount;
        }

        public void setQuestacceptcondition(QuestAction[] questacceptcondition) {
            questacceptconditionamount = questacceptcondition.length;
            this.questacceptcondition = questacceptcondition;
        }

        public int getQuestdescriptionline() {
            return questdescriptionline;
        }

        private void setQuestdescriptionline(int questdescriptionline) {
            this.questdescriptionline = questdescriptionline;
        }

        public boolean isTypeSet() {
            return questtype != null;
        }
        public boolean isAcceptconditionSet() {
            return questacceptcondition != null;
        }
        public boolean isTargetSet() {
            return questtarget != null;
        }
        public boolean isDescriptionSet() {
            return questdescription != null;
        }

        public boolean isPositionSet() {
            return questposition != null;
        }
    }


    public static class QuestPosition implements Serializable  {
        private int questlevel;
        private String parentquest;
        private String childquestother1;
        private String childquestother2;
        private String childquestother3;
        private String childquest;
        private String questpositionname;

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.writeObject(questlevel);
            out.writeObject(parentquest);
            out.writeObject(childquestother1);
            out.writeObject(childquestother2);
            out.writeObject(childquestother3);
            out.writeObject(childquest);
            out.writeObject(questpositionname);
        }
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
            questlevel = (int) in.readObject();
            parentquest = (String) in.readObject();
            childquestother1 = (String) in.readObject();
            childquestother2 = (String) in.readObject();
            childquestother3 = (String) in.readObject();
            childquest = (String) in.readObject();
            questpositionname = (String) in.readObject();
        }
        private void readObjectNoData() throws ObjectStreamException{
            new QuestPosition();
        }


        public Boolean readQpFromYml(String name){
            return readQpFromYml(quest_yml,name);
        }

        public Boolean readQpFromYml(YamlConfiguration ymlfile,String name){

            if(!ymlfile.contains(name)){
                say("[WARNING]读取QuestPosition数据错误，数据不存在");
                return false;
            }

            if(ymlfile.getInt(name+".tpye")!=2){
                say("[WARNING]读取QuestPosition数据错误，该名的类型不正确");
                return false;
            }
            parentquest = ymlfile.getString(name+".property-set.parentquest");
            childquest = ymlfile.getString(name+".property-set.childquest");
            childquestother1 = ymlfile.getString(name+".property-set.childquestother1");
            childquestother2 = ymlfile.getString(name+".property-set.childquestother2");
            childquestother3 = ymlfile.getString(name+".property-set.childquestother3");
            questlevel = ymlfile.getInt(name+".property-set.questlevel");
            return true;
        }

        public void saveQpToYml(String name){
            saveQpToYml(quest_yml,name);
        }

        public void saveQpToYml(YamlConfiguration ymlfile,String name){
            ymlfile.set(name+".type",2);
            ymlfile.set(name+".property-set.parentquest",parentquest);
            ymlfile.set(name+".property-set.childquest",childquest);
            ymlfile.set(name+".property-set.childquestother1",childquestother1);
            ymlfile.set(name+".property-set.childquestother2",childquestother2);
            ymlfile.set(name+".property-set.childquestother3",childquestother3);
            ymlfile.set(name+".property-set.questlevel",questlevel);

        }

        public String getQuestpositionname() {
            return questpositionname;
        }

        public void setQuestpositionname(String questpositionname) {
            this.questpositionname = questpositionname;
        }

        public int getQuestlevel(){
            return questlevel;
        }

        public String getParentquest(){
            return parentquest;
        }

        public void setParentquest(String parentquest) {
            this.parentquest = parentquest;
        }

        public String getChildquest(){
            return childquest;
        }

        public void setChildquest(String childquest) {
            this.childquest = childquest;
        }

        public void setQuestlevel(int questlevel) {
            this.questlevel = questlevel;
        }

        public String getChildquestother1() {
            return childquestother1;
        }

        public void setChildquestother1(String childquestother1) {
            this.childquestother1 = childquestother1;
        }

        public String getChildquestother2() {
            return childquestother2;
        }

        public void setChildquestother2(String childquestother2) {
            this.childquestother2 = childquestother2;
        }

        public String getChildquestother3() {
            return childquestother3;
        }

        public void setChildquestother3(String childquestother3) {
            this.childquestother3 = childquestother3;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public String toString() {
            return "QuestPosition{" +
                    "questlevel=" + questlevel +
                    ", parentquest='" + parentquest + '\'' +
                    ", childquestother1='" + childquestother1 + '\'' +
                    ", childquestother2='" + childquestother2 + '\'' +
                    ", childquestother3='" + childquestother3 + '\'' +
                    ", childquest='" + childquest + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof QuestPosition)) return false;
            QuestPosition that = (QuestPosition) o;
            return getQuestlevel() == that.getQuestlevel() && getParentquest().equals(that.getParentquest()) && Objects.equals(getChildquestother1(), that.getChildquestother1()) && Objects.equals(getChildquestother2(), that.getChildquestother2()) && Objects.equals(getChildquestother3(), that.getChildquestother3()) && Objects.equals(getChildquest(), that.getChildquest());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getQuestlevel(), getParentquest(), getChildquestother1(), getChildquestother2(), getChildquestother3(), getChildquest());
        }



        /*任务等级：（int）
            主线（1）
            主线的支线（2）
            主线的支线的支线（3）
            ....

        父任务：（New 任务）

        子任务集：(New 任务[]）
        */
    }


    public static class QuestAction implements Serializable  {

        private String questactionname;
        private QuestActionType questactiontype;
        private QuestActionData questactiondata;



        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.writeObject(questactionname);
            out.writeObject(questactiontype.getKey().getKey());
            out.writeObject(questactiondata);
        }
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
            questactionname = (String) in.readObject();
            questactiontype = QuestActionType.valueOf((String) in.readObject());
            questactiondata = (QuestActionData) in.readObject();

        }
        private void readObjectNoData() throws ObjectStreamException{
            new QuestAction();
        }
        public boolean readQaFromYml(String name){
            return readQaFromYml(quest_yml,name);
        }

        public boolean readQaFromYml(YamlConfiguration ymlfile,String name){
            if(!ymlfile.contains(name)){
                say("[WARNING]读取QuestAction数据错误，数据不存在");
                return false;
            }

            if(ymlfile.getInt(name+".tpye")!=3){
                say("[WARNING]读取QuestAction数据错误，该名的类型不正确");
                return false;
            }
            questactionname = name;
            questactiondata.readQaDataFromYml(ymlfile,ymlfile.getString(name+".property-inherit.questactiondata"));
            questactiontype = QuestActionType.valueOf(Objects.requireNonNull(ymlfile.getString(name + ".property-inherit.questactiontype")).toUpperCase());
            return true;
        }

        public void saveQaToYml(){
            saveQaToYml(quest_yml);
        }

        public void saveQaToYml(YamlConfiguration ymlfile){
            ymlfile.set(questactionname+".type",3);
            ymlfile.set(questactionname+".property-inherit.questactiontype",questactiontype.getKey().getKey());
            ymlfile.set(questactionname+".property-inherit.questactiondata",questactiondata.getQuestActndtname());
            questactiondata.saveQaDataToYml(ymlfile);
        }

        public String getQuestactionname() {
            return questactionname;
        }

        public void setQuestactionname(String questactionname) {
            this.questactionname = questactionname;
        }

        public QuestActionType getQuestactiontype() {
            return questactiontype;
        }

        public void setQuestactiondata(QuestActionData questactiondata) {
            this.questactiondata = questactiondata;
        }

        public void setQuestactiontype(QuestActionType questactiontype) {
            this.questactiontype = questactiontype;
        }

        public QuestActionData getQuestactiondata() {
            return questactiondata;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof QuestAction)) return false;
            QuestAction that = (QuestAction) o;
            return getQuestactiontype() == that.getQuestactiontype() && getQuestactiondata().equals(that.getQuestactiondata());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getQuestactiontype(), getQuestactiondata());
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override
        public String toString() {
            return "QuestAction{" +
                    "questactiontype=" + questactiontype +
                    ", questactiondata=" + questactiondata +
                    '}';
        }
    }


    public static class QuestActionData implements Serializable  {
        public double defaultdistance = -1;
        private String questActndtname;
        private ItemStack[] questtargetitem;
        private int questtimelimit = -1;
        private Map<EntityType, Integer> questtargetentity;
        //private Map<Block, Integer> questtargetblock;
        private UUID questtargetnpc;
        private int targetpositionx = 0,targetpositiony = 0,targetpositionz = 0;
        private Location targetlocation;

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.writeObject(defaultdistance);
            out.writeObject(questActndtname);
            out.writeObject(questtargetitem);
            out.writeObject(questtimelimit);
            out.writeObject(questtargetentity);
            out.writeObject(questtargetnpc);
            out.writeObject(targetpositionx);
            out.writeObject(targetpositiony);
            out.writeObject(targetpositionz);
            out.writeObject(targetlocation);
        }
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
            defaultdistance = (double) in.readObject();
            questActndtname = (String) in.readObject();
            questtargetitem = (ItemStack[]) in.readObject();
            questtimelimit = (int) in.readObject();
            questtargetentity = (Map<EntityType, Integer>) in.readObject();
            questtargetnpc = (UUID) in.readObject();
            targetpositionx = (int) in.readObject();
            targetpositiony = (int) in.readObject();
            targetpositionz = (int) in.readObject();
            targetlocation = (Location) in.readObject();
        }
        private void readObjectNoData() throws ObjectStreamException{
            new QuestActionData();
        }


        public double getDefaultdistance() {
            return defaultdistance;
        }

        public void setDefaultdistance(double defaultdistance) {
            this.defaultdistance = defaultdistance;
        }

        public Boolean readQaDataFromYml(String name){
            return readQaDataFromYml(quest_yml,name);
        }
        public Boolean readQaDataFromYml(YamlConfiguration ymlfile, String name){
            if(!ymlfile.contains(name)){
                say("[WARNING]读取QuestActionData数据错误，数据不存在");
                return false;
            }

            if(ymlfile.getInt(name+".tpye")!=4){
                say("[WARNING]读取QuestActionData数据错误，该名的类型不正确");
                return false;
            }

            questActndtname = name;

            int i = 0;
            if(ymlfile.contains(name+".property-inherit.questtargetitem")){
                while (ymlfile.contains(name + ".property-inherit.questtargetitem." + i))
                    questtargetitem[i++] = readItemStackFromYml(ymlfile, name + ".property-inherit.questtargetitem" + i);
            }

            if(ymlfile.contains(name+".property-set.defaultdistance")){
                defaultdistance = ymlfile.getInt(name+".property-set.defaultdistance");
            }

            if(ymlfile.contains(name+".property-set.questtimelimit"))
                questtimelimit = ymlfile.getInt(name+".property-set.questtimelimit");

            if(ymlfile.contains(name+".property-set.questtargetentity")) {
                while(ymlfile.contains(name+".property-set.questtargetentity."+i)) {
                    if(!ymlfile.contains(name + ".property-set.questtargetentity."+i+".amount")||!ymlfile.contains(name + ".property-set.questtargetentity."+i+".entitytype")){
                        say("[WARNING]读取QuestAction数据错误，数据非法："+this.toString());
                        say("[WARNING]读取QuestAction数据错误，数据非法："+i);
                        continue;
                    }
                    questtargetentity.put(EntityType.valueOf(Objects.requireNonNull(ymlfile.getString(name + ".property-set.questtargetentity."+i+".entitytype")).toUpperCase()),
                            ymlfile.getInt(name + ".property-set.questtargetentity."+i+".amount"));

                }
            }

            if(ymlfile.contains(name+".property-set.targetpositionx")) {
                if (ymlfile.contains(name + ".property-set.targetpositiony") && ymlfile.contains(name + ".property-set.targetpositionz")) {
                    targetpositionx = ymlfile.getInt(name + ".property-set.targetpositionx");
                    targetpositiony = ymlfile.getInt(name + ".property-set.targetpositiony");
                    targetpositionz = ymlfile.getInt(name + ".property-set.targetpositionz");
                    targetlocation.setX(targetpositionx);
                    targetlocation.setY(targetpositiony);
                    targetlocation.setZ(targetpositionz);

                } else {
                    say("[WARNING]QuestTargetPosition数据可能出现问题！");
                }
            }
            if(ymlfile.contains(name+".property-set.questtargetnpc")) {
                questtargetnpc= UUID.fromString(Objects.requireNonNull(ymlfile.getString(name + ".property-set.questtargetnpc")));
            }

            return true;
        }

        public void saveQaDataToYml(){
            saveQaDataToYml(quest_yml);
        }
        public void saveQaDataToYml(YamlConfiguration ymlfile){
            ymlfile.set(questActndtname+".type",4);

            if(questtargetitem != null) {
                int i = 0;
                for (ItemStack a : questtargetitem) {
                    saveItemStackToYml(ymlfile, questActndtname + ".property-inherit.questtargetitem." + i++, a);
                }
            }
            if(questtimelimit != -1)
                ymlfile.set(questActndtname+".property-set.questtimelimit",questtimelimit);

            if(defaultdistance != -1)
                ymlfile.set(defaultdistance+".property-set.defaultdistance",defaultdistance);

            if(questtargetentity != null){
                int i=0;
                for(EntityType key:questtargetentity.keySet()){
                    ymlfile.set(questActndtname+".property-set.questtargetentity."+ i +"entitytype",key.getKey().getKey());
                    ymlfile.set(questActndtname+".property-set.questtargetentity."+ i +"amount",questtargetentity.get(key));
                    ++i;
                }
            }
/*
            if(questtargetblock != null){
                int i=0;
                for(Block key:questtargetblock.keySet()){
                    ymlfile.set(questActndtname+".property-set.questtargetblock."+ i +"blockdata",key.getBlockData().get);
                    ymlfile.set(questActndtname+".property-set.questtargetblock."+ i +"amount",questtargetblock.get(key));
                    ++i;
                }
            }*/

            ymlfile.set(questActndtname+".property-set.targetpositionx",targetpositionx);
            ymlfile.set(questActndtname+".property-set.targetpositiony",targetpositiony);
            ymlfile.set(questActndtname+".property-set.targetpositionz",targetpositionz);
            ymlfile.set(questActndtname+".property-set.questtargetnpc",questtargetnpc.toString());
        }

        public void addCollectquesttargetitem(ItemStack a){
            ItemStack[] tempis = questtargetitem.clone();
            questtargetitem = new ItemStack[tempis.length+1];
            int i=0;
            for (ItemStack b:
                 tempis) {
                questtargetitem[i++]=b;
            }
            questtargetitem[i]= a;
        }

        public void removeCollectquesttargetitem(int index){
            ItemStack[] tempis = questtargetitem.clone();
            questtargetitem = new ItemStack[tempis.length-1];
            int i=0;
            for (ItemStack b:
                    tempis) {
                if(i>index) {
                    questtargetitem[i-1]=b;
                    ++i;
                } else questtargetitem[i++]=b;
            }
        }

        public void addQuesttargetentity(EntityType key,int value){
            questtargetentity.put(key,value);
        }

        public void removeQuesttargetentity(EntityType key){
            questtargetentity.remove(key);
        }
/*
        public void addQuesttargetblock(Block key,int value){
            questtargetblock.put(key,value);
        }

        public void removeQuesttargetblock(Block key){
            questtargetblock.remove(key);
        }*/

        public Location getTargetlocation() {
            return targetlocation;
        }

        public void setTargetlocation(Location targetlocation) {
            this.targetlocation = targetlocation;
        }

        public UUID getQuesttargetnpc() {
            return questtargetnpc;
        }

        public void setQuesttargetnpc(UUID questtargetnpc) {
            this.questtargetnpc = questtargetnpc;
        }

        public String getQuestActndtname() {
            return questActndtname;
        }

        public void setQuestActndtname(String questActndtname) {
            this.questActndtname = questActndtname;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof QuestActionData)) return false;
            QuestActionData that = (QuestActionData) o;
            return questtimelimit == that.questtimelimit && targetpositionx == that.targetpositionx && targetpositiony == that.targetpositiony && targetpositionz == that.targetpositionz && Objects.equals(getQuestActndtname(), that.getQuestActndtname()) && Arrays.equals(questtargetitem, that.questtargetitem) && questtargetentity.equals(that.questtargetentity);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(getQuestActndtname(), questtimelimit, questtargetentity, targetpositionx, targetpositiony, targetpositionz);
            result = 31 * result + Arrays.hashCode(questtargetitem);
            return result;
        }

        public void setTargetpositiony(int targetpositiony) {
            this.targetpositiony = targetpositiony;
        }

        @Override
        public String toString() {
            return "QuestActionData{" +
                    "defaultdistance=" + defaultdistance +
                    ", questActndtname='" + questActndtname + '\'' +
                    ", questtargetitem=" + Arrays.toString(questtargetitem) +
                    ", questtimelimit=" + questtimelimit +
                    ", questtargetentity=" + questtargetentity +
                    ", questtargetnpc=" + questtargetnpc +
                    ", targetpositionx=" + targetpositionx +
                    ", targetpositiony=" + targetpositiony +
                    ", targetpositionz=" + targetpositionz +
                    ", targetlocation=" + targetlocation +
                    '}';
        }

        public int getQuesttimelimit() {
            return questtimelimit;
        }

        public void setQuesttimelimit(int questtimelimit) {
            this.questtimelimit = questtimelimit;
        }

        public int getTargetpositionx() {
            return targetpositionx;
        }

        public void setTargetpositionx(int targetpositionx) {
            this.targetpositionx = targetpositionx;
        }

        public int getTargetpositiony() {
            return targetpositiony;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public int getTargetpositionz() {
            return targetpositionz;
        }

        public void setTargetpositionz(int targetpositionz) {
            this.targetpositionz = targetpositionz;
        }

        public ItemStack[] getQuesttargetitem() {
            return questtargetitem;
        }

        public void setQuesttargetitem(ItemStack[] questtargetitem) {
            this.questtargetitem = questtargetitem;
        }

        public Map<EntityType, Integer> getQuesttargetentity() {
            return questtargetentity;
        }

        public void setQuesttargetentity(Map<EntityType, Integer> questtargetentity) {
            this.questtargetentity = questtargetentity;
        }


    }


    public static class QuestReward implements Serializable  {
        private String questrewardname;
        private double rewardmoney = 0;
        private ItemStack[] rewarditems;
        private int rewarditemamount;
        private String[] rewardpermission;
        private int rewardpermissionamount;

        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.writeObject(questrewardname);
            out.writeObject(rewardmoney);
            out.writeObject(rewarditems);
            out.writeObject(rewarditemamount);
            out.writeObject(rewardpermission);
            out.writeObject(rewardpermissionamount);
        }
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
            questrewardname = (String) in.readObject();
            rewardmoney = (double) in.readObject();
            rewarditems = (ItemStack[]) in.readObject();
            rewarditemamount = (int) in.readObject();
            rewardpermission = (String[]) in.readObject();
            rewardpermissionamount = (int) in.readObject();
        }
        private void readObjectNoData() throws ObjectStreamException{
            new QuestReward();
        }

        public Boolean give(Player player){
            if(player.getInventory().getSize()-player.getInventory().getArmorContents().length<rewarditemamount){
                playerquest_yml.set(player.getName()+".rewarding",true);
                player.sendMessage(ChatColor.RED+"奖励发送失败，请尝试清空背包并使用指令/quest getreward重新获得奖励！");
                return false;
            }

            player.sendMessage(ChatColor.GREEN+"即将发送奖励~");
            for (ItemStack item:
                 rewarditems) {
                player.getInventory().addItem(item);
                player.updateInventory();
            }
            sneconomy.depositPlayer(player,rewarditemamount);
            for (String prm:
                    rewardpermission) {
                snperm.playerAdd(null,player,prm);

            }
            player.sendMessage(ChatColor.GREEN+"奖励发送完成~");
            return true;
        }


        public void addRewarditem(ItemStack a){
            rewarditemamount++;
            ItemStack[] tempis = rewarditems.clone();
            rewarditems = new ItemStack[tempis.length+1];
            int i=0;
            for (ItemStack b:
                    tempis) {
                rewarditems[i++]=b;
            }
            rewarditems[i]= a;
        }

        public void addRewardpermission(String a){
            rewardpermissionamount++;
            String[] tempstr = rewardpermission.clone();
            rewardpermission = new String[tempstr.length+1];
            int i=0;
            for (String b:
                    tempstr) {
                rewardpermission[i++]=b;
            }
            rewardpermission[i]= a;
        }


        public void removeRewarditem(int index){
            rewarditemamount--;
            ItemStack[] tempis = rewarditems.clone();
            rewarditems = new ItemStack[tempis.length-1];
            int i=0;
            for (ItemStack b:
                    tempis) {
                if(i>index) {
                    rewarditems[i-1]=b;
                    ++i;
                } else rewarditems[i++]=b;
            }
        }

        public void removeRewardpermission(int index){
            rewardpermissionamount--;
            String[] tempstr = rewardpermission.clone();
            rewardpermission = new String[tempstr.length-1];
            int i=0;
            for (String b:
                    tempstr) {
                if(i>index) {
                    rewardpermission[i-1]=b;
                    ++i;
                } else rewardpermission[i++]=b;
            }
        }

        public Boolean readQrFromYml(String name){
            return readQrFromYml(quest_yml,name);
        }

        public Boolean readQrFromYml(YamlConfiguration ymlfile,String name){

            questrewardname = name;

            if(ymlfile.contains(name+".property-set.rewarditemamount")) {
                rewarditemamount = ymlfile.getInt(name + ".property-set.rewarditemamount");
                for(int i=0 ;i<rewarditemamount ;i++){
                    rewarditems[i]= readItemStackFromYml(ymlfile,name+".property-set.rewarditem."+i);
                }
            }
            if(ymlfile.contains(name+".property-set.rewardmoney")) {
                rewardmoney = ymlfile.getInt(name + ".property-set.rewardmoney");
            }
            if(ymlfile.contains(name+".property-set.rewardpermissionamount")) {
                rewardpermissionamount = ymlfile.getInt(name + ".property-set.rewardpermissionamount");
                for(int i=0 ;i<rewardpermissionamount ;i++){
                    rewardpermission[i]=ymlfile.getString(name+".property-set.rewardpermissionamount."+i);
                }
            }
            return true;
        }

        public void saveQrToYml(){
            saveQrToYml(quest_yml);
        }

        public void saveQrToYml(YamlConfiguration ymlfile){

            if(rewardmoney != 0){
                ymlfile.set(questrewardname+".property-set.rewardmoney",rewardmoney);
            }
            if(rewarditemamount !=0){
                ymlfile.set(questrewardname+".property-set.rewarditemamount",rewarditemamount);
                for (int i = 0; i < rewarditemamount; i++) {
                    saveItemStackToYml(ymlfile,questrewardname+".property-set.rewarditem."+i,rewarditems[i]);
                }
            }
            if(rewardpermissionamount !=0){
                ymlfile.set(questrewardname+".property-set.rewardpermissionamount",rewardpermissionamount);
                for (int i = 0; i < rewardpermissionamount; i++) {
                    ymlfile.set(questrewardname+".property-set.rewarditemamount."+i,rewardpermission[i]);
                }
            }

        }

        public String getQuestrewardname() {
            return questrewardname;
        }

        public void setQuestrewardname(String questrewardname) {
            this.questrewardname = questrewardname;
        }

        public int getRewarditemamount() {
            return rewarditemamount;
        }

        public int getRewardpermissionamount() {
            return rewardpermissionamount;
        }

        private void setRewarditemamount(int rewarditemamount) {
            this.rewarditemamount = rewarditemamount;
        }

        private void setRewardpermissionamount(int rewardpermissionamount) {
            this.rewardpermissionamount = rewardpermissionamount;
        }

        public double getRewardmoney() {
            return rewardmoney;
        }

        public String[] getRewardpermission() {
            return rewardpermission;
        }

        public ItemStack[] getRewarditems() {
            return rewarditems;
        }

        public void setRewarditems(ItemStack[] rewarditems) {
            this.rewarditems = rewarditems;
        }

        public void setRewardmoney(double rewardmoney) {
            this.rewardmoney = rewardmoney;
        }

        public void setRewardpermission(String[] rewardpermission) {
            this.rewardpermission = rewardpermission;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof QuestReward)) return false;
            QuestReward that = (QuestReward) o;
            return getRewardmoney() == that.getRewardmoney() && getRewarditemamount() == that.getRewarditemamount() && getRewardpermissionamount() == that.getRewardpermissionamount() && Arrays.equals(getRewarditems(), that.getRewarditems()) && Arrays.equals(getRewardpermission(), that.getRewardpermission());
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(getRewardmoney(), getRewarditemamount(), getRewardpermissionamount());
            result = 31 * result + Arrays.hashCode(getRewarditems());
            result = 31 * result + Arrays.hashCode(getRewardpermission());
            return result;
        }

        @Override
        public String toString() {
            return "QuestReward{" +
                    "rewardmoney=" + rewardmoney +
                    ", rewarditems=" + Arrays.toString(rewarditems) +
                    ", rewarditemamount=" + rewarditemamount +
                    ", rewardpermission=" + Arrays.toString(rewardpermission) +
                    ", rewardpermissionamount=" + rewardpermissionamount +
                    '}';
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }



        QuestReward(){
            double rewardmoney = 0;
            int rewarditemamount = 0;
            int rewardpermissionamount = 0;

        }

        /*
            1、货币（int）
            2、物品奖励（ItemStack[])
            3、权限（Permission)*/
    }




}
