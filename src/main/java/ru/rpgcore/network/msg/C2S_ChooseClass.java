package ru.rpgcore.network.msg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import ru.rpgcore.api.class_.event.RpgClassChangedEvent;
import ru.rpgcore.core.class_.RpgClassRegistries;
import ru.rpgcore.core.config.RpgGameRules;
import ru.rpgcore.core.config.RpgWorldConfigData;
import ru.rpgcore.core.level.RpgLevelingService;
import ru.rpgcore.core.profile.RpgProfile;
import ru.rpgcore.core.profile.RpgProfileStorage;
import ru.rpgcore.core.xp.RpgXpCurve;
import ru.rpgcore.network.RpgNetwork;

import java.util.Map;
import java.util.function.Supplier;

public final class C2S_ChooseClass {

    public final String classId; // "modid:class"

    public C2S_ChooseClass(String classId) {
        this.classId = classId == null ? "" : classId;
    }

    public static void encode(C2S_ChooseClass msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.classId);
    }

    public static C2S_ChooseClass decode(FriendlyByteBuf buf) {
        return new C2S_ChooseClass(buf.readUtf(32767));
    }

    public static void handle(C2S_ChooseClass msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();

        ctx.enqueueWork(() -> {
            if (player == null) return;

            RpgProfile p = RpgLevelingService.syncLevel(player);

            // choose once
            if (p.hasClass()) {
                RpgNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        S2C_ActionMessage.error("rpg_core.class.gui.already_chosen", "")
                );
                sendProfile(player, p);
                return;
            }

            ResourceLocation id = ResourceLocation.tryParse(msg.classId);
            if (id == null) {
                RpgNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        S2C_ActionMessage.error("rpg_core.class.gui.invalid", "")
                );
                sendProfile(player, p);
                return;
            }

            var reg = RpgClassRegistries.registry();
            if (!reg.containsKey(id)) {
                RpgNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        S2C_ActionMessage.error("rpg_core.class.gui.not_registered", id.toString())
                );
                sendProfile(player, p);
                return;
            }

            ResourceLocation old = p.classIdAsRL();
            p.setClassId(id.toString());
            RpgProfileStorage.save(player, p);

            MinecraftForge.EVENT_BUS.post(new RpgClassChangedEvent(player, old, id));

            RpgNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    S2C_ActionMessage.ok("rpg_core.class.gui.chosen", id.toString())
            );

            sendProfile(player, p);

            // refresh class list info on client (so GUI disables choose)
            RpgNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new S2C_ClassList(
                            reg.getKeys().stream().map(ResourceLocation::toString).sorted().toList(),
                            p.classId(),
                            !p.hasClass()
                    )
            );
        });

        ctx.setPacketHandled(true);
    }

    private static void sendProfile(ServerPlayer player, RpgProfile profile) {
        ServerLevel level = player.serverLevel();
        RpgWorldConfigData cfg = RpgWorldConfigData.get(level);
        int maxLevel = cfg.getMaxLevel();

        int xpToNext;
        int xpIntoLevel;
        int xpNeededThisLevel;

        if (profile.
                level() >= maxLevel) {
            xpToNext = -1;
            xpIntoLevel = 0;
            xpNeededThisLevel = 0;
        } else {
            int totalForThis = RpgXpCurve.totalXpForLevel(profile.level(), cfg);
            int totalForNext = RpgXpCurve.totalXpForLevel(profile.level() + 1, cfg);

            xpIntoLevel = Math.max(0, profile.xp() - totalForThis);
            xpNeededThisLevel = Math.max(1, totalForNext - totalForThis);
            xpToNext = Math.max(0, totalForNext - profile.xp());
        }

        int tokensTotal = profile.totalPerkTokensGranted();
        int tokensSpent = profile.perkTokensSpent();
        int tokensAvailable = profile.perkTokensAvailable();

        boolean hudEnabled = level.getGameRules().getBoolean(RpgGameRules.RPG_HUD_ENABLED);
        boolean hideVanillaHud = level.getGameRules().getBoolean(RpgGameRules.RPG_HIDE_VANILLA_HUD);

        String classId = (profile.hasClass() ? profile.classId() : "");
        Map<Integer, String> chosenByTier = profile.chosenPerksByTier();

        RpgNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new S2C_ProfileData(
                        profile.level(),
                        maxLevel,
                        profile.xp(),
                        xpToNext,
                        tokensTotal,
                        tokensSpent,
                        tokensAvailable,
                        classId,
                        hudEnabled,
                        hideVanillaHud,
                        xpIntoLevel,
                        xpNeededThisLevel,
                        chosenByTier
                )
        );
    }
}