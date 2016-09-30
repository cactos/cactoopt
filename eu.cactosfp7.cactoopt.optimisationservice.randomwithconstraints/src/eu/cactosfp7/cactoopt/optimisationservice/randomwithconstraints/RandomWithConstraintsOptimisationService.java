package eu.cactosfp7.cactoopt.optimisationservice.randomwithconstraints;

import eu.cactosfp7.cactoopt.optimisationservice.AbstractOptimisationService;

public class RandomWithConstraintsOptimisationService extends
		AbstractOptimisationService {
	
	/**
	 * Creates an {@link LinKernighanOptimisationAlgorithmService}.
	 */
	public RandomWithConstraintsOptimisationService() {
	    this.algorithm = new RandomWithConstraintsOptimisationAlgorithm();
	    this.configurable = new RandomWithConstraintsOptimisationConfigurable();
	}

}
