package github.uncandango.leakdiagtool.tracker;

import github.uncandango.leakdiagtool.LeakDiagTool;
import net.minecraft.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ClassTracker {
    LOADED,
    LEAKING;

    private static long lastTimeRanGC = 0;
    private final Map<Class<?>, List<ObjectTracker>> map;

    ClassTracker() {
        this.map = new ConcurrentHashMap<>();
    }

    public void add(Class<?> clazz, ObjectTracker object) {
        map.computeIfPresent(clazz, (key, value) -> {
            value.add(object);
            return value;
        });
        map.computeIfAbsent(clazz, (key) -> {
            List<ObjectTracker> newList = new ArrayList<>();
            newList.add(object);
            return newList;
        });
    }

    public int size(Class<?> clazz) {
        return size(clazz, false);
    }

    public int size(Class<?> clazz, boolean cleanUp) {
        if (cleanUp && (Util.getMillis() - lastTimeRanGC) > 5000) {
            System.gc();
            lastTimeRanGC = Util.getMillis();
            LeakDiagTool.LOGGER.debug("Running GC...");
        }
        var set = map.get(clazz);
        if (set == null) return 0;
        set.removeIf(ObjectTracker::isEmpty);
        return set.size();
    }

    public synchronized void transferTo(ClassTracker tracker, ObjectTracker.ValidClass type) {
        cleanNulls();
        this.map.forEach((key, value) -> {
            if (!type.getClasses().contains(key)) return;
            for (ObjectTracker objTracker : value) {
                var objGen = objTracker.getGeneration();
                var trackerGen = type.getCurrentGeneration();
                if (!objTracker.isEmpty() &&
                        objGen.getNumber() != trackerGen.getNumber() &&
                     objGen.getEvent() != Generation.EvolutionEvent.INIT
                ) {
                    if (type == ObjectTracker.ValidClass.INGREDIENT){
                        if (objGen.getNumber() >= 2) tracker.add(type, objTracker);
                    } else tracker.add(type, objTracker);
                }
            }
        });
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        map.keySet().forEach(clazz -> {
            sb.append("Class: ").append(clazz.getSimpleName()).append("\n");
            sb.append("Objects: ").append(this.size(clazz, true)).append("\n");
        });
        return sb.toString();
    }

    public Map<Class<?>, Integer> getSummary() {
        var result = new HashMap<Class<?>, Integer>();
        map.forEach((key, value) -> result.put(key, this.size(key, true)));
        return result;
    }

    public void add(ObjectTracker.ValidClass type, Object object) {
        type.getClasses().stream()
                .filter(clazz -> {
                    if (object instanceof ObjectTracker wrapped){
                        return clazz.isAssignableFrom(wrapped.unwrap().getClass());
                    }
                    return clazz.isAssignableFrom(object.getClass());
                })
                .forEach(clazz -> {
                    if (object instanceof ObjectTracker wrapped){
                        add(clazz, wrapped);
                        return;
                    }
                    add(clazz, ObjectTracker.of(object, type));
                });
    }

    public void cleanNulls(){
        System.gc();
        map.values().forEach(list -> {
            list.removeIf(ObjectTracker::isEmpty);
            ((ArrayList<?>) list).trimToSize();
        });
    }

}