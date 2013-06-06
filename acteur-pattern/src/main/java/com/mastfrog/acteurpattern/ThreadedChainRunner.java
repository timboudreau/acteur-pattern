package com.mastfrog.acteurpattern;

import com.mastfrog.guicy.scope.ReentrantScope;
import com.mastfrog.util.thread.QuietAutoCloseable;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * Parallel executor of Acteurs.  Does the same thing as ChainRunner, but 
 * uses a thread pool, and each Acteur is invoked as a separate Callable
 * dispatched to the thread pool.
 *
 * @author Tim Boudreau
 */
public final class ThreadedChainRunner<T> {

    private final Chain chain;
    private final ReentrantScope scope;
    private final ExecutorService svc;

    /**
     * Create a new runner
     * @param chain The chain it runs on
     * @param scope The scope to use for injection
     * @param svc A thread pool
     */
    public ThreadedChainRunner(Chain chain, ReentrantScope scope, ExecutorService svc) {
        this.chain = chain;
        this.scope = scope;
        this.svc = scope.wrapThreadPool(svc);
    }

    /**
     * Call this method to dispatch something to the chain.  This method will
     * dispatch the first acteur on a background thread;  it returns immediately.
     * 
     * @param obj The object to decorate or process
     * @param finisher Equivalent of overriding onDone() in ChainRunner - a callback
     * which is called on completion
     * @param moreContents Any additional objects to include in the scope
     * @return A CountDownLatch which tests or similar could wait on before
     * checking results
     */
    public CountDownLatch onEvent(T obj, Finisher<T> finisher, Object... moreContents) {
        // Enter the scope with our initial obbject
        try (QuietAutoCloseable cl = scope.enter(obj)) {
            // Get our iterator of dynamically created Acteurs
            Iterator<Acteur> iter = chain.iterator();
            // Invoke recursively
            CountDownLatch latch = new CountDownLatch(1);;
            next(obj, iter, new RunResult(), latch, finisher, moreContents);
            return latch;
        }
    }

    private void next(T obj, Iterator<Acteur> iter, RunResult result, CountDownLatch latch, Finisher<T> finisher, Object... lastStateContext) {
        if (!iter.hasNext() || result.wasDone()) {
            finisher.onDone(obj, result);
        }
        // The wrapped thread pool will freeze the current scope contents, and reconstitute it
        // before invoking our next Acteur
        svc.submit(new OneActeurCallable (obj, finisher, iter, result, latch, lastStateContext));
    }

    private class OneActeurCallable implements Callable<Void> {

        private final T obj;
        private final Finisher<T> finisher;
        private final Iterator<Acteur> iter;
        private final RunResult result;
        private final CountDownLatch latch;
        private final Object[] lastStateContext;

        OneActeurCallable(T obj, Finisher<T> finisher, Iterator<Acteur> iter, RunResult result, CountDownLatch latch, Object... lastStateContext) {
            assert iter.hasNext();
            this.obj = obj;
            this.finisher = finisher;
            this.iter = iter;
            this.result = result;
            this.latch = latch;
            this.lastStateContext = lastStateContext;
        }

        @Override
        public Void call() throws Exception {
            assert scope.inScope();
            try (QuietAutoCloseable ac = scope.enter(lastStateContext)) {
                Acteur acteur = iter.next();
                State state = acteur.getState();
                RunResult newResult = new RunResult(result, state);
                if (!state.isDone()) {
                    // Call ourselves back recursively
                    next(obj, iter, newResult, latch, finisher, state.context());
                } else {
                    // We are finished, move along
                    finisher.onDone(obj, newResult);
                    latch.countDown();;
                }
            }
            return null;
        }
    }

    /**
     * Equivalent of overriding <code>ChainRunner.onDone()</code> - a callback
     * called on completion of processing
     * @param <T> The object type
     */
    public interface Finisher<T> {
        /**
         * Called when processing is completed
         * @param obj The object to process or decorate
         * @param res Whether or not processing succeeded and whether or not it 
         * fell through to the end or an acteur stopped it by setting the done
         * flag on its state
         */
        void onDone(T obj, RunResult res);
    }
}
