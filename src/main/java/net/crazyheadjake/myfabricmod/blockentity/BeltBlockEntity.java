package net.crazyheadjake.myfabricmod.blockentity;

import org.jspecify.annotations.Nullable;

import net.crazyheadjake.myfabricmod.ModBlockEntities;
import net.crazyheadjake.myfabricmod.menu.BeltMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BeltBlockEntity extends BlockEntity implements MenuProvider {
    public static final int BELT_CONTAINER_SIZE = 1;
    public static final int BELT_SPEED = 4;
    public static final int MAX_STACK_SIZE = 4;
    private int cooldownTime = -1;
    private NonNullList<ItemStack> items = NonNullList.withSize(BELT_CONTAINER_SIZE, ItemStack.EMPTY);
    private static final Component DEFAULT_NAME = Component.translatable("automation.belt");


    public BeltBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELT_BLOCK_ENTITY, pos, state);
    }

    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.items = NonNullList.withSize(BELT_CONTAINER_SIZE, ItemStack.EMPTY);
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
}
