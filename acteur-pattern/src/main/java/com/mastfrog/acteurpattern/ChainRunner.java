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

import com.mastfrog.guicy.scope.ReentrantScope;
import com.mastfrog.util.thread.QuietAutoCloseable;
import java.util.Iterator;

/**
 * Synchronously, recursively runs a list of Acteurs over some input
 *
 * @author Tim Boudreau
 */
public abstract class ChainRunner<T> {

    private final Chain chain;
    private final ReentrantScope scope;

    public ChainRunner(Chain chain, ReentrantScope scope) {
        this.chain = chain;
        this.scope = scope;
    }

    /**
     * Call this method, pasisng in some object that all of the acteurs in 
     * the chain should decorate or process somehow.
     * 
     * @param obj The object
     * @param moreContents Any other objects which should be able to be 
     * injected into Acteurs in the chain
     * @return A result indicating whether this chain processed the request to
     * completion, and whether or not other chains (if any) should be tried, or
     * if the final processing result is whatever this chain did
     */
    public RunResult onEvent(T obj, Object... moreContents) {
        // Enter the scope with our initial obbject
        try (QuietAutoCloseable cl = scope.enter(obj)) {
            // Get our iterator of dynamically created Acteurs
            Iterator<Acteur> iter = chain.iterator();
            // Invoke recursively
            return next(obj, iter, new RunResult(), moreContents);
        }
    }

    /**
     * Run the next Acteur - this method calls itself recursively
     * 
     * @param obj The object to decorate
     * @param iter An iterator which may contain subsequent acteurs or may be
     * empty
     * @param result The result
     * @param lastStateContext Any objects belonging to the state of the 
     * preceding run, which should be added to the injection context before
     * instantiating Acteurs
     * @return The result of recursively calling this method
     */
    private RunResult next(T obj, Iterator<Acteur> iter, RunResult result, Object... lastStateContext) {
        if (!iter.hasNext()) {  // Done with everything
            onDone(obj, result);
            return result;
        }
        // Enter the scope with any objects we were given to add to our
        // set of injectable stuff
        try (QuietAutoCloseable ac = scope.enter(lastStateContext)) {
            // This is the money shot - all the real work happens here:
            State state = iter.next().getState();
            // Build a new run result that signals whether any Acteur locked
            // the chain, and holds the done value from the state
            RunResult newResult = new RunResult(result, state);
            if (!state.isDone()) {
                // Call ourselves back recursively
                return next(obj, iter, newResult, state.context());
            } else {
                // We are finished, move along
                onDone(obj, newResult);
                return newResult;
            }
        }
    }
    /**
     * Override this method to do whatever happens when processing a request
     * is completed
     * 
     * @param obj The object to decorate or work on
     * @param res The result
     */
    protected abstract void onDone(T obj, RunResult res);
}
