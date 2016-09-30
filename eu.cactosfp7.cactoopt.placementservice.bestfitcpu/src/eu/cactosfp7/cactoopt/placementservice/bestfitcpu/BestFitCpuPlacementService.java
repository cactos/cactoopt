package eu.cactosfp7.cactoopt.placementservice.bestfitcpu;

import eu.cactosfp7.cactoopt.placementservice.impl.AbstractPlacementService;

public class BestFitCpuPlacementService extends AbstractPlacementService {
    
    /**
     * Creates a {@link FirstFitPlacementService}.
     */
    public BestFitCpuPlacementService()  {
        this.placementAlgorithm = new BestFitCpuPlacementAlgorithm();
        this.configurable = new BestFitCpuConfigurable();
    }
}
