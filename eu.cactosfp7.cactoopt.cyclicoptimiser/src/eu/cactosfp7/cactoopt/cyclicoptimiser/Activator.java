package eu.cactosfp7.cactoopt.cyclicoptimiser;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator class for the optimisation service.
 * @author stier
 *
 */
public class Activator implements BundleActivator {

    private static BundleContext context;

    /**
     * Get the configured context of this bundle's instance.
     * @return The bundle context of this bundle's instance.
     */
    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        if(CyclicOptimiserClient.INSTANCE != null){
        	CyclicOptimiserClient.INSTANCE.checkAndStartCyclicOptimiser();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
        if(CyclicOptimiserClient.INSTANCE != null){
        	CyclicOptimiserClient.INSTANCE.deactivateCyclicOptimizer();
        }
    }

}