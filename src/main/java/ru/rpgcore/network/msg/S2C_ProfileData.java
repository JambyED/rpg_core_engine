package ru.rpgcore.network.msg;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class S2C_ProfileData {
    public final int level;
    public final int maxLevel;
    public final int xp;
    public final int xpToNext;

    public final int tokensTotal;
    public final int tokensSpent;
    public final int tokensAvailable;

    public final long balance;

    public final String classId;

    public final boolean hudEnabled;
    public final boolean hideVanillaHud;

    public final int xpIntoLevel;
    public final int xpNeededThisLevel;

    public final Map<Integer, String> chosenPerkByTier;

    public S2C_ProfileData(int level,
                           int maxLevel,
                           int xp,
                           int xpToNext,
                           int tokensTotal,
                           int tokensSpent,
                           int tokensAvailable,
                           long balance,
                           String classId,
                           boolean hudEnabled,
                           boolean hideVanillaHud,
                           int xpIntoLevel,
                           int xpNeededThisLevel,
                           Map<Integer, String> chosenPerkByTier) {
        this.level = level;
        this.maxLevel = maxLevel;
        this.xp = xp;
        this.xpToNext = xpToNext;

        this.tokensTotal = tokensTotal;
        this.tokensSpent = tokensSpent;
        this.tokensAvailable = tokensAvailable;

        this.balance = Math.max(0L, balance);

        this.classId = classId == null ? "" : classId;

        this.hudEnabled = hudEnabled;
        this.hideVanillaHud = hideVanillaHud;

        this.xpIntoLevel = xpIntoLevel;
        this.xpNeededThisLevel = xpNeededThisLevel;

        this.chosenPerkByTier = (chosenPerkByTier == null) ? Map.of() : Map.copyOf(chosenPerkByTier);
    }

    public static void encode(S2C_ProfileData msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level);
        buf.writeInt(msg.maxLevel);
        buf.writeInt(msg.xp);
        buf.writeInt(msg.xpToNext);

        buf.writeInt(msg.tokensTotal);
        buf.writeInt(msg.tokensSpent);
        buf.writeInt(msg.tokensAvailable);

        buf.writeLong(msg.balance);

        buf.writeUtf(msg.classId);

        buf.writeBoolean(msg.hudEnabled);
        buf.writeBoolean(msg.hideVanillaHud);

        buf.writeInt(msg.xpIntoLevel);
        buf.writeInt(msg.xpNeededThisLevel);

        buf.writeInt(msg.chosenPerkByTier.size());
        for (var e : msg.chosenPerkByTier.entrySet()) {
            buf.writeInt(e.getKey());
            buf.writeUtf(e.getValue());
        }
    }

    public static S2C_ProfileData decode(FriendlyByteBuf buf) {
        int level = buf.readInt();
        int maxLevel = buf.readInt();
        int xp = buf.readInt();
        int xpToNext = buf.readInt();

        int tokensTotal = buf.readInt();
        int tokensSpent = buf.readInt();
        int tokensAvailable = buf.readInt();

        long balance = buf.readLong();

        String classId = buf.readUtf(32767);

        boolean hudEnabled = buf.readBoolean();
        boolean hideVanillaHud = buf.readBoolean();

        int xpIntoLevel = buf.readInt();
        int xpNeededThisLevel = buf.readInt();

        int size = buf.readInt();
        Map<Integer, String> chosenByTier = new HashMap<>();
        for (int i = 0; i < size; i++) {
            int tier = buf.readInt();
            String perkId = buf.readUtf(32767);
            chosenByTier.put(tier, perkId);
        }

        return new S2C_ProfileData(
                level,
                maxLevel,
                xp,
                xpToNext,
                tokensTotal,
                tokensSpent,
                tokensAvailable,
                balance,
                classId,
                hudEnabled,
                hideVanillaHud,
                xpIntoLevel,
                xpNeededThisLevel,
                chosenByTier
        );
    }

    public static void handle(S2C_ProfileData msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            ClientProfileCache.set(msg);
        });
        ctx.setPacketHandled(true);
    }
}