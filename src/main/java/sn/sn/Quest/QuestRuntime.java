package sn.sn.Quest;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import sn.sn.Basic.Other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sn.sn.Sn.*;

@SuppressWarnings("SpellCheckingInspection")
public class QuestRuntime extends BukkitRunnable {



    public static Map<ItemStack,Boolean> getBlockListAndCnt(Player player, double distance, List<ItemStack> itemtocheck){
        Map<ItemStack,Boolean> ret = new HashMap<>();
        List<Block> nowblock = getBlockListOnGround(player.getLocation(),distance);
        if(nowblock == null)return null;
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

    /**Count the blocks that upside is air in a distance.
     * Use about 20s to count the blocks in 100 distance,
     * although it is up to your server. Do not use it repeatedly.
     * @param ori the center Location
     * @param distance the distance to get
     * @return the block list
     */
    public static List<Block> getBlockListOnGround(Location ori, double distance){

        World world = ori.getWorld();
        if(world == null)return null;

        List<Block> temp = new ArrayList<>();

        for (int x = 0; x <= distance ; x++) {
            for (int z = 0; z <= distance ; z++) {
                Block t1 = world.getBlockAt(ori.getBlockX()+x, ori.getBlockY(), ori.getBlockZ()+z);
                Block t2 = world.getBlockAt(ori.getBlockX()+x, ori.getBlockY(), ori.getBlockZ()-z);
                Block t3 = world.getBlockAt(ori.getBlockX()-x, ori.getBlockY(), ori.getBlockZ()+z);
                Block t4 = world.getBlockAt(ori.getBlockX()-x, ori.getBlockY(), ori.getBlockZ()-z);
                Block c1 = getBlockAtXZ(t1, distance);
                Block c2 = getBlockAtXZ(t2, distance);
                Block c3 = getBlockAtXZ(t3, distance);
                Block c4 = getBlockAtXZ(t4, distance);
                boolean jump = !checkC(ori, distance, temp, c1);
                if (checkC(ori, distance, temp, c2)) {
                    jump = false;
                }
                if (checkC(ori, distance, temp, c3)) {
                    jump = false;
                }
                if (checkC(ori, distance, temp, c4)) {
                    jump = false;
                }
                if(jump) break;
            }
        }
        return temp;
    }

    public static List<Block> getBlockList(Player player,double distance) {
        return getBlockList(player.getWorld(),player.getLocation(),distance);
    }

    public static List<Block> getBlockList(World world,Location location,double distance) {
        Block block = world.getBlockAt(location);
        int x=block.getX(),y=block.getY(),z=block.getZ();
        List<Block> now = new ArrayList<>();
        return getBlockInDistance(block,distance,x,y,z,now,new boolean[(int) (3*distance)][(int) (3*distance)][(int) (3*distance)]);
    }

    private static List<Block> getBlockInDistance(Block firstblock, double distance, int x, int y, int z, List<Block> now, boolean[][][] k) {
        double n_d =(x- firstblock.getX())*(x- firstblock.getX())+(y- firstblock.getY())*(y- firstblock.getY())+(z- firstblock.getZ())*(z- firstblock.getZ());
        if(n_d>distance*distance) {
            return null;
        }

        k[(int) (x- firstblock.getX()+distance)][(int) (y- firstblock.getY()+distance)][(int) (z- firstblock.getZ()+distance)] = true;

        World noww = firstblock.getWorld();
        Block got = noww.getBlockAt(x, y, z);
        if(!got.getType().equals(Material.AIR))
            now.add(got);

        Other.sendDebug("Quest Runtime 5");
        if (!k[(int) (x+1- firstblock.getX()+distance)][(int) (y- firstblock.getY()+distance)][(int) (z- firstblock.getZ()+distance)]) {
            getBlockInDistance(firstblock,distance,x+1,y,z,now,k);
        }
        if (!k[(int) (x- firstblock.getX()+distance)][(int) (y+1- firstblock.getY()+distance)][(int) (z- firstblock.getZ()+distance)]) {
            getBlockInDistance(firstblock,distance,x,y+1,z,now,k);
        }
        if (!k[(int) (x- firstblock.getX()+distance)][(int) (y- firstblock.getY()+distance)][(int) (z+1- firstblock.getZ()+distance)]) {
            getBlockInDistance(firstblock,distance,x,y,z+1,now,k);
        }
        if((int) (x-1- firstblock.getX()+distance)>=0)
        if (!k[(int) (x-1- firstblock.getX()+distance)][(int) (y- firstblock.getY()+distance)][(int) (z- firstblock.getZ()+distance)]) {
            getBlockInDistance(firstblock,distance,x-1,y,z,now,k);
        }
        if((int) (y-1- firstblock.getY()+distance)>=0)
        if (!k[(int) (x- firstblock.getX()+distance)][(int) (y-1- firstblock.getY()+distance)][(int) (z- firstblock.getZ()+distance)]) {
            getBlockInDistance(firstblock,distance,x,y-1,z,now,k);
        }
        if((int) (z-1- firstblock.getZ()+distance)>=0)
        if (!k[(int) (x- firstblock.getX()+distance)][(int) (y- firstblock.getY()+distance)][(int) (z-1- firstblock.getZ()+distance)]) {
            getBlockInDistance(firstblock,distance,x,y,z-1,now,k);
        }

        return now;
    }

    private static boolean checkC(Location ori, double distance, List<Block> temp, Block c1) {
        if(c1 != null){
            if(c1.getLocation().distanceSquared(ori) <= squard(distance)){
                temp.add(c1);
                return true;
            }
        }
        return false;
    }

    private static double squard(double distance) {
        return distance * distance;
    }

    private static Block getBlockAtXZ(Block t,double distance){
        if( (!t.getType().equals(Material.AIR)) && t.getRelative(BlockFace.UP,1).getType().equals(Material.AIR)){
            return t;
        }
        if( (t.getType().equals(Material.AIR)) && !t.getRelative(BlockFace.DOWN, 1).getType().equals(Material.AIR)){
            return t.getRelative(BlockFace.DOWN, 1);
        }
        for (int y = 1; y <= distance ; y++) {
            if(t.getRelative(BlockFace.UP,y+1).getType().equals(Material.AIR)&&
                    !t.getRelative(BlockFace.UP,y).getType().equals(Material.AIR)){
                return t.getRelative(BlockFace.UP,y);
            }
            if(t.getRelative(BlockFace.DOWN,y).getType().equals(Material.AIR)&&
                    !t.getRelative(BlockFace.DOWN,y+1).getType().equals(Material.AIR)){
                return t.getRelative(BlockFace.DOWN,y+1);
            }
        }
        return null;
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        if(eco_use_vault)
            if(!eco_system_set)
                if(!Other.initVault()) {
                    Other.sendInfo("[SN][WARNING]vault插件挂钩失败，请检查vault插件。");
                }
        Other.sendDebug("Quest Runtime 1");
        for (Player player: Bukkit.getServer().getOnlinePlayers()) {
            if(playerquest_yml.contains(player.getName())){
                if(playerquest_yml.contains(player.getName()+".nowtaskid")){
                    if(playerquest_yml.getInt(player.getName()+".nowtaskid")==this.getTaskId()){
                        if(debug) player.sendMessage("snQuest在线程"+this.getTaskId()+"检测你的任务情况~");
                        while(playerquest_yml.getBoolean(player.getName()+".check",true)) {
                            Other.sendDebug("Quest Runtime 3");
                            int checktime = playerquest_yml.getInt(player.getName()+".checktime",5000);
                            try {
                                Thread.sleep(checktime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String name = playerquest_yml.getString(player.getName() + ".nowquest");
                            Quest quest = new Quest(name);
                            quest.readQuestFromYml(name);

                            if(!quest.isOn()){
                                this.cancel();
                                return;
                            }

                            for (QuestAction action : quest.getQuest_target()) {
                                boolean questsucceed = false;
                                double defaultdistance = 50;
                                if (action.getQuest_action_data().getDefaultdistance() != -1)
                                    defaultdistance = action.getQuest_action_data().getDefaultdistance();
                                switch (action.getQuest_action_type()) {
                                    case FIND_POSITION:
                                        Location loc = player.getLocation();
                                        if (loc.distance(action.getQuest_action_data().getTargetlocation()) > defaultdistance) {
                                            questsucceed = true;
                                        }
                                        break;

                                    case ACCOMPLISHMENT:
                                    case HUSBANDRY:
                                        List<Entity> tg = player.getNearbyEntities(defaultdistance, defaultdistance, defaultdistance);
                                        Map<EntityType, Integer> tmap = new HashMap<>();
                                        Map<EntityType, Integer> questt = action.getQuest_action_data().getQuesttargetentity();
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
                                        Entity e = Bukkit.getEntity(action.getQuest_action_data().getQuesttargetnpc());
                                        assert e != null;
                                        if (l.distance(e.getLocation()) <= defaultdistance) {
                                            questsucceed = true;
                                        }
                                        break;

                                    case AGRICULTURE:
                                        success = true;
                                        Map<ItemStack, Boolean> titbmap = getBlockListAndCnt(player, defaultdistance, action.getQuest_action_data().getQuesttargetitem());
                                        if(titbmap == null) break;
                                        for (ItemStack stack : action.getQuest_action_data().getQuesttargetitem()) {
                                            if (!titbmap.get(stack)) {
                                                success = false;
                                                break;
                                            }
                                        }

                                        questsucceed = success;
                                        break;

                                    case CRUSADE:
                                        success = true;
                                        for (EntityType et: action.getQuest_action_data().getQuesttargetentity().keySet())
                                            if (playerquest_yml.getInt(player.getName() + ".process." + action.getQuest_action_name() + "." + et.getKey().getKey(), 0) < action.getQuest_action_data().getQuesttargetentity().get(et)) {
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
                                    playerquest_yml.set(player.getName()+".progress."+action.getQuest_action_name(),true);
                                }

                            }
                            boolean questend = true;
                            List<QuestAction> questtarget = quest.getQuest_target();
                            for (QuestAction action : questtarget) {
                                if (!playerquest_yml.getBoolean(player.getName() + ".progress." + action.getQuest_action_name(), false))
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
}
