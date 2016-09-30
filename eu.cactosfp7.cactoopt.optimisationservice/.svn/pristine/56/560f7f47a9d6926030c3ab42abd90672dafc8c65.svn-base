package eu.cactosfp7.cactoopt.optimisationservice;

import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.OptimisationPlan;

/**Interface of Optimisation Algorithms.
 * 
 * @author hgroenda
 *
 */
public interface IOptimisationAlgorithm {
	
	/**Run an optimization with the algorithm.
	 * @param pdcm Physical Data Centre Model.
	 * @param ldcm Logical Data Centre Model.
	 * @param plm Physical Load Model.
	 * @param llm Logical Load Model.
	 * @return An optimisation plan or <code>null</code> if no optimisation is suggested by the algorithm.
	 */
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm);
	
}
