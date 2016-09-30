package eu.cactosfp7.cactoopt.framework.functions;

import java.util.List;

import eu.cactosfp7.cactoopt.framework.functions.impl.MigrationSelectionStrategies;
import eu.cactosfp7.cactoopt.framework.impl.IterativePlacementOptimizer;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;
import eu.cactosfp7.cactoopt.framework.functions.MigrationPathSelectionStrategy;
import eu.cactosfp7.cactoopt.framework.functions.ObjectiveFunction;

/**
 * A {@link MigrationPathSelectionStrategy} is used by the
 * {@link IterativePlacementOptimizer} to select one {@link MigrationPath} from
 * a set of possible candidate {@link MigrationPath}s to continue along.
 *
 * @see IterativePlacementOptimizer
 * @see MigrationSelectionStrategies
 */
public interface MigrationPathSelectionStrategy {

	/**
	 * Given a set of candidate {@link MigrationPath}s, selects one
	 * {@link MigrationPath} that appears more promising to continue along than
	 * the others.
	 *
	 * @param candidates
	 *            A set of {@link MigrationPath} candidates to continue along.
	 * @param objectiveFunction
	 *            The {@link ObjectiveFunction} used to determine the benefit of
	 *            a {@link MigrationPath}, with regard to some criteria such as
	 *            load-balancing, consolidation or energy-efficiency.
	 * @return One candidate from the set of candidates.
	 */
	public MigrationPath selectCandidate(List<MigrationPath> candidates,
			ObjectiveFunction objectiveFunction);
}
