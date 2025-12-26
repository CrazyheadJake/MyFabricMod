package net.crazyheadjake.myfabricmod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ModMenus {
	public static final MenuType<BeltMenu> BELT_MENU = register("belt_1x1", BeltMenu::new);

	public static void initialize() {

	}

	private static <T extends AbstractContainerMenu> MenuType<T> register(String string, MenuType.MenuSupplier<T> menuSupplier) {
		return Registry.register(BuiltInRegistries.MENU, string, new MenuType<>(menuSupplier, FeatureFlags.VANILLA_SET));
	}

	private static <T extends AbstractContainerMenu> MenuType<T> register(String string, MenuType.MenuSupplier<T> menuSupplier, FeatureFlag... featureFlags) {
		return Registry.register(BuiltInRegistries.MENU, string, new MenuType<>(menuSupplier, FeatureFlags.REGISTRY.subset(featureFlags)));
	}
}
