package ru.rpgcore.core.class_;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.rpgcore.RpgCore;
import ru.rpgcore.api.class_.RpgClass;

import java.util.List;

/**
 * TEMP: Debug classes for DEV testing of Class GUI and networking.
 * Will be moved to rpg_content addon before release.
 */
public final class RpgCoreClasses {
    private RpgCoreClasses() {}

    public static final DeferredRegister<RpgClass> CLASSES =
            DeferredRegister.create(RpgClassRegistries.CLASSES_REGISTRY_NAME, RpgCore.MODID);

    // NOTE: Using a vanilla 256x256 texture as placeholder icon (top-left fragment).
    private static final ResourceLocation ICON_PLACEHOLDER =
            new ResourceLocation("minecraft", "textures/gui/icons.png");

    public static final RegistryObject<RpgClass> DEBUG_WARRIOR =
            CLASSES.register("debug_warrior", () -> new DebugClass(
                    "rpg_core.debug.class.warrior.name",
                    "rpg_core.debug.class.warrior.desc",
                    ICON_PLACEHOLDER,
                    List.of(
                            Component.translatable("rpg_core.debug.class.warrior.stat1"),
                            Component.translatable("rpg_core.debug.class.warrior.stat2")
                    )
            ));

    public static final RegistryObject<RpgClass> DEBUG_SCOUT =
            CLASSES.register("debug_scout", () -> new DebugClass(
                    "rpg_core.debug.class.rogue.name",
                    "rpg_core.debug.class.rogue.desc",
                    ICON_PLACEHOLDER,
                    List.of(
                            Component.translatable("rpg_core.debug.class.rogue.stat1"),
                            Component.translatable("rpg_core.debug.class.rogue.stat2")
                    )
            ));

    public static final RegistryObject<RpgClass> DEBUG_BERSERK =
            CLASSES.register("debug_berserk", () -> new DebugClass(
                    "rpg_core.debug.class.berserk.name",
                    "rpg_core.debug.class.berserk.desc",
                    ICON_PLACEHOLDER,
                    List.of(
                            Component.translatable("rpg_core.debug.class.berserk.stat1"),
                            Component.translatable("rpg_core.debug.class.berserk.stat2")
                    )
            ));

    public static final RegistryObject<RpgClass> DEBUG_DUELIST =
            CLASSES.register("debug_duelist", () -> new DebugClass(
                    "rpg_core.debug.class.duelist.name",
                    "rpg_core.debug.class.duelist.desc",
                    ICON_PLACEHOLDER,
                    List.of(
                            Component.translatable("rpg_core.debug.class.duelist.stat1"),
                            Component.translatable("rpg_core.debug.class.duelist.stat2")
                    )
            ));

    public static void register(IEventBus modBus) {
        CLASSES.register(modBus);
    }

    private static final class DebugClass implements RpgClass {
        private final String nameKey;
        private final String descKey;
        private final ResourceLocation icon;
        private final List<Component> stats;

        private DebugClass(String nameKey, String descKey, ResourceLocation icon, List<Component> stats) {
            this.nameKey = nameKey;
            this.descKey = descKey;
            this.icon = icon;
            this.stats = (stats == null) ? List.of() : List.copyOf(stats);
        }

        @Override
        public Component displayName() {
            return Component.translatable(nameKey);
        }

        @Override
        public Component description() {
            return Component.translatable(descKey);
        }

        @Override
        public ResourceLocation iconTexture() {
            return icon;
        }

        @Override
        public List<Component> statsLines() {
            return stats;
        }
    }
}