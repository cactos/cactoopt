package eu.cactosfp7.cactoopt.optimisationservice.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PuMeasurement;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.NodeState;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ProcessingUnitSpecification;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationplanFactory;
import eu.cactosfp7.optimisationplan.SequentialSteps;
import eu.cactosfp7.optimisationplan.VmMigrationAction;

public class RandomOptimisationAlgorithm implements IOptimisationAlgorithm {

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(RandomOptimisationAlgorithm.class.getName());
	
	public RandomOptimisationAlgorithm() {
		log.info("Created:RandomOptimisationAlgorithm");
	}
	
	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm,
			LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm) {
		
		log.info("starting RandomOptimisationAlgorithm... ");
		
		OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
		SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
		plan.setOptimisationStep(rootStep);
		rootStep.setOptimisationPlan(plan);
		
		boolean anyPm = false;
		List<ComputeNode> pMsOverUtilizedCpu = new ArrayList<ComputeNode>();
		List<ProcessingUnitSpecification> overUtlizedPus = new ArrayList<ProcessingUnitSpecification>();
		List<ProcessingUnitSpecification> underUtlizedPus = new ArrayList<ProcessingUnitSpecification>();
		double maxCpuUtilization = 0;
		double minCpuUtilization = 0;
		List<ComputeNode> pMsUnderUtilizedMemory = new ArrayList<ComputeNode>();
		List<ComputeNode> pMsUnderUtilizedCpu = new ArrayList<ComputeNode>();
		
		for (PuMeasurement cpuMeasurement : plm.getCpuMeasurement()) {
			double utilization = cpuMeasurement.getUtilization().getValue().getEstimatedValue();
			
			if (utilization > maxCpuUtilization) {
				ProcessingUnitSpecification cpu = cpuMeasurement.getObservedPu();
				ComputeNode pm = (ComputeNode)cpu.getNode();
				pMsOverUtilizedCpu.add(pm);
				overUtlizedPus.add(cpu);
				log.info("\t" + pm.toString());
				log.info("\t\tCPU: " + cpu.toString());
				log.info("\t\t\tutilization: " + (utilization * 100) + "%");
				anyPm = true;
			}
			
			if (utilization < minCpuUtilization) {
				ProcessingUnitSpecification cpu = cpuMeasurement.getObservedPu();
				ComputeNode pm = (ComputeNode)cpu.getNode();
				pMsUnderUtilizedCpu.add(pm);
				underUtlizedPus.add(cpu);
				log.info("\t" + pm.toString());
				log.info("\t\tCPU: " + cpu.toString());
				log.info("\t\t\tutilization: " + (utilization * 100) + "%");
				anyPm = true;
			}
		}
		
		List<Hypervisor> potentialMigrationTargets = new ArrayList<Hypervisor>();
		
		log.info("PMs to host migrated VM:");
		for (Hypervisor h : ldcm.getHypervisors()) {
			ComputeNode pm = h.getNode();
			if (pm.getState() == NodeState.RUNNING && pMsUnderUtilizedMemory.contains(pm) && pMsUnderUtilizedCpu.contains(pm)) {
				log.info("\t" + h.getNode().toString());
				potentialMigrationTargets.add(h);
			}
		}
		
		Random ran = new Random();
		for(ComputeNode cn : pMsOverUtilizedCpu) {
			Hypervisor source = cn.getHypervisor();
			List<VirtualMachine> vms = source.getVirtualMachines();
			if(vms.size() == 0) continue;
			VirtualMachine vm = vms.get(ran.nextInt(vms.size()));
			//ran.nextInt(potentialMigrationTargets.size())
			if(potentialMigrationTargets.size() == 0) continue;
			Hypervisor target = potentialMigrationTargets.get(0);
			
			VmMigrationAction migration = OptimisationplanFactory.eINSTANCE.createVmMigrationAction();
			migration.setMigratedVm(vm);
			migration.setSourceHost(source);
			migration.setTargetHost(target);
			migration.setSequentialSteps(rootStep);
			
			vm.setHypervisor(target);
		}
			
		return plan;
	}
}