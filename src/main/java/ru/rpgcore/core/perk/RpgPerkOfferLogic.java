package ru.rpgcore.core.perk;

import net.minecraft.resources.ResourceLocation;
import ru.rpgcore.api.perk.RpgPerk;
import ru.rpgcore.core.profile.RpgProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RpgPerkOfferLogic {
    private RpgPerkOfferLogic() {}

    /** Первый невыбранный тир в диапазоне [1..maxTierAllowed]. */
    public static int firstUnchosenTier(RpgProfile profile, int maxTierAllowed) {
        for (int t = 1; t <= maxTierAllowed; t++) {
            if (!profile.hasChosenTier(t)) return t;
        }
        return -1;
    }

    /** Дефолтные офферы: первые 3 перка данного тира по сортировке id. */
    public static List<ResourceLocation> getOffersForTier(int tier) {
        var reg = RpgPerkRegistries.registry();

        List<ResourceLocation> ids = new ArrayList<>(reg.getKeys());
        ids.sort(Comparator.comparing(ResourceLocation::toString));

        List<ResourceLocation> out = new ArrayList<>();
        for (ResourceLocation id : ids) {
            RpgPerk perk = reg.getValue(id);
            if (perk != null && perk.tier() == tier) {
                out.add(id);
                if (out.size() == 3) break;
            }
        }
        return out;
    }
}