package ru.rpgcore.network.msg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class S2C_PerkOffers {
    public final int tier;
    public final List<ResourceLocation> offers;

    // error payload
    public final String errorKey;
    public final int errorArg;

    public S2C_PerkOffers(int tier, List<ResourceLocation> offers) {
        this.tier = tier;
        this.offers = new ArrayList<>(offers);
        this.errorKey = null;
        this.errorArg = 0;
    }

    public S2C_PerkOffers(String errorKey, int errorArg) {
        this.tier = 0;
        this.offers = List.of();
        this.errorKey = errorKey;
        this.errorArg = errorArg;
    }

    public static void encode(S2C_PerkOffers msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.tier);

        buf.writeBoolean(msg.errorKey != null);
        if (msg.errorKey != null) {
            buf.writeUtf(msg.errorKey);
            buf.writeVarInt(msg.errorArg);
            buf.writeVarInt(0);
            return;
        }

        buf.writeVarInt(msg.offers.size());
        for (ResourceLocation id : msg.offers) {
            buf.writeResourceLocation(id);
        }
    }

    public static S2C_PerkOffers decode(FriendlyByteBuf buf) {
        int tier = buf.readVarInt();
        boolean hasErr = buf.readBoolean();

        if (hasErr) {
            String key = buf.readUtf();
            int arg = buf.readVarInt();
            buf.readVarInt(); // size=0
            return new S2C_PerkOffers(key, arg);
        }

        int n = buf.readVarInt();
        List<ResourceLocation> ids = new ArrayList<>(n);
        for (int i = 0; i < n; i++) ids.add(buf.readResourceLocation());

        return new S2C_PerkOffers(tier, ids);
    }

    public static void handle(S2C_PerkOffers msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientPerkOffersCache.set(msg));
        ctx.setPacketHandled(true);
    }
}