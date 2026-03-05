package ru.rpgcore.network.msg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import ru.rpgcore.core.config.RpgGameRules;
import ru.rpgcore.core.config.RpgWorldConfigData;
import ru.rpgcore.core.level.RpgLevelingService;
import ru.rpgcore.core.profile.RpgProfile;
import ru.rpgcore.core.xp.RpgXpCurve;
import ru.rpgcore.network.RpgNetwork;

import java.util.Map;
import java.util.function.Supplier;

public final class C2S_RequestProfile {

    public C2S_RequestProfile() {}

    public static void encode(C2S_RequestProfile msg, FriendlyByteBuf buf) {
        // no fields
    }

    public static C2S_RequestProfile decode(FriendlyByteBuf buf) {
        return new C2S_RequestProfile();
    }

    public static void handle(C2S_RequestProfile msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();

        ctx.enqueueWork(() -> {
            if (player == null) return;

            // sync & load profile
            RpgProfile p = RpgLevelingService.syncLevel(player);

            ServerLevel level = player.serverLevel();
            RpgWorldConfigData cfg = RpgWorldConfigData.get(level);
            int maxLevel = cfg.getMaxLevel();

            // XP breakdown
            int xpToNext;
            int xpIntoLevel;
            int xpNeededThisLevel;

            if (p.level() >= maxLevel) {
                xpToNext = -1;
                xpIntoLevel = 0;
                xpNeededThisLevel = 0;
            } else {
                int totalForThis = RpgXpCurve.totalXpForLevel(p.level(), cfg);
                int totalForNext = RpgXpCurve.totalXpForLevel(p.level() + 1, cfg);

                xpIntoLevel = Math.max(0, p.xp() - totalForThis);
                xpNeededThisLevel = Math.max(1, totalForNext - totalForThis);
                xpToNext = Math.max(0, totalForNext - p.xp());
            }

            // Tokens
            int tokensTotal = p.totalPerkTokensGranted();
            int tokensSpent = p.perkTokensSpent();
            int tokensAvailable = p.perkTokensAvailable();

            // HUD flags
            boolean hudEnabled = level.getGameRules().getBoolean(RpgGameRules.RPG_HUD_ENABLED);
            boolean hideVanillaHud = level.getGameRules().getBoolean(RpgGameRules.RPG_HIDE_VANILLA_HUD);

            // Class
            String classId = (p.hasClass() ? p.classId() : "");

            // Tier picks (tier -> perkId)
            Map<Integer, String> chosenByTier = p.chosenPerksByTier();

            RpgNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new S2C_ProfileData(
                            p.level(),
                            maxLevel,
                            p.xp(),
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
        });

        ctx.setPacketHandled(true);
    }
}