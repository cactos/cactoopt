package eu.cactosfp7.cactoopt.optimisationservice.causa;

import java.util.Dictionary;

import eu.cactosfp7.cactoopt.optimisationservice.causa.CausaOptimisationAlgorithm.Algorithm;
import eu.cactosfp7.cactoopt.optimisationservice.config.IOptimisationConfigurable;

/**
 * Configurable for Causa algorithm.
 * @author stier, jakub
 *
 */
public class CausaOptimisationConfigurable implements IOptimisationConfigurable {
	
	public static final String ALGORITHM_KEY = "algorithm";
	public static Algorithm chosenAlgorithm;
	public static final String MANAGE_PHYSICAL_NODE_ACTIONS_KEY = "managePhysicalNodeActions";
	public static Boolean managePhysicalNodeAction;
	public static final String ITERATIONS_KEY = "iterations";
	public static Integer iterations;

    @Override
    public void updated(Dictionary<String, ?> properties) {
    	chosenAlgorithm = Algorithm.valueOf((String) properties.get(CausaOptimisationConfigurable.ALGORITHM_KEY));
    	managePhysicalNodeAction = Boolean.valueOf((String) properties.get(CausaOptimisationConfigurable.MANAGE_PHYSICAL_NODE_ACTIONS_KEY));
    	iterations = Integer.valueOf((String) properties.get(CausaOptimisationConfigurable.ITERATIONS_KEY));
    }

    // TODO jakub add methods for querying specific configuration properties if needed.
}
