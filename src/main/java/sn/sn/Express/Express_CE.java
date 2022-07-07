package sn.sn.Express;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import sn.sn.Ask.AskSetEvent;
import sn.sn.Basic.Other;
import sn.sn.Basic.SnFileIO;
import sn.sn.Sn;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static sn.sn.Sn.*;



/*


        雪花速递
        作者：LtSeed

     命令：
     /express：
        - 或 help 或 ? 或 ？:
            返回使用方法


        send:
            hand：
                1.检查手上的物品
                2.按照文件中的对照表向共享文件中输入物品（方块 个数）
            box：（待实现、因为发现好像没啥大用，直接咕咕咕）
                1.检查玩家选择一个箱子
                2.寻找箱子中的物品
                3.按照文件中的对照表向共享文件中输入物品（方块 个数）

        show:
            给玩家显示他的格子里的物品
            使用ui界面，让玩家可以直接从背包实现存取


        setpath:
            权限：op(!未添加权限)
            [String Path]：共享文件的位置，应该是可以访问的，可以修改的（两个服务端都要可以修改）
                更改插件的config文件中配置文件地址项。
                注意：不需要包含"share.yml"，但需要包含最后一个"\"
                在设置的共享地址下，生成一个share.yml文件 存放玩家虚拟背包


        每个人的空间：n_max格物品
            配置文件中每个人的配置信息格式：
            player:
             using: true 面板是否打开，防止两个客户端同时打开面板导致bug
             line: n   有n行物品
             max: 54 最多几格物品
             items:
                 '1':
                  type: LIGHT_GRAY_SHULKER_BOX
                  amount: 1
                  tag:
                    v: 2865
                    type: LIGHT_GRAY_SHULKER_BOX
                    meta:
                      ==: ItemMeta
                      meta-type: TILE_ENTITY
                      internal: H4sIAAAAAAAA/43SOwvCMBQF4Ju2ERv.....
                      blockMaterial: LIGHT_GRAY_SHULKER_BOX
                ……


   类：
        CE主类: Express_CE
        附属类：InvOperateEvent

   方法：
        private boolean help(Player p)
        向使用者发送信息。

*/

public class Express_CE implements CommandExecutor {


    public static void setStateFalse(Player senderPlayer) {
        Sn.share_yml.set(senderPlayer.getName()+".using",false);
        try {
            Sn.share_yml.save(Sn.share_file);
        } catch (IOException e) {
            Other.sendInfo("[WARN]文件保存可能出错");}
        try {
            Sn.share_yml.load(Sn.share_file);
        } catch (IOException | InvalidConfigurationException ignored) {}
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args){

        Player sender_player;
        //获取玩家背包信息
        if(sender instanceof Player){
            sender_player = (Player)sender;
        } else {
            Other.sendInfo("玩家信息异常，请联系管理员。");
            return true;
        }

        if(label.equalsIgnoreCase("express")&& sender_player.hasPermission("sn.express")) {

            if(args.length==0)
                return help(sender_player);


            if(args[0].equalsIgnoreCase("help")||args[0].equalsIgnoreCase("？")||args[0].equalsIgnoreCase("?")){
                return help(sender_player);
            }

            if(args[0].equalsIgnoreCase("mes")&& sender_player.hasPermission("sn.express.mes")){
                Other.sendInfo("share_yml地址："+ share_path);
                Other.sendInfo("plugin地址："+Sn.plugin_path);
                return true;
            }

            if(args[0].equalsIgnoreCase("reset_state")){
                return workExpressResetStateCE(args,sender_player);
            }

            if(args[0].equalsIgnoreCase("sn.set_path")){
                if (workExpressSetPathCE(sender_player)) return false;
            }

            checkExpressPlayerAndFile(sender_player);

            if(args[0].equalsIgnoreCase("clean")){
                return workExpressCleanCE(sender_player);
            }

            //判断是否同时使用雪花速递 using：true 则不允许使用
            if(share_yml.getBoolean(sender_player.getName()+".using")){
                sender_player.sendMessage(ChatColor.RED+"警告：请不要尝试同时打开雪花速递！");
                sender_player.sendMessage(ChatColor.YELLOW+"若没有多开雪花速递，请联系管理员！");
                return true;
            }
            share_yml.set(sender_player.getName()+".using",true);
            try {
                share_yml.save(share_file);
            } catch (IOException e) {
                sender_player.sendMessage(ChatColor.YELLOW+"系统繁忙，请稍后再试~");
                sender_player.sendMessage(ChatColor.YELLOW+"若短时间内多次出现该信息，请联系管理员！");
                setStateFalse(sender_player);
                return false;
            }

            if(args[0].equalsIgnoreCase("send")){
                return workExpressSendCE(sender, args);
            }

            if(args[0].equalsIgnoreCase("show")){
                return workExpressShowCE(sender);
            }
        }

        setStateFalse(sender_player);
        return true;
    }

    private boolean workExpressShowCE(@NotNull CommandSender sender) {
        Player sender_player;
        //获取玩家背包信息
        if(sender instanceof Player){
            sender_player = (Player)sender;
        } else {
            Other.sendInfo("玩家信息异常，请联系管理员。");
            return true;
        }
        if(!sender_player.hasPermission("sn.express.show")){
            noPermission("sn.express.show",sender_player);
            return false;
        }

        //为玩家创建一个inventory，只在开启和关闭时进行文件操作。
        show_inv.put(sender_player,
                SnFileIO.readInvFromYml(sender_player, sender.getName(), share_yml, ChatColor.BLUE+"雪花速递"));

        //打开GUI
        sender_player.openInventory(show_inv.get(sender_player));
        return true;
        //这个指令到这里就结束了 剩下的交给showInvEvent
    }

    private boolean workExpressSendCE(@NotNull CommandSender sender, @NotNull String[] args) {
        Player sender_player;
        //获取玩家背包信息
        if(sender instanceof Player){
            sender_player = (Player)sender;
        } else {
            Other.sendInfo("玩家信息异常，请联系管理员。");
            return true;
        }
        if(!sender_player.hasPermission("sn.express.send")){
            noPermission("sn.express.send",sender_player);
            return false;
        }
        //sendInfo("玩家"+sender.getName()+"尝试使用express send");

        if(args.length == 1){
            help(sender_player);
            setStateFalse(sender_player);
            return true;
        }
        if(args[1].equalsIgnoreCase("hand")){
            String str_line = sender.getName() + ".line";
            int n=Sn.share_yml.getInt(str_line);

            Inventory temp = Bukkit.createInventory(null, 54);
            for(int i=0;i<n;i++)
                temp.addItem(SnFileIO.readItemStackFromYml(share_yml, sender.getName() + ".items"+'.'+i));

            ItemStack hand = sender_player.getInventory().getItemInMainHand();

            HashMap<Integer, ItemStack> remain = temp.addItem(hand);


            int now_n = 0;
            for (ItemStack content : temp.getContents()) {
                if(content != null) now_n++;
            }
            Sn.share_yml.set(str_line, now_n);
            if (remain.isEmpty())
                sender_player.getInventory().setItemInMainHand(null);
            else sender_player.getInventory().setItemInMainHand(remain.get(0));

            share_yml.set(sender.getName() + ".items",null);

            for(int i = 0; i< now_n; i++)
                SnFileIO.saveItemStackToYml(share_yml, sender.getName() + ".items."+ i, Objects.requireNonNull(temp.getItem(i)));


            try {
                Sn.share_yml.save(Sn.share_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Sn.share_yml.load(Sn.share_file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }

            sender_player.sendMessage("物品"+ hand +"已经发送！");
            setStateFalse(sender_player);
            return true;
        }


        if(args[1].equalsIgnoreCase("box")){
            //检查还有没有空余的格子,与之前不同的是要先检查箱子里的物品
            Chest chest;
            Block target_block = sender_player.getTargetBlock(null,10);
            if(target_block.getType()== Material.CHEST){
                chest = (Chest) target_block.getState();
            } else {
                sender_player.sendMessage("请指向一个箱子！");
                setStateFalse(sender_player);
                return true;
            }

            //取出箱子里的物品
            ItemStack[] stored_items = chest.getBlockInventory().getContents();
            List<ItemStack> store_item_list = new ArrayList<>();
            for(ItemStack tmp_cl : stored_items){
                if(tmp_cl !=null) store_item_list.add(tmp_cl);
            }
            int n_inv = store_item_list.size();

            //格子检查
            String str_line = sender.getName() + ".line";
            int n=Sn.share_yml.getInt(str_line);
            int n_max =Sn.share_yml.getInt(sender.getName() + ".max");
            if (n+ n_inv > n_max) {
                sender_player.sendMessage(ChatColor.RED+"可用空间不足");
                setStateFalse(sender_player);
                return true;
            }

            //添加物品

            Inventory temp = Bukkit.createInventory(null, 54);
            Inventory remains = Bukkit.createInventory(null, 27);
            for(int i=0;i<n;i++)
                temp.addItem(SnFileIO.readItemStackFromYml(share_yml, sender.getName() + ".items"+'.'+i));

            for(ItemStack tmp_im : store_item_list){
                HashMap<Integer, ItemStack> remain = temp.addItem(tmp_im);
                if(!remain.isEmpty())remains.addItem(remain.get(0));
            }

            if(!SnFileIO.saveInvToYml(share_yml,share_file, sender_player.getName(),temp)){
                sender_player.sendMessage("文件保存可能出错！");
                if(remains.isEmpty()) chest.getBlockInventory().clear();
                else chest.getBlockInventory().setContents(remains.getContents());
                setStateFalse(sender_player);
                return false;
            }

            if(remains.isEmpty()) chest.getBlockInventory().clear();
            else chest.getBlockInventory().setContents(remains.getContents());
            setStateFalse(sender_player);
            return true;
        }

        setStateFalse(sender_player);
        return true;
    }

    private boolean workExpressCleanCE(Player sender_player) {

        if(!sender_player.hasPermission("sn.express.clean")){
            noPermission("sn.express.clean",sender_player);
            return false;
        }
        Sn.share_yml.set(sender_player.getName()+".using",false);
        Sn.share_yml.set(sender_player.getName()+".line",0);
        Sn.share_yml.set(sender_player.getName()+".max",54);
        Sn.share_yml.set(sender_player.getName()+".items",null);
        sender_player.sendMessage("完成，你的快递箱已经清除。");
        try {
            share_yml.save(share_file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void checkExpressPlayerAndFile(Player sender_player) {
        try {
            share_yml.load(share_file);
        } catch (IOException | InvalidConfigurationException e) {
            sender_player.sendMessage(ChatColor.YELLOW+"系统繁忙，请稍后再试~");
            sender_player.sendMessage(ChatColor.YELLOW+"若短时间内多次出现该信息，请联系管理员！");
        }
        //检查是否有玩家的信息记录 没有则补充
        if(!Sn.share_yml.contains(sender_player.getName())){
            Sn.share_yml.set(sender_player.getName()+".using",false);
            Sn.share_yml.set(sender_player.getName()+".line",0);
            Sn.share_yml.set(sender_player.getName()+".max",54);
            Sn.share_yml.set(sender_player.getName()+".items",null);
        }
    }

    private boolean workExpressSetPathCE(Player sender_player) {
        if(!sender_player.hasPermission("sn.express.set_path")){
            noPermission("sn.express.set_path",sender_player);
            return true;
        }

        List<String> q = new ArrayList<>();
        q.add("请输入share.yml的路径");
        List<Consumer<String>> d = new ArrayList<>();
        d.add((str)->{
            if(!str.endsWith(File.separator)) str = str + File.separator;
            share_path = str;
            config_yml.set("share-path",share_path);
            try {
                config_yml.save(config_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Other.sendInfo("完成，请/reload,或重启服务器。");
        });

        AskSetEvent.askSetAsync(sender_player,q,d,null, p -> p.sendMessage("设置被打断！"));

        return false;
    }

    private boolean workExpressResetStateCE(@NotNull String[] args,Player sender_player) {
        if(!sender_player.hasPermission("sn.express.reset_state")){
            noPermission("sn.express.reset_state",sender_player);
            return false;
        }
        if(args.length == 1){
            Sn.share_yml.set(sender_player.getName()+".using",false);
            try {
                share_yml.save(share_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        Player tmp = Bukkit.getPlayer(args[1]);
        if(tmp!=null){
            tmp.closeInventory();
            Sn.share_yml.set(tmp.getName()+".using",false);
            tmp.sendMessage("管理员尝试更改了你的状态！");
            try {
                share_yml.save(share_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            OfflinePlayer tmp2 = Bukkit.getOfflinePlayer(UUID.fromString(args[1]));
            Sn.share_yml.set(tmp2.getName()+".using",false);
            try {
                share_yml.save(share_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void noPermission(String per,Player sender_player) {
        sender_player.sendMessage("你没有"+per+"权限");
        setStateFalse(sender_player);
    }


    private boolean help(Player sender_player) {
        sender_player.sendMessage(ChatColor.GREEN+"欢迎使用雪花速递~");
        if(sender_player.isOp()) {

            sender_player.sendMessage(ChatColor.GREEN + "你可以使用下列命令");
            sender_player.sendMessage(ChatColor.GREEN + "/express send hand 发送手上的物品到快递箱");
            sender_player.sendMessage(ChatColor.GREEN + "/express send box 发送你指向的箱子内的物品到快递箱");
            sender_player.sendMessage(ChatColor.GREEN + "/express show 查看你的快递箱");
            sender_player.sendMessage(ChatColor.GREEN + "/express reset_state 切换快递使用状态");
            sender_player.sendMessage(ChatColor.GREEN + "/express clean 删除自己的快递箱");
            sender_player.sendMessage(ChatColor.GREEN + "/express setpath 设置快递箱的服务端文件地址");
            sender_player.sendMessage(ChatColor.GREEN + "/express mes 让后台打印雪花速递的存储地址信息");
        } else {
            sender_player.sendMessage(ChatColor.GREEN + "你可以使用下列命令");
            sender_player.sendMessage(ChatColor.GREEN + "/express send hand 发送手上的物品到快递箱");
            sender_player.sendMessage(ChatColor.GREEN + "/express send box 发送你指向的箱子内的物品到快递箱");
            sender_player.sendMessage(ChatColor.GREEN + "/express clean 删除自己的快递箱");
            sender_player.sendMessage(ChatColor.GREEN + "/express show 查看你的快递箱并取出你的快递");
        }
        return true;
    }
}
