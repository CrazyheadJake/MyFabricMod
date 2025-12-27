package net.crazyheadjake.myfabricmod.blockentity;

import java.util.Iterator;

import org.jspecify.annotations.Nullable;

import net.crazyheadjake.myfabricmod.AutomationContainer;
import net.crazyheadjake.myfabricmod.ModBlockEntities;
import net.crazyheadjake.myfabricmod.block.InserterBlock;
import net.crazyheadjake.myfabricmod.menu.InserterMenu;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class InserterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INSERTER_SPEED = 8;
    private int cooldownTime = -1;
    private static final Component DEFAULT_NAME = Component.translatable("automation.inserter");


    public InserterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INSERTER_BLOCK_ENTITY, pos, state);
    }

    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);

        this.cooldownTime = valueInput.getIntOr("TransferCooldown", -1);
   }

    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);

        valueOutput.putInt("TransferCooldown", this.cooldownTime);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new InserterMenu(i, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return DEFAULT_NAME;
    }

    public static void pushItemsTick(Level level, BlockPos blockPos, BlockState blockState, InserterBlockEntity inserterBlockEntity) {
        // Only running on server side 
        if (inserterBlockEntity.cooldownTime > 0) {
            inserterBlockEntity.cooldownTime--;
        } else {
            InserterBlockEntity.tryPushItems(level, blockPos, blockState, inserterBlockEntity);
        }
    }

    private static void tryPushItems(Level level, BlockPos blockPos, BlockState blockState,
            InserterBlockEntity inserterBlockEntity) {
        // 1. Determine direction (Assuming the block has a FACING property)
        Direction facing = blockState.getValue(InserterBlock.FACING);
        Direction inputSide = facing.getOpposite(); // The side we pull FROM
        Direction outputSide = facing;              // The side we push TO

        // 2. Get the Storage of the block BEHIND us (Source)
        // We ask for the storage available on the side FACING our inserter.
        BlockPos sourcePos = blockPos.relative(inputSide);
        BlockState sourceState = level.getBlockState(sourcePos);
        AutomationContainer sourceAutomation = getAutomationContainer(level, sourcePos, sourceState);
        Container source = getBlockContainer(level, sourcePos, sourceState);
        if (source == null && sourceAutomation == null) return;

        Iterator<ItemStack> sourceItemsIterator = null;
        if (sourceAutomation == null) {
            sourceItemsIterator = source.iterator();
        }
        else
            sourceItemsIterator = sourceAutomation.getItemsIterator();

        // 3. Get the Storage of the block IN FRONT of us (Target)
        // We ask for the storage available on the side FACING our inserter.
        // If either storage is missing (e.g., pointing at air or a rock), do nothing.
        BlockPos targetPos = blockPos.relative(outputSide);
        BlockState targetState = level.getBlockState(targetPos);
        Container target = getBlockContainer(level, targetPos, targetState);
        AutomationContainer targetAutomation = getAutomationContainer(level, targetPos, targetState);
        if (target == null && targetAutomation == null) return;

        // 4. Move the items!
        while (sourceItemsIterator.hasNext()) {
            ItemStack sourceStack = sourceItemsIterator.next();
            if (sourceStack.isEmpty()) continue;
            int space;
            if (targetAutomation != null) {
                space = targetAutomation.hasSpaceFor(sourceStack);
            } else {
                space = AutomationContainer.containerHasSpaceFor(target, sourceStack, inputSide, 1);
            }
            if (space <= 0) continue;

            if (targetAutomation != null) {
                targetAutomation.insert(sourceStack.split(1));
                inserterBlockEntity.cooldownTime = INSERTER_SPEED;

                break;
            } else {
                ItemStack remaining = HopperBlockEntity.addItem(
                null,           // Source container (usually null is fine here for simple insertions)
                        target, // The destination Container
                        sourceStack.split(1),    // The stack you want to insert
                        inputSide    // The side you are inserting into (can be null)
                );
                if (source != null)
                    source.setChanged();
                else {
                    sourceAutomation.setChanged();
                }
                inserterBlockEntity.cooldownTime = INSERTER_SPEED;

                break;
            }

        }

    }

    @Nullable
	public static Container getBlockContainer(Level level, BlockPos blockPos, BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof WorldlyContainerHolder) {
			return ((WorldlyContainerHolder)block).getContainer(blockState, level, blockPos);
		} 
        else if (blockState.hasBlockEntity() && level.getBlockEntity(blockPos) instanceof Container container) {
			if (container instanceof ChestBlockEntity && block instanceof ChestBlock) {
				container = ChestBlock.getContainer((ChestBlock)block, blockState, level, blockPos, true);
			}
			return container;
		} 
		return null;
	}

    @Nullable
    public static AutomationContainer getAutomationContainer(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.getBlockEntity(blockPos) instanceof AutomationContainer automationContainer) {
            return automationContainer;
        }
        return null;
    }
}
