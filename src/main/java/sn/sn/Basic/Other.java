package sn.sn.Basic;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.RegisteredServiceProvider;
import sn.sn.Sn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Other {
    public static List<String> toStrList(Map<String,Object> map){
        List<String> list = new ArrayList<>();
        for (String s : map.keySet()) {
            if(map.get(s)==null)continue;
            list.add(s+"->"+map.get(s).toString());
        }
        return list;
    }

    /** 给Console发送信息
     * send message to console
     * @param mes 要发送的信息(the message to send)
     */
    public static void sendInfo(String mes){
        CommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage("[sn][INFO]"+ mes);
    }

    /** 给Console发送debug信息
     * send debug message to console when debug is true.
     * @param mes 要发送的信息(the message to send)
     */
    public static void sendDebug(String mes){
        if(Sn.debug){
            CommandSender sender = Bukkit.getConsoleSender();
            sender.sendMessage(ChatColor.BLUE+"[sn][DEBUG]"+ mes);
        }
    }

    /** 给Console发送Warn信息
     * send Warn message to console.
     * @param mes 要发送的信息(the message to send)
     */
    public static void sendWarn(String mes){
        CommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage(ChatColor.YELLOW+"[sn][WARN]"+ mes);
    }

    /** 给Console发送Error信息
     * send Error message to console.
     * @param mes 要发送的信息(the message to send)
     */
    public static void sendError(String mes){
        CommandSender sender = Bukkit.getConsoleSender();
        sender.sendMessage(ChatColor.RED+"[sn][Error]"+ mes);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean initVault(){
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        RegisteredServiceProvider<Permission> perProvider = Bukkit.getServicesManager().getRegistration(Permission.class);

        if(economyProvider != null && perProvider != null){
            Sn.sn_economy = economyProvider.getProvider();
            Sn.sn_perm = perProvider.getProvider();
            Sn.eco_system_set = true;
            return true;
        } else return false;
    }

    public static class EnchantPair {
        Enchantment a;
        int b;

        public EnchantPair(String data){
            int index = data.indexOf(' ');
            a = Enchantment.getByKey(NamespacedKey.minecraft(data.substring(0,index)));
            b = Integer.parseInt(data.substring(index+1));
        }

        public Enchantment getA() {
            return a;
        }

        public int getB() {
            return b;
        }
    }
}
