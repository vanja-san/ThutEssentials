package thut.essentials.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class ItemList extends Items
{
    public static boolean is(final ResourceLocation tag, final Object toCheck)
    {
        if (toCheck instanceof Item)
        {
            final Item item = (Item) toCheck;
            final boolean tagged = ItemTags.getCollection().getOrCreate(tag).contains(item);
            if (!tagged) return item.getRegistryName().equals(tag);
            return tagged;
        }
        else if (toCheck instanceof ItemStack) return ItemList.is(tag, ((ItemStack) toCheck).getItem());
        else if (toCheck instanceof Block)
        {

            final Block block = (Block) toCheck;
            final boolean tagged = BlockTags.getCollection().getOrCreate(tag).contains(block);
            if (!tagged) return block.getRegistryName().equals(tag);
            return tagged;
        }
        else if (toCheck instanceof BlockState) return ItemList.is(tag, ((BlockState) toCheck).getBlock());
        return false;
    }
}