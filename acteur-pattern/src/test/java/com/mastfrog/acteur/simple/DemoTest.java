package com.mastfrog.acteur.simple;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mastfrog.acteurpattern.Acteur;
import com.mastfrog.acteurpattern.Chain;
import com.mastfrog.acteurpattern.ChainRunner;
import com.mastfrog.acteurpattern.RunResult;
import com.mastfrog.acteurpattern.State;
import com.mastfrog.acteurpattern.ThreadedChainRunner;
import com.mastfrog.acteurpattern.ThreadedChainRunner.Finisher;
import com.mastfrog.guicy.scope.ReentrantScope;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class DemoTest {

    @Test
    public void test() {
        Injector inj = Guice.createInjector(new DemoModule());
        ReentrantScope scope = inj.getInstance(ReentrantScope.class);
        Chain chain = new Chain(inj, TemplateFinder.class, SubstituteName.class, SubstituteNumber.class);

        ChainRunner<StringBuilder> runner = new ChainRunner<StringBuilder>(chain, scope) {

            @Override
            protected void onDone(StringBuilder obj, RunResult res) {
                System.out.println("DONE!!!");
                System.out.println(obj);
                assertTrue(res.wasDone());
                assertTrue(res.wasLocked());
            }
        };

        UserPojo user = new UserPojo("Joe");
        StringBuilder writeInto = new StringBuilder();

        runner.onEvent(writeInto, "ONE", user, 23);
        writeInto = new StringBuilder();
        runner.onEvent(writeInto, "TWO", user, 23);
        writeInto = new StringBuilder();
        runner.onEvent(writeInto, "THREE", user, 23);
    }

    @Test
    public void threadedTest() throws InterruptedException {
        Injector inj = Guice.createInjector(new DemoModule());
        ReentrantScope scope = inj.getInstance(ReentrantScope.class);
        Chain chain = new Chain(inj, TemplateFinder.class, SubstituteName.class, SubstituteNumber.class);

        Finisher finisher = new Finisher<StringBuilder>() {

            @Override
            public void onDone(StringBuilder obj, RunResult res) {
                System.out.println("DONE!!!");
                System.out.println(obj);
                assertTrue(res.wasDone());
                assertTrue(res.wasLocked());
            }
        };

        System.out.println("Begin threaded test");

        UserPojo user = new UserPojo("Marvin");
        StringBuilder writeInto = new StringBuilder();
        ExecutorService svc = Executors.newCachedThreadPool();
        ThreadedChainRunner<StringBuilder> runner = new ThreadedChainRunner<>(chain, scope, svc);

        // Note the order in which onDone() is called for these is not
        // deterministic - they run in parallel
        CountDownLatch done1 = runner.onEvent(writeInto, finisher, "ONE", user, 23);
        writeInto = new StringBuilder();
        CountDownLatch done2 = runner.onEvent(writeInto, finisher, "TWO", user, 23);
        writeInto = new StringBuilder();
        CountDownLatch done3 = runner.onEvent(writeInto, finisher, "THREE", user, 23);

        done1.await();
        done2.await();
        done3.await();

    }

    private static class DemoModule extends AbstractModule {

        @Override
        protected void configure() {
            ReentrantScope scope = new ReentrantScope();
            bind(ReentrantScope.class).toInstance(scope);
            scope.bindTypes(binder(), UserPojo.class, String.class,
                    StringBuilder.class, Templates.class, Integer.class);
        }

    }

    static final class UserPojo {

        private final String name;

        public UserPojo(String name) {
            this.name = name;
        }
    }

    public enum Templates {

        ONE("Hello, {{name}}!\nYou have won {{number}} puppies"),
        TWO("Why, {{name}}!\nWhy wouldn't you want {{number}} puppies?!"),
        THREE("So, {{name}}!\nMaybe you'd like {{number}} kittens?");
        private final String text;

        Templates(String text) {
            this.text = text;
        }

        public String getTemplate() {
            return text;
        }
    }

    public static class TemplateFinder extends Acteur {

        @Inject
        public TemplateFinder(String requestedTemplate) {
            setState(new State(false, false, Templates.valueOf(requestedTemplate)));
        }
    }

    public static class SubstituteName extends Acteur {

        @Inject
        SubstituteName(UserPojo user, Templates template, StringBuilder text) {
            text.append(template.getTemplate());
            Pattern p = Pattern.compile("\\{\\{name\\}\\}", Pattern.MULTILINE);
            String substituted = p.matcher(text).replaceAll(user.name);
            text.setLength(0);
            text.append(substituted);
            setState(new State(true, false, text));
        }
    }

    public static class SubstituteNumber extends Acteur {

        @Inject
        SubstituteNumber(Integer number, StringBuilder text) {
            Pattern p = Pattern.compile("\\{\\{number\\}\\}", Pattern.MULTILINE);
            String substituted = p.matcher(text).replaceAll(number.toString());
            text.setLength(0);
            text.append(substituted);
            setState(new State(true, true));
        }
    }
}
