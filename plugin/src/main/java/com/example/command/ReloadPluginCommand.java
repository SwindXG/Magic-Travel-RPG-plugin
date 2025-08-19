package com.example.command; // 套件：指令類

import com.example.Main; // 主插件類引用
import org.bukkit.command.Command; // 指令物件
import org.bukkit.command.CommandExecutor; // 指令執行介面
import org.bukkit.command.CommandSender; // 指令發送者（玩家 / 控制台）

/**
 * /mtreload 指令處理：
 * 呼叫 Main.reloadAll() 重新載入所有設定與玩家檔案邏輯
 */
public class ReloadPluginCommand implements CommandExecutor { // 類開始

    private final Main plugin; // 主插件引用（供呼叫 reloadAll）

    public ReloadPluginCommand(Main plugin) { // 建構子
        this.plugin = plugin; // 儲存引用
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) { // 指令回呼
        plugin.reloadAll(); // 執行統一重載流程
        sender.sendMessage("§a設定已重新載入！"); // 回覆發送者
        return true; // 回傳成功
    }
} // 類
