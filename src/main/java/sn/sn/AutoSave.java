package sn.sn;

import org.bukkit.entity.Player;

import java.io.IOException;

import static sn.sn.Sn.*;

public class AutoSave extends Thread{

    int n = 0;

    @Override
    public void run() {
        sendInfo("开始自动保存配置。");
        for (Player player : collectors.keySet()) {
            for (Collector_CE.Collector collector : collectors.get(player)) {
                collector.saveCollectorToYml(collector_yml,null);
                collector_yml.set("list."+n,collector.getName());
                n++;
            }
        }
        collector_yml.set("amount",n);

        try {
            collector_yml.save(collector_file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendInfo("配置保存成功。");
    }
}
