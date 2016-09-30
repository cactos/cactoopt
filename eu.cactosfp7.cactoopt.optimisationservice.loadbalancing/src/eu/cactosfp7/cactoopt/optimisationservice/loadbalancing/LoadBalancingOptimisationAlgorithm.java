package eu.cactosfp7.cactoopt.optimisationservice.loadbalancing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import eu.cactosfp7.cactoopt.algorithms.commons.LoadBalancingApproach;
import eu.cactosfp7.cactoopt.models.PhysicalMachine;
import eu.cactosfp7.cactoopt.models.PhysicalMachineCpuComparator;
import eu.cactosfp7.cactoopt.models.PhysicalMachineMemoryComparator;
import eu.cactosfp7.cactoopt.models.VirtualMachineMemoryComparator;
import eu.cactosfp7.cactoopt.models.VirtualMachineMigrationAction;
import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
import eu.cactosfp7.cactoopt.util.CDOModelHelper;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMemory;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualProcessingUnit;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.AbstractNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.MemorySpecification;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.NodeState;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ProcessingUnitSpecification;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.Rack;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationplanFactory;
import eu.cactosfp7.optimisationplan.SequentialSteps;

/**
 * Load balancing virtual machine migration algorithm
 * @author jakub
 *
 */
public class LoadBalancingOptimisationAlgorithm implements IOptimisationAlgorithm {
	
	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(LoadBalancingOptimisationAlgorithm.class.getName());
	
	/** The weight of CPU over memory (used for sorting Virtual Machines)*/
	double alpha = 0.5;
	
	public LoadBalancingOptimisationAlgorithm() {
	}

	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm,
			LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm) {
			
			log.info("LoadBalancing algorithm starts");
		
		// Create optimisation plan object
//		OptimisationPlan plan = CDOModelHelper.createOptimisationPlan();

			
			OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
			SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
			plan.setOptimisationStep(rootStep);
			rootStep.setOptimisationPlan(plan);
			rootStep.setExecutionStatus(ExecutionStatus.READY);
			plan.setCreationDate(new Date());
		
//		List<PhysicalMachine> pms = CDOModelHelper.getPhysicalMachinesFromCdoModel(pdcm, ldcm);
			List<PhysicalMachine> pms = this.getPhysicalMachinesFromCdoModel(pdcm, ldcm);
			
		VirtualMachineMigrationAction migrationSuggested = null;
		double evaluation;
		
		log.info("Initial DC state");
		for(PhysicalMachine pm : pms) {
			log.info(pm.toString());
		}	
		
		do
		{
//			evaluation = LoadBalancingApproach.getEvaluationFunctionLoadBalancingMax(pms, alpha);
			evaluation = LoadBalancingApproach.getEvaluationFunctionLoadBalancing(pms, alpha);
			migrationSuggested = migrationLoadBalancing(pms);
			
			log.info("Initial eval: " + evaluation);
			
			if (migrationSuggested != null) {
				VirtualMachine vmToMigrate = CDOModelHelper.getVirtualMachineById(migrationSuggested.getVm().getId(), ldcm);
				Hypervisor sourceHypervisor = CDOModelHelper.getComputeNodeById(migrationSuggested.getSource().getId(), pdcm).getHypervisor();
				Hypervisor destinationHypervisor = CDOModelHelper.getComputeNodeById(migrationSuggested.getTarget().getId(), pdcm).getHypervisor();
				CDOModelHelper.addMigrationActionToOptimisationPlan(plan, vmToMigrate, sourceHypervisor, destinationHypervisor);
				
				log.info("After migration");
				for(PhysicalMachine pm : pms) {
					log.info(pm.toString());
				}	
			}
		} while (migrationSuggested != null);
		
		plan.setCreationDate(new Date());
		return plan;
	}

	private VirtualMachineMigrationAction migrationLoadBalancing(List<PhysicalMachine> pms) {
		VirtualMachineMigrationAction migration = null;
//		double evaluationOfCurrentState = LoadBalancingApproach.getEvaluationFunctionLoadBalancingMax(pms, alpha);
		double evaluationOfCurrentState = LoadBalancingApproach.getEvaluationFunctionLoadBalancing(pms, alpha);
		
		// Create a deep copy of physical machines list
		List<PhysicalMachine> pmsCpu = new ArrayList<PhysicalMachine>();
		List<PhysicalMachine> pmsMemory = new ArrayList<PhysicalMachine>();
//		int totalCpuCores = 0; // Total number of CPU cores in the whole DC
//		double totalMemory = 0; // Total size of memory in the whole DC
//		int assignedCpuCores = 0; // Total number of CPU cores assigned to virtual machines (in the whole DC)
//		double assignedMemory = 0; // Total size of memory assigned to virtual machines (in the whole DC)
//		
		for (PhysicalMachine pm : pms) {

			pmsCpu.add(new PhysicalMachine(pm));
			pmsMemory.add(new PhysicalMachine(pm));
//			
//			totalCpuCores += pm.getNoCores();
//			totalMemory += pm.getTotalMemory();
//			
//			assignedCpuCores += pm.getUtilizedCores();
//			assignedMemory += pm.getUtilizedMemory();
		}
		
//		double meanCpuUtilization = assignedCpuCores / (double) totalCpuCores;
//		double meanMemoryUtilization = assignedMemory / totalMemory;
		
		// Sort lists in increasing utilization order
		Collections.sort(pmsCpu, new PhysicalMachineCpuComparator());
		Collections.sort(pmsMemory, new PhysicalMachineMemoryComparator());
		
		int lastIndex = pms.size()-1; 
		
		if(lastIndex<=0) {
		    return null;
		}
		PhysicalMachine pmCpuMin = pmsCpu.get(0);
		PhysicalMachine pmCpuMax = pmsCpu.get(lastIndex);
//		double cpuUtilizationDiffrence = pmCpuMax.getCpuUtilization() - pmCpuMin.getCpuUtilization(); 
//		Collections.sort(pmCpuMax.getVms(), new VirtualMachineCpuComparator());
		
//		List<eu.cactosfp7.cactoopt.models.VirtualMachine> vms = pmCpuMax.getVms();
		
//		for (eu.cactosfp7.cactoopt.VirtualMachine vm : vms) {
//			if (vm.get)
//		}
		
		if (pmCpuMax.getVms().size() == 0) {
			log.info(pmCpuMax.getId() + " doesn't host any VMs");
			return null;
		}

		eu.cactosfp7.cactoopt.models.VirtualMachine vmCpu = pmCpuMax.getVms().get(0);
		double evaluationCpu = Double.MIN_VALUE;
		
		if (pmCpuMin.assignVm(vmCpu)) {
			pmCpuMax.unassignVm(vmCpu);
//			evaluationCpu = LoadBalancingApproach.getEvaluationFunctionLoadBalancingMax(pmsCpu, alpha);
			evaluationCpu = LoadBalancingApproach.getEvaluationFunctionLoadBalancing(pmsCpu, alpha);
			log.info(pmCpuMin.getId() + " (CPU) eval: " + evaluationCpu);
		}
		
		PhysicalMachine pmMemoryMin = pmsMemory.get(0);
		PhysicalMachine pmMemoryMax = pmsMemory.get(lastIndex);
		Collections.sort(pmMemoryMax.getVms(), new VirtualMachineMemoryComparator());
		eu.cactosfp7.cactoopt.models.VirtualMachine vmMemory = pmMemoryMax.getVms().get(0);
		double evaluationMemory = Double.MIN_VALUE;
		
		if (pmMemoryMin.assignVm(vmMemory)) {
			pmMemoryMax.unassignVm(vmMemory);
//			evaluationMemory = LoadBalancingApproach.getEvaluationFunctionLoadBalancingMax(pmsMemory, alpha);
			evaluationMemory = LoadBalancingApproach.getEvaluationFunctionLoadBalancing(pmsMemory, alpha);
			log.info(pmMemoryMin.getId() +" (memory) eval: " + evaluationMemory);
		}
		
		if ((evaluationCpu < evaluationOfCurrentState) && (evaluationCpu < evaluationMemory)) {
			log.info("Migrate " + vmCpu.getId() + " from " + pmCpuMax.getId() + " to " + pmCpuMin.getId());
			migration = new VirtualMachineMigrationAction(vmCpu, pmCpuMax, pmCpuMin);
			for (PhysicalMachine pm : pms) {
				if (pm.getId() == pmCpuMin.getId()) {
					pm.assignVm(vmCpu);
				}
				if (pm.getId() == pmCpuMax.getId()) {
					pm.unassignVm(vmCpu);
				}
			}
		} else if (evaluationMemory < evaluationOfCurrentState) {
			log.info("Migrate " + vmMemory.getId() + " from " + pmMemoryMax.getId() + " to " + pmMemoryMin.getId());
			migration = new VirtualMachineMigrationAction(vmCpu, pmMemoryMax, pmMemoryMin);
			for (PhysicalMachine pm : pms) {
				if (pm.getId() == pmMemoryMin.getId()) {
					pm.assignVm(vmMemory);
				}
				if (pm.getId() == pmMemoryMax.getId()) {
					pm.unassignVm(vmMemory);
				}
			}
		} else {
			log.info("No migration");
		}
		
		return migration;
	}
	
	/**
	 * Transforms Infrastructure Models into list of physical machines with assigned virtual machines
	 * @param pdcm Physical Data Center Model
	 * @param ldcm Logical Data Center Model
	 * @return List of all physical machines with assigned virtual machines (simplified model)
	 */
	public List<PhysicalMachine> getPhysicalMachinesFromCdoModel(PhysicalDCModel pdcm, LogicalDCModel ldcm) {
		List<PhysicalMachine> pms = new ArrayList<PhysicalMachine>();
		
		for(Rack rack : pdcm.getRacks()) {
			for(AbstractNode node : rack.getNodes()) {
				if (node instanceof ComputeNode) {
					ComputeNode computeNode = (ComputeNode) node;
					
//					if (computeNode.getState() == NodeState.RUNNING) {
						
						String id = computeNode.getId();
						int noCores = 0;
						double totalMemory = 0;
						
						for (ProcessingUnitSpecification pus : computeNode.getCpuSpecifications()) {
							noCores += pus.getNumberOfCores();
						}
						for (MemorySpecification ms : computeNode.getMemorySpecifications()) {
	//						if (ms.getSize() != null)
							try {
								totalMemory += ms.getSize().getEstimatedValue();
							} catch (Exception ex) {
								
							}
						}
						
						PhysicalMachine pm = new PhysicalMachine(id, noCores, totalMemory);
						
						for (Hypervisor h : ldcm.getHypervisors()) {
							if (h.getNode() != null) {
								if (h.getNode().getId().equals(id)) {
									for (VirtualMachine vm : h.getVirtualMachines()) {
										String vmId = vm.getId();
										int vmNoCores = 0;
										
										for(VirtualProcessingUnit vProcessingUnit : vm.getVirtualProcessingUnits()) {
										    vmNoCores += vProcessingUnit.getVirtualCores();
										}
										
										double vmMemory = 0;
										
										for (VirtualMemory vmem : vm.getVirtualMemoryUnits()) {
	//										if (vmem.getProvisioned() != null)
											try {
												vmMemory += vmem.getProvisioned().getEstimatedValue();
											} catch (Exception ex) {
												
											}
										}
										eu.cactosfp7.cactoopt.models.VirtualMachine vmToAssign = new eu.cactosfp7.cactoopt.models.VirtualMachine(vmId, vmNoCores, vmMemory);
										pm.assignVm(vmToAssign);
									}
								}
							} else {
								log.info("Hypervisor " + h.getId() + " is not linked with any compute node! Problems with importing virtual machines may occur.");
							}
						}
						
						pms.add(pm);
//					} else {
//						log.info("ComputeNode " + computeNode.getName() + " is not running");
//					}
				}
			}	
		}
		
		return pms;
	}

}
