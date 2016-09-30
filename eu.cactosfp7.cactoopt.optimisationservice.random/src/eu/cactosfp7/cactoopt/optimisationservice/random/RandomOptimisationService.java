package eu.cactosfp7.cactoopt.optimisationservice.random;

import eu.cactosfp7.cactoopt.optimisationservice.AbstractOptimisationService;

public class RandomOptimisationService extends AbstractOptimisationService {
	
	/**
	 * Creates an {@link LinKernighanOptimisationAlgorithmService}.
	 */
	public RandomOptimisationService() {
	    this.algorithm = new RandomOptimisationAlgorithm();
	    this.configurable = new RandomOptimisationConfigurable();
	}

}
