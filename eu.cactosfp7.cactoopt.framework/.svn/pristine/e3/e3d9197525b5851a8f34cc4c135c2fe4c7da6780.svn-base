package eu.cactosfp7.cactoopt.framework.stats;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;

/**
 * Statistics toolbox for {@link PlacementMapping}.
 */
public class PlacementMappingStats {

	/**
	 * Calculates the mean load over all cores in the {@link PhysicalMachine} in
	 * the {@link PlacementMapping}.
	 *
	 * @param mapping
	 *            The mapping of interest.
	 * @return The mean load over all CPU cores in the mapping.
	 */
	public static double meanCoresLoad(PlacementMapping mapping) {
		double sum = 0;

		for (PhysicalMachine physicalMachine : mapping.getPhysicalMachines()) {
			sum += physicalMachine.getCoreUtilizationPercentage();
		}

		return sum / mapping.getPhysicalMachines().size();
	}

	/**
	 * Calculates the mean memory load in all {@link PhysicalMachine}s in the
	 * {@link PlacementMapping}.
	 *
	 * @param mapping
	 *            The mapping of interest.
	 * @return The mean memory load over all physical machines in the mapping.
	 */
	public static double meanMemoryLoad(PlacementMapping mapping) {
		double sum = 0;

		for (PhysicalMachine physicalMachine : mapping.getPhysicalMachines()) {
			sum += physicalMachine.getMemoryUtilizationPercentage();
		}

		return sum / mapping.getPhysicalMachines().size();
	}

}
