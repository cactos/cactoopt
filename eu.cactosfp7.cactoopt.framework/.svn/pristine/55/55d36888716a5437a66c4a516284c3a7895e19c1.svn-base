package eu.cactosfp7.cactoopt.framework.functions.impl;

import java.util.List;

import eu.cactosfp7.cactoopt.framework.functions.MigrationPathSelectionStrategy;
import eu.cactosfp7.cactoopt.framework.functions.ObjectiveFunction;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;

/**
 * Implementations of some typical {@link MigrationPathSelectionStrategy}
 * variants.
 */
public class MigrationSelectionStrategies {

	/**
	 * Returns the best {@link MigrationPath}, as determined by asking the
	 * {@link ObjectiveFunction} for the value of each candidate.
	 * 
	 * @return The best {@link MigrationPath}, as determined by asking the
	 *         {@link ObjectiveFunction} for the value of each candidate.
	 */
	public static MigrationPathSelectionStrategy best() {
		return new MigrationPathSelectionStrategy() {
			@Override
			public MigrationPath selectCandidate(
					List<MigrationPath> candidates,
					ObjectiveFunction objectiveFunction) {
				MigrationPath best = candidates.get(0);
				double lowestCost = objectiveFunction
						.determineObjectiveValue(candidates.get(0));

				for (MigrationPath placementMapping : candidates) {
					double currentCost = objectiveFunction
							.determineObjectiveValue(placementMapping);
					if (currentCost < lowestCost) {
						best = placementMapping;
						lowestCost = currentCost;
					}
				}

				return best;
			}
		};
	}

	/**
	 * Returns the first {@link MigrationPath} that the
	 * {@link ObjectiveFunction} gives a lower value than the given threshold.
	 * If no such {@link MigrationPath} is found, the first among the candidates
	 * is returned.
	 *
	 * @param threshold
	 *            The threshold value.
	 * @return The first among the candidates to have a lower value than the
	 *         given threshold, or the first among them, if no better is found.
	 */
	public static MigrationPathSelectionStrategy threshold(
			final double threshold) {
		return new MigrationPathSelectionStrategy() {

			@Override
			public MigrationPath selectCandidate(
					List<MigrationPath> candidates,
					ObjectiveFunction objectiveFunction) {
				for (MigrationPath placementMapping : candidates) {
					double currentCost = objectiveFunction
							.determineObjectiveValue(placementMapping);
					if (currentCost < threshold) {
						return placementMapping;
					}
				}
				return candidates.get(0);
			}
		};
	}
}
