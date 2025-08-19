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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DarkAttackListener implements Listener {

    private static final String MARK_META = "dark_mark_stack";       // 保存層數
    private static final String MARK_TIME_META = "dark_mark_time";   // 保存最後刷新時間 (ms)
    private final Main plugin;

    public DarkAttackListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;
        if (ElementDamageUtil.isElementExtra(target)) return;

        PlayerAttribute attr = plugin.getPlayerAttribute(attacker.getUniqueId());
        int dark = attr.getDarkAttack();
        if (dark <= 0) return;

        double per = plugin.getConfig().getDouble("damage.dark.extra-per-point", 0.65D);
        double baseExtra = dark * per;
        if (baseExtra <= 0) return;

        // 疊層處理
        int stacks = 0;
        long now = System.currentTimeMillis();
        if (plugin.getConfig().getBoolean("damage.dark.mark.enabled", true)) {
            int maxStacks = plugin.getConfig().getInt("damage.dark.mark.max-stacks", 8);
            long expireMs = (long)(plugin.getConfig().getDouble("damage.dark.mark.expire-seconds", 6D) * 1000L);

            stacks = getIntMeta(target, MARK_META);
            long lastTime = getLongMeta(target, MARK_TIME_META);
            if (lastTime == 0 || now - lastTime <= expireMs) {
                stacks += plugin.getConfig().getInt("damage.dark.mark.per-hit-stack", 1);
                if (stacks > maxStacks) stacks = maxStacks;
            } else {
                stacks = plugin.getConfig().getInt("damage.dark.mark.per-hit-stack", 1); // 重置
            }
            target.setMetadata(MARK_META, new FixedMetadataValue(plugin, stacks));
            target.setMetadata(MARK_TIME_META, new FixedMetadataValue(plugin, now));
        }

        // 層數加成
        double stackPct = plugin.getConfig().getDouble("damage.dark.mark.extra-damage-percent-per-stack", 0.05D);
        double stackedExtra = baseExtra * (1 + stacks * stackPct);

        // 施加額外暗傷
        ElementDamageUtil.applyElementExtra(plugin, attacker, target, stackedExtra);
        ElementDamageUtil.showDamageHologram(plugin, target, stackedExtra); // stackedExtra 為本次造成的傷害數值

        // 吸血 (用最終暗額外傷害 totalDark = stackedExtra)
        double lsPercent = plugin.getConfig().getDouble("damage.dark.lifesteal-percent", 0.20D);
        double heal = stackedExtra * lsPercent;
        if (heal > 0) {
            double max = attacker.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
            attacker.setHealth(Math.min(max, attacker.getHealth() + heal));
        }

        // 凋零機率
        double chancePer = plugin.getConfig().getDouble("damage.dark.wither.chance-per-point", 0.01D);
        double maxChance = plugin.getConfig().getDouble("damage.dark.wither.max-chance", 0.40D);
        double chance = Math.min(maxChance, dark * chancePer);
        if (chance > 0 && ThreadLocalRandom.current().nextDouble() < chance) {
            int ticks = plugin.getConfig().getInt("damage.dark.wither.ticks", 60);
            int amp = plugin.getConfig().getInt("damage.dark.wither.amplifier", 0);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, ticks, amp, true, true));
        }

        // 可選 debug:
        // attacker.sendMessage("暗傷+" + stackedExtra + " 吸血+" + heal + " 層數:" + stacks);
    }

    private int getIntMeta(LivingEntity ent, String key) {
        List<MetadataValue> list = ent.getMetadata(key);
        for (MetadataValue mv : list) {
            if (mv.getOwningPlugin() == plugin) {
                try { return Integer.parseInt(String.valueOf(mv.value())); } catch (NumberFormatException ignored) {}
            }
        }
        return 0;
    }

    private long getLongMeta(LivingEntity ent, String key) {
        List<MetadataValue> list = ent.getMetadata(key);
        for (MetadataValue mv : list) {
            if (mv.getOwningPlugin() == plugin) {
                try { return Long.parseLong(String.valueOf(mv.value())); } catch (NumberFormatException ignored) {}
            }
        }
        return 0L;
    }
}