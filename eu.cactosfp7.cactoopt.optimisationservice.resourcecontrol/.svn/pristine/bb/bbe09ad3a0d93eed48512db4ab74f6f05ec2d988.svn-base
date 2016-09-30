package eu.cactosfp7.cactoopt.optimisationservice.resourcecontrol.json;
import java.util.ArrayList;
import java.util.List;

public class RequestServer {
	private String id;
	private int totalCores;
	private List<RequestVirtualMachine> virtualMachines;

	public RequestServer(String id, int totalCores) {
		super();
		this.id = id;
		this.totalCores = totalCores;
		this.virtualMachines = new ArrayList<RequestVirtualMachine>();
	}

	public void addVirtualMachine(RequestVirtualMachine vm) {
		this.virtualMachines.add(vm);
	}
}
