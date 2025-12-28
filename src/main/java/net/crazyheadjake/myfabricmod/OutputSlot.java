package net.crazyheadjake.myfabricmod;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class OutputSlot extends Slot {


	public OutputSlot(Container container, int i, int j, int k) {
		super(container, i, j, k);
	}

	public boolean mayPlace(ItemStack itemStack) {
		return false;
	}

	public boolean mayPickup(Player player) {
		return true;
	}

}
