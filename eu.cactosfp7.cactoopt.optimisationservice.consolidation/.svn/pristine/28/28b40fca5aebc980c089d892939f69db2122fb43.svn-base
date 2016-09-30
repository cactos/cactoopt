package eu.cactosfp7.cactoopt.optimisationservice.consolidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import eu.cactosfp7.cactoopt.models.PhysicalMachine;
import eu.cactosfp7.cactoopt.models.PhysicalMachineResidualComparator;
import eu.cactosfp7.cactoopt.models.VirtualMachineMigrationAction;
import eu.cactosfp7.cactoopt.models.VirtualMachineWeightComparator;
import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
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
import eu.cactosfp7.optimisationplan.VmMigrationAction;

/**
 * Consolidation virtual machine migration algorithm
 * @author jakub
 *
 */
public class ConsolidationOptimisationAlgorithm implements IOptimisationAlgorithm {

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(ConsolidationOptimisationAlgorithm.class.getName());
	
	public ConsolidationOptimisationAlgorithm() {
	}

	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm,
			LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm) {
		
		log.info("Consolidation algorithm starts");
		
//		CDOModelHelper helper = new CDOModelHelper();
		
		// Create optimisation plan object
		OptimisationPlan plan = this.createOptimisationPlan();
//		OptimisationPlan plan = helper.createOptimisationPlan();
		
		List<PhysicalMachine> pms = this.getPhysicalMachinesFromCdoModel(pdcm, ldcm);
		
		VirtualMachineMigrationAction migrationSuggested = null;
		double evaluation;
		
		log.info("Initial DC state");
		for(PhysicalMachine pm : pms) {
			log.info(pm.toString());
		}	
		
		do
		{
			evaluation = getEvaluationFunctionConsolidation(pms);		
			migrationSuggested = migrationConsolidation(pms);
			
			log.info("Initial eval: " + evaluation);
			
			if (migrationSuggested != null) {
				VirtualMachine vmToMigrate = this.getVirtualMachineById(migrationSuggested.getVm().getId(), ldcm);
				Hypervisor sourceHypervisor = this.getComputeNodeById(migrationSuggested.getSource().getId(), pdcm).getHypervisor();
				Hypervisor destinationHypervisor = this.getComputeNodeById(migrationSuggested.getTarget().getId(), pdcm).getHypervisor();
				this.addMigrationActionToOptimisationPlan(plan, vmToMigrate, sourceHypervisor, destinationHypervisor);
				
				log.info("After migration");
				for(PhysicalMachine pm : pms) {
					log.info(pm.toString());
				}	
			}
		} while (migrationSuggested != null);
		
		for (PhysicalMachine pm : pms) {
			ComputeNode pmToPowerDown = this.getComputeNodeById(pm.getId(), pdcm);
			
			NodeState pmState = pmToPowerDown.getState();
			
			int noVirtualMachines = pm.getVms().size();
			if ((pmState == NodeState.RUNNING) && (noVirtualMachines == 0))
				this.addPowerDownActionToOptimisationPlan(plan, pmToPowerDown);
		}
		
		return plan;
	}
	
	private VirtualMachineMigrationAction migrationConsolidation(List<PhysicalMachine> pms) {
		VirtualMachineMigrationAction migration = null;
		double evaluationOfInitialState = getEvaluationFunctionConsolidation(pms);
		
		// Create a deep copy of physical machines list
		List<PhysicalMachine> pmsConsolidation = new ArrayList<PhysicalMachine>();
		for (PhysicalMachine pm : pms) {
			if (pm.isPoweredOn())
				pmsConsolidation.add(new PhysicalMachine(pm));
		}
		
		// Sort physical machines according to the residual capacity (smallest residual capacity first)
		Collections.sort(pmsConsolidation, new PhysicalMachineResidualComparator());
		
		int lastIndex = pmsConsolidation.size()-1; 
		
		for (int i=0; i<lastIndex; i++) {
			PhysicalMachine pmResidualMin = pmsConsolidation.get(i);
			for (int j=lastIndex; j>i; j--) {
				PhysicalMachine pmResidualMax = pmsConsolidation.get(j);
				
				if (pmResidualMax.getVms().size() == 0)
					continue;
					
				Collections.sort(pmResidualMax.getVms(), new VirtualMachineWeightComparator());
				eu.cactosfp7.cactoopt.models.VirtualMachine vm = pmResidualMax.getVms().get(0);
	
				if (pmResidualMin.assignVm(vm)) {
					pmResidualMax.unassignVm(vm);
					double evaluation = getEvaluationFunctionConsolidation(pmsConsolidation);
					
					if (evaluation < evaluationOfInitialState) {
						log.info("Migrate " + vm.getId() + " from " + pmResidualMax.getId() + " to " + pmResidualMin.getId());
						migration = new VirtualMachineMigrationAction(vm, pmResidualMax, pmResidualMin);
						
						// Do actual migration
						for (PhysicalMachine pm : pms) {
							if (pm.getId() == pmResidualMin.getId()) {
								pm.assignVm(vm);
							}
							if (pm.getId() == pmResidualMax.getId()) {
								pm.unassignVm(vm);
							}
						}
						log.info("Eval: " + evaluation);
						
						return migration; // managed to place VM on PM, no need to continue
					} else {
						log.info("Migration " + vm.getId() + " from " + pmResidualMax.getId() + " to " + pmResidualMin.getId()
								+ " will not improve the DC evaluation (eval before: " + evaluationOfInitialState + ", eval after: " + evaluation + ")");
					}
				} else {
					log.info("Migration " + vm.getId() + " from " + pmResidualMax.getId() + " to " + pmResidualMin.getId()
							+ " impossible");
				}
			}
		}
		
		return null; // no migration possible
	}
	
	private double getEvaluationFunctionConsolidation(List<PhysicalMachine> pms) {
		double totalCost = 0;
		double residualMin = Double.MAX_VALUE;
		for (PhysicalMachine pm : pms) {
			double residualLocal;
			if (pm.getVms().size() > 0) {
				totalCost += pm.getNoCores();
				residualLocal = pm.getResidualEvaluation();
//				if ((residualLocal < residualMin) && (pm.getVms().size()>0)) {
				if (residualLocal < residualMin) {
					residualMin = residualLocal;
				}
			}
		}
		return totalCost - residualMin;
	}
	
	private OptimisationPlan createOptimisationPlan() {
		OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
		SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
		plan.setOptimisationStep(rootStep);
		rootStep.setOptimisationPlan(plan);
		rootStep.setExecutionStatus(ExecutionStatus.READY);
		plan.setCreationDate(new Date());
		return plan;
	}
	
	private List<PhysicalMachine> getPhysicalMachinesFromCdoModel(PhysicalDCModel pdcm, LogicalDCModel ldcm) {
		List<PhysicalMachine> pms = new ArrayList<PhysicalMachine>();
		
		for(Rack rack : pdcm.getRacks()) {
			for(AbstractNode node : rack.getNodes()) {
				if (node instanceof ComputeNode) {
					ComputeNode computeNode = (ComputeNode) node;
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
									double vmMemory = 0;
									for(VirtualProcessingUnit vProcessingUnit : vm.getVirtualProcessingUnits()) {
                                        vmNoCores += vProcessingUnit.getVirtualCores();
                                    }
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
				}
			}	
		}
		
		return pms;
	}
	
	private VirtualMachine getVirtualMachineById(String vmId, LogicalDCModel ldcm) {
		for (Hypervisor h: ldcm.getHypervisors()) {
			for (VirtualMachine vm: h.getVirtualMachines()) {
				if (vmId == vm.getId())
					return vm;
			}
		}
		
		return null;
	}
	
	private ComputeNode getComputeNodeById(String nodeId, PhysicalDCModel pdcm) {		
		for(Rack rack : pdcm.getRacks()) {
			for(AbstractNode node : rack.getNodes()) {
				if (node instanceof ComputeNode) {
					if (nodeId == node.getId())
						return (ComputeNode)node;
				}
			}
		}
		
		return null;
	}
	
	private void addMigrationActionToOptimisationPlan(OptimisationPlan plan, VirtualMachine vmToMigrate, Hypervisor sourceHypervisor, Hypervisor destinationHypervisor) {
		VmMigrationAction migration = OptimisationplanFactory.eINSTANCE.createVmMigrationAction();
		migration.setMigratedVm(vmToMigrate);
		migration.setSourceHost(sourceHypervisor);
		migration.setTargetHost(destinationHypervisor);
		migration.setSequentialSteps((SequentialSteps) plan.getOptimisationStep());
		migration.setExecutionStatus(ExecutionStatus.READY);
	}
	
	private void addPowerDownActionToOptimisationPlan(OptimisationPlan plan, eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode pmToDowerDown) {
		eu.cactosfp7.optimisationplan.ManagePhysicalNodeAction powerDown = OptimisationplanFactory.eINSTANCE.createManagePhysicalNodeAction();
		powerDown.setManagedNode(pmToDowerDown);
		powerDown.setTargetState(NodeState.OFF);
		powerDown.setSequentialSteps((SequentialSteps) plan.getOptimisationStep());
		powerDown.setExecutionStatus(ExecutionStatus.READY);
	}
}
