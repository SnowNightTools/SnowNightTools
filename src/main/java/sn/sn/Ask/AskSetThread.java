package sn.sn.Ask;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static sn.sn.Ask.AskSetEvent.askSet;

public class AskSetThread extends Thread {

    private final Player commander;
    private final List<Consumer<String>> consumer_list;
    private final int time_limit;
    private final List<String> questions;
    private final Consumer<Player> n_done;
    private final Consumer<Player> done;

    public AskSetThread(Player commander, List<String> questions, List<Consumer<String>> consumer_list, int time_limit){
        this.questions = questions;
        this.commander = commander;
        this.consumer_list = consumer_list;
        this.time_limit = time_limit;
        n_done = null;
        done = null;
    }
    @SuppressWarnings("unused")
    public AskSetThread(Player commander, List<String> questions, List<Consumer<String>> consumer_list, int time_limit, Consumer<Player> n_done){
        this.questions = questions;
        this.commander = commander;
        this.consumer_list = consumer_list;
        this.time_limit = time_limit;
        this.n_done = n_done;
        done = null;
    }
    public AskSetThread(Player commander, List<String> questions, List<Consumer<String>> consumer_list, int time_limit, Consumer<Player> done, Consumer<Player> n_done){
        this.questions = questions;
        this.commander = commander;
        this.consumer_list = consumer_list;
        this.time_limit = time_limit;
        this.n_done = n_done;
        this.done = done;
    }


    @Override
    public void run() {
        try {
            List<String> requireNonNull = Objects.requireNonNull(askSet(commander,questions, time_limit,done,n_done));
            for (int i = 0, requireNonNullSize = requireNonNull.size(); i < requireNonNullSize; i++) {
                String s = requireNonNull.get(i);
                consumer_list.get(i).accept(s);
            }
        } catch (Exception ignored) {
        }
    }


}
