package sn.sn.City;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sn.sn.Basic.SnFileIO;
import sn.sn.Range.Range;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static sn.sn.Sn.*;

public class City {

    private ItemStack icon;
    private String name, welcome_message = "欢迎~";
    private List<String> description = new ArrayList<>();
    private UUID mayor;
    private List<Range> territorial = new ArrayList<>();
    private CITY_TYPE type = CITY_TYPE.NOT_ACTIVE;
    private final List<UUID> residents = new ArrayList<>();
    private final List<UUID> application = new ArrayList<>();
    private final List<UUID> perm_set = new ArrayList<>();
    private final Map<String, List<UUID>> perm_group = new HashMap<>();
    private final Map<String ,Map<String, Boolean>> perm_list = new HashMap<>();
    private final List<Chunk> chunks = new ArrayList<>();
    private final Map<String, Location> warps = new HashMap<>();
    private boolean admin = false;
    private boolean activated = false;


    private Inventory shop;
    private Long bal = 0L;


    @Nullable
    public static City getCity(OfflinePlayer player) {
        City temp;
        for (String s : cities.keySet()) {
            temp = cities.get(s);
            if (temp.getMayor().equals(player.getUniqueId())) {
                return temp;
            }
            for (UUID resident : temp.getResidents()) {
                if (resident.equals(player.getUniqueId())) {
                    return temp;
                }
            }
        }
        if (player.isOnline()) Objects.requireNonNull(player.getPlayer()).sendMessage("未能找到你的小镇！");
        return null;
    }

    @Nullable
    public static City checkMayorAndGetCity(Player commander) {
        City city;
        city = getCity(commander);
        try {
            if (!Objects.requireNonNull(city).getMayor().equals(commander.getUniqueId())) {
                commander.sendMessage("这个操作只能由市长完成！");
                return null;
            }
        } catch (Exception e) {
            commander.sendMessage("你好像还没有加入城市！");
            return null;
        }
        return city;
    }

    public City(){
        Map<String,Boolean> default_perm = new HashMap<>();
        for (String s : perm_city_settable) {
            default_perm.put(s,true);
        }
        Map<String,Boolean> mayor_perm = new HashMap<>();
        for (String s : perm_city_settable) {
            mayor_perm.put(s,true);
        }

        perm_list.put("mayor",mayor_perm);
        perm_list.put("residents",default_perm);
        perm_group.put("residents",residents);
    }

    public String getWelcomeMessage() {
        return welcome_message;
    }

    public void setWelcomeMessage(String welcome_message) {
        this.welcome_message = welcome_message;
    }

    public CITY_TYPE getType() {
        return type;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public ItemStack getIcon() {
        return icon;
    }

    @NotNull
    public List<String> getDescription() {
        return description;
    }

    public Map<String, Location> getWarps() {
        return warps;
    }

    public Location getWarp(String warp) {
        return warps.get(warp);
    }

    public boolean addWarp(String name, Location loc) {
        boolean in = false;
        for (Range range : this.territorial) {
            if (range.isInRange(loc)) in = true;
        }
        if (!in) {
            return false;
        }
        for (String s : warps.keySet()) {
            if (s.equals(name)) {
                this.warps.put(name, loc);
                return true;
            }
        }
        if (warps.size() < type.getWarpAmountPerm()) {
            this.warps.put(name, loc);
            return true;
        } else return false;
    }

    public boolean resetWarp(String name, Location loc){
        if(!warps.containsKey(name)) return false;
        warps.put(name,loc);
        return true;
    }

    public void addApplication(UUID applier) {
        if (!application.contains(applier)) application.add(applier);
    }

    public void acceptApplication(UUID applier) {
        application.remove(applier);
        this.addResident(applier);
    }

    public void shelveApplication(UUID applier) {
        application.remove(applier);
    }

    public boolean isActivated() {
        return activated;
    }

    public List<UUID> getApplications() {
        return application;
    }

    public List<UUID> getResidents() {
        return residents;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public boolean addChunk(Chunk chunk) {
        if (chunk.isForceLoaded()) return false;
        for (Chunk c : chunks) {
            if (c.equals(chunk)) return false;
        }
        Range chunk_range = new Range(chunk);
        if (!chunk_range.isInRange(territorial)) return false;
        if (chunks.size() < type.getChunkPerm()) {
            this.chunks.add(chunk);
            chunk.setForceLoaded(true);
            return true;
        }
        return false;
    }

    public void saveCityToYml(YamlConfiguration ymlfile, String path){
        ymlfile.set(path+".name",name);
        ymlfile.set(path+".welcome_message",welcome_message);
        SnFileIO.saveItemStackToYml(ymlfile,path+".icon",icon);
        ymlfile.set(path+".admin",admin);
        ymlfile.set(path+".mayor",mayor.toString());

        int cnt = 0;
        for (String s : description) {
            ymlfile.set(path+".description."+cnt++,s);
        }
        ymlfile.set(path+".dp_line",cnt);

        cnt = 0;
        for (UUID s : residents) {
            ymlfile.set(path+".resident."+cnt++,s.toString());
        }
        ymlfile.set(path+".res_amt",cnt);

        cnt = 0;
        for (Range s : territorial) {
            s.saveRangeToYml(ymlfile, path+".range."+ cnt++);
        }
        ymlfile.set(path+".range_amt",cnt);

        cnt = 0;
        for (String s : perm_group.keySet()) {
            ymlfile.set(path+".perm."+cnt+".name",s);
            List<UUID> pg_player = perm_group.get(s);
            Map<String, Boolean> pg_perm = perm_list.get(s);
            int cnt1 = 0;
            for (UUID uuid : pg_player) {
                ymlfile.set(path+".perm."+cnt+".player."+cnt1++,uuid.toString());
            }
            ymlfile.set(path+".perm."+cnt+".pg_player_amt",cnt1);
            cnt1 = 0;
            for (String p_name : pg_perm.keySet()) {
                ymlfile.set(path+".perm."+cnt+".perm."+cnt1+".p_name",p_name);
                ymlfile.set(path+".perm."+cnt+".perm."+ cnt1++ +".p_on",pg_perm.get(p_name));
            }
            ymlfile.set(path+".perm."+ cnt++ +".pg_player_amt",cnt1);
        }
        ymlfile.set(path+".perm_grp_amt",cnt);

        cnt = 0;
        for (String s : warps.keySet()) {
            ymlfile.set(path+".warp."+cnt+".name",s);
            SnFileIO.saveLocationToYml(ymlfile,path+".warp."+ cnt++ +".loc",warps.get(s));
        }
        ymlfile.set(path+".warp_amt",cnt);

        cnt = 0;
        for (Chunk s : chunks) {
            ymlfile.set(path+".chunk."+cnt+".world",s.getWorld().getUID().toString());
            ymlfile.set(path+".chunk."+cnt+".x",s.getX());
            ymlfile.set(path+".chunk."+ cnt++ +".z",s.getZ());
        }
        ymlfile.set(path+".chunk_amt",cnt);

        cnt = 0;
        for (UUID s : application) {
            ymlfile.set(path+".application."+ cnt++,s.toString());
        }
        ymlfile.set(path+".app_amt",cnt);

    }

    public UUID getMayor() {
        return mayor;
    }

    public void setMayor(UUID mayor) {
        this.mayor = mayor;
        perm_set.add(mayor);
        List<UUID> t = new ArrayList<>();
        t.add(mayor);
        perm_group.put("mayor",t);
    }

    public Map<String, Map<String, Boolean>> getPermList() {
        return perm_list;
    }

    public void addPlayerToPermGroup(String pg_name, UUID player) {
        if(pg_name.equals("mayor")||pg_name.equals("residents")) return;
        if(perm_set.contains(player))return;
        perm_set.add(player);
        List<UUID> list = this.perm_group.getOrDefault(pg_name, new ArrayList<>());
        list.add(player);
        perm_group.put(pg_name, list);
    }

    public void removePlayerFromPermGroup(String pg_name, UUID player) {
        if(!perm_set.contains(player))return;
        perm_set.remove(player);
        List<UUID> list = this.perm_group.get(pg_name);
        if(!list.contains(player))return;
        list.remove(player);
        perm_group.put(pg_name, list);
    }

    public void setPermToPermGroup(String pg_name, String perm, boolean on) {
        Map<String, Boolean> temp = perm_list.getOrDefault(pg_name,new HashMap<>());
        if(perm.equalsIgnoreCase("all")){
            for (String s : perm_city_settable) {
                temp.put(s,on);
            }
        } else temp.put(perm,on);
        perm_list.put(pg_name,temp);
        perm_group.put(pg_name,new ArrayList<>());
    }

    public Map<String, List<UUID>> getPermGroupList() {
        return perm_group;
    }

    private void checkType() {
        while (residents.size() > type.getMaxResident()) {
            if (!this.upgrade()) {
                break;
            }
        }
        while ((type.getPermLevel() >= 2) && (residents.size() <= Objects.requireNonNull(CITY_TYPE.getCityTypeByLevel(type.getPermLevel() - 1)).getMaxResident())) {
            if (!this.downgrade()) {
                break;
            }
        }
    }

    public boolean upgrade() {
        if(admin)return false;
        CITY_TYPE new_type = CITY_TYPE.getCityTypeByLevel(type.getPermLevel() + 1);
        if (new_type == null) return false;
        activated = new_type.getPermLevel() >= 1;
        this.type = new_type;
        return true;
    }

    public boolean downgrade() {
        if(admin)return false;
        CITY_TYPE new_type = CITY_TYPE.getCityTypeByLevel(type.getPermLevel() - 1);
        if (new_type == null) return false;
        activated = new_type.getPermLevel() >= 1;
        this.type = new_type;
        return true;
    }

    public boolean addTerritorial(Range territorial) {
        List<Range> temp = new ArrayList<>(this.territorial);
        double ori_a = Range.countUnionAreaFromDifferentWorld(temp);
        temp.add(territorial);
        double new_a = Range.countUnionAreaFromDifferentWorld(temp);
        if (abs(ori_a - new_a) < 0.05) return false;
        if (new_a <= pow(type.getRangePerm(), 3)) {
            this.territorial = new ArrayList<>(temp);
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UUID> getPerm_group(String perm_name) {
        return perm_group.get(perm_name);
    }

    public List<Range> getTerritorial() {
        return territorial;
    }

    public void removeResident(OfflinePlayer commander) {
        residents.remove(commander.getUniqueId());
    }

    public void removeTerritorial(int index) {
        territorial.remove(index);
        checkChunks();
    }

    private void checkChunks() {
        List<Chunk> del = new ArrayList<>();
        for (Chunk chunk : chunks) {
            if (!new Range(chunk).isInRange(territorial)) {
                del.add(chunk);
            }
        }
        for (Chunk chunk : del) {
            chunks.remove(chunk);
            chunk.getWorld().setChunkForceLoaded(chunk.getX(),chunk.getZ(),false);
        }
    }

    public void setAdmin() {
        type = CITY_TYPE.ADMIN;
        admin = true;
    }

    public void addResident(UUID resident) {
        perm_set.add(mayor);
        this.residents.add(resident);
        perm_group.put("default",residents);
        city_joined.put(Bukkit.getOfflinePlayer(resident), true);
        this.checkType();
    }

}
