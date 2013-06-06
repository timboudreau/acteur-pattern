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
