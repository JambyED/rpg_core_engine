package ru.rpgcore.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.rpgcore.RpgCore;
import ru.rpgcore.network.RpgNetwork;
import ru.rpgcore.network.msg.C2S_RequestProfile;
import ru.rpgcore.network.msg.ClientProfileCache;
import ru.rpgcore.network.msg.S2C_ProfileData;

@Mod.EventBusSubscriber(modid = RpgCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RpgCoreClientEvents {
    private RpgCoreClientEvents() {}

    private static int profileSyncTick = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (RpgCoreClient.OPEN_MENU != null && RpgCoreClient.OPEN_MENU.consumeClick()) {
            mc.setScreen(new RpgCharacterScreen());
        }

        profileSyncTick++;
        if (profileSyncTick >= 40) {
            profileSyncTick = 0;
            RpgNetwork.CHANNEL.sendToServer(new C2S_RequestProfile());
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        S2C_ProfileData d = ClientProfileCache.get();
        if (d == null) return;
        if (!d.hudEnabled) return;

        RpgTopLeftHudOverlay.render(event.getGuiGraphics());
    }
}