package eu.cactosfp7.cactoopt.framework.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;
import eu.cactosfp7.cactoopt.framework.model.exceptions.CoresExhaustedException;
import eu.cactosfp7.cactoopt.framework.model.exceptions.MemoryExhaustedException;
import eu.cactosfp7.cactoopt.framework.model.exceptions.PlacementException;

/**
 * The model of a physical machine in the placement optimization engine.
 *
 * <p>
 * This class is not immutable, since the list of VMs can be altered. The class
 * offers a {@link #copy()} method that returns a copy of the object, which has
 * its own backing {@link ArrayList} of VMs. The copy's list can therefore be
 * modified without risk of modifying the original's.
 */
public final class PhysicalMachine {

	/**
	 * Id of physical machine
	 */
	private final String id;

	/**
	 * Total number of CPU cores
	 */
	private final int numCores;

	/**
	 * Total amount of memory, in bytes.
	 */
	private final long totalMemory;

	/**
	 * List of virtual machines assigned
	 */
	private final List<VirtualMachine> vms;

	/**
	 * Creates a new instance.
	 *
	 * @param id
	 *            The ID of the physical machine.
	 * @param numCores
	 *            The number of available CPU cores.
	 * @param memory
	 *            The amount of available memory, in bytes.
	 */
	public PhysicalMachine(String id, int numCores, long memory) {
		this(id, numCores, memory, new ArrayList<VirtualMachine>());
	}

	private PhysicalMachine(String id, int numCores, long memory,
			List<VirtualMachine> vms) {
		this.id = id;
		this.numCores = numCores;
		this.totalMemory = memory;
		this.vms = new ArrayList<VirtualMachine>(vms);
	}

	/**
	 * @return the numCores
	 */
	public int getNumCores() {
		return this.numCores;
	}

	/**
	 * @return the totalMemory
	 */
	public long getTotalMemory() {
		return this.totalMemory;
	}

	/**
	 * @return the vms
	 */
	public List<VirtualMachine> getVirtualMachines() {
		return this.vms;
	}

	/**
	 * @return The unique identifier of this {@link PhysicalMachine}.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return A shallow copy of this {@link PhysicalMachine}. A new list of
	 *         virtual machines is created, so modifications to the copy's list
	 *         will not affect the original. The {@link VirtualMachine} elements
	 *         themselves are immutable.
	 */
	public PhysicalMachine copy() {
		return new PhysicalMachine(this.id, this.numCores, this.totalMemory,
				new ArrayList<VirtualMachine>(this.vms));
	}

	/**
	 * @return Number of utilized CPU cores.
	 */
	public int getUtilizedCores() {
		int sum = 0;
		for (VirtualMachine vm : this.vms) {
			sum += vm.getRequiredCores();
		}
		return sum;
	}

	/**
	 * @return The utilized memory, in bytes.
	 */
	public long getUtilizedMemory() {
		long sum = 0;
		for (VirtualMachine vm : this.vms) {
			sum += vm.getRequiredMemory();
		}
		return sum;
	}

	/**
	 * Determine if a {@link VirtualMachine} can fit on this
	 * {@link PhysicalMachine} or not.
	 *
	 * @param vm
	 *            The VM to check.
	 * @return true if the given VM can fit, given the current amount of
	 *         available resources, false otherwise.
	 */
	public boolean canFit(VirtualMachine vm) {
		Preconditions.checkArgument(!this.vms.contains(vm), String.format(
				"VM %s already hosted on %s", vm.getId(), getId()));
		return canFitCores(vm) && canFitMemory(vm);
	}

	/**
	 * @return The utilization percentage of number of cores.
	 */
	public double getCoreUtilizationPercentage() {
		return (double) getUtilizedCores() / this.numCores;
	}

	/**
	 * @return The memory utilization percentage.
	 */
	public double getMemoryUtilizationPercentage() {
		return (double) getUtilizedMemory() / this.totalMemory;
	}

	/**
	 * Assign the {@link VirtualMachine} to this {@link PhysicalMachine} and
	 * decrease the number of available resources from the
	 * {@link PhysicalMachine} according to the requirements of the
	 * {@link VirtualMachine}.
	 *
	 * @param vm
	 *            The VM to assign to this physical machine.
	 * @throws CoresExhaustedException
	 *             Thrown if there are not enough CPU cores available.
	 * @throws MemoryExhaustedException
	 *             Thrown if there is not enough free memory available.
	 */
	public void assignVm(VirtualMachine vm) throws CoresExhaustedException,
			MemoryExhaustedException {
		Preconditions.checkArgument(!this.vms.contains(vm), String.format(
				"VM %s already hosted on %s", vm.getId(), getId()));

		if (!canFitCores(vm)) {
			throw new CoresExhaustedException(String.format(
					"VM required %d cores, only %d available",
					vm.getRequiredCores(), getAvailableCores()));
		}

		if (!canFitMemory(vm)) {
			throw new MemoryExhaustedException(String.format(
					"VM required %d bytes of memory, only %d available",
					vm.getRequiredMemory(), getAvailableMemory()));
		}

		this.vms.add(vm);
	}

	/**
	 * Unassigns a {@link VirtualMachine} and reclaims the resources that it
	 * used.
	 *
	 * @param vm
	 *            Virtual machine to unassign.
	 */
	public void unassignVm(VirtualMachine vm) {
		Preconditions.checkState(this.vms.contains(vm),
				"Asked to remove non-assigned VM %s from %s", vm.getId(),
				this.id);
		this.vms.remove(vm);
	}

	private boolean canFitMemory(VirtualMachine vm) {
		return getAvailableMemory() >= vm.getRequiredMemory();
	}

	private double getAvailableMemory() {
		return this.totalMemory - getUtilizedMemory();
	}

	private boolean canFitCores(VirtualMachine vm) {
		return getAvailableCores() >= vm.getRequiredCores();
	}

	private int getAvailableCores() {
		return this.numCores - getUtilizedCores();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PhysicalMachine [id=" + this.id + ", numCores=" + this.numCores
				+ " (" + getAvailableCores() + " available)" + ", totalMemory="
				+ this.totalMemory + " (" + getAvailableMemory()
				+ " available), vms=" + this.vms + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.id == null ? 0 : this.id.hashCode());
		result = prime * result + this.numCores;
		long temp;
		temp = Double.doubleToLongBits(this.totalMemory);
		result = prime * result + (int) (temp ^ temp >>> 32);
		result = prime * result + (this.vms == null ? 0 : this.vms.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PhysicalMachine other = (PhysicalMachine) obj;
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		if (this.numCores != other.numCores) {
			return false;
		}
		if (Double.doubleToLongBits(this.totalMemory) != Double
				.doubleToLongBits(other.totalMemory)) {
			return false;
		}
		if (this.vms == null) {
			if (other.vms != null) {
				return false;
			}
		} else if (!this.vms.equals(other.vms)) {
			return false;
		}
		return true;
	}

}
