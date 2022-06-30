package sn.sn.City;

import org.jetbrains.annotations.Nullable;

public enum CITY_TYPE {
    METROPOLIS("FIRST-TIER_CITY", 5, 5000, 36, 50, 120),
    CITY("SECOND-TIER_CITY", 4, 3000, 30, 40, 80),
    TOWN("THIRD-TIER_CITY", 3, 1000, 24, 30, 50),
    TOWNSHIP("FOURTH-TIER_CITY", 2, 500, 18, 20, 30),
    VILLAGE("FIFTH-TIER_CITY", 1, 300, 12, 10, 10),
    NOT_ACTIVE("NOT_ACTIVE", 0, 100, 6, 2, 2),
    ADMIN("ADMIN", 6, 0x7FFFFFFF, 0x7FFFFFFF, 0x7FFFFFFF, 0x7FFFFFFF);

    private final String name;
    private final int range_perm;
    private final int perm_level;
    private final int warp_amount_perm;
    private final int max_resident;
    private final int perm_group_amount;

    CITY_TYPE(String name, int perm_level, int range_perm, int warp_amount_perm, int perm_group_amount, int max_resident) {
        this.max_resident = max_resident;
        this.name = name;
        this.warp_amount_perm = warp_amount_perm;
        this.perm_group_amount = perm_group_amount;
        this.perm_level = perm_level;
        this.range_perm = range_perm;
    }

    @Nullable
    public static CITY_TYPE getCityTypeByLevel(int level) {
        for (CITY_TYPE value : CITY_TYPE.values()) {
            if (value.getPermLevel() == level) return value;
        }
        return null;
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
