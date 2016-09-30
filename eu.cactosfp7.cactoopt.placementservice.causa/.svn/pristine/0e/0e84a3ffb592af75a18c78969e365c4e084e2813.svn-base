package eu.cactosfp7.cactoopt.placementservice.causa;

import java.util.Dictionary;

import eu.cactosfp7.cactoopt.placementservice.causa.CausaPlacementAlgorithm.Algorithm;
import eu.cactosfp7.cactoopt.placementservice.config.IPlacementConfigurable;

/**
 * Configurable for Causa placement algorithm.
 * @author stier, jakub
 *
 */
public class CausaPlacementConfigurable implements IPlacementConfigurable {
	
	public static final String ALGORITHM_KEY = "algorithm";
	public static Algorithm chosenAlgorithm;

    @Override
    public void updated(Dictionary<String, ?> properties) {
    	chosenAlgorithm = Algorithm.valueOf((String) properties.get(CausaPlacementConfigurable.ALGORITHM_KEY));
    }

    // TODO jakub add methods for querying specific configuration properties if needed.
}
