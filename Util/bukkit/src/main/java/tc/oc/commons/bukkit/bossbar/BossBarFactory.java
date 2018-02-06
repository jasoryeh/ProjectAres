package tc.oc.commons.bukkit.bossbar;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import tc.oc.minecraft.protocol.MinecraftVersion;

public interface BossBarFactory {
    BossBar createBossBar();

    BossBar createBossBar(BaseComponent title, BarColor color, BarStyle style, boolean old, BarFlag...flags);

    default BossBar createBossBar(Player p, BaseComponent title, BarColor color, BarStyle style, BarFlag... flags) {
        BossBar b = createBossBar(title, color, style, p.getProtocolVersion() <= MinecraftVersion.MINECRAFT_1_8.protocol(), flags);
        b.addPlayer(p);
        return b;
    }

    default BossBar createBossBar(BaseComponent title, BarColor color, BarStyle style, BarFlag... flags) {
        return createBossBar(title, color, style, false, flags);
    }

    BossBar createRenderedBossBar();

    BossBar createRenderedOldBossBar();
}
