package github.uncandango.leakdiagtool.tracker;

import java.lang.ref.WeakReference;

public class IdentityWeakReference<T> extends WeakReference<T> {
    private final int referentHash;

    public IdentityWeakReference(T referent) {
        super(referent);
        referentHash = referent.hashCode();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WeakReference<?> weak) {
            return this.refersTo((T) weak.get());
        }
        return this.refersTo((T) obj);
    }


    @Override
    public int hashCode() {
        return referentHash;
    }
}