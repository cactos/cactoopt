package eu.cactosfp7.cactoopt.framework.functions;

import eu.cactosfp7.cactoopt.framework.functions.impl.StopStrategies;
import eu.cactosfp7.cactoopt.framework.impl.IterativePlacementOptimizer;

/**
 * Used by the {@link IterativePlacementOptimizer} to determine when to give up
 * and stop iterating to produce more optimized placements.
 *
 * @see IterativePlacementOptimizer
 * @see StopStrategies
 */
public interface StopStrategy {
	/**
	 * Called by {@link IterativePlacementOptimizer} at the beginning of every
	 * new iteration to determine if a new optimization iteration should be
	 * carried out. The decision can be based either on the number of iterations
	 * that have been carried out or the time that has elapsed since the start
	 * of the first iteration.
	 *
	 * @param iterations
	 *            The number of completed optimization iterations.
	 * @param elapsedMilliseconds
	 *            The elapsed time (in milliseconds) since the start of the
	 *            first iteration.
	 * @return <code>true</code> if a new optimization iteration should be
	 *         started, <code>false</code> otherwise.
	 */
	public boolean shouldContinue(long iterations, long elapsedMilliseconds);
}
