package com.example.data;

import java.util.UUID;
import com.example.Main;
import org.bukkit.configuration.file.YamlConfiguration;

public class PlayerAttribute {

    private final UUID uuid;

    private int health;
    private int attack;
    private int defense;
    private int speed;
    private int mana;
    private int magicAttack;
    private int magicDefense;
    private int luck;
    private int criticalHitChance;
    private int criticalHitDamage;
    private int fireAttack;
    private int fireDefense;
    private int iceAttack;
    private int iceDefense;
    private int lightningAttack;
    private int lightningDefense;
    private int windAttack;
    private int windDefense;
    private int grassAttack;
    private int grassDefense;
    private int lightAttack;
    private int lightDefense;
    private int darkAttack;
    private int darkDefense;
    private int waterAttack;
    private int waterDefense;

    public PlayerAttribute(UUID uuid, Main plugin) {
        this.uuid = uuid;
        loadDefaults(plugin); // 初始化載入
    }

    // 初始化：完全覆蓋為預設（若缺就用後面預設值）
    private void loadDefaults(Main plugin) {
        var y = plugin.getDefaultAttrConfig(); // 改用新方法
        String base = "attributes.";
        String def = base + "default.";
        String per = base + "players." + uuid.toString() + ".";
        health = y.getInt(per + "health", y.getInt(def + "health", 20));
        attack = y.getInt(per + "attack", y.getInt(def + "attack", 0));
        defense = y.getInt(per + "defense", y.getInt(def + "defense", 0));
        speed = y.getInt(per + "speed", y.getInt(def + "speed", 5));
        mana = y.getInt(per + "mana", y.getInt(def + "mana", 50));
        magicAttack = y.getInt(per + "magicAttack", y.getInt(def + "magicAttack", 15));
        magicDefense = y.getInt(per + "magicDefense", y.getInt(def + "magicDefense", 10));
        luck = y.getInt(per + "luck", y.getInt(def + "luck", 0));
        criticalHitChance = y.getInt(per + "criticalHitChance", y.getInt(def + "criticalHitChance", 10));
        criticalHitDamage = y.getInt(per + "criticalHitDamage", y.getInt(def + "criticalHitDamage", 50));
        fireAttack = y.getInt(per + "fireattack", y.getInt(def + "fireattack", 0));
        fireDefense = y.getInt(per + "firedefense", y.getInt(def + "firedefense", 0));
        waterAttack = y.getInt(per + "waterattack", y.getInt(def + "waterattack", 0));
        waterDefense = y.getInt(per + "waterdefense", y.getInt(def + "waterdefense", 0));
        iceAttack = y.getInt(per + "iceattack", y.getInt(def + "iceattack", 0));
        iceDefense = y.getInt(per + "icedefense", y.getInt(def + "icedefense", 0));
        lightningAttack = y.getInt(per + "lightningattack", y.getInt(def + "lightningattack", 0));
        lightningDefense = y.getInt(per + "lightningdefense", y.getInt(def + "lightningdefense", 0));
        windAttack = y.getInt(per + "windattack", y.getInt(def + "windattack", 0));
        windDefense = y.getInt(per + "winddefense", y.getInt(def + "winddefense", 0));
        grassAttack = y.getInt(per + "grassattack", y.getInt(def + "grassattack", 0));
        grassDefense = y.getInt(per + "grassdefense", y.getInt(def + "grassdefense", 0));
        lightAttack = y.getInt(per + "lightattack", y.getInt(def + "lightattack", 0));
        lightDefense = y.getInt(per + "lightdefense", y.getInt(def + "lightdefense", 0));
        darkAttack = y.getInt(per + "darkattack", y.getInt(def + "darkattack", 0));
        darkDefense = y.getInt(per + "darkdefense", y.getInt(def + "darkdefense", 0));
    }

    public void reloadDefaults(Main plugin) { // 重新套用
        loadDefaults(plugin);
    }

    public void saveToDefaultAttr(Main plugin) { // 可選：回寫玩家覆寫
        var y = plugin.getDefaultAttrConfig();
        String per = "attributes.players." + uuid + ".";
        y.set(per + "health", health);
        y.set(per + "attack", attack);
        y.set(per + "defense", defense);
        y.set(per + "speed", speed);
        y.set(per + "mana", mana);
        y.set(per + "magicAttack", magicAttack);
        y.set(per + "magicDefense", magicDefense);
        y.set(per + "luck", luck);
        y.set(per + "criticalHitChance", criticalHitChance);
        y.set(per + "criticalHitDamage", criticalHitDamage);
        y.set(per + "fireattack", fireAttack);
        y.set(per + "firedefense", fireDefense);
        y.set(per + "waterattack", waterAttack);
        y.set(per + "waterdefense", waterDefense);
        y.set(per + "iceattack", iceAttack);
        y.set(per + "icedefense", iceDefense);
        y.set(per + "lightningattack", lightningAttack);
        y.set(per + "lightningdefense", lightningDefense);
        y.set(per + "windattack", windAttack);
        y.set(per + "winddefense", windDefense);
        y.set(per + "grassattack", grassAttack);
        y.set(per + "grassdefense", grassDefense);
        y.set(per + "lightattack", lightAttack);
        y.set(per + "lightdefense", lightDefense);
        y.set(per + "darkattack", darkAttack);
        y.set(per + "darkdefense", darkDefense);
        plugin.saveDefaultAttrConfig();
    }

    // Getter
    public int getHealth() { return health; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }
    public int getMana() { return mana; }
    public int getMagicAttack() { return magicAttack; }
    public int getMagicDefense() { return magicDefense; }
    public int getLuck() { return luck; }
    public int getCriticalHitChance() { return criticalHitChance; }
    public int getCriticalHitDamage() { return criticalHitDamage; }
    public int getFireAttack() { return fireAttack; }
    public int getFireDefense() { return fireDefense; }
    public int getIceAttack() { return iceAttack; }
    public int getIceDefense() { return iceDefense; }
    public int getLightningAttack() { return lightningAttack; }
    public int getLightningDefense() { return lightningDefense; }
    public int getWindAttack() { return windAttack; }
    public int getWindDefense() { return windDefense; }
    public int getGrassAttack() { return grassAttack; }
    public int getGrassDefense() { return grassDefense; }
    public int getLightAttack() { return lightAttack; }
    public int getLightDefense() { return lightDefense; }
    public int getDarkAttack() { return darkAttack; }
    public int getDarkDefense() { return darkDefense; }
    public int getWaterAttack() { return waterAttack; }
    public int getWaterDefense() { return waterDefense; }
    public UUID getUuid() { return uuid; }

    public void saveToFileSection(YamlConfiguration y) { // 寫入欄位
        y.set("health", health);
        y.set("attack", attack);
        y.set("defense", defense);
        y.set("speed", speed);
        y.set("mana", mana);
        y.set("magicattack", magicAttack);
        y.set("magicdefense", magicDefense);
        y.set("luck", luck);
        y.set("criticalhitchance", criticalHitChance);
        y.set("criticalhitdamage", criticalHitDamage);
        y.set("fireattack", fireAttack);
        y.set("firedefense", fireDefense);
        y.set("waterattack", waterAttack);
        y.set("waterdefense", waterDefense);
        y.set("iceattack", iceAttack);
        y.set("icedefense", iceDefense);
        y.set("lightningattack", lightningAttack);
        y.set("lightningdefense", lightningDefense);
        y.set("windattack", windAttack);
        y.set("winddefense", windDefense);
        y.set("grassattack", grassAttack);
        y.set("grassdefense", grassDefense);
        y.set("lightattack", lightAttack);
        y.set("lightdefense", lightDefense);
        y.set("darkattack", darkAttack);
        y.set("darkdefense", darkDefense);
    }

    public void loadFromFileSection(YamlConfiguration y) { // 讀取欄位(不存在則保留現值)
        health = y.getInt("health", health);
        attack = y.getInt("attack", attack);
        defense = y.getInt("defense", defense);
        speed = y.getInt("speed", speed);
        mana = y.getInt("mana", mana);
        magicAttack = y.getInt("magicattack", magicAttack);
        magicDefense = y.getInt("magicdefense", magicDefense);
        luck = y.getInt("luck", luck);
        criticalHitChance = y.getInt("criticalhitchance", criticalHitChance);
        criticalHitDamage = y.getInt("criticalhitdamage", criticalHitDamage);
        fireAttack = y.getInt("fireattack", fireAttack);
        fireDefense = y.getInt("firedefense", fireDefense);
        waterAttack = y.getInt("waterattack", waterAttack);
        waterDefense = y.getInt("waterdefense", waterDefense);
        iceAttack = y.getInt("iceattack", iceAttack);
        iceDefense = y.getInt("icedefense", iceDefense);
        lightningAttack = y.getInt("lightningattack", lightningAttack);
        lightningDefense = y.getInt("lightningdefense", lightningDefense);
        windAttack = y.getInt("windattack", windAttack);
        windDefense = y.getInt("winddefense", windDefense);
        grassAttack = y.getInt("grassattack", grassAttack);
        grassDefense = y.getInt("grassdefense", grassDefense);
        lightAttack = y.getInt("lightattack", lightAttack);
        lightDefense = y.getInt("lightdefense", lightDefense);
        darkAttack = y.getInt("darkattack", darkAttack);
        darkDefense = y.getInt("darkdefense", darkDefense);
    }

    /**
     * 依屬性鍵設定數值（成功回傳 true，未知鍵回傳 false）
     */
    public boolean setByKey(String key, int value) {
        switch (key.toLowerCase()) {
            case "health" -> this.health = value;
            case "attack" -> this.attack = value;
            case "defense" -> this.defense = value;
            case "speed" -> this.speed = value;
            case "mana" -> this.mana = value;
            case "magicattack" -> this.magicAttack = value;
            case "magicdefense" -> this.magicDefense = value;
            case "luck" -> this.luck = value;
            case "criticalhitchance" -> this.criticalHitChance = value;
            case "criticalhitdamage" -> this.criticalHitDamage = value;
            case "fireattack" -> this.fireAttack = value;
            case "firedefense" -> this.fireDefense = value;
            case "iceattack" -> this.iceAttack = value;
            case "icedefense" -> this.iceDefense = value;
            case "lightningattack" -> this.lightningAttack = value;
            case "lightningdefense" -> this.lightningDefense = value;
            case "windattack" -> this.windAttack = value;
            case "winddefense" -> this.windDefense = value;
            case "grassattack" -> this.grassAttack = value;
            case "grassdefense" -> this.grassDefense = value;
            case "lightattack" -> this.lightAttack = value;
            case "lightdefense" -> this.lightDefense = value;
            case "darkattack" -> this.darkAttack = value;
            case "darkdefense" -> this.darkDefense = value;
            case "waterattack" -> this.waterAttack = value;
            case "waterdefense" -> this.waterDefense = value;
            default -> { return false; }
        }
        return true;
    }
}