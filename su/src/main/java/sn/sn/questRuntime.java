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

import java.util.*;

import static sn.sn.Sn.playerquest_yml;

public class questRuntime extends BukkitRunnable {



    @Override
    public void run() {
        for (Player player: Bukkit.getServer().getOnlinePlayers()) {
            if(playerquest_yml.contains(player.getName())){
                if(playerquest_yml.contains(player.getName()+".nowtaskid")){
                    if(playerquest_yml.getInt(player.getName()+".nowtaskid")==this.getTaskId()){

                        while(playerquest_yml.getBoolean(player.getName()+".check",true)) {

                            int checktime = playerquest_yml.getInt(player.getName()+".checktime",5000);
                            try {
                                Thread.sleep(checktime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            String name = playerquest_yml.getString(player.getName() + ".nowquest");
                            quest.Quest quest = new quest.Quest(name);
                            quest.readQuestFromYml(name);

                            if(!quest.isOn()){
                                this.cancel();
                                return;
                            }

                            for (sn.sn.quest.QuestAction action : quest.getQuesttarget()) {
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
                                        Entity pl = (Entity) player;
                                        List<Entity> tg = pl.getNearbyEntities(defaultdistance, defaultdistance, defaultdistance);
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
                            sn.sn.quest.QuestAction[] questtarget = quest.getQuesttarget();
                            for (int i = 0, questtargetLength = questtarget.length; i < questtargetLength; i++) {
                                sn.sn.quest.QuestAction action = questtarget[i];
                                if(!playerquest_yml.getBoolean(player.getName()+".progress."+action.getQuestactionname(),false))questend = false;
                            }
                            if(questend)quest.succeed(player);
                        }




                        return;
                    }
                }

            }

        }
        this.cancel();
    }


    private Map<ItemStack,Boolean> getBlockListAndCnt(Player player,double distance,ItemStack[] itemtocheck){
        Map<ItemStack,Boolean> ret = new HashMap<>();
        List<Block> nowblock = getBlockList(player,distance);
        int[] cnt = new int[itemtocheck.length+5];
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
        List<Block> now =new List<Block>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<Block> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(Block block) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends Block> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends Block> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public Block get(int index) {
                return null;
            }

            @Override
            public Block set(int index, Block element) {
                return null;
            }

            @Override
            public void add(int index, Block element) {

            }

            @Override
            public Block remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<Block> listIterator() {
                return null;
            }

            @Override
            public ListIterator<Block> listIterator(int index) {
                return null;
            }

            @Override
            public List<Block> subList(int fromIndex, int toIndex) {
                return null;
            }
        };
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
