package sn.sn;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static sn.sn.Sn.*;

public class questLgInEvent implements Listener {

    public void JoinLoadQuest(PlayerJoinEvent event){
        if(!sneconomy.hasAccount(event.getPlayer()))sneconomy.createPlayerAccount(event.getPlayer());

        if(!config_yml.getBoolean("login-load"))return;

        if(playerquest_yml.contains(event.getPlayer().getName()+".nowquest")){
            if(quest.loadQuest(event.getPlayer(),playerquest_yml.getString(event.getPlayer().getName()+".nowquest")))
                event.getPlayer().sendMessage("你的任务已加载，你可以完成任务辣！");
            else event.getPlayer().sendMessage("你的任务加载失败了，请联系管理员！");
        } else {
            event.getPlayer().sendMessage("你现在没有任务，若获得任务请使用/quest load加载它！");
        }


    }
}
