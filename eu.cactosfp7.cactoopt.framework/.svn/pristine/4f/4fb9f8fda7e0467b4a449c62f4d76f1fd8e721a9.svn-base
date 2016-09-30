package eu.cactosfp7.cactoopt.framework.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.PlacementOptimizer;
import eu.cactosfp7.cactoopt.framework.functions.MigrationPathSelectionStrategy;
import eu.cactosfp7.cactoopt.framework.functions.ObjectiveFunction;
import eu.cactosfp7.cactoopt.framework.functions.StopStrategy;
import eu.cactosfp7.cactoopt.framework.migrationstrategies.DoubleMigrationMoveStrategy;
import eu.cactosfp7.cactoopt.framework.migrationstrategies.SingleMigrationMoveStrategy;
import eu.cactosfp7.cactoopt.framework.model.MigrationMove;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;
import eu.cactosfp7.cactoopt.framework.util.PlacementMappingUtils;
import eu.cactosfp7.cactoopt.framework.impl.IterativePlacementOptimizer;

/**
 * A {@link PlacementOptimizer} that performs several iterative passes, in each
 * trying to find a better {@link PlacementMapping}. It does this by, for each
 * iteration, first finding the best mapping resulting form a single-move
 * migration. If no single-move migration improves the {@link PlacementMapping},
 * it assumes it can be stuck in a local minima, and therefore increases the
 * search space to include double-move migrations. If an improved
 * {@link PlacementMapping} is found then, it applies the migration and
 * continues with single-move migrations as before. If however no double-move
 * migrations improve the {@link PlacementMapping}, it assumes that the local
 * minima is a good enough approximation of the global minimum, and exits.
 *
 * <p>
 * All important aspects of the algorithm are abstracted away by interfaces, and
 * it is therefore possible to completely change how the optimizer behaves by
 * supplying different implementations of e.g. the {@link ObjectiveFunction}.
 *
 * <p>
 * This class is immutable.
 */
public class IterativePlacementOptimizer implements PlacementOptimizer {

	private static final Logger log = LoggerFactory
			.getLogger(IterativePlacementOptimizer.class);

	private final SingleMigrationMoveStrategy singleMigrationMoveStrategy;
	private final ImmutableList<DoubleMigrationMoveStrategy> doubleMigrationMoveStrategies;
	private final MigrationPathSelectionStrategy migrationPathSelectionStrategy;
	private final ObjectiveFunction objectiveFunction;
	private final StopStrategy stopStrategy;

	/**
	 * Creates a new instance.
	 *
	 * @param objectiveFunction
	 *            The objective function to use.
	 * @param migrationPathSelectionStrategy
	 *            The migration path selection strategy to use.
	 * @param singleMigrationMoveStrategy
	 *            The single-move migration generation strategy to use.
	 * @param doubleMigrationMoveStrategies
	 *            The double-move migration generation strategies to use.
	 * @param stopStrategy
	 *            The stopping strategy to use, used to optionally limit how
	 *            many iterations or how long time the calculation is allowed to
	 *            continue running. At the start of each new iteration, we see
	 *            if we are allowed to continue with another one. We do not
	 *            terminate the iteration prematurely if a time limit is
	 *            exceeded, but rather note that we are not allowed to continue
	 *            once it has been.
	 */
	public IterativePlacementOptimizer(ObjectiveFunction objectiveFunction,
			MigrationPathSelectionStrategy migrationPathSelectionStrategy,
			SingleMigrationMoveStrategy singleMigrationMoveStrategy,
			List<DoubleMigrationMoveStrategy> doubleMigrationMoveStrategies,
			StopStrategy stopStrategy) {
		Preconditions.checkArgument(!doubleMigrationMoveStrategies.isEmpty(),
				"Must specify at least one double migration move strategy!");

		this.objectiveFunction = objectiveFunction;
		this.migrationPathSelectionStrategy = migrationPathSelectionStrategy;
		this.singleMigrationMoveStrategy = singleMigrationMoveStrategy;
		this.doubleMigrationMoveStrategies = ImmutableList
				.copyOf(doubleMigrationMoveStrategies);
		this.stopStrategy = stopStrategy;
	}

	@Override
	public MigrationPath optimizePlacement(PlacementMapping current) {
		Preconditions.checkArgument(!current.getPhysicalMachines().isEmpty(),
				"No physical machines in current mapping");

		List<MigrationMove> allMigrationMoves = new LinkedList<>();

		PlacementMapping bestMapping = current.copy();

		long iterations = 0;
		Stopwatch stopwatch = Stopwatch.createStarted();

		while (this.stopStrategy.shouldContinue(iterations,
				stopwatch.elapsed(TimeUnit.MILLISECONDS))) {

			log.debug("Iteration {} begins with best mapping {}", iterations,
					PlacementMappingUtils.representMapping(bestMapping));

			// The best candidate during **this** iteration.
			MigrationPath bestIterationCandidate;
			double bestIterationScore;

			List<MigrationPath> singleMoveCandidates = singleMoveCandidates(bestMapping);
			log.debug("Generated {} single move candidates",
					singleMoveCandidates.size());

			if (singleMoveCandidates.isEmpty()) {
				break; // can't do anything!
			}

			bestIterationCandidate = getNextCandidate(singleMoveCandidates);
			bestIterationScore = cost(bestIterationCandidate);

			log.debug(
					"Lowest single candidate cost {} with moves {} in candidate mapping {}",
					bestIterationScore, bestIterationCandidate
							.getMigrationMoves(), PlacementMappingUtils
							.representMapping(bestIterationCandidate
									.getResultMapping()));

			if (!isImprovement(bestIterationScore)) {
				// have to widen the search space!
				log.debug("Generating double move candidates...");
				List<MigrationPath> doubleMoveCandidates = doubleMoveCandidates(bestMapping);
				log.debug("{} double move candidates generated",
						doubleMoveCandidates.size());

				if (doubleMoveCandidates.isEmpty()) {
					break; // can't do anything here either...
				}

				bestIterationCandidate = getNextCandidate(doubleMoveCandidates);
				bestIterationScore = cost(bestIterationCandidate);

				log.debug(
						"Lowest double candidate cost {} with moves {} in candidate mapping {}",
						bestIterationScore, bestIterationCandidate
						.getMigrationMoves(), PlacementMappingUtils
						.representMapping(bestIterationCandidate
								.getResultMapping()));

				if (!isImprovement(bestIterationScore)) {
					log.debug("Cannot find better candidate after double moves, exiting");
					/*
					 * In spite of widening the search space, we could not find
					 * a better candidate. We stop looking.
					 */
					break;
				}
			}

			bestMapping = bestIterationCandidate.getResultMapping();

			allMigrationMoves
					.addAll(bestIterationCandidate.getMigrationMoves());

			log.debug("Iteration {} resulting steps {}", iterations,
					bestIterationCandidate.getMigrationMoves());
			iterations++;
		}

		// TODO Create a MigrationPath
		return new MigrationPath(current, bestMapping, allMigrationMoves);

//		return bestMapping;
	}

	/**
	 * Determines whether a given iteration score is an improvement. This means,
	 * since we always to try to minimize, that anything below 0 is an
	 * improvment. This method only adds readability.
	 *
	 * @param iterationScore
	 *            The given iteration score.
	 * @return true if the score is an improvement, false otherwise.
	 */
	private boolean isImprovement(double iterationScore) {
		return iterationScore < 0;
	}

	/**
	 * Lists all single-move migration candidates by having the single-move
	 * migration strategy generate them.
	 *
	 * @param mapping
	 *            The current mapping.
	 * @return All generated single-move migration candidates.
	 */
	private List<MigrationPath> singleMoveCandidates(PlacementMapping mapping) {
		return this.singleMigrationMoveStrategy.generateMappings(mapping);
	}

	/**
	 * Lists all double-move migration candidates by asking all double-move
	 * migration generation strategies for their candidates and collecting them.
	 *
	 * @param mapping
	 *            The current mapping.
	 * @return All generated double-move migration candidates.
	 */
	private List<MigrationPath> doubleMoveCandidates(PlacementMapping mapping) {
		List<MigrationPath> doubleMoveCandidates = new LinkedList<>();
		for (DoubleMigrationMoveStrategy strategy : this.doubleMigrationMoveStrategies) {
			doubleMoveCandidates.addAll(strategy.generateMappings(mapping));
		}
		return doubleMoveCandidates;
	}

	/**
	 * The value of the given mapping, according to the
	 * {@link ObjectiveFunction}.
	 *
	 * @param mapping
	 *            The mapping to evaluate.
	 * @return The value of the mapping, according to the
	 *         {@link ObjectiveFunction}.
	 */
	private Double cost(MigrationPath mapping) {
		return this.objectiveFunction.determineObjectiveValue(mapping);
	}

	/**
	 * Selects the next candidate from the given set of candidates, using the
	 * {@link MigrationPathSelectionStrategy}. Note that is does not have to be
	 * the <strong>best</strong>, as that is up to the strategy.
	 *
	 * @param candidates
	 *            The candidate migration paths from which one is to be
	 *            selected.
	 * @return The selected migration path.
	 */
	private MigrationPath getNextCandidate(List<MigrationPath> candidates) {
		return this.migrationPathSelectionStrategy.selectCandidate(candidates,
				this.objectiveFunction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IterativePlacementOptimizer [singleMigrationMoveStrategy="
				+ this.singleMigrationMoveStrategy
				+ ", doubleMigrationMoveStrategies="
				+ this.doubleMigrationMoveStrategies + ", objectiveFunction="
				+ this.objectiveFunction + "]";
	}

	/**
	 * @return The single-move migration generation strategy in use.
	 */
	public SingleMigrationMoveStrategy getSingleMigrationMoveStrategy() {
		return this.singleMigrationMoveStrategy;
	}

	/**
	 * @return The list of double-move migration generation strategies in use.
	 *         The list is immutable.
	 */
	public List<DoubleMigrationMoveStrategy> getDoubleMigrationMoveStrategy() {
		return this.doubleMigrationMoveStrategies;
	}

	/**
	 * @return The objective function in use.
	 */
	public ObjectiveFunction getObjectiveFunction() {
		return this.objectiveFunction;
	}

	/**
	 * @return The migration path selection strategy in use.
	 */
	public MigrationPathSelectionStrategy getMigrationPathSelectionStrategy() {
		return this.migrationPathSelectionStrategy;
	}

	/**
	 * @return The stopping strategy in use.
	 */
	public StopStrategy getStopStrategy() {
		return this.stopStrategy;
	}

}
