package sn.sn.Ask;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class AskSetEvent implements Listener {

    private final UUID uuid;
    private final Consumer<String> consumer;

    public AskSetEvent(UUID uuid, Consumer<String> consumer) {
        this.uuid = uuid;
        this.consumer = consumer;
    }

    public static List<String> askSet(Player commander, List<String> questions, int time_limit, Consumer<Player> done, Consumer<Player> n_done ) throws InterruptedException {
        int amount = questions.size();
        commander.sendMessage("开始设置变量：");

        List<String> tmpl = new ArrayList<>();
        double waiting = 0;
        for (int i = 0; i < amount; i++) {

            AskSetEvent task = new AskSetEvent(commander.getUniqueId(), tmpl::add);
            Bukkit.getPluginManager().registerEvents(task, Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Sn")));

            while (tmpl.size() != i+1){
                //noinspection BusyWait
                Thread.sleep(500);
                if(waiting % 10 == 0) {
                    commander.sendMessage("你设置了" + tmpl.size() + "/" + amount + "个参数,你还有" + (time_limit - waiting) + "秒来完成设置");
                    commander.sendMessage(questions.get(i));
                }
                waiting += 0.5;
                if(waiting >= time_limit){
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

    /**Begin to ask question asynchronously,without sending message to the player.
     * Nothing will be done to consumers when time out(60s), and the n_done will be called.
     * As the threads are called one by one, you can use interruptAsking()
     * to stop it, and it will be stopped when the player put "!stop" as one of the answers.
     *  If the asks were all done, the done will be called.
     *
     * @param commander who to ask
     * @param consumers the answers
     * @param done the thing to do when the asks were all done.
     * @param n_done the thing to do when the asks were not done.
     * @throws IllegalArgumentException - throw when the questions size do
     * not match the consumer size
     */
    public static synchronized void askSetAsync(@NotNull Player commander, @NotNull List<Consumer<String>> consumers, @Nullable Consumer<Player> done, @Nullable Consumer<Player> n_done){
        List<String> a = new ArrayList<>();
        for (int i = 0; i < consumers.size(); i++) {
            a.add("");
        }
        askSetAsync(commander,a,consumers,60,done,n_done);
    }

    /**Begin to ask question asynchronously. Nothing will be done to consumers when
     * time out(60s), and the n_done will be called. As the threads are called one by one,
     * you can use interruptAsking() to stop it, and it will be stopped when the player
     * put "!stop" as one of the answers. If the asks were all done, the done will be
     * called.
     * @param commander who to ask
     * @param questions the messages to send while asking
     * @param consumers the answers
     * @param done the thing to do when the asks were all done.
     * @param n_done the thing to do when the asks were not done.
     * @throws IllegalArgumentException - throw when the questions size do
     * not match the consumer size
     */
    public static synchronized void askSetAsync(@NotNull Player commander,List<String> questions,@NotNull List<Consumer<String>> consumers,@Nullable Consumer<Player> done,@Nullable Consumer<Player> n_done) throws IllegalArgumentException {
        askSetAsync(commander,questions,consumers,60,done,n_done);
    }

    /**Begin to ask question asynchronously. Nothing will be done to consumers when
     * time out, and the n_done will be called. As the threads are called one by one,
     * you can use interruptAsking() to stop it, and it will be stopped when the player
     * put "!stop" as one of the answers. If the asks were all done, the done will be
     * called.
     * @param commander who to ask
     * @param questions the messages to send while asking
     * @param consumers the answers
     * @param time_limit time limit
     * @param done the thing to do when the asks were all done.
     * @param n_done the thing to do when the asks were not done.
     * @throws IllegalArgumentException - throw when the questions size do
     * not match the consumer size
     */
    public static synchronized void askSetAsync(@NotNull Player commander, List<String> questions, @NotNull List<Consumer<String>> consumers, int time_limit, @Nullable Consumer<Player> done, @Nullable Consumer<Player> n_done) throws IllegalArgumentException {

        if(questions.size()!=consumers.size()){
            String a = "the questions size "+questions.size()+" do not match the consumer size "+consumers.size();
            throw new IllegalArgumentException(a);
        }

        commander.sendMessage("设置变量中：");
        commander.sendMessage("直接将变量输入来设置变量。");
        AskSetThread thread = new AskSetThread(commander,questions,consumers, time_limit,done,n_done);
        thread.start();

    }

    /**@deprecated Begin to ask some question from specified player, but
     * without sending question content.
     * Best never use it. Because it will block the main thread
     * until the settings are all done.
     * @param commander who to ask
     * @param amount the question amount
     * @return the answer
     * @throws InterruptedException - throw when the setting thread was interrupted
     */
    public static synchronized List<String> askSetSync(@NotNull Player commander, int amount) throws InterruptedException {
        List<String> a = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            a.add("");
        }
        return askSetSync(commander,a);
    }

    /**
     * @deprecated Begin to ask some question from specified player.
     * Best never use it. Because it will block the main thread
     * until the settings are all done.
     * @param commander who to ask
     * @param question the question to ask
     * @return the answer
     * @throws InterruptedException - throw when the setting thread was interrupted
     */
    public static synchronized List<String> askSetSync(@NotNull Player commander, List<String> question) throws InterruptedException {
        commander.sendMessage("设置变量中：");
        commander.sendMessage("直接将变量输入来设置变量。");
        int amount = question.size();
        List<String> a = new ArrayList<>();
        List<Consumer<String>> b = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            b.add(a::add);
        }
        AskSetThread thread = new AskSetThread(commander,question,b,5);
        thread.start();
        thread.join();
        return a;
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
