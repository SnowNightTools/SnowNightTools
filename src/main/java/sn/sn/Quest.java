package sn.sn;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

import static sn.sn.Sn.*;

@SerializableAs("SnQuest")
public class Quest implements Cloneable, ConfigurationSerializable, Serializable {
    private int questnumber;
    private String questname;
    private QuestPosition questposition = null;
    private QuestType questtype = null;
    private List<QuestAction> questacceptcondition = new ArrayList<>();
    private int questacceptconditionamount = -1;
    private List<QuestAction> questtarget = new ArrayList<>();
    private int questtargetamount = -1;
    private QuestReward questreward = new QuestReward();
    private List<String> questdescription = new ArrayList<>();
    private int questdescriptionline = -1;
    private boolean issync = false;
    private boolean on = false;


    public Quest(String name) {
        questname = name;
        questnumber = quest_yml.getInt("Amount");//amount比序号大1，所以不用+1
    }

    public Quest(YamlConfiguration ymlfile, String name) {
        questname = name;
        questnumber = ymlfile.getInt("Amount");//amount比序号大1，所以不用+1
    }

    public boolean isOn() {
        return on;
    }

    public Boolean turnOn() {

        if (isTypeSet() && isTargetSet() && isAcceptconditionSet() || isPositionSet()) {
            this.on = true;
            return true;
        }
        return false;
    }

    public Boolean turnOff() {

        Collection<? extends Player> tmp2 = Bukkit.getOnlinePlayers();
        for (OfflinePlayer tmpplayer : Bukkit.getOfflinePlayers()) {
            if (playerquest_yml.getString(tmpplayer.getName() + ".nowquest", "").equals(questname)) {
                return false;
            }
        }
        for (Player tmpplayer : tmp2) {
            if (playerquest_yml.getString(tmpplayer.getName() + ".nowquest", "").equals(questname)) {
                return false;
            }
        }
        this.on = false;
        return true;
    }

    public void forceOff() {


        Collection<? extends Player> tmp2 = Bukkit.getOnlinePlayers();
        for (OfflinePlayer tmpplayer : Bukkit.getOfflinePlayers()) {
            if (playerquest_yml.getString(tmpplayer.getName() + ".nowquest", "").equals(questname)) {
                Quest_CE.addQuest(tmpplayer, playerquest_yml.getString(tmpplayer.getName() + ".nowquest"));
                playerquest_yml.set(tmpplayer.getName() + ".nowquest", null);
                playerquest_yml.set(tmpplayer.getName() + ".process", null);
            }
        }
        for (Player tmpplayer : tmp2) {
            if (playerquest_yml.getString(tmpplayer.getName() + ".nowquest", "").equals(questname)) {
                Quest_CE.addQuest(tmpplayer, playerquest_yml.getString(tmpplayer.getName() + ".nowquest"));
                playerquest_yml.set(tmpplayer.getName() + ".nowquest", null);
                playerquest_yml.set(tmpplayer.getName() + ".process", null);
            }
        }
        this.on = false;
    }

    public boolean isSync() {
        return issync;
    }

    public void setSync(boolean issync) {
        this.issync = issync;
    }

    public void succeed(Player winner) {

        if (!this.getQuestreward().give(winner)) {
            return;
        }

        if (this.getQuestposition().getChildquest() != null) {
            Quest_CE.addQuest(winner, this.getQuestposition().getChildquest());
            Quest_CE.addQuest(winner, this.getQuestposition().getChildquestother1());
            Quest_CE.addQuest(winner, this.getQuestposition().getChildquestother2());
            Quest_CE.addQuest(winner, this.getQuestposition().getChildquestother3());
            Quest_CE.loadQuest(winner, this.getQuestposition().getChildquest());
        }
        addDoneQuest(winner, this);
        resetQuestProcess(winner);
    }

    private void addDoneQuest(Player winner, Quest quest) {
        addDoneQuest(playerquest_yml, winner, quest);
    }

    public void addDoneQuest(YamlConfiguration ymlfile, Player winner, Quest quest) {

        ymlfile.set(winner.getName() + ".doneamount", ymlfile.getInt(winner.getName() + ".doneamount", 0) + 1);
        ymlfile.set(winner.getName() + ".donelist." + (ymlfile.getInt(winner.getName() + ".doneamount") - 1), quest.getQuestnumber());

        ymlfile.set(winner.getName() + ".done." + quest.getQuestname() + ".id", quest.getQuestnumber());
        ymlfile.set(winner.getName() + ".done." + quest.getQuestname() + ".starttime", ymlfile.getString(winner.getName() + ".starttime"));
        Calendar nowc = Calendar.getInstance(), starttime = readCalendarFromString(ymlfile.getString(winner.getName() + ".starttime"));
        String time = recordCalendarToString(nowc);
        double usedtime = starttime.compareTo(nowc) / 1000.0 / 60.0;
        ymlfile.set(winner.getName() + ".done." + quest.getQuestname() + ".endtime", time);
        ymlfile.set(winner.getName() + ".done." + quest.getQuestname() + ".usedtime", usedtime);

    }

    private String recordCalendarToString(Calendar nowc) {
        return nowc.get(Calendar.YEAR) + '.' + nowc.get(Calendar.MONTH) + '.' + '.' + nowc.get(Calendar.DATE) + '_' + nowc.get(Calendar.HOUR_OF_DAY) + ':' + nowc.get(Calendar.MINUTE) + "::" + nowc.get(Calendar.SECOND);
    }

    private Calendar readCalendarFromString(String str) {
        Calendar time = null;
        //str = "year.mon..day_hour:min::sec"

        int year = Integer.parseInt(str, 0, str.indexOf("."), 10);
        int mon = Integer.parseInt(str, str.indexOf(".") + 1, str.indexOf(".."), 10);
        int day = Integer.parseInt(str, str.indexOf("..") + 2, str.indexOf("_"), 10);
        int hour = Integer.parseInt(str, str.indexOf("_") + 1, str.indexOf(":"), 10);
        int min = Integer.parseInt(str, str.indexOf(":") + 1, str.indexOf("::"), 10);
        int sec = Integer.parseInt(str, str.indexOf("::") + 2, str.length(), 10);

        assert false;
        //noinspection MagicConstant
        time.set(year, (mon - 1), day, hour, min, sec);

        return time;
    }

    private void resetQuestProcess(Player winner) {
        resetQuestProcess(playerquest_yml, winner);
    }

    public void resetQuestProcess(YamlConfiguration ymlfile, Player winner) {
        if (ymlfile.contains(winner.getName() + ".process"))
            ymlfile.set(winner.getName() + ".process", null);
    }

    public Boolean readQuestFromYml() {
        return readQuestFromYml(quest_yml, questname);
    }

    public Boolean readQuestFromYml(String name) {
        return readQuestFromYml(quest_yml, name);
    }

    public Boolean readQuestFromYml(int id) {
        return readQuestFromYml(quest_yml, quest_yml.getString("inside." + id));
    }

    public Boolean readQuestFromYml(YamlConfiguration ymlfile, int id) {
        return readQuestFromYml(ymlfile, ymlfile.getString("inside." + id));
    }

    public Boolean readQuestFromYml(YamlConfiguration ymlfile, String name) {
        if (!ymlfile.contains(name)) {
            sendInfo("[WARNING]读取Quest数据错误，数据不存在");
            return false;
        }

        if (ymlfile.getInt(name + ".tpye") != 1) {
            sendInfo("[WARNING]读取Quest数据错误，该名的类型不正确");
            return false;
        }
        //读取quest position信息
        questposition.readQpFromYml(ymlfile.getString(questname + ".property-inherit.questposition"));

        //读取quest type信息
        questtype = QuestType.valueOf(Objects.requireNonNull(ymlfile.getString("property-set.questtype")).toUpperCase(Locale.ROOT));

        //读取quest accept condition 和quest target
        if (ymlfile.contains(questname + "property-set.questacceptconditionamount"))
            if (ymlfile.getInt(questname + "property-set.questacceptconditionamount") != 0) {
                questacceptconditionamount = ymlfile.getInt(questname + "property-set.questacceptconditionamount");
                for (int j = 0; j < questacceptconditionamount; j++) {
                    if (!ymlfile.contains(questname + ".property-inherit.questacceptcondition." + j)) {
                        sendInfo("[WARNING]读取Quest时错误，信息文件可能损坏！");
                        break;
                    }
                    String tname = ymlfile.getString(questname + ".property-inherit.questacceptcondition." + j);
                    questacceptcondition.get(j).readQaFromYml(ymlfile, tname);
                }
            }

        if (ymlfile.contains(questname + "property-set.questtargetamount"))
            if (ymlfile.getInt(questname + "property-set.questtargetamount") != 0) {
                questtargetamount = ymlfile.getInt(questname + "property-set.questtargetamount");
                for (int j = 0; j < questtargetamount; j++) {
                    if (!ymlfile.contains(questname + ".property-inherit.questtarget." + j)) {
                        sendInfo("[WARNING]读取Quest时错误，信息文件可能损坏！");
                        break;
                    }
                    String tname = ymlfile.getString(questname + ".property-inherit.questtarget." + j);
                    questtarget.get(j).readQaFromYml(ymlfile, tname);
                }
            }

        //quest description
        questdescriptionline = ymlfile.getInt("property-set.questdescriptionline");
        for (int j = 0; j < this.getQuestdescriptionline(); j++) {
            questdescription.set(j, ymlfile.getString(questname + ".property-set.questdescription." + j));
        }
        //questreward
        questreward.readQrFromYml(ymlfile, ymlfile.getString(questname + ".property-inherit.questreward"));

        //isSync
        if (ymlfile.contains(questname + ".property-set.issync"))
            issync = ymlfile.getBoolean(questname + ".property-set.issync");

        issync = ymlfile.getBoolean(questname + ".property-set.on", false);
        return true;
    }

    public Boolean saveQuestToYml() {
        return saveQuestToYml(quest_yml);
    }

    public Boolean saveQuestToYml(YamlConfiguration ymlfile) {

        if (!ymlfile.contains(questname)) {
            ymlfile.set("inside." + questname, ymlfile.get("Amount", 0));
            ymlfile.set("inside." + ymlfile.get("Amount", 0), questname);
            ymlfile.set("Amount", ymlfile.getInt("Amount", 0) + 1);
        }

        ymlfile.set(questname + ".type", 1);
        ymlfile.set(questname + ".property-inherit.questposition", questposition.getQuestpositionname());
        questposition.saveQpToYml();
        ymlfile.set(questname + ".property-inherit.questreward", questreward.getQuestrewardname());
        questreward.saveQrToYml();

        ymlfile.set(questname + ".property-set.questnumber", questnumber);
        ymlfile.set(questname + ".property-set.questname", questname);
        ymlfile.set(questname + ".property-set.questtype", questtype.getKey());
        //ymlfile.set(questname+".property-inherit","QUEST");
        ymlfile.set(questname + ".property-set.questdescriptionline", questdescriptionline);
        ymlfile.set(questname + ".property-set.questacceptconditionamount", questacceptconditionamount);
        ymlfile.set(questname + ".property-set.questtargetamount", questtargetamount);
        ymlfile.set(questname + ".property-set.issync", issync);
        ymlfile.set(questname + ".property-set.on", on);

        for (int i = 0; i < questdescriptionline; i++) {
            ymlfile.set(questname + ".property-set.questdescription." + i, questdescription.get(i));
        }
        for (int i = 0; i < questacceptconditionamount; i++) {
            ymlfile.set(questname + ".property-inherit.questacceptcondition." + i, questacceptcondition.get(i).getQuestactionname());
            questacceptcondition.get(i).saveQaToYml(ymlfile);
        }

        for (int i = 0; i < questtargetamount; i++) {
            ymlfile.set(questname + ".property-inherit.questtarget." + i, questtarget.get(i).getQuestactionname());
            questtarget.get(i).saveQaToYml(ymlfile);
        }

        return true;
    }

    public void addQuesttarget(QuestAction qa) {
        questtarget.add(qa);
    }

    public void addQuestacceptcondition(QuestAction a) {
        questacceptcondition.add(a);
    }

    public void addQuestdescription(String a) {
        questdescription.add(a);
    }

    public void removeQuesttarget(int index) {
        questtarget.remove(index);
    }

    public void removeQuestacceptcondition(int index) {
        questacceptcondition.remove(index);
    }

    public void removeQuestdescription(int index) {
        questdescription.remove(index);
    }

    public int getQuestnumber() {
        return questnumber;
    }

    public void setQuestnumber(int questnumber) {
        this.questnumber = questnumber;
    }

    public String getQuestname() {
        return questname;
    }

    public void setQuestname(String questname) {
        this.questname = questname;
    }

    @Override
    public String toString() {
        return "Quest{" +
                "questnumber=" + questnumber +
                ", questname='" + questname + '\'' +
                ", questposition=" + questposition +
                ", questtype=" + questtype +
                ", questacceptcondition=" + questacceptcondition +
                ", questacceptconditionamount=" + questacceptconditionamount +
                ", questtarget=" + questtarget +
                ", questtargetamount=" + questtargetamount +
                ", questreward=" + questreward +
                ", questdescription=" + questdescription +
                ", questdescriptionline=" + questdescriptionline +
                ", issync=" + issync +
                ", on=" + on +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quest)) return false;
        Quest quest = (Quest) o;
        return getQuestnumber() == quest.getQuestnumber() && getQuestacceptconditionamount() == quest.getQuestacceptconditionamount() && getQuesttargetamount() == quest.getQuesttargetamount() && getQuestdescriptionline() == quest.getQuestdescriptionline() && issync == quest.issync && isOn() == quest.isOn() && getQuestname().equals(quest.getQuestname()) && getQuestposition().equals(quest.getQuestposition()) && getQuesttype() == quest.getQuesttype() && getQuestacceptcondition().equals(quest.getQuestacceptcondition()) && getQuesttarget().equals(quest.getQuesttarget()) && getQuestreward().equals(quest.getQuestreward()) && getQuestdescription().equals(quest.getQuestdescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestnumber(), getQuestname(), getQuestposition(), getQuesttype(), getQuestacceptcondition(), getQuestacceptconditionamount(), getQuesttarget(), getQuesttargetamount(), getQuestreward(), getQuestdescription(), getQuestdescriptionline(), issync, isOn());
    }

    public List<QuestAction> getQustAccptCndtn() {
        return questacceptcondition;
    }

    public QuestType getQuestType() {
        return questtype;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public List<QuestAction> getQuesttarget() {
        return questtarget;
    }

    public void setQuesttarget(List<QuestAction> questtarget) {
        questtargetamount = questtarget.size();
        this.questtarget = questtarget;
    }

    public int getQuestacceptconditionamount() {
        return questacceptconditionamount;
    }

    private void setQuestacceptconditionamount(int questacceptconditionamount) {
        this.questacceptconditionamount = questacceptconditionamount;
    }

    public int getQuesttargetamount() {
        return questtargetamount;
    }

    private void setQuesttargetamount(int questtargetamount) {
        this.questtargetamount = questtargetamount;
    }

    public QuestPosition getQuestposition() {
        return questposition;
    }

    public void setQuestposition(QuestPosition questposition) {
        this.questposition = questposition;
    }

    public QuestReward getQuestreward() {
        return questreward;
    }

    public void setQuestreward(QuestReward questreward) {
        this.questreward = questreward;
    }

    public List<String> getQuestdescription() {
        return questdescription;
    }

    public void setQuestdescription(List<String> questdescription) {
        questdescriptionline = questdescription.size();
        this.questdescription = questdescription;
    }

    public QuestType getQuesttype() {
        return questtype;
    }

    public void setQuesttype(QuestType questtype) {
        this.questtype = questtype;
    }

    public List<QuestAction> getQuestacceptcondition() {
        return questacceptcondition;
    }

    public void setQuestacceptcondition(List<QuestAction> questacceptcondition) {
        questacceptconditionamount = questacceptcondition.size();
        this.questacceptcondition = questacceptcondition;
    }

    public int getQuestAcceptConditionAmount() {
        return questacceptconditionamount;
    }

    public int getQuestTargetAmount() {
        return questtargetamount;
    }

    public int getQuestdescriptionline() {
        return questdescriptionline;
    }

    private void setQuestdescriptionline(int questdescriptionline) {
        this.questdescriptionline = questdescriptionline;
    }

    public boolean isTypeSet() {
        return questtype != null;
    }

    public boolean isAcceptconditionSet() {
        return questacceptcondition.size() != 0;
    }

    public boolean isTargetSet() {
        return questtarget.size() != 0;
    }

    public boolean isDescriptionSet() {
        return questdescription != null;
    }

    public boolean isPositionSet() {
        return questposition.getQuestpositionname() != null;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("questnumber", questnumber);
        tmp.put("questname", questname);
        tmp.put("questposition", questposition);
        tmp.put("questtype", questtype);
        tmp.put("questacceptcondition", questacceptcondition);
        tmp.put("questacceptconditionamount", questacceptconditionamount);
        tmp.put("questtarget", questtarget);
        tmp.put("questtargetamount", questtargetamount);
        tmp.put("questreward", questreward);
        tmp.put("questdescription", questdescription);
        tmp.put("questdescriptionline", questdescriptionline);
        tmp.put("issync", issync);
        tmp.put("on", on);

        return tmp;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(questnumber);
        out.writeObject(questname);
        out.writeObject(questposition);
        out.writeObject(questtype);
        out.writeObject(questacceptcondition);
        out.writeObject(questacceptconditionamount);
        out.writeObject(questtarget);
        out.writeObject(questtargetamount);
        out.writeObject(questreward);
        out.writeObject(questdescription);
        out.writeObject(questdescriptionline);
        out.writeObject(issync);
        out.writeObject(on);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        questnumber = (int) in.readObject();
        questname = (String) in.readObject();
        questposition = (QuestPosition) in.readObject();
        questtype = (QuestType) in.readObject();
        questacceptcondition = (List<QuestAction>) in.readObject();
        questacceptconditionamount = (int) in.readObject();
        questtarget = (List<QuestAction>) in.readObject();
        questtargetamount = (int) in.readObject();
        questreward = (QuestReward) in.readObject();
        questdescription = (List<String>) in.readObject();
        questdescriptionline = (int) in.readObject();
        issync = (boolean) in.readObject();
        on = (boolean) in.readObject();
    }

    private void readObjectNoData() throws ObjectStreamException {
        String name = "NewQuest";
        questname = name;
        questnumber = quest_yml.getInt("Amount");//amount比序号大1，所以不用+1
        quest_yml.set("Amount", questnumber + 1);//计数
        quest_yml.set("inside." + name, questnumber);
        quest_yml.set("inside." + questnumber, name);
    }

    public boolean isRewardset() {
        return questreward != null;
    }
}
