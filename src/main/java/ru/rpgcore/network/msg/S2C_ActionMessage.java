package ru.rpgcore.network.msg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class S2C_ActionMessage {
    public final boolean isError;
    public final String key;
    public final String arg;

    private S2C_ActionMessage(boolean isError, String key, String arg) {
        this.isError = isError;
        this.key = key;
        this.arg = arg;
    }

    public static S2C_ActionMessage ok(String key, String arg) {
        return new S2C_ActionMessage(false, key, arg);
    }

    public static S2C_ActionMessage error(String key, String arg) {
        return new S2C_ActionMessage(true, key, arg);
    }

    public Component asComponent() {
        if (arg == null || arg.isBlank()) return Component.translatable(key);
        return Component.translatable(key, arg);
    }

    public static void encode(S2C_ActionMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isError);
        buf.writeUtf(msg.key == null ? "" : msg.key);
        buf.writeUtf(msg.arg == null ? "" : msg.arg);
    }

    public static S2C_ActionMessage decode(FriendlyByteBuf buf) {
        boolean err = buf.readBoolean();
        String key = buf.readUtf();
        if (key.isEmpty()) key = "rpg_core.gui.perks.unknown";
        String arg = buf.readUtf();
        return new S2C_ActionMessage(err, key, arg);
    }

    public static void handle(S2C_ActionMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientActionMessageCache.set(msg));
        ctx.setPacketHandled(true);
    }
}