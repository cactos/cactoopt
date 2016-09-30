package eu.cactosfp7.cactoopt.models;
import java.util.ArrayList;
import java.util.List;

public class PhysicalMachine {
	/**
	 * Id of physical machine
	 */
	private String id;

	/**
	 * Total number of CPU cores 
	 */
	private int noCores;
	
	/**
	 * Number of utilized cores (assigned to virtual machines)
	 */
	private int utilizedCores;
	
	/**
	 * Total amount of memory
	 */
	private double totalMemory;
	
	/**
	 * Amount of utilized memory (assigned to virual machines)
	 */
	private double utilizedMemory;
	
	/**
	 * List of virtual machines assigned
	 */
	private List<VirtualMachine> vms;
	
	private boolean poweredOn;

	public String getId() {
		return id;
	}
	
	public int getNoCores() {
		return noCores;
	}

	public double getTotalMemory() {
		return totalMemory;
	}
	
	public List<VirtualMachine> getVms() {
		return vms;
	}
	
	public boolean isPoweredOn() {
		return poweredOn;
	}

	/**
	 * 
	 * @param id
	 * @param noCores
	 * @param memory
	 */
	public PhysicalMachine(String id, int noCores, double memory) {
		this.id = id;
		this.noCores = noCores;
		this.totalMemory = memory;
		this.vms = new ArrayList<VirtualMachine>();
	}
	
	/**
	 * 
	 * @param id
	 * @param noCores
	 * @param memory
	 * @param poweredOn
	 */
	public PhysicalMachine(String id, int noCores, double memory, boolean poweredOn) {
		this.id = id;
		this.noCores = noCores;
		this.totalMemory = memory;
		this.vms = new ArrayList<VirtualMachine>();
		this.poweredOn = poweredOn;
	}

	/**
	 * 
	 * @param id
	 * @param noCores
	 * @param utilizedCores
	 * @param totalMemory
	 * @param utilizedMemory
	 */
	public PhysicalMachine(String id, int noCores, int utilizedCores, double totalMemory, double utilizedMemory) {
		this.id = id;
		this.noCores = noCores;
		this.utilizedCores = utilizedCores;
		this.vms = new ArrayList<VirtualMachine>();
		this.totalMemory = totalMemory;
		this.utilizedMemory = utilizedMemory;
	}

	/**
	 * 
	 * @param id
	 * @param noCores
	 * @param utilizedCores
	 * @param totalMemory
	 * @param utilizedMemory
	 * @param poweredOn
	 */
	public PhysicalMachine(String id, int noCores, int utilizedCores, double totalMemory, double utilizedMemory, boolean poweredOn) {
		this.id = id;
		this.noCores = noCores;
		this.utilizedCores = utilizedCores;
		this.vms = new ArrayList<VirtualMachine>();
		this.totalMemory = totalMemory;
		this.utilizedMemory = utilizedMemory;
		this.poweredOn = poweredOn;
	}

	/**
	 * 
	 * @param pm
	 */
	public PhysicalMachine(PhysicalMachine pm) {
		this.id = pm.id;
		this.noCores = pm.noCores;
		this.utilizedCores = pm.utilizedCores;
		this.vms = new ArrayList<VirtualMachine>(pm.vms);
		this.totalMemory = pm.totalMemory;
		this.utilizedMemory = pm.utilizedMemory;
	}

	/**
	 * 
	 * @return
	 */
	public int getUtilizedCores() {
		return utilizedCores;
	}
	
	/**
	 * 
	 * @return Size of utilized memory 
	 */
	public double getUtilizedMemory() {
		return utilizedMemory;
	}
	
	/**
	 * 
	 * @return Ratio between utilized (assigned) CPU cores and total number of CPU cores 
	 */
	public double getCpuUtilization() {
		double cpuLoad = utilizedCores / (double) noCores;
		if (cpuLoad > 1)
			cpuLoad = 1;
		if (cpuLoad < 0)
			cpuLoad = 0;
		if (Double.isNaN(cpuLoad))
			cpuLoad = 0;
		
		return cpuLoad;
	}
	
	/**
	 * 
	 * @return Ratio between utilized (assigned) memory and total amount of memory
	 */
	public double getMemoryUtilization() {
		double memoryLoad = utilizedMemory / totalMemory;
		if (memoryLoad > 1)
			memoryLoad = 1;
		if (memoryLoad < 0)
			memoryLoad = 0;
		if (Double.isNaN(memoryLoad))
			memoryLoad = 0;
		
		return memoryLoad;
	}
	
	/**
	 * Assigns virtual machine to physical machine and assign necessary resources (CPU, memory)
	 * @param vm Virtual machine to assign
	 * @return
	 */
	public boolean assignVm(VirtualMachine vm) {
		if ((this.utilizedCores + vm.getNoCores() > this.noCores)
			|| (this.utilizedMemory + vm.getMemory() > this.totalMemory)) {
//			System.out.println("Not possible to place " + vm.id +" on " + this.id);
			return false;
		} else {
			vms.add(vm);
			this.utilizedCores += vm.getNoCores();
			this.utilizedMemory += vm.getMemory();
			return true;
		}
	}
	
	/**
	 * Unassigns virtual machine and releases resources (CPU, memory)
	 * @param vm Virtual machine to unassign
	 */
	public void unassignVm(VirtualMachine vm) {
		vms.remove(vm);
		this.utilizedCores -= vm.getNoCores();
		this.utilizedMemory -= vm.getMemory();
	}
	
	/**
	 * Returns the 
	 * @return
	 */
	public double getResidualEvaluation() {
		double cpuLoad = this.getCpuUtilization();		
		double memoryLoad = this.getMemoryUtilization();

		return this.noCores * 4 * (cpuLoad - memoryLoad) * (cpuLoad - memoryLoad) + (cpuLoad + memoryLoad) * (cpuLoad + memoryLoad);
	}
		
	@Override
	public String toString() {
		return "PhysicalMachine [id=" + this.id + ", numCores=" + this.noCores
				+ " (" + this.utilizedCores + " allocated)" + ", totalMemory="
				+ this.totalMemory + " (" + this.utilizedMemory
				+ " allocated), vms=" + this.vms + "]";
	}
	
}
