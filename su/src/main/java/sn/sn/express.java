package sn.sn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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


        每个人的空间：nmax格物品
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
                      internal: H4sIAAAAAAAA/43SOwvCMBQF4Ju2ERvBRxX1tziJ4uCsu/QRNTRNIE3B+utN0d1zl3OHjzMdQSRoetC2rE/GK99f80dM7EXhQvZDpsTPXjatCP+MUXLR1tOYIlXRolFGli6/+10hKxdaGPGj7Yzf008yWEawjGGZwJLDcgTLFJZzWGawXMJyBcs1LDew3P6XYYrvYYpfOWmfna6luxU2DPUDotFgJsMCAAA=
                      blockMaterial: LIGHT_GRAY_SHULKER_BOX
                ……


   类：
        主类: express
        附属类：showInvEvent

   方法：
        private boolean help()
        向使用者发送信息。

*/

public class express implements CommandExecutor {

    protected Player senderPlayer;

    static void setstatefalse(Player senderPlayer) {
        Sn.share_yml.set(senderPlayer.getName()+".using",false);
        try {
            Sn.share_yml.save(Sn.share_file);
        } catch (IOException e) {say("[WARN]文件保存可能出错");}
        try {
            Sn.share_yml.load(Sn.share_file);
        } catch (IOException | InvalidConfigurationException ignored) {}
    }

    public static String share_Path;

    private void setstatefalse(){
        setstatefalse(senderPlayer);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){



        if(label.equalsIgnoreCase("express")) {


            //获取玩家背包信息
            if(sender instanceof Player){
                senderPlayer = (Player)sender;
            } else {
                say("玩家信息异常，请联系管理员。");
                return true;
            }


            if(args.length==0){
                return help();
            }
            if(args[0].equalsIgnoreCase("help")||args[0].equalsIgnoreCase("？")||args[0].equalsIgnoreCase("?")){
                return help();
            }

            if(args[0].equalsIgnoreCase("mes")&&senderPlayer.isOp()){
                say("share_yml地址："+share_Path);
                say("plugin地址："+Sn.plugin_Path);
            }

            if(args[0].equalsIgnoreCase("setpath")&&senderPlayer.isOp()){

                String path = args[1];

                //检测目录是否有空格
                int i=2;
                while(args[i] != null)
                    args[1]= args[1]+' '+args[i++];


                say("3s后即将设置sharePath，请结束任何速递指令!");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
                say("你的sharePath即将设置为"+path);
                config_yml.set("share_path",path);
                say("正在寻找目录……");

                try {
                    if(!Sn.share_file.getParentFile().exists()){
                        say("目录不存在，正在自动创建……");
                        if(Sn.share_file.getParentFile().mkdirs())say("创建成功！");
                        else say("创建失败，请检查文件权限。");
                    } else say("目录已找到！");
                } catch (NullPointerException e){
                    say("你的目录出现错误，请不要在目录中包含空格，或选择手动配置config文件");
                }

                say("准备创建文件……");
                try {
                    if(Sn.share_file.createNewFile()){
                        say("sharefile 重载成功");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                config_yml.set("share_path_ed","true");

                try {
                    config_yml.save(config_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                say("完成，请/reload,或重启服务器。");
            }


            try {
                share_yml.load(share_file);
            } catch (IOException | InvalidConfigurationException e) {
                senderPlayer.sendMessage(ChatColor.YELLOW+"系统繁忙，请稍后再试~");
                senderPlayer.sendMessage(ChatColor.YELLOW+"若短时间内多次出现该信息，请联系管理员！");

            }
            //检查是否有玩家的信息记录 没有则补充
            if(!Sn.share_yml.contains(senderPlayer.getName())){
                Sn.share_yml.set(senderPlayer.getName()+".using",false);
                Sn.share_yml.set(senderPlayer.getName()+".line",0);
                Sn.share_yml.set(senderPlayer.getName()+".max",54);
                Sn.share_yml.set(senderPlayer.getName()+".items",null);
            }

            if(args[0].equalsIgnoreCase("clean")){
                Sn.share_yml.set(senderPlayer.getName()+".using",false);
                Sn.share_yml.set(senderPlayer.getName()+".line",0);
                Sn.share_yml.set(senderPlayer.getName()+".max",54);
                Sn.share_yml.set(senderPlayer.getName()+".items",null);
                senderPlayer.sendMessage("完成，你的快递箱已经清除。");
                try {
                    share_yml.save(share_file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }

            //判断是否同时使用雪花速递 using：true 则不允许使用
            if(share_yml.getBoolean(senderPlayer.getName()+".using")){
                senderPlayer.sendMessage(ChatColor.RED+"警告：请不要尝试同时打开雪花速递！");
                senderPlayer.sendMessage(ChatColor.YELLOW+"若没有多开雪花速递，请联系管理员！");
                return true;
            }
            share_yml.set(senderPlayer.getName()+".using",true);
            try {
                share_yml.save(share_file);
            } catch (IOException e) {
                senderPlayer.sendMessage(ChatColor.YELLOW+"系统繁忙，请稍后再试~");
                senderPlayer.sendMessage(ChatColor.YELLOW+"若短时间内多次出现该信息，请联系管理员！");
                setstatefalse();
                return false;
            }


            if(args[0].equalsIgnoreCase("send")){
                //say("玩家"+sender.getName()+"尝试使用express send");

                if(args.length == 1){
                    help();
                    setstatefalse();
                    return true;
                }
                if(args[1].equalsIgnoreCase("hand")){
                    String strline=sender.getName() + ".line";
                    int n=Sn.share_yml.getInt(strline);

                    Inventory temp = Bukkit.createInventory(null, 54);
                    for(int i=0;i<n;i++)
                        temp.addItem(readItemStackFromYml(share_yml,sender.getName() + ".items"+'.'+i));

                    ItemStack hand = senderPlayer.getInventory().getItemInMainHand();

                    HashMap<Integer, ItemStack> remain = temp.addItem(hand);


                    int nown = 0;
                    for (ItemStack content : temp.getContents()) {
                        if(content != null) nown++;
                    }
                    Sn.share_yml.set(strline, nown);
                    if (remain.isEmpty())
                        senderPlayer.getInventory().setItemInMainHand(null);
                    else senderPlayer.getInventory().setItemInMainHand(remain.get(0));

                    share_yml.set(sender.getName() + ".items",null);

                    for(int i=0;i<nown;i++)
                        saveItemStackToYml(share_yml,sender.getName() + ".items."+ i, Objects.requireNonNull(temp.getItem(i)));


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

                    senderPlayer.sendMessage("物品"+ hand +"已经发送！");
                    setstatefalse();
                    return true;
                }


                if(args[1].equalsIgnoreCase("box")){
                    //检查还有没有空余的格子,与之前不同的是要先检查箱子里的物品
                    //箱子检查 zjxz:左键箱子
                    Chest chest;
                    Block targetblock = senderPlayer.getTargetBlock(null,10);
                    if(targetblock.getType()== Material.CHEST){
                        chest = (Chest) targetblock.getState();
                    } else {
                        senderPlayer.sendMessage("请指向一个箱子！");
                        setstatefalse();
                        return true;
                    }

                    //取出箱子里的物品
                    ItemStack[] storeditems = chest.getBlockInventory().getContents();
                    List<ItemStack> storeitemlist = new ArrayList<>();
                    for(ItemStack tmpcl:storeditems){
                        if(tmpcl!=null)storeitemlist.add(tmpcl);
                    }
                    int ninv = storeitemlist.size();

                    //格子检查
                    String strline=sender.getName() + ".line";
                    int n=Sn.share_yml.getInt(strline);
                    int nmax=Sn.share_yml.getInt(sender.getName() + ".max");
                    if (n+ninv>nmax) {
                        senderPlayer.sendMessage(ChatColor.RED+"可用空间不足");
                        setstatefalse();
                        return true;
                    }

                    Sn.share_yml.set(strline, n+ninv);
                    //添加物品

                    Inventory temp = Bukkit.createInventory(null, 54);
                    Inventory remains = Bukkit.createInventory(null, 27);

                    for(int i=0;i<n;i++)
                        temp.addItem(readItemStackFromYml(share_yml,sender.getName() + ".items"+'.'+i));

                    for(ItemStack tmpim:storeitemlist){
                        HashMap<Integer, ItemStack> remain = temp.addItem(tmpim);
                        if(!remain.isEmpty())remains.addItem(remain.get(0));
                    }

                    if(!saveInvToYml(share_yml,share_file,senderPlayer.getName(),temp)){
                        senderPlayer.sendMessage("文件保存可能出错！");
                        if(remains.isEmpty()) chest.getBlockInventory().clear();
                        else chest.getBlockInventory().setContents(remains.getContents());
                        setstatefalse();
                        return false;
                    }

                    if(remains.isEmpty()) chest.getBlockInventory().clear();
                    else chest.getBlockInventory().setContents(remains.getContents());
                    setstatefalse();
                    return true;
                }

                setstatefalse();
                return true;
            }

            if(args[0].equalsIgnoreCase("show")){
                //say("玩家"+sender.getName()+"尝试使用express show");


                //为玩家创建一个inventory，只在开启和关闭时进行文件操作。
                showInv.put(senderPlayer, Bukkit.createInventory(senderPlayer,54,ChatColor.BLUE+"雪花速递"));

                //读取文件
                String strline = sender.getName() + ".line";
                int n = Sn.share_yml.getInt(strline);
                for(int i=0;i<n;i++){
                    ItemStack tempstack = readItemStackFromYml(share_yml,sender.getName() + ".items"+'.'+i);
                    showInv.get(senderPlayer).addItem(tempstack);//添加
                    showInv.get(senderPlayer).setItem(i,tempstack);//设置GUI
                }
                showInvEvent.showInv_nmax = n;


                //打开GUI
                senderPlayer.openInventory(showInv.get(senderPlayer));
                return true;
                //这个指令到这里就结束了 剩下的交给showInvEvent

            }
        }

        setstatefalse();
        return true;
    }



    private boolean help() {
        senderPlayer.sendMessage(ChatColor.GREEN+"欢迎使用雪花速递~");
        if(senderPlayer.isOp()) {
            senderPlayer.sendMessage(ChatColor.GREEN + "你可以使用下列命令");
            senderPlayer.sendMessage(ChatColor.GREEN + "/express send hand 发送手上的物品到快递箱");
            senderPlayer.sendMessage(ChatColor.GREEN + "/express send box 发送你指向的箱子内的物品到快递箱");
            senderPlayer.sendMessage(ChatColor.GREEN + "/express show 查看你的快递箱");
            senderPlayer.sendMessage(ChatColor.GREEN + "/express setpath 设置快递箱的服务端文件地址");
            senderPlayer.sendMessage(ChatColor.GREEN + "/express mes 让后台打印雪花速递的存储地址信息");
        } else {
            senderPlayer.sendMessage(ChatColor.GREEN + "你可以使用下列命令");
            senderPlayer.sendMessage(ChatColor.GREEN + "/express send hand 发送手上的物品到快递箱");
            senderPlayer.sendMessage(ChatColor.GREEN + "/express send box 发送你指向的箱子内的物品到快递箱");
            senderPlayer.sendMessage(ChatColor.GREEN + "/express show 查看你的快递箱并取出你的快递");
        }
        return true;
    }
}
