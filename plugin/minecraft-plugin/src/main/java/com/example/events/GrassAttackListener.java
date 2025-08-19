package com.example.events;

import com.example.Main;
import com.example.data.PlayerAttribute;
import com.example.util.ElementDamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 草屬性：
 *  - 初始額外草傷 (獨立 damage)
 *  - 施加持續傷害 DoT（每秒一次）
 *  - 吸血：初始 + DoT 總傷害 * 百分比 回復攻擊者
 *  - 若 DoT 已存在則只延長剩餘秒數（不疊加倍增）
 */
public class GrassAttackListener implements Listener {

    private static final String GRASS_DOT_META = "grass_dot_flag";
    private final Main plugin;

    public GrassAttackListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;
        if (ElementDamageUtil.isElementExtra(target)) return; // 避免遞迴處理

        PlayerAttribute attr = plugin.getPlayerAttribute(attacker.getUniqueId());
        int grass = attr.getGrassAttack();
        if (grass <= 0) return;

        // 初始草傷
        double per = plugin.getConfig().getDouble("damage.grass.extra-per-point", 0.40D);
        double baseExtra = grass * per;
        if (baseExtra <= 0) return;

        // 施加初始獨立傷害
        ElementDamageUtil.applyElementExtra(plugin, attacker, target, baseExtra);
        ElementDamageUtil.showDamageHologram(plugin, target, baseExtra); // baseExtra 為本次造成的傷害數值
        // DoT 秒數
        double secPer = plugin.getConfig().getDouble("damage.grass.dot.seconds-per-point", 0.35D);
        double maxSec = plugin.getConfig().getDouble("damage.grass.dot.max-seconds", 6D);
        double totalSec = Math.min(maxSec, grass * secPer);
        int totalTicks = (int) Math.round(totalSec * 20);
        if (totalTicks <= 0) return;

        // 每秒傷害：依初始草傷百分比
        double pctPerSecond = plugin.getConfig().getDouble("damage.grass.dot.damage-percent-per-second", 0.30D);
        double perTickBase = Math.max(
                plugin.getConfig().getDouble("damage.grass.dot.min-damage", 0.5D),
                baseExtra * pctPerSecond
        );

        // 若目標已有 DoT：延長而不重複啟多個
        int extendTicks = extendExistingDot(target, totalTicks);
        if (extendTicks > 0) {
            // 仍做吸血：只有初始草傷 + 預估新增延長部分的 DoT
            applyLifeSteal(attacker, baseExtra + perTickBase * (extendTicks / 20.0), baseExtra);
            return;
        }

        // 設定 metadata 保存剩餘 tick
        AtomicInteger remain = new AtomicInteger(totalTicks);
        target.setMetadata(GRASS_DOT_META, new FixedMetadataValue(plugin, remain));

        // 計算總 DoT 次數 (每秒一次 => 每 20 tick)
        int period = 20;

        // 開始排程
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!target.isValid() || target.isDead()) {
                target.removeMetadata(GRASS_DOT_META, plugin);
                task.cancel();
                return;
            }
            int r = remain.addAndGet(-period);
            if (r + period > totalTicks) { // 第一輪 (初始) 時回復吸血：初始 + 全 DoT 預估
                // 吸血一次即可（含全部預估 DoT）
            }
            // 施加 DoT 傷害
            ElementDamageUtil.applyElementExtra(plugin, attacker, target, perTickBase);

            if (r <= 0) {
                target.removeMetadata(GRASS_DOT_META, plugin);
                task.cancel();
            }
        }, period, period);

        // 吸血：初始 + 全長 DoT 預估 (不含延長後續)
        applyLifeSteal(attacker, baseExtra + perTickBase * (totalTicks / 20.0), baseExtra);
    }

    // 嘗試延長既有 DoT，回傳延長的 tick；若沒有既有 DoT 回傳 0
    private int extendExistingDot(LivingEntity target, int addTicks) {
        List<MetadataValue> list = target.getMetadata(GRASS_DOT_META);
        for (MetadataValue mv : list) {
            if (mv.value() instanceof AtomicInteger ai) {
                int before = ai.get();
                int after = Math.min(before + addTicks, before + addTicks); // 可加上最大上限邏輯
                ai.set(after);
                return addTicks;
            }
        }
        return 0;
    }

    private void applyLifeSteal(Player attacker, double totalDamageForLifeSteal, double baseExtra) {
        double percent = plugin.getConfig().getDouble("damage.grass.lifesteal.percent", 0.25D);
        double heal = totalDamageForLifeSteal * percent;
        if (heal <= 0) return;
        double max = attacker.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        attacker.setHealth(Math.min(max, attacker.getHealth() + heal));
        // 可選提示：
        // attacker.sendMessage(String.format("草屬吸收 +%.1f HP (初始: %.1f)", heal, baseExtra));
    }
}
