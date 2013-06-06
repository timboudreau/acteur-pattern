package com.mastfrog.acteurpattern;

import com.google.inject.Injector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/**
 * A collection of acteurs, dynamically created by injection on demand
 *
 * @author Tim Boudreau
 */
public final class Chain implements Iterable<Acteur> {

    private final Injector injector;
    List<Class<? extends Acteur>> acteurTypes = new LinkedList<>();

    @SuppressWarnings("unchecked")
    public Chain(Injector injector, Class<?>... acteurTypes) {
        for (Class<?> type : acteurTypes) {
            if (!Acteur.class.isAssignableFrom(type)) {
                throw new ClassCastException("Not a subtype of Acteur: " + type);
            }
            this.acteurTypes.add((Class<? extends Acteur>) type);
        }
        this.injector = injector;
    }

    @Override
    public Iterator<Acteur> iterator() {
        return new It(acteurTypes.iterator(), injector);
    }

    private static class It implements Iterator<Acteur> {
        private final Iterator<Class<? extends Acteur>> types;
        private final Injector injector;

        public It(Iterator<Class<? extends Acteur>> types, Injector injector) {
            this.types = types;
            this.injector = injector;
        }

        @Override
        public boolean hasNext() {
            return types.hasNext();
        }

        @Override
        public Acteur next() {
            return injector.getInstance(types.next());
        }
    }
}
