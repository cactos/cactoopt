package eu.cactosfp7.cactoopt.framework.migrationstrategies;

import eu.cactosfp7.cactoopt.framework.migrationstrategies.impl.RotationMigrationMoveStrategy;
import eu.cactosfp7.cactoopt.framework.migrationstrategies.MigrationStrategy;

/**
 * A {@link MigrationStrategy} that considers two migrations at the same time.
 * Note that this does not mean that they have to be performed in parallel, only
 * that the solver considers two at a time. For a reason why this is useful, see
 * e.g. {@link RotationMigrationMoveStrategy}.
 */
public interface DoubleMigrationMoveStrategy extends MigrationStrategy {

}
