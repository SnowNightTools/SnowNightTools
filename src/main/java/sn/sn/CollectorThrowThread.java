package sn.sn;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static sn.sn.Collector_CE.rubbishes;
import static sn.sn.Sn.*;

public class CollectorThrowThread extends Thread{

    File folder = new File(data_folder,"rubbishes");

    @Override
    public void run() {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            sendError(e.getLocalizedMessage());
        }
        Date now = new Date();
        File rubbish_file = new File(folder,"rubbish-" + now + ".yml");
        YamlConfiguration rubbish_yml = new YamlConfiguration();
        int amount = 0;
        for (ItemStack item : rubbishes) {
            saveItemStackToYml(rubbish_yml, String.valueOf(amount++),item);
        }
        rubbish_yml.set("amount",amount);
        try {
            rubbish_yml.save(rubbish_file);
        } catch (IOException e) {
            sendError("在保存掉落物品文件时发送错误！可能丢失掉落物品！");
            sendError(e.getLocalizedMessage());
        }
    }
}
