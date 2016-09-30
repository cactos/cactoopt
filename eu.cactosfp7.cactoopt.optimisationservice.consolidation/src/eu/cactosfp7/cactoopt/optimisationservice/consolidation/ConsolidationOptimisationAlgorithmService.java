package eu.cactosfp7.cactoopt.optimisationservice.consolidation;

import eu.cactosfp7.cactoopt.optimisationservice.AbstractOptimisationService;
import eu.cactosfp7.cactoopt.optimisationservice.consolidation.ConsolidationOptimisationAlgorithm;
import eu.cactosfp7.cactoopt.optimisationservice.consolidation.ConsolidationOptimisationAlgorithmService;
import eu.cactosfp7.cactoopt.optimisationservice.consolidation.ConsolidationOptimisationConfigurable;

public class ConsolidationOptimisationAlgorithmService extends
		AbstractOptimisationService {
	/**
	 * Creates an {@link ConsolidationOptimisationAlgorithmService}.
	 */
	public ConsolidationOptimisationAlgorithmService() {
	    this.algorithm = new ConsolidationOptimisationAlgorithm();
	    this.configurable = new ConsolidationOptimisationConfigurable();
	}
}
