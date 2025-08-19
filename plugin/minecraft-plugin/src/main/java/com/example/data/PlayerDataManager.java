package com.example.data; // 套件宣告

import com.example.Main; // 引入主插件類
import org.bukkit.configuration.file.YamlConfiguration; // Yaml 讀寫工具
import java.io.File; // 檔案類
import java.io.IOException; // 例外處理
import java.util.UUID; // UUID 類

/**
 * 玩家資料檔案管理器：在 userdata 目錄中以 UUID.yml 儲存每位玩家屬性 // 類說明
 */
public class PlayerDataManager { // 類開始

    private final Main plugin; // 主插件實例
    private final File folder; // userdata 目錄

    public PlayerDataManager(Main plugin) { // 建構子
        this.plugin = plugin; // 指派插件
        this.folder = new File(plugin.getDataFolder(), "userdata"); // 建立 userdata 目錄 File
        if (!folder.exists()) folder.mkdirs(); // 若不存在則建立
    } // 建構子結束

    public boolean hasData(UUID uuid) { // 檢查是否已有玩家檔案
        return new File(folder, uuid + ".yml").exists(); // 回傳檔案存在與否
    } // hasData 結束

    public PlayerAttribute load(UUID uuid) { // 載入玩家屬性（首次會建立）
        File f = new File(folder, uuid + ".yml"); // 生成檔案路徑
        PlayerAttribute attr = new PlayerAttribute(uuid, plugin); // 先用 defaultattribute 初始化
        if (f.exists()) { // 若檔案已存在
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(f); // 讀取 yml
            attr.loadFromFileSection(yml); // 將檔案數值覆蓋屬性
        } else {
            save(attr); // 沒檔案：立即保存（之後不再受 defaultattribute 影響）
        }
        return attr; // 回傳屬性
    } // load 結束

    public void reload(PlayerAttribute attr) { // 重新從玩家專屬檔案載入
        File f = new File(folder, attr.getUuid() + ".yml"); // 取得檔案
        if (!f.exists()) return; // 沒檔案直接返回
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(f); // 載入 yml
        attr.loadFromFileSection(yml); // 覆蓋屬性值
    } // reload 結束

    public void save(PlayerAttribute attr) { // 保存單一玩家
        if (attr == null) return; // 空物件防護
        File f = new File(folder, attr.getUuid() + ".yml"); // 目標檔案
        YamlConfiguration yml = new YamlConfiguration(); // 建立 yml 容器
        attr.saveToFileSection(yml); // 寫入屬性值
        try { // 嘗試保存
            yml.save(f); // 寫入磁碟
        } catch (IOException e) { // 捕捉例外
            plugin.getLogger().warning("[UserData] 保存失敗: " + f.getName() + " -> " + e.getMessage()); // 輸出警告
        }
    } // save 結束

    public void saveAll(Iterable<PlayerAttribute> all) { // 批次保存
        for (PlayerAttribute a : all) save(a); // 逐個保存
    } // saveAll 結束
} // 類結束