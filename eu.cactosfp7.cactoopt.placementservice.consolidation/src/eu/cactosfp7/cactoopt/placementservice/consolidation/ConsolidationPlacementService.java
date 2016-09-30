package eu.cactosfp7.cactoopt.placementservice.consolidation;

import eu.cactosfp7.cactoopt.placementservice.impl.AbstractPlacementService;

public class ConsolidationPlacementService extends AbstractPlacementService {
    
    /**
     * Creates a {@link FirstFitPlacementService}.
     */
    public ConsolidationPlacementService()  {
        this.placementAlgorithm = new ConsolidationPlacementAlgorithm();
        this.configurable = new ConsolidationConfigurable();
    }
}
