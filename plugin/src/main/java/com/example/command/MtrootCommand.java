package com.example.command;

import com.example.Main;
import com.example.command.sub.SubCommand;
import com.example.command.sub.AdminAttributeSubCommand;
import com.example.command.sub.ReloadSubCommand;
import com.example.command.sub.PlayerInfoSubCommand;
import org.bukkit.command.*;
import org.bukkit.util.StringUtil;

import java.util.*;

public class MtrootCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final Map<String, SubCommand> map = new HashMap<>();

    public MtrootCommand(Main plugin) {
        this.plugin = plugin;
        register(new ReloadSubCommand(plugin));
        register(new PlayerInfoSubCommand(plugin));
        register(new AdminAttributeSubCommand(plugin));
    }

    private void register(SubCommand sc) {
        map.put(sc.getName().toLowerCase(), sc);
        for (String a : sc.getAliases()) {
            map.put(a.toLowerCase(), sc);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // 支援透過舊別名 label 直接呼叫（例如 /mtreload）→ 轉成 /mt reload
        if (!label.equalsIgnoreCase("mt")) {
            if (label.equalsIgnoreCase("mtreload")) {
                SubCommand reload = map.get("reload");
                if (reload != null && reload.hasPermission(sender)) {
                    reload.execute(sender, new String[0], "mt");
                }
                return true;
            }
            if (label.equalsIgnoreCase("mtplayerinfo")) {
                sender.sendMessage("§7請改用 /mt playerinfo <玩家>");
                return true;
            }
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }
        SubCommand sc = map.get(args[0].toLowerCase());
        if (sc == null) {
            sender.sendMessage("§c未知子指令: " + args[0]);
            sendHelp(sender, label);
            return true;
        }
        if (!sc.hasPermission(sender)) {
            sender.sendMessage("§c缺少權限: " + sc.getPermission());
            return true;
        }
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        if (!sc.execute(sender, subArgs, label)) {
            sender.sendMessage("§e用法: " + sc.getUsage("mt"));
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§6====== /" + label + " 指令列表 ======");
        Set<SubCommand> printed = new HashSet<>(map.values());
        for (SubCommand sc : printed) {
            if (sc.isListedInHelp() && sc.hasPermission(sender)) {
                sender.sendMessage("§e" + sc.getUsage("mt") + " §7- " + sc.getDescription());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> bases = new ArrayList<>();
            Set<SubCommand> uniq = new HashSet<>(map.values());
            for (SubCommand sc : uniq) {
                if (sc.isListedInHelp() && sc.hasPermission(sender)) {
                    bases.add(sc.getName());
                }
            }
            return StringUtil.copyPartialMatches(args[0], bases, new ArrayList<>());
        }
        SubCommand sc = map.get(args[0].toLowerCase());
        if (sc == null || !sc.hasPermission(sender)) return Collections.emptyList();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return sc.tabComplete(sender, subArgs);
    }
}