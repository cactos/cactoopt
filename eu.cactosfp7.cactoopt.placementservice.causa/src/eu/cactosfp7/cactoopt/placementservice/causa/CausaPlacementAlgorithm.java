package eu.cactosfp7.cactoopt.placementservice.causa;

import java.util.ArrayList;
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

import eu.cactosfp7.cactoopt.placementservice.InitialPlacementAlgorithm;
import eu.cactosfp7.cactoopt.util.CDOModelHelper;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.StorageMeasurement;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VMImage;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VM_State;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualDisk;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
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
import eu.cactosfp7.infrastructuremodels.physicaldc.power.specification.PowerModel;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationplanFactory;
import eu.cactosfp7.optimisationplan.SequentialSteps;
import eu.cactosfp7.optimisationplan.VmPlacementAction;
import se.umu.cs.ds.causa.algorithms.BestFitPlacement;
//import se.umu.cs.ds.causa.algorithms.MolproBestFitPlacement;
import se.umu.cs.ds.causa.algorithms.PlacementAlgorithm;
import se.umu.cs.ds.causa.constraints.Constraint;
import se.umu.cs.ds.causa.constraints.global.NoCPUCoreOverCommitGlobalConstraint;
import se.umu.cs.ds.causa.constraints.global.NoRAMOverCommitGlobalConstraint;
import se.umu.cs.ds.causa.constraints.global.StorageReservationGlobalConstraint;
import se.umu.cs.ds.causa.functions.cost.local.EnergyEfficiencyLocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.LoadBalancingRAMLocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.LocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.MolproPlacementLocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.ResourceFragmentationCPUCoreRAMLocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.ServerConsolidationEmptyLocalCostFunction;
import se.umu.cs.ds.causa.functions.cost.local.ServerConsolidationRAMLocalCostFunction;
//import se.umu.cs.ds.causa.functions.cost.global.GlobalCostFunction;
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
import se.umu.cs.ds.causa.models.Machine;
import se.umu.cs.ds.causa.models.OptimizationPlan;
import se.umu.cs.ds.causa.models.PhysicalMachine;
import se.umu.cs.ds.causa.models.PhysicalMachine.Id;
import se.umu.cs.ds.causa.util.TraceLogger;
//import se.umu.cs.ds.causa.demos.cactosy3.CactoOptIntegration;

/**
 * Causa initial placement algorithm
 * 
 * @author jakub
 *
 */
public class CausaPlacementAlgorithm implements InitialPlacementAlgorithm {

	enum Algorithm {
		NONE, BEST_FIT, 
		MOLPRO_BEST_FIT, MOLPRO_LOAD_BALANCING_RAM, MOLPRO_CONSOLIDATION_RAM, MOLPRO_CONSOLIDATION, MOLPRO_FRAGMENTATION, MOLPRO_ENERGY_EFFICIENCY, 
		CONSOLIDATION_RAM, CONSOLIDATION, LOAD_BALANCING_RAM, FRAGMENTATION, ENERGY_EFFICIENCY
	}

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(CausaPlacementAlgorithm.class.getName());
	
	   

	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm,
			LogicalLoadModel llm, List<VirtualMachine> vmsToPlace) {

		log.info("Translation of CDO models into Causa models");

		// NOTE: using updated constraint model
//		DataCenter datacenter = getDataCenterModelFromCdo(pdcm, ldcm, plm, vmsToPlace);
		DataCenter datacenter = getDataCenterModelFromCdo_withConstraints(pdcm, ldcm, plm, vmsToPlace);
		Configuration configuration = getConfigurationFromCdo(pdcm, ldcm);
		// NOTE: adding labels and constraints
        datacenter = CactoOptIntegration.updateLabelsAndConstraints(datacenter,configuration);

		if (CausaPlacementConfigurable.chosenAlgorithm == null) {
			log.info("Causa placement algorithm not set. Modify cactoopt_placement_causa.cfg configuration file!");
			return null;
		}

		log.info("Causa placement algorithm starts");

		PlacementAlgorithm algorithm = null;

		final Machine.Selector selectorSizeRAM = new se.umu.cs.ds.causa.models.VirtualMachine.SizeSelector(
				se.umu.cs.ds.causa.models.VirtualMachine.RAMSizeMetric.SINGLETON);
                //		final LocalCostFunction costLBRAM = LoadBalancingRAMLocalCostFunction.SINGLETON;
		final LocalCostFunction costLBRAM = LoadBalancingRAMLocalCostFunction.Factory.SINGLETON.getInstance(datacenter);
		final LocalCostFunction costConsolidationRAM = ServerConsolidationRAMLocalCostFunction.SINGLETON;
		final LocalCostFunction costConsolidation = ServerConsolidationEmptyLocalCostFunction.SINGLETON;
		final LocalCostFunction costFragmentation = ResourceFragmentationCPUCoreRAMLocalCostFunction.SINGLETON;
		final se.umu.cs.ds.causa.models.PowerModel powerModel = CoarseGrainedPowerModel.SINGLETON;
		final LocalCostFunction costEnergy = EnergyEfficiencyLocalCostFunction.getInstance(powerModel);
//		final LocalCostFunction costMolpro = MolproPlacementLocalCostFunction.SINGLETON;

		switch (CausaPlacementConfigurable.chosenAlgorithm) {
		case BEST_FIT:
		case LOAD_BALANCING_RAM:
			algorithm = new BestFitPlacement(selectorSizeRAM, LoadBalancingGlobalCostFunction.getInstance(costLBRAM));
			break;
		case CONSOLIDATION_RAM:
		case CONSOLIDATION:
			algorithm = new BestFitPlacement(selectorSizeRAM, ServerConsolidationGlobalCostFunction.getInstance(costConsolidationRAM));
			break;
                        //		case CONSOLIDATION:
                        //			algorithm = new BestFitPlacement(selectorSizeRAM, costConsolidation);
                        //			break;
		case FRAGMENTATION:
			algorithm = new BestFitPlacement(selectorSizeRAM, ResourceFragmentationGlobalCostFunction.getInstance(costFragmentation));
			break;
		case ENERGY_EFFICIENCY:
			algorithm = new BestFitPlacement(selectorSizeRAM, EnergyEfficiencyGlobalCostFunction.getInstance(powerModel));
			break;
/*
//		case MOLPRO_BEST_FIT:
//			algorithm = new MolproBestFitPlacement(selectorSizeRAM, costMolpro);
//			break;
//		case MOLPRO_LOAD_BALANCING_RAM:
//			algorithm = new MolproBestFitPlacement(selectorSizeRAM, costLBRAM);
//			break;
//		case MOLPRO_CONSOLIDATION_RAM:
//			algorithm = new MolproBestFitPlacement(selectorSizeRAM, costConsolidationRAM);
//			break;
//		case MOLPRO_CONSOLIDATION:
//			algorithm = new MolproBestFitPlacement(selectorSizeRAM, costConsolidation);
//			break;
//		case MOLPRO_FRAGMENTATION:
//			algorithm = new MolproBestFitPlacement(selectorSizeRAM, costFragmentation);
//			break;
//		case MOLPRO_ENERGY_EFFICIENCY:
//			algorithm = new MolproBestFitPlacement(selectorSizeRAM, costEnergy);
//			break;
		case MOLPRO_BEST_FIT:
			algorithm = new BestFitPlacement(selectorSizeRAM, costLBRAM);
			break;
		case MOLPRO_LOAD_BALANCING_RAM:
			algorithm = new BestFitPlacement(selectorSizeRAM, costLBRAM);
			break;
		case MOLPRO_CONSOLIDATION_RAM:
			algorithm = new BestFitPlacement(selectorSizeRAM, costConsolidationRAM);
			break;
		case MOLPRO_CONSOLIDATION:
			algorithm = new BestFitPlacement(selectorSizeRAM, costConsolidation);
			break;
		case MOLPRO_FRAGMENTATION:
			algorithm = new BestFitPlacement(selectorSizeRAM, costFragmentation);
			break;
		case MOLPRO_ENERGY_EFFICIENCY:
			algorithm = new BestFitPlacement(selectorSizeRAM, costEnergy);
			break;
*/
		case NONE:
			break;
		default:
			break;
		}

		log.info("### Causa placement algorithm: " + algorithm);

		OptimizationPlan causaPlan = null;
		if (algorithm != null) {
			log.info("### Causa placement invocation @ " + System.currentTimeMillis());
			TraceLogger.saveLogEntry(CactoOptIntegration.PREFIX_TRACE_PLACEMENT,datacenter,configuration);
			causaPlan = algorithm.getOptimizationPlan(datacenter, configuration);
			log.info("### Causa placement completed @ " + System.currentTimeMillis());
			TraceLogger.saveLogEntry(CactoOptIntegration.PREFIX_TRACE_PLACEMENT,datacenter,configuration,causaPlan);
		}

		OptimisationPlan plan = null;

		if (causaPlan != null) {
			plan = transformCausaOptimisationPlanToCdoOptimisationPlan(causaPlan, pdcm, ldcm, vmsToPlace);
		}

		return plan;
	}

	// private static Experiment getCdoExperiment(PhysicalDCModel pdcm,
	// LogicalDCModel ldcm, List<VirtualMachine> vmsToPlace) {
	// DataCenter datacenter = getDataCenterModelFromCdo(pdcm, ldcm,
	// vmsToPlace);
	// Configuration configuration = getConfigurationFromCdo(pdcm, ldcm);
	//
	// return new Experiment(datacenter, configuration);
	// }

	public static OptimisationPlan transformCausaOptimisationPlanToCdoOptimisationPlan(
			se.umu.cs.ds.causa.models.OptimizationPlan causaOptimizationPlan, PhysicalDCModel pdcm, LogicalDCModel ldcm,
			List<VirtualMachine> vmsToPlace) {

		OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
		log.info("CDO OptimisationPlan [" + plan.getId() + "]: ");
		SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
		plan.setOptimisationStep(rootStep);
		rootStep.setOptimisationPlan(plan);
		rootStep.setExecutionStatus(ExecutionStatus.READY);

		// for (se.umu.cs.ds.causa.models.OptimizationPlan causaOptimizationPlan
		// : causaOptimizationPlans) {
		for (se.umu.cs.ds.causa.models.OptimizationPlan.Action action : causaOptimizationPlan.getActions()) {
			if (action instanceof OptimizationPlan.Placement) {
				OptimizationPlan.Placement causaPlacement = (OptimizationPlan.Placement) action;

				String vmId = causaPlacement.getVirtualMachine().getValue();
				String destinationId = causaPlacement.getPhysicalMachine().getValue();

				VmPlacementAction placement = OptimisationplanFactory.eINSTANCE.createVmPlacementAction();

				placement.setTargetHost(getComputeNodeById(destinationId, pdcm).getHypervisor());
				placement.setSequentialSteps(rootStep);

				for (VirtualMachine vm : vmsToPlace) {
					if (vm.getId().equals(vmId)) {
						placement.setUnassignedVirtualMachine(vm);
						break;
					}
				}

				placement.setExecutionStatus(ExecutionStatus.READY);

				log.info("CDO VmPlacementAction [" + placement.getId() + "]: place " + vmId + " on "
						+ placement.getTargetHost().getId());
			}
		}
		// }

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

									se.umu.cs.ds.causa.models.VirtualMachine.Id vmId = new se.umu.cs.ds.causa.models.VirtualMachine.Id(
											vm.getId());

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

//    //--------------------------------------------------------------------
//    private static double getStorageCapacity (StorageSpecification ss)
//    {
//      if (ss == null)
//        return 0;
//      if (ss.getSize() == null)
//        return 0;
////      if (ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE)) == null)
////        return 0;
//      return ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE));
//    }

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
//      long value = vm.getVMImageInstance().getRootDisk().getCapacity().longValue(SI.GIGA(NonSI.BYTE));
      long value = CDOModelHelper.VM_SIZE_AGGREGATOR.doSwitch(vm.getVMImageInstance().getRootDisk());
//		Math.ceil( vm.getVMImageInstance().getRootDisk().getCapacity().doubleValue(SI.GIGA(NonSI.BYTE)));
      log.info("### VM Image Size [" + vm.getVMImageInstance().getId() + "] for [" + vm.getId() + "] == " + value + " GB");
      return value;
    }

//    //--------------------------------------------------------------------
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
//
//	//--------------------------------------------------------------------
//    private static boolean eligibleForMigration (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm)
//    {
//      VM_State vmState = vm.getState();
//      if (vmState == null)
//      {
//  		log.info("unable to assess state of VM [" + vm.getName() + "]");
//  		return true;
//      }
//
//      log.info("VM [" + vm.getName() + "] is in [" + vmState.toString() + "] state.");
//      switch (vmState)
//      {
//        case NEW:
//          log.info("VM [" + vm.getName() + "] filtered (new VM cannot be migrated)");
//          return false;
//	//	case PAUSED:
//	//		log.info("Migration of [" + vmId + "] from [" + sourceId + "] to [" + destinationId + "] prevented!"
//	//				+ " Paused VM cannot be migrated!");
//	//		continue;
//        case IN_OPTIMISATION:
//          log.info("VM [" + vm.getName() + "] filtered (VM is already in optimisation)");
//          return false;
//        case SHUTDOWN:
//          log.info("VM [" + vm.getName() + "] filtered (shutdown VM cannot be migrated)");
//          return false;
//        case UNASSIGNED:
//          log.info("VM [" + vm.getName() + "] filtered (unassigned VM cannot be migrated)");
//          return false;
//        case PLACED:
//          log.info("VM [" + vm.getName() + "] filtered (placed VM cannot be migrated)");
//          return false;
//        case RUNNING:
//            log.info("VM [" + vm.getName() + "] accepted (running VMs can be migrated)");
//            return false;
//        default :
//          log.info("WARNING: VM [" + vm.getName() + "] has unknown state: " + vmState + "!");
//      }
//      return true;
//    }

	//--------------------------------------------------------------------
    private static se.umu.cs.ds.causa.models.VirtualMachine.Label[] toArray (se.umu.cs.ds.causa.models.VirtualMachine.Label label)
    {
//      if (label == null)
//        return new se.umu.cs.ds.causa.models.VirtualMachine.Label[0];
//      else
      return new se.umu.cs.ds.causa.models.VirtualMachine.Label[] {label};
    }

//	//--------------------------------------------------------------------
//    private static se.umu.cs.ds.causa.models.VirtualMachine.Label[] getVMJobTypeLabels (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm)
//    {
//      final String APPLICATION_TYPE = "applicationType";
//      final String JOBTYPE_DFT = "molpro-dft";
//      final String JOBTYPE_LCCSD = "molpro-lccsd";
//      final se.umu.cs.ds.causa.models.VirtualMachine.Label[] EMPTY = new se.umu.cs.ds.causa.models.VirtualMachine.Label[0];
//
//      if (vm.getInputParameters() == null)
//        return EMPTY;
//      if (vm.getInputParameters().get(APPLICATION_TYPE) == null)
//        return EMPTY;
//      if (vm.getInputParameters().get(APPLICATION_TYPE).equals(JOBTYPE_DFT))
//        return toArray(CactoOptIntegration.VMLABEL_JOBTYPE_MOLPRO_DFT);
//      if (vm.getInputParameters().get(APPLICATION_TYPE).equals(JOBTYPE_LCCSD))
//        return toArray(CactoOptIntegration.VMLABEL_JOBTYPE_MOLPRO_LCCSD_KIZ);
//      return EMPTY;
//    }

	//--------------------------------------------------------------------
    private static se.umu.cs.ds.causa.models.VirtualMachine.Label[] getVMJobTypeLabels (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm)
    {
      final String APPLICATION_TYPE = "applicationType";
      final String JOBTYPE_DFT = "molpro-dft";
      final String JOBTYPE_LCCSD = "molpro-lccsd";
      final se.umu.cs.ds.causa.models.VirtualMachine.Label[] EMPTY = new se.umu.cs.ds.causa.models.VirtualMachine.Label[0];

      ArrayList<se.umu.cs.ds.causa.models.VirtualMachine.Label> list = new ArrayList<se.umu.cs.ds.causa.models.VirtualMachine.Label>();
      EMap<String,String> inputParameters = vm.getInputParameters();
//      log.info("### getVMJobTypeLabels() VM [" + vm.getName() + "] input parameters [" + inputParameters + "]");
      if (inputParameters != null)
      {
        String applicationType = inputParameters.get(APPLICATION_TYPE);
//        log.info("### getVMJobTypeLabels() VM [" + vm.getName() + "] application type [" + applicationType + "]");
	    if (applicationType != null)
	    {
          if (applicationType.equals(JOBTYPE_DFT))
            list.add(CactoOptIntegration.VMLABEL_JOBTYPE_MOLPRO_DFT);
          else if (applicationType.equals(JOBTYPE_LCCSD))
            list.add(CactoOptIntegration.VMLABEL_JOBTYPE_MOLPRO_LCCSD_KIZ);
        }
      }
//      log.info("### getVMJobTypeLabels() VM [" + vm.getName() + "] #labels [" + list.size() + "]");
//      for (se.umu.cs.ds.causa.models.VirtualMachine.Label label : list)
//        log.info("###   getVMJobTypeLabels() VM [" + vm.getName() + "] label [" + label + "]");
//      if (!isEligibleForMigration(vm))
//        list.add(CactoOptIntegration.VMLABEL_IMMOVABLE);
      return list.toArray(new se.umu.cs.ds.causa.models.VirtualMachine.Label[list.size()]);
    }

//    //--------------------------------------------------------------------
//	private static DataCenter getDataCenterModelFromCdo(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm,
//			List<VirtualMachine> vmsToPlace) {
//		List<PhysicalMachine> physicalMachines = new ArrayList<>();
//		List<se.umu.cs.ds.causa.models.VirtualMachine> virtualMachines = new ArrayList<>();
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
//						int totalMemory = 0;
//						// TODO FIXME use size of hypervisor specification
//						// int totalMemory = -2048; // Buffer for Hypervisor
//
//						EList<ProcessingUnitSpecification> cpuSpecs = computeNode.getCpuSpecifications();
//
//						List<CPU> cpus = new ArrayList<>();
//
//						for (ProcessingUnitSpecification pus : cpuSpecs) {
//							log.info("CDO ProcessingUnitSpecification " + pus.getName() + " [" + pus.getId()
//									+ "] #cores=" + pus.getNumberOfCores() + ", freq=" + pus.getFrequency());
//
//							int noCores = pus.getNumberOfCores();
//							List<Core> cores = new ArrayList<>();
//							for (int i = 0; i < noCores; i++) {
//								Core core = new Core(
//										(int) Math.round(pus.getFrequency().doubleValue(SI.MEGA(SI.HERTZ))));
//								cores.add(core);
//							}
//							CPU cpu = new CPU(cores.toArray(new Core[cores.size()]));
//							cpus.add(cpu);
//						}
//
//						for (MemorySpecification ms : computeNode.getMemorySpecifications()) {
//							log.info("CDO MemorySpecification " + ms.getName() + " [" + ms.getId() + "] size="
//									+ ms.getSize());
//							totalMemory += ms.getSize().doubleValue(SI.MEGA(NonSI.BYTE));
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
//							log.info("CDO StorageSpecification " + ss.getName() + " [" + ss.getId() + "] size="
//									+ ss.getSize() + ", read=" + storageRead + " bps, write=" + storageWrite
//									+ " bps");
//						}
//
//						int networkCapacity = 0;
//						int networkPerformance = 0;
//
//						if (aggregateStorageCapacity == 0)
//							log.info("\tPM [" + computeNode.getName() + "] is a disk-less node!");
//						
//						double availableStorage = CDOModelHelper.getAvailableStorage(computeNode, plm);
//						
//						for (Hypervisor h : ldcm.getHypervisors()) {
//							log.info("CDO Hypervisor [" + h.getId() + "]");
//							if ((h.getNode() != null) && (h.getNode().getName().equals(id))) {
//								for (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm : h
//										.getVirtualMachines()) {
//									se.umu.cs.ds.causa.models.VirtualMachine.Id vmId = new se.umu.cs.ds.causa.models.VirtualMachine.Id(
//											vm.getId());
//
//									long vmMemory = 0;
//
//									List<CPU> vmCPUs = new ArrayList<>();
//
//									for (VirtualProcessingUnit vpu : vm.getVirtualProcessingUnits()) {
//										log.info("CDO VirtualProcessingUnit " + vpu.getName() + " [" + vpu.getId() + "]"
//												+ "#virtualCores=" + vpu.getVirtualCores());
//										Core vCore = new Core(2000);
//										Core[] vCores = new Core[1];
//										vCores[0] = vCore;
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
//										vmMemory += vmem.getProvisioned().doubleValue(SI.MEGA(NonSI.BYTE));
//									}
//
//									double aggregateVMStorageCapacity = getAggregateVMStorageCapacity(vm);
//									log.info("VM[" + vm.getId() + " @ " + vm + "] disk allocation: " + aggregateVMStorageCapacity);
//									double aggregateVMStoragePerformance = 0.0;
//									// for (StorageSpecification ss :
//									// vm.getStorageSpecifications()) {
//									// double storageCapacity;
//									// double storageRead;
//									// double storageWrite;
//									//
//									// if (ss.getSize() != null)
//									// storageCapacity =
//									// ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE));
//									// else
//									// storageCapacity = 0.0;
//									//
//									// if (ss.getReadBandwidth() != null)
//									// storageRead =
//									// ss.getReadBandwidth().getValue().getEstimatedValue();
//									// else
//									// storageRead = 0.0;
//									//
//									// if (ss.getWriteBandwidth() != null)
//									// storageWrite =
//									// ss.getWriteBandwidth().getValue().getEstimatedValue();
//									// else
//									// storageWrite = 0.0;
//									//
//									// aggregateStorageCapacity +=
//									// storageCapacity;
//									// aggregateStoragePerformance +=
//									// storageRead;
//									// aggregateStoragePerformance +=
//									// storageWrite;
//									//
//									// log.info("CDO StorageSpecification " +
//									// ss.getName() + " [" + ss.getId() + "]
//									// size=" + ss.getSize() + " GB, read=" +
//									// storageRead + " bps, write=" +
//									// storageWrite + " bps");
//									// }
//
//									// CPU[] vcpus = new CPU[1];
//									// vcpus[0] = new CPU(cores.toArray(new
//									// Core[cores.size()]));
//									
//									se.umu.cs.ds.causa.models.VirtualMachine virtualMachine = new se.umu.cs.ds.causa.models.VirtualMachine(
//											vmId, vmCPUs.toArray(new CPU[vmCPUs.size()]), (int) vmMemory,
//											(int) Math.round(aggregateVMStorageCapacity),
//											(int) Math.round(aggregateVMStoragePerformance),
//											networkCapacity,
//											networkPerformance);
//
//									virtualMachines.add(virtualMachine);
//								}
//							}
//						}
//						
//						int noLccsdMolpro = CDOModelHelper.getNoLccsdMolpro(computeNode, ldcm);
//						
//						int storagePerformance; // 1 - when the PM doesn't host any LCCSD Molpro jobs, 0 - when it hosts at least one LCCSD Molpro job.
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
//								totalMemory, (int) Math.round(availableStorage),
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
//		log.info("Virtual Machine waiting for placement");
//		for (VirtualMachine vm : vmsToPlace) {
//
//			if (vm == null) {
//				log.warning("List of virtual machines waiting for placement constains a null!");
//				continue;
//			}
//
//			log.info("CDO VirtualMachine " + vm.getName() + " [" + vm.getId() + "]");
//
//			se.umu.cs.ds.causa.models.VirtualMachine.Id vmId = new se.umu.cs.ds.causa.models.VirtualMachine.Id(
//					vm.getId());
//
//			long vmMemory = 0;
//			int storageCapacity = 0;
//			int storagePerformance = 0;
//			int networkCapacity = 0;
//			int networkPerformance = 0;
//
//			List<CPU> vmCPUs = new ArrayList<>();
//
//			for (VirtualProcessingUnit vpu : vm.getVirtualProcessingUnits()) {
//				log.info("CDO VirtualProcessingUnit " + vpu.getName() + " [" + vpu.getId() + "]" + "#virtualCores="
//						+ vpu.getVirtualCores());
//				Core vCore = new Core(2000);
//				Core[] vCores = new Core[1];
//				vCores[0] = vCore;
//				CPU vCPU = new CPU(vCores);
//				vmCPUs.add(vCPU);
//			}
//
//			// if VM doesn't have any CPUs assigned add one
//			// (for compatibility with Causa - CPU freq)
//			if (vmCPUs.size() == 0) {
//				Core vCore = new Core(2000);
//				Core[] vCores = new Core[1];
//				vCores[0] = vCore;
//				CPU vCPU = new CPU(vCores);
//				vmCPUs.add(vCPU);
//			}
//
//			for (VirtualMemory vmem : vm.getVirtualMemoryUnits()) {
//				if (vmem == null) {
//					log.log(Level.WARNING, "vmem is null for vm " + vm.getName());
//					continue;
//				}
//				if (vmem.getProvisioned() == null) {
//					log.log(Level.WARNING, "vmem.getProvisioned() is null for vm " + vm.getName());
//					continue;
//				}
//				log.info("CDO VirtualMemory [" + vmem.getId() + "] size=" + vmem.getProvisioned());
//				vmMemory += vmem.getProvisioned().doubleValue(SI.MEGA(NonSI.BYTE));
//			}
//
//			String applicationType = vm.getInputParameters().get("applicationType");
//			if (applicationType != null) {
//				log.info("VM [" + vm.getName() + "] has Application Type [" + applicationType + "]");
//				switch (applicationType) {
//				case "molpro-lccsd":
//					storageCapacity = (int) Math.ceil(CDOModelHelper.getVMImageStorageCapacity(vm, 70.0));
//					storagePerformance = 1;
//					break;
//				case "molpro-dft":
//					storageCapacity = (int) Math.ceil(CDOModelHelper.getVMImageStorageCapacity(vm, 220.0));
//					storagePerformance = 0;
//					break;
//				default:
//					break;
//				}
//			} else {
//				log.info("VM [" + vm.getName() + "] has no Application Type specified!");
//			}
//
//			// CPU[] vcpus = new CPU[1];
//			// vcpus[0] = new CPU(cores.toArray(new
//			// Core[cores.size()]));
//
//			se.umu.cs.ds.causa.models.VirtualMachine virtualMachine = new se.umu.cs.ds.causa.models.VirtualMachine(vmId,
//					vmCPUs.toArray(new CPU[vmCPUs.size()]), (int) vmMemory, storageCapacity, storagePerformance,
//					networkCapacity, networkPerformance);
//
//			virtualMachines.add(virtualMachine);
//		}
//
//		PhysicalMachine[] pms = physicalMachines.toArray(new PhysicalMachine[physicalMachines.size()]);
//		se.umu.cs.ds.causa.models.VirtualMachine[] vms = virtualMachines
//				.toArray(new se.umu.cs.ds.causa.models.VirtualMachine[virtualMachines.size()]);
//		Constraint[] constraints = new Constraint[] { NoCPUCoreOverCommitGlobalConstraint.SINGLETON,
//                                                              NoRAMOverCommitGlobalConstraint.SINGLETON};
//		DataCenter dataCenter = new DataCenter(pms, vms, constraints);
//
//		return dataCenter;
//	}

    //--------------------------------------------------------------------
	private static DataCenter getDataCenterModelFromCdo_withConstraints (PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm,
			List<VirtualMachine> vmsToPlace) {
		List<PhysicalMachine> physicalMachines = new ArrayList<>();
		List<se.umu.cs.ds.causa.models.VirtualMachine> virtualMachines = new ArrayList<>();

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
						long totalMemory = 0;
						// TODO FIXME use size of hypervisor specification
						// int totalMemory = -2048; // Buffer for Hypervisor

						EList<ProcessingUnitSpecification> cpuSpecs = computeNode.getCpuSpecifications();

						List<CPU> cpus = new ArrayList<>();

						for (ProcessingUnitSpecification pus : cpuSpecs) {
							log.info("CDO ProcessingUnitSpecification " + pus.getName() + " [" + pus.getId()
									+ "] #cores=" + pus.getNumberOfCores() + ", freq=" + pus.getFrequency());

							int noCores = pus.getNumberOfCores();
							List<Core> cores = new ArrayList<>();
							for (int i = 0; i < noCores; i++) {
								Core core = new Core(
										(int) Math.round(pus.getFrequency().doubleValue(SI.MEGA(SI.HERTZ))));
								cores.add(core);
							}
							CPU cpu = new CPU(cores.toArray(new Core[cores.size()]));
							cpus.add(cpu);
						}

						for (MemorySpecification ms : computeNode.getMemorySpecifications()) {
							log.info("CDO MemorySpecification " + ms.getName() + " [" + ms.getId() + "] size="
									+ ms.getSize());
//							totalMemory += ms.getSize().doubleValue(SI.MEGA(NonSI.BYTE));
							totalMemory += ms.getSize().longValue(SI.GIGA(NonSI.BYTE));
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

							log.info("CDO StorageSpecification " + ss.getName() + " [" + ss.getId() + "] size="
									+ ss.getSize() + ", read=" + storageRead + " bps, write=" + storageWrite
									+ " bps");
						}

						int networkCapacity = 0;
						int networkPerformance = 0;

//						if (aggregateStorageCapacity == 0)
//							log.info("\tPM [" + computeNode.getName() + "] is a disk-less node!");
						
						double availableStorage = CDOModelHelper.getAvailableStorage(computeNode, plm);
						
						for (Hypervisor h : ldcm.getHypervisors()) {
							log.info("CDO Hypervisor [" + h.getId() + "]");
							if ((h.getNode() != null) && (h.getNode().getName().equals(id))) {
								for (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm : h
										.getVirtualMachines()) {
									se.umu.cs.ds.causa.models.VirtualMachine.Id vmId = new se.umu.cs.ds.causa.models.VirtualMachine.Id(
											vm.getId());

									long vmMemory = 0;

									List<CPU> vmCPUs = new ArrayList<>();

									for (VirtualProcessingUnit vpu : vm.getVirtualProcessingUnits()) {
										log.info("CDO VirtualProcessingUnit " + vpu.getName() + " [" + vpu.getId() + "]"
												+ "#virtualCores=" + vpu.getVirtualCores());
										Core vCore = new Core(2000);
										Core[] vCores = new Core[1];
										vCores[0] = vCore;
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
//										vmMemory += vmem.getProvisioned().doubleValue(SI.MEGA(NonSI.BYTE));
										vmMemory += vmem.getProvisioned().longValue(SI.GIGA(NonSI.BYTE));
										log.info("### VM[" + vmId + "] RAM (part): " + vmem.getProvisioned().longValue(SI.GIGA(NonSI.BYTE)));
									}
									log.info("### VM[" + vmId + "] total RAM: " + vmMemory);

									long aggregateVMStorageCapacity = getAggregateVMStorageCapacity(vm);
									log.info("VM[" + vm.getId() + " @ " + vm + "] disk allocation: " + aggregateVMStorageCapacity);
									double aggregateVMStoragePerformance = 0.0;
									// for (StorageSpecification ss :
									// vm.getStorageSpecifications()) {
									// double storageCapacity;
									// double storageRead;
									// double storageWrite;
									//
									// if (ss.getSize() != null)
									// storageCapacity =
									// ss.getSize().doubleValue(SI.GIGA(NonSI.BYTE));
									// else
									// storageCapacity = 0.0;
									//
									// if (ss.getReadBandwidth() != null)
									// storageRead =
									// ss.getReadBandwidth().getValue().getEstimatedValue();
									// else
									// storageRead = 0.0;
									//
									// if (ss.getWriteBandwidth() != null)
									// storageWrite =
									// ss.getWriteBandwidth().getValue().getEstimatedValue();
									// else
									// storageWrite = 0.0;
									//
									// aggregateStorageCapacity +=
									// storageCapacity;
									// aggregateStoragePerformance +=
									// storageRead;
									// aggregateStoragePerformance +=
									// storageWrite;
									//
									// log.info("CDO StorageSpecification " +
									// ss.getName() + " [" + ss.getId() + "]
									// size=" + ss.getSize() + " GB, read=" +
									// storageRead + " bps, write=" +
									// storageWrite + " bps");
									// }

									// CPU[] vcpus = new CPU[1];
									// vcpus[0] = new CPU(cores.toArray(new
									// Core[cores.size()]));
									
									se.umu.cs.ds.causa.models.VirtualMachine virtualMachine = new se.umu.cs.ds.causa.models.VirtualMachine(
											vmId,
											getVMJobTypeLabels(vm),
											vmCPUs.toArray(new CPU[vmCPUs.size()]),
											(int) vmMemory,
											(int) aggregateVMStorageCapacity,
//											(int) Math.round(aggregateVMStorageCapacity),
											(int) Math.round(aggregateVMStoragePerformance),
											networkCapacity,
											networkPerformance);

									virtualMachines.add(virtualMachine);
								}
							}
						}
						
						int noLccsdMolpro = CDOModelHelper.getNoLccsdMolpro(computeNode, ldcm);
						
						int storagePerformance; // 1 - when the PM doesn't host any LCCSD Molpro jobs, 0 - when it hosts at least one LCCSD Molpro job.
						
						if (noLccsdMolpro <= 0) {
							log.info("Physical Machine [" + computeNode.getName() + "] doesn't host any LCCSD Molpro jobs.");
							storagePerformance = 1;
						} else {
							log.info("Physical Machine [" + computeNode.getName() + "] hosts already " + noLccsdMolpro + " LCCSD Molpro jobs.");
							storagePerformance = 0;
						}
						
						PhysicalMachine pm = new PhysicalMachine(new Id(id), cpus.toArray(new CPU[cpus.size()]),
								(int)totalMemory,
							    (int)aggregateStorageCapacity,
//								(int) Math.round(availableStorage), // !!!
								storagePerformance,
//								(int) Math.round(aggregateStoragePerformance),
								networkCapacity, networkPerformance);

						physicalMachines.add(pm);
					}
				}
			}
		}

		log.info("Virtual Machine waiting for placement");
		for (VirtualMachine vm : vmsToPlace) {

			if (vm == null) {
				log.warning("List of virtual machines waiting for placement constains a null!");
				continue;
			}

			log.info("CDO VirtualMachine " + vm.getName() + " [" + vm.getId() + "]");

			se.umu.cs.ds.causa.models.VirtualMachine.Id vmId = new se.umu.cs.ds.causa.models.VirtualMachine.Id(vm.getId());

			long vmMemory = 0;
			int storageCapacity_old = 0;
			int storagePerformance = 0;
			int networkCapacity = 0;
			int networkPerformance = 0;

			List<CPU> vmCPUs = new ArrayList<>();

			for (VirtualProcessingUnit vpu : vm.getVirtualProcessingUnits()) {
				log.info("CDO VirtualProcessingUnit " + vpu.getName() + " [" + vpu.getId() + "]" + "#virtualCores="
						+ vpu.getVirtualCores());
				Core vCore = new Core(2000);
				Core[] vCores = new Core[1];
				vCores[0] = vCore;
				CPU vCPU = new CPU(vCores);
				vmCPUs.add(vCPU);
			}

			// if VM doesn't have any CPUs assigned add one
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
					log.log(Level.WARNING, "vmem.getProvisioned() is null for vm " + vm.getName());
					continue;
				}
				log.info("CDO VirtualMemory [" + vmem.getId() + "] size=" + vmem.getProvisioned());
//				vmMemory += vmem.getProvisioned().doubleValue(SI.MEGA(NonSI.BYTE));
				vmMemory += vmem.getProvisioned().longValue(SI.GIGA(NonSI.BYTE));
			}

			String applicationType = vm.getInputParameters().get("applicationType");
			if (applicationType != null) {
				log.info("VM [" + vm.getName() + "] has Application Type [" + applicationType + "]");
				switch (applicationType) {
				case "molpro-lccsd":
					storageCapacity_old = (int) Math.ceil(CDOModelHelper.getVMImageStorageCapacity(vm, 70.0));
					storagePerformance = 1;
					break;
				case "molpro-dft":
					storageCapacity_old = (int) Math.ceil(CDOModelHelper.getVMImageStorageCapacity(vm, 220.0));
					storagePerformance = 0;
					break;
				default:
					break;
				}
			} else {
				log.info("VM [" + vm.getName() + "] has no Application Type specified!");
			}

			// CPU[] vcpus = new CPU[1];
			// vcpus[0] = new CPU(cores.toArray(new
			// Core[cores.size()]));

			int storageCapacity = (int)getAggregateVMStorageCapacity(vm);
		    log.info("### VM Image Size [" + vmId + "] = " + CDOModelHelper.getVMImageStorageCapacity(vm, 70.0) + " GB (default 70)");

			se.umu.cs.ds.causa.models.VirtualMachine virtualMachine = new se.umu.cs.ds.causa.models.VirtualMachine(vmId, getVMJobTypeLabels(vm),
					vmCPUs.toArray(new CPU[vmCPUs.size()]), (int) vmMemory, storageCapacity, storagePerformance,
					networkCapacity, networkPerformance);

			virtualMachines.add(virtualMachine);
		}

        PhysicalMachine[] pms = physicalMachines.toArray(new PhysicalMachine[physicalMachines.size()]);
        se.umu.cs.ds.causa.models.VirtualMachine[] vms = virtualMachines.toArray(new se.umu.cs.ds.causa.models.VirtualMachine[virtualMachines.size()]);
        Constraint[] constraints = new Constraint[0];
        DataCenter datacenter = new DataCenter(pms,vms,constraints);
//        return CactoOptIntegration.updateLabelsAndConstraints(datacenter);
        return datacenter;
	}
}