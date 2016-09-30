package eu.cactosfp7.cactoopt.placementservice.firstfit;

import eu.cactosfp7.cactoopt.placementservice.impl.AbstractPlacementService;

/**
 * Service offering a first-fit placement algorithm.
 * @author stier
 *
 */
public class FirstFitPlacementService extends AbstractPlacementService {
    
    /**
     * Creates a {@link FirstFitPlacementService}.
     */
    public FirstFitPlacementService()  {
        this.placementAlgorithm = new FirstFitPlacementAlgorithm();
        this.configurable = new FirstFitConfigurable();
    }
}
