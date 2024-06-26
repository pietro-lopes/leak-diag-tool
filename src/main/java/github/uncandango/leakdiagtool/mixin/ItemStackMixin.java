package github.uncandango.leakdiagtool.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = {"<init>(Ljava/lang/Void;)V","<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V"}, at = @At("TAIL"))
    void ldt$grabItemStackInstance(CallbackInfo ci) {
//        ObjectTracker.add(ClassTracker.LOADED, ObjectTracker.ValidClass.ITEMSTACK, this);
    }
}
