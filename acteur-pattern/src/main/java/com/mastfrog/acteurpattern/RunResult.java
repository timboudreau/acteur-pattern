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
 *
 * @author Tim Boudreau
 */
public final class RunResult<T> {
    private final boolean wasLocked;
    private final boolean wasDone;

    public RunResult() {
        this(false, false);
    }

    public RunResult(RunResult prev, State state) {
        this(prev.wasLocked() || state.isLocked(), state.isDone());
        assert !prev.wasDone();
    }

    public RunResult(boolean wasLocked, boolean wasDone) {
        this.wasLocked = wasLocked;
        this.wasDone = wasDone;
    }

    public boolean wasLocked() {
        return wasLocked;
    }

    public boolean wasDone() {
        return wasDone;
    }
}
