package eu.cactosfp7.cactoopt.framework;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.model.MigrationMove;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;
import eu.cactosfp7.cactoopt.framework.model.exceptions.CoresExhaustedException;
import eu.cactosfp7.cactoopt.framework.model.exceptions.MemoryExhaustedException;
import eu.cactosfp7.cactoopt.framework.PlacementOptimizer;

/**
 * A placement mapping describes how a number of virtual machines are laid out
 * over a number of physical machines.
 * <p/>
 * A {@link PlacementMapping} is both the input and the output of a
 * {@link PlacementOptimizer}.
 *
 * @see PlacementOptimizer
 */
public final class PlacementMapping {

	/** Map of physical machines, keyed on machine ID. */
	private final Map<String, PhysicalMachine> machineMap;

	/**
	 * Tracks on what physical machine a given virtual machine is hosted. Keyed
	 * on VM ID.
	 */
	private final Map<String, PhysicalMachine> vmHostMap;

	public PlacementMapping(List<PhysicalMachine> physicalMachines) {
		this.machineMap = Maps.newHashMap();
		this.vmHostMap = Maps.newHashMap();
		for (PhysicalMachine machine : physicalMachines) {
			this.machineMap.put(machine.getId(), machine);
			for (VirtualMachine vm : machine.getVirtualMachines()) {
				this.vmHostMap.put(vm.getId(), machine);
			}
		}
	}

	/**
	 * Applies a sequence of {@link MigrationMove}s to this
	 * {@link PlacementMapping} and returns the {@link PlacementMapping} that
	 * would be the result of these changes.
	 * <p/>
	 * Note that the method has no side-effects on the owning object.
	 *
	 * @param moves
	 *            The sequence of moves that are to be applied to produce a new
	 *            {@link PlacementMapping}.
	 * @return
	 * @throws CoresExhaustedException
	 * @throws MemoryExhaustedException
	 */
	public PlacementMapping applyMigrationMoves(List<MigrationMove> moves)
			throws CoresExhaustedException, MemoryExhaustedException {
		PlacementMapping copy = copy();

		for (MigrationMove move : moves) {
			PhysicalMachine source = copy.getHostById(move.getMigration()
					.getSource().getId());
			PhysicalMachine destination = copy.getHostById(move.getMigration()
					.getDestination().getId());

			source.unassignVm(move.getVm());
			destination.assignVm(move.getVm());
			copy.vmHostMap.put(move.getVm().getId(), destination);
		}

		return copy;
	}

	/**
	 * Returns a deep copy of this {@link PlacementMapping}.
	 *
	 * @return
	 */
	public PlacementMapping copy() {
		List<PhysicalMachine> copiedPhysicalMachines = new ArrayList<PhysicalMachine>(
				this.machineMap.values());
		for (PhysicalMachine physicalMachine : this.machineMap.values()) {
			copiedPhysicalMachines.add(physicalMachine.copy());
		}
		return new PlacementMapping(copiedPhysicalMachines);
	}

	/**
	 * Returns a physical machine host by its id. Throws an
	 * {@link IllegalArgumentException} if no machine with the given identifier
	 * is found.
	 *
	 * @param id
	 *            A machine identifier.
	 * @return
	 */
	private PhysicalMachine getHostById(String id) {
		if (!this.machineMap.containsKey(id)) {
			throw new IllegalArgumentException(String.format(
					"No physical machine with ID %s found", id));
		}
		return this.machineMap.get(id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.machineMap, this.vmHostMap);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlacementMapping) {
			PlacementMapping that = (PlacementMapping) obj;
			return Objects.equal(this.machineMap, that.machineMap)
					&& Objects.equal(this.vmHostMap, that.vmHostMap);

		}
		return false;
	}

	@Override
	public String toString() {
		return "PlacementMapping [physicalMachines=" + getPhysicalMachines()
				+ "]";
	}

	/**
	 * Returns all physical machines.
	 *
	 * @return the physicalMachines
	 */
	public List<PhysicalMachine> getPhysicalMachines() {
		return Lists.newArrayList(this.machineMap.values());
	}

	public List<VirtualMachine> getVirtualMachines() {
		List<VirtualMachine> vms = new LinkedList<>();
		for (PhysicalMachine physicalMachine : getPhysicalMachines()) {
			vms.addAll(physicalMachine.getVirtualMachines());
		}
		return vms;
	}

	/**
	 * Returns the physical machine that hosts a given virtual machine. Throws
	 * an {@link IllegalArgumentException} if the given VM could not be found.
	 *
	 * @param vm
	 *            A virtual machine.
	 * @return The {@link PhysicalMachine} that hosts the vm.
	 */
	public PhysicalMachine getPhysicalMachineForVm(VirtualMachine vm) {
		if (!this.vmHostMap.containsKey(vm.getId())) {
			throw new IllegalArgumentException(String.format(
					"Could not find physical machine for VM %s", vm.getId()));

		}
		return this.vmHostMap.get(vm.getId());
	}
}
