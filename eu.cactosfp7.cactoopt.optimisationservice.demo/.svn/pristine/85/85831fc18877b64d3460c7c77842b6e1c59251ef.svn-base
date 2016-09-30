package eu.cactosfp7.cactoopt.optimisationservice.demo;

import org.osgi.service.cm.ManagedService;

import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
import eu.cactosfp7.cactoopt.optimisationservice.config.IOptimisationConfigurable;
import eu.cactosfp7.cactoopt.placementservice.InitialPlacementAlgorithm;
import eu.cactosfp7.cactoopt.placementservice.impl.AbstractPlacementService;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.OptimisationPlan;

/**
 * Service offering a demo optimisation algorithm.
 * @author stier
 *
 */
public class DemoOptimisationAlgorithmService extends AbstractPlacementService implements IOptimisationAlgorithm, ManagedService {
	
    /*
     * The algorithm used by the offered optimisation service.
     */
    protected IOptimisationAlgorithm algorithm;
    
    /*
     * A configurable that holds a set of optimisation properties.
     */
    protected IOptimisationConfigurable optConfigurable;
	
	/**
	 * Creates an {@link DemoOptimisationAlgorithmService}.
	 */
	public DemoOptimisationAlgorithmService() {
//        final Actuator actuator = null;
//        final Sensor sensor = null;
//	    this.algorithm = new DemoOptimisationAlgorithm(sensor, actuator);
        this.algorithm = new DemoOptimisationAlgorithm();
	    this.optConfigurable = new DemoOptimisationConfigurable();
	    this.configurable = new DemoPlacementConfigurable();
	    this.placementAlgorithm = (InitialPlacementAlgorithm) this.algorithm;
	}

	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm,
			LogicalLoadModel llm) {
		return this.algorithm.generateOptimizationPlan(pdcm, ldcm, plm, llm);
	}

}
