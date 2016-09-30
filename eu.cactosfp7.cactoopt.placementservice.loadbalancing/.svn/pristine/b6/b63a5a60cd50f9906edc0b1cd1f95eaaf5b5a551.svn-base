package eu.cactosfp7.cactoopt.placementservice.loadbalancing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import eu.cactosfp7.cactoopt.algorithms.commons.LoadBalancingApproach;
import eu.cactosfp7.cactoopt.models.PhysicalMachine;
import eu.cactosfp7.cactoopt.models.PhysicalMachineCpuComparator;
import eu.cactosfp7.cactoopt.models.PhysicalMachineMemoryComparator;
import eu.cactosfp7.cactoopt.models.VirtualMachineWeightComparator;
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
 * Load balancing initial placement algorithm
 * @author jakub
 *
 */
public class LoadBalancingPlacementAlgorithm implements InitialPlacementAlgorithm {
	
	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(LoadBalancingPlacementAlgorithm.class.getName());
	
	private double alpha = 1.0;
	
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
			PhysicalMachine pmToPlace = initialPlacementLoadBalancing(pms, vm);
			if (pmToPlace != null)
			{
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
		}
		
		log.info(successfullPlacements + " successfull placements");
		
		return plan;
	}

	private PhysicalMachine initialPlacementLoadBalancing(List<PhysicalMachine> pms, eu.cactosfp7.cactoopt.models.VirtualMachine vm) {
		PhysicalMachine targetPM = null;
		
//		double evaluationInit = LoadBalancingApproach.getEvaluationFunctionLoadBalancingMax(pms, alpha);
//		log.info("Init eval: " + evaluationInit);
		
		// Create a deep copy of physical machines list for all
		List<PhysicalMachine> pmsCpu = new ArrayList<PhysicalMachine>();
		List<PhysicalMachine> pmsMemory = new ArrayList<PhysicalMachine>();
		for (PhysicalMachine pm : pms) {
			if (pm.isPoweredOn()) { // only physical machines that are powered on
				pmsCpu.add(new PhysicalMachine(pm));
				pmsMemory.add(new PhysicalMachine(pm));
			}
		}
		
		// Sort lists in increasing utilization order
		Collections.sort(pmsCpu, new PhysicalMachineCpuComparator());
		Collections.sort(pmsMemory, new PhysicalMachineMemoryComparator());
		
		PhysicalMachine pmCpu = null;
		double evaluationCpu = Double.MIN_VALUE;
		for (PhysicalMachine pm : pmsCpu) {
			if (pm.assignVm(vm)) {
				pmCpu = pm;
				evaluationCpu = LoadBalancingApproach.getEvaluationFunctionLoadBalancingMax(pmsCpu, alpha);
				log.info(pmCpu.getId() + " (CPU) eval: " + evaluationCpu);
				break;
			}
		}
		
		PhysicalMachine pmMemory = null;
		double evaluationMemory = Double.MIN_VALUE;
		for (PhysicalMachine pm : pmsMemory) {
			if (pm.assignVm(vm)) {
				pmMemory = pm;
				evaluationMemory = LoadBalancingApproach.getEvaluationFunctionLoadBalancingMax(pmsMemory, alpha);
				log.info(pmMemory.getId() +" (memory) eval: " + evaluationMemory);
				break;
			}
		}
		
		if ((pmCpu != null) && (evaluationCpu >= evaluationMemory)) {
			log.info("Place " + vm.getId() + " on " + pmCpu.getId());
			for (PhysicalMachine pm : pms) {
				if (pm.getId() == pmCpu.getId()) {
					pm.assignVm(vm);
					targetPM = pm;
				}
			}
		}
		
		if ((pmMemory != null) && (evaluationMemory > evaluationCpu))  {
			log.info("Place " + vm.getId() + " on " + pmMemory.getId());
			for (PhysicalMachine pm : pms) {
				if (pm.getId() == pmMemory.getId()) {
					pm.assignVm(vm);
					targetPM = pm;
				}
			}
		}
		
		return targetPM;
	}
}