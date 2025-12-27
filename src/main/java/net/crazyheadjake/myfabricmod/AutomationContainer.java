package net.crazyheadjake.myfabricmod;

import java.util.Iterator;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

public interface AutomationContainer {
    public int insert(ItemStack itemStack);
    public int extract(ItemStack itemStack);
    public int hasSpaceFor(ItemStack itemStack);
    public int hasItems(ItemStack itemStack);
    public Iterator<ItemStack> getItemsIterator();
    public void setChanged();
    public static int containerHasSpaceFor(Container target, ItemStack sourceStack, Direction inputSide, int max) {
        int[] is;
        WorldlyContainer facedContainer = null;
        if (target instanceof WorldlyContainer worldlyContainer) {
			is = worldlyContainer.getSlotsForFace(inputSide);
            facedContainer = worldlyContainer;
		} 
        else {
            // create a list with all slot indexes
            is = new int[target.getContainerSize()];
			for (int i = 0; i < is.length; i++) {
				is[i] = i;
			}
		}
        int amount = 0;
		for (int i : is) {
            if (facedContainer != null && !facedContainer.canPlaceItemThroughFace(i, sourceStack, inputSide)) {
                continue;
            }
			ItemStack itemStack = target.getItem(i);
            if (itemStack.isEmpty()) {
                amount += sourceStack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(sourceStack, itemStack)) {
                amount += itemStack.getMaxStackSize() - itemStack.getCount();
            }
            if (amount >= max) {
                break;
            }
		}

		return amount;
    }
}
