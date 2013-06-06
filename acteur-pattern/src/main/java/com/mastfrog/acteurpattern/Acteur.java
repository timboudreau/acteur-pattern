package com.mastfrog.acteurpattern;

import com.mastfrog.util.Checks;

/**
 * An Actor which does its work either in its constructor, culminating in a call
 * to setState(), or in its overridden getState() method.
 *
 * @author Tim Boudreau
 */
public abstract class Acteur {
    private State state;

    /**
     * Call this method from the constructor
     * @param state The state
     */
    protected final void setState(State state) {
        Checks.notNull("state", state);
        this.state = state;
    }

    /**
     * Override this method if you don't implement a constructur
     * @return The state
     */
    public State getState() {
        if (state == null) {
            throw new IllegalStateException("State not set in constructor and "
                    + "getState() not overridden");
        }
        return state;
    }
}
