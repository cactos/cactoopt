package eu.cactosfp7.cactoopt.behaviourinference.registry;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator class for the behaviour inference service.
 * @author stier, groenda
 *
 */
public class Activator implements BundleActivator {

	/** OSGI bundle context. */
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
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
