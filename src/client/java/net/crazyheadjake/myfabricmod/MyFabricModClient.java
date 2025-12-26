package net.crazyheadjake.myfabricmod;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class MyFabricModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		MenuScreens.register(ModMenus.BELT_MENU, BeltScreen::new);
	}
}