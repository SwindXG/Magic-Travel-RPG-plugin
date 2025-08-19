package com.example.command.sub;

import com.example.Main;
import com.example.data.PlayerAttribute;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdminAttributeSubCommand implements SubCommand {

    private final Main plugin;
    private static final List<String> ATTR_KEYS = Arrays.asList(
            "health","attack","defense","speed","mana","magicattack","magicdefense","luck",
            "criticalhitchance","criticalhitdamage","fireattack","firedefense","iceattack","icedefense",
            "lightningattack","lightningdefense","windattack","winddefense","grassattack","grassdefense",
            "lightattack","lightdefense","darkattack","darkdefense","waterattack","waterdefense"
    );

    public AdminAttributeSubCommand(Main plugin){ this.plugin = plugin; }

    @Override public String getName() { return "admin"; }
    @Override public String getDescription() { return "管理指令 (attribute set)"; }
    @Override public String getPermission() { return "mt.admin.attribute"; }
    @Override public String getUsage(String root) { return "/" + root + " admin attribute set <玩家> <屬性> <數值>"; }
    @Override public List<String> getAliases() { return Collections.emptyList(); }
    @Override public boolean isListedInHelp() { return true; }

    @SuppressWarnings("deprecation")
    @Override
    public boolean execute(CommandSender sender, String[] args, String rootLabel) {
        // args: attribute set <player> <key> <value>
        if (args.length != 5) return false;
        if (!args[0].equalsIgnoreCase("attribute") || !args[1].equalsIgnoreCase("set")) return false;

        String playerName = args[2];
        String attrKey = args[3].toLowerCase();
        String valueStr = args[4];

        int val;
        try { val = Integer.parseInt(valueStr); }
        catch (NumberFormatException e) {
            sender.sendMessage("§c數值必須是整數");
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
            sender.sendMessage("§7可用: " + String.join(", ", ATTR_KEYS));
            return true;
        }

        plugin.savePlayerAttribute(op.getUniqueId());
        sender.sendMessage("§a已設定 " + op.getName() + " 的 " + attrKey + " = " + val);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // args: attribute set <player> <key> <value>
        if (args.length == 1) {
            return partial(args[0], Collections.singletonList("attribute"));
        } else if (args.length == 2) {
            return partial(args[1], Collections.singletonList("set"));
        } else if (args.length == 3) { // player
            List<String> names = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
            return partial(args[2], names);
        } else if (args.length == 4) { // key
            return partial(args[3], ATTR_KEYS);
        }
        return Collections.emptyList();
    }

    private List<String> partial(String token, List<String> base){
        List<String> out = new ArrayList<>();
        for (String b : base) if (b.toLowerCase().startsWith(token.toLowerCase())) out.add(b);
        return out;
    }
}