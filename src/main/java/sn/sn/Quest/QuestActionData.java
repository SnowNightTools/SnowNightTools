package sn.sn.Quest;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import sn.sn.Basic.SnFileIO;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

import static sn.sn.Sn.quest_yml;
import static sn.sn.Sn.sendInfo;

@SuppressWarnings("unused")
@SerializableAs("SnQuestActionData")
public class QuestActionData implements Cloneable, ConfigurationSerializable, Serializable {
    public double defaultdistance = -1;
    private String questActndtname;
    private List<ItemStack> questtargetitem;
    private int questtimelimit = -1;
    private Map<EntityType, Integer> questtargetentity;
    //private Map<Block, Integer> questtargetblock;
    private UUID questtargetnpc;
    private int targetpositionx = 0, targetpositiony = 0, targetpositionz = 0;
    private Location targetlocation = null;

    public QuestActionData(String questactionname) {
        this.questActndtname = questactionname;
        questtargetnpc = null;
        questtargetentity = new HashMap<>();
        questtargetitem = new ArrayList<>();
    }

    public QuestActionData() {
        questtargetnpc = null;
        questtargetentity = new HashMap<>();
        questtargetitem = new ArrayList<>();
        questActndtname = null;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(defaultdistance);
        out.writeObject(questActndtname);
        out.writeObject(questtargetitem);
        out.writeObject(questtimelimit);
        out.writeObject(questtargetentity);
        out.writeObject(questtargetnpc);
        out.writeObject(targetpositionx);
        out.writeObject(targetpositiony);
        out.writeObject(targetpositionz);
        out.writeObject(targetlocation);
    }

    private void readObjectNoData() throws ObjectStreamException {
        new QuestActionData();
    }


    public double getDefaultdistance() {
        return defaultdistance;
    }

    public void setDefaultdistance(double defaultdistance) {
        this.defaultdistance = defaultdistance;
    }

    public Boolean readQaDataFromYml(String name) {
        return readQaDataFromYml(quest_yml, name);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        defaultdistance = (double) in.readObject();
        questActndtname = (String) in.readObject();
        questtargetitem = (List<ItemStack>) in.readObject();
        questtimelimit = (int) in.readObject();
        questtargetentity = (Map<EntityType, Integer>) in.readObject();
        questtargetnpc = (UUID) in.readObject();
        targetpositionx = (int) in.readObject();
        targetpositiony = (int) in.readObject();
        targetpositionz = (int) in.readObject();
        targetlocation = (Location) in.readObject();
    }

    public void saveQaDataToYml() {
        saveQaDataToYml(quest_yml);
    }

    public void saveQaDataToYml(YamlConfiguration ymlfile) {
        ymlfile.set(questActndtname + ".type", 4);

        if (questtargetitem != null) {
            int i = 0;
            for (ItemStack a : questtargetitem) {
                SnFileIO.saveItemStackToYml(ymlfile, questActndtname + ".property-inherit.questtargetitem." + i++, a);
            }
        }
        if (questtimelimit != -1)
            ymlfile.set(questActndtname + ".property-set.questtimelimit", questtimelimit);

        if (defaultdistance != -1)
            ymlfile.set(defaultdistance + ".property-set.defaultdistance", defaultdistance);

        if (questtargetentity != null) {
            int i = 0;
            for (EntityType key : questtargetentity.keySet()) {
                ymlfile.set(questActndtname + ".property-set.questtargetentity." + i + "entitytype", key.getKey().getKey());
                ymlfile.set(questActndtname + ".property-set.questtargetentity." + i + "amount", questtargetentity.get(key));
                ++i;
            }
        }
        ymlfile.set(questActndtname + ".property-set.targetpositionx", targetpositionx);
        ymlfile.set(questActndtname + ".property-set.targetpositiony", targetpositiony);
        ymlfile.set(questActndtname + ".property-set.targetpositionz", targetpositionz);
        ymlfile.set(questActndtname + ".property-set.questtargetnpc", questtargetnpc.toString());
    }

    public Boolean readQaDataFromYml(YamlConfiguration ymlfile, String name) {
        if (!ymlfile.contains(name)) {
            sendInfo("[WARNING]读取QuestActionData数据错误，数据不存在");
            return false;
        }

        if (ymlfile.getInt(name + ".tpye") != 4) {
            sendInfo("[WARNING]读取QuestActionData数据错误，该名的类型不正确");
            return false;
        }

        questActndtname = name;

        int i = 0;
        if (ymlfile.contains(name + ".property-inherit.questtargetitem")) {
            while (ymlfile.contains(name + ".property-inherit.questtargetitem." + i))
                questtargetitem.set(i++, SnFileIO.readItemStackFromYml(ymlfile, name + ".property-inherit.questtargetitem" + i));
        }

        if (ymlfile.contains(name + ".property-set.defaultdistance")) {
            defaultdistance = ymlfile.getInt(name + ".property-set.defaultdistance");
        }

        if (ymlfile.contains(name + ".property-set.questtimelimit"))
            questtimelimit = ymlfile.getInt(name + ".property-set.questtimelimit");

        if (ymlfile.contains(name + ".property-set.questtargetentity")) {
            while (ymlfile.contains(name + ".property-set.questtargetentity." + i)) {
                if (!ymlfile.contains(name + ".property-set.questtargetentity." + i + ".amount") || !ymlfile.contains(name + ".property-set.questtargetentity." + i + ".entitytype")) {
                    sendInfo("[WARNING]读取QuestAction数据错误，数据非法：" + this);
                    sendInfo("[WARNING]读取QuestAction数据错误，数据非法：" + i);
                    continue;
                }
                questtargetentity.put(EntityType.valueOf(Objects.requireNonNull(ymlfile.getString(name + ".property-set.questtargetentity." + i + ".entitytype")).toUpperCase()),
                        ymlfile.getInt(name + ".property-set.questtargetentity." + i + ".amount"));

            }
        }

        if (ymlfile.contains(name + ".property-set.targetpositionx")) {
            if (ymlfile.contains(name + ".property-set.targetpositiony") && ymlfile.contains(name + ".property-set.targetpositionz")) {
                targetpositionx = ymlfile.getInt(name + ".property-set.targetpositionx");
                targetpositiony = ymlfile.getInt(name + ".property-set.targetpositiony");
                targetpositionz = ymlfile.getInt(name + ".property-set.targetpositionz");
                targetlocation.setX(targetpositionx);
                targetlocation.setY(targetpositiony);
                targetlocation.setZ(targetpositionz);

            } else {
                sendInfo("[WARNING]QuestTargetPosition数据可能出现问题！");
            }
        }
        if (ymlfile.contains(name + ".property-set.questtargetnpc")) {
            questtargetnpc = UUID.fromString(Objects.requireNonNull(ymlfile.getString(name + ".property-set.questtargetnpc")));
        }

        return true;
    }

    public void addCollectquesttargetitem(ItemStack a) {
        List<ItemStack> tempis = questtargetitem;
        questtargetitem = new ArrayList<>();
        int i = 0;
        for (ItemStack b :
                tempis) {
            questtargetitem.set(i++, b);
        }
        questtargetitem.set(i, a);
    }

    public void removeCollectquesttargetitem(int index) {
        List<ItemStack> tempis = questtargetitem;
        questtargetitem = new ArrayList<>();
        int i = 0;
        for (ItemStack b :
                tempis) {
            if (i > index) {
                questtargetitem.set(i - 1, b);
                ++i;
            } else questtargetitem.set(i++, b);
        }
    }

    public void removeQuesttargetentity(EntityType key) {
        questtargetentity.remove(key);
    }

    public Location getTargetlocation() {
        return targetlocation;
    }

    public void setTargetlocation(Location targetlocation) {
        this.targetlocation = targetlocation;
    }

    public UUID getQuesttargetnpc() {
        return questtargetnpc;
    }

    public void setQuesttargetnpc(UUID questtargetnpc) {
        this.questtargetnpc = questtargetnpc;
    }

    public String getQuestActndtname() {
        return questActndtname;
    }

    public void setQuestActndtname(String questActndtname) {
        this.questActndtname = questActndtname;
    }

    public void addQuesttargetentity(EntityType key, int value) {
        if (questtargetentity.containsKey(key))
            questtargetentity.replace(key, questtargetentity.get(key) + value);
        else questtargetentity.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestActionData)) return false;
        QuestActionData that = (QuestActionData) o;
        return Double.compare(that.getDefaultdistance(), getDefaultdistance()) == 0 && getQuesttimelimit() == that.getQuesttimelimit() && getTargetpositionx() == that.getTargetpositionx() && getTargetpositiony() == that.getTargetpositiony() && getTargetpositionz() == that.getTargetpositionz() && getQuestActndtname().equals(that.getQuestActndtname()) && getQuesttargetitem().equals(that.getQuesttargetitem()) && getQuesttargetentity().equals(that.getQuesttargetentity()) && getQuesttargetnpc().equals(that.getQuesttargetnpc()) && getTargetlocation().equals(that.getTargetlocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDefaultdistance(), getQuestActndtname(), getQuesttargetitem(), getQuesttimelimit(), getQuesttargetentity(), getQuesttargetnpc(), getTargetpositionx(), getTargetpositiony(), getTargetpositionz(), getTargetlocation());
    }

    public int getQuesttimelimit() {
        return questtimelimit;
    }

    public void setQuesttimelimit(int questtimelimit) {
        this.questtimelimit = questtimelimit;
    }

    public int getTargetpositionx() {
        return targetpositionx;
    }

    public void setTargetpositionx(int targetpositionx) {
        this.targetpositionx = targetpositionx;
    }

    public int getTargetpositiony() {
        return targetpositiony;
    }

    public void setTargetpositiony(int targetpositiony) {
        this.targetpositiony = targetpositiony;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getTargetpositionz() {
        return targetpositionz;
    }

    public void setTargetpositionz(int targetpositionz) {
        this.targetpositionz = targetpositionz;
    }

    @Override
    public String toString() {
        return "QuestActionData{" +
                "defaultdistance=" + defaultdistance +
                ", questActndtname='" + questActndtname + '\'' +
                ", questtargetitem=" + questtargetitem +
                ", questtimelimit=" + questtimelimit +
                ", questtargetentity=" + questtargetentity +
                ", questtargetnpc=" + questtargetnpc +
                ", targetpositionx=" + targetpositionx +
                ", targetpositiony=" + targetpositiony +
                ", targetpositionz=" + targetpositionz +
                ", targetlocation=" + targetlocation +
                '}';
    }

    public List<ItemStack> getQuesttargetitem() {
        return questtargetitem;
    }

    public void setQuesttargetitem(List<ItemStack> questtargetitem) {
        this.questtargetitem = questtargetitem;
    }

    public Map<EntityType, Integer> getQuesttargetentity() {
        return questtargetentity;
    }

    public void setQuesttargetentity(Map<EntityType, Integer> questtargetentity) {
        this.questtargetentity = questtargetentity;
    }

    /**
     * Creates a Map representation of this class.
     * <p>
     * This class must provide a method to restore this class, as defined in
     * the {@link ConfigurationSerializable} interface javadocs.
     *
     * @return Map containing the current state of this class
     */
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("defaultdistance", defaultdistance);
        tmp.put("questActndtname", questActndtname);
        tmp.put("questtargetitem", questtargetitem);
        tmp.put("questtimelimit", questtimelimit);
        tmp.put("questtargetentity", questtargetentity);
        tmp.put("questtargetnpc", questtargetnpc);
        tmp.put("targetpositionx", targetpositionx);
        tmp.put("targetpositiony", targetpositiony);
        tmp.put("targetpositionz", targetpositionz);
        tmp.put("targetlocation", targetlocation);
        return tmp;
    }

    public boolean isLocSet() {
        return this.targetlocation != null;
    }
}
