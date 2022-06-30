package sn.sn;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static sn.sn.Sn.quest_yml;
import static sn.sn.Sn.sendInfo;

@SerializableAs("SnQuestPosition")
public class QuestPosition implements Cloneable, ConfigurationSerializable, Serializable {
    private int questlevel;
    private String parentquest;
    private String childquestother1;
    private String childquestother2;
    private String childquestother3;
    private String childquest;
    private String questpositionname;

    public QuestPosition() {
        questlevel = -1;
        parentquest = null;
        childquestother1 = null;
        childquestother2 = null;
        childquestother3 = null;
        childquest = null;
        questpositionname = "QuestPosition" + new Random().nextInt(99999);
    }

    public QuestPosition(String name) {
        questlevel = -1;
        parentquest = null;
        childquestother1 = null;
        childquestother2 = null;
        childquestother3 = null;
        childquest = null;
        questpositionname = name;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(questlevel);
        out.writeObject(parentquest);
        out.writeObject(childquestother1);
        out.writeObject(childquestother2);
        out.writeObject(childquestother3);
        out.writeObject(childquest);
        out.writeObject(questpositionname);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        questlevel = (int) in.readObject();
        parentquest = (String) in.readObject();
        childquestother1 = (String) in.readObject();
        childquestother2 = (String) in.readObject();
        childquestother3 = (String) in.readObject();
        childquest = (String) in.readObject();
        questpositionname = (String) in.readObject();
    }

    private void readObjectNoData() throws ObjectStreamException {
        new QuestPosition();
    }


    public Boolean readQpFromYml(String name) {
        return readQpFromYml(quest_yml, name);
    }

    public Boolean readQpFromYml(YamlConfiguration ymlfile, String name) {

        if (!ymlfile.contains(name)) {
            sendInfo("[WARNING]读取QuestPosition数据错误，数据不存在");
            return false;
        }

        if (ymlfile.getInt(name + ".tpye") != 2) {
            sendInfo("[WARNING]读取QuestPosition数据错误，该名的类型不正确");
            return false;
        }
        parentquest = ymlfile.getString(name + ".property-set.parentquest");
        childquest = ymlfile.getString(name + ".property-set.childquest");
        childquestother1 = ymlfile.getString(name + ".property-set.childquestother1");
        childquestother2 = ymlfile.getString(name + ".property-set.childquestother2");
        childquestother3 = ymlfile.getString(name + ".property-set.childquestother3");
        questlevel = ymlfile.getInt(name + ".property-set.questlevel");
        return true;
    }

    public void saveQpToYml() {
        saveQpToYml(quest_yml, questpositionname);
    }

    public void saveQpToYml(String name) {
        saveQpToYml(quest_yml, name);
    }

    public void saveQpToYml(YamlConfiguration ymlfile, String name) {
        ymlfile.set(name + ".type", 2);
        ymlfile.set(name + ".property-set.parentquest", parentquest);
        ymlfile.set(name + ".property-set.childquest", childquest);
        ymlfile.set(name + ".property-set.childquestother1", childquestother1);
        ymlfile.set(name + ".property-set.childquestother2", childquestother2);
        ymlfile.set(name + ".property-set.childquestother3", childquestother3);
        ymlfile.set(name + ".property-set.questlevel", questlevel);

    }

    public String getQuestpositionname() {
        return questpositionname;
    }

    public void setQuestpositionname(String questpositionname) {
        this.questpositionname = questpositionname;
    }

    public int getQuestlevel() {
        return questlevel;
    }

    public void setQuestlevel(int questlevel) {
        this.questlevel = questlevel;
    }

    public String getParentquest() {
        return parentquest;
    }

    public void setParentquest(String parentquest) {
        this.parentquest = parentquest;
    }

    public String getChildquest() {
        return childquest;
    }

    public void setChildquest(String childquest) {
        this.childquest = childquest;
    }

    public String getChildquestother1() {
        return childquestother1;
    }

    public void setChildquestother1(String childquestother1) {
        this.childquestother1 = childquestother1;
    }

    public String getChildquestother2() {
        return childquestother2;
    }

    public void setChildquestother2(String childquestother2) {
        this.childquestother2 = childquestother2;
    }

    public String getChildquestother3() {
        return childquestother3;
    }

    public void setChildquestother3(String childquestother3) {
        this.childquestother3 = childquestother3;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "QuestPosition{" +
                "questlevel=" + questlevel +
                ", parentquest='" + parentquest + '\'' +
                ", childquestother1='" + childquestother1 + '\'' +
                ", childquestother2='" + childquestother2 + '\'' +
                ", childquestother3='" + childquestother3 + '\'' +
                ", childquest='" + childquest + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestPosition)) return false;
        QuestPosition that = (QuestPosition) o;
        return getQuestlevel() == that.getQuestlevel() && getParentquest().equals(that.getParentquest()) && Objects.equals(getChildquestother1(), that.getChildquestother1()) && Objects.equals(getChildquestother2(), that.getChildquestother2()) && Objects.equals(getChildquestother3(), that.getChildquestother3()) && Objects.equals(getChildquest(), that.getChildquest());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestlevel(), getParentquest(), getChildquestother1(), getChildquestother2(), getChildquestother3(), getChildquest());
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
        tmp.put("questlevel", questlevel);
        tmp.put("parentquest", parentquest);
        tmp.put("childquestother1", childquestother1);
        tmp.put("childquestother2", childquestother2);
        tmp.put("childquestother3", childquestother3);
        tmp.put("childquest", childquest);
        tmp.put("questpositionname", questpositionname);
        return tmp;
    }



    /*任务等级：（int）
        主线（1）
        主线的支线（2）
        主线的支线的支线（3）
        ....

    父任务：（New 任务）

    子任务集：(New 任务[]）
    */
}
