package eu.cactosfp7.cactoopt.framework.migrationstrategies.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.combinatorics.Subsets;
import eu.cactosfp7.cactoopt.framework.migrationstrategies.DoubleMigrationMoveStrategy;
import eu.cactosfp7.cactoopt.framework.model.Migration;
import eu.cactosfp7.cactoopt.framework.model.MigrationMove;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;

/**
 * Double-move migration strategy that generates all splitting pairs, such that:
 *
 * <pre>
 * VM  Host  Host
 * VM1   A -> B
 * VM2   A -> C
 * </pre>
 *
 * Note that it is unclear at this point in time if this offers anything beyond
 * repeated single moves, but has been left in for now.
 * <p>
 * This class is stateless.
 */
public class SplitMigrationMoveStrategy implements DoubleMigrationMoveStrategy {

	@Override
	public List<MigrationPath> generateMappings(PlacementMapping current) {

		List<MigrationPath> mappings = new LinkedList<>();

		for (PhysicalMachine sourceMachine : current.getPhysicalMachines()) {
			List<List<VirtualMachine>> vmPairs = getVmPairs(sourceMachine);

			List<List<PhysicalMachine>> destinationPairs = getDestinationPairs(
					sourceMachine, current.getPhysicalMachines());

			for (List<VirtualMachine> vmPair : vmPairs) {
				for (List<PhysicalMachine> destinationPair : destinationPairs) {

					VirtualMachine vmA = vmPair.get(0);
					VirtualMachine vmB = vmPair.get(1);

					PhysicalMachine destinationA = destinationPair.get(0);
					PhysicalMachine destinationB = destinationPair.get(1);

					if (!isNewDestination(vmA, destinationA)
							|| !isNewDestination(vmB, destinationB)) {
						continue; // can't migrate to current host!
					}

					if (destinationA.canFit(vmA) && destinationB.canFit(vmB)) {
						List<MigrationMove> migrationMoves = new ArrayList<>();
						migrationMoves.add(new MigrationMove(vmA,
								new Migration(sourceMachine, destinationA)));

						migrationMoves.add(new MigrationMove(vmB,
								new Migration(sourceMachine, destinationB)));

						mappings.add(new MigrationPath(current, current
								.applyMigrationMoves(migrationMoves),
								migrationMoves));
					}
				}
			}
		}

		return mappings;
	}

	private boolean isNewDestination(VirtualMachine vm,
			PhysicalMachine destination) {
		return destination.getVirtualMachines().contains(vm);
	}

	private List<List<VirtualMachine>> getVmPairs(PhysicalMachine machine) {
		if (machine.getVirtualMachines().size() < 2) {
			return Collections.emptyList();
		}
		List<List<VirtualMachine>> vmPairs = Subsets.kSubsets(
				machine.getVirtualMachines(), 2);
		return vmPairs;
	}

	private List<List<PhysicalMachine>> getDestinationPairs(
			PhysicalMachine source, List<PhysicalMachine> machines) {
		if (machines.size() < 2) {
			return Collections.emptyList();
		}

		List<List<PhysicalMachine>> pairs = Subsets.kSubsets(machines, 2);
		return pairs;
	}

}
