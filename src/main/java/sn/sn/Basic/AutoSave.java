package sn.sn.Basic;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import sn.sn.City.CityPermissionItemStack;
import sn.sn.Collector.Collector;
import sn.sn.Quest.Quest;

import java.io.IOException;
import java.util.Map;

import static sn.sn.Sn.*;

public class AutoSave extends Thread{

    int n = 0;

    @Override
    synchronized public void run() {
        Other.sendInfo("开始自动保存配置。");

        //collector save
        for (OfflinePlayer player : collectors.keySet()) {
            for (Collector collector : collectors.get(player)) {
                collector.saveCollectorToYml(collector_yml,null);
                collector_yml.set("list."+n,collector.getName());
                n++;
            }
        }
        collector_yml.set("amount",n);

        //bins save
        int n = bin_yml.getInt("amount",0);
        for (int i = 0; i < n; i++) {
            bin_yml.set(String.valueOf(i),null);
        }
        n = bins.size();
        bin_yml.set("amount",n);
        for (int i = 0; i < n; i++) {
            bin_yml.set(String.valueOf(i),bins.get(i));
        }

        //city save
        int cnt = 0;
        for (String city_name : city_names) {
            cities.get(city_name).saveCityToYml(city_yml,String.valueOf( cnt++ ));
        }
        city_yml.set("amount",cnt);

        //quest save

        for (Quest quest : quests) {
            quest.saveQuestToYml();
        }

        //corresponding_material save
        cnt = 0;
        Map<String, Material> map = CityPermissionItemStack.getCorrespondingMaterials();
        for (String s : map.keySet()) {
            city_perm_corresponding_material_yml.set(cnt+".pn",s);
            city_perm_corresponding_material_yml.set(cnt++ +".material",map.get(s).getKey().getKey());
        }
        city_perm_corresponding_material_yml.set("amount",cnt);

        try {
            city_perm_corresponding_material_yml.save(city_perm_corresponding_material_file);
            city_yml.save(city_file);
            collector_yml.save(collector_file);
            bin_yml.save(bin_file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Other.sendInfo("配置保存成功。");
    }
}
