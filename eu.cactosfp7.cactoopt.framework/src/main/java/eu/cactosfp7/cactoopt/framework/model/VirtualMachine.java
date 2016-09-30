package eu.cactosfp7.cactoopt.framework.model;

import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;

/**
 * The model of a virtual machine in the placement optimization engine.
 *
 * <p>
 * This class is immutable.
 */
public class VirtualMachine {
	/**
	 * Id of virtual machine
	 */
	private final String id;
	/**
	 * Number of required CPU cores.
	 */
	private final int requiredCores;
	/**
	 * Amount of required memory in bytes.
	 */
	private final long requiredMemory;

	/**
	 * Creates virtual machine
	 *
	 * @param id
	 *            Id of virtual machine
	 * @param requiredCores
	 *            Number of assigned CPU cores
	 * @param requiredMemory
	 *            Amount of assigned memory
	 */
	public VirtualMachine(String id, int requiredCores, long requiredMemory) {
		this.id = id;
		this.requiredCores = requiredCores;
		this.requiredMemory = requiredMemory;
	}

	public String getId() {
		return this.id;
	}

	/**
	 * @return the requiredCores
	 */
	public int getRequiredCores() {
		return this.requiredCores;
	}

	/**
	 * @return the requiredMemory
	 */
	public long getRequiredMemory() {
		return this.requiredMemory;
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
		VirtualMachine other = (VirtualMachine) obj;
		if (this.id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!this.id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "VM [" + this.id + "]";
	}

}
