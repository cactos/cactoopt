package eu.cactosfp7.cactoopt.placementservice.loadbalancing;

import eu.cactosfp7.cactoopt.placementservice.impl.AbstractPlacementService;

public class LoadBalancingPlacementService extends AbstractPlacementService {
    
    /**
     * Creates a {@link FirstFitPlacementService}.
     */
    public LoadBalancingPlacementService()  {
        this.placementAlgorithm = new LoadBalancingPlacementAlgorithm();
        this.configurable = new LoadBalancingConfigurable();
    }
}
