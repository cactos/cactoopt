package eu.cactosfp7.cactoopt.framework.functions;

import eu.cactosfp7.cactoopt.framework.PlacementOptimizer;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;

/**
 * Used by a {@link PlacementOptimizer} to determine how good a suggested
 * {@link MigrationPath} is, with regard to some criteria such as
 * load-balancing, consolidation or energy-efficiency.
 * <p/>
 * The idea is to assign low scores to good {@link MigrationPath}s. The lower
 * the better.
 *
 * @see PlacementOptimizer
 */
public interface ObjectiveFunction {

	/**
	 * Called by a {@link PlacementOptimizer} to calculate a score that
	 * indicates how good a suggested {@link MigrationPath} is, with regard to
	 * some criteria such as load-balancing, consolidation or energy-efficiency.
	 * <p/>
	 * The idea is to assign low scores to good {@link MigrationPath}s. The
	 * lower the better.
	 *
	 * @param migrationPath
	 *            A {@link MigrationPath} to be evaulated.
	 * @return The score of the {@link MigrationPath}.
	 */
	public double determineObjectiveValue(MigrationPath migrationPath);

}
