package com.fastasyncworldedit.bukkit.adapter;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;

import java.util.EnumMap;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class CachedBukkitAdapter implements IBukkitAdapter {

    private EnumMap<Material, Integer> itemTypes;
    private EnumMap<Material, Integer> blockTypes;

    private boolean init() {
        if (itemTypes == null) {
            Material[] materials = Material.values();
            itemTypes = new EnumMap<>(Material.class);
            blockTypes = new EnumMap<>(Material.class);
            for (Material material : materials) {
                if (material.isLegacy()) {
                    continue;
                }
                NamespacedKey key = material.getKey();
                String id = key.getNamespace() + ":" + key.getKey();
                if (material.isBlock()) {
                    blockTypes.put(material, BlockTypes.get(id).getInternalId());
                }
                if (material.isItem()) {
                    itemTypes.put(material, ItemTypes.get(id).getInternalId());
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Converts a Material to a ItemType.
     *
     * @param material The material
     * @return The itemtype
     */
    @Override
    public ItemType asItemType(Material material) {
        try {
            return ItemTypes.get(itemTypes.getOrDefault(material, Integer.MAX_VALUE));
        } catch (NullPointerException e) {
            if (init()) {
                return asItemType(material);
            }
            return ItemTypes.get(itemTypes.getOrDefault(material, Integer.MAX_VALUE));
        }
    }

    @Override
    public BlockType asBlockType(Material material) {
        try {
            return BlockTypesCache.values[blockTypes.get(material)];
        } catch (NullPointerException e) {
            if (init()) {
                return asBlockType(material);
            }
            return BlockTypes.AIR;
        }
    }

    /**
     * Create a WorldEdit BlockStateHolder from a Bukkit BlockData.
     *
     * @param blockData The Bukkit BlockData
     * @return The WorldEdit BlockState
     */
    @Override
    public BlockState adapt(BlockData blockData) {
        try {
            checkNotNull(blockData);
            Material material = blockData.getMaterial();
            BlockType type = BlockTypes.getFromStateId(blockTypes.get(material));
            List<? extends Property> propList = type.getProperties();
            if (propList.size() == 0) {
                return type.getDefaultState();
            }
            String properties = blockData.getAsString();
            return BlockState.get(type, properties, type.getDefaultState());
        } catch (NullPointerException e) {
            if (init()) {
                return adapt(blockData);
            }
            throw e;
        }
    }

    protected abstract int[] getIbdToOrdinal();

    protected abstract int[] getOrdinalToIbdID();

}
