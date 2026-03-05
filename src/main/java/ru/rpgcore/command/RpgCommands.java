package ru.rpgcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import ru.rpgcore.api.class_.event.RpgClassChangedEvent;
import ru.rpgcore.api.perk.offer.RpgPerkOffersEvent;
import ru.rpgcore.core.class_.RpgClassRegistries;
import ru.rpgcore.core.config.MobXpRule;
import ru.rpgcore.core.config.RpgGameRules;
import ru.rpgcore.core.config.RpgWorldConfigData;
import ru.rpgcore.core.level.RpgLevelingService;
import ru.rpgcore.core.perk.RpgPerkOfferLogic;
import ru.rpgcore.core.perk.RpgPerkRegistries;
import ru.rpgcore.core.profile.RpgProfile;
import ru.rpgcore.core.profile.RpgProfileStorage;
import ru.rpgcore.core.xp.RpgXpCurve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class RpgCommands {
    private RpgCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rpg")
                        .then(profile())
                        .then(reset())      // OP-only + selector
                        .then(addXp())      // OP-only
                        .then(config())     // OP-only
                        .then(hud())        // OP-only
                        .then(mobXp())      // OP-only
                        .then(perks())
                        .then(rpgClass())
        );
    }

    private static int failOnlyPlayer(CommandSourceStack source) {
        source.sendFailure(Component.translatable("rpg_core.msg.only_player"));
        return 0;
    }

    /* =========================
       /rpg hud on|off|status (OP only)
       ========================= */
    private static ArgumentBuilder<CommandSourceStack, ?> hud() {
        return Commands.literal("hud")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("on").executes(ctx -> setHud(ctx.getSource(), true)))
                .then(Commands.literal("off").executes(ctx -> setHud(ctx.getSource(), false)))
                .then(Commands.literal("status").executes(ctx -> hudStatus(ctx.getSource())))
                .then(vanillaHud());
    }

    private static int setHud(CommandSourceStack source, boolean enabled) {
        ServerLevel level = source.getLevel();
        level.getGameRules().getRule(RpgGameRules.RPG_HUD_ENABLED).set(enabled, level.getServer());

        source.sendSuccess(
                () -> Component.translatable(enabled ? "rpg_core.hud.enabled" : "rpg_core.hud.disabled"),
                true
        );
        return 1;
    }

    private static int hudStatus(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        boolean enabled = level.getGameRules().getBoolean(RpgGameRules.RPG_HUD_ENABLED);

        source.sendSuccess(
                () -> Component.translatable("rpg_core.hud.status", enabled),
                false
        );
        return 1;
    }

    /* =========================
       /rpg hud vanilla on|off|status (OP only)
       ========================= */
    private static ArgumentBuilder<CommandSourceStack, ?> vanillaHud() {
        return Commands.literal("vanilla")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("on").executes(ctx -> setHideVanillaHud(ctx.getSource(), false)))
                .then(Commands.literal("off").executes(ctx -> setHideVanillaHud(ctx.getSource(), true)))
                .then(Commands.literal("status").executes(ctx -> hideVanillaHudStatus(ctx.getSource())));
    }

    private static int setHideVanillaHud(CommandSourceStack source, boolean hide) {
        ServerLevel level = source.getLevel();
        level.getGameRules().getRule(RpgGameRules.RPG_HIDE_VANILLA_HUD).set(hide, level.getServer());

        source.sendSuccess(
                () -> Component.translatable(hide ? "rpg_core.hud.vanilla_hidden" : "rpg_core.hud.vanilla_shown"),
                true
        );
        return 1;
    }

    private static int hideVanillaHudStatus(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        boolean hide = level.getGameRules().getBoolean(RpgGameRules.RPG_HIDE_VANILLA_HUD);

        source.sendSuccess(
                () -> Component.translatable("rpg_core.hud.vanilla_status", hide),
                false
        );
        return 1;
    }

    /* =========================
       /rpg profile
       ========================= */
    private static ArgumentBuilder<CommandSourceStack, ?> profile() {
        return Commands.literal("profile")
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) return failOnlyPlayer(source);

                    RpgProfile profile = RpgLevelingService.syncLevel(player);

                    ServerLevel level = player.serverLevel();
                    RpgWorldConfigData cfg = RpgWorldConfigData.get(level);
                    int maxLevel = cfg.getMaxLevel();

                    player.sendSystemMessage(Component.translatable("rpg_core.profile.title"));
                    player.sendSystemMessage(Component.translatable("rpg_core.profile.player", player.getGameProfile().getName()));
                    player.sendSystemMessage(Component.translatable("rpg_core.profile.level", profile.level(), maxLevel));
                    player.sendSystemMessage(Component.translatable("rpg_core.profile.total_xp", profile.xp()));

                    if (profile.level() >= maxLevel) {
                        player.sendSystemMessage(Component.translatable("rpg_core.profile.xp_to_next_max"));
                    } else {
                        int nextTotal = RpgXpCurve.totalXpForLevel(profile.level() + 1, cfg);
                        int toNext = Math.max(0, nextTotal - profile.xp());
                        player.sendSystemMessage(Component.translatable("rpg_core.profile.xp_to_next", toNext));
                    }

                    player.sendSystemMessage(Component.translatable(
                            "rpg_core.profile.perk_tokens",
                            profile.totalPerkTokensGranted(),
                            profile.perkTokensSpent(),
                            profile.perkTokensAvailable()
                    ));

                    if (profile.hasClass()) {
                        player.sendSystemMessage(Component.translatable("rpg_core.class.get.value", profile.classId()));
                    } else {
                        player.sendSystemMessage(Component.translatable("rpg_core.class.get.none"));
                    }

                    return 1;
                });
    }

    /* =========================
       /rpg reset <targets> (OP only)
       ========================= */
    private static ArgumentBuilder<CommandSourceStack, ?> reset() {
        return Commands.literal("reset")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(ctx -> {
                            int count = 0;
                            for (ServerPlayer p : EntityArgument.getPlayers(ctx, "targets")) {
                                RpgLevelingService.reset(p);
                                count++;
                                p.sendSystemMessage(Component.
                                        translatable("rpg_core.reset.done_admin_to_target"));
                            }
                            final int finalCount = count;
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("rpg_core.reset.done_admin_summary", finalCount),
                                    true
                            );
                            return finalCount;
                        })
                );
    }

    /* =========================
       /rpg addxp (OP only)
       ========================= */
    private static ArgumentBuilder<CommandSourceStack, ?> addXp() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("addxp")
                .requires(src -> src.hasPermission(2));

        root.then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) return failOnlyPlayer(source);

                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                    RpgProfile p = RpgLevelingService.addXp(player, amount);

                    source.sendSuccess(
                            () -> Component.translatable("rpg_core.addxp.self", amount, p.level()),
                            true
                    );
                    return 1;
                }));

        root.then(Commands.argument("targets", EntityArgument.players())
                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                            int count = 0;

                            for (ServerPlayer p : EntityArgument.getPlayers(ctx, "targets")) {
                                RpgLevelingService.addXp(p, amount);
                                count++;
                            }
                            final int finalCount = count;

                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable("rpg_core.addxp.targets", amount, finalCount),
                                    true
                            );
                            return finalCount;
                        })
                ));

        return root;
    }

    /* =========================
       /rpg config ... (OP only)
       ========================= */
    private static ArgumentBuilder<CommandSourceStack, ?> config() {
        return Commands.literal("config")
                .requires(src -> src.hasPermission(2))
                .then(maxLevel());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> maxLevel() {
        return Commands.literal("maxlevel")
                .then(Commands.literal("get").executes(ctx -> {
                    ServerLevel level = ctx.getSource().getLevel();
                    int v = RpgWorldConfigData.get(level).getMaxLevel();
                    ctx.getSource().sendSuccess(() -> Component.translatable("rpg_core.config.maxlevel.get", v), false);
                    return 1;
                }))
                .then(Commands.literal("set")
                        .then(Commands.argument("value", IntegerArgumentType.integer(5))
                                .executes(ctx -> {
                                    int value = IntegerArgumentType.getInteger(ctx, "value");
                                    if (value % 5 != 0) {
                                        ctx.getSource().sendFailure(Component.translatable("rpg_core.config.maxlevel.must_be_multiple_of_5"));
                                        return 0;
                                    }
                                    ServerLevel level = ctx.getSource().getLevel();
                                    RpgWorldConfigData.get(level).setMaxLevel(value);
                                    ctx.getSource().sendSuccess(() -> Component.
                                            translatable("rpg_core.config.maxlevel.set", value), true);
                                    return 1;
                                })
                        )
                );
    }

    /* =========================
       /rpg mobxp ... (OP only)
       ========================= */
    private static ArgumentBuilder<CommandSourceStack, ?> mobXp() {
        return Commands.literal("mobxp")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("list").executes(ctx -> mobXpList(ctx.getSource())))
                .then(Commands.literal("set")
                        .then(Commands.argument("entity", StringArgumentType.string())
                                .then(Commands.argument("xp", IntegerArgumentType.integer(0))
                                        .executes(ctx -> mobXpSetImpl(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "entity"),
                                                IntegerArgumentType.getInteger(ctx, "xp"),
                                                null
                                        ))
                                        .then(Commands.argument("nbt", StringArgumentType.greedyString())
                                                .executes(ctx -> mobXpSetImpl(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "entity"),
                                                        IntegerArgumentType.getInteger(ctx, "xp"),
                                                        StringArgumentType.getString(ctx, "nbt")
                                                ))
                                        )
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("entity", StringArgumentType.string())
                                .executes(ctx -> mobXpRemoveImpl(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "entity"), null))
                                .then(Commands.argument("nbt", StringArgumentType.greedyString())
                                        .executes(ctx -> mobXpRemoveImpl(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "entity"),
                                                StringArgumentType.getString(ctx, "nbt")))
                                )
                        )
                );
    }

    private static int mobXpSetImpl(CommandSourceStack source, String entityIdStr, int xp, String nbtStr) {
        ResourceLocation id = ResourceLocation.tryParse(entityIdStr);
        if (id == null) {
            source.sendFailure(Component.translatable("rpg_core.mobxp.invalid_entity", entityIdStr));
            return 0;
        }

        CompoundTag predicate = null;
        if (nbtStr != null && !nbtStr.isBlank()) {
            try {
                predicate = TagParser.parseTag(nbtStr);
            } catch (Exception e) {
                source.sendFailure(Component.translatable("rpg_core.mobxp.invalid_nbt"));
                return 0;
            }
        }

        ServerLevel level = source.getLevel();
        RpgWorldConfigData cfg = RpgWorldConfigData.get(level);

        cfg.upsertRule(new MobXpRule(id, xp, predicate));

        CompoundTag finalPredicate = predicate;
        source.sendSuccess(
                () -> Component.translatable("rpg_core.mobxp.set",
                        id.toString(),
                        xp,
                        finalPredicate == null ? "" : finalPredicate.toString()),
                true
        );
        return 1;
    }

    private static int mobXpRemoveImpl(CommandSourceStack source, String entityIdStr, String nbtStr) {
        ResourceLocation id = ResourceLocation.tryParse(entityIdStr);
        if (id == null) {
            source.sendFailure(Component.translatable("rpg_core.mobxp.invalid_entity", entityIdStr));
            return 0;
        }

        CompoundTag predicate = null;
        if (nbtStr != null && !nbtStr.isBlank()) {
            try {
                predicate = TagParser.parseTag(nbtStr);
            } catch (Exception e) {
                source.sendFailure(Component.translatable("rpg_core.mobxp.invalid_nbt"));
                return 0;
            }
        }

        ServerLevel level = source.getLevel();
        RpgWorldConfigData cfg = RpgWorldConfigData.get(level);

        boolean ok = cfg.removeRule(id, predicate);
        if (!ok) {
            source.sendFailure(Component.translatable("rpg_core.mobxp.remove.not_found"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("rpg_core.mobxp.remove.ok"), true);
        return 1;
    }

    private static int mobXpList(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        Map<ResourceLocation, List<MobXpRule>> all = RpgWorldConfigData.get(level).snapshotAllRules();

        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("rpg_core.mobxp.list.empty"), false);
            return 1;
        }

        source.sendSuccess(() -> Component.translatable("rpg_core.mobxp.list.title"), false);

        all.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().toString()))
                .forEach(e -> {
                    for (MobXpRule r : e.getValue()) {
                        String pred = r.nbtPredicate() == null ? "" : r.nbtPredicate().toString();
                        source.sendSuccess(() -> Component.literal(" - " + r.entityId() + " xp=" + r.xp() + (pred.isBlank() ? "" : " nbt=" + pred)), false);
                    }
                });

        return 1;
    }

    /* =========================
       /rpg perks ...
       ========================= */
    private static ArgumentBuilder<CommandSourceStack, ?> perks() {
        return Commands.literal("perks")
                .then(Commands.literal("status").executes(ctx -> perksStatus(ctx.getSource())))
                .then(Commands.literal("list").executes(ctx -> perksList(ctx.getSource())))
                .then(Commands.literal("available").executes(ctx -> perksAvailable(ctx.getSource())))
                .then(Commands.literal("choose")
                        .then(Commands.argument("perk", ResourceLocationArgument.id())
                                .executes(ctx -> perksChoose(ctx.getSource(), ResourceLocationArgument.getId(ctx, "perk")))
                        )
                )
                .then(Commands.literal("reset")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> perksReset(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))
                        )
                );
    }

    private static int perksStatus(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return failOnlyPlayer(source);

        RpgProfile profile = RpgLevelingService.syncLevel(player);

        player.sendSystemMessage(Component.translatable("rpg_core.perks.status.title"));
        player.sendSystemMessage(Component.translatable(
                "rpg_core.perks.status.tokens",
                profile.totalPerkTokensGranted(),
                profile.perkTokensSpent(),
                profile.perkTokensAvailable()
        ));

        int nextTier = profile.perkTokensSpent() + 1;
        if (nextTier <= profile.totalPerkTokensGranted()) {
            player.sendSystemMessage(Component.translatable("rpg_core.perks.status.next_tier", nextTier));
        } else {
            player.sendSystemMessage(Component.translatable("rpg_core.perks.status.no_tiers"));
        }

        return 1;
    }

    private static int perksList(CommandSourceStack source) {
        if (!(source.
                getEntity() instanceof ServerPlayer player)) return failOnlyPlayer(source);

        RpgProfile profile = RpgProfileStorage.load(player);
        if (profile.chosenPerks().isEmpty()) {
            player.sendSystemMessage(Component.translatable("rpg_core.perks.list.empty"));
            return 1;
        }

        player.sendSystemMessage(Component.translatable("rpg_core.perks.list.title"));
        for (String perkId : profile.chosenPerks()) {
            player.sendSystemMessage(Component.literal(" - " + perkId));
        }
        return 1;
    }

    private static int perksAvailable(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return failOnlyPlayer(source);

        RpgProfile profile = RpgLevelingService.syncLevel(player);

        int nextTier = profile.perkTokensSpent() + 1;
        if (nextTier > profile.totalPerkTokensGranted()) {
            player.sendSystemMessage(Component.translatable("rpg_core.perks.available.none"));
            return 1;
        }

        // Core defaults (best-effort)
        List<ResourceLocation> defaults = RpgPerkOfferLogic.getOffersForTier(nextTier);

        // Allow addons to override/replace
        RpgPerkOffersEvent evt = new RpgPerkOffersEvent(player, nextTier, defaults);
        MinecraftForge.EVENT_BUS.post(evt);

        List<ResourceLocation> offers = List.copyOf(evt.offersView());
        if (offers.size() < 3) {
            player.sendSystemMessage(Component.translatable("rpg_core.perks.available.empty_for_tier", nextTier));
            return 1;
        }

        player.sendSystemMessage(Component.translatable("rpg_core.perks.available.title", nextTier));
        for (ResourceLocation id : offers) {
            player.sendSystemMessage(Component.literal(" - " + id));
        }
        return 1;
    }

    private static int perksChoose(CommandSourceStack source, ResourceLocation perkId) {
        if (!(source.getEntity() instanceof ServerPlayer player)) return failOnlyPlayer(source);

        RpgProfile profile = RpgLevelingService.syncLevel(player);
        int nextTier = profile.perkTokensSpent() + 1;

        if (nextTier > profile.totalPerkTokensGranted()) {
            player.sendSystemMessage(Component.translatable("rpg_core.perks.choose.no_tokens"));
            return 0;
        }

        if (profile.hasPerk(perkId.toString())) {
            player.sendSystemMessage(Component.translatable("rpg_core.perks.choose.already_have"));
            return 0;
        }

        var reg = RpgPerkRegistries.registry();
        if (reg.getValue(perkId) == null) {
            player.sendSystemMessage(Component.translatable("rpg_core.perks.choose.unknown", perkId.toString()));
            return 0;
        }

        // Core defaults (best-effort)
        List<ResourceLocation> defaults = RpgPerkOfferLogic.getOffersForTier(nextTier);

        // Allow addons to override/replace
        RpgPerkOffersEvent evt = new RpgPerkOffersEvent(player, nextTier, defaults);
        MinecraftForge.EVENT_BUS.post(evt);

        List<ResourceLocation> offers = List.copyOf(evt.offersView());
        if (!offers.contains(perkId)) {
            player.sendSystemMessage(Component.translatable("rpg_core.perks.choose.not_offered", nextTier));
            return 0;
        }

        // Apply
        profile.addPerk(perkId.toString());
        profile.setChosenPerkForTier(nextTier, perkId.toString());
        profile.spendPerkTokens(1);
        RpgProfileStorage.save(player, profile);

        player.sendSystemMessage(Component.translatable("rpg_core.perks.choose.ok", perkId.toString(), nextTier));
        return 1;
    }

    private static int perksReset(CommandSourceStack source, java.util.Collection<ServerPlayer> targets) {
        int count = 0;
        for (ServerPlayer p : targets) {
            RpgProfile profile = RpgProfileStorage.load(p);
            profile.clearPerks();
            profile.clearPerksByTier();
            profile.setPerkTokensSpent(0);
            RpgProfileStorage.save(p, profile);

            p.
                    sendSystemMessage(Component.translatable("rpg_core.perks.reset.done_admin_to_target"));
            count++;
        }
        final int finalCount = count;

        source.sendSuccess(
                () -> Component.translatable("rpg_core.perks.reset.done_admin_summary", finalCount),
                true
        );
        return finalCount;
    }

    /* =========================
       Classes: /rpg class ...
       ========================= */
    private static ArgumentBuilder<CommandSourceStack, ?> rpgClass() {
        return Commands.literal("class")
                .then(classGet())
                .then(classList())
                .then(classSet()); // OP-only
    }

    private static ArgumentBuilder<CommandSourceStack, ?> classGet() {
        return Commands.literal("get")
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    if (!(source.getEntity() instanceof ServerPlayer player)) return failOnlyPlayer(source);

                    RpgProfile p = RpgProfileStorage.load(player);
                    if (!p.hasClass()) {
                        player.sendSystemMessage(Component.translatable("rpg_core.class.get.none"));
                        return 1;
                    }
                    player.sendSystemMessage(Component.translatable("rpg_core.class.get.value", p.classId()));
                    return 1;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> classList() {
        return Commands.literal("list")
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    var reg = RpgClassRegistries.registry();

                    source.sendSuccess(() -> Component.translatable("rpg_core.class.list.title", reg.getKeys().size()), false);
                    for (ResourceLocation id : reg.getKeys()) {
                        var clazz = reg.getValue(id);
                        Component name = (clazz != null) ? clazz.displayName() : Component.literal("(null)");
                        source.sendSuccess(() -> Component.translatable("rpg_core.class.list.item", id.toString(), name), false);
                    }
                    return 1;
                });
    }

    private static ArgumentBuilder<CommandSourceStack, ?> classSet() {
        return Commands.literal("set")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("class_id", ResourceLocationArgument.id())
                                .executes(ctx -> {
                                    ResourceLocation classId = ResourceLocationArgument.getId(ctx, "class_id");
                                    var reg = RpgClassRegistries.registry();
                                    if (!reg.containsKey(classId)) {
                                        ctx.getSource().sendFailure(Component.translatable("rpg_core.class.set.not_registered", classId.toString()));
                                        return 0;
                                    }

                                    int count = 0;
                                    for (ServerPlayer target : EntityArgument.getPlayers(ctx, "targets")) {
                                        RpgProfile p = RpgProfileStorage.load(target);
                                        ResourceLocation oldId = p.classIdAsRL();

                                        p.setClassId(classId.toString());
                                        RpgProfileStorage.save(target, p);

                                        MinecraftForge.EVENT_BUS.post(new RpgClassChangedEvent(target, oldId, classId));
                                        target.sendSystemMessage(Component.translatable("rpg_core.class.set.done_to_target", classId.toString()));
                                        count++;
                                    }

                                    final int finalCount = count;
                                    ctx.getSource().sendSuccess(
                                            () -> Component.translatable("rpg_core.class.set.done_admin", classId.toString(), finalCount),
                                            true
                                    );
                                    return finalCount;
                                })
                        )
                );
    }

}