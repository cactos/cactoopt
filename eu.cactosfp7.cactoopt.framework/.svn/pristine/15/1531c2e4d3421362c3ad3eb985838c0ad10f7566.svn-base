package eu.cactosfp7.cactoopt.framework.functions.impl;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.functions.MappingEvaluationFunction;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.stats.PlacementMappingStats;

/**
 * A {@link MappingEvaluationFunction} that evaluates how good a certain
 * {@link PlacementMapping} is with regard to the degree of load-balancing that
 * it accomplishes.
 * <p/>
 * The function calculates:
 *
 * <pre>
 * sum((vm[i](mem) - mean(vm(mem))) &circ; 2)
 * </pre>
 */
public class MemoryLoadBalancingEvaluationFunction implements
		MappingEvaluationFunction {
	@Override
	public double evaluateMapping(PlacementMapping mapping) {
		final double averageMemoryLoad = PlacementMappingStats
				.meanMemoryLoad(mapping);

		double sumOfDiffsFromMean = 0.0d;

		for (PhysicalMachine physicalMachine : mapping.getPhysicalMachines()) {
			sumOfDiffsFromMean += Math.abs(physicalMachine
					.getMemoryUtilizationPercentage() - averageMemoryLoad);
		}

		return Math.pow(sumOfDiffsFromMean, 2);
	}
}
