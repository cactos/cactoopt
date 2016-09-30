/**
 * 
 */
package eu.cactosfp7.autoscaler;

import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationInstance;

/**
 * Interface for AutoScalerIntegration implementations allowing to manage
 * scalable connectors of applications. AutoScalerIntegration implementations
 * are responsible to manage each individual connector of an application
 * instance at the points and with the settings specified in the description.
 * Only White-Box appliations need to be taken into account as they are the only
 * type of application with more than one VM.
 * 
 * @author hgroenda
 *
 */
public interface IAutoScalerIntegration {
	/**
	 * Registers an application to be scaled automatically.
	 * 
	 * @param appInstance
	 */
	void register(WhiteBoxApplicationInstance appInstance);

	/**De-registers an application to be scaled automatically.
	 * @param appInstance
	 */
	void deregister(WhiteBoxApplicationInstance appInstance);
}
