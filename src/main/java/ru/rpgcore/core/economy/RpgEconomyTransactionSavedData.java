package ru.rpgcore.core.economy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * World-level persistent storage for economy transactions.
 */
public class RpgEconomyTransactionSavedData extends SavedData {

    public static final String DATA_NAME = "rpg_core_economy_transactions";

    private static final String KEY_TRANSACTIONS = "transactions";

    private final List<RpgEconomyTransaction> transactions = new ArrayList<>();

    public RpgEconomyTransactionSavedData() {
    }

    public static RpgEconomyTransactionSavedData load(CompoundTag tag) {
        RpgEconomyTransactionSavedData data = new RpgEconomyTransactionSavedData();

        if (tag.contains(KEY_TRANSACTIONS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(KEY_TRANSACTIONS, Tag.TAG_COMPOUND);

            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                data.transactions.add(RpgEconomyTransaction.load(entry));
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();

        for (RpgEconomyTransaction tx : transactions) {
            list.add(tx.save());
        }

        tag.put(KEY_TRANSACTIONS, list);
        return tag;
    }

    public List<RpgEconomyTransaction> transactions() {
        return transactions;
    }

    public void add(RpgEconomyTransaction transaction) {
        transactions.add(transaction);
        setDirty();
    }

    public void removeFirst() {
        if (!transactions.isEmpty()) {
            transactions.remove(0);
            setDirty();
        }
    }

    public void clearAll() {
        transactions.clear();
        setDirty();
    }

    public int size() {
        return transactions.size();
    }
}