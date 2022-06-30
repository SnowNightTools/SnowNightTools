package sn.sn;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;

import static sn.sn.Sn.*;

@SuppressWarnings("unused")
@SerializableAs("SnQuestReward")
public class QuestReward implements Cloneable, ConfigurationSerializable, Serializable {
    private String questrewardname;
    private double rewardmoney = 0;
    private List<ItemStack> rewarditems = new ArrayList<>();
    private int rewarditemamount;
    private List<String> rewardpermission = new ArrayList<>();
    private int rewardpermissionamount;
    private boolean isadmin = false;

    public QuestReward() {
        rewarditemamount = 0;
        rewardpermissionamount = 0;
        questrewardname = "QuestReward" + new Random().nextInt(99999);
    }

    public QuestReward(String name) {
        rewarditemamount = 0;
        rewardpermissionamount = 0;
        questrewardname = name;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(questrewardname);
        out.writeObject(rewardmoney);
        out.writeObject(rewarditems);
        out.writeObject(rewarditemamount);
        out.writeObject(rewardpermission);
        out.writeObject(rewardpermissionamount);
        out.writeObject(isadmin);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        questrewardname = (String) in.readObject();
        rewardmoney = (double) in.readObject();
        rewarditems = (List<ItemStack>) in.readObject();
        rewarditemamount = (int) in.readObject();
        rewardpermission = (List<String>) in.readObject();
        rewardpermissionamount = (int) in.readObject();
        isadmin = (boolean) in.readObject();
    }

    private void readObjectNoData() throws ObjectStreamException {
        new QuestReward();
    }

    public Boolean give(Player player) {
        if (player.getInventory().getSize() - player.getInventory().getArmorContents().length < rewarditemamount) {
            playerquest_yml.set(player.getName() + ".rewarding", true);
            player.sendMessage(ChatColor.RED + "奖励发送失败，请尝试清空背包并使用指令/quest getreward重新获得奖励！");
            return false;
        }

        player.sendMessage(ChatColor.GREEN + "即将发送奖励~");
        for (ItemStack item :
                rewarditems) {
            player.getInventory().addItem(item);
            player.updateInventory();
        }
        sn_economy.depositPlayer(player, rewarditemamount);
        for (String prm :
                rewardpermission) {
            sn_perm.playerAdd(null, player, prm);

        }
        player.sendMessage(ChatColor.GREEN + "奖励发送完成~");
        return true;
    }


    public void addRewarditem(ItemStack a) {
        rewarditemamount++;
        List<ItemStack> tempis = rewarditems;
        rewarditems = new ArrayList<>();
        int i = 0;
        for (ItemStack b :
                tempis) {
            rewarditems.set(i++, b);
        }
        rewarditems.set(i, a);
    }

    public void addRewardpermission(String a) {
        rewardpermissionamount++;
        List<String> tempstr = rewardpermission;
        rewardpermission = new ArrayList<>();
        int i = 0;
        for (String b :
                tempstr) {
            rewardpermission.set(i++, b);
        }
        rewardpermission.set(i, a);
    }


    public void removeRewarditem(int index) {
        rewarditemamount--;
        List<ItemStack> tempis = rewarditems;
        rewarditems = new ArrayList<>();
        int i = 0;
        for (ItemStack b :
                tempis) {
            if (i > index) {
                rewarditems.set(i - 1, b);
                ++i;
            } else rewarditems.set(i++, b);
        }
    }

    public void removeRewardpermission(int index) {
        rewardpermissionamount--;
        List<String> tempstr = rewardpermission;
        rewardpermission = new ArrayList<>();
        int i = 0;
        for (String b :
                tempstr) {
            if (i > index) {
                rewardpermission.set(i - 1, b);
                ++i;
            } else rewardpermission.set(i++, b);
        }
    }

    public Boolean readQrFromYml(String name) {
        return readQrFromYml(quest_yml, name);
    }

    public Boolean readQrFromYml(YamlConfiguration ymlfile, String name) {

        questrewardname = name;

        if (ymlfile.contains(name + ".property-set.rewarditemamount")) {
            rewarditemamount = ymlfile.getInt(name + ".property-set.rewarditemamount");
            for (int i = 0; i < rewarditemamount; i++) {
                rewarditems.set(i, SnFileIO.readItemStackFromYml(ymlfile, name + ".property-set.rewarditem." + i));
            }
        }
        if (ymlfile.contains(name + ".property-set.rewardmoney")) {
            rewardmoney = ymlfile.getInt(name + ".property-set.rewardmoney");
        }
        if (ymlfile.contains(name + ".property-set.rewardpermissionamount")) {
            rewardpermissionamount = ymlfile.getInt(name + ".property-set.rewardpermissionamount");
            for (int i = 0; i < rewardpermissionamount; i++) {
                rewardpermission.set(i, ymlfile.getString(name + ".property-set.rewardpermissionamount." + i));
            }
        }
        return true;
    }

    public void saveQrToYml() {
        saveQrToYml(quest_yml);
    }

    public void saveQrToYml(YamlConfiguration ymlfile) {

        if (rewardmoney != 0) {
            ymlfile.set(questrewardname + ".property-set.rewardmoney", rewardmoney);
        }
        if (rewarditemamount != 0) {
            ymlfile.set(questrewardname + ".property-set.rewarditemamount", rewarditemamount);
            for (int i = 0; i < rewarditemamount; i++) {
                SnFileIO.saveItemStackToYml(ymlfile, questrewardname + ".property-set.rewarditem." + i, rewarditems.get(i));
            }
        }
        if (rewardpermissionamount != 0) {
            ymlfile.set(questrewardname + ".property-set.rewardpermissionamount", rewardpermissionamount);
            for (int i = 0; i < rewardpermissionamount; i++) {
                ymlfile.set(questrewardname + ".property-set.rewarditemamount." + i, rewardpermission.get(i));
            }
        }

    }

    public String getQuestrewardname() {
        return questrewardname;
    }

    public void setQuestrewardname(String questrewardname) {
        this.questrewardname = questrewardname;
    }

    public int getRewarditemamount() {
        return rewarditemamount;
    }

    private void setRewarditemamount(int rewarditemamount) {
        this.rewarditemamount = rewarditemamount;
    }

    public int getRewardpermissionamount() {
        return rewardpermissionamount;
    }

    private void setRewardpermissionamount(int rewardpermissionamount) {
        this.rewardpermissionamount = rewardpermissionamount;
    }

    public double getRewardmoney() {
        return rewardmoney;
    }

    public void setRewardmoney(double rewardmoney) {
        this.rewardmoney = rewardmoney;
    }

    public List<String> getRewardpermission() {
        return rewardpermission;
    }

    public void setRewardpermission(List<String> rewardpermission) {
        this.rewardpermission = rewardpermission;
    }

    public List<ItemStack> getRewarditems() {
        return rewarditems;
    }

    public void setRewarditems(List<ItemStack> rewarditems) {
        this.rewarditems = rewarditems;
    }

    public boolean isAdmin() {
        return isadmin;
    }

    public void setAdmin(boolean isadmin) {
        this.isadmin = isadmin;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuestReward)) return false;
        QuestReward that = (QuestReward) o;
        return Double.compare(that.getRewardmoney(), getRewardmoney()) == 0 && getRewarditemamount() == that.getRewarditemamount() && getRewardpermissionamount() == that.getRewardpermissionamount() && isadmin == that.isadmin && getQuestrewardname().equals(that.getQuestrewardname()) && getRewarditems().equals(that.getRewarditems()) && getRewardpermission().equals(that.getRewardpermission());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getQuestrewardname(), getRewardmoney(), getRewarditems(), getRewarditemamount(), getRewardpermission(), getRewardpermissionamount(), isadmin);
    }

    @Override
    public String toString() {
        return "QuestReward{" +
                "questrewardname='" + questrewardname + '\'' +
                ", rewardmoney=" + rewardmoney +
                ", rewarditems=" + rewarditems +
                ", rewarditemamount=" + rewarditemamount +
                ", rewardpermission=" + rewardpermission +
                ", rewardpermissionamount=" + rewardpermissionamount +
                ", isadmin=" + isadmin +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
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
        tmp.put("questrewardname", questrewardname);
        tmp.put("rewardmoney", rewardmoney);
        tmp.put("rewarditems", rewarditems);
        tmp.put("rewarditemamount", rewarditemamount);
        tmp.put("rewardpermission", rewardpermission);
        tmp.put("rewardpermissionamount", rewardpermissionamount);
        tmp.put("isadmin", isadmin);
        return tmp;
    }

    /*
        1、货币（int）
        2、物品奖励（ItemStack[])
        3、权限（Permission)*/
}
