package net.crazyheadjake.myfabricmod;

import java.util.Iterator;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AutomationBlockEntity extends BlockEntity implements WorldlyContainer {
    public AutomationBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        //TODO Auto-generated constructor stub
    }
    public abstract int insert(ItemStack itemStack);
    public abstract int extract(ItemStack itemStack);
    public abstract int hasSpaceFor(ItemStack itemStack);
    public abstract int hasItems(ItemStack itemStack);
    public abstract Iterator<ItemStack> getItemsIterator();

    public static final int[] NO_SLOTS = new int[0];

    
    @Override
    public boolean stillValid(Player player) {
        // Standard check to ensure player is close enough
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        // Return an empty array. 
        // Hoppers will see this and think "Oh, this block has no inventory."
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, Direction direction) {
        return false; // Double protection: Nothing goes in.
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack itemStack, Direction direction) {
        return false; // Double protection: Nothing comes out.
    }

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

    @Nullable
    public static AutomationBlockEntity getAutomationContainer(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.getBlockEntity(blockPos) instanceof AutomationBlockEntity automationContainer) {
            return automationContainer;
        }
        return null;
    }
}
