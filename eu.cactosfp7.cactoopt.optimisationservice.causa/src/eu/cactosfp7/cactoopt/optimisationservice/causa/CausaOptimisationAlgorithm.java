package eu.cactosfp7.cactoopt.optimisationservice.causa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.measure.quantity.DataAmount;
import javax.measure.quantity.DataRate;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
import eu.cactosfp7.cactoopt.util.CDOModelHelper;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.StorageMeasurement;
import eu.cactosfp7.infrastructuremodels.load.physical.Utilization;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VMImage;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VM_State;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualDisk;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMemory;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualProcessingUnit;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.util.CoreSwitch;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.AbstractNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ComputeNode;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.MemorySpecification;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.NodeState;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.ProcessingUnitSpecification;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.Rack;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.StorageSpecification;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.ManagePhysicalNodeAction;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationplanFactory;
import eu.cactosfp7.optimisationplan.SequentialSteps;
import eu.cactosfp7.optimisationplan.VmMigrationAction;
import se.umu.cs.ds.causa.algorithms.OptimizationAlgorithm;
import se.umu.cs.ds.causa.algorithms.PowerControlAlgorithm;
import se.umu.cs.ds.causa.algorithms.SingleMigrationLinKernighanMigration;
import se.umu.cs.ds.causa.algorithms.ThresholdPowerControl;
import se.umu.cs.ds.causa.algorithms.ConstraintProgrammingMigrationConsolidationMemory;
import se.umu.cs.ds.causa.algorithms.ConstraintProgrammingMigrationLoadBalancingMemory;
import se.umu.cs.ds.causa.algorithms.GreatDelugeMigrationLoadBalancing;
import se.umu.cs.ds.causa.algorithms.HighToLowMigrationRAMLoadBalancing;
import se.umu.cs.ds.causa.algorithms.LinKernighanMigration;
import se.umu.cs.ds.causa.constraints.Constraint;
import se.umu.cs.ds.causa.constraints.global.NoCPUCoreOverCommitGlobalConstraint;
import se.umu.cs.ds.causa.constraints.global.NoRAMOverCommitGlobalConstraint;
import se.umu.cs.ds.causa.constraints.global.StorageReservationGlobalConstraint;
import se.umu.cs.ds.causa.functions.cost.local.EnergyEfficiencyLocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.LoadBalancingRAMLocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.LocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.ResourceFragmentationCPUCoreRAMLocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.ServerConsolidationEmptyLocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.ServerConsolidationRAMLocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.global.EnergyEfficiencyGlobalCostFunction;
import se.umu.cs.ds.causa.functions.cost.global.LoadBalancingGlobalCostFunction;
import se.umu.cs.ds.causa.functions.cost.global.ResourceFragmentationGlobalCostFunction;
import se.umu.cs.ds.causa.functions.cost.global.ServerConsolidationGlobalCostFunction;
import se.umu.cs.ds.causa.models.AbstractMachine.CPU;
import se.umu.cs.ds.causa.models.AbstractMachine.CPU.Core;
import se.umu.cs.ds.causa.models.CoarseGrainedPowerModel;
import se.umu.cs.ds.causa.models.DataCenter;
import se.umu.cs.ds.causa.models.DataCenter.Configuration;
import se.umu.cs.ds.causa.models.DataCenter.Configuration.Mapping;
import se.umu.cs.ds.causa.models.OptimizationPlan;
import se.umu.cs.ds.causa.models.OptimizationPlan.Action;
import se.umu.cs.ds.causa.models.PhysicalMachine;
import se.umu.cs.ds.causa.models.PhysicalMachine.Id;
import se.umu.cs.ds.causa.models.PowerModel;
import se.umu.cs.ds.causa.models.VirtualMachine;
import se.umu.cs.ds.causa.simulator.Experiment;
import se.umu.cs.ds.causa.simulator.Simulator;
import se.umu.cs.ds.causa.simulator.Trace;
import se.umu.cs.ds.causa.util.TraceLogger;
//import se.umu.cs.ds.causa.demos.cactosy3.CactoOptIntegration;

/**
 * Causa optimisation algorithm.
 * 
 * @author jakub
 *
 */
public class CausaOptimisationAlgorithm implements IOptimisationAlgorithm {

	enum Algorithm {
		LOAD_BALANCING, CONSOLIDATION, ENERGY_EFFICIENCY, FRAGMENTATION, CP_LOAD_BALANCING, CP_CONSOLIDATION, GD_LOAD_BALANCING, HIGH_TO_LOW_LOAD_BALANCING, SINGLE_MIGRATION_LOAD_BALANCING, SINGLE_MIGRATION_CONSOLIDATION, NONE
	}

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(CausaOptimisationAlgorithm.class.getName());

	   
	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm,
			LogicalLoadModel llm) {

		log.info("Translation of CDO models into Causa models");

		DataCenter datacenter = null;
		Configuration configuration = null;

		try {
			// NOTE: using updated constraint model
//			datacenter = getDataCenterModelFromCdo(pdcm, ldcm);
			datacenter = getDataCenterModelFromCdo_withConstraints(pdcm, ldcm);
			configuration = getConfigurationFromCdo(pdcm, ldcm);
			// NOTE: adding labels and constraints
            datacenter = CactoOptIntegration.updateLabelsAndConstraints(datacenter,configuration);
		} catch (IllegalArgumentException e) {
			log.log(Level.SEVERE, "Data Center is in an invalid state/configuration!", e);
			return null;
		}
		
		OptimizationPlan[] migrationPlans = null;
		
		if (datacenter.getVirtualMachines().length == 0) {
			log.info("There are no VMs in the data center model. Optimisation of placement skipped!");
		} else {
			log.info("Causa optimisation algorithm starts");
	
			OptimizationAlgorithm algorithm = null;
	
                        //			final LocalCostFunction costLBRAM = LoadBalancingRAMLocalCostFunction.SINGLETON;
			final LocalCostFunction costLBRAM = LoadBalancingRAMLocalCostFunction.Factory.SINGLETON.getInstance(datacenter);
			final LocalCostFunction costSCRAM = ServerConsolidationRAMLocalCostFunction.SINGLETON;
	
			switch (CausaOptimisationConfigurable.chosenAlgorithm) {
			case LOAD_BALANCING:
				algorithm = new LinKernighanMigration(LoadBalancingGlobalCostFunction.getInstance(costLBRAM));
				break;
			case CONSOLIDATION:
				algorithm = new LinKernighanMigration(ServerConsolidationGlobalCostFunction.getInstance(costSCRAM));
				break;
			case ENERGY_EFFICIENCY:
				final PowerModel powerModel = CoarseGrainedPowerModel.SINGLETON;
				algorithm = new LinKernighanMigration(EnergyEfficiencyGlobalCostFunction.getInstance(powerModel));
				break;
			case FRAGMENTATION:
				final LocalCostFunction costRFCPUCoresRAM = ResourceFragmentationCPUCoreRAMLocalCostFunction.SINGLETON;
				algorithm = new LinKernighanMigration(ResourceFragmentationGlobalCostFunction.getInstance(costRFCPUCoresRAM));
				break;
//			case CP_LOAD_BALANCING:
//				algorithm = new ConstraintProgrammingMigrationLoadBalancingMemory(
//						LoadBalancingGlobalCostFunction.getInstance(costLBRAM));
//				break;
//			case CP_CONSOLIDATION:
//				final LocalCostFunction costSCEmpty = ServerConsolidationEmptyLocalCostFunction.SINGLETON;
//				algorithm = new ConstraintProgrammingMigrationConsolidationMemory(
//						ServerConsolidationGlobalCostFunction.getInstance(costSCEmpty));
//				break;
//			case GD_LOAD_BALANCING:
//				algorithm = new GreatDelugeMigrationLoadBalancing(LoadBalancingGlobalCostFunction.getInstance(costLBRAM));
//				break;
			case HIGH_TO_LOW_LOAD_BALANCING:
				algorithm = HighToLowMigrationRAMLoadBalancing.SINGLETON;
				break;
			case SINGLE_MIGRATION_LOAD_BALANCING:
				algorithm = new SingleMigrationLinKernighanMigration(LoadBalancingGlobalCostFunction.getInstance(costLBRAM));
				break;
			case SINGLE_MIGRATION_CONSOLIDATION:
				algorithm = new SingleMigrationLinKernighanMigration(ServerConsolidationGlobalCostFunction.getInstance(costSCRAM));
				break;
			case NONE:
				break;
			default:
				break;
			}
	
			int nrIterations;
			if (CausaOptimisationConfigurable.iterations != null) {
				nrIterations = CausaOptimisationConfigurable.iterations.intValue();
				log.info("Number of iterations (Causa optimisation steps): " + nrIterations);
			} else {
				nrIterations = 1;
				log.warning("Iterations parameter not set in the Causa configuration file. Default value: " + nrIterations + " is used.");
			}
			boolean gc = false;
	
			log.info("### Causa optimization algorithm: " + algorithm);

			if (algorithm != null) {
				try {
					log.info("### Causa optimization invocation @ " + System.currentTimeMillis());
					TraceLogger.saveLogEntry(CactoOptIntegration.PREFIX_TRACE_OPTIMIZATION,datacenter,configuration);
					//					Experiment experiment = new Experiment(datacenter, configuration);
					//					Trace trace = Simulator.runExperiment(experiment, algorithm, nrIterations, gc);
					//					migrationPlans = trace.getOptimizationPlans();

					// test / debug
					OptimizationPlan plan = algorithm.getOptimizationPlan(datacenter,configuration);
					TraceLogger.saveLogEntry(CactoOptIntegration.PREFIX_TRACE_OPTIMIZATION,datacenter,configuration,plan);
					log.info("### Causa optimization completed @ " + System.currentTimeMillis());
					// TODO: validate plans locally
					//                                        Configuration cfg = DataCenter.enact(configuration,plan);
					//                                        if (!datacenter.validate(cfg))
					//                                          throw new IllegalStateException("ILLEGAL STATE AT " + System.currentTimeMillis());
					migrationPlans = new OptimizationPlan[] {plan};
				} catch (Error e) {
					log.info("### Causa optimization failure @ " + System.currentTimeMillis() + " (" + e.getMessage() + ")");
					log.severe("Causa optimisation failed!");
				}
			}
		}

		log.info("Threshold Power Control");
		Id[] ids = getPMIdsFromCdo(pdcm);

		log.info("All physical machines:");
		for (Id id : ids) {
			log.info(id.getValue());
		}

		log.info("Only powered up physical machines:");
		for (PhysicalMachine pm : datacenter.getPhysicalMachines())
			log.info(pm.getId().getValue());

		List<OptimizationPlan> causaPlansList = new ArrayList<>();
		if (migrationPlans != null) {
			causaPlansList.addAll(Arrays.asList(migrationPlans));
			log.info(causaPlansList.size() + " migration(s) suggested");
		} else {
			log.info("No migration suggested");
		}

		if (CausaOptimisationConfigurable.managePhysicalNodeAction.booleanValue()) {
			log.info("Manage Physical Node Actions turned on");
			PowerControlAlgorithm powercontroller = new ThresholdPowerControl(ids);
			OptimizationPlan powerControlPlan = powercontroller.getOptimizationPlan(datacenter, configuration);
			
			if (powerControlPlan != null) {
				causaPlansList.add(powerControlPlan);
				log.info(powerControlPlan.getActions().length + " power control action(s) suggested");
	
				for (Action action : powerControlPlan.getActions()) {
					if (action instanceof OptimizationPlan.PowerDown) {
						OptimizationPlan.PowerDown causaPowerDown = (OptimizationPlan.PowerDown) action;
						log.info("Power down " + causaPowerDown.getPhysicalMachine());
	
					} else if (action instanceof OptimizationPlan.PowerUp) {
						OptimizationPlan.PowerUp causaPowerUp = (OptimizationPlan.PowerUp) action;
						log.info("Power up " + causaPowerUp.getPhysicalMachine());
					}
				}
			} else {
				log.info("No power control actions suggested");
			}
		} else {
			log.info("Manage Physical Node Actions turned off");
		}

		// NOTE: remove this call to to molproMigrationFiltering to disable post-optimization filtering
//		List<OptimizationPlan> filteredCausaPlansList = molproMigrationFiltering(causaPlansList, pdcm, ldcm, plm);
//		
////		OptimisationPlan plan = transformCausaOptimisationPlanToCdoOptimisationPlan(
////				causaPlansList.toArray(new OptimizationPlan[causaPlansList.size()]), pdcm, ldcm);
//		
//		OptimisationPlan plan = transformCausaOptimisationPlanToCdoOptimisationPlan(
//				filteredCausaPlansList.toArray(new OptimizationPlan[filteredCausaPlansList.size()]), pdcm, ldcm);
//
//		return plan;
		return transformCausaOptimisationPlanToCdoOptimisationPlan(causaPlansList.toArray(new OptimizationPlan[causaPlansList.size()]), pdcm, ldcm);
	}

	// private OptimisationPlan CdoEmptyOptimisationPlan() {
	// OptimisationPlan plan =
	// OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
	// log.info("CDO OptimisationPlan [" + plan.getId() + "]: ");
	// SequentialSteps rootStep =
	// OptimisationplanFactory.eINSTANCE.createSequentialSteps();
	// plan.setOptimisationStep(rootStep);
	// rootStep.setOptimisationPlan(plan);
	// rootStep.setExecutionStatus(ExecutionStatus.READY);
	//
	// plan.setCreationDate(new Date());
	// return plan;
	// }

// no pre- or -post-filtering of optimization, distorts optimization algorithms
//	private static List<OptimizationPlan> molproMigrationFiltering(
//			List<OptimizationPlan> causaOptimizationPlans, PhysicalDCModel pdcm,
//			LogicalDCModel ldcm, PhysicalLoadModel plm) {
//		List<OptimizationPlan> newCausaOptimizationPlans = new ArrayList<>();
//		
//		log.info("Filtering Molpro migrations.");
//		
//		for (OptimizationPlan causaOptimizationPlan : causaOptimizationPlans) {
//			List<OptimizationPlan.Action> actions = new ArrayList<>();
//			for (se.umu.cs.ds.causa.models.OptimizationPlan.Action action : causaOptimizationPlan.getActions()) {
//				if (action instanceof OptimizationPlan.Migration) {
//					OptimizationPlan.Migration causaMigration = (OptimizationPlan.Migration) action;
//					
//					String vmId = causaMigration.getVirtualMachine().getValue();
//					String sourceId = causaMigration.getSourcePhysicalMachine().getValue();
//					String destinationId = causaMigration.getDestinationPhysicalMachine().getValue();
//					
//					eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm = getVirtualMachineById(vmId, ldcm);
//					ComputeNode source = getComputeNodeById(sourceId, pdcm);
//					ComputeNode destination = getComputeNodeById(destinationId, pdcm);
//					
//					double availableStorageAtDestination = CDOModelHelper.getAvailableStorage(destination, plm);
//					
//					VM_State vmState = vm.getState();
//					
//					if (vmState != null) {
//						log.info("VM [" + vm.getName() + "] is in [" + vmState.toString() + "] state.");
//						switch (vmState) {
//						case NEW:
//							log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//									+ " New VM cannot be migrated!");
//							continue;
//					//	case PAUSED:
//					//		log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//					//				+ " Paused VM cannot be migrated!");
//					//		continue;
//						case IN_OPTIMISATION:
//							log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//									+ " VM is already in optimisation!");
//							continue;
//						case SHUTDOWN:
//							log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//									+ " Shutdown VM cannot be migrated!");
//							continue;
//						case UNASSIGNED:
//							log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//									+ " Unassigned VM cannot be migrated!");
//							continue;
//						case PLACED:
//							log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//									+ " Placed VM cannot be migrated!");
//							continue;
//						default:
//							break;
//						}
//					} else {
//						log.info("VM [" + vm.getName() + "] has no state!");
//					}
//					
//					String applicationType = vm.getInputParameters().get("applicationType");
//					
//					if (applicationType != null) {
//						log.info("VM [" + vm.getName() + "] has Application Type [" + applicationType + "].");
//						switch (applicationType) {
//						case "molpro-dft":
//							double molproDftVMImageStorageSize = CDOModelHelper.getVMImageStorageCapacity(vm, 220.0);
//							
//							if (availableStorageAtDestination < molproDftVMImageStorageSize) {
//								log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//										+ " Molpro-dft cannot be migrated to a compute node with available storage smaller than " + molproDftVMImageStorageSize +" GB!");
//								continue;
//							}
//							break;
//						case "molpro-lccsd":
//							double molproLccsdVMImageStorageSize = CDOModelHelper.getVMImageStorageCapacity(vm, 70.0);
//							
//							if (!hasLocalStorage(destination)) {
//								log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//										+ " Molpro-lccsd cannot be migrated to a disk-less node!");
//								continue;
//							}
//							if (availableStorageAtDestination < molproLccsdVMImageStorageSize) {
//								log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//										+ " Molpro-lccsd cannot be migrated to a compute node with available storage smaller than " + molproLccsdVMImageStorageSize +" GB!");
//								continue;
//							}
//							int noLccsdMolpro = CDOModelHelper.getNoLccsdMolpro(destination, ldcm);
//							if (noLccsdMolpro > 0) {
//								log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//										+ " Molpro-lccsd cannot be migrated to a compute node that already hosts another Molpro-lccsd job!");
//								continue;
//							}
//							break;
//						default:
//							log.info("No filters for application type [" + applicationType + "]");
//							break;
//						}
//					} else {
//						log.info("VM [" + vm.getName() + "] has no Application Type specified!");
//					}
//					
//					// For FLEXIANT testbed check if the PMs are in the same cluster (don't migrate VMs between clusters)
//					if (destination.getName().startsWith("10.15")
//							&& !destination.getName().startsWith(source.getName().substring(0, 7))) { 
//						log.info("Migration of VM [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] removed!"
//								+ " Migrations between clusters are not supported on Flex testbed!");
//						continue;
//					}
//					
//					actions.add(action);
//				}
//			}
//			OptimizationPlan newCausaOptimizationPlan = new OptimizationPlan(actions.toArray(new Action[actions.size()]));
//			newCausaOptimizationPlans.add(newCausaOptimizationPlan);
//		}
//		
//		return newCausaOptimizationPlans;
//	}

	private static boolean hasLocalStorage(ComputeNode computeNode) {
		double aggregateStorageCapacity = 0.0;
		double aggregateStoragePerformance = 0.0;
		for (StorageSpecification ss : computeNode.getStorageSpecifications()) {
			double storageCapacity;
			double storageRead;
			double storageWrite;

			if (ss.getSize() != null)
				storageCapacity = ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE));
			else
				storageCapacity = 0.0;

			if ((ss.getReadBandwidth() != null) && (ss.getReadBandwidth().getValue() != null))
				storageRead = ss.getReadBandwidth().getValue().doubleValue(DataRate.UNIT);
			else
				storageRead = 0.0;

			if ((ss.getWriteBandwidth() != null) && (ss.getWriteBandwidth().getValue() != null))
				storageWrite = ss.getWriteBandwidth().getValue().doubleValue(DataRate.UNIT);
			else
				storageWrite = 0.0;

			aggregateStorageCapacity += storageCapacity;
			aggregateStoragePerformance += storageRead / (1024 * 1024); // b/s to Mb/s
			aggregateStoragePerformance += storageWrite;

			log.info("CDO StorageSpecification " + ss.getName() + " [" + ss.getId() + "] size="
					+ ss.getSize() + " GB, read=" + storageRead + " Mb/s, write=" + storageWrite
					+ " Mb/s");
		}
		
		if (aggregateStorageCapacity > 0)
			return true;
		
		return false;
	}
	
	private static OptimisationPlan transformCausaOptimisationPlanToCdoOptimisationPlan(
			se.umu.cs.ds.causa.models.OptimizationPlan[] causaOptimizationPlans, PhysicalDCModel pdcm,
			LogicalDCModel ldcm) {

		OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
		log.info("CDO OptimisationPlan [" + plan.getId() + "]: ");
		SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
		plan.setOptimisationStep(rootStep);
		rootStep.setOptimisationPlan(plan);
		rootStep.setExecutionStatus(ExecutionStatus.READY);

		for (se.umu.cs.ds.causa.models.OptimizationPlan causaOptimizationPlan : causaOptimizationPlans) {
			for (se.umu.cs.ds.causa.models.OptimizationPlan.Action action : causaOptimizationPlan.getActions()) {
				if (action instanceof OptimizationPlan.Migration) {
					OptimizationPlan.Migration causaMigration = (OptimizationPlan.Migration) action;

					String vmId = causaMigration.getVirtualMachine().getValue();
					String sourceId = causaMigration.getSourcePhysicalMachine().getValue();
					String destinationId = causaMigration.getDestinationPhysicalMachine().getValue();

					VmMigrationAction migration = OptimisationplanFactory.eINSTANCE.createVmMigrationAction();

					migration.setMigratedVm(getVirtualMachineById(vmId, ldcm));
					migration.setSourceHost(getComputeNodeById(sourceId, pdcm).getHypervisor());
					migration.setTargetHost(getComputeNodeById(destinationId, pdcm).getHypervisor());
					migration.setSequentialSteps(rootStep);
					migration.setExecutionStatus(ExecutionStatus.READY);

					log.info("CDO VmMigrationAction [" + migration.getId() + "]: " + migration.getMigratedVm().getName()
							+ " from " + migration.getSourceHost().getId() + " to "
							+ migration.getTargetHost().getId());
				} else if (action instanceof OptimizationPlan.PowerDown) {
					OptimizationPlan.PowerDown causaPowerDown = (OptimizationPlan.PowerDown) action;

					String pmId = causaPowerDown.getPhysicalMachine().getValue();

					ManagePhysicalNodeAction powerDown = OptimisationplanFactory.eINSTANCE
							.createManagePhysicalNodeAction();
					powerDown.setManagedNode(getComputeNodeById(pmId, pdcm));
					powerDown.setTargetState(NodeState.OFF);
					powerDown.setSequentialSteps(rootStep);
					powerDown.setExecutionStatus(ExecutionStatus.READY);

					log.info("CDO ManagePhysicalNodeAction [" + powerDown.getId() + "]: change "
							+ powerDown.getManagedNode().getName() + " to state " + powerDown.getTargetState());
				} else if (action instanceof OptimizationPlan.PowerUp) {
					OptimizationPlan.PowerUp causaPowerUp = (OptimizationPlan.PowerUp) action;

					String pmId = causaPowerUp.getPhysicalMachine().getValue();

					ManagePhysicalNodeAction powerUp = OptimisationplanFactory.eINSTANCE
							.createManagePhysicalNodeAction();
					powerUp.setManagedNode(getComputeNodeById(pmId, pdcm));
					powerUp.setTargetState(NodeState.RUNNING);
					powerUp.setSequentialSteps(rootStep);
					powerUp.setExecutionStatus(ExecutionStatus.READY);

					log.info("CDO ManagePhysicalNodeAction [" + powerUp.getId() + "]: change "
							+ powerUp.getManagedNode().getName() + " to state " + powerUp.getTargetState());
				}
			}
		}

		plan.setCreationDate(new Date());
		return plan;
	}

	private static ComputeNode getComputeNodeById(String nodeId, PhysicalDCModel pdcm) {
		for (Rack rack : pdcm.getRacks()) {
			for (AbstractNode node : rack.getNodes()) {
				if (node instanceof ComputeNode) {
					if (nodeId.equals(node.getName()))
						return (ComputeNode) node;
				}
			}
		}

		return null;
	}

	private static eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine getVirtualMachineById(String vmId,
			LogicalDCModel ldcm) {
		for (Hypervisor h : ldcm.getHypervisors()) {
			for (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm : h.getVirtualMachines()) {
				if (vmId == vm.getId())
					return vm;
			}
		}

		return null;
	}

	private static Configuration getConfigurationFromCdo(PhysicalDCModel pdcm, LogicalDCModel ldcm) {
		List<Mapping> mappings = new ArrayList<>();

		log.info("CDO Physical DataCenter Model [" + pdcm.getId() + "]");
		log.info("CDO Logical DataCenter Model [" + ldcm.getId() + "]");

		for (Rack rack : pdcm.getRacks()) {
			for (AbstractNode node : rack.getNodes()) {
				if (node instanceof ComputeNode) {
					ComputeNode computeNode = (ComputeNode) node;

					log.info("CDO ComputeNode " + computeNode.getName() + " [" + computeNode.getId() + "] state="
							+ computeNode.getState());

					if (computeNode.getState() == NodeState.RUNNING) {
						String id = computeNode.getName();
						PhysicalMachine.Id pmId = new PhysicalMachine.Id(id);

						for (Hypervisor h : ldcm.getHypervisors()) {
							log.info("CDO Hypervisor [" + h.getId() + "]");
							if ((h.getNode() != null) && (h.getNode().getName().equals(id))) {
								for (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm : h
										.getVirtualMachines()) {

									log.info("CDO VirtualMachine " + vm.getName() + " [" + vm.getId() + "]");

									VirtualMachine.Id vmId = new VirtualMachine.Id(vm.getId());

									Mapping mapping = new Mapping(vmId, pmId);
									mappings.add(mapping);
								}
							}
						}
					}
				}
			}
		}

		Configuration configuration = new Configuration(mappings.toArray(new Mapping[mappings.size()]));

		return configuration;
	}

    //--------------------------------------------------------------------
    private static double getStorageCapacity (StorageSpecification ss)
    {
      if (ss != null)
//        if (ss.getSize() != null)
        if ((ss.getSize() != null) && (ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE)) > 0))
          return ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE));

      // NOTE: hardcoded due to lack of total network storage information in the CDO models
      //       nodes lacking local storage are assumed to use network storage
      final int STORAGE_NETWORK_NODESHARE = 2100 / 4;
      return STORAGE_NETWORK_NODESHARE;
    }

//    //--------------------------------------------------------------------
//    private static long getAggregateVMStorageCapacity (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm)
//	{
////      double aggregateVMStorageCapacity = vm.getVMImageInstance().getRootDisk().getCapacity().doubleValue(SI.GIGA(NonSI.BYTE));
//      if (vm.getVMImageInstance() == null)
//        return 0;
//      if (vm.getVMImageInstance().getRootDisk() == null)
//        return 0;
//      if (vm.getVMImageInstance().getRootDisk().getCapacity() == null)
//        return 0;
////      if (vm.getVMImageInstance().getRootDisk().getCapacity().doubleValue(SI.GIGA(NonSI.BYTE)) == null)
////        return 0;
//      return vm.getVMImageInstance().getRootDisk().getCapacity().longValue(SI.GIGA(NonSI.BYTE));
//    }

    //--------------------------------------------------------------------
    private static long getAggregateVMStorageCapacity (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm)
	{
//      double aggregateVMStorageCapacity = vm.getVMImageInstance().getRootDisk().getCapacity().doubleValue(SI.GIGA(NonSI.BYTE));
      if (vm.getVMImageInstance() == null)
        return 0;
      if (vm.getVMImageInstance().getRootDisk() == null)
        return 0;
      if (vm.getVMImageInstance().getRootDisk().getCapacity() == null)
        return 0;
//      if (vm.getVMImageInstance().getRootDisk().getCapacity().doubleValue(SI.GIGA(NonSI.BYTE)) == null)
//        return 0;
//      return vm.getVMImageInstance().getRootDisk().getCapacity().doubleValue(SI.GIGA(NonSI.BYTE));
      long value = CDOModelHelper.VM_SIZE_AGGREGATOR.doSwitch(vm.getVMImageInstance().getRootDisk());
//		Math.ceil( vm.getVMImageInstance().getRootDisk().getCapacity().doubleValue(SI.GIGA(NonSI.BYTE)));
      log.info("### VM Image Size [" + vm.getVMImageInstance().getId() + "] for [" + vm.getId() + "] == " + value + " GB");
      return value;
    }

//	//--------------------------------------------------------------------
//    private static boolean eligibleForPlacement (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm)
//    {
//      VM_State vmState = vm.getState();
//      if (vmState == null)
//      {
//  		log.info("unable to assess state of VM [" + vm.getName() + "]");
//  		return false;
//      }
//
//      log.info("VM [" + vm.getName() + "] is in [" + vmState.toString() + "] state.");
//      switch (vmState)
//      {
//        case NEW:
//          log.info("VM [" + vm.getName() + "] filtered (new VM cannot be migrated)");
//          return true;
//      }
//      return false;
//    }

	//--------------------------------------------------------------------
    private static boolean isEligibleForMigration (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm)
    {
      VM_State vmState = vm.getState();
      if (vmState == null)
      {
  		log.info("unable to assess state of VM [" + vm.getName() + "]");
  		return true;
      }

      log.info("VM [" + vm.getName() + "] is in [" + vmState.toString() + "] state.");
      switch (vmState)
      {
        case NEW:
          log.info("VM [" + vm.getName() + "] tagged (new VM cannot be migrated)");
          return false;
	//	case PAUSED:
	//		log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] prevented!"
	//				+ " Paused VM cannot be migrated!");
	//		continue;
        case IN_OPTIMISATION:
          log.info("VM [" + vm.getName() + "] tagged as immovable (VM is already in optimisation)");
          return false;
        case SHUTDOWN:
          log.info("VM [" + vm.getName() + "] tagged as immovable (shutdown VM cannot be migrated)");
          return false;
        case UNASSIGNED:
          log.info("VM [" + vm.getName() + "] tagged as immovable (unassigned VM cannot be migrated)");
          return false;
        case PLACED:
          log.info("VM [" + vm.getName() + "] tagged as immovable (placed VM cannot be migrated)");
          return false;
        case RUNNING:
          log.info("VM [" + vm.getName() + "] not tagged (running VMs can be migrated)");
          return true;
        default :
          log.info("WARNING: VM [" + vm.getName() + "] has unknown state: " + vmState + "!");
      }
      return true;
    }

	//--------------------------------------------------------------------
    private static se.umu.cs.ds.causa.models.VirtualMachine.Label[] toArray (se.umu.cs.ds.causa.models.VirtualMachine.Label label)
    {
//      if (label == null)
//        return new se.umu.cs.ds.causa.models.VirtualMachine.Label[0];
//      else
      return new se.umu.cs.ds.causa.models.VirtualMachine.Label[] {label};
    }

	//--------------------------------------------------------------------
    private static se.umu.cs.ds.causa.models.VirtualMachine.Label[] getVMJobTypeLabels (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm)
    {
      final String APPLICATION_TYPE = "applicationType";
      final String JOBTYPE_DFT = "molpro-dft";
      final String JOBTYPE_LCCSD = "molpro-lccsd";
      final se.umu.cs.ds.causa.models.VirtualMachine.Label[] EMPTY = new se.umu.cs.ds.causa.models.VirtualMachine.Label[0];

//      if (vm.getInputParameters() == null)
//        return EMPTY;
//      if (vm.getInputParameters().get(APPLICATION_TYPE) == null)
//        return EMPTY;
//      if (vm.getInputParameters().get(APPLICATION_TYPE).equals(JOBTYPE_DFT))
//        return toArray(CactoOptIntegration.VMLABEL_JOBTYPE_MOLPRO_DFT);
//      if (vm.getInputParameters().get(APPLICATION_TYPE).equals(JOBTYPE_LCCSD))
//        return toArray(CactoOptIntegration.VMLABEL_JOBTYPE_MOLPRO_LCCSD_KIZ);
//      return EMPTY;

      ArrayList<se.umu.cs.ds.causa.models.VirtualMachine.Label> list = new ArrayList<se.umu.cs.ds.causa.models.VirtualMachine.Label>();
      EMap<String,String> inputParameters = vm.getInputParameters();
      if (inputParameters != null)
      {
        String applicationType = inputParameters.get(APPLICATION_TYPE); 
	    if (applicationType != null)
	    {
          if (applicationType.equals(JOBTYPE_DFT))
            list.add(CactoOptIntegration.VMLABEL_JOBTYPE_MOLPRO_DFT);
          else if (applicationType.equals(JOBTYPE_LCCSD))
            list.add(CactoOptIntegration.VMLABEL_JOBTYPE_MOLPRO_LCCSD_KIZ);
        }
      }
      if (!isEligibleForMigration(vm))
        list.add(CactoOptIntegration.VMLABEL_IMMOVABLE);
      return list.toArray(new se.umu.cs.ds.causa.models.VirtualMachine.Label[list.size()]);
    }

//    //--------------------------------------------------------------------
//	private static DataCenter getDataCenterModelFromCdo(PhysicalDCModel pdcm, LogicalDCModel ldcm) {
//		List<PhysicalMachine> physicalMachines = new ArrayList<>();
//		List<VirtualMachine> virtualMachines = new ArrayList<>();
//
//		// List<Mapping> mappings = new
//		// ArrayList<DataCenter.Configuration.Mapping>();
//
//		log.info("CDO Physical DataCenter Model [" + pdcm.getId() + "]");
//		log.info("CDO Logical DataCenter Model [" + ldcm.getId() + "]");
//
//		for (Rack rack : pdcm.getRacks()) {
//			for (AbstractNode node : rack.getNodes()) {
//				if (node instanceof ComputeNode) {
//					ComputeNode computeNode = (ComputeNode) node;
//
//					log.info("CDO ComputeNode " + computeNode.getName() + " [" + computeNode.getId() + "] state="
//							+ computeNode.getState());
//
//					if (computeNode.getState() == NodeState.RUNNING) {
//						String id = computeNode.getName();
//						PhysicalMachine.Id pmId = new PhysicalMachine.Id(id);
//						int noCores = 0;
//						int totalMemory = 0;
//						//int totalMemory = -2048; // Buffer for Hypervisor
//
//						EList<ProcessingUnitSpecification> cpuSpecs = computeNode.getCpuSpecifications();
//
//						List<CPU> cpus = new ArrayList<>();
//
//						for (ProcessingUnitSpecification pus : cpuSpecs) {
//
//							log.info("CDO ProcessingUnitSpecification " + pus.getName() + " [" + pus.getId()
//									+ "] #cores=" + pus.getNumberOfCores() + ", freq=" + pus.getFrequency());
//
//							noCores += pus.getNumberOfCores();
//							List<Core> cores = new ArrayList<>();
//							// Core core = new
//							// Core((int)Math.round(pus.getFrequency().doubleValue(SI.HERTZ)));
//							for (int i = 0; i < noCores; i++) {
//								int freq = (int) Math.round(pus.getFrequency().to(SI.MEGA(SI.HERTZ)).getEstimatedValue());
//								
//								if ((freq < 1000) || (freq > 4000)) {
//									int oldFreq = freq;
//									freq = 2500;
//									log.warning("CPU frequency of [" + oldFreq + "] out of the predefined bounds [1000 MHz - 4000 MHz]!"
//											+ " CPU frequency of " + freq + " MHz will be used instead!");
//								}
//								
//								Core core = new Core(freq);
//								cores.add(core);
//							}
//							CPU cpu = new CPU(cores.toArray(new Core[cores.size()]));
//							cpus.add(cpu);
//						}
//
//						for (MemorySpecification ms : computeNode.getMemorySpecifications()) {
//							log.info("CDO MemorySpecification " + ms.getName() + " [" + ms.getId() + "] size="
//									+ ms.getSize());
//							totalMemory += ms.getSize().to(SI.MEGA(NonSI.BYTE)).getEstimatedValue();
//						}
//
//						double aggregateStorageCapacity = 0.0;
////						double aggregateStoragePerformance = 0.0;
//						for (StorageSpecification ss : computeNode.getStorageSpecifications()) {
//							double storageCapacity;
//							double storageRead;
//							double storageWrite;
//							
////							if (ss.getSize() != null)
////								storageCapacity = ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE));
////							else 
////								storageCapacity = 0.0;
//							storageCapacity = getStorageCapacity(ss);
//							
//							if ((ss.getReadBandwidth() != null) && (ss.getReadBandwidth().getValue() != null))
//								storageRead = ss.getReadBandwidth().getValue().doubleValue(DataRate.UNIT);
//							else 
//								storageRead = 0.0;
//							
//							if ((ss.getWriteBandwidth() != null) && (ss.getWriteBandwidth().getValue() != null))
//								storageWrite = ss.getWriteBandwidth().getValue().doubleValue(DataRate.UNIT);
//							else 
//								storageWrite = 0.0;
//							
//							aggregateStorageCapacity += storageCapacity;
////							aggregateStoragePerformance += storageRead;
////							aggregateStoragePerformance += storageWrite;
//							
//							log.info("CDO StorageSpecification " + ss.getName() + " [" + ss.getId() + "] size=" + ss.getSize() + " GB, read=" + storageRead + " bps, write=" + storageWrite + " bps");
//						}
//						
//						int networkCapacity = 0;
//						int networkPerformance = 0;
//						
//						for (Hypervisor h : ldcm.getHypervisors()) {
//							log.info("CDO Hypervisor [" + h.getId() + "]");
//							if ((h.getNode() != null) && (h.getNode().getName().equals(id))) {
//								for (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm : h
//										.getVirtualMachines()) {
//									log.info("CDO VirtualMachine " + vm.getName() + " [" + vm.getId() + "]");
//
//									VirtualMachine.Id vmId = new VirtualMachine.Id(vm.getId());
//
//									long vmMemory = 0;
//
//									List<CPU> vmCPUs = new ArrayList<>();
//
//									for (VirtualProcessingUnit vpu : vm.getVirtualProcessingUnits()) {
//										log.info("CDO VirtualProcessingUnit " + vpu.getName() + " [" + vpu.getId() + "]"
//												+ "#virtualCores=" + vpu.getVirtualCores());
//										// int vmNoCores = vpu.ge;
//										// List<Core> vmCores = new
//										// ArrayList<Core>();
//										// for(int i=0; i<noCores; i++) {
//										// Core vCore = new
//										// Core((int)Math.round(vpu.getFrequency().getEstimatedValue()));
//										Core vCore = new Core(2000);
//										// vmCores.add(vCore);
//										// }
//										Core[] vCores = new Core[1];
//										vCores[0] = vCore;
//										// vmCores.add(core);
//										// CPU vCPU = new
//										// CPU(vmCores.toArray(new
//										// Core[vmCores.size()]));
//										CPU vCPU = new CPU(vCores);
//										vmCPUs.add(vCPU);
//									}
//
//									// if VM doesn't have any CPUs assigned add
//									// one
//									// (for compatibility with Causa - CPU freq)
//									if (vmCPUs.size() == 0) {
//										Core vCore = new Core(2000);
//										Core[] vCores = new Core[1];
//										vCores[0] = vCore;
//										CPU vCPU = new CPU(vCores);
//										vmCPUs.add(vCPU);
//									}
//
//									for (VirtualMemory vmem : vm.getVirtualMemoryUnits()) {
//										if (vmem == null) {
//											log.log(Level.WARNING, "vmem is null for vm " + vm.getName());
//											continue;
//										}
//										if (vmem.getProvisioned() == null) {
//											log.log(Level.WARNING,
//													"vmem.getProvisioned() is null for vm " + vm.getName());
//											continue;
//										}
//										log.info("CDO VirtualMemory [" + vmem.getId() + "] size="
//												+ vmem.getProvisioned());
//										vmMemory += vmem.getProvisioned().getEstimatedValue();
//									}
//							
//									// NOTE: skipping a VM causes the VM reference to be unresolved in causa
////									if(vm == null) {
////										log.severe("vm is null: ignoring"); continue;
////									} else if(vm.getVMImageInstance() == null) {
////										log.severe("vm (" + vm + ") image instance is null: ignoring"); continue;
////									} else if(null == vm.getVMImageInstance().getRootDisk()) {
////										log.severe("vm (" + vm + ") image instance (" + vm.getVMImageInstance() + ") root disk is null: ignoring"); continue;
////									} else if(null == vm.getVMImageInstance().getRootDisk().getCapacity()) {
////										log.severe("vm image instance root disk capacity is null: ignoring"); continue;
////									} 
//
////									double aggregateVMStorageCapacity = vm.getVMImageInstance().getRootDisk().getCapacity().doubleValue(SI.GIGA(NonSI.BYTE));
//									double aggregateVMStorageCapacity = getAggregateVMStorageCapacity(vm);
//									log.info("VM[" + vm.getId() + " @ " + vm + "] disk allocation: " + aggregateVMStorageCapacity);
//									double aggregateVMStoragePerformance = 0.0;
////									for (StorageSpecification ss : vm.getStorageSpecifications()) {
////										double storageCapacity;
////										double storageRead;
////										double storageWrite;
////										
////										if (ss.getSize() != null)
////											storageCapacity = ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE));
////										else 
////											storageCapacity = 0.0;
////										
////										if (ss.getReadBandwidth() != null)
////											storageRead = ss.getReadBandwidth().getValue().getEstimatedValue();
////										else 
////											storageRead = 0.0;
////										
////										if (ss.getWriteBandwidth() != null)
////											storageWrite = ss.getWriteBandwidth().getValue().getEstimatedValue();
////										else 
////											storageWrite = 0.0;
////										
////										aggregateStorageCapacity += storageCapacity;
////										aggregateStoragePerformance += storageRead;
////										aggregateStoragePerformance += storageWrite;
////										
////										log.info("CDO StorageSpecification " + ss.getName() + " [" + ss.getId() + "] size=" + ss.getSize() + " GB, read=" + storageRead + " bps, write=" + storageWrite + " bps");
////									}
//									
//									// CPU[] vcpus = new CPU[1];
//									// vcpus[0] = new CPU(cores.toArray(new
//									// Core[cores.size()]));
//
//									VirtualMachine virtualMachine = new VirtualMachine(vmId,
//											vmCPUs.toArray(new CPU[vmCPUs.size()]), (int) vmMemory, (int) Math.round(aggregateVMStorageCapacity),
//											(int) Math.round(aggregateVMStoragePerformance), networkCapacity, networkPerformance);
//
//									virtualMachines.add(virtualMachine);
//
//									// Mapping mapping = new Mapping(vmId,
//									// pmId);
//									// mappings.add(mapping);
//								}
//							}
//						}
//						
//						int noLccsdMolpro = CDOModelHelper.getNoLccsdMolpro(computeNode, ldcm);
//						
//						int storagePerformance;
//						
//						if (noLccsdMolpro <= 0) {
//							log.info("Physical Machine [" + computeNode.getName() + "] doesn't host any LCCSD Molpro jobs.");
//							storagePerformance = 1;
//						} else {
//							log.info("Physical Machine [" + computeNode.getName() + "] hosts already " + noLccsdMolpro + " LCCSD Molpro jobs.");
//							storagePerformance = 0;
//						}
//
//						PhysicalMachine pm = new PhysicalMachine(new Id(id), cpus.toArray(new CPU[cpus.size()]),
//								totalMemory, (int) Math.round(aggregateStorageCapacity),
//								storagePerformance,
////								(int) Math.round(aggregateStoragePerformance),
//								networkCapacity, networkPerformance);
//
//						physicalMachines.add(pm);
//					}
//				}
//			}
//		}
//
////        final int storagePMOSReservationInGB = 1;
////		return new DataCenter(physicalMachines.toArray(new PhysicalMachine[physicalMachines.size()]),
////				virtualMachines.toArray(new VirtualMachine[virtualMachines.size()]), new Constraint[] {
////                                        NoCPUCoreOverCommitGlobalConstraint.SINGLETON, NoRAMOverCommitGlobalConstraint.SINGLETON,
////                                        StorageReservationGlobalConstraint.getInstance(storagePMOSReservationInGB) });
//
//        return new DataCenter(physicalMachines.toArray(new PhysicalMachine[physicalMachines.size()]),
//		virtualMachines.toArray(new VirtualMachine[virtualMachines.size()]), new Constraint[] {
//                                NoCPUCoreOverCommitGlobalConstraint.SINGLETON, NoRAMOverCommitGlobalConstraint.SINGLETON });
//	}

	//--------------------------------------------------------------------
	private static DataCenter getDataCenterModelFromCdo_withConstraints (PhysicalDCModel pdcm, LogicalDCModel ldcm) {
		List<PhysicalMachine> physicalMachines = new ArrayList<>();
		List<VirtualMachine> virtualMachines = new ArrayList<>();

		// List<Mapping> mappings = new
		// ArrayList<DataCenter.Configuration.Mapping>();

		log.info("CDO Physical DataCenter Model [" + pdcm.getId() + "]");
		log.info("CDO Logical DataCenter Model [" + ldcm.getId() + "]");

		for (Rack rack : pdcm.getRacks()) {
			for (AbstractNode node : rack.getNodes()) {
				if (node instanceof ComputeNode) {
					ComputeNode computeNode = (ComputeNode) node;

					log.info("CDO ComputeNode " + computeNode.getName() + " [" + computeNode.getId() + "] state="
							+ computeNode.getState());

					if (computeNode.getState() == NodeState.RUNNING) {
						String id = computeNode.getName();
						PhysicalMachine.Id pmId = new PhysicalMachine.Id(id);
						int noCores = 0;
						long totalMemory = 0;
						//int totalMemory = -2048; // Buffer for Hypervisor

						EList<ProcessingUnitSpecification> cpuSpecs = computeNode.getCpuSpecifications();

						List<CPU> cpus = new ArrayList<>();

						for (ProcessingUnitSpecification pus : cpuSpecs) {

							log.info("CDO ProcessingUnitSpecification " + pus.getName() + " [" + pus.getId()
									+ "] #cores=" + pus.getNumberOfCores() + ", freq=" + pus.getFrequency());

							noCores += pus.getNumberOfCores();
							List<Core> cores = new ArrayList<>();
							// Core core = new
							// Core((int)Math.round(pus.getFrequency().doubleValue(SI.HERTZ)));
							for (int i = 0; i < noCores; i++) {
								int freq = (int) Math.round(pus.getFrequency().to(SI.MEGA(SI.HERTZ)).getEstimatedValue());
								
								if ((freq < 1000) || (freq > 4000)) {
									int oldFreq = freq;
									freq = 2500;
									log.warning("CPU frequency of [" + oldFreq + "] out of the predefined bounds [1000 MHz - 4000 MHz]!"
											+ " CPU frequency of " + freq + " MHz will be used instead!");
								}
								
								Core core = new Core(freq);
								cores.add(core);
							}
							CPU cpu = new CPU(cores.toArray(new Core[cores.size()]));
							cpus.add(cpu);
						}

						for (MemorySpecification ms : computeNode.getMemorySpecifications()) {
							log.info("CDO MemorySpecification " + ms.getName() + " [" + ms.getId() + "] size=" + ms.getSize());
//							totalMemory += ms.getSize().to(SI.MEGA(NonSI.BYTE)).getEstimatedValue();
							totalMemory += ms.getSize().longValue(SI.GIGA(NonSI.BYTE)); //.getEstimatedValue();
						}

						double aggregateStorageCapacity = 0.0;
//						double aggregateStoragePerformance = 0.0;
						for (StorageSpecification ss : computeNode.getStorageSpecifications()) {
							double storageCapacity;
							double storageRead;
							double storageWrite;
							
//							if (ss.getSize() != null)
//								storageCapacity = ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE));
//							else 
//								storageCapacity = 0.0;
							storageCapacity = getStorageCapacity(ss);
							
							if ((ss.getReadBandwidth() != null) && (ss.getReadBandwidth().getValue() != null))
								storageRead = ss.getReadBandwidth().getValue().doubleValue(DataRate.UNIT);
							else 
								storageRead = 0.0;
							
							if ((ss.getWriteBandwidth() != null) && (ss.getWriteBandwidth().getValue() != null))
								storageWrite = ss.getWriteBandwidth().getValue().doubleValue(DataRate.UNIT);
							else 
								storageWrite = 0.0;
							
							aggregateStorageCapacity += storageCapacity;
//							aggregateStoragePerformance += storageRead;
//							aggregateStoragePerformance += storageWrite;
							
							log.info("CDO StorageSpecification " + ss.getName() + " [" + ss.getId() + "] size=" + ss.getSize() + " GB, read=" + storageRead + " bps, write=" + storageWrite + " bps");
						}
						
						int networkCapacity = 0;
						int networkPerformance = 0;
						
						for (Hypervisor h : ldcm.getHypervisors()) {
							log.info("CDO Hypervisor [" + h.getId() + "]");
							if ((h.getNode() != null) && (h.getNode().getName().equals(id))) {
								for (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm : h.getVirtualMachines()) {
									log.info("CDO VirtualMachine " + vm.getName() + " [" + vm.getId() + "]");

									// NOTE: do NOT use prefiltering (deprives causa from needed data)
									//       also no post-filtering (distorts optimization results)
									// filter out VMs not eligible for migration prior to optimization
//									if (!eligibleForMigration(vm))
//										continue;

									VirtualMachine.Id vmId = new VirtualMachine.Id(vm.getId());

									long vmMemory = 0;

									List<CPU> vmCPUs = new ArrayList<>();

									for (VirtualProcessingUnit vpu : vm.getVirtualProcessingUnits()) {
										log.info("CDO VirtualProcessingUnit " + vpu.getName() + " [" + vpu.getId() + "]"
												+ "#virtualCores=" + vpu.getVirtualCores());
										// int vmNoCores = vpu.ge;
										// List<Core> vmCores = new
										// ArrayList<Core>();
										// for(int i=0; i<noCores; i++) {
										// Core vCore = new
										// Core((int)Math.round(vpu.getFrequency().getEstimatedValue()));
										Core vCore = new Core(2000);
										// vmCores.add(vCore);
										// }
										Core[] vCores = new Core[1];
										vCores[0] = vCore;
										// vmCores.add(core);
										// CPU vCPU = new
										// CPU(vmCores.toArray(new
										// Core[vmCores.size()]));
										CPU vCPU = new CPU(vCores);
										vmCPUs.add(vCPU);
									}

									// if VM doesn't have any CPUs assigned add
									// one
									// (for compatibility with Causa - CPU freq)
									if (vmCPUs.size() == 0) {
										Core vCore = new Core(2000);
										Core[] vCores = new Core[1];
										vCores[0] = vCore;
										CPU vCPU = new CPU(vCores);
										vmCPUs.add(vCPU);
									}

									for (VirtualMemory vmem : vm.getVirtualMemoryUnits()) {
										if (vmem == null) {
											log.log(Level.WARNING, "vmem is null for vm " + vm.getName());
											continue;
										}
										if (vmem.getProvisioned() == null) {
											log.log(Level.WARNING,
													"vmem.getProvisioned() is null for vm " + vm.getName());
											continue;
										}
										log.info("CDO VirtualMemory [" + vmem.getId() + "] size="
												+ vmem.getProvisioned());
//										vmMemory += vmem.getProvisioned().getEstimatedValue();
										// NOTE: updated so it's calculated the same way in both placement and optimization
										//       should be a long? later converted to an int?
//										vmMemory += vmem.getProvisioned().doubleValue(SI.MEGA(NonSI.BYTE));
										vmMemory += vmem.getProvisioned().longValue(SI.GIGA(NonSI.BYTE));
										log.info("### VM[" + vmId + "] RAM (part): " + vmem.getProvisioned().longValue(SI.GIGA(NonSI.BYTE)));
									}
									log.info("### VM[" + vmId + "] total RAM: " + vmMemory);
							
									// NOTE: skipping a VM causes the VM reference to be unresolved in causa
//									if(vm == null) {
//										log.severe("vm is null: ignoring"); continue;
//									} else if(vm.getVMImageInstance() == null) {
//										log.severe("vm (" + vm + ") image instance is null: ignoring"); continue;
//									} else if(null == vm.getVMImageInstance().getRootDisk()) {
//										log.severe("vm (" + vm + ") image instance (" + vm.getVMImageInstance() + ") root disk is null: ignoring"); continue;
//									} else if(null == vm.getVMImageInstance().getRootDisk().getCapacity()) {
//										log.severe("vm image instance root disk capacity is null: ignoring"); continue;
//									} 

//									double aggregateVMStorageCapacity = vm.getVMImageInstance().getRootDisk().getCapacity().doubleValue(SI.GIGA(NonSI.BYTE));
									double aggregateVMStorageCapacity = getAggregateVMStorageCapacity(vm);
									log.info("VM[" + vm.getId() + " @ " + vm + "] disk allocation: " + aggregateVMStorageCapacity);
									double aggregateVMStoragePerformance = 0.0;
//									for (StorageSpecification ss : vm.getStorageSpecifications()) {
//										double storageCapacity;
//										double storageRead;
//										double storageWrite;
//										
//										if (ss.getSize() != null)
//											storageCapacity = ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE));
//										else 
//											storageCapacity = 0.0;
//										
//										if (ss.getReadBandwidth() != null)
//											storageRead = ss.getReadBandwidth().getValue().getEstimatedValue();
//										else 
//											storageRead = 0.0;
//										
//										if (ss.getWriteBandwidth() != null)
//											storageWrite = ss.getWriteBandwidth().getValue().getEstimatedValue();
//										else 
//											storageWrite = 0.0;
//										
//										aggregateStorageCapacity += storageCapacity;
//										aggregateStoragePerformance += storageRead;
//										aggregateStoragePerformance += storageWrite;
//										
//										log.info("CDO StorageSpecification " + ss.getName() + " [" + ss.getId() + "] size=" + ss.getSize() + " GB, read=" + storageRead + " bps, write=" + storageWrite + " bps");
//									}
									
									// CPU[] vcpus = new CPU[1];
									// vcpus[0] = new CPU(cores.toArray(new
									// Core[cores.size()]));

									VirtualMachine virtualMachine = new VirtualMachine(vmId, getVMJobTypeLabels(vm),
											vmCPUs.toArray(new CPU[vmCPUs.size()]), (int) vmMemory, (int) Math.round(aggregateVMStorageCapacity),
											(int) Math.round(aggregateVMStoragePerformance), networkCapacity, networkPerformance);

									virtualMachines.add(virtualMachine);

									// Mapping mapping = new Mapping(vmId,
									// pmId);
									// mappings.add(mapping);
								}
							}
						}
						
						int noLccsdMolpro = CDOModelHelper.getNoLccsdMolpro(computeNode, ldcm);
						
						int storagePerformance;
						
						if (noLccsdMolpro <= 0) {
							log.info("Physical Machine [" + computeNode.getName() + "] doesn't host any LCCSD Molpro jobs.");
							storagePerformance = 1;
						} else {
							log.info("Physical Machine [" + computeNode.getName() + "] hosts already " + noLccsdMolpro + " LCCSD Molpro jobs.");
							storagePerformance = 0;
						}

						PhysicalMachine pm = new PhysicalMachine(new Id(id), cpus.toArray(new CPU[cpus.size()]),
								(int)totalMemory, (int) Math.round(aggregateStorageCapacity),
								storagePerformance,
//								(int) Math.round(aggregateStoragePerformance),
								networkCapacity, networkPerformance);

						physicalMachines.add(pm);
					}
				}
			}
		}

        DataCenter datacenter = new DataCenter(physicalMachines.toArray(new PhysicalMachine[physicalMachines.size()]),
                                               virtualMachines.toArray(new VirtualMachine[virtualMachines.size()]), new Constraint[0]);
//        return CactoOptIntegration.updateLabelsAndConstraints(datacenter);
        return datacenter;
	}

	private static Id[] getPMIdsFromCdo(PhysicalDCModel pdcm) {
		List<Id> ids = new ArrayList<>();

		for (Rack rack : pdcm.getRacks()) {
			log.info("CDO Rack " + rack.getName() + " [" + rack.getId() + "]");
			for (AbstractNode node : rack.getNodes()) {
				if (node instanceof ComputeNode) {
					ComputeNode computeNode = (ComputeNode) node;
					log.info("CDO ComputeNode " + computeNode.getName() + " [" + computeNode.getId() + "] state="
							+ computeNode.getState());

					ids.add(new Id(computeNode.getName()));
				}
			}
		}

		return ids.toArray(new Id[ids.size()]);
	}

	// private static PhysicalMachine.Id[] getIds(PM[] pms) {
	// PhysicalMachine.Id[] ids = new PhysicalMachine.Id[pms.length];
	// for (int i = 0; i < ids.length; i++)
	// ids[i] = new PhysicalMachine.Id(pms[i].getId());
	// return ids;
	// }
}
