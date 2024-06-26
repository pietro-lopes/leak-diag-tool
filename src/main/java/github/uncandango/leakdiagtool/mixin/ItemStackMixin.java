package github.uncandango.leakdiagtool.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = {"<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/nbt/CompoundTag;)V", "<init>(Ljava/lang/Void;)V","<init>(Lnet/minecraft/nbt/CompoundTag;)V"}, at = @At("TAIL"))
    void ldt$grabItemStackInstance(CallbackInfo ci) {
//        ObjectTracker.add(ClassTracker.LOADED, ObjectTracker.ValidClass.ITEMSTACK, this);
    }
}
