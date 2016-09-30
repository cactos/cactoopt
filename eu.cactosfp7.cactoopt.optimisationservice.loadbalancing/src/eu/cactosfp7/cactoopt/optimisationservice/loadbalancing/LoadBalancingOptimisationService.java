package eu.cactosfp7.cactoopt.optimisationservice.loadbalancing;

import eu.cactosfp7.cactoopt.optimisationservice.AbstractOptimisationService;

public class LoadBalancingOptimisationService extends AbstractOptimisationService {
    
     public LoadBalancingOptimisationService() {
         this.algorithm = new LoadBalancingOptimisationAlgorithm();
         this.configurable = new LoadBalancingOptimisationConfigurable();
     }
}
