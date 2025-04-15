package soys.plugin.healthcmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;

/**
 * 主要插件类，实现血量百分比调整功能
 * 提供/hcmd命令用于增减玩家血量，支持配置重载
 */
public class HealthCMD extends JavaPlugin {

    /**
     * 插件启用时执行的基础逻辑
     * 1. 注册命令执行器
     * 2. 保存默认配置文件
     */
    @Override
    public void onEnable() {
        // Plugin startup logic
        getCommand("healthcmd").setExecutor(this);
        saveDefaultConfig();
    }

    /**
     * 插件禁用时的清理逻辑（当前无具体实现）
     */
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * 命令处理核心方法
     * @param sender 命令发送者
     * @param command 被执行的命令对象
     * @param label 实际使用的命令别名
     * @param args 命令参数数组
     * @return boolean 是否成功处理命令
     *
     * 支持功能：
     * 1. 重载配置（/hcmd reload）
     * 2. 调整玩家血量百分比
     * 3. 显示帮助信息
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        /* 基础命令校验 */
        if (!label.equalsIgnoreCase("healthcmd") && !label.equalsIgnoreCase("hcmd")) return true;
        if(!sender.isOp() || !sender.hasPermission("commands.hcmd")) return true;

        /* 处理配置重载指令 */
        if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
            reloadConfig();
            sender.sendMessage("§e[HealthCMD]§2重载成功!");
            return true;
        }

        /* 血量调整主逻辑 */
        if (args.length == 2) {
            // 获取目标玩家对象
            Player player= Bukkit.getPlayer(args[0]);
            if (player == null) {
                sendMessage(sender,"§e[HealthCMD]§4玩家不在线!");
                return true;
            }

            /* 解析百分比参数 */
            double healthPercent;
            try {
                healthPercent = Double.parseDouble(args[1])/100;
            } catch (NumberFormatException nfe) {
                sendMessage(sender,"§e[HealthCMD]§4请输入一个数字!");
                return true;
            }

            /* 计算最终血量值 */
            double health = player.getHealth();
            double maxHealth = player.getMaxHealth();
            double damageHealth = maxHealth * healthPercent;
            health += damageHealth;

            /* 血量临界值处理 */
            if (health <= 0) {
                // 通过反射调用NMS方法强制杀死玩家
                try {
                    NMS.killEntityNMS(player);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    sendMessage(sender,"§e[HealthCMD]§4调用杀死玩家函数异常,请联系开发者");
                    throw new RuntimeException("[HealthCMD]调用杀死玩家函数异常,请联系开发者",e);
                }
                return true;
            }else if (health > maxHealth){
                health = maxHealth;
            }

            /* 应用血量变更 */
            player.setHealth(health);

            sendMessage(sender,"§e[HealthCMD]"+(healthPercent>0?"§2增加":"§4扣除")+"§2玩家 "+player.getName()+" §e"+String.format("%.1f", Math.abs(healthPercent*100))+"%§2(§e"+Math.abs(damageHealth)+"点§2) 血量");
            return true;
        }

        /* 显示命令帮助信息 */
        String[] help = {
                "§e[HealthCMD]§4/hcmd reload",
                "§2重载插件配置文件(仅OP)",
                "§e[HealthCMD]§4/hcmd <玩家名> <扣除的百分比血量>",
                "§2扣除玩家百分比血量,数值范围:-100至100",
                "§2指令权限: §ecommands.hcmd",
                "§e例如:/hcmd soys -50.5",
                "§4扣除§esoys玩家最大血量的50.5%",
                "§e例如:/hcmd soys 50.5",
                "§2增加§esoys玩家最大血量的50.5%",
        };
        for (String s : help) {
            sender.sendMessage(s);
        }
        return true;
    }

    /**
     * 条件消息发送方法
     * @param player 消息接收者
     * @param msg 要发送的消息内容
     * 根据配置文件中的msg设置决定是否实际发送消息
     */
    public void sendMessage(CommandSender player, String msg) {
        if (getConfig().getBoolean("msg")) {
            player.sendMessage(msg);
        }
    }

}