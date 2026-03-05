package ru.rpgcore.core.perk;

import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.rpgcore.RpgCore;
import ru.rpgcore.api.perk.RpgPerk;

public final class RpgCorePerks {
    private RpgCorePerks() {}

    public static final DeferredRegister<RpgPerk> PERKS =
            DeferredRegister.create(RpgPerkRegistries.PERKS_REGISTRY_NAME, RpgCore.MODID);

    // TEMP DEBUG PERKS (will be moved to rpg_content later)
    public static final RegistryObject<RpgPerk> DEBUG_TOUGHNESS =
            PERKS.register("debug_toughness",
                    () -> new SimplePerk(1, Component.translatable("perk.rpg_core.debug_toughness"))
            );

    public static final RegistryObject<RpgPerk> DEBUG_SPEED =
            PERKS.register("debug_speed",
                    () -> new SimplePerk(1, Component.translatable("perk.rpg_core.debug_speed"))
            );

    public static final RegistryObject<RpgPerk> DEBUG_ARMOR =
            PERKS.register("debug_armor",
                    () -> new SimplePerk(1, Component.translatable("perk.rpg_core.debug_armor"))
            );

    public static void register(IEventBus modBus) {
        PERKS.register(modBus);
    }
}