package net.crazyheadjake.myfabricmod.blockentity;

import org.jspecify.annotations.Nullable;

import net.crazyheadjake.myfabricmod.ModBlockEntities;
import net.crazyheadjake.myfabricmod.menu.InserterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class InserterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INSERTER_SPEED = 4;
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
}
