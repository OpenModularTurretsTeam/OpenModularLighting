package omtteam.openmodularlighting.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a game test method. Minecraft 1.21.11 removed vanilla's {@code @GameTest} (tests are registry entries since
 * 1.21.5); this annotation has its name and the attributes the tests use, so the test classes are written the same on
 * every version branch (the others import vanilla's annotation). {@link GameTestCases} turns the methods into
 * registry entries.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GameTest {
    /** Id of the test structure ({@link OMLGameTests#EMPTY_STRUCTURE}). */
    String template();

    /**
     * Tests of one batch run side by side; a test that must not see the others gets its own. Each batch is a test
     * environment, because the framework batches tests by environment.
     */
    String batch() default GameTestCases.DEFAULT_BATCH;

    /** Time limit in ticks; the test fails if it has not succeeded by then (the old annotation's default). */
    int timeoutTicks() default 100;
}
