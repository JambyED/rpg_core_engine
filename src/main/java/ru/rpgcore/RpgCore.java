package ru.rpgcore;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import ru.rpgcore.command.RpgCommands;
import ru.rpgcore.core.class_.RpgCoreClasses;
import ru.rpgcore.core.config.RpgGameRules;
import ru.rpgcore.core.perk.RpgCorePerks;
import ru.rpgcore.core.storage.RpgStorageMenuType;
import ru.rpgcore.event.MobXpEvents;
import ru.rpgcore.network.RpgNetwork;

@Mod(RpgCore.MODID)
public final class RpgCore {
    public static final String MODID = "rpg_core";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RpgCore() {
        // MOD bus (DeferredRegister / registry stuff)
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Debug perks (temporary for testing)
        RpgCorePerks.register(modBus);

        // Debug classes (TEMP for testing Class GUI)
        RpgCoreClasses.register(modBus);

        // Storage menu types
        RpgStorageMenuType.MENUS.register(modBus);

        // Common init
        RpgGameRules.init();
        RpgNetwork.register();

        // Forge bus
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(MobXpEvents::onLivingDeath);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        RpgCommands.register(event.getDispatcher());
    }
}