package com.example.events; // 套件宣告

import com.example.Main; // 主插件類引用
import com.example.util.ElementDamageUtil; // 元素傷害與浮空字工具類
import org.bukkit.entity.Player; // 玩家實體類
import org.bukkit.entity.LivingEntity; // 可受傷活體實體
import org.bukkit.event.Listener; // 事件監聽器介面
import org.bukkit.event.EventHandler; // 事件處理註解
import org.bukkit.event.EventPriority; // 事件優先級列舉
import org.bukkit.event.entity.EntityDamageByEntityEvent; // 實體被另一實體傷害事件

/**
 * 普通攻擊傷害顯示監聽器 // 類用途：顯示每次普通攻擊最終傷害數值
 */
public class NormalAttackListener implements Listener { // 類別實作 Listener

    private final Main plugin; // 保存主插件實例供方法使用

    public NormalAttackListener(Main plugin) { // 建構子：注入主插件
        this.plugin = plugin; // 指派主插件實例
    } // 建構子結束

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) // 事件註冊：普通優先級且忽略已取消事件
    public void onHit(EntityDamageByEntityEvent e) { // 處理實體間傷害事件方法
        if (!(e.getDamager() instanceof Player)) return; // 若傷害來源不是玩家則返回
        if (!(e.getEntity() instanceof LivingEntity target)) return; // 若受擊者不是活體則返回
        if (ElementDamageUtil.isElementExtra(target)) return; // 若是我們加的元素額外傷害（避免重複顯示）則返回
        ElementDamageUtil.showDamageHologram(plugin, target, e.getFinalDamage()); // 顯示最終傷害數值的浮空字
    } // onHit 方法結束    } // onHit 方法結束
} // 類別