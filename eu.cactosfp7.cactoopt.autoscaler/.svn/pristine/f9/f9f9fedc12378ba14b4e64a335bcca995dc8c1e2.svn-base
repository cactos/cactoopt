/**
 * 
 */
package eu.cactosfp7.cactoopt.autoscaler;

import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ApplicationInstance;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.ScalableVMImageConnector;
import eu.cactosfp7.optimisationplan.OptimisationPlan;

/**
 * Interface for an Auto Scaler that determines the optimal number of connected
 * VMs for a scalable connection.
 *
 */
public interface IAutoScaler {
	/**
	 * Request a calculation of the optimal number of connected VMs for a
	 * scalable connection.
	 * 
	 * @param appInstance
	 *            The application instance.
	 * @param connector
	 *            The scalable connector.
	 * @param llm
	 *            Load and request information.
	 * @return An optimisation plan with suggested changes to get to the optimal
	 *         number. Empty plan if nothing should be changed.
	 */
	public OptimisationPlan optimiseScaling(ApplicationInstance appInstance, 
			ScalableVMImageConnector connector,	LogicalLoadModel llm);
}
