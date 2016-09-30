package eu.cactosfp7.cactoopt.framework.functions;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.functions.impl.MigrationAwareObjectiveFunction;

/**
 * Used by {@link MigrationAwareObjectiveFunction} to evaluate how good a
 * certain {@link PlacementMapping} is with regard to some criteria such as
 * load-balancing, consolidation or energy-efficiency.
 *
 * @see MigrationAwareObjectiveFunction
 */
public interface MappingEvaluationFunction {

	/**
	 * Calculates an evaluation score that indicates how good a suggested
	 * {@link PlacementMapping} is, with regard to some criteria such as
	 * load-balancing, consolidation or energy-efficiency.
	 * <p/>
	 * The evaluation function should be regarded as a cost function. The idea
	 * is to assign low scores to good {@link PlacementMapping}s. The lower the
	 * better.
	 *
	 * @param mapping
	 *            A {@link PlacementMapping} to be evaluated.
	 * @return The evaluation
	 */
	public double evaluateMapping(PlacementMapping mapping);
}
