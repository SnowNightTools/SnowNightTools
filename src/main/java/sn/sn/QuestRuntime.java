package sn.sn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sn.sn.Sn.*;

@SuppressWarnings("SpellCheckingInspection")
public class QuestRuntime extends BukkitRunnable {



    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        if(eco_use_vault)
            if(!eco_system_set)
                if(!initVault()) {
                    sendInfo("[SN][WARNING]vault插件挂钩失败，请检查vault插件。");
                }

        for (Player player: Bukkit.getServer().getOnlinePlayers()) {
            if(playerquest_yml.contains(player.getName())){
                if(playerquest_yml.contains(player.getName()+".nowtaskid")){
                    if(playerquest_yml.getInt(player.getName()+".nowtaskid")==this.getTaskId()){
                        if(debug) player.sendMessage("snQuest在线程"+this.getTaskId()+"检测你的任务情况~");
                        while(playerquest_yml.getBoolean(player.getName()+".check",true)) {

                            int checktime = playerquest_yml.getInt(player.getName()+".checktime",5000);
                            try {
                                Thread.sleep(checktime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String name = playerquest_yml.getString(player.getName() + ".nowquest");
                            Quest_CE.Quest quest = new Quest_CE.Quest(name);
                            quest.readQuestFromYml(name);

                            if(!quest.isOn()){
                                this.cancel();
                                return;
                            }

                            for (Quest_CE.QuestAction action : quest.getQuesttarget()) {
                                boolean questsucceed = false;
                                double defaultdistance = 50;
                                if (action.getQuestactiondata().getDefaultdistance() != -1)
                                    defaultdistance = action.getQuestactiondata().getDefaultdistance();
                                switch (action.getQuestactiontype()) {
                                    case FIND_POSITION:
                                        Location loc = player.getLocation();
                                        if (loc.distance(action.getQuestactiondata().getTargetlocation()) > defaultdistance) {
                                            questsucceed = true;
                                        }
                                        break;

                                    case ACCOMPLISHMENT:
                                    case HUSBANDRY:
                                        List<Entity> tg = player.getNearbyEntities(defaultdistance, defaultdistance, defaultdistance);
                                        Map<EntityType, Integer> tmap = new HashMap<>();
                                        Map<EntityType, Integer> questt = action.getQuestactiondata().getQuesttargetentity();
                                        for (Entity tent : tg) {
                                            if (!tmap.containsKey(tent.getType())) tmap.put(tent.getType(), 1);
                                            else tmap.put(tent.getType(), tmap.get(tent.getType()) + 1);
                                        }
                                        boolean success = true;
                                        for (EntityType type : questt.keySet()) {
                                            if (tmap.getOrDefault(type, 0) < questt.get(type)) {
                                                success = false;
                                                break;
                                            }
                                        }
                                        questsucceed = success;
                                        break;

                                    case FIND_NPC:
                                        Location l = player.getLocation();
                                        Entity e = Bukkit.getEntity(action.getQuestactiondata().getQuesttargetnpc());
                                        assert e != null;
                                        if (l.distance(e.getLocation()) <= defaultdistance) {
                                            questsucceed = true;
                                        }
                                        break;

                                    case AGRICULTURE:
                                        success = true;
                                        Map<ItemStack, Boolean> titbmap = getBlockListAndCnt(player, defaultdistance, action.getQuestactiondata().getQuesttargetitem());
                                        for (ItemStack stack : action.getQuestactiondata().getQuesttargetitem()) {
                                            if (!titbmap.get(stack)) {
                                                success = false;
                                                break;
                                            }
                                        }

                                        questsucceed = success;
                                        break;

                                    case CRUSADE:
                                        success = true;
                                        for (EntityType et: action.getQuestactiondata().getQuesttargetentity().keySet())
                                            if (playerquest_yml.getInt(player.getName() + ".process." + action.getQuestactionname() + "." + et.getKey().getKey(), 0) < action.getQuestactiondata().getQuesttargetentity().get(et)) {
                                                success = false;
                                                break;
                                            }
                                        questsucceed = success;
                                        break;

                                    case FIND_ITEM:
                                    case COLLECT:
                                    case BUILD:
                                        break;
                                }
                                if(questsucceed){
                                    playerquest_yml.set(player.getName()+".progress."+action.getQuestactionname(),true);
                                }

                            }
                            boolean questend = true;
                            List<Quest_CE.QuestAction> questtarget = quest.getQuesttarget();
                            for (Quest_CE.QuestAction action : questtarget) {
                                if (!playerquest_yml.getBoolean(player.getName() + ".progress." + action.getQuestactionname(), false))
                                    questend = false;
                            }
                            if(questend)quest.succeed(player);
                        }
                        if(debug) player.sendMessage("snQuest在"+this.getTaskId()+"检测你的任务情况的线程已经结束！");
                    }
                }

            }

        }

        this.cancel();
    }


    private Map<ItemStack,Boolean> getBlockListAndCnt(Player player, double distance, List<ItemStack> itemtocheck){
        Map<ItemStack,Boolean> ret = new HashMap<>();
        List<Block> nowblock = getBlockList(player,distance);
        int[] cnt = new int[itemtocheck.size()+5];
        boolean succeed;
        int i=0;
        for (ItemStack stack : itemtocheck) {
            succeed = false;
            for (Block block : nowblock) {
                if(block.getType().equals(stack.getType()))cnt[i]++;
            }
            ++i;
            if(cnt[i]>=stack.getAmount()){
                succeed = true;
            }
            ret.put(stack,succeed);
        }
        return ret;
    }

    private List<Block> getBlockList(Player player,double distance) {
        return getBlockList(player.getWorld(),player.getLocation(),distance);
    }

    private List<Block> getBlockList(World world,Location location,double distance) {
        Block block = world.getBlockAt(location);
        int x=block.getX(),y=block.getY(),z=block.getZ();
        List<Block> now = new ArrayList<>();
        return getBlockInDistance(block,distance,x,y,z,now);

    }

    private List<Block> getBlockInDistance(Block firstblock, double distance, int x, int y, int z, List<Block> now) {
        if((x- firstblock.getX())*(x- firstblock.getX())+(y- firstblock.getY())*(y- firstblock.getY())+(z- firstblock.getZ())*(z- firstblock.getZ())>distance*distance) {
            return now;
        }
        getBlockInDistance(firstblock,distance,x+1,y,z,now);
        getBlockInDistance(firstblock,distance,x,y+1,z,now);
        getBlockInDistance(firstblock,distance,x,y,z+1,now);
        getBlockInDistance(firstblock,distance,x-1,y,z,now);
        getBlockInDistance(firstblock,distance,x,y-1,z,now);
        getBlockInDistance(firstblock,distance,x,y,z-1,now);
        World noww = firstblock.getWorld();
        now.add(noww.getBlockAt(x, y, z));
        return now;
    }
}
