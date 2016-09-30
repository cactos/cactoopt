package eu.cactosfp7.cactoopt.framework.functions;

import java.util.List;

import eu.cactosfp7.cactoopt.framework.PlacementOptimizer;
import eu.cactosfp7.cactoopt.framework.functions.impl.MigrationAwareObjectiveFunction;
import eu.cactosfp7.cactoopt.framework.model.MigrationMove;

/**
 * Used by {@link MigrationAwareObjectiveFunction} as a cost function for a
 * particular sequence of virtual machine migrations.
 *
 * @see MigrationAwareObjectiveFunction
 */
public interface MigrationCostFunction {

	/**
	 * Calculates the cost of performing a certain sequence of virtual machine
	 * migrations.
	 * <p/>
	 * The function is used by a {@link MigrationAwareObjectiveFunction} to
	 * determine if a certain sequence of {@link MigrationMove}s are worth while
	 * (that is, if the benefit of the resulting machine mapping motivates the
	 * cost of actually performing the migrations). A high cost is less
	 * attractive from the {@link PlacementOptimizer}'s standpoint.
	 *
	 * @param migrationMoves
	 *            The {@link MigrationMove}s whose cost is to be determined.
	 * @return The cost of performing the migration moves.
	 */
	public double determineMigrationCost(List<MigrationMove> migrationMoves);

}
