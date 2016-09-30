package eu.cactosfp7.cactoopt.framework.util;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;

/**
 * Utilities related to {@link PlacementMapping}.
 */
public class PlacementMappingUtils {

	/**
	 * More compact representation than the {@link PlacementMapping#toString()}
	 * method.
	 * 
	 * @param mapping
	 *            The mapping to represent.
	 * @return A compact representation of the {@link PlacementMapping}.
	 */
	public static String representMapping(PlacementMapping mapping) {
		StringBuilder sb = new StringBuilder();

		sb.append("PlacementMapping [");

		for (int i = 0; i < mapping.getPhysicalMachines().size(); i++) {
			PhysicalMachine host = mapping.getPhysicalMachines().get(i);

			sb.append("[");
			sb.append(host.getId());
			sb.append(": ");
			sb.append(host.getNumCores() - host.getUtilizedCores());
			sb.append("/");
			sb.append(host.getNumCores());
			sb.append("c, ");
			sb.append(host.getTotalMemory() - host.getUtilizedMemory());
			sb.append("/");
			sb.append(host.getTotalMemory());
			sb.append("m, vms=[");
			for (int vmIndex = 0; vmIndex < host.getVirtualMachines().size(); vmIndex++) {
				VirtualMachine vm = host.getVirtualMachines().get(vmIndex);

				sb.append(vm.getId());
				sb.append(": ");
				sb.append(vm.getRequiredCores());
				sb.append("c/");
				sb.append(vm.getRequiredMemory());
				sb.append("m");
				if (vmIndex != host.getVirtualMachines().size() - 1) {
					sb.append(", ");
				}
			}
			sb.append("]]");

			if (i != mapping.getPhysicalMachines().size() - 1) {
				sb.append(", ");
			}
		}

		sb.append("]");

		return sb.toString();
	}

}
