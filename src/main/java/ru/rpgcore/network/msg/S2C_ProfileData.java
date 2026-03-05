package ru.rpgcore.network.msg;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class S2C_ProfileData {

    // базовые поля (как было)
    public final int level;
    public final int maxLevel;
    public final int xp;
    public final int xpToNext;

    public final int tokensTotal;
    public final int tokensSpent;
    public final int tokensAvailable;

    public final String classId;

    public final boolean hudEnabled;
    public final boolean hideVanillaHud;

    public final int xpIntoLevel;
    public final int xpNeededThisLevel;

    // NEW: выбранные перки по тирам: tier -> "namespace:perk"
    public final Map<Integer, String> chosenPerkByTier;

    public S2C_ProfileData(
            int level,
            int maxLevel,
            int xp,
            int xpToNext,
            int tokensTotal,
            int tokensSpent,
            int tokensAvailable,
            String classId,
            boolean hudEnabled,
            boolean hideVanillaHud,
            int xpIntoLevel,
            int xpNeededThisLevel,
            Map<Integer, String> chosenPerkByTier
    ) {
        this.level = level;
        this.maxLevel = maxLevel;
        this.xp = xp;
        this.xpToNext = xpToNext;

        this.tokensTotal = tokensTotal;
        this.tokensSpent = tokensSpent;
        this.tokensAvailable = tokensAvailable;

        this.classId = classId == null ? "" : classId;

        this.hudEnabled = hudEnabled;
        this.hideVanillaHud = hideVanillaHud;

        this.xpIntoLevel = xpIntoLevel;
        this.xpNeededThisLevel = xpNeededThisLevel;

        this.chosenPerkByTier = chosenPerkByTier == null ? Map.of() : Map.copyOf(chosenPerkByTier);
    }

    public static void encode(S2C_ProfileData msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.level);
        buf.writeInt(msg.maxLevel);
        buf.writeInt(msg.xp);
        buf.writeInt(msg.xpToNext);

        buf.writeInt(msg.tokensTotal);
        buf.writeInt(msg.tokensSpent);
        buf.writeInt(msg.tokensAvailable);

        buf.writeUtf(msg.classId);

        buf.writeBoolean(msg.hudEnabled);
        buf.writeBoolean(msg.hideVanillaHud);

        buf.writeInt(msg.xpIntoLevel);
        buf.writeInt(msg.xpNeededThisLevel);

        // NEW: chosenPerkByTier
        Map<Integer, String> map = msg.chosenPerkByTier;
        buf.writeVarInt(map.size());
        for (var e : map.entrySet()) {
            buf.writeVarInt(e.getKey());
            buf.writeUtf(e.getValue() == null ? "" : e.getValue());
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

        String classId = buf.readUtf(32767);

        boolean hudEnabled = buf.readBoolean();
        boolean hideVanillaHud = buf.readBoolean();

        int xpIntoLevel = buf.readInt();
        int xpNeededThisLevel = buf.readInt();

        // NEW: chosenPerkByTier
        int size = buf.readVarInt();
        Map<Integer, String> chosen = new HashMap<>();
        for (int i = 0; i < size; i++) {
            int tier = buf.readVarInt();
            String perkId = buf.readUtf(32767);
            if (tier > 0 && perkId != null && !perkId.isBlank()) {
                chosen.put(tier, perkId);
            }
        }

        return new S2C_ProfileData(
                level, maxLevel, xp, xpToNext,
                tokensTotal, tokensSpent, tokensAvailable,
                classId,
                hudEnabled, hideVanillaHud,
                xpIntoLevel, xpNeededThisLevel,
                chosen
        );
    }

    public static void handle(S2C_ProfileData msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.
                Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            // на клиенте обновляем кэш
            ClientProfileCache.set(msg);

            // если вдруг GUI открыт — можно триггерить перерисовку не нужно, сам экран прочитает кэш
            Minecraft.getInstance();
        });
        ctx.setPacketHandled(true);
    }
}