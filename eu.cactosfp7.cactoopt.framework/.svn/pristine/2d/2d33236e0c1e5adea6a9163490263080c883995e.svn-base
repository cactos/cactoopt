package eu.cactosfp7.cactoopt.framework.model;

import eu.cactosfp7.cactoopt.framework.model.Migration;
import eu.cactosfp7.cactoopt.framework.model.MigrationMove;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;

/**
 * The model of a single migration move for a single {@link VirtualMachine} from
 * one {@link PhysicalMachine} to another, as modeled by {@link Migration}.
 * <p>
 * This class is immutable.
 */
public class MigrationMove {
	/**
	 * The virtual machine involved in the migration.
	 */
	private final VirtualMachine vm;

	/**
	 * The actual migration specification.
	 */
	private final Migration migration;

	/**
	 * Creates a new instance.
	 *
	 * @param vm
	 *            The virtual machine.
	 * @param migration
	 *            The migration (from one physical machine to another).
	 */
	public MigrationMove(VirtualMachine vm, Migration migration) {
		this.vm = vm;
		this.migration = migration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + this.vm.getId() + ": " + this.migration + "}";
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
		result = prime * result
				+ (this.migration == null ? 0 : this.migration.hashCode());
		result = prime * result + (this.vm == null ? 0 : this.vm.hashCode());
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
		MigrationMove other = (MigrationMove) obj;
		if (this.migration == null) {
			if (other.migration != null) {
				return false;
			}
		} else if (!this.migration.equals(other.migration)) {
			return false;
		}
		if (this.vm == null) {
			if (other.vm != null) {
				return false;
			}
		} else if (!this.vm.equals(other.vm)) {
			return false;
		}
		return true;
	}

	/**
	 * @return The virtual machine involved in the move.
	 */
	public VirtualMachine getVm() {
		return this.vm;
	}

	/**
	 * @return The migration itself, with source and destination hosts.
	 */
	public Migration getMigration() {
		return this.migration;
	}

}
