package ru.rpgcore.core.storage;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.rpgcore.RpgCore;

public final class RpgStorageMenuType {

    private RpgStorageMenuType() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RpgCore.MODID);

    public static final RegistryObject<MenuType<RpgStorageMenu>> STORAGE_MENU =
            MENUS.register("storage_menu",
                    () -> IForgeMenuType.create((windowId, playerInv, data) -> {
                        int size = 27;
                        if (data != null && data.readableBytes() > 0) {
                            size = Math.max(1, data.readVarInt());
                        }
                        return RpgStorageMenu.client(windowId, playerInv, size);
                    }));
}