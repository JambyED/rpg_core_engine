package ru.rpgcore.network.msg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class S2C_ClassList {

    public final List<String> classIds;     // "modid:class"
    public final String currentClassId;     // "" если нет
    public final boolean canChoose;         // true если игрок может выбрать класс сейчас

    public S2C_ClassList(List<String> classIds, String currentClassId, boolean canChoose) {
        this.classIds = classIds == null ? List.of() : List.copyOf(classIds);
        this.currentClassId = currentClassId == null ? "" : currentClassId;
        this.canChoose = canChoose;
    }

    public static void encode(S2C_ClassList msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.classIds.size());
        for (String id : msg.classIds) buf.writeUtf(id == null ? "" : id);

        buf.writeUtf(msg.currentClassId);
        buf.writeBoolean(msg.canChoose);
    }

    public static S2C_ClassList decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        List<String> ids = new ArrayList<>(Math.max(0, n));
        for (int i = 0; i < n; i++) {
            String s = buf.readUtf(32767);
            if (s != null && !s.isBlank()) ids.add(s);
        }
        String current = buf.readUtf(32767);
        boolean canChoose = buf.readBoolean();
        return new S2C_ClassList(ids, current, canChoose);
    }

    public static void handle(S2C_ClassList msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientClassListCache.set(msg));
        ctx.setPacketHandled(true);
    }

}