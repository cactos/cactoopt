package eu.cactosfp7.cactoopt.placementservice.bestfitmemory;

import eu.cactosfp7.cactoopt.placementservice.impl.AbstractPlacementService;

public class BestFitMemoryPlacementService extends
		AbstractPlacementService {
		    
		    /**
		     * Creates a {@link FirstFitPlacementService}.
		     */
		    public BestFitMemoryPlacementService()  {
		        this.placementAlgorithm = new BestFitMemoryPlacementAlgorithm();
		        this.configurable = new BestFitMemoryConfigurable();
		    }
}
