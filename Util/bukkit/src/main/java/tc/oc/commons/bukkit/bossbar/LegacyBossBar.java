package tc.oc.commons.bukkit.bossbar;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public abstract class LegacyBossBar implements BossBar {
    public abstract Location getFakeEntityLocation(Player p);

    protected static BlockFace getDirection(Location loc) {
        float dir = Math.round(loc.getYaw() / 90);
        if (dir == -4 || dir == 0 || dir == 4)
            return BlockFace.SOUTH;
        if (dir == -1 || dir == 3)
            return BlockFace.EAST;
        if (dir == -2 || dir == 2)
            return BlockFace.NORTH;
        if (dir == -3 || dir == 1)
            return BlockFace.WEST;
        return null;
    }
}
