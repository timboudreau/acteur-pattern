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
