package net.crazyheadjake.myfabricmod.blockentity;
import java.util.Iterator;

import org.jspecify.annotations.Nullable;

import net.crazyheadjake.myfabricmod.AutomationContainer;
import net.crazyheadjake.myfabricmod.ModBlockEntities;
import net.crazyheadjake.myfabricmod.block.InserterBlock;
import net.crazyheadjake.myfabricmod.menu.BeltMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BeltBlockEntity extends BlockEntity implements MenuProvider, AutomationContainer {
    public static final int BELT_CONTAINER_SIZE = 4;
    public static final int BELT_SPEED = 4;
    private int cooldownTime = -1;
    private NonNullList<ItemStack> items = NonNullList.create();

    private static final Component DEFAULT_NAME = Component.translatable("automation.belt");


    public BeltBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELT_BLOCK_ENTITY, pos, state);
    }

    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.items = NonNullList.create();
        for (int i = 0; i < BELT_CONTAINER_SIZE; i++) {
            this.items.add(ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(valueInput, this.items);

        this.cooldownTime = valueInput.getIntOr("TransferCooldown", -1);
   }

    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        ContainerHelper.saveAllItems(valueOutput, this.items);

        valueOutput.putInt("TransferCooldown", this.cooldownTime);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BeltMenu(i, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return DEFAULT_NAME;
    }

    private int getSize() {
        return this.items.size();
    }

    @Override
    public int insert(ItemStack itemStack) {
        int spaceAvailable = BELT_CONTAINER_SIZE - this.getSize();
        if (spaceAvailable <= 0) {
            return 0;
        }
        if (itemStack.getCount() < spaceAvailable) {
            spaceAvailable = itemStack.getCount();
        }
        for (int i = 0; i < spaceAvailable; i++) {
            this.items.add(itemStack.split(1));
        }
        return spaceAvailable;
    }

    @Override
    public int extract(ItemStack itemStack) {
        int itemsExtracted = 0;
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack stack = this.items.get(i);
            if (ItemStack.isSameItemSameComponents(stack, itemStack)) {
                itemsExtracted += stack.getCount();
                this.items.remove(i);
                i--;
            }
            if (itemsExtracted >= itemStack.getCount()) {
                break;
            }
        }
        return itemsExtracted;

    }

    @Override
    public int hasSpaceFor(ItemStack itemStack) {
        int spaceAvailable = BELT_CONTAINER_SIZE - this.getSize();
        return spaceAvailable;
    }

    @Override
    public int hasItems(ItemStack itemStack) {
        int itemsAvailable = 0;
        for (ItemStack stack : this.items) {
            if (ItemStack.isSameItemSameComponents(stack, itemStack)) {
                itemsAvailable += stack.getCount();
            }
        }
        return itemsAvailable;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty()) {
                this.items.remove(i);
                i--;
            }
        }
    }

    @Override
    public Iterator<ItemStack> getItemsIterator() {
        return this.items.iterator();
    }
    public static void pushItemsTick(Level level, BlockPos blockPos, BlockState blockState, BeltBlockEntity beltBlockEntity) {
        // Only running on server side 
        if (beltBlockEntity.cooldownTime > 0) {
            beltBlockEntity.cooldownTime--;
        } else {
            beltBlockEntity.cooldownTime = BELT_SPEED;
            BeltBlockEntity.tryPushItems(level, blockPos, blockState, beltBlockEntity);
        }
    }

    public static void tryPushItems(Level level, BlockPos blockPos, BlockState blockState,
            BeltBlockEntity beltBlockEntity) {
        // 1. Determine direction (Assuming the block has a FACING property)
        Direction facing = blockState.getValue(InserterBlock.FACING);
        Direction outputSide = facing;              // The side we push TO

        // 2. Get the Storage of the block IN FRONT of us (Target)
        // We ask for the storage available on the side FACING our inserter.
        // If either storage is missing (e.g., pointing at air or a rock), do nothing.
        BlockPos targetPos = blockPos.relative(outputSide);
        BlockState targetState = level.getBlockState(targetPos);
        AutomationContainer targetAutomation = InserterBlockEntity.getAutomationContainer(level, targetPos, targetState);
        if (targetAutomation == null) return;

        // 3. Move the items!
        if (beltBlockEntity.items.isEmpty()) return;
        ItemStack itemToMove = beltBlockEntity.items.get(0);
        if (itemToMove.isEmpty()) return;
        int space = targetAutomation.hasSpaceFor(itemToMove);
        if (space <= 0) return;
        targetAutomation.insert(itemToMove);
        beltBlockEntity.items.remove(0);
    }
}
