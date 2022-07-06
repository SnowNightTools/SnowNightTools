package sn.sn.City;

import org.bukkit.World;
import sn.sn.Range.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static sn.sn.Sn.sn_perm;

public class CityPermissionCheckThread extends Thread{

    private final City city;
    public CityPermissionCheckThread(City city){
        this.city = city;
    }

    @Override
    public void run() {

        Map<String, Map<String, Boolean>> permGroup = city.getPermList();
        for (String s : permGroup.keySet()) {
            String grp_name = city.getName() + "-" + s;
            List<World> allWorld = new ArrayList<>();
            for (Range range : city.getTerritorial()) {
                if(!allWorld.contains(range.getWorld())){
                    allWorld.add(range.getWorld());
                }
            }
            for (World world : allWorld) {
                Map<String, Boolean> map = permGroup.get(s);
                for (String s1 : map.keySet()) {
                    if(map.get(s1)) {
                        if(!sn_perm.groupHas(world, grp_name, s1))
                            sn_perm.groupAdd(world, grp_name, s1);
                    } else {
                        if(sn_perm.groupHas(world, grp_name, s1))
                            sn_perm.groupRemove(world, grp_name, s1);
                    }
                }
            }
        }

    }
}
