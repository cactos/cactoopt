package eu.cactosfp7.cactoopt.optimisationservice.resourcecontrol.json;
import java.util.List;


public class ResourceControlResponse {
	private List<ResponseServer> servers;

	public void print() {
		System.out.println("Response: ");

		for (ResponseServer server : servers) {
			System.out.println("\tServer: [" + server.id + "]");

			for (ResourceControlResponse.ResponseServer.ResponseVirtualMachine vm : server.virtualMachines) {
				System.out.println("\t\tVirtualMachine: [" + vm.id + ", " + vm.capacity + "]");
			}
		}
	}

	private class ResponseServer {
		private String id;
		private List<ResponseVirtualMachine> virtualMachines;

		private class ResponseVirtualMachine {
			private String id;
			private int capacity;
		}
	}
}
