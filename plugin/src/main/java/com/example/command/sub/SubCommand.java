package com.example.command.sub;

import org.bukkit.command.CommandSender;
import java.util.List;

public interface SubCommand {
    String getName();
    String getDescription();
    String getPermission();              // 可為 null
    String getUsage(String rootLabel);   // 例: /mt reload
    List<String> getAliases();           // 可空
    boolean isListedInHelp();            // 是否顯示於 /mt
    boolean execute(CommandSender sender, String[] args, String rootLabel);
    List<String> tabComplete(CommandSender sender, String[] args);

    default boolean hasPermission(CommandSender sender) {
        String perm = getPermission();
        return perm == null || perm.isEmpty() || sender.hasPermission(perm);
    }
}