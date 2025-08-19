package com.example.events; // 套件：事件相關類別集中放這裡，方便維護

import com.example.Main;                     // 主插件類，用來取得玩家屬性 Map 與設定檔
import com.example.data.PlayerAttribute;     // 玩家自訂屬性資料（含 fireAttack）
import com.example.util.ElementDamageUtil;     // 額外傷害工具類，負責安全追加第二段火焰傷害
import org.bukkit.event.Listener;            // 事件監聽介面
import org.bukkit.event.EventHandler;        // 事件處理方法註解
import org.bukkit.event.EventPriority;       // 事件優先順序列舉
import org.bukkit.event.entity.EntityDamageByEntityEvent; // 實體被另一實體攻擊事件（近戰/射擊等）
import org.bukkit.entity.Player;             // 玩家類
import org.bukkit.entity.LivingEntity;       // 任何可受傷害且具有生命的實體

/**
 * FireAttackListener
 * 功能：
 *  - 監聽玩家對目標造成的基礎傷害事件
 *  - 不修改原事件傷害（避免與其他插件衝突）
 *  - 依照玩家 fireAttack 屬性額外施加一段「獨立」的火焰傷害
 *  - 追加燃燒效果（點燃目標）
 * 設計要點：
 *  - 使用 ExtraDamageUtil 以 metadata 旗標避免額外傷害再次觸發自身（防遞迴）
 *  - 讀取倍率與秒數設定皆從 config.yml，可熱重載
 */
public class FireAttackListener implements Listener {

    // 保存主插件實例：取得玩家屬性、讀 config、排程等
    private final Main plugin;

    /**
     * 建構子 (依賴注入)
     * @param plugin 主插件實例
     * 使用依賴注入而非 static，降低耦合、利於測試
     */
    public FireAttackListener(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * onHit
     * 監聽實體受到另一實體傷害事件：
     * 1. 確認攻擊者是玩家
     * 2. 確認被攻擊者是活體
     * 3. 過濾掉我們自己補的額外火焰傷害（避免遞迴）
     * 4. 讀取玩家 fireAttack 點數，若 <=0 直接結束
     * 5. 計算火焰額外傷害（不動原事件傷害）
     * 6. 呼叫工具類追加第二段 damage()
     * 7. 計算燃燒秒數並套用（僅延長，不縮短）
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) //事件優先級normal
    public void onHit(EntityDamageByEntityEvent e) { //e是事件變數

        // (A) 攻擊者不是玩家 → 不處理
        if (!(e.getDamager() instanceof Player attacker)) {
            return;
        }

        // (B) 受擊者不是活體（例如盔甲座、物品框）→ 不處理
        if (!(e.getEntity() instanceof LivingEntity target)) {
            return;
        }

        // (C) 若這次觸發其實是我們後面「額外火焰傷害」造成的第二段 damage：
        //     利用工具類的 metadata 旗標識別 → 避免再計算（防止無限遞迴）
        if (ElementDamageUtil.isElementExtra(target)) {
            return;
        }

        // (D) 取得玩家屬性物件
        PlayerAttribute attr = plugin.getPlayerAttribute(attacker.getUniqueId());

        // (E) 讀取火焰攻擊點數
        int fireAttack = attr.getFireAttack();

        // (F) 沒有火焰攻擊點數 → 不做任何額外處理
        if (fireAttack <= 0) {
            return;
        }

        // (G) 從 config.yml 讀取每點火焰攻擊對應的額外傷害數值（線性）
        //     key: damage.fire.extra-per-point
        double extraPerPoint = plugin.getConfig().getDouble("damage.fire.extra-per-point", 0.6D);

        // (H) 計算第二段額外火焰傷害（不乘原事件傷害，不影響其他插件可能已改動的 e.getDamage()）
        double fireDamage = fireAttack * extraPerPoint;

        // (I) 呼叫工具類補上獨立傷害：
        //     工具類內會：
        //       1. 下一 tick 標記 metadata → 呼叫 target.damage()
        //       2. 再下一 tick 清旗標
        ElementDamageUtil.applyElementExtra(plugin, attacker, target, fireDamage);
        ElementDamageUtil.showDamageHologram(plugin, target, fireDamage); // fireDamage 為本次造成的傷害數值
        // (J) 燃燒秒數參數：每點增加 igniteSecPer 秒，上限 maxIgniteSec 秒
        double igniteSecPer = plugin.getConfig().getDouble("damage.fire.ignite-seconds-per-point", 0.2D);
        double maxIgniteSec = plugin.getConfig().getDouble("damage.fire.max-ignite-seconds", 6D);

        // (K) 實際燃燒秒數 = min(上限, 點數 * 每點秒數)
        double finalSeconds = Math.min(maxIgniteSec, fireAttack * igniteSecPer);

        // (L) 換算成 tick（20 tick = 1 秒），再轉成 int
        int ticks = (int) (finalSeconds * 20.0D);

        // (M) 若計算結果 > 0 而且當前的燃燒時間比較短 → 延長
        if (ticks > 0 && target.getFireTicks() < ticks) {
            target.setFireTicks(ticks);
        }

        // (N) 除錯或顯示（選擇性）
        // attacker.sendMessage("火焰額外傷害: " + fireDamage + "  燃燒: " + finalSeconds + "s");
    }
}