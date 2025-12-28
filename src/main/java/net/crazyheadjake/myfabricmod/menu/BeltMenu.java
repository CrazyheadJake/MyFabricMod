package net.crazyheadjake.myfabricmod.menu;

import net.crazyheadjake.myfabricmod.ModMenus;
import net.crazyheadjake.myfabricmod.OutputSlot;
import net.crazyheadjake.myfabricmod.blockentity.BeltBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class BeltMenu extends AbstractContainerMenu {
	private final BeltBlockEntity belt;
	private final Container container;


	public BeltMenu(int i, Inventory inventory) {
		this(i, inventory, new SimpleContainer(BeltBlockEntity.BELT_CONTAINER_SIZE));
	}

	public BeltMenu(int i, Inventory inventory, Container container) {
		super(ModMenus.BELT_MENU, i);
		this.belt = null;
		this.container = container;
		this.container.startOpen(inventory.player);
		int center_x = 71;
		for (int j = 0; j < BeltBlockEntity.BELT_CONTAINER_SIZE; j++) {
			this.addSlot(new OutputSlot(this.container, j, j * 18 + center_x - 18, 35));
		}

		this.addStandardInventorySlots(inventory, 8, 84);
	}

	public BeltBlockEntity getBeltBlockEntity() {
		return this.belt;
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
