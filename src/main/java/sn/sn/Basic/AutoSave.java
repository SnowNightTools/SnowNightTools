package sn.sn.Basic;

import org.bukkit.OfflinePlayer;
import sn.sn.Collector.Collector;

import java.io.IOException;

import static sn.sn.Sn.*;

public class AutoSave extends Thread{

    int n = 0;

    @Override
    synchronized public void run() {
        sendInfo("开始自动保存配置。");
        for (OfflinePlayer player : collectors.keySet()) {
            for (Collector collector : collectors.get(player)) {
                collector.saveCollectorToYml(collector_yml,null);
                collector_yml.set("list."+n,collector.getName());
                n++;
            }
        }
        collector_yml.set("amount",n);

        int n = bin_yml.getInt("amount",0);
        for (int i = 0; i < n; i++) {
            bin_yml.set(String.valueOf(i),null);
        }

        n = bins.size();
        bin_yml.set("amount",n);
        for (int i = 0; i < n; i++) {
            bin_yml.set(String.valueOf(i),bins.get(i));
        }


        try {
            collector_yml.save(collector_file);
            bin_yml.save(bin_file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendInfo("配置保存成功。");
    }
}
