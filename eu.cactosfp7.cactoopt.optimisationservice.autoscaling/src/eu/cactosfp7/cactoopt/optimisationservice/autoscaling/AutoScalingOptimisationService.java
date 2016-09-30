package eu.cactosfp7.cactoopt.optimisationservice.autoscaling;

import eu.cactosfp7.cactoopt.optimisationservice.AbstractOptimisationService;

/**
 * The service implementation for the AutoScaling algorithm.
 * 
 * @author Sebastian Krach
 *
 */
public class AutoScalingOptimisationService extends AbstractOptimisationService {

	public AutoScalingOptimisationService() {
		this.algorithm = new AutoScalingOptimisationAlgorithm();
		this.configurable = new AutoScalingOptimisationConfigurable();
	}
	
	public void setTimeProvider(ITimeProvider timeProvider) {
		((AutoScalingOptimisationAlgorithm) this.algorithm).setTimeProvider(timeProvider);
	}
}
