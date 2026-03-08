package ru.rpgcore.network.msg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import ru.rpgcore.api.perk.offer.RpgPerkOffersEvent;
import ru.rpgcore.core.config.RpgWorldConfigData;
import ru.rpgcore.core.level.RpgLevelingService;
import ru.rpgcore.core.perk.RpgPerkOfferLogic;
import ru.rpgcore.core.perk.RpgPerkRegistries;
import ru.rpgcore.core.profile.RpgProfile;
import ru.rpgcore.network.RpgNetwork;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

public final class C2S_RequestPerkOffers {

    public C2S_RequestPerkOffers() {}

    public static void encode(C2S_RequestPerkOffers msg, FriendlyByteBuf buf) {
        // no fields
    }

    public static C2S_RequestPerkOffers decode(FriendlyByteBuf buf) {
        return new C2S_RequestPerkOffers();
    }

    public static void handle(C2S_RequestPerkOffers msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();

        ctx.enqueueWork(() -> {
            if (player == null) return;

            RpgProfile profile = RpgLevelingService.syncLevel(player);

            int worldMaxTiers = RpgWorldConfigData.get(player.serverLevel()).getMaxTiers();
            int maxTierAllowed = Math.min(worldMaxTiers, profile.level() / 5);

            if (maxTierAllowed <= 0) {
                // ВАЖНО: errorArg = tier, который будет открыт первым (tier=1 -> нужен уровень 5)
                sendError(player, "rpg_core.perks.offers.none_level", 1); // arg = tier to unlock
                return;
            }

            int tier = RpgPerkOfferLogic.firstUnchosenTier(profile, maxTierAllowed);
            if (tier < 0) {
                sendError(player, "rpg_core.perks.offers.none_left", 0);
                return;
            }

            // Core defaults (best-effort)
            List<ResourceLocation> defaults = RpgPerkOfferLogic.getOffersForTier(tier);

            // Allow addons to override/replace
            RpgPerkOffersEvent evt = new RpgPerkOffersEvent(player, tier, defaults);
            MinecraftForge.EVENT_BUS.post(evt);

            // Normalize + validate
            List<ResourceLocation> offers = normalizeAndValidateOffers(tier, evt.offersView());
            if (offers.size() < 3) {
                sendError(player, "rpg_core.perks.offers.not_enough", tier);
                return;
            }

            RpgNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new S2C_PerkOffers(tier, offers)
            );
        });

        ctx.setPacketHandled(true);
    }

    private static List<ResourceLocation> normalizeAndValidateOffers(int tier, List<ResourceLocation> raw) {
        var reg = RpgPerkRegistries.registry();

        // Keep order, remove duplicates, and keep only valid perks for this tier
        LinkedHashSet<ResourceLocation> unique = new LinkedHashSet<>();
        if (raw != null) unique.addAll(raw);

        List<ResourceLocation> out = new ArrayList<>(3);
        for (ResourceLocation id : unique) {
            var perk = reg.getValue(id);
            if (perk == null) continue;
            if (perk.tier() != tier) continue;
            out.add(id);
            if (out.size() == 3) break;
        }
        return out;
    }

    private static void sendError(ServerPlayer player, String key, int arg) {
        RpgNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new S2C_PerkOffers(key, arg)
        );
    }
}