package eu.cactosfp7.cactoopt.framework.migrationstrategies;

import java.util.List;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;

/**
 * A migration strategy shall output possible {@link MigrationPath}s given a
 * current {@link PlacementMapping}.
 */
public interface MigrationStrategy {

	public List<MigrationPath> generateMappings(PlacementMapping current);

}
