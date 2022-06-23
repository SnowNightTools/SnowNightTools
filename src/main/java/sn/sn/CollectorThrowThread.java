package sn.sn;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static sn.sn.Collector_CE.bin;
import static sn.sn.Collector_CE.rubbishes;
import static sn.sn.Sn.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class CollectorThrowThread extends Thread{

    public static File rubbishes_folder = new File(data_folder,"rubbishes");

    @Override
    public void run() {
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            sendError(e.getLocalizedMessage());
        }
        try {
            rubbishes_folder.mkdirs();
            sleep(500);
        } catch (Exception e) {
            sendError(e.getLocalizedMessage());
        }
        Date now = new Date();
        String now_str = now.toString();
        now_str = now_str.replace(' ', '_') + ".yml";
        now_str = now_str.replace(':', '-');
        sendDebug(now_str);
        File rubbish_file = new File(rubbishes_folder.getAbsolutePath(),now_str);
        try {
            rubbish_file.createNewFile();
        } catch (IOException e) {
            sendError(e.getLocalizedMessage());
        }
        YamlConfiguration rubbish_yml = new YamlConfiguration();
        int amount = 0;
        for (ItemStack item : rubbishes) {
            saveItemStackToYml(rubbish_yml, String.valueOf(amount++),item);
        }
        rubbish_yml.set("amount",amount);
        bin.add(now_str);
        int n = bin_yml.getInt("amount",0);
        bin_yml.set("amount",n+1);
        bin_yml.set(String.valueOf(n),now_str);
        try {
            rubbish_yml.save(rubbish_file);
            bin_yml.save(bin_file);
        } catch (IOException e) {
            sendError("在保存掉落物品文件时发送错误！可能丢失掉落物品！");
            sendError(e.getLocalizedMessage());
        }
    }
}
