package com.example.command; // 指令套件

import com.example.Main;
import com.example.data.PlayerAttribute;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 子指令：/mt playerinfo [玩家]
 * 無參數 → 查看自己
 * 指定玩家 → 需權限 mtplayerinfo.others
 * 已移除舊獨立指令 /mtplayerinfo
 */
public class PlayerInfoCommand implements CommandExecutor {

    private final Main plugin;

    public PlayerInfoCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 僅允許從 /mt 呼叫：label 必須是 mt，且第一參數為 playerinfo
        if (!label.equalsIgnoreCase("mt")) return false;                  // 交回給其他處理
        if (args.length == 0 || !args[0].equalsIgnoreCase("playerinfo"))  // 不是這個子指令
            return false;

        // player 名稱參數位於 args[1]（若存在）
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("mtplayerinfo.others")) {
                sender.sendMessage(ChatColor.RED + "你沒有權限查看其他玩家。");
                return true;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "玩家不在線: " + args[1]);
                return true;
            }
        } else {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatColor.YELLOW + "用法: /mt playerinfo <玩家>");
                return true;
            }
            target = p;
        }

        PlayerAttribute attr = plugin.getPlayerAttribute(target.getUniqueId());
        if (attr == null) {
            sender.sendMessage(ChatColor.RED + "無法讀取玩家屬性。");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "===== " + ChatColor.YELLOW + target.getName() + " 屬性 =====");
        send(sender, "血量", attr.getHealth());
        send(sender, "攻擊力", attr.getAttack());
        send(sender, "防禦力", attr.getDefense());
        send(sender, "速度", attr.getSpeed());
        send(sender, "魔力", attr.getMana());
        send(sender, "魔法攻擊", attr.getMagicAttack());
        send(sender, "魔法防禦", attr.getMagicDefense());
        send(sender, "幸運值", attr.getLuck());
        send(sender, "暴擊機率", attr.getCriticalHitChance());
        send(sender, "暴擊傷害", attr.getCriticalHitDamage());
        send(sender, "火焰攻擊", attr.getFireAttack());
        send(sender, "火焰防禦", attr.getFireDefense());
        send(sender, "冰霜攻擊", attr.getIceAttack());
        send(sender, "冰霜防禦", attr.getIceDefense());
        send(sender, "雷電攻擊", attr.getLightningAttack());
        send(sender, "雷電防禦", attr.getLightningDefense());
        send(sender, "風屬性攻擊", attr.getWindAttack());
        send(sender, "風屬性防禦", attr.getWindDefense());
        send(sender, "草屬性攻擊", attr.getGrassAttack());
        send(sender, "草屬性防禦", attr.getGrassDefense());
        send(sender, "光屬性攻擊", attr.getLightAttack());
        send(sender, "光屬性防禦", attr.getLightDefense());
        send(sender, "暗屬性攻擊", attr.getDarkAttack());
        send(sender, "暗屬性防禦", attr.getDarkDefense());
        return true;
    }

    private void send(CommandSender sender, String name, int val) {
        sender.sendMessage(ChatColor.AQUA + name + "： " + ChatColor.WHITE + val);
    }
}