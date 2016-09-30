package eu.cactosfp7.cactoopt.algorithms.commons;

import java.util.List;

import eu.cactosfp7.cactoopt.models.PhysicalMachine;

/**
 * 
 * @author jakub
 *
 */
public class LoadBalancingApproach {

	/**
	 * 
	 * @param pms
	 * @param alpha
	 * @return
	 */
	public static double getEvaluationFunctionLoadBalancingMax(List<PhysicalMachine> pms, double alpha) {
		double minCpuLoad = Double.MAX_VALUE;
		double minMemoryLoad = Double.MAX_VALUE;
		
		for (PhysicalMachine pm : pms) {			
			double cpuLoad = pm.getCpuUtilization();
			if (cpuLoad < minCpuLoad) {
				minCpuLoad = cpuLoad;
			}

			double memoryLoad = pm.getMemoryUtilization();
			if (memoryLoad < minMemoryLoad) {
				minMemoryLoad = memoryLoad;
			}
		}
		return (1-alpha) * minCpuLoad + alpha * minMemoryLoad;
	}
	
	/**
	 * 
	 * @param pms
	 * @param alpha
	 * @return
	 */
	public static double getEvaluationFunctionLoadBalancing(List<PhysicalMachine> pms, double alpha) {
		double totalCpuLoad = 0.0;
		double totalMemoryLoad = 0.0;
		
		for (PhysicalMachine pm : pms) {			
			double cpuLoad = pm.getCpuUtilization();
			totalCpuLoad += cpuLoad;

			double memoryLoad = pm.getMemoryUtilization();
			totalMemoryLoad += memoryLoad;
		}
		
		double meanCpuLoad = totalCpuLoad / pms.size();
		double meanMemoryLoad = totalMemoryLoad / pms.size();
		
		double totalCpuLoadDiff = 0.0;
		double totalMemoryLoadDiff = 0.0;
		
		for (PhysicalMachine pm : pms) {			
			double cpuLoad = pm.getCpuUtilization();
			totalCpuLoadDiff += Math.abs(cpuLoad - meanCpuLoad);

			double memoryLoad = pm.getMemoryUtilization();
			totalMemoryLoadDiff += Math.abs(memoryLoad - meanMemoryLoad);
		}
		return (1-alpha) * totalCpuLoadDiff + alpha * totalMemoryLoadDiff;
	}
	
	/**
	 * 
	 * @param pms
	 * @param alpha
	 * @return
	 */
	public static double getEvaluationFunctionLoadBalancingMin(List<PhysicalMachine> pms, double alpha) {
		double maxCpuLoad = Double.MIN_VALUE;
		double maxMemoryLoad = Double.MIN_VALUE;
		
		for (PhysicalMachine pm : pms) {			
			double cpuLoad = pm.getCpuUtilization();
			if (cpuLoad > maxCpuLoad) {
				maxCpuLoad = cpuLoad;
			}

			double memoryLoad = pm.getMemoryUtilization();
			if (memoryLoad > maxMemoryLoad) {
				maxMemoryLoad = memoryLoad;
			}
		}
		return (1-alpha) * maxCpuLoad + alpha * maxMemoryLoad;
	}
}
