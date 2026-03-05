package ru.rpgcore.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import ru.rpgcore.RpgCore;
import ru.rpgcore.network.msg.*;

public final class RpgNetwork {
    private RpgNetwork() {}

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(RpgCore.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int index = 0;

    public static void register() {
        index = 0;

        // ===== Profile =====
        CHANNEL.messageBuilder(C2S_RequestProfile.class, index++)
                .encoder(C2S_RequestProfile::encode)
                .decoder(C2S_RequestProfile::decode)
                .consumerMainThread(C2S_RequestProfile::handle)
                .add();

        CHANNEL.messageBuilder(S2C_ProfileData.class, index++)
                .encoder(S2C_ProfileData::encode)
                .decoder(S2C_ProfileData::decode)
                .consumerMainThread(S2C_ProfileData::handle)
                .add();

        // ===== Perks GUI =====
        CHANNEL.messageBuilder(C2S_RequestPerkOffers.class, index++)
                .encoder(C2S_RequestPerkOffers::encode)
                .decoder(C2S_RequestPerkOffers::decode)
                .consumerMainThread(C2S_RequestPerkOffers::handle)
                .add();

        CHANNEL.messageBuilder(S2C_PerkOffers.class, index++)
                .encoder(S2C_PerkOffers::encode)
                .decoder(S2C_PerkOffers::decode)
                .consumerMainThread(S2C_PerkOffers::handle)
                .add();

        CHANNEL.messageBuilder(C2S_ChoosePerk.class, index++)
                .encoder(C2S_ChoosePerk::encode)
                .decoder(C2S_ChoosePerk::decode)
                .consumerMainThread(C2S_ChoosePerk::handle)
                .add();

        // ===== Classes GUI =====
        CHANNEL.messageBuilder(C2S_RequestClassList.class, index++)
                .encoder(C2S_RequestClassList::encode)
                .decoder(C2S_RequestClassList::decode)
                .consumerMainThread(C2S_RequestClassList::handle)
                .add();

        CHANNEL.messageBuilder(S2C_ClassList.class, index++)
                .encoder(S2C_ClassList::encode)
                .decoder(S2C_ClassList::decode)
                .consumerMainThread(S2C_ClassList::handle)
                .add();

        CHANNEL.messageBuilder(C2S_ChooseClass.class, index++)
                .encoder(C2S_ChooseClass::encode)
                .decoder(C2S_ChooseClass::decode)
                .consumerMainThread(C2S_ChooseClass::handle)
                .add();

        // ===== Action messages =====
        CHANNEL.messageBuilder(S2C_ActionMessage.class, index++)
                .encoder(S2C_ActionMessage::encode)
                .decoder(S2C_ActionMessage::decode)
                .consumerMainThread(S2C_ActionMessage::handle)
                .add();
    }
}