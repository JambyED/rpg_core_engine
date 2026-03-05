package ru.rpgcore.network.msg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import ru.rpgcore.api.perk.offer.RpgPerkOffersEvent;
import ru.rpgcore.core.config.RpgGameRules;
import ru.rpgcore.core.config.RpgWorldConfigData;
import ru.rpgcore.core.level.RpgLevelingService;
import ru.rpgcore.core.perk.RpgPerkOfferLogic;
import ru.rpgcore.core.perk.RpgPerkRegistries;
import ru.rpgcore.core.profile.RpgProfile;
import ru.rpgcore.core.profile.RpgProfileStorage;
import ru.rpgcore.core.xp.RpgXpCurve;
import ru.rpgcore.network.RpgNetwork;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class C2S_ChoosePerk {

    public final int tier;
    public final String perkId;

    public C2S_ChoosePerk(int tier, String perkId) {
        this.tier = tier;
        this.perkId = perkId == null ? "" : perkId;
    }

    public static void encode(C2S_ChoosePerk msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.tier);
        buf.writeUtf(msg.perkId);
    }

    public static C2S_ChoosePerk decode(FriendlyByteBuf buf) {
        int tier = buf.readInt();
        String perkId = buf.readUtf(32767);
        return new C2S_ChoosePerk(tier, perkId);
    }

    public static void handle(C2S_ChoosePerk msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();

        ctx.enqueueWork(() -> {
            if (player == null) return;

            // sync & load profile
            RpgProfile profile = RpgLevelingService.syncLevel(player);

            // tier must be explicit
            if (msg.tier <= 0) {
                sendProfile(player, profile);
                return;
            }

            // must be allowed by level/tokens
            int maxTierAllowed = profile.totalPerkTokensGranted();
            if (msg.tier > maxTierAllowed) {
                sendProfile(player, profile);
                return;
            }

            // deny if tier already chosen
            if (profile.hasChosenTier(msg.tier)) {
                sendProfile(player, profile);
                return;
            }

            ResourceLocation rl = ResourceLocation.tryParse(msg.perkId);
            if (rl == null) {
                sendProfile(player, profile);
                return;
            }

            // perk must exist
            var reg = RpgPerkRegistries.registry();
            var perk = reg.getValue(rl);
            if (perk == null) {
                sendProfile(player, profile);
                return;
            }

            // validate offered list for THIS tier (addon-safe)
            List<ResourceLocation> defaults = RpgPerkOfferLogic.getOffersForTier(msg.tier);
            RpgPerkOffersEvent evt = new RpgPerkOffersEvent(player, msg.tier, defaults);
            MinecraftForge.EVENT_BUS.post(evt);

            List<ResourceLocation> offers = List.copyOf(evt.offersView());
            if (!offers.contains(rl)) {
                sendProfile(player, profile);
                return;
            }

            // apply choice
            profile.setChosenPerkForTier(msg.tier, rl.toString());
            profile.addPerk(rl.toString()); // convenience list
            profile.spendPerkTokens(1);

            RpgProfileStorage.save(player, profile);

            sendProfile(player, profile);
        });

        ctx.setPacketHandled(true);
    }

    private static void sendProfile(ServerPlayer player, RpgProfile profile) {
        ServerLevel level = player.serverLevel();
        RpgWorldConfigData cfg = RpgWorldConfigData.get(level);
        int maxLevel = cfg.getMaxLevel();

        // XP breakdown
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

        // Tokens
        int tokensTotal = profile.totalPerkTokensGranted();
        int tokensSpent = profile.perkTokensSpent();
        int tokensAvailable = profile.perkTokensAvailable();

        // HUD flags
        boolean hudEnabled = level.getGameRules().getBoolean(RpgGameRules.RPG_HUD_ENABLED);
        boolean hideVanillaHud = level.getGameRules().getBoolean(RpgGameRules.RPG_HIDE_VANILLA_HUD);

        // Class
        String classId = (profile.hasClass() ? profile.classId() : "");

        // Tier picks (tier -> perkId)
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