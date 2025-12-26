package net.crazyheadjake.myfabricmod;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BeltMenu extends AbstractContainerMenu {
	private final Container belt;

	public BeltMenu(int i, Inventory inventory) {
		this(i, inventory, new SimpleContainer(BeltBlockEntity.BELT_CONTAINER_SIZE));
	}

	public BeltMenu(int i, Inventory inventory, Container container) {
		super(ModMenus.BELT_MENU, i);
		this.belt = container;
		checkContainerSize(container, BeltBlockEntity.BELT_CONTAINER_SIZE);
		container.startOpen(inventory.player);

		for (int j = 0; j < BeltBlockEntity.BELT_CONTAINER_SIZE; j++) {
			this.addSlot(new Slot(container, j, 44 + j * 18, 20));
		}

		this.addStandardInventorySlots(inventory, 8, 51);
	}

	@Override
	public boolean stillValid(Player player) {
		return this.belt.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int i) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(i);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack2 = slot.getItem();
			itemStack = itemStack2.copy();
			if (i < this.belt.getContainerSize()) {
				if (!this.moveItemStackTo(itemStack2, this.belt.getContainerSize(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 0, this.belt.getContainerSize(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.belt.stopOpen(player);
	}
}
