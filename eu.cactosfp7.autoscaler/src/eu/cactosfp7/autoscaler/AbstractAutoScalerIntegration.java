/**
 * 
 */
package eu.cactosfp7.autoscaler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationInstance;

/**Helper class to implement the common functionality for all AutoScalerIntegration implementations.
 * @author hgroenda
 *
 */
public abstract class AbstractAutoScalerIntegration implements IAutoScalerIntegration {

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(AbstractAutoScalerIntegration.class.getName());
	
	protected static List<WhiteBoxApplicationInstance> registeredAppInstances = new ArrayList<>();
	
	/* (non-Javadoc)
	 * @see eu.cactosfp7.autoscaler.IAutoScalerIntegration#register(eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationInstance)
	 */
	@Override
	public void register(WhiteBoxApplicationInstance appInstance) {
		registeredAppInstances.add(appInstance);
		log.info("App instance [" + appInstance.getId() + "] registered!");
	}

	/* (non-Javadoc)
	 * @see eu.cactosfp7.autoscaler.IAutoScalerIntegration#deregister(eu.cactosfp7.infrastructuremodels.logicaldc.application.WhiteBoxApplicationInstance)
	 */
	@Override
	public void deregister(WhiteBoxApplicationInstance appInstance) {
		registeredAppInstances.remove(appInstance);
		log.info("App instance [" + appInstance.getId() + "] deregistered!");
	}

	//TODO: Implement integration into Runtime or Simulation that allow the actual processing at required points in time.
}
