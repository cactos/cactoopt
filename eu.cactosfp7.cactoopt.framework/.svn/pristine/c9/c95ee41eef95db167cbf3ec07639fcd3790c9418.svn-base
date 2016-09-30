package eu.cactosfp7.cactoopt.framework.functions.impl;

import java.util.List;

import eu.cactosfp7.cactoopt.framework.functions.MigrationCostFunction;
import eu.cactosfp7.cactoopt.framework.model.MigrationMove;
import eu.cactosfp7.cactoopt.framework.functions.impl.MemoryPageMigrationCostFunction;

/**
 * A {@link MigrationCostFunction} that determines the cost for a particular
 * sequence of virtual machine migrations based on the amount memory that needs
 * to be transferred.
 * <p/>
 * The cost for a given {@link MigrationMove} is calculated as follows:
 *
 * <pre>
 * scalingFactor * vm.memory + staticOverheadCost
 * </pre>
 */
public class MemoryPageMigrationCostFunction implements MigrationCostFunction {
	/** A fixed overhead cost associated with a virtual machine migration. */
	private final double staticOverheadCost;
	private final double scalingFactor;

	/**
	 * Constructs a new {@link MemoryPageMigrationCostFunction}.
	 *
	 * @param staticOverheadCost
	 *            A fixed overhead cost associated with a virtual machine
	 *            migration.
	 * @param scalingFactor
	 *            A weight factor that can be used to increase/reduce the
	 *            overall impact of the migration cost.
	 */
	public MemoryPageMigrationCostFunction(double staticOverheadCost,
			double scalingFactor) {
		this.staticOverheadCost = staticOverheadCost;
		this.scalingFactor = scalingFactor;
	}

	@Override
	public double determineMigrationCost(List<MigrationMove> migrationMoves) {
		double cost = 0d;

		for (MigrationMove migrationMove : migrationMoves) {
			cost += this.scalingFactor
					* migrationMove.getVm().getRequiredMemory();
			cost += this.staticOverheadCost;
		}
		return cost;
	}

}
