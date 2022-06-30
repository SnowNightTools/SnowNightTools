package sn.sn.City;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import sn.sn.Range.Range;

import java.util.*;

import static java.lang.Math.pow;
import static sn.sn.Sn.cities;
import static sn.sn.Sn.city_joined;

public class City {

    private ItemStack icon;
    private String name;
    private List<String> description;
    private List<UUID> residents = new ArrayList<>(), application = new ArrayList<>();
    private Map<String, List<UUID>> perm_group;
    private UUID mayor;
    private List<Range> territorial;
    private Map<String, Boolean> perm;
    private List<Chunk> chunks;
    private boolean activated = false;
    private Map<String, Location> warps = new HashMap<>();
    private CITY_TYPE type = CITY_TYPE.NOT_ACTIVE;

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


    public ItemStack getIcon() {
        return icon;
    }

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

    public void setResidents(List<UUID> residents) {
        for (UUID resident : residents) {
            this.addResident(resident);
        }
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public boolean addChunks(Chunk chunk) {
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

    public Map<String, Boolean> getPerm() {
        return perm;
    }

    public void setPerm(String perm_name, boolean perm) {
        this.perm.put(perm_name, perm);
    }

    public UUID getMayor() {
        return mayor;
    }

    public void setMayor(UUID mayor) {
        this.mayor = mayor;
    }

    public Map<String, List<UUID>> getPermGroup() {
        return perm_group;
    }

    public void addPermGroup(String perm_name, UUID player) {
        List<UUID> list = this.perm_group.getOrDefault(perm_name, new ArrayList<>());
        list.add(player);
        perm_group.put(perm_name, list);
    }

    public void addResident(UUID resident) {
        this.residents.add(resident);
        city_joined.put(Bukkit.getOfflinePlayer(resident), true);
        this.checkType();
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
        CITY_TYPE new_type = CITY_TYPE.getCityTypeByLevel(type.getPermLevel() + 1);
        if (new_type == null) return false;
        activated = new_type.getPermLevel() >= 1;
        this.type = new_type;
        return true;
    }

    public boolean downgrade() {
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
        if (ori_a == new_a) return false;
        if (new_a <= pow(type.getRangePerm(), 3)) {
            this.territorial = temp;
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

    public Map<String, List<UUID>> getPerm_groups() {
        return perm_group;
    }

    public List<UUID> getPerm_group(String perm_name) {
        return perm_group.get(perm_name);
    }

    public boolean addPerm_group(String name, List<UUID> perm_group) {
        if (perm.size() < type.getPermGroupAmount()) {
            this.perm_group.put(name, perm_group);
            return true;
        }
        return false;
    }

    public List<Range> getTerritorial() {
        return territorial;
    }

    public void removeResident(Player commander) {
        residents.remove(commander.getUniqueId());
    }

    public void removeTerritorial(int index) {
        territorial.remove(index);
    }
}
