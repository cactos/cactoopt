package eu.cactosfp7.cactoopt.framework.functions.impl;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.functions.MappingEvaluationFunction;
import eu.cactosfp7.cactoopt.framework.functions.MigrationCostFunction;
import eu.cactosfp7.cactoopt.framework.functions.ObjectiveFunction;
import eu.cactosfp7.cactoopt.framework.impl.IterativePlacementOptimizer;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;
import eu.cactosfp7.cactoopt.framework.functions.impl.MigrationAwareObjectiveFunction;

/**
 * Calculates a score for a given {@link MigrationPath} based on the following
 * formula:
 *
 * <pre>
 * evaluationFunction(path.end) - evaluationFunction(path.start)
 * 		+ migrationCostFactor * migrationCostFunction(path.moves)
 * </pre>
 *
 * @see IterativePlacementOptimizer
 */
public class MigrationAwareObjectiveFunction implements ObjectiveFunction {
	/** Determines how good a certain {@link PlacementMapping} is. */
	private final MappingEvaluationFunction evaluationFunction;
	/**
	 * Determines the cost of a particular sequence of virtual machine
	 * migrations.
	 */
	private final MigrationCostFunction migrationCostFunction;
	/**
	 * A weight factor that can be used to increase/reduce the overall impact of
	 * the migration cost.
	 */
	private final double migrationCostFactor;

	/**
	 * Constructs a new {@link MigrationAwareObjectiveFunction}.
	 *
	 * @param evaluationFunction
	 *            Determines how good a certain {@link PlacementMapping} is.
	 * @param migrationCostFunction
	 *            Determines the cost of a particular sequence of virtual
	 *            machine migrations.
	 * @param migrationCostFactor
	 *            A weight factor that can be used to increase/reduce the
	 *            overall impact of the migration cost.
	 */
	public MigrationAwareObjectiveFunction(
			MappingEvaluationFunction evaluationFunction,
			MigrationCostFunction migrationCostFunction,
			double migrationCostFactor) {
		this.evaluationFunction = evaluationFunction;
		this.migrationCostFunction = migrationCostFunction;
		this.migrationCostFactor = migrationCostFactor;
	}

	@Override
	public double determineObjectiveValue(MigrationPath path) {
		return this.evaluationFunction.evaluateMapping(path.getResultMapping())
				- this.evaluationFunction.evaluateMapping(path
						.getInitialMapping())
				+ this.migrationCostFactor
				* this.migrationCostFunction.determineMigrationCost(path
						.getMigrationMoves());
	}

	/**
	 * @return the evaluationFunction
	 */
	public MappingEvaluationFunction getEvaluationFunction() {
		return this.evaluationFunction;
	}

	/**
	 * @return the migrationCostFunction
	 */
	public MigrationCostFunction getMigrationCostFunction() {
		return this.migrationCostFunction;
	}

	/**
	 * @return the migrationCostFactor
	 */
	public double getMigrationCostFactor() {
		return this.migrationCostFactor;
	}
}
