package eu.cactosfp7.cactoopt.placementservice.registry;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import eu.cactosfp7.cactoopt.placementservice.IPlacementService;

public class PlacementSettings implements ManagedService, ServiceListener {

    /**
     * Filename of the placement configuration file which is watched by the Felix Fileinstaller
     * The file is located in eu.cactosfp7.configuration.
     */
    public static final String PLACEMENTCONFIGID = "cactoopt_placement";
    
    public static IPlacementService SELECTED_PLACEMENT;
    
    /** Logger for this class. */
    private static final Logger log = Logger.getLogger(PlacementSettings.class.getName());

    private static final String USED_PLACEMENT_ALGORITHM_NAME_KEY = "placementName";

    private Properties properties;

    public PlacementSettings() {

        this.properties = new Properties();

    }
    
    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    public Properties getProperties() {
        return this.properties;
    }
    
    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {

        if (properties == null || properties.isEmpty()) {
            log.info(PLACEMENTCONFIGID + ".cfg is empty!");
        } else {
            // configure
            updateChosenPlacementAlgorithm((String) properties.get(USED_PLACEMENT_ALGORITHM_NAME_KEY));
        }

    }

    private void updateChosenPlacementAlgorithm(String name) {
     // Collect all algorithms that fit the chosen name.
        String filter = "(&(" + Constants.OBJECTCLASS + "=" + IPlacementService.class.getName() + ")(" 
                + USED_PLACEMENT_ALGORITHM_NAME_KEY + "=" + name + "))";
        ServiceReference<IPlacementService>[] serviceReferences = getServiceReferences(filter);
        // pick one (or any of the algorithms that fit the conditions
        if(serviceReferences != null && serviceReferences.length > 0) {
            setUsedService(serviceReferences[0]);
        }
        // register to wait for a suitable placement service to be registered and/or to react to de-registrations.
        try {
            Activator.getContext().addServiceListener(this, filter);
        } catch (InvalidSyntaxException e) {
            log.log(Level.SEVERE,"Filter condition for services was not correctly specified. Fix in source code.", e);
        }
    }
    
    private ServiceReference<IPlacementService>[] getServiceReferences(String filter) {
        ServiceReference<IPlacementService>[] serviceReferences = null;
        try {
            serviceReferences = (ServiceReference<IPlacementService>[]) Activator.getContext().getServiceReferences(IPlacementService.class.getName(), filter);
        } catch (InvalidSyntaxException e) {
            log.log(Level.SEVERE,"Filter condition for services was not correctly specified. Fix in source code.", e);
        }
        return serviceReferences;
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        ServiceReference<IPlacementService> serviceReference = (ServiceReference<IPlacementService>) event.getServiceReference();
        if(serviceReference != null && event.getType() == ServiceEvent.REGISTERED) {
            setUsedService(serviceReference);
        } else if(event.getType() == ServiceEvent.UNREGISTERING) {
            unregisterUsedService();
        }
    }

    private void unregisterUsedService() {
        PlacementSettings.SELECTED_PLACEMENT = null;
        //String unregisteredEvent = PlacementServiceRegistry.PLACEMENT_UNREGISTERED;
        //triggerEvent(unregisteredEvent);
    }

    private void setUsedService(ServiceReference<IPlacementService> serviceReference) {
        SELECTED_PLACEMENT = Activator.getContext().getService(serviceReference);
    }
    
    private void triggerEvent(String unregisteredEvent) {
        Event event = new Event(unregisteredEvent, (Map<String, ?>) null);
        BundleContext context = Activator.getContext();
        ServiceReference<?> serviceReference = context.getServiceReference(EventAdmin.class.getName());
        EventAdmin eventAdmin = (EventAdmin) context.getService(serviceReference);
        eventAdmin.postEvent(event);
    }
}
