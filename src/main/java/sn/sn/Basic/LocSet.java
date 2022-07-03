package sn.sn.Basic;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class LocSet {

    private int x;
    private int y;
    private int z;
    private boolean xm = true;
    private boolean ym = true;
    private boolean zm = true;
    private World world;

    public LocSet(Location l){
        this.x = (int) l.getX();
        this.y = (int) l.getY();
        this.z = (int) l.getZ();
        this.world = l.getWorld();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public boolean isXm() {
        return xm;
    }

    public boolean isYm() {
        return ym;
    }

    public boolean isZm() {
        return zm;
    }

    public void revXm(){
        this.xm = !this.xm;
    }

    public void revYm(){
        this.ym = !this.ym;
    }

    public void revZm(){
        this.zm = !this.zm;
    }

    public void addX(int x) {
        this.x += x;
    }

    public void addY(int y) {
        this.y += y;
    }

    public void addZ(int z) {
        this.z += z;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocSet)) return false;
        LocSet locSet = (LocSet) o;
        return getX() == locSet.getX() && getY() == locSet.getY() && getZ() == locSet.getZ() && isXm() == locSet.isXm() && isYm() == locSet.isYm() && isZm() == locSet.isZm() && Objects.equals(getWorld(), locSet.getWorld());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getZ(), isXm(), isYm(), isZm(), getWorld());
    }

    @Override
    public String toString() {
        return "LocSet{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", xm=" + xm +
                ", ym=" + ym +
                ", zm=" + zm +
                ", world=" + world +
                '}';
    }

    public Location getLoc() {
        return new Location(world,x,y,z);
    }

}
