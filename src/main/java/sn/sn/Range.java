package sn.sn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Objects;

public class Range {

    static World world;
    static double startX,startY,startZ;
    static double endX,endY,endZ;

    public Range(double startX, double startY, double startZ, double endX, double endY, double endZ) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
    }

    public static boolean isInRange(Location loc){
        if(world != null)
            if (Objects.equals(loc.getWorld(), world))
                if (loc.getX() >= startX)
                    if (loc.getX() <= endX)
                        if (loc.getY() >= startY)
                            if (loc.getY() <= endY)
                                if (loc.getZ() >= startZ)
                                    return loc.getZ() <= endZ;
        else if (loc.getX() >= startX)
                if (loc.getX() <= endX)
                    if (loc.getY() >= startY)
                        if (loc.getY() <= endY)
                            if (loc.getZ() >= startZ)
                                return loc.getZ() <= endZ;
        return false;
    }

    public void saveRangeToYml(YamlConfiguration ymlfile, String path){
        ymlfile.set(path+".startX",startX);
        ymlfile.set(path+".startY",startY);
        ymlfile.set(path+".startZ",startZ);
        ymlfile.set(path+".endX",endX);
        ymlfile.set(path+".endY",endY);
        ymlfile.set(path+".endY",endZ);
        if(world != null) ymlfile.set(path+".world",world.getUID());
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        Range.world = world;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public double getEndZ() {
        return endZ;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getStartZ() {
        return startZ;
    }
}
