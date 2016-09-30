package eu.cactosfp7.cactoopt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javax.measure.quantity.DataAmount;
import javax.measure.unit.NonSI;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.jscience.physics.amount.Amount;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

import eu.cactosfp7.cactoopt.models.PhysicalMachine;
import eu.cactosfp7.cactoopt.models.PhysicalMachineCpuComparator;
import eu.cactosfp7.cactoopt.models.PhysicalMachineMemoryComparator;
import eu.cactosfp7.cactoopt.models.PhysicalMachineResidualComparator;
import eu.cactosfp7.cactoopt.models.VirtualMachineComparator;
import eu.cactosfp7.cactoopt.models.VirtualMachineMemoryComparator;
import eu.cactosfp7.cactoopt.models.VirtualMachineMigrationAction;
import eu.cactosfp7.cactoopt.models.VirtualMachineWeightComparator;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.logical.VirtualProcessingUnitMeasurement;
import eu.cactosfp7.infrastructuremodels.load.logical.VirtualProcessingUnitsMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.MemoryMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PuMeasurement;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VMImage;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualDisk;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMemory;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualProcessingUnit;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.AbstractNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.MemorySpecification;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ProcessingUnitSpecification;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.Rack;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationplanFactory;
import eu.cactosfp7.optimisationplan.SequentialSteps;
import eu.cactosfp7.optimisationplan.VmMigrationAction;
import eu.cactosfp7.optimisationplan.VmPlacementAction;

/**
 * 
 * USE eu.cactosfp7.cactoopt.optimisation.DefaultOptimisationAlgorithm instead!
 *
 */
@Deprecated
public class OptimisationEngine {
	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(OptimisationEngine.class.getName());
	/** List of Virtual Machines waiting for being assigned*/
	static EList<VirtualMachine> vmsToAssign;
	static EList<VirtualMachine> assignedVms;
	static double minCpuUtilization = 0.2;
	static double maxCpuUtilization = 0.7;
	static double minMemoryUtilization = 0.2;
	static double maxMemoryUtilization = 0.7;
	static double minVCpuUtilization = 0.2;
	static double maxVCpuUtilization = 0.7;
	static double minVMemoryUtilization = 0.2;
	static double maxVMemoryUtilization = 0.7;
	
	public enum OptimisationApproach { LOAD_BALANCING, CONSOLIDATION, RANDOM }
	private static OptimisationApproach approach = OptimisationApproach.LOAD_BALANCING;

	/** The weight of CPU over memory (used for sorting Virtual Machines)*/
	static double alpha = 0.5;

	public static OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm, Date deadline) throws Exception {
		if (deadline.after(new Date())) {
			return OptimisationEngine.generateOptimizationPlan(pdcm, ldcm, plm, llm, null, true);
		} else {
			throw new Exception("Deadline for OptimisationPlan has passed already.");
		}
	}
	
	public static OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm) {
		return OptimisationEngine.generateOptimizationPlan(pdcm, ldcm, plm, llm, null, true);
	}
	
	public static ComputeNode getComputeNodeById(String nodeId, PhysicalDCModel pdcm) {		
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
	
	public static VirtualMachine getVirtualMachineById(String vmId, LogicalDCModel ldcm) {
		for (Hypervisor h: ldcm.getHypervisors()) {
			for (VirtualMachine vm: h.getVirtualMachines()) {
				if (vmId == vm.getId())
					return vm;
			}
		}
		
		return null;
	}
	
	public static ComputeNode getComputeNode(String measurementId, LogicalDCModel ldcm) {
		ComputeNode pm = null;
		
		for (Hypervisor h: ldcm.getHypervisors()) {
			for (VirtualMachine vm: h.getVirtualMachines()) {
				if (measurementId.contains(vm.getId()))
					return h.getNode();
			}
		}
		
		return null;
	}
	
	public static VirtualMachine getVirtualMachine(String measurementId, LogicalDCModel ldcm) {
		ComputeNode pm = null;
		
		for (Hypervisor h: ldcm.getHypervisors()) {
			for (VirtualMachine vm: h.getVirtualMachines()) {
				if (measurementId.contains(vm.getId()))
					return vm;
			}
		}
		
		return null;
	}
		
	private static List<PhysicalMachine> getPhysicalMachinesFromCdoModel(PhysicalDCModel pdcm, LogicalDCModel ldcm) {
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
						totalMemory += ms.getSize().getEstimatedValue();
					}
					
					PhysicalMachine pm = new PhysicalMachine(id, noCores, totalMemory);
					
					for (Hypervisor h : ldcm.getHypervisors()) {
						if (h.getNode().getId() == id) {
							for (VirtualMachine vm : h.getVirtualMachines()) {
								String vmId = vm.getId();
								int vmNoCores = 0;
								
                                for(VirtualProcessingUnit vProcessingUnit : vm.getVirtualProcessingUnits()) {
                                    vmNoCores += vProcessingUnit.getVirtualCores();
                                }
                                
								double vmMemory = 0;
								
								for (VirtualMemory vmem : vm.getVirtualMemoryUnits()) {
									vmMemory += vmem.getProvisioned().getEstimatedValue();
								}
								eu.cactosfp7.cactoopt.models.VirtualMachine vmToAssign = new eu.cactosfp7.cactoopt.models.VirtualMachine(vmId, vmNoCores, vmMemory);
								pm.assignVm(vmToAssign);
							}
						}
					}
					
					pms.add(pm);
				}
			}	
		}
		
		return pms;
	}
	
	public static OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm, EList<VirtualMachine> vmsToAssign, boolean sort) {
		OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
		SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
		plan.setOptimisationStep(rootStep);
		rootStep.setOptimisationPlan(plan);
		
		List<PhysicalMachine> pms = getPhysicalMachinesFromCdoModel(pdcm, ldcm);
		VirtualMachineMigrationAction migrationSuggested = null;
		double evaluation;
		
		System.out.println("Initial DC state");
		for(PhysicalMachine pm : pms) {
			System.out.println(pm.toString());
		}	
		
		do
		{
			switch (approach) {
				case LOAD_BALANCING:
					evaluation = getEvaluationFunctionLoadBalancingMax(pms, alpha);
					migrationSuggested = migrationLoadBalancing(pms);
					break;

				case CONSOLIDATION:
					evaluation = getEvaluationFunctionConsolidation(pms);		
					migrationSuggested = migrationConsolidation(pms);
					break;
					
				case RANDOM:
					evaluation = 0;
					migrationSuggested = migrationRandom(pms);
					break;
					
				default:
					evaluation = 0;
					break;
			}
			
			System.out.println("Initial eval: " + evaluation);
			
			if (migrationSuggested != null) {
				VmMigrationAction migration = OptimisationplanFactory.eINSTANCE.createVmMigrationAction();
				migration.setMigratedVm(getVirtualMachineById(migrationSuggested.getVm().getId(), ldcm));
				migration.setSourceHost(getComputeNodeById(migrationSuggested.getSource().getId(), pdcm).getHypervisor());
				migration.setTargetHost(getComputeNodeById(migrationSuggested.getTarget().getId(), pdcm).getHypervisor());
				migration.setSequentialSteps(rootStep);
				
				System.out.println("After migration");
				for(PhysicalMachine pm : pms) {
					System.out.println(pm.toString());
				}	
			}
		} while (migrationSuggested != null);
		
		return plan;
	}
	
	public static OptimisationPlan OldMigration(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm, boolean sort) {
		OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
		SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
		plan.setOptimisationStep(rootStep);
		rootStep.setOptimisationPlan(plan);
		
		log.info("=============================================================================================");
		log.info("VIRTUAL MACHINE MIGRATION");
		
		double minCPUUtilizationThreshold = 0.1;

		List<ComputeNode> pMsUnderUtilizedCpu = new ArrayList<ComputeNode>();
		List<ComputeNode> pMsOverUtilizedCpu = new ArrayList<ComputeNode>();
		List<ComputeNode> pMsUnderUtilizedMemory = new ArrayList<ComputeNode>();
		List<ComputeNode> pMsOverUtilizedMemory = new ArrayList<ComputeNode>();
		List<ProcessingUnitSpecification> underUtlizedPus = new ArrayList<ProcessingUnitSpecification>();
		List<ProcessingUnitSpecification> overUtlizedPus = new ArrayList<ProcessingUnitSpecification>();
		List<VirtualProcessingUnit> underUtlizedVCpu = new ArrayList<VirtualProcessingUnit>();
		List<VirtualProcessingUnit> overUtlizedVCpu = new ArrayList<VirtualProcessingUnit>();
		List<VirtualMemory> underUtlizedVMemory = new ArrayList<VirtualMemory>();
		List<VirtualMemory> overUtlizedVMemory = new ArrayList<VirtualMemory>();
//		List<VirtualMachine> overloadedVMs = new ArrayList<VirtualMachine>();

		Map<String, Integer> pmsUtilizedCores = new HashMap<String, Integer>(); // number of utilized cores per each physical machine
		Map<String, Integer> vmsUtilizedCores = new HashMap<String, Integer>(); // number of utilized cores per each virtual machine
		
		for (VirtualProcessingUnitsMeasurement cpuMeasurement : llm.getVirtualProcessingUnitMeasurements()) {
			try {
				double cpuUtilization = cpuMeasurement.getUtilization().getValue().getEstimatedValue();
				
				if (cpuUtilization >= minCPUUtilizationThreshold) {
					
					ComputeNode pm = getComputeNode(cpuMeasurement.getId(), ldcm);
					VirtualMachine vm = getVirtualMachine(cpuMeasurement.getId(), ldcm);
					//ComputeNode pm = cpuMeasurement.getObservedVirtualPu().getVirtualmachine().getHypervisor().getNode();
					
					if (pmsUtilizedCores.containsKey(pm.getId())) {
						// if compute node is already in the map, increment the number of utilized CPU cores
						Integer previousNumberOfUtilizedCores = pmsUtilizedCores.get(pm.getId());
						pmsUtilizedCores.put(pm.getId(), previousNumberOfUtilizedCores + 1);
					} else {
						// if compute node is not yet in the map, add new pair (CPU, 1)
						pmsUtilizedCores.put(pm.getId(), 1);
					}
					
					if (vmsUtilizedCores.containsKey(vm.getId())) {
						// if compute node is already in the map, increment the number of utilized CPU cores
						Integer previousNumberOfUtilizedCores = vmsUtilizedCores.get(vm.getId());
						vmsUtilizedCores.put(vm.getId(), previousNumberOfUtilizedCores + 1);
					} else {
						// if compute node is not yet in the map, add new pair (CPU, 1)
						vmsUtilizedCores.put(vm.getId(), 1);
					}
				}
			} catch(Exception e) {
				log.warning("No CPU utilization measurement for " + cpuMeasurement.getId());
			}
		}
		
		log.info("PMs with CPU utilization outside of bounds (min: " + minCpuUtilization*100 + "%, max: " + maxCpuUtilization*100 +"%)");

		boolean anyPm = false;
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
		
		if (!anyPm) {
			log.info("\t all VMs inside the bounds");
		}
		
		log.info("PMs with memory utilization outside of bounds (min: " + minMemoryUtilization*100 + "%, max: " + maxMemoryUtilization*100 +"%)");

		anyPm = false;
		for (MemoryMeasurement memoryMeasurement : plm.getMemoryMeasurements()) {
			double utilization = memoryMeasurement.getUtilization().getValue().getEstimatedValue();
			
			if (utilization > maxMemoryUtilization) {
				MemorySpecification memory = memoryMeasurement.getObservedMemory();
				ComputeNode pm = (ComputeNode)memory.getNode();
				pMsOverUtilizedMemory.add(pm);
//				overUtlizedMemory.add(memory);
				log.info("\t" + pm.toString());
				log.info("\t\tmemory: " + memory.toString());
				log.info("\t\t\tutilization: " + (utilization * 100) + "%");
				anyPm = true;
			}
			
			if (utilization < minMemoryUtilization) {
				MemorySpecification memory = memoryMeasurement.getObservedMemory();
				ComputeNode pm = (ComputeNode)memory.getNode();
				pMsUnderUtilizedMemory.add(pm);
//				underUtlizedMemory.add(memory);
				log.info("\t" + pm.toString());
				log.info("\t\tmemory: " + memory.toString());
				log.info("\t\t\tutilization: " + (utilization * 100) + "%");
				anyPm = true;
			}
		}
		
		if (!anyPm) {
			log.info("\t all VMs inside the bounds");
		}
		
		List<Hypervisor> potentialMigrationTargets = new ArrayList<Hypervisor>();
		
		log.info("PMs to host migrated VM:");
		for (Hypervisor h : ldcm.getHypervisors()) {
			ComputeNode pm = h.getNode();
			if (pMsUnderUtilizedMemory.contains(pm) && pMsUnderUtilizedCpu.contains(pm)) {
				log.info("\t" + h.getNode().toString());
				potentialMigrationTargets.add(h);
			}
		}
		
		Map<ComputeNode, Amount<DataAmount>> pmsOrderedByResidualMemory = new HashMap<ComputeNode, Amount<DataAmount>>();
	
		for (Rack r : pdcm.getRacks()) {
			for (AbstractNode node : r.getNodes()) {
				if (node instanceof ComputeNode) {
					ComputeNode pm = (ComputeNode) node;
			    	Amount<DataAmount> totalMemory = Amount.valueOf(0, NonSI.BYTE);
					for (MemorySpecification memory : pm.getMemorySpecifications()) {
						totalMemory = totalMemory.plus(memory.getSize());
					}

					Amount<DataAmount> memoryProvisioned = Amount.valueOf(0, NonSI.BYTE);
					if ((pm.getHypervisor() != null) && (pm.getHypervisor().getVirtualMachines().size() > 0)) {
						for (VirtualMachine vm : pm.getHypervisor().getVirtualMachines()) {
							for(VirtualMemory memory : vm.getVirtualMemoryUnits()) {
								memoryProvisioned = memoryProvisioned.plus(memory.getProvisioned());
							}
						}					
						pmsOrderedByResidualMemory.put(pm, totalMemory.minus(memoryProvisioned));
					} else {
						pmsOrderedByResidualMemory.put(pm, totalMemory);
					}
				}
			}
		}
		
		Map<ComputeNode, Integer> pmsOrderedByResidualCpu = new HashMap<ComputeNode, Integer>();
		
		for (Rack r : pdcm.getRacks()) {
			for (AbstractNode node : r.getNodes()) {
				// check if that node exists in the hashmap vm utilized cores
				if (node instanceof ComputeNode) {
					ComputeNode pm = (ComputeNode) node;
					String pm_id = pm.getId();
					
			    	int totalCores = 0;
					for (ProcessingUnitSpecification cpu : pm.getCpuSpecifications()) {
						totalCores += cpu.getNumberOfCores();
					}

					int coresProvisioned = 0;
					if ((pm.getHypervisor() != null) && (pm.getHypervisor().getVirtualMachines().size() > 0)) {
//						for (VirtualMachine vm : pm.getHypervisor().getVirtualMachines()) {
//							for(VirtualProcessingUnit cpu : vm.getVirtualPus()) {
//								coresProvisioned ++;
//							}
//						}					
						if (pmsUtilizedCores.containsKey(pm_id)) {
							coresProvisioned = pmsUtilizedCores.get(pm_id);
						}
						pmsOrderedByResidualCpu.put(pm, totalCores - coresProvisioned);
					} else {
						pmsOrderedByResidualCpu.put(pm, totalCores);
					}
				}
			}
		}
		
//		Map<ComputeNode, Amount<DataAmount>> pmsOrderedByProvisionedMemory = new HashMap<ComputeNode, Amount<DataAmount>>();
//	
//		for(Hypervisor h : ldcm.getHypervisors()) {
//			ComputeNode pm = h.getNode();
//			Amount<DataAmount> memoryProvisioned = Amount.valueOf(0, NonSI.BYTE);
//			for (VirtualMachine vm : h.getVirtualMachines()) {
//				for(VirtualMemory memory : vm.getVirtualMemoryUnits()) {
//					memoryProvisioned = memoryProvisioned.plus(memory.getProvisioned());
//				}
//			}
//			pmsOrderedByProvisionedMemory.put(pm, memoryProvisioned);
//		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
//		List<Map.Entry<ComputeNode, Amount<DataAmount>>> activePmsList = new ArrayList(pmsOrderedByProvisionedMemory.entrySet());
//		Collections.sort(activePmsList, byMapValues.reverse());
		List<Map.Entry<ComputeNode, Amount<DataAmount>>> activePmsList = new ArrayList(pmsOrderedByResidualMemory.entrySet());
		Collections.sort(activePmsList, byMapValues);
		
		
//		log.info("---------------------------------------------------------------------------------------------");
//		log.info("PMs ordered by provisioned memory");
//		
//		for (Entry<ComputeNode, Amount<DataAmount>> entry : pmsOrderedByProvisionedMemory.entrySet()) {
//			log.info(entry.getKey().toString() + " " + entry.getValue().toString());
//		}
		
		log.info("---------------------------------------------------------------------------------------------");
		log.info("PMs ordered by residual memory");
		
		for (Entry<ComputeNode, Amount<DataAmount>> entry : pmsOrderedByResidualMemory.entrySet()) {
			log.info(entry.getKey().toString() + " " + entry.getValue().toString());
		}
		
//		log.info("---------------------------------------------------------------------------------------------");
//		log.info("VM migrations");
//		for (Hypervisor h : ldcm.getHypervisors()) {
//			ComputeNode pm = h.getNode();
//			if (pMsOverUtilizedMemory.contains(pm) || pMsOverUtilizedCpu.contains(pm)) {
//				for (VirtualMachine vm : h.getVirtualMachines()) {
//					Hypervisor source = h;
//					Hypervisor target = potentialMigrationTargets.remove(0);
//					
//					log.info("\t VM " + vm.toString() + " from PM " + source.getNode().toString() + " to PM " + target.getNode().toString());
//					
//					VmMigrationAction migration = OptimisationplanFactory.eINSTANCE.createVmMigrationAction();
//					migration.setMigratedVm(vm);
//					migration.setSourceHost(source);
//					migration.setTargetHost(target);
//					migration.setSequentialSteps(rootStep);
//				}
//			}
//		}
		
		log.info("---------------------------------------------------------------------------------------------");
		log.info("VM migrations for DC consolidation");
		List<String> forbiddenPms = new ArrayList<String>();
//		for (ComputeNode pm : pmsOrderedByProvisionedMemory.keySet()) {
//		for (ComputeNode pm : pmsOrderedByResidualMemory.keySet()) {
		for (ComputeNode pm : pmsOrderedByResidualCpu.keySet()) {
			Hypervisor h = pm.getHypervisor();
			
			if (h != null) {
				forbiddenPms.add(pm.getId());
				
				ConcurrentLinkedQueue<VirtualMachine> virtualMachines = new ConcurrentLinkedQueue<VirtualMachine>(h.getVirtualMachines());
				
				for (VirtualMachine vm : virtualMachines) {
//				for (VirtualMachine vm : h.getVirtualMachines()) {
					Hypervisor source = h;
					
					ComputeNode targetPm = getBestPm(pdcm, plm, ldcm, vm, 2, forbiddenPms);
					
					if (targetPm != null && !source.getNode().getId().equals(targetPm.getId())) {
						log.info("\t MIGRATION of VM " + vm.getId() + " from PM " + source.getNode().getId() + " to PM " + targetPm.getId());
						Hypervisor target = targetPm.getHypervisor();
						
						VmMigrationAction migration = OptimisationplanFactory.eINSTANCE.createVmMigrationAction();
						migration.setMigratedVm(vm);
						migration.setSourceHost(source);
						migration.setTargetHost(target);
						migration.setSequentialSteps(rootStep);
						
						vm.setHypervisor(target);
					}
				}
			}
		}
		
		// TESTING
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
		
		for (Rack r : pdcm.getRacks()) {
			for (AbstractNode node : r.getNodes()) {
				if (node instanceof ComputeNode) {
					ComputeNode pm = (ComputeNode) node;

					if ((pm.getHypervisor() != null) && (pm.getHypervisor().getVirtualMachines().size() == 0)) {
						log.info("\t TURN OFF PM " + pm.getId());
					}
				}
			}
		}
		
		return plan;
	}
	
	public static void OldVerticalScaling() {
		/*
		log.info("=============================================================================================");
		log.info("VERTICAL SCALING");
		
		log.info("VMs with virtual cpu utilization outside of bounds (min: " + minVCpuUtilization*100 + "%, max: " + maxVCpuUtilization*100 +"%)");

		boolean anyVm = false;
		for (VirtualProcessingUnitMeasurement vCpuMeasurement : llm.getVirtualProcessingUnitMeasurements()) {
			Amount<Dimensionless> utilization = vCpuMeasurement.getUtilization().getValue();
			
			if (utilization.getEstimatedValue() > maxVCpuUtilization) {
				VirtualProcessingUnit vCpu = vCpuMeasurement.getObservedVirtualPu();
				VirtualMachine vm = vCpu.getVirtualmachine();
//				pMsOverUtilizedCpu.add(pm);
				overUtlizedVCpu.add(vCpu);
				log.info("\t" + vm.toString());
				log.info("\t\tmemory: " + vCpu.toString());
				log.info("\t\t\tutilization: " + (utilization.getEstimatedValue() * 100) + "%");
				anyVm = true;
			}
			
			if (utilization.getEstimatedValue() < minVMemoryUtilization) {
				VirtualProcessingUnit vCpu = vCpuMeasurement.getObservedVirtualPu();
				VirtualMachine vm = vCpu.getVirtualmachine();
//				pMsOverUtilizedCpu.add(pm);
				underUtlizedVCpu.add(vCpu);
				log.info("\t" + vm.toString());
				log.info("\t\tmemory: " + vCpu.toString());
				log.info("\t\t\tutilization: " + (utilization.getEstimatedValue() * 100) + "%");
				anyVm = true;
			}
		}
		
		if (!anyVm) {
			log.info("\t all VMs inside the bounds");
		}
		
		log.info("VMs with virtual memory utilization outside of bounds (min: " + minVMemoryUtilization*100 + "%, max: " + maxVMemoryUtilization*100 +"%)");

		anyVm = false;
		for (VirtualMemoryMeasurement vMemoryMeasurement : llm.getVirtualMemoryMeasurements()) {
			Amount<Dimensionless> utilization = vMemoryMeasurement.getUtilization().getValue();
			
			if (utilization.getEstimatedValue() > maxVMemoryUtilization) {
				VirtualMemory vMemory = vMemoryMeasurement.getObservedVirtualMemory();
				VirtualMachine vm = vMemory.getVirtualMachine();
//				pMsOverUtilizedCpu.add(pm);
				overUtlizedVMemory.add(vMemory);
				log.info("\t" + vm.toString());
				log.info("\t\tmemory: " + vMemory.toString());
				log.info("\t\t\tutilization: " + (utilization.getEstimatedValue() * 100) + "%");
				anyVm = true;
			}
			
			if (utilization.getEstimatedValue() < minVMemoryUtilization) {
				VirtualMemory vMemory = vMemoryMeasurement.getObservedVirtualMemory();
				VirtualMachine vm = vMemory.getVirtualMachine();
//				pMsUnderUtilizedCpu.add(pm);
				underUtlizedVMemory.add(vMemory);
				log.info("\t" + vm.toString());
				log.info("\t\tmemory: " + vMemory.toString());
				log.info("\t\t\tutilization: " + (utilization.getEstimatedValue() * 100) + "%");
				anyVm = true;
			}
		}
		
		if (!anyVm) {
			log.info("\t all VMs inside the bounds");
		}

//		log.info("VMs with overloaded VCPU");
//		for (VirtualProcessingUnitMeasurement vcpu : llm.getVirtualProcessingUnitMeasurements()) {
//			double utilization = vcpu.getUtilization().getValue();
//			if (utilization > maxVCpuUtilization) {
//				VirtualMachine vm = vcpu.getObservedVirtualPu().getVirtualmachine();
//				overloadedVMs.add(vm);
//				log.info("\t" + vm.toString());
//				log.info("\t\tutilization: " + utilization + "%");
//			}
//		}
//		
//		for (Hypervisor h : ldcm.getHypervisors()) {
//			ComputeNode pm = h.getNode();
//			if (overloadedPMs.contains(pm)) {
//
//			}
//		}
		
		log.info("VMs to scale virtual CPU");
		for (Hypervisor h : ldcm.getHypervisors()) {
			for (VirtualMachine vm : h.getVirtualMachines()) {
//				EList<ProcessingUnitSpecification> cpus = vm.getPuAffinity().getAffinePus();
				for (VirtualProcessingUnit vCpu : vm.getVirtualPus()) {
					if (overUtlizedVCpu.contains(vCpu)) {
						log.info("\tUpscale: " + vm.toString());
						PhysicalFrequencyScalingAction scaling = OptimisationplanFactory.eINSTANCE.createPhysicalFrequencyScalingAction();
	//					scaling.setScaledPhysicalProcessingUnit(cpus.get(0));
						scaling.setProposedFrequency(Amount.valueOf(2.6, SI.GIGA(SI.HERTZ)));
						scaling.setSequentialSteps(rootStep);
					}
	
					if (underUtlizedVCpu.contains(vCpu)) {
						log.info("\tDownscale: " + vm.toString());
						PhysicalFrequencyScalingAction scaling = OptimisationplanFactory.eINSTANCE.createPhysicalFrequencyScalingAction();
	//					scaling.setScaledPhysicalProcessingUnit(cpus.get(0));
						scaling.setProposedFrequency(Amount.valueOf(1.4, SI.GIGA(SI.HERTZ)));
						scaling.setSequentialSteps(rootStep);
					}
	//				int cpuNumber = cpus.size();
	//				int overloadedCpuNumber = 0;
	//				for (ProcessingUnitSpecification cpu : cpus) {
	//					
	//				}
				}
			}
		}
		
		log.info("VMs to scale virtual memory");
		for (Hypervisor h : ldcm.getHypervisors()) {
			for (VirtualMachine vm : h.getVirtualMachines()) {
				for (VirtualMemory vMemory : vm.getVirtualMemoryUnits()) {
					if (overUtlizedVMemory.contains(vMemory)) {
						log.info("\tUpscale: " + vm.toString());
						LogicalMemoryScalingAction scaling = OptimisationplanFactory.eINSTANCE.createLogicalMemoryScalingAction();
						scaling.setProposedSize(Amount.valueOf(4, SI.GIGA(NonSI.BYTE)));
						scaling.setSequentialSteps(rootStep);
					}
	
//					if (underUtlizedVMemory.contains(vMemory)) {
//						log.info("\tDownscale: " + vm.toString());
//						LogicalMemoryScalingAction scaling = OptimisationplanFactory.eINSTANCE.createLogicalMemoryScalingAction();
//						scaling.setProposedSize(Amount.valueOf(1, SI.GIGA(NonSI.BYTE)));
//						scaling.setSequentialSteps(rootStep);
//					}
				}
			}
		}
		
//		for (Hypervisor h : ldcm.getHypervisors()) {
//			for (VirtualMachine vm : h.getVirtualMachines()) {
//				for (VirtualProcessingUnit vcpu : vm.getVirtualPus()) {
//					if (overloadedPus.contains(vcpu)) {
//						LogicalFrequencyScalingAction scaling = OptimisationplanFactory.eINSTANCE.createLogicalFrequencyScalingAction();
////						scaling.getScaledPhysicalProcessingUnit();
//						scaling.setScaledVirtualProcessingUnit(vcpu);
//						scaling.setSequentialSteps(rootStep);
//					}
//				}
////				if (overloadedPus.containsAll(vm.getVirtualPus())) {
////
////				}
//			}
//		}
*/
	}
	
	public static OptimisationPlan OldInitialPlacement(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm, EList<VirtualMachine> vmsToAssign, boolean sort) {
		OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
		SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
		plan.setOptimisationStep(rootStep);
		rootStep.setOptimisationPlan(plan);
		
		log.info("=============================================================================================");
		log.info("INITIAL PLACEMENT");
		
		if (vmsToAssign != null) {
			
	//		vmsToAssign = new BasicEList<VirtualMachine>(vms);
			assignedVms = new BasicEList<VirtualMachine>();
			
			if (sort) {
				// Sorting for First/Best Fit Decreasing
				Collections.sort(vmsToAssign, new VirtualMachineComparator());
				
				log.info("-------------------------------");
				log.info("VMs to place (sorted)");
				for (VirtualMachine vm : vmsToAssign) {
					log.info("\t" + vm.toString());
					for (VirtualMemory vmemory : vm.getVirtualMemoryUnits()) {
						log.info("\t\t\t" + vmemory.getProvisioned());
					}
				}
			}
			
		//	EList<VMImage> vmi = ldcm.getVmImages();
			
			for (VirtualMachine vm : vmsToAssign) {
				log.info("-------------------------------");
				log.info("Indetifying the best Initial Placement for " + vm.toString());
				ComputeNode pm = getBestPm(pdcm, plm, ldcm, vm, 4);
		//		ComputeNode pm = getFirstPm(plm, ldcm, vm);
				if (pm == null) {
					log.info("Impossible to place VM");
				} else {
					VmPlacementAction placement = OptimisationplanFactory.eINSTANCE.createVmPlacementAction();
	//				Hypervisor h = EcoreUtil.copy(pm.getHypervisor());
	//				HypervisorType ht = EcoreUtil.copy(pm.getHypervisor().getHypervisorType());
					Hypervisor h = pm.getHypervisor();
	//				Hypervisor hCopy = CoreFactory.INSTANCE.createHypervisor();
	//				h.setHypervisorType(null);
	//				h.setLogicalDCModel(null);
	//				h.setNode(null);
	//				h.setId(null);
					placement.setTargetHost(h);
	//				placement.setTargetHost(null);
	//				VMImage vmi = EcoreUtil.copy(vm.getVMImageInstance().getExecutedVMImage());
					VirtualDisk vd = vm.getVMImageInstance().getRootDisk();
					placement.setVmImage(vd);
	//				placement.setVMImage(null);
	//				placement.setProposedMemory(null);
	//				placement.setProposedStorage(null);
					placement.setSequentialSteps(rootStep);
					assignVmToPm(vm, pm);
				}
			}
			vmsToAssign.removeAll(assignedVms);
		}

		log.info("=============================================================================================");
		return plan;
	}

	public static ComputeNode getFirstPm(PhysicalLoadModel plm, LogicalDCModel ldcm, VirtualMachine vmToPlace) {
		for (MemoryMeasurement m : plm.getMemoryMeasurements()) {
			ComputeNode node = (ComputeNode)m.getObservedMemory().getNode();
			double freeMemory = m.getObservedMemory().getSize().times((1.0 - m.getUtilization().getValue().getEstimatedValue())).getEstimatedValue();
	
			for(Hypervisor h : ldcm.getHypervisors()) {
				if (h.getNode().equals(node)) {
					for(VirtualMachine vm : h.getVirtualMachines()) {
						for(VirtualMemory vMemory : vm.getVirtualMemoryUnits()) {
							freeMemory -= vMemory.getProvisioned().getEstimatedValue();
						}
					}
				}
			}
			double neededMemory = 0.0;
			for(VirtualMemory vMemory : vmToPlace.getVirtualMemoryUnits()) {
				neededMemory += vMemory.getProvisioned().getEstimatedValue();
			}
			
			if (freeMemory >= neededMemory) {
				return node;
			}
		}
		return null;
	}
	
	public static ComputeNode getBestPm(PhysicalDCModel pdcm, PhysicalLoadModel plm, LogicalDCModel ldcm, VirtualMachine vmToPlace, int alg) {
		return getBestPm(pdcm, plm, ldcm, vmToPlace, alg, null);
	}
	
	public static ComputeNode getBestPm(PhysicalDCModel pdcm, PhysicalLoadModel plm, LogicalDCModel ldcm, VirtualMachine vmToPlace, int alg, List<String> forbiddenPms) {
		Multimap<Double, ComputeNode> pms = ArrayListMultimap.create();
	
		if (forbiddenPms == null) {
			forbiddenPms = new ArrayList<String>();
		}
		
		double neededMemory = 0.0;
		for(VirtualMemory vMemory : vmToPlace.getVirtualMemoryUnits()) {
			neededMemory += vMemory.getProvisioned().getEstimatedValue();
		}
		
		switch (alg) {
			case 1:
				//
				// Less utilized CPU
				//
				// Chooses ComputeNode with less utilized CPU (in total)
				
				for (PuMeasurement c : plm.getCpuMeasurement()) {
	//				ComputeNode node = (ComputeNode)c.getObservedCpu().getNode();
					Double freeCpu = 1.0 - c.getUtilization().getValue().getEstimatedValue();
	//				Double freeCpuCores = c.getObservedCpu().getNumberOfCores() * (1.0 - c.getUtilization().getValue());
						
					ComputeNode pm = (ComputeNode)c.getObservedPu().getNode();
					
					if (!forbiddenPms.contains(pm.getId())) {
						pms.put(freeCpu, pm);
					}
				}
				break;
				
			case 2:
				//
				// Biggest number of not reserved CPU cores
				//
				// Chooses ComputeNode with the biggest number of not reserved CPU cores
				// Assumptions: 
				//   1) Utilization shows percentage of already used CPU cores, e.g. ComputeNode
				//      with 8 cores in total and 2 already reserved/used will have utilization equal to 25%
				//   2) 1:1 mapping between physical cores and VMs
				
				double coresPerVm = 1.0;
				
				for (PuMeasurement c : plm.getCpuMeasurement()) {
					ComputeNode node = (ComputeNode)c.getObservedPu().getNode();
	//				Double freeCpu = c.getObservedCpu().getFrequency().getEstimatedValue() * (1.0 - c.getUtilization().getValue());
					Double freeCpuCores = c.getObservedPu().getNumberOfCores() * (1.0 - c.getUtilization().getValue().getEstimatedValue());
					
					for(Hypervisor h : ldcm.getHypervisors()) {
						if (h.getNode().equals(node)) {
							freeCpuCores -= coresPerVm * h.getVirtualMachines().size();
	//						for(VirtualMachine vm : h.getVirtualMachines()) {
	//							freeCpuCores -= coresPerVm;
	//						}
						}
					}
					
					ComputeNode pm = (ComputeNode)c.getObservedPu().getNode();
	
					if (!forbiddenPms.contains(pm.getId())) {
						pms.put(freeCpuCores, pm);
					}
				}
				break;
				
			case 3:
				//
				// Less utilized physical memory
				//
				// Chooses ComputeNode with less utilized memory (in total)
				
				for (MemoryMeasurement m : plm.getMemoryMeasurements()) {
	//				ComputeNode node = (ComputeNode)m.getObservedMemory().getNode();
					double freeMemory = m.getObservedMemory().getSize().times((1.0 - m.getUtilization().getValue().getEstimatedValue())).getEstimatedValue();
					
					if (freeMemory >= neededMemory) {
						ComputeNode pm = (ComputeNode)m.getObservedMemory().getNode();
						if (!forbiddenPms.contains(pm.getId())) {
							pms.put(freeMemory, pm);
						}
					}
				}
				break;
				
			case 4:
				//
				// Biggest amount of not reserved memory
				//
				// Chooses ComputeNode with the biggest amount of not reserved memory (in total)
				
				for (MemoryMeasurement m : plm.getMemoryMeasurements()) {
					ComputeNode node = (ComputeNode)m.getObservedMemory().getNode();
					double freeMemory = m.getObservedMemory().getSize().times((1.0 - m.getUtilization().getValue().getEstimatedValue())).getEstimatedValue();
					
					for(Hypervisor h : ldcm.getHypervisors()) {
						if (h.getNode().equals(node)) {
							for(VirtualMachine vm : h.getVirtualMachines()) {
								for(VirtualMemory vMemory : vm.getVirtualMemoryUnits()) {
									freeMemory -= vMemory.getProvisioned().getEstimatedValue();
								}
							}
						}
					}
					
					if (freeMemory >= neededMemory) {
						ComputeNode pm = (ComputeNode)m.getObservedMemory().getNode();
						if (!forbiddenPms.contains(pm.getId())) {
							pms.put(freeMemory, pm);
						}
					}
				}
	
				break;
		}
		
		log.info("-------------------------------");
		log.info("PMs available:");
		for (Map.Entry<Double, ComputeNode> e : pms.entries()) {
			log.info("\t" + e.toString());
		}
		
		Double max = 0.0;
		
		for(Double d : pms.keySet()) {
			if (d.doubleValue() > max) max = d.doubleValue();
		}
		
		ArrayList<ComputeNode> best = new ArrayList<ComputeNode>(pms.get(max));
		
		log.info("-------------------------------");
		log.info("Best value: " + max);
		log.info("Best PMs:");
		for (ComputeNode e : best) {
			log.info("\t" + e.toString());
		}
		if (best.size()>0) {
			return best.get(0);
		} else {
			return null;
		}
	}

	public static boolean assignVmToPm(VirtualMachine vm, ComputeNode pm) {
		Hypervisor h = pm.getHypervisor();
		vm.setHypervisor(h);
//		vmsToAssign.remove(vm);
		assignedVms.add(vm);

		log.info("-------------------------------");
		log.info("VM " + vm.toString() + " assigned to " + pm.toString() + ":");
		
		return true;
	}
	
	public static boolean initialPlacementLoadBalancing(List<PhysicalMachine> pms, eu.cactosfp7.cactoopt.models.VirtualMachine vm) {
		boolean ableToPlace = false;
		
		// Create a deep copy of physical machines list for all
		List<PhysicalMachine> pmsCpu = new ArrayList<PhysicalMachine>();
		List<PhysicalMachine> pmsMemory = new ArrayList<PhysicalMachine>();
		for (PhysicalMachine pm : pms) {
			pmsCpu.add(new PhysicalMachine(pm));
			pmsMemory.add(new PhysicalMachine(pm));
		}
		
		// Sort lists in increasing utilization order
		Collections.sort(pmsCpu, new PhysicalMachineCpuComparator());
		Collections.sort(pmsMemory, new PhysicalMachineMemoryComparator());
		
		PhysicalMachine pmCpu = pmsCpu.get(0);
		double evaluationCpu = Double.MIN_VALUE;
		if (pmCpu.assignVm(vm)) {
			evaluationCpu = getEvaluationFunctionLoadBalancingMax(pmsCpu, alpha);
//			System.out.println(pmCpu.id + " (CPU) eval: " + evaluationCpu);
			ableToPlace = true;
		}
		
		PhysicalMachine pmMemory = pmsMemory.get(0);
		double evaluationMemory = Double.MIN_VALUE;
		if (pmMemory.assignVm(vm)) {
			evaluationMemory = getEvaluationFunctionLoadBalancingMax(pmsMemory, alpha);
//			System.out.println(pmMemory.id +" (memory) eval: " + evaluationMemory);
			ableToPlace = true;
		}
		
		if (ableToPlace) {
			if (evaluationCpu > evaluationMemory) {
//				System.out.println("Place " + vm.id + " on " + pmCpu.id);
				for (PhysicalMachine pm : pms) {
					if (pm.getId() == pmCpu.getId()) {
						pm.assignVm(vm);
					}
				}
			} else {
//				System.out.println("Place " + vm.id + " on " + pmMemory.id);
				for (PhysicalMachine pm : pms) {
					if (pm.getId() == pmMemory.getId()) {
						pm.assignVm(vm);
					}
				}
			}
		}
		
		return ableToPlace;
	}
	
	public static VirtualMachineMigrationAction migrationLoadBalancing(List<PhysicalMachine> pms) {
		VirtualMachineMigrationAction migration = null;
		double evaluationOfCurrentState = getEvaluationFunctionLoadBalancingMax(pms, alpha);
//		boolean migrationSuggested;
		
		// Create a deep copy of physical machines list
		List<PhysicalMachine> pmsCpu = new ArrayList<PhysicalMachine>();
		List<PhysicalMachine> pmsMemory = new ArrayList<PhysicalMachine>();
		int totalCpuCores = 0; // Total number of CPU cores in the whole DC
		double totalMemory = 0; // Total size of memory in the whole DC
		int assignedCpuCores = 0; // Total number of CPU cores assigned to virtual machines (in the whole DC)
		double assignedMemory = 0; // Total size of memory assigned to virtual machines (in the whole DC)
		
		for (PhysicalMachine pm : pms) {
			pmsCpu.add(new PhysicalMachine(pm));
			pmsMemory.add(new PhysicalMachine(pm));
			
			totalCpuCores += pm.getNoCores();
			totalMemory += pm.getTotalMemory();
			
			assignedCpuCores += pm.getUtilizedCores();
			assignedMemory += pm.getUtilizedMemory();
		}
		
		double targetMeanCpuUtilization = assignedCpuCores / (double) totalCpuCores;
		double targetMeanMemoryUtilization = assignedMemory / totalMemory;
		
		// Sort lists in increasing utilization order
		Collections.sort(pmsCpu, new PhysicalMachineCpuComparator());
		Collections.sort(pmsMemory, new PhysicalMachineMemoryComparator());
		
		int lastIndex = pms.size()-1; 
		
		PhysicalMachine pmCpuMin = pmsCpu.get(0);
		PhysicalMachine pmCpuMax = pmsCpu.get(lastIndex);
//		double cpuUtilizationDiffrence = pmCpuMax.getCpuUtilization() - pmCpuMin.getCpuUtilization(); 
//		Collections.sort(pmCpuMax.getVms(), new VirtualMachineCpuComparator());
		
		List<eu.cactosfp7.cactoopt.models.VirtualMachine> vms = pmCpuMax.getVms();
		
//		for (eu.cactosfp7.cactoopt.VirtualMachine vm : vms) {
//			if (vm.get)
//		}
		
		eu.cactosfp7.cactoopt.models.VirtualMachine vmCpu = pmCpuMax.getVms().get(0);
		double evaluationCpu = Double.MIN_VALUE;
		
		if (pmCpuMin.assignVm(vmCpu)) {
			pmCpuMax.unassignVm(vmCpu);
			evaluationCpu = getEvaluationFunctionLoadBalancingMax(pmsCpu, alpha);
			System.out.println(pmCpuMin.getId() + " (CPU) eval: " + evaluationCpu);
		}
		
		PhysicalMachine pmMemoryMin = pmsMemory.get(0);
		PhysicalMachine pmMemoryMax = pmsMemory.get(lastIndex);
		Collections.sort(pmMemoryMax.getVms(), new VirtualMachineMemoryComparator());
		eu.cactosfp7.cactoopt.models.VirtualMachine vmMemory = pmMemoryMax.getVms().get(0);
		double evaluationMemory = Double.MIN_VALUE;
		
		if (pmMemoryMin.assignVm(vmMemory)) {
			pmMemoryMax.unassignVm(vmMemory);
			evaluationMemory = getEvaluationFunctionLoadBalancingMax(pmsMemory, alpha);
			System.out.println(pmMemoryMin.getId() +" (memory) eval: " + evaluationMemory);
		}
		
		if ((evaluationCpu > evaluationOfCurrentState) && (evaluationCpu > evaluationMemory)) {
//			migrationSuggested = true;
			System.out.println("Migrate " + vmCpu.getId() + " from " + pmCpuMax.getId() + " to " + pmCpuMin.getId());
			migration = new VirtualMachineMigrationAction(vmCpu, pmCpuMax, pmCpuMin);
			for (PhysicalMachine pm : pms) {
				if (pm.getId() == pmCpuMin.getId()) {
					pm.assignVm(vmCpu);
				}
				if (pm.getId() == pmCpuMax.getId()) {
					pm.unassignVm(vmCpu);
				}
			}
		} else if (evaluationMemory > evaluationOfCurrentState) {
//			migrationSuggested = true;
			System.out.println("Migrate " + vmMemory.getId() + " from " + pmMemoryMax.getId() + " to " + pmMemoryMin.getId());
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
//			migrationSuggested = false;
			System.out.println("No migration");
		}
		
		return migration;
	}

	public static VirtualMachineMigrationAction migrationConsolidation(List<PhysicalMachine> pms) {
		VirtualMachineMigrationAction migration = null;
		double evaluationOfCurrentState = getEvaluationFunctionConsolidation(pms);
		
		// Create a deep copy of physical machines list
		List<PhysicalMachine> pmsConsolidation = new ArrayList<PhysicalMachine>();
		for (PhysicalMachine pm : pms) {
			pmsConsolidation.add(new PhysicalMachine(pm));
		}
		
		// Sort physical machines according to the residual capacity (smallest residual capacity first)
		Collections.sort(pmsConsolidation, new PhysicalMachineResidualComparator());
		
		int lastIndex = pmsConsolidation.size()-1; 
		
		
//		PhysicalMachine pmResidualMax = pmsConsolidation.get(lastIndex);
//		Collections.sort(pmResidualMax.vms, new VirtualMachineComparator());
		
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
					
					if (evaluation > evaluationOfCurrentState) {
						System.out.println("Migrate " + vm.getId() + " from " + pmResidualMax.getId() + " to " + pmResidualMin.getId());
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
						System.out.println("Eval: " + evaluation);
						
						return migration; // managed to place VM on PM, no need to continue
					} else {
						System.out.println("Migration " + vm.getId() + " from " + pmResidualMax.getId() + " to " + pmResidualMin.getId()
								+ " will not improve the DC evaluation");
						System.out.println("\t eval before migration: " + evaluation);
						System.out.println("\t eval after migration:  " + evaluation);
					}
				} else {
					System.out.println("No migration");
				}
			}
		}
		
		return null; // no migration possible
	}
	
	public static VirtualMachineMigrationAction migrationRandom(List<PhysicalMachine> pms) {
		VirtualMachineMigrationAction migration = null;
		
		Random rnd = new Random();
		
		int suggestMigration = rnd.nextInt(100);
		
		if (suggestMigration >= 25) {
			PhysicalMachine source = pms.get(rnd.nextInt(pms.size()));
			List<eu.cactosfp7.cactoopt.models.VirtualMachine> vms = source.getVms();
			eu.cactosfp7.cactoopt.models.VirtualMachine vm = vms.get(rnd.nextInt(vms.size()));
			PhysicalMachine target;
			
			do {
				target = pms.get(rnd.nextInt(pms.size()));
			} while (target.getId() == source.getId());
			
			if (target.assignVm(vm)) {
				source.unassignVm(vm);
				System.out.println("Migrate " + vm.getId() + " from " + source.getId() + " to " + target.getId());
				
				migration = new VirtualMachineMigrationAction(vm, source, target);	
			}
		}
		
		return migration;
	}
	
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
	
	public static double getEvaluationFunctionConsolidation(List<PhysicalMachine> pms) {
		double totalCost = 0;
		double residualMin = Double.MAX_VALUE;
		String residualMinId = "";
		for (PhysicalMachine pm : pms) {
			double residualLocal;
			if (pm.getVms().size() > 0) {
				totalCost += pm.getNoCores();
				residualLocal = pm.getResidualEvaluation();
				if ((residualLocal < residualMin) && (pm.getVms().size()>0)) {
					residualMin = residualLocal;
					residualMinId = pm.getId();
				}
			}
		}
		String x = residualMinId;
		return totalCost + residualMin;
	}

	/**
	 * Temporary solution to deploy VMs for the validation.
	 * Similar to initialPlacement block of the generateOptimizationPlan method.
	 *  
	 * @return the best matching ComputeNode
	 */
	public static ComputeNode initialVmPlacement(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm, VirtualMachine vmToAssign){
		OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
		SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
		plan.setOptimisationStep(rootStep);
		rootStep.setOptimisationPlan(plan);
		
		System.out.println("=============================================================================================");
		System.out.println("INITIAL PLACEMENT");
		
		System.out.println("-------------------------------");
		System.out.println("Indetifying the best Initial Placement for " + vmToAssign.toString());
		ComputeNode pm = getBestPm(pdcm, plm, ldcm, vmToAssign, 4);
		//		ComputeNode pm = getFirstPm(plm, ldcm, vm);
				
			
		

		System.out.println("=============================================================================================");
		return pm;
	}
	
//	public static boolean memoryUpscale(VirtualMachine vm, Amount<DataAmount> newSize) {
//		Hypervisor h = vm.getHypervisor();
//		ComputeNode pm = h.getNode();
//		Amount<DataAmount> availableMemory = Amount.valueOf(0, NonSI.BYTE);
//		
//		for (MemorySpecification memory : pm.getMemorySpecifications()) {
//			availableMemory = availableMemory.plus(memory.getSize());
//		}
//		log.info("whole memory @pm: " + availableMemory.toString());
//		for (VirtualMachine v : h.getVirtualMachines()) {
//			for (VirtualMemory memory : v.getVirtualMemoryUnits()) {
//				availableMemory = availableMemory.minus(memory.getProvisioned());
//			}
//		}
//		log.info("available memory @pm: " + availableMemory.doubleValue(SI.GIGA(NonSI.BYTE)) + "GB");
//
//		log.info("-------------------------------");
//		log.info("Memory upscale for VM " + vm.toString() + ", new memory size: " + newSize.toString());
//		
//		return true;
//	}
	
	static Ordering<Map.Entry<ComputeNode, Amount<DataAmount>>> byMapValues = new Ordering<Map.Entry<ComputeNode, Amount<DataAmount>>>() {
		   @Override
		   public int compare(Map.Entry<ComputeNode, Amount<DataAmount>> left, Map.Entry<ComputeNode, Amount<DataAmount>> right) {
		        return left.getValue().compareTo(right.getValue());
		   }
		};
}
