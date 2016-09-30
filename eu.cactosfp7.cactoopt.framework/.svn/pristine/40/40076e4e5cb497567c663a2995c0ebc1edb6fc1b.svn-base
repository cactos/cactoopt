package eu.cactosfp7.cactoopt.framework;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.PlacementOptimizer;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;

/**
 * A {@link PlacementOptimizer} tries to optimize virual machine placement over
 * a set of physical machines according to some criteria (such as
 * load-balancing, consolidation or energy-efficiency).
 */
public interface PlacementOptimizer {
	/**
	 * Instructs the {@link PlacementOptimizer} to calculate a better layout of
	 * virtual machines onto physical machines, given a current layout.
	 * <p/>
	 * The particular optimizations carried out by a {@link PlacementOptimizer}
	 * are dependent on the implementation and the optimization criteria
	 * according to which it operates.
	 *
	 * @param current
	 *            The current layout of virtual machines onto physical machines.
	 * @return A {@link PlacementMapping} with an optimized {@link PlacementMapping} together with a list of {@link MigrationMove}.
	 */
	public MigrationPath optimizePlacement(PlacementMapping current);
}
