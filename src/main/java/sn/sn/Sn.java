package sn.sn;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import sn.sn.Basic.AutoSave;
import sn.sn.Basic.LoginEvent;
import sn.sn.Basic.SayToEveryoneThread;
import sn.sn.Basic.SnFileIO;
import sn.sn.City.City;
import sn.sn.City.City_CE;
import sn.sn.Collector.Collector;
import sn.sn.Collector.CollectorRuntime;
import sn.sn.Collector.CollectorThrowThread;
import sn.sn.Collector.Collector_CE;
import sn.sn.Express.Express_CE;
import sn.sn.Quest.*;
import sn.sn.Range.RangeSelector;
import sn.sn.UI.InvOperateEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class Sn extends JavaPlugin {

    public static Plugin sn = null;

    public static File share_file;
    public static File quest_file;
    public static File playerquest_file;
    public static File config_file;
    public static File collector_file;
    public static File bin_file;
    public static File data_folder;
    public static YamlConfiguration share_yml;
    public static YamlConfiguration bin_yml;
    public static YamlConfiguration config_yml;
    public static YamlConfiguration quest_yml;
    public static YamlConfiguration playerquest_yml;
    public static YamlConfiguration collector_yml;

    public static Map<Player, Location> start_point = new HashMap<>();
    public static Map<Player, Location> end_point = new HashMap<>();
    public static Map<OfflinePlayer, List<Collector>> collectors = new HashMap<>();
    public static Map<Player, QuestAction> quest_action_setting = new HashMap<>();
    public static Map<Player, Inventory> show_inv = new HashMap<>();
    public static Map<Player, List<ItemStack>> item_temp = new HashMap<>();
    public static Map<Player, Quest> quest_setting = new HashMap<>();
    public static Map<Player, Consumer<Object>> setting = new HashMap<>();
    public static Map<Player, Consumer<ArrayList<Object>>> setting_list = new HashMap<>();
    public static Map<Player, QuestSettingType> setting_state = new HashMap<>();
    public static Map<Player, Consumer<Player>> ui_opener = new HashMap<>();
    public static Map<Player, EntityType> entity_type_setting = new HashMap<>();
    public static Map<Player, Boolean> isSetTorC = new HashMap<>();//true when commander is setting Target
    public static Map<OfflinePlayer, Boolean> city_joined = new HashMap<>();//true when commander has joint a city.
    public static Map<Player, InvOperateEvent.LocSet> loc_setting = new HashMap<>();
    public static Map<Player, Double> double_setting = new HashMap<>();
    public static Map<Player, Integer> int_setting = new HashMap<>();
    public static Map<Player, String> string_setting = new HashMap<>();
    public static Map<Player, List<String>> list_str_setting = new HashMap<>();

    public static boolean eco_system_set = false;
    public static boolean eco_use_vault = true;
    public static boolean debug = true;

    public static String plugin_path;
    public static String share_path;

    public static Permission sn_perm;
    public static Economy sn_economy;

    public static List<ItemStack> rubbishes;
    public static List<Quest> quests = new ArrayList<>();
    public static Map<String, City> cities = new HashMap<>();

    public static List<String> bins;
    public static List<String> collector_names = new ArrayList<>();
    public static List<String> city_names = new ArrayList<>();


    /** 给Console发送信息
     * send message to console
     * @param mes 要发送的信息(the message to send)
     */
    public static void sendInfo(String mes){
        CommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage("[sn][INFO]"+ mes);
    }

    /** 给Console发送debug信息
     * send debug message to console when debug is true.
     * @param mes 要发送的信息(the message to send)
     */
    public static void sendDebug(String mes){
        if(debug){
            CommandSender sender = Bukkit.getConsoleSender();
            sender.sendMessage(ChatColor.BLUE+"[sn][DEBUG]"+ mes);
        }
    }

    /** 给Console发送Warn信息
     * send Warn message to console.
     * @param mes 要发送的信息(the message to send)
     */
    public static void sendWarn(String mes){
        CommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage(ChatColor.YELLOW+"[sn][WARN]"+ mes);
    }

    /** 给Console发送Error信息
     * send Error message to console.
     * @param mes 要发送的信息(the message to send)
     */
    public static void sendError(String mes){
        CommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage(ChatColor.RED+"[sn][Error]"+ mes);
    }


    public static List<String> toStrList(Map<String,Object> map){
        List<String> list = new ArrayList<>();
        for (String s : map.keySet()) {
            if(map.get(s)==null)continue;
            list.add(s+"->"+map.get(s).toString());
        }
        return list;
    }
    public String share_Path,quest_Path,playerquest_Path;


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean initVault(){
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        RegisteredServiceProvider<Permission> perProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);

        if(economyProvider != null && perProvider != null){
            sn_economy = economyProvider.getProvider();
            sn_perm = perProvider.getProvider();
            eco_system_set = true;
            return true;
        } else return false;
    }



        /*
        1.玩家任务执行信息（存储在playerquest.yml）每个玩家都有自己的任务信息，它们形如：
        Player:
            nowquest: name(一个quest类的名字)
            questenableamount: amount（能够执行的命令数）
            questenable:（能够执行的命令）
                quest1: name1
                quest2: name2
                ……
            questdone:(完成过的任务 完成时间 用时)
                quest1: name1 time1 usedtime1
                quest2: name2 time2 usedtime2
                ……

        2.类和量信息（存储在quest.yml中）每个被create创建的量都有自己的信息。
        Amount: n  下面有n个任务的信息
        inside: 任务的名字列表
            0:Name1
            Name1:0
            1:Name2
            Name2:1
            ……
        Name1：
            type：1
            property-inherit：(继承的属性)
                questtype: name1
                questposition: firstquest
                ……
            property-set:(直接设置的属性)
                propertyname1: value1
                propertyname2: value2
                ……
        Name2：
            type：1
            property-inherit：(继承的属性)
                questtype: name1
                QuestPosition: name2
                ……
            property-set:(直接设置的属性)
                propertyname1: value1
                propertyname2: value2
                ……
        firstquest：
            type：2
            property-inherit：(继承的属性)
                parentquest: name1
                propertyname2: name2

                questacceptcondition:
                    1: Questacceptconditionname1
                    2: Questacceptconditionname2
                ……
            property-set:(直接设置的属性)
                propertyname1: value1
                propertyname2: value2
                questacceptconditionamount:
                ……


                */

    @Override
    public void onDisable() {

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.closeInventory();
        }

        AutoSave a = new AutoSave();
        a.start();
        try {
            a.join();
        } catch (InterruptedException ignored) {
        } finally {
            sendInfo("雪夜插件已卸载~");
        }
    }

    public static void runCollector(boolean once) {
        Runnable clean = () -> {
            sendInfo("开始收集物品！");
            rubbishes = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    CollectorRuntime cr = new CollectorRuntime(entity);
                    Bukkit.getScheduler().runTask(sn,cr);
                }
            }
            new CollectorThrowThread().start();
            sendInfo("物品收集结束！");
        };
        if(once){
            Bukkit.getScheduler().runTask(sn,()-> new SayToEveryoneThread("扫地即将开始！").start());
            Bukkit.getScheduler().runTaskLater(sn, clean,200);
        } else {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(sn,()-> new SayToEveryoneThread("扫地即将开始！").start(),0,36000);
            Bukkit.getScheduler().scheduleSyncRepeatingTask(sn, clean,200,36000);
        }
    }

    public static void loadCollectors() {
        collectors = new HashMap<>();
        int n = collector_yml.getInt("amount",0);
        String name,uuid;
        int cn;
        for (int i = 0; i < n; i++) {
            name = collector_yml.getString("list."+i);
            collector_names.add(name);
            uuid = collector_yml.getString(name+".owner","d59beeb3-6c24-3f22-8888-f8c83bc38cfa");
            cn = collector_yml.getInt(name+".range_amount",1);
            Collector temp = new Collector();
            temp.setName(name);
            temp.setOwner(UUID.fromString(uuid));
            temp.setBox(SnFileIO.readLocationFromYml(collector_yml,name+".box"));
            for (int i1 = 1; i1 <= cn; i1++) {
                temp.addRange(SnFileIO.readRangeFromYml(collector_yml,name+".range."+i1));
            }
            List<Collector> t = collectors.getOrDefault(Bukkit.getOfflinePlayer(UUID.fromString(uuid)), new ArrayList<>());
            t.add(temp);
            collectors.put(Bukkit.getOfflinePlayer(UUID.fromString(uuid)),t);
        }
    }

    @Override
    public void onEnable() {

        sn = this;
        data_folder = getDataFolder();
        plugin_path = getDataFolder().getPath();

        registerClass(Quest.class);
        registerClass(QuestPosition.class);
        registerClass(QuestAction.class);
        registerClass(QuestReward.class);
        registerClass(QuestActionData.class);


        config_file = new File(data_folder.getAbsolutePath()+ "\\config.yml");
        sendInfo(data_folder.getAbsolutePath()+ "\\config.yml");

        int brkcnt1 = 1 ;
        while (true) {
            sendDebug("尝试寻找config 第"+brkcnt1+"次，会尝试5次。");

            config_file = new File(data_folder.getAbsolutePath()+ "\\config.yml");
            brkcnt1 ++;
            if(brkcnt1 >= 5) {
                sendError("config配置错误");
                break;
            }
            config_yml = YamlConfiguration.loadConfiguration(config_file);
            if(config_yml.contains("share-path"))break;
            getConfig().options().copyDefaults();
            saveDefaultConfig();
        }
        saveConfig();

        boolean sharepathed = config_yml.getBoolean("share-path-ed",false);
        debug = config_yml.getBoolean("debug",false);
        eco_use_vault = config_yml.getBoolean("vault",false);

        if(sharepathed){
            //如果已经设置过地址，默认文件已经创造。
            share_Path = config_yml.getString("share-path");
            try {
                Sn.share_file = new File(share_Path + "share.yml");
                Sn.share_yml = YamlConfiguration.loadConfiguration(Sn.share_file);
            } catch (NullPointerException e){
                sendInfo("share.yml读取失败！ 请重新设置或者手动创建文件!");
            }
        } else {
            sendInfo("share.yml文件加载失败，请使用/express setpath [sharePath]为它添加地址，并使用/reload重载插件！");
        }


        quest_Path = config_yml.getString("quest-path");
        playerquest_Path = config_yml.getString("playerquest-path");

        sendInfo("quest_path=" + quest_Path);
        sendInfo("playerquest_Path=" + playerquest_Path);


        try {
            collector_file = new File(data_folder,"collector.yml");
            //noinspection ResultOfMethodCallIgnored
            collector_file.createNewFile();
            bin_file = new File(data_folder,"bins.yml");
            //noinspection ResultOfMethodCallIgnored
            bin_file.createNewFile();
            quest_file = checkFile(quest_Path + "quest.yml");
            playerquest_file = checkFile(playerquest_Path + "playerquest.yml");
        } catch (IOException e) {
            sendError(e.getLocalizedMessage());
        }

        bin_yml = YamlConfiguration.loadConfiguration(bin_file);
        collector_yml = YamlConfiguration.loadConfiguration(collector_file);
        quest_yml = YamlConfiguration.loadConfiguration(quest_file);
        playerquest_yml = YamlConfiguration.loadConfiguration(playerquest_file);

        //plugin_yml = YamlConfiguration.loadConfiguration(plugin_file);

        // load quests
        loadQuests();

        // load collectors
        loadCollectors();
        loadBin();

        if(eco_use_vault)
            if(!initVault()) sendInfo("vault插件挂钩失败，请检查vault插件。");
            else sendInfo("vault插件已被SnTools加载。");
        else eco_system_set = true;



        BukkitRunnable nt = new QuestRuntime();
        nt.runTaskTimerAsynchronously(this,0L,200L);

        runCollector(false);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this,()->{
            AutoSave as = new AutoSave();
            as.start();
        },0,72000);

        Bukkit.getPluginManager().registerEvents(new LoginEvent(), this);
        Bukkit.getPluginManager().registerEvents(new RangeSelector(), this);
        Bukkit.getPluginManager().registerEvents(new InvOperateEvent(), this);

        Objects.requireNonNull(getCommand("express")).setExecutor(new Express_CE());
        Objects.requireNonNull(getCommand("collector")).setExecutor(new Collector_CE());
        Objects.requireNonNull(getCommand("city")).setExecutor(new City_CE());
        //Objects.requireNonNull(getCommand("npc")).setExecutor(new sn.sn.npc());
        Objects.requireNonNull(getCommand("quest")).setExecutor(new Quest_CE());

        sendInfo("雪夜插件已加载~");
    }

    private void loadBin() {
        bins = new ArrayList<>();
        int n = bin_yml.getInt("amount",0);
        for (int i = 0; i < n; i++) {
            String s = bin_yml.getString(String.valueOf(i));
            bins.add(s);
        }
    }

    private void loadQuests() {
        quests = new ArrayList<>();
        int n = quest_yml.getInt("Amount");
        for(int i = 0; i < n; i++){
            String questname;
            questname = quest_yml.getString("inside."+ i );
            if (quest_yml.getInt(questname +".type")== 1){
                quests.add(new Quest(questname));
                if(!quests.get(i).readQuestFromYml()){
                    sendInfo("警告！ 在加载"+questname+"时出现错误！");
                }
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File checkFile(String path) throws IOException {
        File file = new File(path);
        while (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public static class EnchantPair {
        Enchantment a;
        int b;

        public EnchantPair(String data){
            int index = data.indexOf(' ');
            a = Enchantment.getByKey(NamespacedKey.minecraft(data.substring(0,index)));
            b = Integer.parseInt(data.substring(index+1));
        }

        public Enchantment getA() {
            return a;
        }

        public int getB() {
            return b;
        }
    }


}
