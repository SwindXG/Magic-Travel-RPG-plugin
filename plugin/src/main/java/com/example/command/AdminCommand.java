package com.example.command; // 套件：指令相關

import com.example.Main;
import com.example.data.PlayerAttribute;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * 管理指令：
 * /mt admin attribute set <玩家> <屬性> <數值>
 * 權限：mt.admin.attribute
 */
public class AdminCommand implements CommandExecutor {

    private final Main plugin;

    public AdminCommand(Main plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 需求順序: admin attribute set player key value  → 共 6 參數
        if (args.length < 6) {
            sender.sendMessage("§e用法: /" + label + " admin attribute set <玩家> <屬性> <數值>");
            return true;
        }

        // 前三段固定
        if (!args[0].equalsIgnoreCase("admin")
                || !args[1].equalsIgnoreCase("attribute")
                || !args[2].equalsIgnoreCase("set")) {
            sender.sendMessage("§c子指令無效，用法: /" + label + " admin attribute set <玩家> <屬性> <數值>");
            return true;
        }

        if (!sender.hasPermission("mt.admin.attribute")) {
            sender.sendMessage("§c缺少權限: mt.admin.attribute");
            return true;
        }

        String playerName = args[3];
        String attrKey = args[4];
        String valueStr = args[5];

        int val;
        try {
            val = Integer.parseInt(valueStr);
        } catch (NumberFormatException ex) {
            sender.sendMessage("§c數值必須是整數。");
            return true;
        }

        OfflinePlayer op = Bukkit.getOfflinePlayer(playerName);
        if (op == null || op.getUniqueId() == null) {
            sender.sendMessage("§c找不到玩家: " + playerName);
            return true;
        }

        PlayerAttribute attr = plugin.getPlayerAttribute(op.getUniqueId());

        if (!attr.setByKey(attrKey, val)) {
            sender.sendMessage("§c未知屬性: " + attrKey);
            sender.sendMessage("§7可用: health, attack, defense, speed, mana, magicattack, magicdefense, luck, criticalhitchance, criticalhitdamage, fireattack, firedefense, iceattack, icedefense, lightningattack, lightningdefense, windattack, winddefense, grassattack, grassdefense, lightattack, lightdefense, darkattack, darkdefense, waterattack, waterdefense");
            return true;
        }

        plugin.savePlayerAttribute(op.getUniqueId());
        sender.sendMessage("§a已設定 " + playerName + " 的 " + attrKey + " = " + val);
        return true;
    }
}