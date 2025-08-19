package com.example; // 套件宣告：主插件類所在的命名空間

import java.util.HashMap;             // 玩家屬性快取用
import java.util.UUID;                // 玩家唯一識別碼

import org.bukkit.Bukkit;             // Bukkit API 入口
import org.bukkit.ChatColor;          // 聊天/控制台顏色轉換
import org.bukkit.configuration.file.FileConfiguration; // 主 config.yml 操作介面
import org.bukkit.configuration.file.YamlConfiguration; // 讀取外部 yml（defaultattribute.yml）
import java.io.File;                  // 檔案物件
import java.io.IOException;           // IO 例外
import org.bukkit.plugin.java.JavaPlugin; // Bukkit 插件基底類

// 指令
import com.example.command.PlayerInfoCommand;   // /mtplayerinfo
import com.example.command.ReloadPluginCommand; // /mtreload
import com.example.command.AdminCommand;      // /mt admin <subcommand>

// 玩家資料與屬性
import com.example.data.PlayerAttribute;    // 玩家屬性資料模型
import com.example.data.PlayerDataManager;  // 個別玩家 yml 資料管理

// 事件監聽支援
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

// 各元素/攻擊監聽
import com.example.events.DarkAttackListener;
import com.example.events.FireAttackListener;
import com.example.events.GrassAttackListener;
import com.example.events.IceAttackListener;
import com.example.events.LightAttackListener;
import com.example.events.LightningAttackListener;
import com.example.events.WaterAttackListener;
import com.example.events.WindAttackListener;
import com.example.events.NormalAttackListener;

// 額外工具
import com.example.util.ElementDamageUtil; // 浮空傷害顯示（ArmorStand 版）

/**
 * 插件主類：
 * 負責：
 * 1. 啟動/關閉流程
 * 2. 指令與監聽器註冊
 * 3. 訊息 & 預設屬性設定載入
 * 4. 玩家屬性快取 / userdata 分流
 * 5. /mtreload 統一重載邏輯
 */
public class Main extends JavaPlugin {

    // 已載入玩家屬性快取：只對「曾經取得過」或「在線」的玩家建立
    private final HashMap<UUID, PlayerAttribute> attributeMap = new HashMap<>();

    // 玩家個別檔案管理器（userdata/UUID.yml）
    private PlayerDataManager dataManager;

    // 啟動 / 關閉訊息與廣播 flag（從 config.yml -> messages.* 讀入後快取）
    private String startupLog;
    private String shutdownLog;
    private boolean broadcastStart;
    private boolean broadcastStop;
    private String broadcastStartMsg;
    private String broadcastStopMsg;

    // defaultattribute.yml 管理：僅供「首次建立玩家檔案」時使用
    private File defaultAttrFile;
    private YamlConfiguration defaultAttrConfig;

    // ───────────────────────────────── Lifecycle ─────────────────────────────────

    @Override
    public void onEnable() {
        saveDefaultConfig();                 // 若無 config.yml 則從 jar 複製
        dataManager = new PlayerDataManager(this); // 建立 /plugins/本插件/userdata
        setupDefaultAttrConfig();            // 載入 defaultattribute.yml（放 jar resources）
        loadMessageConfig();                 // 讀取 messages.* 填入快取欄位
        ElementDamageUtil.reload(this);      // 讀取 damage-hologram 區塊

        logColored(startupLog);              // 控制台顯示啟動訊息（允許彩色）
        if (broadcastStart) broadcastColored(broadcastStartMsg); // 可選廣播

        // 指令綁定
        getCommand("mtreload").setExecutor(new ReloadPluginCommand(this));
        getCommand("mtplayerinfo").setExecutor(new PlayerInfoCommand(this));
        getCommand("mt").setExecutor(new AdminCommand(this));

        // 事件監聽器註冊（每個元素傷害邏輯自行在對應 Listener 中實作）
        getServer().getPluginManager().registerEvents(new FireAttackListener(this), this);
        getServer().getPluginManager().registerEvents(new WaterAttackListener(this), this);
        getServer().getPluginManager().registerEvents(new IceAttackListener(this), this);
        getServer().getPluginManager().registerEvents(new WindAttackListener(this), this);
        getServer().getPluginManager().registerEvents(new LightningAttackListener(this), this);
        getServer().getPluginManager().registerEvents(new GrassAttackListener(this), this);
        getServer().getPluginManager().registerEvents(new DarkAttackListener(this), this);
        getServer().getPluginManager().registerEvents(new LightAttackListener(this), this);
        getServer().getPluginManager().registerEvents(new NormalAttackListener(this), this);

        // 玩家登入 / 離線資料載入保存
        getServer().getPluginManager().registerEvents(new SessionListener(), this);
    }

    @Override
    public void onDisable() {
        // 關閉前最後保存（attributeMap 內所有玩家）
        if (dataManager != null) dataManager.saveAll(attributeMap.values());
        logColored(shutdownLog);
        if (broadcastStop) broadcastColored(broadcastStopMsg);
    }

    // ─────────────────────────────── Reload Pipeline ───────────────────────────────
    /**
     * /mtreload 時呼叫：
     * 1. 主 config.yml
     * 2. defaultattribute.yml（只對「尚未有 userdata 檔案」的玩家有效）
     * 3. 訊息、浮空字設定
     * 4. 依據是否已有 userdata 檔案決定重新載入來源
     */
    public void reloadAll() {
        reloadConfig();              // 重載 config.yml
        reloadDefaultAttrConfig();   // 重載 defaultattribute.yml
        loadMessageConfig();         // 重載訊息
        ElementDamageUtil.reload(this); // 重載浮空傷害設定
        reloadActivePlayerAttributes();  // 重新套用玩家屬性
    }

    /**
     * 針對目前快取中的玩家：
     * 有 userdata -> 直接讀取各自檔案（不再受 defaultattribute.yml）
     * 無 userdata -> 套用預設並立即建立檔案
     */
    private void reloadActivePlayerAttributes() {
        attributeMap.forEach((uuid, attr) -> {
            if (dataManager.hasData(uuid)) {
                dataManager.reload(attr);     // 從玩家自己的 UUID.yml 重載
            } else {
                attr.reloadDefaults(this);    // 套 defaultattribute.yml
                dataManager.save(attr);       // 立刻寫出檔案，後續不再受預設影響
            }
        });
    }

    // ─────────────────────────────── Messages Config ───────────────────────────────
    private void loadMessageConfig() {
        FileConfiguration c = getConfig();
        startupLog       = color(c.getString("messages.startup-log", "&aPlugin enabled."));
        shutdownLog      = color(c.getString("messages.shutdown-log", "&cPlugin disabled."));
        broadcastStart   = c.getBoolean("messages.broadcast-start-enabled", true);
        broadcastStop    = c.getBoolean("messages.broadcast-stop-enabled", true);
        broadcastStartMsg= color(c.getString("messages.broadcast-start", "&a[Plugin] Enabled"));
        broadcastStopMsg = color(c.getString("messages.broadcast-stop", "&c[Plugin] Disabled"));
    }

    // ─────────────────────────────── Utility Methods ───────────────────────────────
    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    private void logColored(String msg) {
        getServer().getConsoleSender().sendMessage(msg);
    }

    private void broadcastColored(String msg) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(msg));
    }

    // 快取取得玩家屬性；不存在則透過 dataManager.load(...) 建立/載入
    public PlayerAttribute getPlayerAttribute(UUID uuid) {
        return attributeMap.computeIfAbsent(uuid, id -> dataManager.load(id));
    }

    // 若仍有使用主 config 中 "default-attributes" 區段，可保留此方法（現流程主要使用 defaultattribute.yml）
    public org.bukkit.configuration.ConfigurationSection getDefaultAttributes() {
        return getConfig().getConfigurationSection("default-attributes");
    }

    // ───────────────────────────── defaultattribute.yml ─────────────────────────────
    private void setupDefaultAttrConfig() {
        defaultAttrFile = new File(getDataFolder(), "defaultattribute.yml");
        if (!defaultAttrFile.exists()) {
            // 從插件 jar 內的同名資源複製（需確保已打包進 resources）
            saveResource("defaultattribute.yml", false);
        }
        defaultAttrConfig = YamlConfiguration.loadConfiguration(defaultAttrFile);
    }

    public void reloadDefaultAttrConfig() {
        if (defaultAttrFile == null) setupDefaultAttrConfig();
        defaultAttrConfig = YamlConfiguration.loadConfiguration(defaultAttrFile);
    }

    public YamlConfiguration getDefaultAttrConfig() {
        return defaultAttrConfig;
    }

    public void saveDefaultAttrConfig() {
        if (defaultAttrConfig == null || defaultAttrFile == null) return;
        try {
            defaultAttrConfig.save(defaultAttrFile);
        } catch (IOException e) {
            getLogger().warning("無法保存 defaultattribute.yml: " + e.getMessage());
        }
    }

    // ───────────────────────────── Session Listener ─────────────────────────────
    /**
     * 玩家登入 / 離線監聽：
     * 加入：確保屬性載入（首次 → 建立檔案）
     * 離線：即時保存該玩家
     */
    private class SessionListener implements Listener {

        @EventHandler
        public void onJoin(PlayerJoinEvent e) {
            getPlayerAttribute(e.getPlayer().getUniqueId()); // 觸發載入即可
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent e) {
            PlayerAttribute attr = attributeMap.get(e.getPlayer().getUniqueId());
            if (attr != null) dataManager.save(attr); // 保存離線前狀態
        }
    }

    public void savePlayerAttribute(UUID uuid) { // 保存指定玩家屬性
        PlayerAttribute attr = attributeMap.get(uuid);
        if (attr != null && dataManager != null) {
            dataManager.save(attr);
        }
    }
}