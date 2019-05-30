package com.sasha.replugin;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.data.game.entity.player.PlayerAction;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockFace;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerActionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerSwingArmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientVehicleMovePacket;
import com.sasha.eventsys.SimpleListener;
import com.sasha.reminecraft.Configuration;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.api.RePlugin;
import com.sasha.reminecraft.client.ReClient;
import com.sasha.reminecraft.logging.ILogger;
import com.sasha.reminecraft.logging.LoggerBuilder;
import com.sasha.reminecraft.util.entity.Entity;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.sasha.replugin.WalkingHelper.randBool;

public class Main extends RePlugin implements SimpleListener {

    public static WalkingHelper helper;
    public ILogger logger = LoggerBuilder.buildProperLogger("AntiAFK");
    public Config CFG = new Config("AntiAFK");
    private ScheduledExecutorService executorService;
    private Runnable spamTask = () -> {
        Random rand = new Random();
        if (this.getReMinecraft().minecraftClient != null && this.getReMinecraft().minecraftClient.getSession().isConnected() && isInGame() && !this.getReMinecraft().areChildrenConnected()) {
            this.getReMinecraft().minecraftClient.getSession().send(new ClientChatPacket(CFG.var_spamMessages.get(rand.nextInt(CFG.var_spamMessages.size() - 1))));
        }
    };
    private Runnable twistTask = () -> {
        Random rand = new Random();
        if (CFG.var_walkInsteadOfTwist) {
            helper.walk();
            if (randBool()) return;
        }
        if (this.getReMinecraft().minecraftClient != null && this.getReMinecraft().minecraftClient.getSession().isConnected() && isInGame() && !this.getReMinecraft().areChildrenConnected()) {
            if (randBool()) {
                this.getReMinecraft().minecraftClient.getSession().send(new ClientPlayerSwingArmPacket(Hand.MAIN_HAND));
                if (CFG.var_hitBlock)
                    this.getReMinecraft().minecraftClient.getSession().send(new ClientPlayerActionPacket(PlayerAction.START_DIGGING,
                            new Position((int) ReClient.ReClientCache.INSTANCE.posX, (int) ReClient.ReClientCache.INSTANCE.posY - 1, (int) ReClient.ReClientCache.INSTANCE.posZ), BlockFace.UP));
            } else {
                float yaw = -90 + (90 - -90) * rand.nextFloat();
                float pitch = -90 + (90 - -90) * rand.nextFloat();
                if (isRiding(ReClient.ReClientCache.INSTANCE.player)) {
                    Entity entity = getRiding(ReClient.ReClientCache.INSTANCE.player);
                    this.getReMinecraft().minecraftClient.getSession().send(new ClientVehicleMovePacket(entity.posX, entity.posY, entity.posZ, yaw, pitch));
                    return;
                }
                this.getReMinecraft().minecraftClient.getSession().send(new ClientPlayerRotationPacket(true, yaw, pitch));
            }
        }
    };

    public boolean isRiding(Entity e) {
        return getRiding(e) != null;
    }

    public Entity getRiding(Entity e) {
        for (Map.Entry<Integer, Entity> entry : ReClient.ReClientCache.INSTANCE.entityCache.entrySet()) {
            if (entry.getValue().passengerIds.contains(e.entityId)) return entry.getValue();
        }
        return null;
    }

    @Override
    public void onPluginInit() {
        helper = new WalkingHelper(this);
        executorService = Executors.newScheduledThreadPool(4);
        if (CFG.var_spamChat) {
            executorService.scheduleWithFixedDelay(spamTask, CFG.var_spamIntervalSeconds,
                    CFG.var_spamIntervalSeconds, TimeUnit.SECONDS);
        }
        if (CFG.var_antiAfk) {
            executorService.scheduleWithFixedDelay(twistTask, CFG.var_twistIntervalSeconds,
                    CFG.var_twistIntervalSeconds, TimeUnit.SECONDS);
        }
        if (CFG.var_walkInsteadOfTwist) this.getReMinecraft().EVENT_BUS.registerListener(helper);
    }

    @Override
    public void onPluginEnable() {
        logger.log("AntiAFK plugin enabled");
    }

    @Override
    public void onPluginDisable() {
        //this.executorService.shutdownNow();
        logger.log("AntiAFK plugin disabled");
    }

    @Override
    public void onPluginShutdown() {
        this.executorService.shutdownNow();
        logger.log("AntiAFK plugin shutdown");
    }

    @Override
    public void registerCommands() {
        try {
            ReMinecraft.INGAME_CMD_PROCESSOR.register(SetPosCommand.class);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerConfig() {
        this.getReMinecraft().configurations.add(CFG);
    }

    public boolean isInGame() {
        MinecraftProtocol pckprot = (MinecraftProtocol) this.getReMinecraft().minecraftClient.getSession().getPacketProtocol();
        return pckprot.getSubProtocol() == SubProtocol.GAME;
    }

}

class Config extends Configuration {
    @ConfigSetting
    public boolean var_spamChat = false;
    @ConfigSetting
    public boolean var_antiAfk = true;
    @ConfigSetting
    public boolean var_walkInsteadOfTwist = false;
    @ConfigSetting
    public boolean var_hitBlock = true;
    @ConfigSetting
    public int var_spamIntervalSeconds = 60;
    @ConfigSetting
    public int var_twistIntervalSeconds = 5;
    @ConfigSetting
    public ArrayList<String> var_spamMessages = new ArrayList<>();

    {
        var_spamMessages.add("Spam :D!");
        var_spamMessages.add("Spam D:!");
    }

    Config(String configName) {
        super(configName);
    }
}
