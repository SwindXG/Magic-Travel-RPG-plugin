package com.example.events;

import com.example.Main;
import com.example.data.PlayerAttribute;
import com.example.util.ElementDamageUtil;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 水屬性攻擊：
 *  - 額外水傷（獨立傷害呼叫）
 *  - 緩速效果（依點數給秒數與等級）
 *  - 熄滅火焰（可設定）
 *  與火屬性類似，保持原始事件傷害不變。
 */
public class WaterAttackListener implements Listener{

    private final Main plugin;

    public WaterAttackListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;

    // 避免我們自己加的额外元素傷害再觸發
    if (ElementDamageUtil.isElementExtra(target)) return;

    PlayerAttribute attr = plugin.getPlayerAttribute(attacker.getUniqueId());
    int waterAttack = attr.getWaterAttack();
    if (waterAttack <= 0) return;

    // 額外水傷（固定點數 * 係數）
    double extraPerPoint = plugin.getConfig().getDouble("damage.water.extra-per-point", 0.5D);
    double waterDamage = waterAttack * extraPerPoint;

    // 使用同一工具類（名稱雖是 Fire 旗標；若要嚴謹可做共用 ElementFlag）
        ElementDamageUtil.applyElementExtra(plugin, attacker, target, waterDamage);
        ElementDamageUtil.showDamageHologram(plugin, target, waterDamage); // waterDamage 為本次造成的傷害數值
    // 緩速
    double slowSecPer = plugin.getConfig().getDouble("damage.water.slow-seconds-per-point", 0.25D);
    double maxSlowSec = plugin.getConfig().getDouble("damage.water.max-slow-seconds", 5D);
    double totalSec = Math.min(maxSlowSec, waterAttack * slowSecPer);
    int durationTicks = (int) (totalSec * 20);
    if (durationTicks > 0) {
        int every = plugin.getConfig().getInt("damage.water.slow-amplifier-every", 10);
        int amplifier = Math.max(0, every <= 0 ? 0 : (waterAttack / every)); // 0 為速度 I
        PotionEffect old = target.getPotionEffect(PotionEffectType.SLOWNESS);
        // 只延長或提升（不縮短）
        boolean apply = true;
    if (old != null) {
        if (old.getAmplifier() > amplifier) apply = false;
        else if (old.getAmplifier() == amplifier && old.getDuration() >= durationTicks) apply = false;
        }
    if (apply) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, amplifier, true, true));
        }
    }

    // 熄滅火焰（設定可控）
        boolean extinguish = plugin.getConfig().getBoolean("damage.water.extinguish-fire", true);
        if (extinguish && target.getFireTicks() > 0) {
            target.setFireTicks(0);
        }

        // 可除錯：
        // attacker.sendMessage("水屬額外傷害: " + waterDamage + " 緩速: " + totalSec + "s");
    }
}
