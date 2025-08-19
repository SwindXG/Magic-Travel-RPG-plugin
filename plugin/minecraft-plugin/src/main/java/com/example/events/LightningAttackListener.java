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

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 雷屬性：
 *  - 額外雷傷 (獨立傷害)
 *  - 連鎖 (可遞減)
 *  - 眩暈 (高等級緩速+挖掘疲勞模擬)
 */
public class LightningAttackListener implements Listener {

    private final Main plugin;

    public LightningAttackListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity firstTarget)) return;
        if (ElementDamageUtil.isElementExtra(firstTarget)) return; // 避免遞迴

        PlayerAttribute attr = plugin.getPlayerAttribute(attacker.getUniqueId());
        int lightning = attr.getLightningAttack();
        if (lightning <= 0) return;

        double per = plugin.getConfig().getDouble("damage.lightning.extra-per-point", 0.70D);
        double baseExtra = lightning * per;

        // 對第一個目標施加獨立雷傷
        ElementDamageUtil.applyElementExtra(plugin, attacker, firstTarget, baseExtra);
        ElementDamageUtil.showDamageHologram(plugin, firstTarget, baseExtra); // baseExtra 為本次造成的傷害數值
        // 眩暈
        double chancePer = plugin.getConfig().getDouble("damage.lightning.stun.chance-per-point", 0.015D);
        double maxChance = plugin.getConfig().getDouble("damage.lightning.stun.max-chance", 0.5D);
        double chance = Math.min(maxChance, lightning * chancePer);
        if (chance > 0 && ThreadLocalRandom.current().nextDouble() < chance) {
            int stunTicks = plugin.getConfig().getInt("damage.lightning.stun.ticks", 40);
            // SLOW 高等級 + SLOW_DIGGING 模擬行動受阻
            firstTarget.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stunTicks, 10, true, true));
            firstTarget.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, stunTicks, 5, true, true));
        }

        // 連鎖
        if (plugin.getConfig().getBoolean("damage.lightning.chain.enabled", true)) {
            int maxTargets = plugin.getConfig().getInt("damage.lightning.chain.max-targets", 3);
            double radius = plugin.getConfig().getDouble("damage.lightning.chain.radius", 6D);
            double scalePerHop = plugin.getConfig().getDouble("damage.lightning.chain.damage-scale-per-hop", 0.7D);

            if (maxTargets > 0 && scalePerHop > 0) {
                List<LivingEntity> nearby = firstTarget.getLocation().getWorld()
                        .getNearbyEntities(firstTarget.getLocation(), radius, radius, radius)
                        .stream()
                        .filter(e2 -> e2 instanceof LivingEntity)
                        .map(e2 -> (LivingEntity) e2)
                        .filter(le -> le != firstTarget && le != attacker)
                        .filter(le -> !ElementDamageUtil.isElementExtra(le))
                        .limit(maxTargets)
                        .collect(Collectors.toList());

                int hop = 1;
                for (LivingEntity chainTarget : nearby) {
                    double hopDamage = baseExtra * Math.pow(scalePerHop, hop);
                    if (hopDamage <= 0.01) break;
                    ElementDamageUtil.applyElementExtra(plugin, attacker, chainTarget, hopDamage);
                    hop++;
                }
            }
        }

        // 可選 debug:
        // attacker.sendMessage("雷額外傷害:" + baseExtra);
    }
}