package eu.cactosfp7.cactoopt.cyclicoptimiser;

import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import eu.cactosfp7.cactoopt.optimisationservice.registry.OptimisationServiceRegistry;
import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.service.CdoSessionService;
import eu.cactosfp7.cdosession.settings.CactosUser;

/**
 * Client that periodically optimises the placement of VMs in a data centre.
 * @author jakub, stier
 *
 */
public class CyclicOptimiserClient implements EventHandler, ServiceListener {
    	
	public static CyclicOptimiserClient INSTANCE;
	
    public CyclicOptimiserClient() {
        registerEventListenerForEvent(CdoSessionService.EVENT_TYPE);
		if(INSTANCE != null)
			throw new RuntimeException("Instantiating new CyclicOptimiserClient is not allowed!");
		INSTANCE = this;
    }

    /** The logger for this class. */
    private static final Logger log = Logger.getLogger(CyclicOptimiserClient.class.getName());

    /**
     * Filename of the cdo configuration file which is watched by the Felix Fileinstaller The file
     * is located in eu.cactosfp7.configuration
     */
    public static final String CDOCONFIGID = "cactoopt_cdo";

    /** Handle to the scheduled task. */
    @SuppressWarnings("rawtypes")
    private static ScheduledFuture OPTIMISATION_HANDLE;

    /**
     * The OptimisationServiceRegistry provided by CactoOpt
     */
    private static OptimisationServiceRegistry OPTIMISATION_SERVICE;
    /**
     * The used service registry.
     */
    private static CdoSessionService CDO_SESSION_SERVICE;
    
    // The Runnable periodically executed by the cyclic optimiser.
    private static Runnable CYCLIC_OPTIMISER_TASK = null;

    private boolean isRunning = false;

    /**
     * Bind method for discovered service.
     * 
     * @param service
     *            The discovered service.
     */
    public synchronized void bindCdoSessionService(CdoSessionService cdoSessionService) {
        CyclicOptimiserClient.CDO_SESSION_SERVICE = cdoSessionService;
        checkAndStartCyclicOptimiser();
        log.info("CDO Session Service connected.");
    }

    /**
     * Unbind method for discovered service.
     * 
     * @param service
     *            The removed service.
     */
    public synchronized void unbindCdoSessionService(CdoSessionService service) {
        if (CyclicOptimiserClient.CDO_SESSION_SERVICE == service) {
            deactivateCyclicOptimizer();
            CyclicOptimiserClient.CDO_SESSION_SERVICE = null;
            log.info("CDO Session Service disconnected.");
        }
    }


    /**
     * Bind the optimisation service to be used by <code>this</code>.
     * @param optimisationService The optimisation service to be used.
     */
    public synchronized void bindOptimisationService(OptimisationServiceRegistry optimisationService) {
        CyclicOptimiserClient.OPTIMISATION_SERVICE = optimisationService;
        if(optimisationService.isConfigured()) {
            checkAndStartCyclicOptimiser();            
        } else {
            log.info("No optimisation service available yet. Waiting for optimisation service to become available.");
            registerEventListenerForEvent(OptimisationServiceRegistry.OPTIMISATION_UPDATED);
        }
        log.info("Optimisation Service Registry connected.");
    }

    private void registerEventListenerForEvent(final String eventType) {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put(EventConstants.EVENT_TOPIC, eventType);
        Activator.getContext().registerService(EventHandler.class.getName(), this, properties);
    }
    
    /**
     * Unbind the optimisation service used by the cyclic optimiser.
     * @param optimisationService The service that is unbound. <code>this</code> only enacts the
     * unbinding if the service to be disconnected equals the set service.
     */
    public synchronized void unbindOptimisationService(OptimisationServiceRegistry optimisationService) {
        if ( CyclicOptimiserClient.OPTIMISATION_SERVICE == optimisationService) {
            deactivateCyclicOptimizer();
            CyclicOptimiserClient.OPTIMISATION_SERVICE = null;
        }
        log.info("Optimisation Service Registry disconnected.");
    }
    
    /**
     * Starts the cyclic optimiser if all preconditions are met. Otherwise, nothing happens.
     */
    protected synchronized void checkAndStartCyclicOptimiser() {
        if(!isRunning && CyclicOptimiserClient.OPTIMISATION_HANDLE == null && requiredServicesAvailable()) {
            startCyclicOptimizer();
        }
    }
    
    /** 
     * Used to check whether all services needed by <code>this</code> are available.
     * @return Whether the required services are available.
     */
    private synchronized boolean requiredServicesAvailable() {
        return CyclicOptimiserClient.CDO_SESSION_SERVICE != null 
                && CyclicOptimiserClient.OPTIMISATION_SERVICE != null && CyclicOptimiserClient.OPTIMISATION_SERVICE.isConfigured();
    }
    
    /**
     * Deactivates the currently running cyclic optimisation.
     */
    protected synchronized void deactivateCyclicOptimizer() {
        OPTIMISATION_HANDLE.cancel(true);
        CyclicOptimiserClient.CYCLIC_OPTIMISER_TASK = null;
    }
    
    /**
     * Starts the cyclic optimiser if the CDO session service has been configured.
     */
    private void startCyclicOptimizer() {
    	if(CyclicOptimiserClient.CDO_SESSION_SERVICE.isConfigured()) {
            log.info("Starting cyclic optimiser");
            CactosCdoSession cactosCdoSession = CyclicOptimiserClient.CDO_SESSION_SERVICE.getCactosCdoSession(CactosUser.CACTOOPT);

            CYCLIC_OPTIMISER_TASK = new CyclicOptimiser(CyclicOptimiserClient.OPTIMISATION_SERVICE, cactosCdoSession);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            int freq = CyclicOptimiserClient.OPTIMISATION_SERVICE.getOptimisationFrequency();
            OPTIMISATION_HANDLE = scheduler.scheduleWithFixedDelay(CYCLIC_OPTIMISER_TASK, 0, freq, TimeUnit.SECONDS);            
            isRunning = true;
    	} else {
    	    log.info("CDO Session Service has not been configured yet. Waiting for its configuration.");
    	}
    }

    /**
     * Notifies the cyclic optimiser. This will re-check the configuration of the CDOSessionService configured
     * via {@link CyclicOptimiserClient#bindCdoSessionService(CdoSessionService)}. If the CDO session service
     * has been initialised, the cyclic optimiser will be started.
     * @param event The triggered event.
     */
	@Override
	public void handleEvent(Event event) {
	    checkAndStartCyclicOptimiser();
	}

    @Override
    public void serviceChanged(ServiceEvent event) {
        checkAndStartCyclicOptimiser();
    }
}