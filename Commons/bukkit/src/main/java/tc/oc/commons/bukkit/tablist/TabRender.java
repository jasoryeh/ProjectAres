package tc.oc.commons.bukkit.tablist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import com.github.rmsy.channels.vault.VaultSetup;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.Packet;
import net.minecraft.server.PacketPlayOutPlayerInfo;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import tc.oc.commons.bukkit.chat.ComponentRenderContext;
import tc.oc.commons.bukkit.util.NMSHacks;
import tc.oc.commons.core.chat.Component;

public class TabRender {

    @Inject private static ComponentRenderContext componentRenderContext;

    private final TabView view;

    private final PacketPlayOutPlayerInfo removePacket;
    private final PacketPlayOutPlayerInfo addPacket;
    private final PacketPlayOutPlayerInfo updatePacket;
    private final List<Packet> deferredPackets;

    public TabRender(TabView view) {
        this.view = view;
        this.removePacket = this.createPlayerInfoPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
        this.addPacket    = this.createPlayerInfoPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        this.updatePacket = this.createPlayerInfoPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME);
        this.deferredPackets = new ArrayList<>();
    }

    private String teamName(int slot) {
        return "\u0001TabView" + String.format("%03d", slot);
    }

    private void send(Packet packet) {
        NMSHacks.sendPacket(this.view.getViewer(), packet);
    }

    private PacketPlayOutPlayerInfo createPlayerInfoPacket(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action) {
        return new PacketPlayOutPlayerInfo(action);
    }

    private BaseComponent getContent(TabEntry entry, int index) {
        return this.componentRenderContext.render(entry.getContent(this.view), this.view.getViewer());
    }

    private void appendAddition(TabEntry entry, int index) {
        BaseComponent displayName = new Component();
        displayName.addExtra(this.getContent(entry, index));
        displayName.addExtra(VaultSetup.getPrefix(entry.getFakePlayer(this.view)));
        this.addPacket.add(NMSHacks.playerListPacketData(this.addPacket,
                                                         entry.getId(),
                                                         entry.getName(this.view),
                                                         displayName,
                                                         entry.getGamemode(),
                                                         entry.getPing(),
                                                         entry.getSkin(this.view)));

        // Due to a client bug, display name is ignored in ADD_PLAYER packets,
        // so we have to send an UPDATE_DISPLAY_NAME afterward.
        this.updatePacket.add(NMSHacks.playerListPacketData(this.updatePacket, entry.getId(), displayName));

        this.updateFakeEntity(entry, true);
    }

    private void appendRemoval(TabEntry entry) {
        this.removePacket.add(NMSHacks.playerListPacketData(this.removePacket, entry.getId()));

        int entityId = entry.getFakeEntityId(this.view);
        if(entityId >= 0) {
            this.send(NMSHacks.destroyEntitiesPacket(entityId));
        }
    }

    private void leaveSlot(TabEntry entry, int index) {
        this.send(NMSHacks.teamLeavePacket(this.teamName(index), Collections.singleton(entry.getName(this.view))));
    }

    private void joinSlot(TabEntry entry, int index) {
        this.send(NMSHacks.teamJoinPacket(this.teamName(index), Collections.singleton(entry.getName(this.view))));
    }

    public void finish() {
        if(!this.removePacket.isEmpty()) this.send(this.removePacket);
        if(!this.addPacket.isEmpty())    this.send(this.addPacket);
        if(!this.updatePacket.isEmpty()) this.send(this.updatePacket);

        for(Packet packet : this.deferredPackets) {
            this.send(packet);
        }
    }

    public void changeSlot(TabEntry entry, int oldIndex, int newIndex) {
        Collection<String> names = Collections.singleton(entry.getName(this.view));
        this.send(NMSHacks.teamJoinPacket(this.teamName(newIndex), names));
    }

    public void createSlot(TabEntry entry, int index) {
        String teamName = this.teamName(index);
        this.send(NMSHacks.teamCreatePacket(teamName, teamName, "", "", false, false, Collections.singleton(entry.getName(this.view))));
        this.appendAddition(entry, index);
    }

    public void destroySlot(TabEntry entry, int index) {
        this.send(NMSHacks.teamRemovePacket(this.teamName(index)));
        this.appendRemoval(entry);
    }

    public void addEntry(TabEntry entry, int index) {
        this.joinSlot(entry, index);
        this.appendAddition(entry, index);
    }

    public void removeEntry(TabEntry entry, int index) {
        this.leaveSlot(entry, index);
        this.appendRemoval(entry);
    }

    public void refreshEntry(TabEntry entry, int index) {
        this.appendRemoval(entry);
        this.appendAddition(entry, index);
    }

    public void updateEntry(TabEntry entry, int index) {
        this.updatePacket.add(NMSHacks.playerListPacketData(this.updatePacket, entry.getId(), this.getContent(entry, index)));
    }

    public void setHeaderFooter(TabEntry header, TabEntry footer) {
        view.getViewer().setPlayerListHeaderFooter(componentRenderContext.render(header.getContent(view), view.getViewer()),
                                                   componentRenderContext.render(footer.getContent(view), view.getViewer()));
    }

    public void updateFakeEntity(TabEntry entry, boolean create) {
        Player player = entry.getFakePlayer(this.view);
        if(player != null) {
            int entityId = entry.getFakeEntityId(this.view);
            if(create) {
                this.deferredPackets.add(NMSHacks.spawnPlayerPacket(
                    entityId,
                    entry.getId(),
                    new Location(this.view.getViewer().getWorld(), 0, Integer.MAX_VALUE / 2, 0, 0, 0),
                    player
                ));
            } else {
                this.deferredPackets.add(NMSHacks.entityMetadataPacket(entityId, player, true));
            }
        }
    }
}
