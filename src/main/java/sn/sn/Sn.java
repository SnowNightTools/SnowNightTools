package sn.sn;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import sn.sn.Basic.*;
import sn.sn.City.City;
import sn.sn.City.City_CE;
import sn.sn.Collector.Collector;
import sn.sn.Collector.CollectorRuntime;
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
import static sn.sn.Basic.Lag.getTPS;
import static sn.sn.Basic.Other.sendError;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class Sn extends JavaPlugin {

    public static Plugin sn = null;

    public static File share_file;
    public static File quest_file;
    public static File playerquest_file;
    public static File config_file;
    public static File collector_file;
    public static File bin_file;
    public static File city_file;
    public static File data_folder;
    public static YamlConfiguration share_yml;
    public static YamlConfiguration bin_yml;
    public static YamlConfiguration city_yml;
    public static YamlConfiguration config_yml;
    public static YamlConfiguration quest_yml;
    public static YamlConfiguration playerquest_yml;
    public static YamlConfiguration collector_yml;
    public static String share_path, quest_path, playerquest_path, plugin_path;

    public static Map<Player, Location> start_point = new HashMap<>();
    public static Map<Player, Location> end_point = new HashMap<>();
    public static Map<Player, City> city_in = new HashMap<>();
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
    public static Map<Player, LocSet> loc_setting = new HashMap<>();
    public static Map<Player, Double> double_setting = new HashMap<>();
    public static Map<Player, Integer> int_setting = new HashMap<>();
    public static Map<Player, String> string_setting = new HashMap<>();
    public static Map<Player, List<String>> list_str_setting = new HashMap<>();

    public static boolean eco_system_set = false;
    public static boolean eco_use_vault = true;
    public static boolean debug = true;

    public static Permission sn_perm;
    public static Economy sn_economy;

    public static List<ItemStack> rubbishes;
    public static List<Quest> quests = new ArrayList<>();
    public static Map<String, City> cities = new HashMap<>();
    public static List<String> bins;
    public static List<String> collector_names = new ArrayList<>();
    public static List<String> city_names = new ArrayList<>();

    public static double tps;

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
            Other.sendInfo("雪夜插件已卸载~");
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


        config_file = new File(data_folder.getAbsolutePath()+ File.separator + "config.yml");
        Other.sendInfo(data_folder.getAbsolutePath()+ File.separator +"config.yml");

        int brkcnt1 = 1 ;
        while (true) {
            saveDefaultConfig();

            Other.sendDebug("尝试寻找config 第"+brkcnt1+"次，会尝试5次。");
            config_file = new File(data_folder.getAbsolutePath() + File.separator + "config.yml");
            brkcnt1 ++;
            if(brkcnt1 >= 5) {
                sendError("config为空文件，请配置雪夜插件的config文件！");
                break;
            }
            config_yml = YamlConfiguration.loadConfiguration(config_file);
            if(config_yml.contains("debug"))break;
        }

        boolean sharepathed = config_yml.getBoolean("share-path-ed",false);
        debug = config_yml.getBoolean("debug",false);
        eco_use_vault = config_yml.getBoolean("vault",false);


        share_path = config_yml.getString("share-path");
        quest_path = config_yml.getString("quest-path");
        playerquest_path = config_yml.getString("playerquest-path");

        Other.sendInfo("share_path=" + share_path);
        Other.sendInfo("quest_path=" + quest_path);
        Other.sendInfo("playerquest_path=" + playerquest_path);


        try {

            collector_file = new File(data_folder,"collector.yml");
            //noinspection ResultOfMethodCallIgnored
            collector_file.createNewFile();

            city_file = new File(data_folder,"city.yml");
            //noinspection ResultOfMethodCallIgnored
            city_file.createNewFile();

            bin_file = new File(data_folder,"bins.yml");
            //noinspection ResultOfMethodCallIgnored
            bin_file.createNewFile();

            quest_file = SnFileIO.checkFile(quest_path + "quest.yml");
            playerquest_file = SnFileIO.checkFile(playerquest_path + "playerquest.yml");
            share_file = SnFileIO.checkFile(share_path + "share.yml");

        } catch (IOException e) {
            sendError(e.getLocalizedMessage());
        }

        city_yml = YamlConfiguration.loadConfiguration(city_file);
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

        //load cities
        loadCities();

        if(eco_use_vault)
            if(!Other.initVault()) Other.sendInfo("vault插件挂钩失败，请检查vault插件和经济、权限插件！");
            else Other.sendInfo("vault插件已被SnTools加载。");
        else eco_system_set = true;

        //Run the main threads
        new QuestRuntime().runTaskTimerAsynchronously(this,0L,200L);

        CollectorRuntime.runCollector(false);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new AutoSave()::start,0,72000);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this,()-> getTPS(),0,20);

        Bukkit.getPluginManager().registerEvents(new LoginEvent(), this);
        Bukkit.getPluginManager().registerEvents(new RangeSelector(), this);
        Bukkit.getPluginManager().registerEvents(new InvOperateEvent(), this);

        Objects.requireNonNull(getCommand("express")).setExecutor(new Express_CE());
        Objects.requireNonNull(getCommand("collector")).setExecutor(new Collector_CE());
        Objects.requireNonNull(getCommand("city")).setExecutor(new City_CE());
        //Objects.requireNonNull(getCommand("npc")).setExecutor(new sn.sn.npc());
        Objects.requireNonNull(getCommand("quest")).setExecutor(new Quest_CE());

        Other.sendInfo("雪夜插件已加载~");
    }

    private void loadCities() {
        cities = new HashMap<>();
        city_names = new ArrayList<>();
        int n = city_yml.getInt("amount",0);
        for (int i = 0; i < n; i++) {
            String s = city_yml.getString(i +".name");
            city_names.add(s);
            City city = null;
            try {
                city = SnFileIO.readCityFromYml(city_yml,String.valueOf(i));
            } catch (IllegalArgumentException e) {
                sendError(e.getLocalizedMessage());
                sendError("City文件读取出现错误，可能是文件被破坏！");
            }
            cities.put(s,city);
        }
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
                    Other.sendInfo("警告！ 在加载"+questname+"时出现错误！");
                }
            }
        }
    }


}
