package eu.cactosfp7.cactoopt.placementservice.config;

import java.util.Dictionary;

/**
 * Responsible for handling updates to the properties of a Placement Service. As receiving specific service configuration
 * is distinct from this responsibility, this interface does not subsume such services.
 * @author stier
 *
 */
public interface IPlacementConfigurable {

    void updated(Dictionary<String, ?> properties);
}
