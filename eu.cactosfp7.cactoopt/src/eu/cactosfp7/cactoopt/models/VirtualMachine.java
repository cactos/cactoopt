package eu.cactosfp7.cactoopt.models;

public class VirtualMachine {
	/**
	 * Id of virtual machine
	 */
	private String id;
	/**
	 * Number of assigned CPU cores 
	 */
	private int noCores;
	/**
	 * Amount of assigned memory 
	 */
	private double memory;
	
	/**
	 * Creates virtual machine
	 * @param id Id of virtual machine
	 * @param noCores Number of assigned CPU cores
	 * @param memory Amount of assigned memory 
	 */
	public VirtualMachine(String id, int noCores, double memory) {
		this.id = id;
		this.noCores = noCores;
		this.memory = memory;
	}

	public String getId() {
		return id;
	}

	public int getNoCores() {
		return noCores;
	}

	public double getMemory() {
		return memory;
	}
	
	@Override
	public String toString() {
		return "VirtualMachine [id=" + this.id + ", numCores=" + this.noCores
				 + ", memory=" + this.memory + "]";
	}
}
