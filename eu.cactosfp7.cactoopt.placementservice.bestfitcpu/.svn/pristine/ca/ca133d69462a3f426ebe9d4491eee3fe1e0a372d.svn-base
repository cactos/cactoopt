package eu.cactosfp7.cactoopt.placementservice.bestfitcpu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import eu.cactosfp7.cactoopt.models.PhysicalMachine;
import eu.cactosfp7.cactoopt.models.PhysicalMachineCpuComparator;
import eu.cactosfp7.cactoopt.models.VirtualMachineCpuComparator;
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
 * Best fit initial placement algorithm taking into account the number of allocated CPU cores
 * @author jakub
 *
 */
public class BestFitCpuPlacementAlgorithm implements InitialPlacementAlgorithm {

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(BestFitCpuPlacementAlgorithm.class.getName());
	
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
		
		Collections.sort(vms, Collections.reverseOrder(new VirtualMachineCpuComparator()));
		
		for (eu.cactosfp7.cactoopt.models.VirtualMachine vm : vms) {
			// Call actual placement algorithm
			PhysicalMachine pmToPlace = initialPlacementBestFit(pms, vm);
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

	private PhysicalMachine initialPlacementBestFit(List<PhysicalMachine> pms, eu.cactosfp7.cactoopt.models.VirtualMachine vm) {
		// Create a deep copy of physical machines list
		List<PhysicalMachine> pmsBestFit = new ArrayList<PhysicalMachine>();
		for (PhysicalMachine pm : pms) {
			if (pm.isPoweredOn()) // only physical machines that are powered on
				pmsBestFit.add(new PhysicalMachine(pm));
		}
		
		// Sort list in decreasing utilization order (CPU)
		Collections.sort(pmsBestFit, Collections.reverseOrder(new PhysicalMachineCpuComparator()));
		
		for (PhysicalMachine pm : pmsBestFit) {
			if (pm.assignVm(vm))
				return pm;
		}		
		
		return null;
	}
}