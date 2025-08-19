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
import org.bukkit.util.Vector;

/**
 * 風屬性：
 *  - 額外風傷 (獨立 damage)
 *  - 擊退 (水平 + 輕微上升)
 *  - 攻擊者獲得暫時速度提升
 */
public class WindAttackListener implements Listener {

    private final Main plugin;

    public WindAttackListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;
        if (ElementDamageUtil.isElementExtra(target)) return; // 重用旗標避免遞迴

        PlayerAttribute attr = plugin.getPlayerAttribute(attacker.getUniqueId());
        int windAttack = attr.getWindAttack();
        if (windAttack <= 0) return;

        // 額外風傷
        double per = plugin.getConfig().getDouble("damage.wind.extra-per-point", 0.45D);
        double windDamage = windAttack * per;
        ElementDamageUtil.applyElementExtra(plugin, attacker, target, windDamage);
        ElementDamageUtil.showDamageHologram(plugin, target, windDamage); // windDamage 為本次造成的傷害數值
        // 擊退
        double baseKb = plugin.getConfig().getDouble("damage.wind.knockback-base", 0.4D);
        double incKb = plugin.getConfig().getDouble("damage.wind.knockback-per-point", 0.03D);
        double vertical = plugin.getConfig().getDouble("damage.wind.knockback-vertical", 0.25D);
        Vector dir = target.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
        double horizontal = baseKb + windAttack * incKb;
        Vector kb = dir.multiply(horizontal);
        kb.setY(Math.min(0.8D, vertical));
        target.setVelocity(target.getVelocity().add(kb));

        // 給攻擊者 Speed
        double secPer = plugin.getConfig().getDouble("damage.wind.speed-seconds-per-point", 0.10D);
        double maxSec = plugin.getConfig().getDouble("damage.wind.max-speed-seconds", 4D);
        int every = plugin.getConfig().getInt("damage.wind.speed-amplifier-every", 15);
        double totalSec = Math.min(maxSec, windAttack * secPer);
        int ticks = (int) (totalSec * 20);
        if (ticks > 0) {
            int amp = every <= 0 ? 0 : windAttack / every;
            PotionEffect old = attacker.getPotionEffect(PotionEffectType.SPEED);
            boolean apply = true;
            if (old != null) {
                if (old.getAmplifier() > amp) apply = false;
                else if (old.getAmplifier() == amp && old.getDuration() >= ticks) apply = false;
            }
            if (apply) {
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, amp, true, true));
            }
        }
        // 可選 debug:
        // attacker.sendMessage("風傷+" + windDamage + " 擊退:" + horizontal);
    }
}
