package ru.rpgcore.network.msg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import ru.rpgcore.core.class_.RpgClassRegistries;
import ru.rpgcore.core.profile.RpgProfile;
import ru.rpgcore.core.profile.RpgProfileStorage;
import ru.rpgcore.network.RpgNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class C2S_RequestClassList {

    public C2S_RequestClassList() {}

    public static void encode(C2S_RequestClassList msg, FriendlyByteBuf buf) {
        // no fields
    }

    public static C2S_RequestClassList decode(FriendlyByteBuf buf) {
        return new C2S_RequestClassList();
    }

    public static void handle(C2S_RequestClassList msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();

        ctx.enqueueWork(() -> {
            if (player == null) return;

            RpgProfile p = RpgProfileStorage.load(player);

            // registry keys
            var reg = RpgClassRegistries.registry();
            List<String> ids = new ArrayList<>();
            for (var id : reg.getKeys()) ids.add(id.toString());
            ids.sort(String::compareTo);

            boolean canChoose = !p.hasClass(); // "выбирается один раз"

            RpgNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new S2C_ClassList(ids, p.classId(), canChoose)
            );
        });

        ctx.setPacketHandled(true);
    }
}