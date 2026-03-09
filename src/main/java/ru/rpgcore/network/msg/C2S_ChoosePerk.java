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

            RpgProfile profile = RpgLevelingService.syncLevel(player);

            // tier must be explicit
            if (msg.tier <= 0) {
                sendProfile(player, profile);
                return;
            }

            // validate perk id
            ResourceLocation perkId = ResourceLocation.tryParse(msg.perkId);
            if (perkId == null || RpgPerkRegistries.registry().getValue(perkId) == null) {
                RpgNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        S2C_ActionMessage.error("rpg_core.perks.choose.invalid", msg.perkId)
                );
                sendProfile(player, profile);
                return;
            }

            // player must have enough level for that tier
            int maxTierByLevel = profile.level() / 5;
            if (msg.tier > maxTierByLevel) {
                RpgNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        S2C_ActionMessage.error("rpg_core.perks.offers.none_level", Integer.toString(msg.tier * 5))
                );
                sendProfile(player, profile);
                return;
            }

            // player must have a free token
            if (profile.perkTokensAvailable() <= 0) {
                RpgNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        S2C_ActionMessage.error("rpg_core.perks.choose.not_enough_tokens", "")
                );
                sendProfile(player, profile);
                return;
            }

            // tier already chosen
            if (profile.hasChosenTier(msg.tier)) {
                RpgNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        S2C_ActionMessage.error("rpg_core.perks.choose.already", Integer.toString(msg.tier))
                );
                sendProfile(player, profile);
                return;
            }

            // get default offers
            List<ResourceLocation> defaults = RpgPerkOfferLogic.getOffersForTier(msg.tier);

            // let addons override/replace
            RpgPerkOffersEvent evt = new RpgPerkOffersEvent(player, msg.tier, defaults);
            MinecraftForge.
                    EVENT_BUS.post(evt);

            List<ResourceLocation> offers = List.copyOf(evt.offersView());
            if (!offers.contains(perkId)) {
                RpgNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        S2C_ActionMessage.error("rpg_core.perks.choose.not_offered", "")
                );
                sendProfile(player, profile);
                return;
            }

            // apply perk choice
            profile.addPerk(perkId.toString());
            profile.setChosenPerkForTier(msg.tier, perkId.toString());
            profile.spendPerkTokens(1);
            RpgProfileStorage.save(player, profile);

            RpgNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    S2C_ActionMessage.ok("rpg_core.perks.choose.done", perkId.toString())
            );

            sendProfile(player, profile);
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

        if (profile.level() >= maxLevel) {
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

        String classId = profile.hasClass() ? profile.classId() : "";
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
                        profile.balance(),
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