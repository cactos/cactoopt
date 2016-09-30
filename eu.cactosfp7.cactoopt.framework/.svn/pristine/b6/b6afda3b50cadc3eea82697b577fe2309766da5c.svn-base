package eu.cactosfp7.cactoopt.framework.migrationstrategies.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.migrationstrategies.DoubleMigrationMoveStrategy;
import eu.cactosfp7.cactoopt.framework.model.Migration;
import eu.cactosfp7.cactoopt.framework.model.MigrationMove;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;
import eu.cactosfp7.cactoopt.framework.model.exceptions.PlacementException;

/**
 * Double-move migration strategy that generates pairs with rotation, such that:
 *
 * <pre>
 * VM  Host  Host
 * VM1   A -> B
 * VM2   B -> C
 * </pre>
 *
 * ...where host C can be host A, too.
 * <p>
 *
 * This is not the same as two single moves in sequence, since a case such as
 * the one depicted below (VM capacity demand is given as numbers, hosts are
 * shown as columns):
 *
 * <pre>
 *      A    B    C
 * VM   2    1    1
 * VM   2    2    1
 * </pre>
 * 
 * In the above case, the optimal load balancing solution is that hosts A and C
 * switch VMs, so that all hosts have one VM requiring 2 and one VM requiring 1.
 * Single moves will not appear to achieve this, so that solution will never be
 * found unless a double-move strategy such as this one is used.
 *
 * <p>
 * This class is stateless.
 */
public class RotationMigrationMoveStrategy implements
DoubleMigrationMoveStrategy {

	@Override
	public List<MigrationPath> generateMappings(PlacementMapping current) {
		List<MigrationPath> mappings = new LinkedList<>();

		for (PhysicalMachine hostA : current.getPhysicalMachines()) {

			for (PhysicalMachine hostB : current.getPhysicalMachines()) {

				if (hostA.equals(hostB)) {
					continue; // no same host migrations!
				}

				for (PhysicalMachine hostC : current.getPhysicalMachines()) {
					// C may equal A, that's fine
					if (hostB.equals(hostC)) {
						continue; // no same host migrations!
					}

					for (VirtualMachine vmA : hostA.getVirtualMachines()) {
						for (VirtualMachine vmB : hostB.getVirtualMachines()) {

							// see if vmB fits on hostC, if not, continue with
							// new vmB

							if (!hostC.canFit(vmB)) {
								continue;
							}

							List<MigrationMove> migrationMoves = new ArrayList<>();

							migrationMoves.add(new MigrationMove(vmA,
									new Migration(hostA, hostB)));

							migrationMoves.add(new MigrationMove(vmB,
									new Migration(hostB, hostC)));

							try {
								mappings.add(new MigrationPath(current, current
										.applyMigrationMoves(migrationMoves),
										migrationMoves));
							} catch (PlacementException e) {
								/*
								 * Assume that this migration works, but if we
								 * get an exception, the migrations could not be
								 * made in sequence. This should be accurate,
								 * since a VM still requires resources from its
								 * source host while migrating.
								 */
								continue;
							}
						}
					}
				}
			}

		}

		return mappings;
	}
}
