package eu.cactosfp7.cactoopt.optimisationservice.random;

import java.util.Dictionary;

import eu.cactosfp7.cactoopt.optimisationservice.config.IOptimisationConfigurable;

public class RandomOptimisationConfigurable implements IOptimisationConfigurable {

    @Override
    public void updated(Dictionary<String, ?> properties) {
        // TODO jakub if needed, handle the collection of reconfiguration algorithms here.
    }

    // TODO jakub add methods for querying specific configuration properties if needed.
}
