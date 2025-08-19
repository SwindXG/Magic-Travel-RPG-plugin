package com.example.command.sub;

import com.example.Main;
import com.example.data.PlayerAttribute;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.*;

public class PlayerInfoSubCommand implements SubCommand {

    private final Main plugin;
    public PlayerInfoSubCommand(Main plugin){ this.plugin = plugin; }

    @Override public String getName() { return "playerinfo"; }
    @Override public String getDescription() { return "查看玩家所有屬性"; }
    @Override public String getPermission() { return "mt.playerinfo"; }
    @Override public String getUsage(String root) { return "/" + root + " playerinfo <玩家>"; }
    @Override public List<String> getAliases() { return Collections.singletonList("pinfo"); }
    @Override public boolean isListedInHelp() { return true; }

    @SuppressWarnings("deprecation")
    @Override
    public boolean execute(CommandSender sender, String[] args, String rootLabel) {
        if (args.length != 1) return false;

        OfflinePlayer op = Bukkit.getOfflinePlayer(args[0]);
        if (op == null || op.getUniqueId() == null) {
            sender.sendMessage("§c找不到玩家: " + args[0]);
            return true;
        }

        PlayerAttribute a = plugin.getPlayerAttribute(op.getUniqueId());
        String name = (op.getName()==null? op.getUniqueId().toString() : op.getName());

        sender.sendMessage("§6====== §e" + name + " §6屬性 ======");

        // 基礎
        sender.sendMessage("§e[基礎]§7 血量: §f" + a.getHealth()
                + " §7攻擊: §f" + a.getAttack()
                + " §7防禦: §f" + a.getDefense()
                + " §7速度: §f" + a.getSpeed()
                + " §7魔力: §f" + a.getMana()
                + " §7幸運: §f" + a.getLuck());

        // 進階戰鬥
        sender.sendMessage("§e[戰鬥]§7 魔法攻擊: §f" + a.getMagicAttack()
                + " §7魔法防禦: §f" + a.getMagicDefense()
                + " §7爆擊機率: §f" + a.getCriticalHitChance()
                + " §7爆擊傷害: §f" + a.getCriticalHitDamage());

        // 元素攻擊
        sender.sendMessage("§e[元素攻擊]§7 火: §f" + a.getFireAttack()
                + " §7水: §f" + a.getWaterAttack()
                + " §7冰: §f" + a.getIceAttack()
                + " §7雷: §f" + a.getLightningAttack()
                + " §7風: §f" + a.getWindAttack()
                + " §7草: §f" + a.getGrassAttack()
                + " §7光: §f" + a.getLightAttack()
                + " §7暗: §f" + a.getDarkAttack());

        // 元素防禦
        sender.sendMessage("§e[元素防禦]§7 火: §f" + a.getFireDefense()
                + " §7水: §f" + a.getWaterDefense()
                + " §7冰: §f" + a.getIceDefense()
                + " §7雷: §f" + a.getLightningDefense()
                + " §7風: §f" + a.getWindDefense()
                + " §7草: §f" + a.getGrassDefense()
                + " §7光: §f" + a.getLightDefense()
                + " §7暗: §f" + a.getDarkDefense());

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> list.add(p.getName()));
            return partial(args[0], list);
        }
        return Collections.emptyList();
    }

    private List<String> partial(String token, List<String> base){
        List<String> out = new ArrayList<>();
        for (String b : base) if (b.toLowerCase().startsWith(token.toLowerCase())) out.add(b);
        return out;
    }
}