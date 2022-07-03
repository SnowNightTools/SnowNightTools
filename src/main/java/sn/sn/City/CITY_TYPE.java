package sn.sn.City;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.pow;

public enum CITY_TYPE {
    METROPOLIS("FIRST-TIER_CITY", 5, 5000, 36, 50, 120, Material.DIAMOND_BLOCK),
    CITY("SECOND-TIER_CITY", 4, 3000, 30, 40, 80, Material.EMERALD_BLOCK),
    TOWN("THIRD-TIER_CITY", 3, 1000, 24, 30, 50, Material.GOLD_BLOCK),
    TOWNSHIP("FOURTH-TIER_CITY", 2, 500, 18, 20, 30, Material.IRON_BLOCK),
    VILLAGE("FIFTH-TIER_CITY", 1, 300, 12, 10, 10, Material.STONE),
    NOT_ACTIVE("NOT_ACTIVE", 0, 100, 6, 2, 2, Material.DIRT),
    ADMIN("ADMIN", 6, 0x7FFFFFFF, 0x7FFFFFFF, 0x7FFFFFFF, 0x7FFFFFFF, Material.NETHERITE_BLOCK);

    private final String name;
    private final int range_perm;
    private final int perm_level;
    private final int warp_amount_perm;
    private final int max_resident;
    private final int perm_group_amount;
    private final Material symbol;

    CITY_TYPE(String name, int perm_level, int range_perm, int warp_amount_perm, int perm_group_amount, int max_resident, Material symbol) {
        this.max_resident = max_resident;
        this.name = name;
        this.warp_amount_perm = warp_amount_perm;
        this.perm_group_amount = perm_group_amount;
        this.perm_level = perm_level;
        this.range_perm = range_perm;
        this.symbol = symbol;
    }

    public Material getSymbol() {
        return symbol;
    }

    @Nullable
    public static CITY_TYPE getCityTypeByLevel(int level) {
        for (CITY_TYPE value : CITY_TYPE.values()) {
            if (value.getPermLevel() == level) return value;
        }
        return null;
    }


    @NotNull
    public ItemStack getSymbolItemStack(){
        ItemStack itemStack = new ItemStack(this.getSymbol());
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return new ItemStack(Material.IRON_AXE);
        itemMeta.setDisplayName(this.getName());
        List<String> lore = new ArrayList<>();
        lore.add("最大领地体积: "+pow(3,this.range_perm));
        lore.add("最大强加载区块数: "+this.getChunkPerm());
        lore.add("最大warp数: "+this.warp_amount_perm);
        lore.add("最大权限组数: "+this.perm_group_amount);
        lore.add("升级人数: "+this.getMaxResident());
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public int getPermGroupAmount() {
        return perm_group_amount;
    }

    public int getPermLevel() {
        return perm_level;
    }

    public int getRangePerm() {
        return range_perm;
    }

    public String getName() {
        return name;
    }

    public int getWarpAmountPerm() {
        return warp_amount_perm;
    }

    public int getMaxResident() {
        return max_resident;
    }

    public int getChunkPerm() {
        //强加载区块权限与warp权限公用一个数据
        return warp_amount_perm;
    }
}
