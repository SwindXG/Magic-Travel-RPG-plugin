package com.example.command.sub;

import com.example.Main;
import org.bukkit.command.CommandSender;
import java.util.Collections;
import java.util.List;

public class ReloadSubCommand implements SubCommand {
    private final Main plugin;
    public ReloadSubCommand(Main plugin){ this.plugin = plugin; }
    @Override public String getName() { return "reload"; }
    @Override public String getDescription() { return "重載插件設定"; }
    @Override public String getPermission() { return "mt.reload"; }
    @Override public String getUsage(String root) { return "/" + root + " reload"; }
    @Override public List<String> getAliases() { return Collections.emptyList(); }
    @Override public boolean isListedInHelp() { return true; }
    @Override public boolean execute(CommandSender sender, String[] args, String rootLabel) {
        if (args.length != 0) return false;
        plugin.reloadAll();
        sender.sendMessage("§a已重新載入設定。");
        return true;
    }
    @Override public List<String> tabComplete(CommandSender sender, String[] args) { return Collections.emptyList(); }
}