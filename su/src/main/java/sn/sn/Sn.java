package sn.sn;

import com.google.common.collect.Multimap;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Sn extends JavaPlugin {

    public static File plugin_file;
    public static File share_file;
    public static File quest_file;
    public static File playerquest_file;
    public static File config_file;

    /** 给Console发送信息
     * send message to console
     * @param a 要发送的信息(the message to send)
     */
    public static void say(String a){
        CommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage(a);
    }

    public static String plugin_Path;

    {
        plugin_Path = getDataFolder().getPath();
    }
    public static YamlConfiguration share_yml,plugin_yml,config_yml,quest_yml,playerquest_yml;
    public static quest.Quest[] quests;
    public static Map<Player, Inventory> showInv = new HashMap<>();
    public static Map<Player, quest.Quest> questseting = new HashMap<>();
    //public static Map<Player, Map<ItemStack, Map<String, String>>> spitemtags = new HashMap<>();
    public static int questamount = 0;
    public static Permission snperm = null;
    public static Economy sneconomy = null;
    public String share_Path,quest_Path,playerquest_Path;


    /** read Block from ymlfile
     * @param ymlfile where to read
     * @param path the saving path in ymlfile
     * @param location the location of block which to be saved
     * @return the Block read result.
     */
    public static Block readBlockFromYml(YamlConfiguration ymlfile, String path , Location location){
        return readBlockFromYml(ymlfile,path, Objects.requireNonNull(location.getWorld()),location);
    }

    /**read Block from ymlfile
     * @param ymlfile where to read
     * @param path the saving path in ymlfile
     * @param world the world the block reading to
     * @return the Block read result
     */
    public static Block readBlockFromYml(YamlConfiguration ymlfile, String path , World world){
        Location templocation = new Location(world,ymlfile.getDouble(path + ".x"),ymlfile.getDouble(path + ".y"),ymlfile.getDouble(path + ".z"));
        return readBlockFromYml(ymlfile,path,world,templocation);
    }

    /**read Block from ymlfile
     * @param ymlfile where to save
     * @param path the saving path in ymlfile
     * @return the Block read result
     */
    public static Block readBlockFromYml(YamlConfiguration ymlfile, String path){
        UUID tmpuuid = UUID.fromString(Objects.requireNonNull(ymlfile.getString(path + ".defaultblocksaveworld")));
        return readBlockFromYml(ymlfile,path, Objects.requireNonNull(Bukkit.getWorld(tmpuuid)));
    }

    /**read Block from ymlfile,the ymlfile defult in quest_yml
     * @param path the saving path in ymlfile
     * @param world the world the block reading to
     * @param location the location the block save in
     * @return Block read result
     */
    public static Block readBlockFromYml(String path, World world, Location location){
        return readBlockFromYml(quest_yml,path,world,location);
    }

    /**read Block from ymlfile,the ymlfile defult in quest_yml
     * @param path the saving path in ymlfile
     * @param location the location the block save in
     * @return Block read result
     */
    public static Block readBlockFromYml(String path , Location location){
        return readBlockFromYml(quest_yml,path,location);
    }

    /**read Block from ymlfile,the ymlfile defult in quest_yml
     * @param path the saving path in ymlfile
     * @param world the world the block reading to
     * @return Block read result
     */
    public static Block readBlockFromYml(String path , World world){
        return readBlockFromYml(quest_yml,path,world);
    }

    /**read Block from ymlfile,the ymlfile defult in quest_yml
     * @param path the saving path in ymlfile
     * @return Block read result
     */
    public static Block readBlockFromYml(String path){
        return readBlockFromYml(quest_yml,path);
    }

    /**read Block from ymlfile,with the specific world to save the block modle
     * and get the block in the location of the world, the ymlfile save the block
     * data, but the modle block method is much better.
     * @param ymlfile where to read
     * @param path the saving path in ymlfile
     * @param world the world the block reading to
     * @param location the location the block save in
     * @return Block read result
     */
    public static Block readBlockFromYml(YamlConfiguration ymlfile, String path, World world, Location location){
        Block block = world.getBlockAt(location);
        if(ymlfile.contains(path + ".biome"))
            block.setBiome(Biome.valueOf(Objects.requireNonNull(ymlfile.getString(path + ".biome")).toUpperCase()));
        return block;
    }

    /**save the location data to the ymlfile, without saving any other data.
     * @param ymlfile ymlfile to save
     * @param path the path in ymlfile
     * @param blocktosave the block to save
     */
    public static void saveBlockToYml(YamlConfiguration ymlfile,String path,Block blocktosave){
        ymlfile.set(path+".defaultblocksaveworld",blocktosave.getWorld().getUID().toString());
        ymlfile.set(path+".x",blocktosave.getX());
        ymlfile.set(path+".y",blocktosave.getY());
        ymlfile.set(path+".z",blocktosave.getZ());
    }

    /** set the block with biome config
     * @param ymlfile ymlfile to save
     * @param path path in ymlfile
     * @param blocktosave block to save
     * @param biome the block biome
     */
    public static void saveBlockToYml(YamlConfiguration ymlfile,String path,Block blocktosave,Biome biome){

        ymlfile.set(path+".biome",biome);
        saveBlockToYml(ymlfile,path,blocktosave);
    }

    /**save block to ymlfile
     * @param ymlfile ymlfile to save
     * @param path path in ymlfile
     * @param blocktosave block to save
     * @param isSetBiome true to set the biome ,false to not set
     */
    public static void saveBlockToYml(YamlConfiguration ymlfile,String path,Block blocktosave,boolean isSetBiome){
        if(isSetBiome) ymlfile.set(path+".biome",blocktosave.getBiome().getKey().getKey());
        saveBlockToYml(ymlfile,path,blocktosave);
    }

    /**save block to ymlfile , with defult ymlfile quest_yml
     * @param path path in ymlfile
     * @param blocktosave block to save
     */
    public static void saveBlockToYml(String path,Block blocktosave){
        saveBlockToYml(quest_yml,path,blocktosave);
    }

    /**save block to ymlfile , with defult ymlfile quest_yml
     * @param path path in ymlfile
     * @param blocktosave block to save
     * @param biome the specific biome to set
     */
    public static void saveBlockToYml(String path,Block blocktosave,Biome biome){
        saveBlockToYml(quest_yml,path,blocktosave,biome);
    }

    /**save block to ymlfile , with defult ymlfile quest_yml
     * @param path path in ymlfile
     * @param blocktosave block to save
     * @param isSetBiome true to set the biome of now
     */
    public static void saveBlockToYml(String path,Block blocktosave,boolean isSetBiome){
        saveBlockToYml(quest_yml,path,blocktosave,isSetBiome);
    }

    /**
     * @param ymlfile:where the ItemStack saved.
     * @param path:the path in yamlfile where the ItemStack is.
     * @return ItemStack which has been read.
     * @exception ClassCastException: when data were broken(May not clean the data before reset it)
     */
    public static ItemStack readItemStackFromYml(YamlConfiguration ymlfile, String path){
        String tmpmtrilname = ymlfile.getString(path+".type");
        assert tmpmtrilname != null;
        ItemStack itemwhckrd = new ItemStack(Objects.requireNonNull(Material.getMaterial(tmpmtrilname)));

        int tmpmnt = ymlfile.getInt(path+".amount");
        itemwhckrd.setAmount(tmpmnt);


        ItemMeta tmpmeta = itemwhckrd.getItemMeta();
        if(ymlfile.contains(path+".meta.displayname")) {
            assert tmpmeta != null;
            tmpmeta.setDisplayName(ymlfile.getString(path+".meta.displayname"));
        }
        if(ymlfile.contains(path+".meta.lore")) {
            assert tmpmeta != null;
            tmpmeta.setLore(ymlfile.getStringList(path+".meta.lore"));
        }
        if(ymlfile.contains(path+".meta.unbreakable")) {
            assert tmpmeta != null;
            tmpmeta.setUnbreakable(ymlfile.getBoolean(path+".meta.unbreakable"));
        }
        if(ymlfile.contains(path+".meta.localizedname")) {
            assert tmpmeta != null;
            tmpmeta.setLocalizedName(ymlfile.getString(path+".meta.localizedname"));
        }
        if(ymlfile.contains(path+".meta.data")) {
            assert tmpmeta != null;
            tmpmeta.setCustomModelData(ymlfile.getInt(path+".meta.data"));
        }
        if(ymlfile.contains(path+".meta.attribute")) {

            for (Attribute i :Attribute.values())
                readAttFromYml(ymlfile, path, tmpmeta, i.getKey().getKey());

        }

        if(ymlfile.contains(path+".meta.enchant")) {
            int i=0;
            while (ymlfile.contains(path+".meta.enchant."+i)){
                EnchantPair tempenp = new EnchantPair(Objects.requireNonNull(ymlfile.getString(path + ".meta.enchant." + i)));
                assert tmpmeta != null;
                tmpmeta.addEnchant(tempenp.a,tempenp.b,true);
                ++i;
            }
        }

        List<String> tmpp = new ArrayList<>();
        if(ymlfile.contains(path+".tag"))
        tmpp = (List<String>) ymlfile.getStringList(path + ".tag");


        /*该部分内容过时，故注释
        if(ymlfile.contains(path+".enchantment")) {
            int i=0;
            while (ymlfile.contains(path+".enchantment."+i)){
                EnchantPair tempenp = new EnchantPair(Objects.requireNonNull(ymlfile.getString(path + ".enchantment." + i)));
                assert tmpmeta != null;
                itemwhckrd.addEnchantment(tempenp.a,tempenp.b);
                ++i;
            }
        }*/

        if(ymlfile.contains(path+".meta.itemflags")) {
            int i=0;
            while (ymlfile.contains(path+".meta.itemflags."+i)){
                assert tmpmeta != null;
                tmpmeta.addItemFlags(ItemFlag.valueOf(path+".meta.itemflags."+i));
                ++i;
            }
        }


        if(ymlfile.contains(path + ".enchantmentstorage")) {
            EnchantmentStorageMeta tmpesm = (EnchantmentStorageMeta) tmpmeta;
            List<Map<?, ?>> tmplis = new ArrayList<>();
            tmplis = ymlfile.getMapList(path+ ".enchantmentstorage");
            for(Map<?, ?> tmpmap:tmplis){
                String enname = tmpmap.keySet().toArray()[0].toString();
                int enlevel = (int) tmpmap.get(tmpmap.keySet().toArray()[0]);
                assert tmpesm != null;
                tmpesm.addStoredEnchant(Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.fromString(enname))),enlevel,true);
            }
            itemwhckrd.setItemMeta(tmpesm);
            return itemwhckrd;
        }


        if(ymlfile.contains(path + ".shulker_box")) {
            BlockStateMeta tmpbsm = (BlockStateMeta) tmpmeta;
            assert tmpbsm != null;
            ShulkerBox tmpblock = (ShulkerBox) tmpbsm.getBlockState();
            int cntitem = ymlfile.getInt(path + ".shulker_box.cntitem");
            Inventory tmpinv = tmpblock.getInventory();
            for (int i = 0; i < cntitem; i++) {
                tmpinv.setItem(ymlfile.getInt(path + ".shulker_box."+i+".index"), readItemStackFromYml(ymlfile,path + ".shulker_box."+i));
            }
            tmpbsm.setBlockState(tmpblock);
            itemwhckrd.setItemMeta(tmpbsm);
            return itemwhckrd;
        }


        if(ymlfile.contains(path + ".knowledgebook")) {
            List<String> tmpens = ymlfile.getStringList(path + ".knowledgebook");
            KnowledgeBookMeta tmpkbm = (KnowledgeBookMeta) tmpmeta;
            assert tmpkbm != null;
            for(String keyname:tmpens){
                tmpkbm.addRecipe(NamespacedKey.fromString(keyname));
            }
            itemwhckrd.setItemMeta(tmpkbm);
            return itemwhckrd;
        }

        //美西螈桶
        if(ymlfile.contains(path + ".axolotlbucket")) {
            AxolotlBucketMeta tmpabm = (AxolotlBucketMeta) tmpmeta;
            assert tmpabm != null;
            tmpabm.setVariant(Axolotl.Variant.valueOf(ymlfile.getString(path + ".axolotlbucket")));
            itemwhckrd.setItemMeta(tmpabm);
            return itemwhckrd;
        }

        //旗帜
        if(ymlfile.contains(path + ".banner")) {
            BannerMeta tmpbm = (BannerMeta) tmpmeta;
            assert tmpbm != null;
            List<Map<?, ?>> bannerlis = ymlfile.getMapList(path + ".banner");
            for(Map<?, ?> tmppa : bannerlis){
                tmpbm.addPattern(new Pattern(DyeColor.valueOf((String) tmppa.get("color")), Objects.requireNonNull(PatternType.getByIdentifier((String) tmppa.get("id")))));
            }
            itemwhckrd.setItemMeta(tmpbm);
            return itemwhckrd;
        }

        /*含有方块信息的 ,产生不可预知的错误 所以注释
        if(ymlfile.contains(path + ".blockdata")) {
            BlockDataMeta tmpbdm = (BlockDataMeta) tmpmeta;
            assert tmpbdm != null;
            tmpbdm.setBlockData(Bukkit.createBlockData(Objects.requireNonNull(ymlfile.getString(path + ".blockdata"))));
            itemwhckrd.setItemMeta(tmpbdm);
            return itemwhckrd;
        }*/

        //书
        if(ymlfile.contains(path + ".book")) {
            BookMeta tmpbm = (BookMeta) tmpmeta;
            assert tmpbm != null;
            List<String> tmplis = tmpbm.getPages();
            if(ymlfile.contains(path + ".book.author"))
                tmpbm.setAuthor(ymlfile.getString(path + ".book.author"));
            if(ymlfile.contains(path + ".book.title"))
                tmpbm.setTitle(ymlfile.getString(path + ".book.title"));
            if(ymlfile.contains(path + ".book.generation"))
                tmpbm.setGeneration(BookMeta.Generation.valueOf(ymlfile.getString(path + ".book.generation")));
            if(ymlfile.contains(path + ".book.pages"))
                tmpbm.setPages(ymlfile.getStringList(path + ".book.pages"));
            itemwhckrd.setItemMeta(tmpbm);
            return itemwhckrd;
        }

        //捆？含有物品的
        if(ymlfile.contains(path + ".bundle")) {
            BundleMeta tmpbm = (BundleMeta) tmpmeta;
            assert tmpbm != null;
            List<ItemStack> tmplis = new ArrayList<>();

            for(int itemcnt=0;itemcnt< ymlfile.getInt(path + ".bundle.itemcnt");itemcnt++)
                tmplis.add(readItemStackFromYml(ymlfile, path + ".bundle." + itemcnt));
            tmpbm.setItems(tmplis);
            itemwhckrd.setItemMeta(tmpbm);
            return itemwhckrd;
        }

        //指南针
        if(ymlfile.contains(path + ".compass")) {
            CompassMeta tmpcm = (CompassMeta) tmpmeta;
            assert tmpcm != null;
            String worlduid = ymlfile.getString(path + ".compass.lodestone.world");
            double x = ymlfile.getDouble(path + ".compass.lodestone.x");
            double y = ymlfile.getDouble(path + ".compass.lodestone.y");
            double z = ymlfile.getDouble(path + ".compass.lodestone.z");
            assert worlduid != null;
            tmpcm.setLodestone(new Location(Bukkit.getWorld(UUID.fromString(worlduid)),x,y,z));
            itemwhckrd.setItemMeta(tmpcm);
            return itemwhckrd;
        }

        //弩 (里面能放东西)
        if(ymlfile.contains(path + ".crossbow")) {
            CrossbowMeta tmpcm = (CrossbowMeta) tmpmeta;
            assert tmpcm != null;
            List<ItemStack> tmplis = new ArrayList<>();
            for(int itemcnt=0;itemcnt< ymlfile.getInt(path + ".crossbow.itemcnt");itemcnt++)
                tmplis.add(readItemStackFromYml(ymlfile, path + ".crossbow." + itemcnt));
            tmpcm.setChargedProjectiles(tmplis);
            itemwhckrd.setItemMeta(tmpcm);
            return itemwhckrd;
        }

        //能掉耐久的
        if(ymlfile.contains(path + ".damage")) {
            Damageable tmpd = (Damageable) tmpmeta;
            assert tmpd != null;
            tmpd.setDamage(ymlfile.getInt(path + ".damage"));
            itemwhckrd.setItemMeta(tmpd);
            return itemwhckrd;
        }

        if(ymlfile.contains(path + ".fireworkeffect")) {
            FireworkEffectMeta tmpfem = (FireworkEffectMeta) tmpmeta;
            assert tmpfem != null;
            tmpfem.setEffect(readEffectFromYml(ymlfile,path+ ".fireworkeffect"));
            itemwhckrd.setItemMeta(tmpfem);
            return itemwhckrd;
        }


        if(ymlfile.contains(path + ".effect")) {
            FireworkMeta tmpfm = (FireworkMeta) tmpmeta;
            assert tmpfm != null;
            for(int effcnt = 0; effcnt<ymlfile.getInt(path+ ".effect.effcnt",0);effcnt++)
                tmpfm.addEffect(readEffectFromYml(ymlfile,path+ ".effect." + effcnt));
            tmpfm.setPower(ymlfile.getInt(path+ ".effect.power" , 1));
            itemwhckrd.setItemMeta(tmpfm);
            return itemwhckrd;
        }

        //皮革盔甲 能染色
        if(ymlfile.contains(path + ".lamcolor")) {
            LeatherArmorMeta tmplam = (LeatherArmorMeta) tmpmeta;
            assert tmplam != null;
            tmplam.setColor(Color.fromRGB(ymlfile.getInt(path + ".lamcolor")));
            itemwhckrd.setItemMeta(tmplam);
            return itemwhckrd;
        }

        //地图
        if(ymlfile.contains(path + ".map")) {

            MapMeta tmpmm = (MapMeta) tmpmeta;
            assert tmpmm != null;
            if(ymlfile.contains(path + ".map.view")) {
                String worlduid = ymlfile.getString(path + ".map.view.world");
                assert worlduid != null;
                MapView tmpmv = Bukkit.createMap(Objects.requireNonNull(Bukkit.getWorld(UUID.fromString(worlduid))));
                tmpmv.setCenterX(ymlfile.getInt(path + ".map.view.x"));
                tmpmv.setCenterZ(ymlfile.getInt(path + ".map.view.z"));
                tmpmv.setScale(MapView.Scale.valueOf(ymlfile.getString(path + ".map.view.scale")));
                tmpmv.setLocked(ymlfile.getBoolean(path + ".map.view.locked"));
                tmpmv.setTrackingPosition(ymlfile.getBoolean(path + ".map.view.tracking"));
                tmpmv.setUnlimitedTracking(ymlfile.getBoolean(path + ".map.view.unlimited"));
                tmpmm.setMapView(tmpmv);
            }
            if(ymlfile.contains(path + ".map.color")){
                tmpmm.setColor(Color.fromRGB(ymlfile.getInt(path + ".map.color")));
            }
            if(ymlfile.contains(path + ".map.locationname")){
                tmpmm.setLocationName(ymlfile.getString(path + ".map.locationname"));
            }
            tmpmm.setScaling(ymlfile.getBoolean(path + ".map.scaling"));

            itemwhckrd.setItemMeta(tmpmm);
            return itemwhckrd;
        }

        //药水
        if(ymlfile.contains(path + ".potion")){

            PotionMeta tmppm = (PotionMeta) tmpmeta;
            assert tmppm != null;
            if(ymlfile.contains(path + ".potion.color")){
                tmppm.setColor(Color.fromRGB(ymlfile.getInt((path + ".potion.color"))));
            }
            if(ymlfile.contains(path + ".potion.effect")){
                for (int i = 0; i < ymlfile.getInt(path + ".potion.effect.effcnt"); i++)
                    tmppm.addCustomEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(Objects.requireNonNull(ymlfile.getString(path + ".potion.effect." + i + ".type")))),
                            ymlfile.getInt(path + ".potion.effect."+i+".duration"), ymlfile.getInt(path + ".potion.effect."+i+".amplifier"), ymlfile.getBoolean(path + ".potion.effect."+i+".ambient"),
                            ymlfile.getBoolean(path + ".potion.effect."+i+".icon"), ymlfile.getBoolean(path + ".potion.effect."+i+".particles")),true);
            }
            tmppm.setBasePotionData(new PotionData(PotionType.valueOf(ymlfile.getString(path + ".potion.basepotiondata.type")), ymlfile.getBoolean(path + ".potion.basepotiondata.extended"),
                    ymlfile.getBoolean(path + ".potion.basepotiondata.upgraded")));

            itemwhckrd.setItemMeta(tmppm);
            return itemwhckrd;
        }

        //头
        if(ymlfile.contains(path + ".skull")){

            SkullMeta tmpsm = (SkullMeta) tmpmeta;
            assert tmpsm != null;
            tmpsm.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(ymlfile.getString(path + ".skull")))));
            itemwhckrd.setItemMeta(tmpsm);
            return itemwhckrd;
        }

        if(ymlfile.contains(path + ".suspiciousstew")){
            SuspiciousStewMeta tmssm = (SuspiciousStewMeta) tmpmeta;
            assert tmssm != null;
            if(ymlfile.contains(path + ".suspiciousstew.effect")){
                for (int i = 0; i < ymlfile.getInt(path + ".suspiciousstew.effect.effcnt"); i++)
                    tmssm.addCustomEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(Objects.requireNonNull(ymlfile.getString(path + ".suspiciousstew.effect." + i + ".type")))),
                            ymlfile.getInt(path + ".suspiciousstew.effect."+i+".duration"), ymlfile.getInt(path + ".suspiciousstew.effect."+i+".amplifier"), ymlfile.getBoolean(path + ".suspiciousstew.effect."+i+".ambient"),
                            ymlfile.getBoolean(path + ".suspiciousstew.effect."+i+".icon"), ymlfile.getBoolean(path + ".suspiciousstew.effect."+i+".particles")),true);
            }
            itemwhckrd.setItemMeta(tmssm);
            return itemwhckrd;
        }

        //热带鱼桶
        if(ymlfile.contains(path + ".tropicalfishbucket")) {
            TropicalFishBucketMeta tmptfbm = (TropicalFishBucketMeta) tmpmeta;
            assert tmptfbm != null;
            tmptfbm.setBodyColor(DyeColor.valueOf(ymlfile.getString(path + ".tropicalfishbucket.bodycolor")));
            tmptfbm.setPatternColor(DyeColor.valueOf(ymlfile.getString(path + ".tropicalfishbucket.pattrencolor")));
            tmptfbm.setPattern(TropicalFish.Pattern.valueOf(ymlfile.getString(path + ".tropicalfishbucket.pattren")));
            itemwhckrd.setItemMeta(tmptfbm);
            return itemwhckrd;
        }




        itemwhckrd.setItemMeta(tmpmeta);
        return itemwhckrd;
    }


    /**
     * Never use outside.
     * @throws IllegalArgumentException  - if this enum type has no constant with the specified name
     * @throws NullPointerException  - if the argument is null
     */
    private static void readAttFromYml(YamlConfiguration ymlfile, String path, ItemMeta tmpmeta, String atttypename) {
        //String atttypename = "generic_max_health";
        if(ymlfile.contains(path +".meta.attribute."+atttypename)){
            String attpath = path +".meta.attribute."+atttypename+'.';
            for(int j = 0; ymlfile.contains(attpath+j); j++){
                Object a = ymlfile.get(attpath+j);
                assert tmpmeta != null;
                tmpmeta.addAttributeModifier(Attribute.valueOf(atttypename.toUpperCase(Locale.ROOT)),
                        new AttributeModifier(UUID.fromString(Objects.requireNonNull(ymlfile.getString(attpath + j + ".uniqueid"))),
                                Objects.requireNonNull(ymlfile.getString(attpath + j + ".name")),
                                ymlfile.getDouble(attpath+j+ ".amount"),
                                AttributeModifier.Operation.valueOf(Objects.requireNonNull(ymlfile.getString(attpath + j + ".operation")).toUpperCase(Locale.ROOT)),
                                EquipmentSlot.valueOf(Objects.requireNonNull(ymlfile.getString(attpath + j + ".slot")).toUpperCase(Locale.ROOT))));
            }
        }
    }

    /**save all item data to yml file ,which can be read by{@link sn.sn.Sn#readItemStackFromYml}
     *
     * @param ymlfile where to save
     * @param path path in ymlfile
     * @param itemtosave item to save
     */
    public static void saveItemStackToYml(YamlConfiguration ymlfile, String path, ItemStack itemtosave){

        ymlfile.set(path+".type",itemtosave.getType().toString());
        ymlfile.set(path+".amount",itemtosave.getAmount());

        Map<String, Object> tmpp = itemtosave.serialize();
        if(!tmpp.isEmpty())
            ymlfile.set(path+".tag",tmpp);


        if(itemtosave.getType().toString().contains("SHULKER_BOX")&&itemtosave.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta tmpbsm = (BlockStateMeta) itemtosave.getItemMeta();
            assert tmpbsm != null;
            ShulkerBox tmpblock = (ShulkerBox) tmpbsm.getBlockState();
            Inventory tmpinv = tmpblock.getInventory();
            int cntitem = 0;
            for (int index=0;index<27;index++) {
                ItemStack tmpi =tmpinv.getItem(index);
                if(tmpi != null) {
                    ymlfile.set(path + ".shulker_box." + cntitem + ".index", index);
                    saveItemStackToYml(ymlfile, path + ".shulker_box." + cntitem++, tmpi);
                }
            }
            ymlfile.set(path + ".shulker_box.cntitem",cntitem);

        }


        if(itemtosave.getType().toString().contains("ENCHANTED_BOOK")||itemtosave.getItemMeta() instanceof EnchantmentStorageMeta) {
            EnchantmentStorageMeta tmpesm = (EnchantmentStorageMeta) itemtosave.getItemMeta();
            assert tmpesm != null;
            Map<Enchantment, Integer> tmpens = tmpesm.getStoredEnchants();
            List<Map<String, Integer>> tmplis = new ArrayList<>();
            for(Enchantment i:tmpens.keySet()){
                Map<String, Integer> tmpmap = new HashMap<>();
                tmpmap.put(i.getKey().getKey(),tmpens.get(i));
                tmplis.add(tmpmap);
            }
            ymlfile.set(path + ".enchantmentstorage",tmplis);

        }


        if(itemtosave.getType().toString().contains("KNOWLEDGE_BOOK")||itemtosave.getItemMeta() instanceof KnowledgeBookMeta) {
            KnowledgeBookMeta tmpkbm = (KnowledgeBookMeta) itemtosave.getItemMeta();
            assert tmpkbm != null;
            if(tmpkbm.hasRecipes()){
                List<NamespacedKey> tmpens = tmpkbm.getRecipes();
                ymlfile.set(path + ".knowledgebook",tmpens);
            }
        }

        //美西螈桶
        if(itemtosave.getType().toString().contains("AXOLOTL_BUCKET")||itemtosave.getItemMeta() instanceof AxolotlBucketMeta) {
            AxolotlBucketMeta tmpabm = (AxolotlBucketMeta) itemtosave.getItemMeta();
            assert tmpabm != null;
            if(tmpabm.hasVariant()){
                Axolotl.Variant tmpens = tmpabm.getVariant();
                ymlfile.set(path + ".axolotlbucket",tmpens.name());
            }
        }

        //旗帜
        if(itemtosave.getItemMeta() instanceof BannerMeta) {
            BannerMeta tmpbm = (BannerMeta) itemtosave.getItemMeta();
            assert tmpbm != null;
            List<Pattern> tmplis = tmpbm.getPatterns();
            Map<String ,String> tmpmap = new HashMap<>();
            List<Map<String ,String>> bannerlis = new ArrayList<>();
            for(Pattern tmppa : tmplis){
                tmpmap.clear();
                tmpmap.put("color",tmppa.getColor().name());
                tmpmap.put("id",tmppa.getPattern().getIdentifier());
                bannerlis.add(tmpmap);
            }
            ymlfile.set(path + ".banner",bannerlis);
        }

        /*含有方块信息的,产生不可预知的错误 所以注释
        if(itemtosave.getItemMeta() instanceof BlockDataMeta) {
            BlockDataMeta tmpbsm = (BlockDataMeta) itemtosave.getItemMeta();
            ymlfile.set(path + ".blockdata",tmpbsm.getBlockData(itemtosave.getType()).getAsString());
        }*/

        /*含有方块状态的? 未处理
        if(itemtosave.getItemMeta() instanceof BlockStateMeta) {
            if(((BlockStateMeta) itemtosave.getItemMeta()).hasBlockState()){
                BlockStateMeta tmpbsm = (BlockStateMeta) itemtosave.getItemMeta();
            }

        }*/

        //书
        if(itemtosave.getItemMeta() instanceof BookMeta) {
            BookMeta tmpbm = (BookMeta) itemtosave.getItemMeta();
            assert tmpbm != null;
            List<String> tmplis = tmpbm.getPages();
            if(tmpbm.hasAuthor())
                ymlfile.set(path + ".book.author", tmpbm.getAuthor());
            if(tmpbm.hasTitle())
                ymlfile.set(path + ".book.title", tmpbm.getTitle());
            if(tmpbm.hasGeneration())
                ymlfile.set(path + ".book.generation", Objects.requireNonNull(tmpbm.getGeneration()).name());
            if(tmpbm.hasPages())
                ymlfile.set(path + ".book.pages", tmplis);
        }

        //捆？含有物品的
        if(itemtosave.getItemMeta() instanceof BundleMeta) {
            BundleMeta tmpbm = (BundleMeta) itemtosave.getItemMeta();
            assert tmpbm != null;
            List<ItemStack> tmplis = tmpbm.getItems();
            int itemcnt=0;
            for(ItemStack tmppp:tmplis)
                saveItemStackToYml(ymlfile, path + ".bundle." + itemcnt++, tmppp);
            ymlfile.set(path + ".bundle.itemcnt",itemcnt);
        }

        //指南针
        if(itemtosave.getItemMeta() instanceof CompassMeta) {
            CompassMeta tmpcm = (CompassMeta) itemtosave.getItemMeta();
            assert tmpcm != null;
            if(tmpcm.hasLodestone()) {
                ymlfile.set(path + ".compass.lodestone.x", Objects.requireNonNull(tmpcm.getLodestone()).getX());
                ymlfile.set(path + ".compass.lodestone.y", Objects.requireNonNull(tmpcm.getLodestone()).getY());
                ymlfile.set(path + ".compass.lodestone.z", Objects.requireNonNull(tmpcm.getLodestone()).getZ());
                ymlfile.set(path + ".compass.lodestone.world", Objects.requireNonNull(Objects.requireNonNull(tmpcm.getLodestone()).getWorld()).getUID().toString());
            }
        }

        //弩
        if(itemtosave.getItemMeta() instanceof CrossbowMeta) {
            CrossbowMeta tmpcm = (CrossbowMeta) itemtosave.getItemMeta();
            assert tmpcm != null;
            if(tmpcm.hasChargedProjectiles()) {
                List<ItemStack> tmplis = tmpcm.getChargedProjectiles();
                int itemcnt=0;
                for(ItemStack tmppp:tmplis)
                    saveItemStackToYml(ymlfile, path + ".crossbow." + itemcnt++, tmppp);
                ymlfile.set(path + ".crossbow.itemcnt",itemcnt);
            }
        }

        //能掉耐久的
        if(itemtosave.getItemMeta() instanceof Damageable) {
            Damageable tmpd = (Damageable) itemtosave.getItemMeta();
            assert tmpd != null;
            if(tmpd.hasDamage()) {
                ymlfile.set(path + ".damage", tmpd.getDamage());
            }
        }

        //烟花效果 比如烟火之星
        if(itemtosave.getItemMeta() instanceof FireworkEffectMeta) {
            FireworkEffectMeta tmpfem = (FireworkEffectMeta) itemtosave.getItemMeta();
            assert tmpfem != null;
            if(tmpfem.hasEffect())
                saveEffectToYml(ymlfile,path + ".fireworkeffect", tmpfem.getEffect());
        }

        //烟花
        if(itemtosave.getItemMeta() instanceof FireworkMeta) {
            FireworkMeta tmpfm = (FireworkMeta) itemtosave.getItemMeta();
            assert tmpfm != null;
            if(tmpfm.hasEffects()) {
                List<FireworkEffect> tmpeffects = tmpfm.getEffects();
                int effcnt = 0;
                for (FireworkEffect tmpeffect : tmpeffects) {
                    saveEffectToYml(ymlfile,path + ".effect." + effcnt++,tmpeffect);
                }
                ymlfile.set(path + ".effect.effcnt", effcnt);
            }
            ymlfile.set(path + ".effect.power", tmpfm.getPower());
        }

        //皮革盔甲 能染色
        if(itemtosave.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta tmpla = (LeatherArmorMeta) itemtosave.getItemMeta();
            assert tmpla != null;
            ymlfile.set(path + ".lamcolor", tmpla.getColor().asRGB());
        }

        //地图
        if(itemtosave.getItemMeta() instanceof MapMeta) {
            MapMeta tmpmm = (MapMeta) itemtosave.getItemMeta();
            assert tmpmm != null;
            if(tmpmm.hasMapView()) {
                ymlfile.set(path + ".map.view.x", Objects.requireNonNull(tmpmm.getMapView()).getCenterX());
                ymlfile.set(path + ".map.view.z", tmpmm.getMapView().getCenterZ());
                ymlfile.set(path + ".map.view.world", Objects.requireNonNull(Objects.requireNonNull(tmpmm.getMapView()).getWorld()).getUID().toString());
                ymlfile.set(path + ".map.view.scale", tmpmm.getMapView().getScale().name());
                ymlfile.set(path + ".map.view.locked", tmpmm.getMapView().isLocked());
                ymlfile.set(path + ".map.view.tracking", tmpmm.getMapView().isTrackingPosition());
                ymlfile.set(path + ".map.view.unlimited", tmpmm.getMapView().isUnlimitedTracking());
                int renderercnt =0;
                for (MapRenderer renderer : tmpmm.getMapView().getRenderers()) {
                    ymlfile.set(path + ".map.view.contextual." + renderercnt++, renderer.isContextual());
                }
                ymlfile.set(path + ".map.view.renderercnt", renderercnt);
            }
            if(tmpmm.hasColor())
                ymlfile.set(path + ".map.color", Objects.requireNonNull(tmpmm.getColor()).asRGB());
            if(tmpmm.hasLocationName())
                ymlfile.set(path + ".map.locationname", tmpmm.getLocationName());
            ymlfile.set(path + ".map.scaling", tmpmm.isScaling());
        }

        //药水
        if(itemtosave.getItemMeta() instanceof PotionMeta){
            PotionMeta tmppm = (PotionMeta) itemtosave.getItemMeta();
            assert tmppm != null;
            if(tmppm.hasColor()){
                ymlfile.set(path + ".potion.color", Objects.requireNonNull(tmppm.getColor()).asRGB());
            }
            if(tmppm.hasCustomEffects()){
                List<PotionEffect> tmplis = tmppm.getCustomEffects();
                for (int i = 0; i < tmplis.size(); i++) {
                    ymlfile.set(path + ".potion.effect."+i+".type", tmplis.get(i).getType().getKey().getKey());
                    ymlfile.set(path + ".potion.effect."+i+".amplifier", tmplis.get(i).getAmplifier());
                    ymlfile.set(path + ".potion.effect."+i+".duration", tmplis.get(i).getDuration());
                    ymlfile.set(path + ".potion.effect."+i+".ambient", tmplis.get(i).isAmbient());
                    ymlfile.set(path + ".potion.effect."+i+".icon", tmplis.get(i).hasIcon());
                    ymlfile.set(path + ".potion.effect."+i+".particles", tmplis.get(i).hasParticles());
                }
                ymlfile.set(path + ".potion.effect.effcnt",tmplis.size());
            }

            ymlfile.set(path + ".potion.basepotiondata.type", tmppm.getBasePotionData().getType().name());
            ymlfile.set(path + ".potion.basepotiondata.extended", tmppm.getBasePotionData().isExtended());
            ymlfile.set(path + ".potion.basepotiondata.upgraded", tmppm.getBasePotionData().isUpgraded());

        }

        //头
        if(itemtosave.getItemMeta() instanceof SkullMeta){
            SkullMeta tmpsm = (SkullMeta) itemtosave.getItemMeta();
            assert tmpsm != null;
            if(tmpsm.hasOwner()){
                ymlfile.set(path + ".skull", Objects.requireNonNull(tmpsm.getOwnerProfile()).getUniqueId());
            }
        }

        //蜜汁炖菜suspiciousstew
        if(itemtosave.getItemMeta() instanceof SuspiciousStewMeta){
            SuspiciousStewMeta tmpssm = (SuspiciousStewMeta) itemtosave.getItemMeta();
            assert tmpssm != null;
            if(tmpssm.hasCustomEffects()){
                List<PotionEffect> tmplis = tmpssm.getCustomEffects();
                for (int i = 0; i < tmplis.size(); i++) {
                    ymlfile.set(path + ".suspiciousstew.effect."+i+".type", tmplis.get(i).getType().getKey().getKey());
                    ymlfile.set(path + ".suspiciousstew.effect."+i+".amplifier", tmplis.get(i).getAmplifier());
                    ymlfile.set(path + ".suspiciousstew.effect."+i+".duration", tmplis.get(i).getDuration());
                    ymlfile.set(path + ".suspiciousstew.effect."+i+".ambient", tmplis.get(i).isAmbient());
                    ymlfile.set(path + ".suspiciousstew.effect."+i+".icon", tmplis.get(i).hasIcon());
                    ymlfile.set(path + ".suspiciousstew.effect."+i+".particles", tmplis.get(i).hasParticles());
                }
                ymlfile.set(path + ".suspiciousstew.effect.effcnt",tmplis.size());
            }
        }

        //热带鱼桶
        if(itemtosave.getItemMeta() instanceof TropicalFishBucketMeta) {
            TropicalFishBucketMeta tmptfbm = (TropicalFishBucketMeta) itemtosave.getItemMeta();
            assert tmptfbm != null;
            if(tmptfbm.hasVariant()) {
                ymlfile.set(path + ".tropicalfishbucket.bodycolor", tmptfbm.getBodyColor().name());
                ymlfile.set(path + ".tropicalfishbucket.pattrencolor", tmptfbm.getPatternColor().name());
                ymlfile.set(path + ".tropicalfishbucket.pattren", tmptfbm.getPattern().name());
            }
        }




        if(Objects.requireNonNull(itemtosave.getItemMeta()).hasDisplayName())
            ymlfile.set(path+".meta.displayname",itemtosave.getItemMeta().getDisplayName());
        if(itemtosave.getItemMeta().hasLore())
            ymlfile.set(path+".meta.lore",itemtosave.getItemMeta().getLore());
        if(itemtosave.getItemMeta().isUnbreakable())
            ymlfile.set(path+".meta.unbreakable",true);
        if(itemtosave.getItemMeta().hasLocalizedName())
            ymlfile.set(path+".meta.localizedname",itemtosave.getItemMeta().getLocalizedName());
        if(itemtosave.getItemMeta().hasCustomModelData())
            ymlfile.set(path+".meta.data",itemtosave.getItemMeta().getCustomModelData());


        if(itemtosave.getItemMeta().hasEnchants()){
            Map<Enchantment,Integer> tenm = itemtosave.getItemMeta().getEnchants();
            int i=0;
            for(Enchantment key:tenm.keySet()){
                ymlfile.set(path+".meta.enchant."+i,key.getKey().getKey()+' '+tenm.get(key));
                //第一个getKey返回的是bukkit的namespacekey类型，第二个getkey返回的是String类型
                ++i;
            }

        }

        int i=0;
        /* 该部分内容过时，故注释
        Map<Enchantment,Integer> tenm = itemtosave.getEnchantments();

        for(Enchantment key:tenm.keySet()){
            say("####");
            ymlfile.set(path+".enchantment."+i,key.getKey().getKey()+' '+tenm.get(key));
            //第一个getKey返回的是bukkit的namespacekey类型，第二个getkey返回的是String类型
            ++i;
        }*/

        i=0;
        if(itemtosave.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)){
            ymlfile.set(path+".meta."+i,"HIDE_ENCHANTS");
            ++i;
        }
        if(itemtosave.getItemMeta().hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)){
            ymlfile.set(path+".meta."+i,"HIDE_ATTRIBUTES");
            ++i;
        }
        if(itemtosave.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)){
            ymlfile.set(path+".meta."+i,"HIDE_UNBREAKABLE");
            ++i;
        }
        if(itemtosave.getItemMeta().hasItemFlag(ItemFlag.HIDE_PLACED_ON)){
            ymlfile.set(path+".meta."+i,"HIDE_PLACED_ON");
            ++i;
        }
        if(itemtosave.getItemMeta().hasItemFlag(ItemFlag.HIDE_POTION_EFFECTS)){
            ymlfile.set(path+".meta."+i,"HIDE_POTION_EFFECTS");
            ++i;
        }
        if(itemtosave.getItemMeta().hasItemFlag(ItemFlag.HIDE_DYE)){
            ymlfile.set(path+".meta."+i,"HIDE_DYE");
            ++i;
        }

        if(itemtosave.getItemMeta().hasAttributeModifiers()){
            Multimap<Attribute, AttributeModifier> tamm = itemtosave.getItemMeta().getAttributeModifiers();
            i=0;
            assert tamm != null;
            for(Attribute key:tamm.keySet()){
                String attpath = key.getKey().getKey();
                //if(attpath.contains("generic_"))attpath.replaceFirst("generic_","");
                Collection<AttributeModifier> tattc = tamm.get(key);
                attpath = path+".meta.attribute."+attpath+".";
                int j=0;
                for (AttributeModifier tatt:
                     tattc) {
                    ymlfile.set(attpath+j+ ".name",tatt.getName());
                    ymlfile.set(attpath+j+ ".amount",tatt.getAmount());
                    ymlfile.set(attpath+j+ ".operation",tatt.getOperation());
                    ymlfile.set(attpath+j+ ".uniqueid",tatt.getUniqueId());
                    ymlfile.set(attpath+j+ ".slot",tatt.getSlot());
                    ++j;
                }
                ++i;
            }

        }
    }
    /**
     * Read a EffectMeta from the YamlConfiguration file. You can save it
     * to the path by using {@link sn.sn.Sn#saveEffectToYml saveEffectToYml}
     * @param ymlfile which yaml file to save.
     * @param path where to save , give the yaml path.
     * @return return the Effect read.
     */
    public static FireworkEffect readEffectFromYml(YamlConfiguration ymlfile, String path){


        List<Integer> color = Objects.requireNonNull(ymlfile.getIntegerList(path + ".color"));
        List<Integer> fadecolor = Objects.requireNonNull(ymlfile.getIntegerList(path + ".fadecolor"));
        List<Color> colors = new ArrayList<>();
        List<Color> fadecolors = new ArrayList<>();
        for (int s : color) {
            colors.add(Color.fromRGB(s));
        }
        for (int s : fadecolor) {
            fadecolors.add(Color.fromRGB(s));
        }
        FireworkEffect.Builder tmpb = FireworkEffect.builder();

        tmpb.flicker(ymlfile.getBoolean(path + ".flicker"));
        tmpb.trail(ymlfile.getBoolean(path + ".trail"));
        tmpb.with(FireworkEffect.Type.valueOf(ymlfile.getString(path + ".type")));
        tmpb.withColor(colors);
        tmpb.withFade(fadecolors);
        return tmpb.build();
    }

    /**
     * Save a EffectMeta to the YamlConfiguration file. You can get it
     * from the path by using {@link sn.sn.Sn#readEffectFromYml readEffectFromYml}
     * @param ymlfile which yaml file to save.
     * @param path where to save , give the yaml path.
     * @param effect what to save.
     */
    public static void saveEffectToYml(YamlConfiguration ymlfile, String path, FireworkEffect effect) {

        List<Integer> colorrgbs = new ArrayList<>();
        List<Integer> fadecolorrgbs = new ArrayList<>();
        for (Color color : Objects.requireNonNull(effect).getColors()) {
            colorrgbs.add(color.asRGB());
        }
        for (Color color : Objects.requireNonNull(effect).getFadeColors()) {
            fadecolorrgbs.add(color.asRGB());
        }

        ymlfile.set(path + ".flicker", Objects.requireNonNull(effect).hasFlicker());
        ymlfile.set(path + ".trail", Objects.requireNonNull(effect).hasTrail());
        ymlfile.set(path + ".type", Objects.requireNonNull(effect).getType().name());
        if(!colorrgbs.isEmpty())
            ymlfile.set(path + ".color", colorrgbs);
        if(!fadecolorrgbs.isEmpty())
            ymlfile.set(path + ".fadecolor", fadecolorrgbs);
    }

    /**
     * @param ymlfile where to save
     * @param file the file to reload the ymlfile
     * @param path the path save in ymlfile
     * @param inventory the inventory to be saved
     * @return true when successfully save ,false when some errors occur.
     */
    public static boolean saveInvToYml(@NotNull YamlConfiguration ymlfile, @NotNull File file, @NotNull String path, @NotNull Inventory inventory) {
        int nown = 0;
        List<ItemStack> noncontents = new ArrayList<>();
        for (ItemStack content : inventory.getContents()) {
            if(content != null) noncontents.add(nown++, content);
        }
        ymlfile.set(path + ".line", nown);
        ymlfile.set(path + ".items",null);

        for (int i = 0; i < nown; i++) {
            saveItemStackToYml(ymlfile, path + ".items." + i, noncontents.get(i));
        }

        try {
            ymlfile.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            ymlfile.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic


        say("雪夜插件已卸载~");
    }

     /*Permission basicio=new Permission("sn.express.basicio") ,admin= new Permission("sn.express.admin");
        basicio.setDefault(PermissionDefault.TRUE);
        admin.setDefault(PermissionDefault.OP);*/

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

    /**
     * 字符串的压缩
     *
     * @param str
     * 待压缩的字符串
     * @return 返回压缩后的字符串
     * @throws IOException
     */
    public static String compress(String str) throws IOException {
        if (null == str || str.length() <= 0) {
            return str;
        }
// 创建一个新的 byte 数组输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
// 使用默认缓冲区大小创建新的输出流
        GZIPOutputStream gzip = new GZIPOutputStream(out);
// 将 b.length 个字节写入此输出流
        gzip.write(str.getBytes());
        gzip.close();
// 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
        return out.toString("ISO-8859-1");
    }

    private boolean initVault(){
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        return economyProvider != null;
    }

    private static class EnchantPair {
        Enchantment a;
        int b;
        EnchantPair(String data){
            int index = data.indexOf(' ');
            a = Enchantment.getByKey(NamespacedKey.minecraft(data.substring(0,index)));
            b = Integer.parseInt(data.substring(index+1,data.length()));
        }
    }

    /**
     * 字符串的解压
     *
     * @param str
     * 对字符串解压
     * @return 返回解压缩后的字符串
     * @throws IOException
     */
    public static String unCompress(String str) throws IOException {
        if (null == str || str.length() <= 0) {
            return str;
        }
// 创建一个新的 byte 数组输出流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
// 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组
        ByteArrayInputStream in = new ByteArrayInputStream(str
                .getBytes(StandardCharsets.ISO_8859_1));
// 使用默认缓冲区大小创建新的输入流
        GZIPInputStream gzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n = 0;
        while ((n = gzip.read(buffer)) >= 0) {// 将未压缩数据读入字节数组
// 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此 byte数组输出流
            out.write(buffer, 0, n);
        }
// 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串
        return out.toString("UTF-8");
    }

    @Override
    public void onEnable() {
        //加载config
    /*
        plugin_file = new File( getDataFolder(), "plugin.yml");
        if(!plugin_file.exists()) {
            plugin_file.mkdir();
            try {
                plugin_file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        config_file = new File(getDataFolder().getAbsolutePath()+ "\\config.yml");
        say(getDataFolder().getAbsolutePath()+ "\\config.yml");

        int brkcnt1 = 0 ;
        while (true) {
            say("尝试寻找config");

            config_file = new File(getDataFolder().getAbsolutePath()+ "\\config.yml");
            brkcnt1 ++;
            if(brkcnt1 >=5) {
                say("config配置错误");
                break;
            }
            config_yml = YamlConfiguration.loadConfiguration(config_file);
            if(config_yml.contains("share-path"))break;
            getConfig().options().copyDefaults();
            saveDefaultConfig();
        }
        saveConfig();



        boolean Sharepathed = config_yml.getBoolean("share-path-ed");

        if(Sharepathed){//如果已经设置过地址，默认文件已经创造。
            //if(true){
            share_Path = config_yml.getString("share-path");
            //if(share_Path == null) share_Path = "E:\\\\Mc server\\";
            try {
                Sn.share_file = new File(share_Path + "share.yml");
                Sn.share_yml = YamlConfiguration.loadConfiguration(Sn.share_file);

            } catch (NullPointerException e){
                say("share.yml读取失败！ 请重新设置或者手动创建文件!");
            }
        } else {
            say("share.yml文件加载失败，请使用/express setpath [sharePath]为它添加地址，并使用/reload重载插件！");

        }

        quest_Path = config_yml.getString("quest-path");
        playerquest_Path = config_yml.getString("playerquest-path");

        quest_file = new File(quest_Path + "quest.yml");
        playerquest_file = new File(playerquest_Path + "quest.yml");

        quest_yml = YamlConfiguration.loadConfiguration(quest_file);
        playerquest_yml = YamlConfiguration.loadConfiguration(playerquest_file);

        //plugin_yml = YamlConfiguration.loadConfiguration(plugin_file);

        int n = quest_yml.getInt("Amount");
        for(int i = 0; i < n; i++){
            String questname;
            questname = quest_yml.getString("inside."+ i );
            if (quest_yml.getInt(questname +".type")== 1){
                quests[i].readQuestFromYml(questname);
            }
        }
            /*
            *
            * 1:Quest
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
            * */

        if(!initVault()) getLogger().info("[SN][WARNING]vault插件挂钩失败，请检查vault插件。");
        Bukkit.getPluginManager().registerEvents(new questLgInEvent(), this);
        Bukkit.getPluginManager().registerEvents(new showInvEvent(), this);
        getCommand("express").setExecutor(new express());
        getCommand("quest").setExecutor(new quest());

        say("雪夜插件已加载~");
    }

}
