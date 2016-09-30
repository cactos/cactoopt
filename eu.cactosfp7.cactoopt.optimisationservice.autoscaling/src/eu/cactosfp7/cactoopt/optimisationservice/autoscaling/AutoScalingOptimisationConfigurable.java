package eu.cactosfp7.cactoopt.optimisationservice.autoscaling;

import java.util.Dictionary;

import eu.cactosfp7.cactoopt.optimisationservice.config.IOptimisationConfigurable;

/**
 * The Configurable for AutoScaling algorithm
 * 
 * @author Sebastian Krach
 *
 */
public class AutoScalingOptimisationConfigurable implements IOptimisationConfigurable {

	@Override
	public void updated(Dictionary<String, ?> properties) {
		// TODO Put configuration properties here
		// configuration entries are read from file "eu.cactosfp7.configuration/cactoopt_opt_autoscaling.cfg"
	}

}
