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

@SerializableAs("SnQuestAction")
public class QuestAction implements Cloneable, ConfigurationSerializable, Serializable {

    private String questactionname;
    private QuestActionType questactiontype;
    private QuestActionData questactiondata;

    public QuestAction(QuestActionData a) {
        questactionname = a.getQuestActndtname();
        questactiondata = a;
        questactiontype = QuestActionType.ACCOMPLISHMENT;
    }

    public QuestAction() {
        questactiontype = QuestActionType.ACCOMPLISHMENT;
        questactionname = "QuestAction" + new Random().nextInt(99999);
        questactiondata = new QuestActionData(questactionname);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(questactionname);
        out.writeObject(questactiontype.getKey().getKey());
        out.writeObject(questactiondata);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        questactionname = (String) in.readObject();
        questactiontype = QuestActionType.valueOf((String) in.readObject());
        questactiondata = (QuestActionData) in.readObject();

    }

    private void readObjectNoData() throws ObjectStreamException {
        new QuestAction();
    }

    public boolean readQaFromYml(String name) {
        return readQaFromYml(quest_yml, name);
    }

    public boolean readQaFromYml(YamlConfiguration ymlfile, String name) {
        if (!ymlfile.contains(name)) {
            sendInfo("[WARNING]读取QuestAction数据错误，数据不存在");
            return false;
        }

        if (ymlfile.getInt(name + ".tpye") != 3) {
            sendInfo("[WARNING]读取QuestAction数据错误，该名的类型不正确");
            return false;
        }
        questactionname = name;
        questactiondata.readQaDataFromYml(ymlfile, ymlfile.getString(name + ".property-inherit.questactiondata"));
        questactiontype = QuestActionType.valueOf(Objects.requireNonNull(ymlfile.getString(name + ".property-inherit.questactiontype")).toUpperCase());
        return true;
    }

    public void saveQaToYml() {
        saveQaToYml(quest_yml);
    }

    public void saveQaToYml(YamlConfiguration ymlfile) {
        ymlfile.set(questactionname + ".type", 3);
        ymlfile.set(questactionname + ".property-inherit.questactiontype", questactiontype.getKey().getKey());
        ymlfile.set(questactionname + ".property-inherit.questactiondata", questactiondata.getQuestActndtname());
        questactiondata.saveQaDataToYml(ymlfile);
    }

    public String getQuestactionname() {
        return questactionname;
    }

    public void setQuestactionname(String questactionname) {
        this.questactionname = questactionname;
    }

    public QuestActionType getQuestactiontype() {
        return questactiontype;
    }

    public void setQuestactiontype(QuestActionType questactiontype) {
        this.questactiontype = questactiontype;
    }

    public QuestActionData getQuestactiondata() {
        return questactiondata;
    }

    public void setQuestactiondata(QuestActionData questactiondata) {
        this.questactiondata = questactiondata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestAction)) return false;
        QuestAction that = (QuestAction) o;
        return getQuestactiontype() == that.getQuestactiontype() && getQuestactiondata().equals(that.getQuestactiondata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestactiontype(), getQuestactiondata());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "QuestAction{" +
                "questactiontype=" + questactiontype +
                ", questactiondata=" + questactiondata +
                '}';
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
        tmp.put("questactionname", questactionname);
        tmp.put("questactiontype", questactiontype);
        tmp.put("questactiondata", questactiondata);
        return tmp;
    }
}
