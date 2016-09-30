package eu.cactosfp7.cactoopt.optimisationservice.registry;

import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.OptimisationPlan;

/**
 * Interface for interacting with the configured optimisation algorithm.
 * @author stier, groenda, hauser
 *
 */
public class OptimisationServiceRegistry implements IOptimisationAlgorithm {
    public static final String OPTIMISATION_UNREGISTERED = "eu/cactosfp7/cactoopt/optimisationservice/registry/OptimisationUnregistered";
    public static final String OPTIMISATION_UPDATED = "eu/cactosfp7/cactoopt/optimisationservice/registry/OptimisationUpdated";
    
    @Override
    public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm,
            LogicalLoadModel llm) {
        IOptimisationAlgorithm algorithm = OptimisationSettings.SELECTED_OPTIMISATION;
        if(algorithm == null) {
        	return null;
        }
        return algorithm.generateOptimizationPlan(pdcm, ldcm, plm, llm);
    }

    public synchronized boolean isConfigured() {
        return OptimisationSettings.SELECTED_OPTIMISATION != null;
    }
    
    public synchronized int getOptimisationFrequency(){
    	return OptimisationSettings.CYCLIC_OPTIMISER_FREQ;
    }
}