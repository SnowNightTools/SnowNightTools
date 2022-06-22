package sn.sn;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static sn.sn.Sn.sneconomy;
/*
*
*
* City 城市系统
* 作者:
*
*
* 指令：
* /city create <name> 发起一个城市的新建，在达到三个人时正式成立城市，使用这个指令的人会成为市长，拥有管理权限。
* /city join <name> 加入一个城市，一个人只能加入一个城市，但是可以同时发很多请求，不储存请求，每次重启请求刷新。
* /city spawn 回到自己小镇的出生点。
* /city quit 退出小镇，小镇人数少于4人时不能退出小镇
* /city my 打开小镇菜单（传送点，出生点，各个成员，点击可以tpa或者warp之类的）
*
* Only For Mayor:
* /city setwarp <name> 在小镇的领土中设置传送点
* /city accept [player name] 同意一个人的加入请求，不填写[player name]，将会打开所有申请人的面板，可以在面板上处理请求。
* /city add <perm group name> <player name> 将一个特定的人添加进该权限组。
* /city set [perm group name] 设置城市对特定权限组的权限，不填写则设置城市对居民的权限（打开面板）。
* /city setspawn 设置小镇出生点
* /city loadchunk 让插件常加载脚下的方块
* /city manage 打开小镇管理面板（踢人之类的操作）
*
* Only For OP:
* /city admin remove <name> 删除一个城市，需要op操作。
* /city admin add <perm> 添加一个可以被城主设置的权限。
* /city admin 打开小镇系统管理面板
*
*
*
*
*
*
*
*
*
*
*
*
* */
public class City_CE implements CommandExecutor {
    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args[0].equals("test")){
            sneconomy.depositPlayer((OfflinePlayer) sender,1000);
            sender.sendMessage(sneconomy.getName());
        }
        return false;
    }

    public static class City {

        String name;
        List<UUID> residents;
        Map<String, List<UUID>> perm_group;
        UUID mayor;
        List<Range> territorial;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, List<UUID>> getPerm_groups() {
            return perm_group;
        }

        public List<UUID> getPerm_group(String perm_name) {
            return perm_group.get(perm_name);
        }

        public void addPerm_group(String name, List<UUID> perm_group) {
            this.perm_group.put(name,perm_group);
        }

        public List<Range> getTerritorial() {
            return territorial;
        }
    }

}
