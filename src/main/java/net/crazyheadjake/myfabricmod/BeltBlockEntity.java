package net.crazyheadjake.myfabricmod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BeltBlockEntity extends BaseContainerBlockEntity {
    public static final int BELT_CONTAINER_SIZE = 1;
    public static final int BELT_SPEED = 4;
    private NonNullList<ItemStack> items = NonNullList.withSize(BELT_CONTAINER_SIZE, ItemStack.EMPTY);
    private static final Component DEFAULT_NAME = Component.translatable("container.belt");


    public BeltBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELT_BLOCK_ENTITY, pos, state);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
		return new BeltMenu(i, inventory, this);
    }
}
