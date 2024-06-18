package github.uncandango.leakdiagtool.mixin.leaks;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;
import java.util.function.Predicate;

@Mixin(FakePlayerFactory.class)
public class FakePlayerFactoryMixin {
    @ModifyArg(method = "unloadLevel", at = @At(value = "INVOKE", target = "Ljava/util/Set;removeIf(Ljava/util/function/Predicate;)Z"))
    private static Predicate<Map.Entry<?,FakePlayer>> ldt$clearCriteriasFromFakePlayers(Predicate<Map.Entry<?,FakePlayer>> predicate, @Local(argsOnly = true) ServerLevel level){
        return entry -> {
            if (entry.getValue().level() == level) {
                entry.getValue().getAdvancements().stopListening();
                return true;
            } else return false;
        };
    }
//    @ModifyReturnValue(method = "lambda$unloadLevel$1", at = @At("RETURN"))
//    private static boolean ldt$clearCriteriasFromFakePlayers(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) Map.Entry<?, FakePlayer> entry){
//        if (original) {
//            BuiltInRegistries.TRIGGER_TYPES.holders().forEach(holder -> {
//                holder.value().removePlayerListeners(entry.getValue().getAdvancements());
//            });
//            return true;
//        }
//        return false;
//    }
}
