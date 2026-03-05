package ru.rpgcore.core.class_;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;
import ru.rpgcore.RpgCore;
import ru.rpgcore.api.class_.RpgClass;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = RpgCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RpgClassRegistries {
    private RpgClassRegistries() {}

    public static final ResourceLocation CLASSES_REGISTRY_NAME =
            ResourceLocation.fromNamespaceAndPath(RpgCore.MODID, "classes");

    private static Supplier<IForgeRegistry<RpgClass>> SUPPLIER;

    @SubscribeEvent
    public static void onNewRegistry(NewRegistryEvent event) {
        SUPPLIER = event.create(
                new RegistryBuilder<RpgClass>()
                        .setName(CLASSES_REGISTRY_NAME)
        );
    }

    public static IForgeRegistry<RpgClass> registry() {
        if (SUPPLIER == null) throw new IllegalStateException("Class registry not created yet: " + CLASSES_REGISTRY_NAME);
        return SUPPLIER.get();
    }
}