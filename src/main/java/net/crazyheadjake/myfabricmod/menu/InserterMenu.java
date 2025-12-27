package net.crazyheadjake.myfabricmod.menu;

import net.crazyheadjake.myfabricmod.ModMenus;
import net.crazyheadjake.myfabricmod.blockentity.BeltBlockEntity;
import net.crazyheadjake.myfabricmod.blockentity.InserterBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class InserterMenu extends AbstractContainerMenu {
	private final InserterBlockEntity inserter;
	private final Container container;


	public InserterMenu(int i, Inventory inventory) {
		this(i, inventory, (InserterBlockEntity) null);
	}

	public InserterMenu(int i, Inventory inventory, InserterBlockEntity blockEntity) {
		super(ModMenus.INSERTER_MENU, i);
		this.inserter = blockEntity;
		this.container = new SimpleContainer(BeltBlockEntity.BELT_CONTAINER_SIZE);
		this.container.startOpen(inventory.player);
		// int center_x = 80;
		// for (int j = 0; j < BeltBlockEntity.BELT_CONTAINER_SIZE; j++) {
		// 	this.addSlot(new Slot(container, j, center_x, 35));
		// }

		this.addStandardInventorySlots(inventory, 8, 84);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.container.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		return ItemStack.EMPTY;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.container.stopOpen(player);
	}
}
