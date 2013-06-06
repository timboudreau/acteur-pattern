/* 
 * The MIT License
 *
 * Copyright 2013 Tim Boudreau.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
