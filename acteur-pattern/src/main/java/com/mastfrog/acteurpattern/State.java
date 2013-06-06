package com.mastfrog.acteurpattern;

/**
 * The result of computation within a single Acteur
 *
 * @author Tim Boudreau
 */
public class State {
    private final boolean locked;
    private final Object[] context;
    private final boolean done;

    /**
     * Construct a new state object
     * 
     * @param locked The creating acteur or an earlier one has marked this call
     * sequence as "locked" - meaning that if execuation fails, alternate paths
     * should not be attempted
     * @param done Further processing is not needed, work has completed.  Mutually
     * exclusive with providing additional state
     * @param context Objects to inject into the next acteur
     */
    public State(boolean locked, boolean done, Object... context) {
        this.locked = locked;
        this.done = done;
        if (done && context != null && context.length > 0) {
            throw new IllegalArgumentException("Providing objects to inject "
                    + "into the next Acteur when passing true for isDone is "
                    + "contradictory - if done, there is no next acteur");
        }
        this.context = context;
    }

    /**
     * This chain is locked - an Acteur in it has positively identified the
     * request/message as one this chain ought to be able to deal with, so don't
     * try others if this one fails
     * @return true if it is locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Processing of this event/message/request has been completed - do not
     * call any further acteurs in this or any other chain, we're done
     * @return 
     */
    public boolean isDone() {
        return done;
    }
    
    /**
     * Any context objects which should be available for injection into 
     * subsequent acteurs
     * @return An array of objects which may be empty but may not be null
     */
    Object[] context() {
        return context;
    }
}
