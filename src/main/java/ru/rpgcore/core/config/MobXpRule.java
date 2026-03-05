package ru.rpgcore.core.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import ru.rpgcore.util.NbtMatcher;

public final class MobXpRule {
    private final ResourceLocation entityId;
    private final int xp;
    private final CompoundTag nbtPredicate; // nullable

    public MobXpRule(ResourceLocation entityId, int xp, CompoundTag nbtPredicate) {
        this.entityId = entityId;
        this.xp = xp;
        this.nbtPredicate = nbtPredicate;
    }

    public ResourceLocation entityId() { return entityId; }
    public int xp() { return xp; }
    public CompoundTag nbtPredicate() { return nbtPredicate; }
    public boolean hasPredicate() { return nbtPredicate != null; }

    public boolean matchesEntityNbt(CompoundTag entityNbt) {
        if (nbtPredicate == null) return true;
        return NbtMatcher.matches(nbtPredicate, entityNbt);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("entity", entityId.toString());
        tag.putInt("xp", xp);
        if (nbtPredicate != null) tag.put("predicate", nbtPredicate.copy());
        return tag;
    }

    public static MobXpRule fromNbt(CompoundTag tag) {
        ResourceLocation id = new ResourceLocation(tag.getString("entity"));
        int xp = tag.getInt("xp");
        CompoundTag predicate = tag.contains("predicate") ? tag.getCompound("predicate") : null;
        return new MobXpRule(id, xp, predicate);
    }

    public boolean sameKey(ResourceLocation id, CompoundTag predicateOrNull) {
        if (!entityId.equals(id)) return false;
        if (nbtPredicate == null && predicateOrNull == null) return true;
        if (nbtPredicate != null && predicateOrNull != null) return nbtPredicate.equals(predicateOrNull);
        return false;
    }
}