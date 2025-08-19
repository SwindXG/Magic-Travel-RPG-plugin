package com.example.util; // 套件名稱

import org.bukkit.Bukkit; // 用於主執行緒排程
import org.bukkit.ChatColor; // 顏色與格式轉換
import org.bukkit.Location; // 座標位置
import org.bukkit.World; // 世界物件
import org.bukkit.configuration.ConfigurationSection; // 讀取設定節點
import org.bukkit.entity.ArmorStand; // 盔甲座（用作浮空字載體）
import org.bukkit.entity.LivingEntity; // 活體實體
import org.bukkit.entity.Player; // 玩家實體
import org.bukkit.metadata.FixedMetadataValue; // 固定值 metadata
import org.bukkit.plugin.Plugin; // 插件介面
import org.bukkit.scheduler.BukkitRunnable; // 排程任務

import java.text.DecimalFormat; // 數字格式化
import java.util.ArrayList; // 動態陣列
import java.util.Comparator; // 比較器
import java.util.List; // List 介面

/**
 * 通用元素額外傷害與浮空傷害字工具，集中避免遞迴與外觀設定。 // 類別說明
 */
public final class ElementDamageUtil { // final：不可被繼承

    private static final String FLAG = "extra_element_damage_flag"; // metadata 標記 key 用於辨識額外元素傷害

    // -------- Hologram 風格快取欄位（由 config 載入） --------
    private static boolean holoEnabled = true; // 是否啟用浮空字
    private static int holoDuration = 20; // 浮空字存活總 tick
    private static int holoInterval = 2; // 更新間隔 tick
    private static double holoRisePer = 0.04; // 每次更新上升距離
    private static double holoYOffset = 0.5; // 基礎垂直偏移（頭頂再上）
    private static String prefix = ""; // 顯示字串前綴
    private static String suffix = ""; // 顯示字串後綴
    private static boolean thousandSep = false; // 是否使用千分位
    private static int decimalPlaces = 1; // 小數位數
    private static List<Phase> phases = List.of( // 預設階段格式列表
            new Phase(0.0, "&c&l-{damage}"), // 起始：紅色粗體
            new Phase(0.6, "&c-{damage}"), // 中段：紅色
            new Phase(0.9, "&7-{damage}") // 結尾：灰色
    );
    // 隨機位置參數
    private static double holoRandomRadius = 0.0; // 水平隨機半徑
    private static double holoRandomYJitter = 0.0; // 垂直隨機擺動

    private record Phase(double timeRatio, String rawFormat) {} // Phase 紀錄：時間比例與格式模板

    private ElementDamageUtil(){} // 私有建構子：防實例化

    /**
     * 從 config 重新載入浮空字相關設定。 // 方法說明
     * @param plugin 插件實例 // 參數：插件
     */
    public static void reload(Plugin plugin) { // reload 方法開始
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("damage-hologram"); // 取得設定節點
        if (sec == null) return; // 無節點直接返回
        holoEnabled = sec.getBoolean("enabled", true); // 是否啟用
        decimalPlaces = Math.max(0, sec.getInt("decimal-places", 1)); // 小數位限制
        prefix = sec.getString("prefix", ""); // 讀取前綴
        suffix = sec.getString("suffix", ""); // 讀取後綴
        holoDuration = Math.max(1, sec.getInt("duration-ticks", 20)); // 存活時間
        holoInterval = Math.max(1, sec.getInt("update-interval-ticks", 2)); // 更新間隔
        holoRisePer = sec.getDouble("rise-per-interval", 0.04D); // 上升距離
        holoYOffset = sec.getDouble("y-offset", 0.5D); // Y 偏移
        thousandSep = sec.getBoolean("thousand-separator", false); // 千分位
        holoRandomRadius = Math.max(0D, sec.getDouble("random-radius", 0D)); // 隨機水平半徑
        holoRandomYJitter = Math.max(0D, sec.getDouble("random-y-jitter", 0D)); // 隨機垂直抖動

        List<Phase> loaded = new ArrayList<>(); // 暫存載入的 Phase
        ConfigurationSection phaseSec = sec.getConfigurationSection("phases"); // 取得 phases 子節點
        if (phaseSec != null) { // 若存在
            for (String key : phaseSec.getKeys(false)) { // 遍歷所有階段 key
                double tr = Math.max(0, Math.min(1, phaseSec.getDouble(key + ".time-ratio", 0))); // 讀取時間比例並夾限
                String fmt = phaseSec.getString(key + ".text", "&c-{damage}"); // 讀取格式字串
                loaded.add(new Phase(tr, fmt)); // 新增 Phase
            }
        }
        if (!loaded.isEmpty()) { // 若有自訂
            loaded.sort(Comparator.comparingDouble(Phase::timeRatio)); // 按比例排序
            phases = loaded; // 套用新列表
        }
    } // reload 方法結束

    /**
     * 施加額外元素傷害（走 Bukkit 管線）並標記避免遞迴。 // 方法說明
     * @param plugin 插件
     * @param attacker 攻擊玩家
     * @param target 目標實體
     * @param dmg 額外傷害數值
     */
    public static void applyElementExtra(Plugin plugin, Player attacker, LivingEntity target, double dmg) { // 方法開始
        if (dmg <= 0 || attacker == null || target == null) return; // 基本參數檢查
        Bukkit.getScheduler().runTask(plugin, () -> { // 切回主執行緒
            if (!target.isValid() || target.isDead()) return; // 目標失效則退出
            target.setMetadata(FLAG, new FixedMetadataValue(plugin, true)); // 設置遞迴防護標記
            target.damage(dmg, attacker); // 呼叫原生傷害（觸發事件）
            Bukkit.getScheduler().runTask(plugin, () -> target.removeMetadata(FLAG, plugin)); // 下一 tick 移除標記
        });
    } // applyElementExtra 結束

    /**
     * 判斷是否為我們施加的額外元素傷害事件。 // 方法說明
     * @param entity 實體
     * @return 是否帶有標記
     */
    public static boolean isElementExtra(LivingEntity entity) { // 方法開始
        return entity != null && entity.hasMetadata(FLAG); // 回傳是否具有 metadata
    } // isElementExtra 結束

    /**
     * 生成傷害浮空字（依設定套用樣式與隨機位置）。 // 方法說明
     * @param plugin 插件
     * @param target 目標
     * @param damage 傷害數值
     */
    public static void showDamageHologram(Plugin plugin, LivingEntity target, double damage) { // 方法開始
        if (!holoEnabled || target == null || !target.isValid()) return; // 若未啟用或目標無效則返回
        Location base = target.getLocation().add(0, target.getHeight() + holoYOffset, 0); // 基礎顯示位置
        if (holoRandomRadius > 0) { // 若啟用水平隨機
            double dist = Math.random() * holoRandomRadius; // 隨機距離
            double angle = Math.random() * Math.PI * 2; // 隨機角度
            base.add(Math.cos(angle) * dist, 0, Math.sin(angle) * dist); // 套用平面偏移
        }
        if (holoRandomYJitter > 0) { // 若啟用垂直 jitter
            base.add(0, (Math.random() * 2 - 1) * holoRandomYJitter, 0); // 加入上下隨機
        }
        World world = target.getWorld(); // 取得世界
        ArmorStand stand = world.spawn(base, ArmorStand.class, as -> { // 生成盔甲座
            as.setVisible(false); // 隱藏模型
            as.setMarker(true); // 標記模式（無碰撞）
            as.setGravity(false); // 禁止重力
            as.setSmall(true); // 小型
            as.setInvulnerable(true); // 無敵
            as.setCustomNameVisible(true); // 顯示名稱
        });

        final int total = holoDuration; // 總存活 tick
        final int interval = holoInterval; // 更新間隔

        new BukkitRunnable() { // 建立定時任務
            int age = 0; // 已經過 tick
            @Override
            public void run() { // 每次執行
                if (!stand.isValid()) { // 若已失效
                    cancel(); // 取消任務
                    return; // 返回
                }
                double ratio = (double) age / total; // 計算時間比例
                String rendered = renderPhaseText(ratio, damage); // 取得對應階段字串
                stand.setCustomName(rendered); // 設定名稱

                Location l = stand.getLocation(); // 目前位置
                l.add(0, holoRisePer, 0); // 上升
                stand.teleport(l); // 移動

                age += interval; // 累加時間
                if (age >= total) { // 超過存活
                    stand.remove(); // 移除盔甲座
                    cancel(); // 停止任務
                }
            }
        }.runTaskTimer(plugin, 0L, interval); // 啟動排程（0 延遲）
    } // showDamageHologram 結束

    /**
     * 根據時間比例與傷害值渲染對應階段文字。 // 方法說明
     * @param timeRatio 時間進度 0~1
     * @param damage 傷害值
     * @return 已套顏色格式字串
     */
    private static String renderPhaseText(double timeRatio, double damage) { // 方法開始
        Phase chosen = phases.get(phases.size() - 1); // 預設最後階段
        for (Phase p : phases) { // 遍歷階段
            if (timeRatio >= p.timeRatio) chosen = p; // 若符合時間比例更新選擇
            else break; // 比例已超過排序後續不用看
        }
        String number = formatNumber(damage); // 格式化傷害數值
        String text = chosen.rawFormat // 取得原始格式
                .replace("{damage}", number) // 取代 {damage}
                .replace("{value}", number); // 取代 {value}
        if (!prefix.isEmpty()) text = prefix + text; // 套前綴
        if (!suffix.isEmpty()) text = text + suffix; // 套後綴
        return ChatColor.translateAlternateColorCodes('&', text); // 翻譯顏色代碼
    } // renderPhaseText 結束

    /**
     * 依設定格式化數字（千分位 / 小數位）。 // 方法說明
     * @param dmg 傷害原值
     * @return 格式化字串
     */
    private static String formatNumber(double dmg) { // 方法開始
        String pattern; // 建立格式模板
        if (thousandSep) { // 使用千分位
            pattern = "#,##0"; // 整數模板
            if (decimalPlaces > 0) pattern += "." + "0".repeat(decimalPlaces); // 加小數位
        } else { // 不使用千分位
            pattern = "0"; // 基本整數
            if (decimalPlaces > 0) pattern += "." + "0".repeat(decimalPlaces); // 加小數位
        }
        DecimalFormat df = new DecimalFormat(pattern); // 建立格式器
        return df.format(dmg); // 回傳格式化結果
    } // formatNumber 結束
} // 類別結束