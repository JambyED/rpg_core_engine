package ru.rpgcore.core.perk;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import ru.rpgcore.RpgCore;
import ru.rpgcore.api.perk.RpgPerk;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = RpgCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RpgPerkRegistries {
    private RpgPerkRegistries() {}

    public static final ResourceLocation PERKS_REGISTRY_NAME =
            ResourceLocation.fromNamespaceAndPath(RpgCore.MODID, "perks");

    private static Supplier<IForgeRegistry<RpgPerk>> SUPPLIER;

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        SUPPLIER = event.create(
                new RegistryBuilder<RpgPerk>()
                        .setName(PERKS_REGISTRY_NAME)
        );
    }

    public static IForgeRegistry<RpgPerk> registry() {
        if (SUPPLIER == null) throw new IllegalStateException("Perk registry not created yet: " + PERKS_REGISTRY_NAME);
        return SUPPLIER.get();
    }
}