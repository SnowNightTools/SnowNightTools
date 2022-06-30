package sn.sn;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static sn.sn.Sn.sendDebug;

public class Collector {

    private String name;
    private UUID owner;
    private List<Range> ranges = new ArrayList<>();
    private Location box;

    @SuppressWarnings("unused")
    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public double getRangeArea() {
        return Range.countUnionAreaFromDifferentWorld(ranges);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Range> getRanges() {
        return ranges;
    }

    public void setRanges(List<Range> ranges) {
        this.ranges = ranges;
    }

    public void addRange(Range range) {
        this.ranges.add(range);
    }

    public Location getBox() {
        return box;
    }

    public void setBox(Location box) {
        this.box = box;
    }

    public void saveCollectorToYml(YamlConfiguration ymlfile, @Nullable String path) {
        if (path == null) path = name;
        else path = path + "." + name;
        ymlfile.set(path + ".owner", owner.toString());
        SnFileIO.saveLocationToYml(ymlfile, path + ".box", this.box);
        int cnt = 1;
        for (Range range : ranges) {
            range.saveRangeToYml(ymlfile, path + ".range." + cnt++);
        }
        ymlfile.set(path + ".range_amount", cnt - 1);
        sendDebug("collector类存储完成，cnt=" + (cnt - 1));
    }
}
