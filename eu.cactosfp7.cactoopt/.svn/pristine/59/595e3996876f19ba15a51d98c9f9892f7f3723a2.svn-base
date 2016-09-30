package eu.cactosfp7.cactoopt.algorithms.commons;

import java.util.List;

import eu.cactosfp7.cactoopt.models.PhysicalMachine;

/**
 * 
 * @author jakub
 *
 */
public class ConsolidationApproach {
	/**
	 * 
	 * @param pms
	 * @return
	 */
	public static double getEvaluationFunctionConsolidation(List<PhysicalMachine> pms) {
		double totalCost = 0;
		double residualMax = Double.MIN_VALUE;
		for (PhysicalMachine pm : pms) {
			double residualLocal;
			if (pm.getVms().size() > 0) {
				totalCost += pm.getNoCores();
				residualLocal = pm.getResidualEvaluation();
				if (residualLocal > residualMax) {
					residualMax = residualLocal;
				}
			}
		}
		return totalCost - residualMax;
	}

}
