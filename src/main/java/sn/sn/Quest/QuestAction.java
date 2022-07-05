package sn.sn.Quest;

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
import static sn.sn.Basic.Other.sendInfo;

@SerializableAs("SnQuestAction")
public class QuestAction implements Cloneable, ConfigurationSerializable, Serializable {

    private String quest_action_name;
    private QuestActionType quest_action_type;
    private QuestActionData quest_action_data;

    public QuestAction(QuestActionData a) {
        quest_action_name = a.getQuestActndtname();
        quest_action_data = a;
        quest_action_type = QuestActionType.ACCOMPLISHMENT;
    }

    public QuestAction() {
        quest_action_type = QuestActionType.ACCOMPLISHMENT;
        quest_action_name = "QuestAction" + new Random().nextInt(99999);
        quest_action_data = new QuestActionData(quest_action_name);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(quest_action_name);
        out.writeObject(quest_action_type.getKey().getKey());
        out.writeObject(quest_action_data);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        quest_action_name = (String) in.readObject();
        quest_action_type = QuestActionType.valueOf((String) in.readObject());
        quest_action_data = (QuestActionData) in.readObject();

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
        quest_action_name = name;
        quest_action_data.readQaDataFromYml(ymlfile, ymlfile.getString(name + ".property-inherit.quest_action_data"));
        quest_action_type = QuestActionType.valueOf(Objects.requireNonNull(ymlfile.getString(name + ".property-inherit.quest_action_type")).toUpperCase());
        return true;
    }

    public void saveQaToYml() {
        saveQaToYml(quest_yml);
    }

    public void saveQaToYml(YamlConfiguration ymlfile) {
        ymlfile.set(quest_action_name + ".type", 3);
        ymlfile.set(quest_action_name + ".property-inherit.quest_action_type", quest_action_type.getKey().getKey());
        ymlfile.set(quest_action_name + ".property-inherit.quest_action_data", quest_action_data.getQuestActndtname());
        quest_action_data.saveQaDataToYml(ymlfile);
    }

    public String getQuest_action_name() {
        return quest_action_name;
    }

    public void setQuest_action_name(String quest_action_name) {
        this.quest_action_name = quest_action_name;
    }

    public QuestActionType getQuest_action_type() {
        return quest_action_type;
    }

    public void setQuest_action_type(QuestActionType quest_action_type) {
        this.quest_action_type = quest_action_type;
    }

    public QuestActionData getQuest_action_data() {
        return quest_action_data;
    }

    public void setQuest_action_data(QuestActionData quest_action_data) {
        this.quest_action_data = quest_action_data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestAction)) return false;
        QuestAction that = (QuestAction) o;
        return getQuest_action_type() == that.getQuest_action_type() && getQuest_action_data().equals(that.getQuest_action_data());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuest_action_type(), getQuest_action_data());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "QuestAction{" +
                "quest_action_type=" + quest_action_type +
                ", quest_action_data=" + quest_action_data +
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
        tmp.put("quest_action_name", quest_action_name);
        tmp.put("quest_action_type", quest_action_type);
        tmp.put("quest_action_data", quest_action_data);
        return tmp;
    }
}
