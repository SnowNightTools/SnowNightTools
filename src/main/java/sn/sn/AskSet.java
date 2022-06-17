package sn.sn;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class AskSet implements Listener {

    private final UUID uuid;
    private final Consumer<String> consumer;

    public AskSet(UUID uuid, Consumer<String> consumer) {
        this.uuid = uuid;
        this.consumer = consumer;
    }

    public static List<String> askSet(Player commander, List<String> questions, int timelimit, Consumer<Player> done, Consumer<Player> n_done ) throws InterruptedException {
        int amount = questions.size();
        commander.sendMessage("开始设置变量：");

        List<String> tmpl = new ArrayList<>();
        int waiting = 0;
        for (int i = 0; i < amount; i++) {

            AskSet task = new AskSet(commander.getUniqueId(), tmpl::add);
            Bukkit.getPluginManager().registerEvents(task, Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Sn")));

            while (tmpl.size() != i+1){
                Thread.sleep(1000);
                commander.sendMessage("你设置了"+tmpl.size()+"/"+amount+"个参数,你还有"+(timelimit-waiting)+"秒来完成设置");
                commander.sendMessage(questions.get(i));
                if(waiting++ >= timelimit){
                    commander.sendMessage("设置取消");
                    if(n_done!=null)
                        n_done.accept(commander);
                    return null;
                }
            }
            if(tmpl.get(i).equalsIgnoreCase("!stop")){
                commander.sendMessage("设置取消");
                if(n_done!=null)
                    n_done.accept(commander);
                return null;
            }
        }


        commander.sendMessage("设置完成,内容如下:");
        for (String s : tmpl) {
            commander.sendMessage(s);
        }
        commander.sendMessage("(end)");
        if(done!=null)
            done.accept(commander);
        return tmpl;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(@NotNull AsyncPlayerChatEvent event) {
        if (event.getPlayer().getUniqueId().equals(uuid)) {
            event.setCancelled(true);
            consumer.accept(event.getMessage());
            HandlerList.unregisterAll(this);
        }

    }

}
