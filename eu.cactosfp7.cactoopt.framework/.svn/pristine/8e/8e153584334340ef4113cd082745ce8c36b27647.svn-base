package eu.cactosfp7.cactoopt.framework.functions.impl;

import eu.cactosfp7.cactoopt.framework.functions.StopStrategy;

/**
 * Implementations of some typical {@link StopStrategy} variants.
 */
public class StopStrategies {

	/**
	 * A {@link StopStrategy} that requests that no new iterations are started
	 * after the number of given milliseconds have passed. By construction, this
	 * does not mean that a current iteration is cancelled once the limit has
	 * been exceeded, only that no new iteration may start after the limit has
	 * been exceeded.
	 *
	 * @param maxElapsedMilliseconds
	 *            The time limit, in milliseconds.
	 * @return A time-limited {@link StopStrategy}.
	 */
	public static StopStrategy timeLimited(final long maxElapsedMilliseconds) {
		return new StopStrategy() {
			@Override
			public boolean shouldContinue(long iterations,
					long elapsedMilliseconds) {
				return elapsedMilliseconds <= maxElapsedMilliseconds;
			}
		};
	}

	/**
	 * A {@link StopStrategy} that disallows exceeding a given number of
	 * iterations.
	 *
	 * @param maxIterations
	 *            The maximum number of iterations to run an algorithm for.
	 * @return An iterations-limited {@link StopStrategy}.
	 */
	public static StopStrategy iterationLimited(final long maxIterations) {
		return new StopStrategy() {
			@Override
			public boolean shouldContinue(long iterations,
					long elapsedMilliseconds) {
				return iterations < maxIterations;
			}
		};
	}

	/**
	 * A {@link StopStrategy} that never says that we need to abort, use with
	 * care, and only if the optimum value shall be computed.
	 * 
	 * @return A {@link StopStrategy} that always thinks it is a good idea to
	 *         continue.
	 */
	public static StopStrategy unlimited() {
		return new StopStrategy() {
			@Override
			public boolean shouldContinue(long iterations,
					long elapsedMilliseconds) {
				return true;
			}
		};
	}

}
