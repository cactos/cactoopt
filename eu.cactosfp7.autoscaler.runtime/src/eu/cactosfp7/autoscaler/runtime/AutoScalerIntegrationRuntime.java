/**
 * 
 */
package eu.cactosfp7.autoscaler.runtime;

import java.util.List;
import java.util.logging.Logger;

import eu.cactosfp7.autoscaler.AbstractAutoScalerIntegration;
import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationInstance;

/**AutoScalerIntegration for the Runtime environment.
 * TODO implement
 *
 */
public class AutoScalerIntegrationRuntime extends AbstractAutoScalerIntegration {
	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(AutoScalerIntegrationRuntime.class.getName());
	
	public static List<WhiteBoxApplicationInstance> getRegisteredAppInstances() {
		log.info("Returning a list of app instances!");
		return registeredAppInstances;
	}
}
