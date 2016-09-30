package eu.cactosfp7.cactoopt.optimisationservice;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import eu.cactosfp7.cactoopt.optimisationservice.config.IOptimisationConfigurable;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.OptimisationPlan;

/**
 * Optimisation service super-class that offers a set of common services that need to be implemented by all optimisation services.
 * @author stier
 *
 */
public abstract class AbstractOptimisationService implements IOptimisationAlgorithm, ManagedService {

    /*
     * The algorithm used by the offered optimisation service.
     */
    protected IOptimisationAlgorithm algorithm;
    
    /*
     * A configurable that holds a set of optimisation properties.
     */
    protected IOptimisationConfigurable configurable;

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        configurable.updated(properties);
    }

    @Override
    public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm) {
        return algorithm.generateOptimizationPlan(pdcm, ldcm, plm, llm);
    }

}