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
import java.util.concurrent.ThreadLocalRandom;

/**
 * 冰屬性：
 *  - 額外冰傷 (獨立 damage 呼叫)
 *  - 緩速 (可疊等級)
 *  - 機率凍結 (freezeTicks)
 */
public class IceAttackListener implements Listener {

    private final Main plugin;

    public IceAttackListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;

        // 避免處理我們自己新增的額外元素傷害 (可共用旗標，目前與火/水一致)
        if (ElementDamageUtil.isElementExtra(target)) return;

        PlayerAttribute attr = plugin.getPlayerAttribute(attacker.getUniqueId());
        int iceAttack = attr.getIceAttack();
        if (iceAttack <= 0) return;

        // 額外冰傷
        double extraPerPoint = plugin.getConfig().getDouble("damage.ice.extra-per-point", 0.55D);
        double iceDamage = iceAttack * extraPerPoint;
        ElementDamageUtil.applyElementExtra(plugin, attacker, target, iceDamage); // (旗標共用命名，必要時可改為通用 Element 旗標)
        ElementDamageUtil.showDamageHologram(plugin, target, iceDamage); // iceDamage 為本次造成的傷害數值
        // 緩速
        double slowSecPer = plugin.getConfig().getDouble("damage.ice.slow-seconds-per-point", 0.30D);
        double maxSlowSec = plugin.getConfig().getDouble("damage.ice.max-slow-seconds", 6D);
        int every = plugin.getConfig().getInt("damage.ice.slow-amplifier-every", 12);
        double totalSec = Math.min(maxSlowSec, iceAttack * slowSecPer);
        int ticks = (int) (totalSec * 20);
        if (ticks > 0) {
            int amplifier = every <= 0 ? 0 : iceAttack / every;
            PotionEffect old = target.getPotionEffect(PotionEffectType.SLOWNESS);
            boolean apply = true;
            if (old != null) {
                if (old.getAmplifier() > amplifier) apply = false;
                else if (old.getAmplifier() == amplifier && old.getDuration() >= ticks) apply = false;
            }
            if (apply) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, amplifier, true, true));
            }
        }

        // 凍結機率
        double per = plugin.getConfig().getDouble("damage.ice.freeze-chance-per-point", 0.02D);
        double maxChance = plugin.getConfig().getDouble("damage.ice.max-freeze-chance", 0.6D);
        double chance = Math.min(maxChance, iceAttack * per);
        if (chance > 0) {
            if (ThreadLocalRandom.current().nextDouble() < chance) {
                int freezeTicks = plugin.getConfig().getInt("damage.ice.freeze-ticks", 40);
                // 1.19+ 可使用 freezeTicks，若版本支援：
                try {
                    int current = target.getFreezeTicks();
                    if (current < freezeTicks) {
                        target.setFreezeTicks(freezeTicks);
                    }
                } catch (NoSuchMethodError ignored) {
                    // 版本不支援可忽略或改用高等級 SLOW + JUMP 限制
                }
            }
        }
        // attacker.sendMessage("冰額外傷害: " + iceDamage);
    }
}
