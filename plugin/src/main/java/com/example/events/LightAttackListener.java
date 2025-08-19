package com.example.events;

import com.example.Main;
import com.example.data.PlayerAttribute;
import com.example.util.ElementDamageUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class LightAttackListener implements Listener {

    private static final String DARK_MARK_META = "dark_mark_stack";
    private final Main plugin;

    private static final Set<PotionEffectType> NEGATIVE = Set.of(
            PotionEffectType.WITHER,
            PotionEffectType.POISON,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.BLINDNESS,
            PotionEffectType.WEAKNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.LEVITATION,
            PotionEffectType.BAD_OMEN,
            PotionEffectType.DARKNESS
    );

    // 直接使用常量，避免 valueOf(String) 棄用
    private static final Attribute MAX_HEALTH_ATTR = Attribute.MAX_HEALTH;

    public LightAttackListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;
        if (ElementDamageUtil.isElementExtra(target)) return;

        PlayerAttribute attr = plugin.getPlayerAttribute(attacker.getUniqueId());
        if (attr == null) return;
        int light = attr.getLightAttack();
        if (light <= 0) return;

        double per = plugin.getConfig().getDouble("damage.light.extra-per-point", 0.60D);
        double baseExtra = light * per;
        if (baseExtra <= 0) return;

        double finalExtra = baseExtra;
        if (plugin.getConfig().getBoolean("damage.light.vs-dark.enabled", true)) {
            int darkStacks = getDarkStacks(target);
            if (darkStacks > 0) {
                double perStack = plugin.getConfig().getDouble("damage.light.vs-dark.damage-bonus-percent-per-dark-stack", 0.06D);
                double maxPct = plugin.getConfig().getDouble("damage.light.vs-dark.max-bonus-percent", 0.60D);
                double bonus = Math.min(maxPct, darkStacks * perStack);
                finalExtra = baseExtra * (1 + bonus);
            }
        }

        ElementDamageUtil.applyElementExtra(plugin, attacker, target, finalExtra);
        ElementDamageUtil.showDamageHologram(plugin, target, finalExtra); // finalExtra 為本次造成的傷害數值
        
        double healPercent = plugin.getConfig().getDouble("damage.light.heal-percent", 0.30D);
        double heal = finalExtra * healPercent;
        if (heal > 0) {
            double max = 20.0D;
            AttributeInstance inst = attacker.getAttribute(MAX_HEALTH_ATTR);
            if (inst != null) max = inst.getValue();
            double newHp = Math.min(max, attacker.getHealth() + heal);
            try { attacker.setHealth(newHp); } catch (IllegalArgumentException ignore) {}
        }

        double chancePer = plugin.getConfig().getDouble("damage.light.purify.chance-per-point", 0.02D);
        double maxChance = plugin.getConfig().getDouble("damage.light.purify.max-chance", 0.50D);
        double chance = Math.min(maxChance, light * chancePer);
        if (chance > 0 && ThreadLocalRandom.current().nextDouble() < chance) {
            int maxRemove = plugin.getConfig().getInt("damage.light.purify.max-remove", 1);
            purgeNegative(attacker, maxRemove);
        }
    }

    private int getDarkStacks(LivingEntity target) {
        List<MetadataValue> list = target.getMetadata(DARK_MARK_META);
        for (MetadataValue mv : list) {
            if (mv.getOwningPlugin() == plugin) {
                try { return Integer.parseInt(String.valueOf(mv.value())); } catch (NumberFormatException ignored) {}
            }
        }
        return 0;
    }

    private void purgeNegative(Player player, int maxRemove) {
        if (maxRemove <= 0) return;
        int removed = 0;
        for (PotionEffect pe : player.getActivePotionEffects()) {
            if (removed >= maxRemove) break;
            if (NEGATIVE.contains(pe.getType())) {
                player.removePotionEffect(pe.getType());
                removed++;
            }
        }
    }
}