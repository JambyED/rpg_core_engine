package ru.rpgcore.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.rpgcore.RpgCore;

@Mod.EventBusSubscriber(modid = RpgCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RpgPlayerCloneEvents {
    private RpgPlayerCloneEvents() {}

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        // Happens on respawn / dimension change. We need to keep our RPG data.
        if (!(event.getOriginal() instanceof ServerPlayer oldP)) return;
        if (!(event.getEntity() instanceof ServerPlayer newP)) return;

        CompoundTag oldData = oldP.getPersistentData();
        if (!oldData.contains(RpgCore.MODID)) return;

        CompoundTag root = oldData.getCompound(RpgCore.MODID).copy();
        newP.getPersistentData().put(RpgCore.MODID, root);
    }

}