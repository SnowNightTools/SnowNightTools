package sn.sn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.*;

public class Range {

    final private double startX,startY,startZ;
    final private double endX,endY,endZ;
    private World world;

    public Range(double startX, double startY, double startZ, double endX, double endY, double endZ) {
        this.startX = min(startX,endX);
        this.startY = min(startY,endY);
        this.startZ = min(startZ,endZ);
        this.endX = max(startX,endX);
        this.endY = max(startY,endY);
        this.endZ = max(startZ,endZ);

    }

    public static double countUnionArea(List<Range> ranges){
        double cnt = 0,i_area;
        int n = ranges.size();
        for (Range range : ranges) {
            cnt += range.getArea();
        }
        if(n == 2){
            return cnt - ranges.get(0).getIntersection(ranges.get(1)).getArea();
        }
        List<Integer> axis;
        for (int i = 2; i <= n-1; i++) {
            i_area = 0;
            axis = new ArrayList<>();
            axis.add(0);
            for (int j = 1; j <= i; j++) {
                axis.add(j);
            }
            int pointer = i;
            do {
                i_area = getU_area(ranges, i_area, axis, i);

                List<Integer> lt = axis.subList(pointer,i+1);
                if(notFull(lt, i - pointer, n)){
                    pointer = i;
                }
                if(axis.get(pointer)>=n-i+pointer) {
                    pointer--;
                    if(pointer == 0) break;
                    axis.set(pointer,axis.get(pointer)+1);
                    for (int j = pointer + 1; j <= i; j++) {
                        axis.set(j,axis.get(j-1)+1);
                    }
                } else axis.set(pointer,axis.get(pointer)+1);

            } while (Range.notFull(axis, i, n));
            i_area = getU_area(ranges, i_area, axis, i);
            cnt += pow(-1,i-1) * i_area;
        }
        return cnt + pow(-1,n-1) *Range.countIntersectionArea(ranges);
    }

    private static double getU_area(List<Range> ranges, double i_area, List<Integer> axis, int i) {
        List<Range> temp;
        temp = new ArrayList<>();
        for (int j = 1; j <= i; j++) {
            temp.add(ranges.get(axis.get(j)-1));
        }
        i_area += Range.countIntersectionArea(temp);
        return i_area;
    }

    private static boolean notFull(List<Integer> axis, int i, int n) {
        for (int j = 1; j <= i; j++) {
            if(axis.get(j) != n-i+j) return true;
        }
        return false;
    }

    public static double countIntersectionArea(List<Range> ranges){
        if(ranges.size()==0)return 0;
        Range temp = ranges.get(0);
        for (Range range : ranges) {
            temp = temp.getIntersection(range);
        }
        return temp.getArea();
    }

    public boolean isInRange(Location loc){
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
        ymlfile.set(path+".endZ",endZ);
        if(world != null) ymlfile.set(path+".world",world.getUID().toString());
    }

    public double getArea(){
        return abs(endX-startX)*abs(endY-startY)*abs(endZ-startZ);
    }

    public double countUnionArea(Range other){
        return this.getArea()+other.getArea()-this.getIntersection(other).getArea();
    }

    public Range getIntersection(Range other){
        double usx = max(this.getStartX(),other.getStartX());
        double usy = max(this.getStartY(),other.getStartY());
        double usz = max(this.getStartZ(),other.getStartZ());
        double uex = min(this.getEndX(),other.getEndX());
        double uey = min(this.getEndY(),other.getEndY());
        double uez = min(this.getEndZ(),other.getEndZ());
        if(usx>=uex||usy>=uey||usz>=uez) return new Range(0,0,0,0,0,0);
        else return new Range(usx,usy,usz,uex,uey,uez);
    }

    public double getDx(){
        return abs(endX-startX);
    }

    public double getDy(){
        return abs(endY-startY);
    }

    public double getDz(){
        return abs(endZ-startZ);
    }


    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
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
