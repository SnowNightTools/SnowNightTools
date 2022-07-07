package sn.sn.City;

import org.bukkit.entity.Player;
import sn.sn.Basic.Other;
import sn.sn.Range.Range;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static sn.sn.Sn.*;

public class CityPlayerRuntime extends Thread{

    private final Player tracker;
    public CityPlayerRuntime(Player tracker){
        this.tracker = tracker;
    }

    @Override
    public void run() {
        try {
            while (true){

                try {
                    if(tps<5) sleep(2500);
                    else if(tps<10) sleep(1000);
                    else if(tps<15) sleep(500);
                } catch (InterruptedException ignored) {
                }

                boolean found = false;
                City cin = city_in.getOrDefault(tracker,null);
                if(cin==null){

                    for (String s : cities.keySet()) {
                        if(found)break;
                        City city = cities.get(s);
                        for (Range range : city.getTerritorial()) {
                            if(found)break;
                            if (range.isInRange(tracker.getLocation())) {
                                foundPermGrpAndSet(city);
                                found = true;
                            }
                        }
                    }

                } else {
                    for (Range range : cin.getTerritorial()) {
                        if(range.isInRange(tracker.getLocation())) {
                            found = true;
                            Other.sendDebug(tracker.getName() + " in " + cin.getName());
                        }
                    }
                    if(!found){
                        for (String s : cities.keySet()) {
                            if(found)break;
                            City city = cities.get(s);
                            for (Range range : city.getTerritorial()) {
                                if(found)break;
                                if (range.isInRange(tracker.getLocation())) {
                                    foundPermGrpAndSet(city);
                                    found = true;
                                }
                            }
                        }
                        if(!found){
                            city_in.remove(tracker);
                        }
                    }
                }

                if(!tracker.isOnline())return;
            }
        } catch (Exception ignored) {
        }
    }

    private void foundPermGrpAndSet(City city) {
        boolean grp_set = false;
        city_in.put(tracker, city);
        tracker.sendTitle(city.getName(), city.getWelcomeMessage(),10,70,20);
        for (String group : sn_perm.getPlayerGroups(tracker)) {
            sn_perm.playerRemoveGroup(tracker,group);
        }
        Map<String, List<UUID>> group = city.getPermGroupList();
        for (String s1 : group.keySet()) {
            List<UUID> uuids = group.get(s1);
            if(uuids.contains(tracker.getUniqueId())){
                String grp_name = city.getName() + '-' + s1;
                sn_perm.playerAddGroup(tracker,grp_name);
                grp_set = true;
                break;
            }
        }
        if(!grp_set){
            sn_perm.playerAddGroup(tracker,"default");
        }
        tracker.sendMessage("你的权限已经更新！");
    }
}
