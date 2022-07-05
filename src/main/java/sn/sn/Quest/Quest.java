package sn.sn.Quest;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import sn.sn.Basic.Other;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

import static sn.sn.Sn.*;

@SerializableAs("SnQuest")
public class Quest implements Cloneable, ConfigurationSerializable, Serializable {

    private int quest_number;
    private String quest_name;
    private QuestPosition questposition = null;
    private QuestType questtype = null;
    private List<QuestAction> quest_accept_condition = new ArrayList<>();
    private int quest_accept_condition_amount = -1;
    private List<QuestAction> quest_target = new ArrayList<>();
    private int quest_target_amount = -1;
    private QuestReward questreward = new QuestReward();
    private List<String> quest_description = new ArrayList<>();
    private int quest_description_line = -1;
    private boolean is_sync = false;
    private boolean on = false;


    public Quest(String name) {
        quest_name = name;
        quest_number = quest_yml.getInt("Amount");//amount比序号大1，所以不用+1
    }

    public Quest(YamlConfiguration ymlfile, String name) {
        quest_name = name;
        quest_number = ymlfile.getInt("Amount");//amount比序号大1，所以不用+1
    }

    public static Boolean loadQuest(Player player, String name) {

        if(!isQuestExist(name)){
            player.sendMessage("任务无法找到，请联系管理员！");
            return false;
        }

        if(!getQuest(name).isOn()){
            if(playerquest_yml.contains(player.getName()+".questenable")){
                player.sendMessage("任务未启动，尝试启动其他任务！");
                addQuest(player,name);
                return loadQuest(player,playerquest_yml.getString(player.getName()+".questenable.0"));
            }
            player.sendMessage("任务启动失败！");
            return false;
        }
        if(!Objects.requireNonNull(playerquest_yml.getString(player.getName() + ".nowquest")).equalsIgnoreCase(name)) {
            int amount = playerquest_yml.getInt(player.getName() + "amount");
            boolean found = false;
            for (int i = 0; i < amount; i++) {
                if (Objects.equals(playerquest_yml.getString(player.getName() + ".questenable." + i), name)) {
                    playerquest_yml.set(player.getName() + ".questenable." + i, playerquest_yml.getString(player.getName() + ".nowquest"));
                    playerquest_yml.set(player.getName() + ".nowquest", name);
                    found = true;
                    break;
                }
            }
            if(!found){
                player.sendMessage("无法找到任务，请联系管理员！");
                return false;
            }
        }

        QuestRuntime a = new QuestRuntime();
        a.runTaskAsynchronously(sn);
        playerquest_yml.set(player.getName()+".nowtaskid",a.getTaskId());

        try {
            playerquest_yml.save(playerquest_file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static Quest getQuest(int id){
        return quests.get(id);
    }

    public static void addQuest(Player player, String name){
        addQuest(player.getName(),name);
    }

    public static void addQuest(OfflinePlayer player, String name){
        addQuest(player.getName(),name);
    }

    public static void addQuest(String playername, String name) {
        if(isQuestExist(name)){
            int amount = playerquest_yml.getInt(playername+".enableamount");
            playerquest_yml.set(playername+".questenable."+amount,name);
            playerquest_yml.set(playername+".enableamount",amount+1);
        }
    }

    public static Boolean isQuestExist(int id){
        return isQuestExist(getQuest(id).getQuestName());
    }

    public static Boolean isQuestExist(String name){
        return isQuestExist(quest_yml,name);
    }

    public static Boolean isQuestExist(YamlConfiguration ymlfile, int id){
        return isQuestExist(quest_yml,getQuest(id).getQuestName());
    }

    public static Boolean isQuestExist(YamlConfiguration ymlfile, String name){
        return ymlfile.contains(name);
    }

    public static Quest getQuest(String name){
        int id = quest_yml.getInt("inside."+name);
        return quests.get(id);
    }

    public boolean isOn() {
        return on;
    }

    public Boolean turnOn() {

        if (isTypeSet() && isTargetSet() && isAcceptConditionSet() || isPositionSet()) {
            this.on = true;
            return true;
        }
        return false;
    }

    public Boolean turnOff() {

        Collection<? extends Player> tmp2 = Bukkit.getOnlinePlayers();
        for (OfflinePlayer tmp_player : Bukkit.getOfflinePlayers()) {
            if (playerquest_yml.getString(tmp_player.getName() + ".nowquest", "").equals(quest_name)) {
                return false;
            }
        }
        for (Player tmp_player : tmp2) {
            if (playerquest_yml.getString(tmp_player.getName() + ".nowquest", "").equals(quest_name)) {
                return false;
            }
        }
        this.on = false;
        return true;
    }

    public void forceOff() {


        Collection<? extends Player> tmp2 = Bukkit.getOnlinePlayers();
        for (OfflinePlayer tmp_player : Bukkit.getOfflinePlayers()) {
            if (playerquest_yml.getString(tmp_player.getName() + ".nowquest", "").equals(quest_name)) {
                addQuest(tmp_player, playerquest_yml.getString(tmp_player.getName() + ".nowquest"));
                playerquest_yml.set(tmp_player.getName() + ".nowquest", null);
                playerquest_yml.set(tmp_player.getName() + ".process", null);
            }
        }
        for (Player tmp_player : tmp2) {
            if (playerquest_yml.getString(tmp_player.getName() + ".nowquest", "").equals(quest_name)) {
                addQuest(tmp_player, playerquest_yml.getString(tmp_player.getName() + ".nowquest"));
                playerquest_yml.set(tmp_player.getName() + ".nowquest", null);
                playerquest_yml.set(tmp_player.getName() + ".process", null);
            }
        }
        this.on = false;
    }

    public boolean isSync() {
        return is_sync;
    }

    public void setSync(boolean is_sync) {
        this.is_sync = is_sync;
    }

    public void succeed(Player winner) {

        if (!this.getQuestReward().give(winner)) {
            return;
        }

        if (this.getQuestPosition().getChildquest() != null) {
            addQuest(winner, this.getQuestPosition().getChildquest());
            addQuest(winner, this.getQuestPosition().getChildquestother1());
            addQuest(winner, this.getQuestPosition().getChildquestother2());
            addQuest(winner, this.getQuestPosition().getChildquestother3());
            loadQuest(winner, this.getQuestPosition().getChildquest());
        }
        addDoneQuest(winner, this);
        resetQuestProcess(winner);
    }

    private void addDoneQuest(Player winner, Quest quest) {
        addDoneQuest(playerquest_yml, winner, quest);
    }

    public void addDoneQuest(YamlConfiguration ymlfile, Player winner, Quest quest) {

        ymlfile.set(winner.getName() + ".doneamount", ymlfile.getInt(winner.getName() + ".doneamount", 0) + 1);
        ymlfile.set(winner.getName() + ".donelist." + (ymlfile.getInt(winner.getName() + ".doneamount") - 1), quest.getQuestNumber());

        ymlfile.set(winner.getName() + ".done." + quest.getQuestName() + ".id", quest.getQuestNumber());
        ymlfile.set(winner.getName() + ".done." + quest.getQuestName() + ".starttime", ymlfile.getString(winner.getName() + ".starttime"));
        Calendar nowc = Calendar.getInstance(), starttime = readCalendarFromString(ymlfile.getString(winner.getName() + ".starttime"));
        String time = recordCalendarToString(nowc);
        double usedtime = starttime.compareTo(nowc) / 1000.0 / 60.0;
        ymlfile.set(winner.getName() + ".done." + quest.getQuestName() + ".endtime", time);
        ymlfile.set(winner.getName() + ".done." + quest.getQuestName() + ".usedtime", usedtime);

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
        return readQuestFromYml(quest_yml, quest_name);
    }

    public void readQuestFromYml(String name) {
        readQuestFromYml(quest_yml, name);
    }

    public Boolean readQuestFromYml(int id) {
        return readQuestFromYml(quest_yml, quest_yml.getString("inside." + id));
    }

    public Boolean readQuestFromYml(YamlConfiguration ymlfile, int id) {
        return readQuestFromYml(ymlfile, ymlfile.getString("inside." + id));
    }

    public Boolean readQuestFromYml(YamlConfiguration ymlfile, String name) {
        if (!ymlfile.contains(name)) {
            Other.sendInfo("[WARNING]读取Quest数据错误，数据不存在");
            return false;
        }

        if (ymlfile.getInt(name + ".tpye") != 1) {
            Other.sendInfo("[WARNING]读取Quest数据错误，该名的类型不正确");
            return false;
        }
        //读取quest position信息
        questposition.readQpFromYml(ymlfile.getString(quest_name + ".property-inherit.questposition"));

        //读取quest type信息
        questtype = QuestType.valueOf(Objects.requireNonNull(ymlfile.getString("property-set.questtype")).toUpperCase(Locale.ROOT));

        //读取quest accept condition 和quest target
        if (ymlfile.contains(quest_name + "property-set.questacceptconditionamount"))
            if (ymlfile.getInt(quest_name + "property-set.questacceptconditionamount") != 0) {
                quest_accept_condition_amount = ymlfile.getInt(quest_name + "property-set.questacceptconditionamount");
                for (int j = 0; j < quest_accept_condition_amount; j++) {
                    if (!ymlfile.contains(quest_name + ".property-inherit.questacceptcondition." + j)) {
                        Other.sendInfo("[WARNING]读取Quest时错误，信息文件可能损坏！");
                        break;
                    }
                    String tname = ymlfile.getString(quest_name + ".property-inherit.questacceptcondition." + j);
                    quest_accept_condition.get(j).readQaFromYml(ymlfile, tname);
                }
            }

        if (ymlfile.contains(quest_name + "property-set.questtargetamount"))
            if (ymlfile.getInt(quest_name + "property-set.questtargetamount") != 0) {
                quest_target_amount = ymlfile.getInt(quest_name + "property-set.questtargetamount");
                for (int j = 0; j < quest_target_amount; j++) {
                    if (!ymlfile.contains(quest_name + ".property-inherit.questtarget." + j)) {
                        Other.sendInfo("[WARNING]读取Quest时错误，信息文件可能损坏！");
                        break;
                    }
                    String tname = ymlfile.getString(quest_name + ".property-inherit.questtarget." + j);
                    quest_target.get(j).readQaFromYml(ymlfile, tname);
                }
            }

        //quest description
        quest_description_line = ymlfile.getInt("property-set.questdescriptionline");
        for (int j = 0; j < this.getQuestDescriptionLine(); j++) {
            quest_description.set(j, ymlfile.getString(quest_name + ".property-set.questdescription." + j));
        }
        //questreward
        questreward.readQrFromYml(ymlfile, ymlfile.getString(quest_name + ".property-inherit.questreward"));

        //isSync
        if (ymlfile.contains(quest_name + ".property-set.issync"))
            is_sync = ymlfile.getBoolean(quest_name + ".property-set.issync");

        is_sync = ymlfile.getBoolean(quest_name + ".property-set.on", false);
        return true;
    }

    public Boolean saveQuestToYml() {
        return saveQuestToYml(quest_yml);
    }

    public Boolean saveQuestToYml(YamlConfiguration ymlfile) {

        if (!ymlfile.contains(quest_name)) {
            ymlfile.set("inside." + quest_name, ymlfile.get("Amount", 0));
            ymlfile.set("inside." + ymlfile.get("Amount", 0), quest_name);
            ymlfile.set("Amount", ymlfile.getInt("Amount", 0) + 1);
        }

        ymlfile.set(quest_name + ".type", 1);
        ymlfile.set(quest_name + ".property-inherit.questposition", questposition.getQuestpositionname());
        questposition.saveQpToYml();
        ymlfile.set(quest_name + ".property-inherit.questreward", questreward.getQuestrewardname());
        questreward.saveQrToYml();

        ymlfile.set(quest_name + ".property-set.questnumber", quest_number);
        ymlfile.set(quest_name + ".property-set.quest_name", quest_name);
        ymlfile.set(quest_name + ".property-set.questtype", questtype.getKey());
        //ymlfile.set(quest_name+".property-inherit","QUEST");
        ymlfile.set(quest_name + ".property-set.questdescriptionline", quest_description_line);
        ymlfile.set(quest_name + ".property-set.questacceptconditionamount", quest_accept_condition_amount);
        ymlfile.set(quest_name + ".property-set.questtargetamount", quest_target_amount);
        ymlfile.set(quest_name + ".property-set.issync", is_sync);
        ymlfile.set(quest_name + ".property-set.on", on);

        for (int i = 0; i < quest_description_line; i++) {
            ymlfile.set(quest_name + ".property-set.questdescription." + i, quest_description.get(i));
        }
        for (int i = 0; i < quest_accept_condition_amount; i++) {
            ymlfile.set(quest_name + ".property-inherit.questacceptcondition." + i, quest_accept_condition.get(i).getQuest_action_name());
            quest_accept_condition.get(i).saveQaToYml(ymlfile);
        }

        for (int i = 0; i < quest_target_amount; i++) {
            ymlfile.set(quest_name + ".property-inherit.questtarget." + i, quest_target.get(i).getQuest_action_name());
            quest_target.get(i).saveQaToYml(ymlfile);
        }

        return true;
    }

    public void addQuestTarget(QuestAction qa) {
        quest_target.add(qa);
    }

    public void addQuestAcceptCondition(QuestAction a) {
        quest_accept_condition.add(a);
    }

    public void addQuestDescription(String a) {
        quest_description.add(a);
    }

    public void removeQuestTarget(int index) {
        quest_target.remove(index);
    }

    public void removeQuestAcceptCondition(int index) {
        quest_accept_condition.remove(index);
    }

    public void removeQuestDescription(int index) {
        quest_description.remove(index);
    }

    public int getQuestNumber() {
        return quest_number;
    }

    public void setQuestNumber(int quest_number) {
        this.quest_number = quest_number;
    }

    public String getQuestName() {
        return quest_name;
    }

    public void setQuestName(String quest_name) {
        this.quest_name = quest_name;
    }

    @Override
    public String toString() {
        return "Quest{" +
                "questnumber=" + quest_number +
                ", quest_name='" + quest_name + '\'' +
                ", questposition=" + questposition +
                ", questtype=" + questtype +
                ", questacceptcondition=" + quest_accept_condition +
                ", questacceptconditionamount=" + quest_accept_condition_amount +
                ", questtarget=" + quest_target +
                ", questtargetamount=" + quest_target_amount +
                ", questreward=" + questreward +
                ", questdescription=" + quest_description +
                ", questdescriptionline=" + quest_description_line +
                ", issync=" + is_sync +
                ", on=" + on +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quest)) return false;
        Quest quest = (Quest) o;
        return getQuestNumber() == quest.getQuestNumber() && getQuestAcceptConditionAmount() == quest.getQuestAcceptConditionAmount() && getQuestTargetAmount() == quest.getQuestTargetAmount() && getQuestDescriptionLine() == quest.getQuestDescriptionLine() && is_sync == quest.is_sync && isOn() == quest.isOn() && getQuestName().equals(quest.getQuestName()) && getQuestPosition().equals(quest.getQuestPosition()) && getQuestType() == quest.getQuestType() && getQuestAcceptCondition().equals(quest.getQuestAcceptCondition()) && getQuest_target().equals(quest.getQuest_target()) && getQuestReward().equals(quest.getQuestReward()) && getQuestDescription().equals(quest.getQuestDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestNumber(), getQuestName(), getQuestPosition(), getQuestType(), getQuestAcceptCondition(), getQuestAcceptConditionAmount(), getQuest_target(), getQuestTargetAmount(), getQuestReward(), getQuestDescription(), getQuestDescriptionLine(), is_sync, isOn());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public List<QuestAction> getQuest_target() {
        return quest_target;
    }

    public void setQuestTarget(List<QuestAction> quest_target) {
        quest_target_amount = quest_target.size();
        this.quest_target = quest_target;
    }

    public QuestPosition getQuestPosition() {
        return questposition;
    }

    public void setQuestPosition(QuestPosition questposition) {
        this.questposition = questposition;
    }

    public QuestReward getQuestReward() {
        return questreward;
    }

    public void setQuestReward(QuestReward questreward) {
        this.questreward = questreward;
    }

    public List<String> getQuestDescription() {
        return quest_description;
    }

    public void setQuestDescription(List<String> quest_description) {
        quest_description_line = quest_description.size();
        this.quest_description = quest_description;
    }

    public QuestType getQuestType() {
        return questtype;
    }

    public void setQuestType(QuestType questtype) {
        this.questtype = questtype;
    }

    public List<QuestAction> getQuestAcceptCondition() {
        return quest_accept_condition;
    }

    public void setQuestAcceptCondition(List<QuestAction> quest_accept_condition) {
        quest_accept_condition_amount = quest_accept_condition.size();
        this.quest_accept_condition = quest_accept_condition;
    }

    public int getQuestAcceptConditionAmount() {
        return quest_accept_condition_amount;
    }

    public int getQuestTargetAmount() {
        return quest_target_amount;
    }

    public int getQuestDescriptionLine() {
        return quest_description_line;
    }

    private void setQuestDescriptionLine(int quest_description_line) {
        this.quest_description_line = quest_description_line;
    }

    public boolean isTypeSet() {
        return questtype != null;
    }

    public boolean isAcceptConditionSet() {
        return quest_accept_condition.size() != 0;
    }

    public boolean isTargetSet() {
        return quest_target.size() != 0;
    }

    public boolean isDescriptionSet() {
        return quest_description != null;
    }

    public boolean isPositionSet() {
        return questposition.getQuestpositionname() != null;
    }

    public boolean isRewardSet() {
        return questreward != null;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("questnumber", quest_number);
        tmp.put("quest_name", quest_name);
        tmp.put("questposition", questposition);
        tmp.put("questtype", questtype);
        tmp.put("questacceptcondition", quest_accept_condition);
        tmp.put("questacceptconditionamount", quest_accept_condition_amount);
        tmp.put("questtarget", quest_target);
        tmp.put("questtargetamount", quest_target_amount);
        tmp.put("questreward", questreward);
        tmp.put("questdescription", quest_description);
        tmp.put("questdescriptionline", quest_description_line);
        tmp.put("issync", is_sync);
        tmp.put("on", on);

        return tmp;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(quest_number);
        out.writeObject(quest_name);
        out.writeObject(questposition);
        out.writeObject(questtype);
        out.writeObject(quest_accept_condition_amount);
        for (QuestAction questAction : quest_accept_condition) {
            out.writeObject(questAction);
        }
        out.writeObject(quest_target_amount);
        for (QuestAction questAction : quest_target) {
            out.writeObject(questAction);
        }
        out.writeObject(questreward);
        out.writeObject(quest_description_line);
        for (String s : quest_description) {
            out.writeObject(s);
        }
        out.writeObject(is_sync);
        out.writeObject(on);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        quest_number = (int) in.readObject();
        quest_name = (String) in.readObject();
        questposition = (QuestPosition) in.readObject();
        questtype = (QuestType) in.readObject();
        quest_accept_condition_amount = (int) in.readObject();
        for (int i = 0; i < quest_accept_condition_amount; i++) {
            quest_accept_condition.add((QuestAction) in.readObject());
        }

        quest_target_amount = (int) in.readObject();
        for (int i = 0; i < quest_accept_condition_amount; i++) {
            quest_target.add( (QuestAction) in.readObject());
        }

        questreward = (QuestReward) in.readObject();
        quest_description_line = (int) in.readObject();
        for (int i = 0; i < quest_description_line; i++) {
            quest_description.add((String) in.readObject());
        }

        is_sync = (boolean) in.readObject();
        on = (boolean) in.readObject();
    }

    private void readObjectNoData() throws ObjectStreamException {
        String name = "NewQuest";
        quest_name = name;
        quest_number = quest_yml.getInt("Amount");//amount比序号大1，所以不用+1
        quest_yml.set("Amount", quest_number + 1);//计数
        quest_yml.set("inside." + name, quest_number);
        quest_yml.set("inside." + quest_number, name);
    }


}
