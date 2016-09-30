package eu.cactosfp7.cactoopt.models;


/**
 * Action of virtual machine migration
 * @author jakub
 *
 */
public class VirtualMachineMigrationAction {
	/**
	 * Virtual machine that is migrated
	 */
	private VirtualMachine vm;

	/**
	 * Physical machine that currently hosts virtual machine to migrate
	 */
	private PhysicalMachine source;
	/**
	 * Physical machine to migrate virtual machine to
	 */
	private PhysicalMachine target;
	
	/**
	 * 
	 * @param vm Virtual machine that is migrated
	 * @param source Physical machine that currently hosts virtual machine to migrate
	 * @param target Physical machine to migrate virtual machine to
	 */
	public VirtualMachineMigrationAction(VirtualMachine vm, PhysicalMachine source, PhysicalMachine target) {
		this.vm = vm;
		this.source = source;
		this.target = target;
	}
	
	public VirtualMachine getVm() {
		return vm;
	}

	public PhysicalMachine getSource() {
		return source;
	}

	public PhysicalMachine getTarget() {
		return target;
	}
}
