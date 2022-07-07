package sn.sn.City;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sn.sn.Basic.Other;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityPermissionItemStack extends ItemStack {

    private static Map<String, Material> corresponding_material;
    private final String perm_name;
    private boolean on;

    public CityPermissionItemStack(String perm_name, Boolean on) {
        this.perm_name = perm_name;
        if(on) turnOn();
        this.setType(corresponding_material.getOrDefault(perm_name, Material.PAPER));
    }

    public static void checkCorrespondingMaterialFromYml(YamlConfiguration ymlfile){
        int amount = ymlfile.getInt("amount");
        corresponding_material = new HashMap<>();
        for (int i = 0; i < amount; i++) {
            String pn = ymlfile.getString(i+".pn");
            Material m = Material.getMaterial(ymlfile.getString(i+".material","PAPER"));
            corresponding_material.put(pn,m);
        }
        Other.sendDebug("checkCorrespondingMaterialFromYml method called");
    }

    public static Map<String, Material> getCorrespondingMaterials() {
        return corresponding_material;
    }

    public static boolean addCorrespondingMaterials(String s, Material m) {
        if(corresponding_material.containsKey(s)){
            return false;
        } else {
            corresponding_material.put(s, m);
            return true;
        }
    }

    public String getPermName() {
        return perm_name;
    }

    public boolean addLore(String lore){
        ItemMeta im = this.getItemMeta();
        List<String> ori_lore;
        if (im != null) {
            ori_lore = im.getLore();
        } else return false;
        if (ori_lore != null) {
            ori_lore.add(lore);
        } else return false;
        im.setLore(ori_lore);
        this.setItemMeta(im);
        return true;
    }

    public void turnOn(){
        on = true;
        ItemMeta im = this.getItemMeta();
        if (im != null) {
            im.addEnchant(Enchantment.LUCK,1,false);
        } else return;
        this.setItemMeta(im);
    }

    public void turnOff(){
        on = false;
        ItemMeta im = this.getItemMeta();
        if (im != null) {
            for (Enchantment enchantment : im.getEnchants().keySet()) {
                im.removeEnchant(enchantment);
            }
        } else return;
        this.setItemMeta(im);
    }

    public boolean isOn() {
        return on;
    }
}
