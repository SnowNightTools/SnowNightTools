package sn.sn.Basic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SayToEveryoneThread extends Thread{

    final String say;

    public SayToEveryoneThread(String say){
        this.say = say;
    }

    @Override
    public void run() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.sendMessage("[Sn]"+say);
        }
    }
}
