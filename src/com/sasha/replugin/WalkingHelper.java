package com.sasha.replugin;

import com.github.steveice10.mc.protocol.data.game.entity.player.PlayerState;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerStatePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.sasha.eventsys.SimpleEventHandler;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.event.RemoteServerPacketRecieveEvent;
import com.sasha.reminecraft.client.ReClient;

import java.util.Random;

public class WalkingHelper implements SimpleListener {

    public static final int POS_X = 1;
    public static final int NEG_X = 2;
    public static final int POS_Z = 3;
    public static final int NEG_Z = 4;
    // 1 - 4
    public int phase = 1;
    public boolean crouched;
    private Main plugin;

    public WalkingHelper(Main plugin) {
        this.plugin = plugin;
    }

    public void walk() {
        if (this.getReMc().minecraftClient != null && this.getReMc().minecraftClient.getSession().isConnected() && plugin.isInGame() && !this.getReMc().areChildrenConnected()) {
            switch (phase) {
                case POS_X:
                    getReMc().minecraftClient.getSession()
                            .send(new ClientPlayerPositionPacket(true, ReClient.ReClientCache.INSTANCE.posX + 0.50, ReClient.ReClientCache.INSTANCE.posY, ReClient.ReClientCache.INSTANCE.posZ));
                    ReClient.ReClientCache.INSTANCE.posX += 0.50;
                    if (randBool()) {
                        getReMc().minecraftClient.getSession()
                                .send(new ClientPlayerPositionPacket(true, ReClient.ReClientCache.INSTANCE.posX, ReClient.ReClientCache.INSTANCE.posY, ReClient.ReClientCache.INSTANCE.posZ + 0.15));
                        ReClient.ReClientCache.INSTANCE.posZ += 0.15;
                    }
                    break;
                case NEG_X:
                    getReMc().minecraftClient.getSession()
                            .send(new ClientPlayerPositionPacket(true, ReClient.ReClientCache.INSTANCE.posX - 0.50, ReClient.ReClientCache.INSTANCE.posY, ReClient.ReClientCache.INSTANCE.posZ));
                    ReClient.ReClientCache.INSTANCE.posX -= 0.50;
                    if (randBool()) {
                        getReMc().minecraftClient.getSession()
                                .send(new ClientPlayerPositionPacket(true, ReClient.ReClientCache.INSTANCE.posX, ReClient.ReClientCache.INSTANCE.posY, ReClient.ReClientCache.INSTANCE.posZ - 0.15));
                        ReClient.ReClientCache.INSTANCE.posZ -= 0.15;
                    }
                    break;
                case POS_Z:
                    getReMc().minecraftClient.getSession()
                            .send(new ClientPlayerPositionPacket(true, ReClient.ReClientCache.INSTANCE.posX, ReClient.ReClientCache.INSTANCE.posY, ReClient.ReClientCache.INSTANCE.posZ + 0.50));
                    ReClient.ReClientCache.INSTANCE.posZ += 0.50;
                    if (randBool()) {
                        getReMc().minecraftClient.getSession()
                                .send(new ClientPlayerPositionPacket(true, ReClient.ReClientCache.INSTANCE.posX - 0.15, ReClient.ReClientCache.INSTANCE.posY, ReClient.ReClientCache.INSTANCE.posZ));
                        ReClient.ReClientCache.INSTANCE.posX -= 0.15;
                    }
                    break;
                default:
                case NEG_Z:
                    getReMc().minecraftClient.getSession()
                            .send(new ClientPlayerPositionPacket(true, ReClient.ReClientCache.INSTANCE.posX, ReClient.ReClientCache.INSTANCE.posY, ReClient.ReClientCache.INSTANCE.posZ - 0.50));
                    ReClient.ReClientCache.INSTANCE.posZ -= 0.50;
                    if (randBool()) {
                        getReMc().minecraftClient.getSession()
                                .send(new ClientPlayerPositionPacket(true, ReClient.ReClientCache.INSTANCE.posX + 0.15, ReClient.ReClientCache.INSTANCE.posY, ReClient.ReClientCache.INSTANCE.posZ));
                        ReClient.ReClientCache.INSTANCE.posX += 0.15;
                    }
                    break;
            }
            if (randBool()) {
                getReMc().minecraftClient.getSession()
                        .send(new ClientPlayerStatePacket(ReClient.ReClientCache.INSTANCE.entityId, crouched ? PlayerState.STOP_SNEAKING : PlayerState.START_SNEAKING, 0));
            }
        }

    }

    public static boolean randBool() {
        Random random = new Random();
        return random.nextBoolean();
    }

    public boolean resolve(String key) {
        if (key.equalsIgnoreCase("POS_X")) {
            phase = POS_X;
            return true;
        } else if (key.equalsIgnoreCase("NEG_X")) {
            phase = NEG_X;
            return true;
        } else if (key.equalsIgnoreCase("POS_Z")) {
            phase = POS_Z;
            return true;
        } else if (key.equalsIgnoreCase("NEG_Z")) {
            phase = NEG_Z;
            return true;
        }
        return false;
    }

    private ReMinecraft getReMc() {
        return ReMinecraft.INSTANCE;
    }

    @SimpleEventHandler
    public void onPckRx(RemoteServerPacketRecieveEvent e) {
        if (e.getRecievedPacket() instanceof ServerPlayerPositionRotationPacket) {
            plugin.logger.log("Walking direction reset.");
            if (randBool()) {
                if (phase <= 1) phase = 5;
                phase--;
            } else {
                if (phase >= 4) phase = 0;
                phase++;
            }
        }
    }

}