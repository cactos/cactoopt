package eu.cactosfp7.cactoopt.framework.migrationstrategies.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.migrationstrategies.SingleMigrationMoveStrategy;
import eu.cactosfp7.cactoopt.framework.model.Migration;
import eu.cactosfp7.cactoopt.framework.model.MigrationMove;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;

/**
 * Single-move migration strategy that tries every possible migration.
 * <p>
 * This class is stateless.
 */
public class TryAllSingleMoveMigrationStrategy implements
		SingleMigrationMoveStrategy {

	@Override
	public List<MigrationPath> generateMappings(PlacementMapping current) {
		LinkedList<MigrationPath> mappings = new LinkedList<>();

		for (VirtualMachine vm : current.getVirtualMachines()) {
			PhysicalMachine source = current.getPhysicalMachineForVm(vm);
			for (PhysicalMachine destination : current.getPhysicalMachines()) {

				if (destination.equals(source)) {
					continue;
				}
				if (destination.canFit(vm)) {
					List<MigrationMove> move = Collections
							.singletonList(new MigrationMove(vm, new Migration(
									source, destination)));
					PlacementMapping mapping = current
							.applyMigrationMoves(move);

					mappings.add(new MigrationPath(current, mapping, move));
				}
			}
		}

		return mappings;
	}
}