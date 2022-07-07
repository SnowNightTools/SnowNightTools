package sn.sn.Basic;

import com.google.common.collect.Multimap;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import sn.sn.City.City;
import sn.sn.Range.Range;
import sn.sn.Sn;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class SnFileIO {


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
        return readBlockFromYml(Sn.quest_yml,path,world,location);
    }

    /**read Block from ymlfile,the ymlfile defult in quest_yml
     * @param path the saving path in ymlfile
     * @param location the location the block save in
     * @return Block read result
     */
    public static Block readBlockFromYml(String path , Location location){
        return readBlockFromYml(Sn.quest_yml,path,location);
    }

    /**read Block from ymlfile,the ymlfile defult in quest_yml
     * @param path the saving path in ymlfile
     * @param world the world the block reading to
     * @return Block read result
     */
    public static Block readBlockFromYml(String path , World world){
        return readBlockFromYml(Sn.quest_yml,path,world);
    }

    /**read Block from ymlfile,the ymlfile defult in quest_yml
     * @param path the saving path in ymlfile
     * @return Block read result
     */
    public static Block readBlockFromYml(String path){
        return readBlockFromYml(Sn.quest_yml,path);
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
        saveBlockToYml(Sn.quest_yml,path,blocktosave);
    }

    /**save block to ymlfile , with defult ymlfile quest_yml
     * @param path path in ymlfile
     * @param blocktosave block to save
     * @param biome the specific biome to set
     */
    public static void saveBlockToYml(String path,Block blocktosave,Biome biome){
        saveBlockToYml(Sn.quest_yml,path,blocktosave,biome);
    }

    /**save block to ymlfile , with defult ymlfile quest_yml
     * @param path path in ymlfile
     * @param blocktosave block to save
     * @param isSetBiome true to set the biome of now
     */
    public static void saveBlockToYml(String path,Block blocktosave,boolean isSetBiome){
        saveBlockToYml(Sn.quest_yml,path,blocktosave,isSetBiome);
    }

    /**
     * @param ymlfile:where ethe ItemStack saved.
     * @param path:the path in yamlfile where the ItemStack is.
     * @return ItemStack which has been read.
     * @exception ClassCastException: when data were broken(May not clean the data before reset it)
     */
    public static ItemStack readItemStackFromYml(YamlConfiguration ymlfile, String path){

        try{
            return Objects.requireNonNull(ymlfile.getItemStack(path+".dr"));
        } catch (IndexOutOfBoundsException|NullPointerException|IllegalArgumentException|ClassCastException ignore){}
        Other.sendInfo("物品在读取时无法直接读取，尝试使用详细信息读取!");


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
                Other.EnchantPair tempenp = new Other.EnchantPair(Objects.requireNonNull(ymlfile.getString(path + ".meta.enchant." + i)));
                assert tmpmeta != null;
                tmpmeta.addEnchant(tempenp.getA(),tempenp.getB(),true);
                ++i;
            }
        }



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
            List<Map<?, ?>> tmplis;
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

    /**save all item data to a yml file ,which can be read by{@link SnFileIO#readItemStackFromYml}
     *
     * @param ymlfile where to save
     * @param path path in ymlfile
     * @param itemtosave item to save
     */
    public static void saveItemStackToYml(YamlConfiguration ymlfile, String path, ItemStack itemtosave){

        ymlfile.set(path+".dr",itemtosave);

        ymlfile.set(path+".type",itemtosave.getType().toString());
        ymlfile.set(path+".amount",itemtosave.getAmount());

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

        int i;
        /* 该部分内容过时，故注释
        Map<Enchantment,Integer> tenm = itemtosave.getEnchantments();

        for(Enchantment key:tenm.keySet()){
            sendInfo("####");
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
     * to the path by using {@link SnFileIO#saveEffectToYml saveEffectToYml}
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
     * from the path by using {@link SnFileIO#readEffectFromYml readEffectFromYml}
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
        try {
            ymlfile.set(path + ".line", nown);
            ymlfile.set(path + ".items",null);
        } catch (Exception e) {
            Other.sendInfo(e.getLocalizedMessage());
        }

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

    public static Range readRangeFromYml(YamlConfiguration ymlfile, String path){

        if(ymlfile.contains(path+".world")){
            try {
                Range temp = new Range(ymlfile.getDouble(path+".startX"),
                        ymlfile.getDouble(path+".startY"),
                        ymlfile.getDouble(path+".startZ"),
                        ymlfile.getDouble(path+".endX"),
                        ymlfile.getDouble(path+".endY"),
                        ymlfile.getDouble(path+".endZ"));
                temp.setWorld(Bukkit.getWorld(UUID.fromString(Objects.requireNonNull(ymlfile.getString(path + ".world")))));
                return temp;
            } catch (Exception e) {
                Other.sendError(e.getLocalizedMessage());
            }
        } else return new Range(ymlfile.getDouble(path+".startX"),
                ymlfile.getDouble(path+".startY"),
                ymlfile.getDouble(path+".startZ"),
                ymlfile.getDouble(path+".endX"),
                ymlfile.getDouble(path+".endY"),
                ymlfile.getDouble(path+".endZ"));
        return null;
    }

    public static Location readLocationFromYml(YamlConfiguration ymlfile, String path) {
        return new Location(
                Bukkit.getWorld(UUID.fromString(Objects.requireNonNull(ymlfile.getString(path + ".world")))),
                ymlfile.getDouble(path+".x"),
                ymlfile.getDouble(path+".y"),
                ymlfile.getDouble(path+".z"));
    }

    public static void saveLocationToYml(YamlConfiguration ymlfile, String path, Location loc){
        ymlfile.set(path+".world", Objects.requireNonNull(loc.getWorld()).getUID().toString());
        ymlfile.set(path+".x", loc.getX());
        ymlfile.set(path+".y", loc.getY());
        ymlfile.set(path+".z", loc.getZ());
    }

    public static ItemStack getSkull(UUID player, List<String> lore){
        ItemStack head = new ItemStack(Material.PLAYER_HEAD,1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        assert meta != null;
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(player));
        if(lore != null) meta.setLore(lore);
        meta.setDisplayName(Bukkit.getOfflinePlayer(player).getName());
        head.setItemMeta(meta);
        return head;
    }

    public static City readCityFromYml(YamlConfiguration ymlfile, String path) throws IllegalArgumentException{
        City temp = new City();

        temp.setWelcomeMessage(ymlfile.getString(path+".welcome_message"));
        temp.setIcon(readItemStackFromYml(ymlfile,path+".icon"));
        temp.setName(ymlfile.getString(path+".name"));
        if(ymlfile.getBoolean(path+".admin"))temp.setAdmin();
        temp.setMayor(UUID.fromString(ymlfile.getString(path+".mayor","")));

        int dp_line = ymlfile.getInt(path+".dp_line",0);
        List<String> description = new ArrayList<>();
        for (int i = 0; i < dp_line; i++) {
            description.add(ymlfile.getString(path+".description."+i));
        }
        temp.setDescription(description);

        int res_amt = ymlfile.getInt(path+".res_amt",0);
        for (int i = 0; i < res_amt; i++) {
            temp.addResident(UUID.fromString(ymlfile.getString(path+".resident."+i,"")));
        }

        int range_amt = ymlfile.getInt(path+".range_amt",0);
        for (int i = 0; i < range_amt; i++) {
            temp.addTerritorial(readRangeFromYml(ymlfile, path+".range."+i));
        }

        int perm_grp_amt = ymlfile.getInt(path+".perm_grp_amt",0);
        for (int i = 0; i < perm_grp_amt; i++) {
            String name = ymlfile.getString(path+".perm."+i+".name");
            int pg_player_amt = ymlfile.getInt(path+".perm."+i+".pg_player_amt",0);
            for (int i1 = 0; i1 < pg_player_amt; i1++) {
                temp.addPlayerToPermGroup(name,UUID.fromString(ymlfile.getString(path+".perm."+i+".player."+i1,"")));
            }
            int pg_perm_amt = ymlfile.getInt(path+".perm."+i+".pg_perm_amt",0);
            for (int i1 = 0; i1 < pg_perm_amt; i1++) {
                String perm = ymlfile.getString(path+".perm."+i+".perm."+i1+".p_name","");
                boolean on = ymlfile.getBoolean(path+".perm."+i+".perm."+i1+".p_on",false);
                temp.setPermToPermGroup(name,perm,on);
            }
        }

        int warp_amt = ymlfile.getInt(path+".warp_amt",0);
        for (int i = 0; i < warp_amt; i++) {
            String warp = ymlfile.getString(path+".warp."+i+".name","");
            temp.addWarp(warp,readLocationFromYml(ymlfile,path+".warp."+i+".loc"));
        }

        int chunk_amt = ymlfile.getInt(path+".chunk_amt",0);
        for (int i = 0; i < chunk_amt; i++) {
            World tw = Bukkit.getWorld(UUID.fromString(ymlfile.getString(path+".chunk."+i+".world","")));
            int x = ymlfile.getInt(path+".chunk."+i+".x");
            int z = ymlfile.getInt(path+".chunk."+i+".z");
            if (tw == null) throw new IllegalArgumentException();
            temp.addChunk(tw.getChunkAt(x,z));
        }

        int app_amt = ymlfile.getInt(path+".app_amt",0);
        for (int i = 0; i < app_amt; i++) {
            temp.addApplication(UUID.fromString(ymlfile.getString(path+".application."+i,"")));
        }

        return temp;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File checkFile(String path) throws IOException {
        File file = new File(path);
        while (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public static Inventory readInvFromYml(InventoryHolder holder, String path, YamlConfiguration ymlfile, String title) {
        Inventory temp = Bukkit.createInventory(holder,54,title);
        String str_line = path + ".line";
        int n = ymlfile.getInt(str_line);
        for(int i=0;i<n;i++){
            ItemStack temp_stack = readItemStackFromYml(ymlfile, path + ".items"+'.'+i);
            temp.addItem(temp_stack);//添加
            temp.setItem(i, temp_stack);//设置GUI
        }
        return temp;
    }
}
