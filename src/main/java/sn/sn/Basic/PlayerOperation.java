package sn.sn.Basic;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerOperation {


    public static void tryCMITpa(Player commander, String name){
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if(onlinePlayer.getName().equals(name)){
                commander.performCommand("/cmi tpa " + name);
                return;
            }
        }
        commander.sendMessage("你要tp的人没有在线！");
    }

    public static void tryCMITpa(Player commander, UUID to) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(to);
        if(p.isOnline()) commander.performCommand("/cmi tpa "+p.getName());
        else commander.sendMessage("你要tp的人没有在线！");
    }
}
