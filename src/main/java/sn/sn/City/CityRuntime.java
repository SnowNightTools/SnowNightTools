package sn.sn.City;

import org.bukkit.entity.Player;
import sn.sn.Range.Range;

import static sn.sn.Basic.Other.sendDebug;
import static sn.sn.Sn.*;

public class CityRuntime extends Thread{

    private final Player tracker;
    public CityRuntime(Player tracker){
        this.tracker = tracker;
    }

    @Override
    public void run() {
        while (true){

            try {
                sendDebug("tps = "+ tps);
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
                            city_in.put(tracker,city);
                            tracker.sendTitle(city.getName(),city.getWelcomeMessage(),10,70,20);
                            found = true;
                        }
                    }
                }

            } else {
                for (Range range : cin.getTerritorial()) {
                    if(range.isInRange(tracker.getLocation())) found = true;
                }
                if(!found){
                    for (String s : cities.keySet()) {
                        if(found)break;
                        City city = cities.get(s);
                        for (Range range : city.getTerritorial()) {
                            if(found)break;
                            if (range.isInRange(tracker.getLocation())) {
                                city_in.put(tracker,city);
                                tracker.sendTitle(city.getName(),city.getWelcomeMessage(),10,70,20);
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
    }
}
