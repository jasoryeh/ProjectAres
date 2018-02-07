package tc.oc.commons.bukkit.bossbar;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.chat.Components;

public class RenderedOldBossBar extends LegacyBossBar implements BossBar {

    private final ComponentRenderContext renderer;
    private final BossBarFactory bossBarFactory;

    private final Map<Player, NMSHacks.FakeWither> views = new HashMap<>();
    private final Map<Player, Integer> tasks = new HashMap<>();

    private BaseComponent title = Components.blank();
    private final BarColor color = BarColor.PURPLE;
    private final BarStyle style = BarStyle.SOLID;
    private double progress = 0;
    private final Set<BarFlag> flags = EnumSet.noneOf(BarFlag.class);
    private boolean visibile = true;

    @Inject RenderedOldBossBar(ComponentRenderContext renderer, BossBarFactory bossBarFactory) {
        this.renderer = renderer;
        this.bossBarFactory = bossBarFactory;
    }

    @Override
    public BaseComponent getTitle() {
        return title;
    }

    @Override
    public BarColor getColor() {
        return BarColor.PURPLE;
    }

    @Override
    public BarStyle getStyle() {
        return BarStyle.SOLID;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public boolean hasFlag(BarFlag flag) {
        return this.flags.contains(flag);
    }

    @Override
    public boolean isVisible() {
        return visibile;
    }

    @Override
    public List<Player> getPlayers() {
        return ImmutableList.copyOf(views.keySet());
    }

    @Override
    public void setTitle(BaseComponent title) {
        this.title = title;
        views.keySet().forEach(p -> { views.get(p).setName(p, renderer.renderLegacy(title, p), true); });
    }

    @Override
    public void setColor(BarColor color) {}

    @Override
    public void setStyle(BarStyle style) {}

    @Override
    public void setFlags(Set<BarFlag> flags) {}

    @Override
    public void removeFlag(BarFlag flag) {}

    @Override
    public void addFlag(BarFlag flag) {}

    @Override
    public void setProgress(double progress) {
        this.progress = progress;
        views.keySet().forEach(p -> { views.get(p).setPercent(p, progress, true); });
    }

    @Override
    public void addPlayer(Player player) {
        if(!views.containsKey(player)) {
            NMSHacks.FakeWither view = new NMSHacks.FakeWither(player.getWorld(), true, renderer.renderLegacy(title, player));
            views.put(player, view);
            tasks.put(
                    player,
                    Bukkit.getScheduler().scheduleSyncRepeatingTask(
                            Bukkit.getPluginManager().getPlugin("Commons"),
                            () -> { if(isVisible()) view.teleport(player, getFakeEntityLocation(player)); },
                            0,
                            1
                    )
            );
            if(isVisible()) view.spawn(player, getFakeEntityLocation(player));
        }
    }

    @Override
    public void removePlayer(Player player) {
        NMSHacks.FakeWither view = views.remove(player);
        try {
            Bukkit.getScheduler().cancelTask(tasks.remove(player));
        } catch(Exception e) { Bukkit.getLogger().info("No task found for" + player.getDisplayName()); }
        if(view != null) view.destroy(player);
    }

    @Override
    public void removeAll() {
        views.keySet().forEach(this::removePlayer);
        views.clear();
    }

    @Override
    public void setVisible(boolean visible) {
        this.visibile = visible;
        views.keySet().forEach(p -> views.get(p).setVisible(p, visible, true));
    }

    @Override
    public void show() {
        setVisible(true);
    }

    @Override
    public void hide() {
        setVisible(false);
    }

    @Override
    public void update(BaseComponent title, double progress, BarColor color, BarStyle style, Set<BarFlag> flags) {
        this.title = title;
        this.progress = progress;
        this.flags.clear();
        this.flags.addAll(flags);

        views.entrySet().forEach(entry -> entry.getValue().change(entry.getKey(), true,
                renderer.renderLegacy(title, entry.getKey()), true, progress, null, !isVisible(),
                null));
    }

    public Location getFakeEntityLocation(Player p) {
        Location l = p.getEyeLocation().clone();
        l.setPitch(l.getPitch() + 45);
        return p.getEyeLocation().add(l.getDirection().multiply(32));
    }
}
