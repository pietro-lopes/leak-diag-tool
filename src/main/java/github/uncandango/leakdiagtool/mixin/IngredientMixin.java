package github.uncandango.leakdiagtool.mixin;

import github.uncandango.leakdiagtool.tracker.ClassTracker;
import github.uncandango.leakdiagtool.tracker.ObjectTracker;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(Ingredient.class)
public class IngredientMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    void ldt$grabIngredientInstance(Stream<? extends Ingredient.Value> stream, CallbackInfo ci) {
        ObjectTracker.add(ClassTracker.LOADED, ObjectTracker.ValidClass.INGREDIENT, this);
    }
}
