package com.thrt.mtrdriver.mixin.client;

import org.mtr.core.data.NameColorDataBase;
import org.mtr.mod.screen.EditNameColorScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EditNameColorScreenBase.class)
public interface EditNameColorScreenAccessor {
	@Invoker(value = "saveData", remap = false)
	void mtd$saveData();

	@Accessor(value = "data", remap = false)
	NameColorDataBase mtd$getData();
}
