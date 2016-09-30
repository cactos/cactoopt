package eu.cactosfp7.cactoopt.placementservice.consolidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import eu.cactosfp7.cactoopt.models.*;
import eu.cactosfp7.cactoopt.algorithms.commons.ConsolidationApproach;
import eu.cactosfp7.cactoopt.placementservice.InitialPlacementAlgorithm;
import eu.cactosfp7.cactoopt.util.CDOModelHelper;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.OptimisationPlan;

/**
 * Consolidation initial placement algorithm
 * @author jakub
 *
 */
public class ConsolidationPlacementAlgorithm implements InitialPlacementAlgorithm {

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(ConsolidationPlacementAlgorithm.class.getName());
	
	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm,
			LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm,
			List<VirtualMachine> vmsToPlace) {

		List<PhysicalMachine> pms = CDOModelHelper.getPhysicalMachinesFromCdoModel(pdcm, ldcm);
		
		log.info("Initial DC configuration");
		for(PhysicalMachine pm : pms) {
			log.info(pm.toString());
		}		
		
		// Create optimisation plan object
		OptimisationPlan plan = CDOModelHelper.createOptimisationPlan();
		
		int successfullPlacements = 0;

		List<eu.cactosfp7.cactoopt.models.VirtualMachine> vms = CDOModelHelper.transformVirtualMachineToSimpleModel(vmsToPlace);
		
		Collections.sort(vms, new VirtualMachineWeightComparator());
		
		for (eu.cactosfp7.cactoopt.models.VirtualMachine vm : vms) {
			PhysicalMachine pmToPlace = initialPlacementConsolidation(pms, vm);
			if (pmToPlace != null)
				successfullPlacements++;
			
			log.info("After placement");
			for(PhysicalMachine pm : pms) {
				log.info(pm.toString());
			}		
			
			// Add initial placement action to optimisation plan
			VirtualMachine vmToPlace = CDOModelHelper.getVirtualMachineById(vm.getId(), vmsToPlace);
			Hypervisor hToPlace = CDOModelHelper.getComputeNodeById(pmToPlace.getId(), pdcm).getHypervisor();
			CDOModelHelper.addInitialPlacementActionToOptimisationPlan(plan, vmToPlace, hToPlace);
		}
		
		log.info(successfullPlacements + " successfull placements");
		
		return plan;
	}

	private PhysicalMachine initialPlacementConsolidation(List<PhysicalMachine> pms,  eu.cactosfp7.cactoopt.models.VirtualMachine vm) {
		// Create a deep copy of physical machines list 
		List<PhysicalMachine> pmsConsolidation = new ArrayList<PhysicalMachine>();
		for (PhysicalMachine pm : pms) {
			if (pm.isPoweredOn()) // only physical machines that are powered on
				pmsConsolidation.add(new PhysicalMachine(pm));
		}
		
		// Sort physical machines according to the residual capacity (smallest residual capacity first)
		Collections.sort(pmsConsolidation, new PhysicalMachineResidualComparator());
		
		for (PhysicalMachine pmConsolidation : pmsConsolidation) {
			if (pmConsolidation.assignVm(vm)) {
				double evaluation = ConsolidationApproach.getEvaluationFunctionConsolidation(pmsConsolidation);
				log.info(pmConsolidation.getId() +" eval: " + evaluation);
				
				log.info("Place " + vm.getId() + " on " + pmConsolidation.getId());
				
				// Do actual placement
				for (PhysicalMachine pm : pms) {
					if (pm.getId() == pmConsolidation.getId()) {
						pm.assignVm(vm);
						return pm; // managed to place VM on PM, no need to continue
					}
				}
			} else {
				log.info("Impolsible to place " + vm.getId() + " on " + pmConsolidation.getId());
			}
		}

		return null;
	}
	
}
