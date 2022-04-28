package sn.sn;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AskLifeExperience implements Listener {


    private final UUID uuid;

    private final CompletableFuture<String> future;


    public AskLifeExperience(UUID uuid, CompletableFuture<String> future) {

        this.uuid = uuid;

        this.future = future;

    }


    @EventHandler

    public void on(AsyncPlayerChatEvent event) {

        if (event.getPlayer().getUniqueId().equals(uuid)) {

            future.complete(event.getMessage());

            HandlerList.unregisterAll(this);

        }

    }

}
