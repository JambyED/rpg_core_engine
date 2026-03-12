package ru.rpgcore.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.rpgcore.RpgCore;
import ru.rpgcore.core.storage.RpgStorageMenuType;

@Mod.EventBusSubscriber(modid = RpgCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RpgClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {

        event.enqueueWork(() -> {

            MenuScreens.register(
                    RpgStorageMenuType.STORAGE_MENU.get(),
                    RpgStorageScreen::new
            );

        });

    }

}