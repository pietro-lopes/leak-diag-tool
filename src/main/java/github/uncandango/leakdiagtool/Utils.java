package github.uncandango.leakdiagtool;

import org.jetbrains.annotations.Nullable;

public class Utils {

    public static boolean safeIsInstanceOf(Object o, String className) {
        try {
            var clazz = Class.forName(className);
            return clazz.isAssignableFrom(o.getClass());
        } catch (Exception ignored) {
        }
        return false;
    }

    @Nullable
    public static Class<?> safeGetClass(String className) {
        try {
            return Class.forName(className);
        } catch (Exception ignored) {
        }
        return null;
    }
}
