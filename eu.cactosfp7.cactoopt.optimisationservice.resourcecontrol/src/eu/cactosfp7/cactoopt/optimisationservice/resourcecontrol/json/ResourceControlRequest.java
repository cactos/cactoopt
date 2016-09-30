package eu.cactosfp7.cactoopt.optimisationservice.resourcecontrol.json;
import java.util.ArrayList;
import java.util.List;

public class ResourceControlRequest {
	private List<RequestServer> servers;

	public ResourceControlRequest() {
		super();
		this.servers = new ArrayList<RequestServer>();
	}

	public void addServer(RequestServer server) {
		this.servers.add(server);
	}
}
