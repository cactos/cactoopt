package eu.cactosfp7.cactoopt.optimisationservice.demo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.EList;

import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
import eu.cactosfp7.cactoopt.placementservice.InitialPlacementAlgorithm;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.Hypervisor;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
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
import eu.cactosfp7.optimisationplan.ManagePhysicalNodeAction;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationplanFactory;
import eu.cactosfp7.optimisationplan.SequentialSteps;
import eu.cactosfp7.optimisationplan.StopVmAction;
import eu.cactosfp7.optimisationplan.VmMigrationAction;
import eu.cactosfp7.optimisationplan.VmPlacementAction;
import se.umu.cs.ds.causa.algorithms.PowerControlAlgorithm;
import se.umu.cs.ds.causa.algorithms.ThresholdPowerControl;
import se.umu.cs.ds.causa.constraints.Constraint;
import se.umu.cs.ds.causa.constraints.global.NoCPUCoreOverCommitGlobalConstraint;
import se.umu.cs.ds.causa.constraints.global.NoRAMOverCommitGlobalConstraint;
import se.umu.cs.ds.causa.demos.cactosy2.CausaSensorActuator;
import se.umu.cs.ds.causa.demos.cactosy2.ExperimentSetup;
import se.umu.cs.ds.causa.demos.cactosy2.InMemoryStateAccessor;
import se.umu.cs.ds.causa.demos.cactosy2.PlacementMigrationDemoService;
import se.umu.cs.ds.causa.demos.cactosy2.PlacementMigrationDemoService.Client;
import se.umu.cs.ds.causa.demos.cactosy2.Trace;
import se.umu.cs.ds.causa.demos.cactosy2.interfaces.Actuator;
import se.umu.cs.ds.causa.demos.cactosy2.interfaces.PM;
import se.umu.cs.ds.causa.demos.cactosy2.interfaces.Sensor;
import se.umu.cs.ds.causa.demos.cactosy2.interfaces.SensorActuator;
import se.umu.cs.ds.causa.demos.cactosy2.interfaces.State;
import se.umu.cs.ds.causa.demos.cactosy2.interfaces.VM;
import se.umu.cs.ds.causa.models.AbstractMachine.CPU;
import se.umu.cs.ds.causa.models.AbstractMachine.CPU.Core;
import se.umu.cs.ds.causa.models.DataCenter;
import se.umu.cs.ds.causa.models.DataCenter.Configuration;
import se.umu.cs.ds.causa.models.DataCenter.Configuration.Mapping;
import se.umu.cs.ds.causa.models.DataCenter.DataCenterConfigurationTuple;
import se.umu.cs.ds.causa.models.OptimizationPlan;
import se.umu.cs.ds.causa.models.OptimizationPlan.Migrate;
import se.umu.cs.ds.causa.models.OptimizationPlan.Place;
import se.umu.cs.ds.causa.models.OptimizationPlan.Terminate;
import se.umu.cs.ds.causa.models.PhysicalMachine;
import se.umu.cs.ds.causa.models.PhysicalMachine.Id;
import se.umu.cs.ds.causa.models.VirtualMachine;

/**
 * Causa optimisation algorithm.
 * 
 * @author jakub
 *
 */
public class DemoOptimisationAlgorithm implements IOptimisationAlgorithm, InitialPlacementAlgorithm {

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(DemoOptimisationAlgorithm.class.getName());
    private Actuator actuator;
    private Sensor sensor;
    private PlacementMigrationDemoService pmds; 
    private Id[] pmIds = null;
    private int demoIndex = 0;
    
    public static final String PROPERTY_DEMOSENSORACTUATORFACTORY = "DemoSensorActuatorFactory";
    
//    public DemoOptimisationAlgorithm(Sensor sensor, Actuator actuator) {
    public DemoOptimisationAlgorithm() {
//        this.sensor = sensor;
//        this.actuator = actuator;
	}

	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm,
			LogicalLoadModel llm) {

		initialize(pdcm);
		
		log.info("Translation of CDO models into Causa models");

		DataCenter datacenter = getDataCenterModelFromCdo(pdcm, ldcm);
		Configuration configuration = getConfigurationFromCdo(pdcm, ldcm);
		
		log.info("Demo optimisation algorithm starts");

		OptimizationPlan causaPlan = pmds.getOptimizationPlan(datacenter, configuration);
		
        Trace trace = pmds.getTrace();
        try {
        	File dir = new File("data");
        	dir.mkdirs();
			trace.writeCSV("demo" + demoIndex);
			
			System.out.println("done, configuration stored in data/demo" + demoIndex + "_XX.csv");
		} catch (IOException e) {
			System.out.println("error while trying to stor data/demo" + demoIndex + "_XX.csv");
			e.printStackTrace();
		}
        demoIndex++;
		
		OptimisationPlan plan = transformCausaOptimisationPlanToCdoOptimisationPlan(
				causaPlan, pdcm, ldcm);

		return plan;
	}

	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm, LogicalDCModel ldcm, PhysicalLoadModel plm,
			LogicalLoadModel llm, List<eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine> vmsToPlace) {

		initialize(pdcm);
		
		log.info("Translation of CDO models into Causa models");

		DataCenter datacenter = getDataCenterModelFromCdo(pdcm, ldcm, vmsToPlace);
		Configuration configuration = getConfigurationFromCdo(pdcm, ldcm);


		log.info("Causa placement algorithm starts");

		OptimizationPlan causaPlan = pmds.getOptimizationPlan(datacenter, configuration);

        Trace trace = pmds.getTrace();
        try {
        	File dir = new File("data");
        	dir.mkdirs();
			trace.writeCSV("demo" + demoIndex);
			
			System.out.println("done, configuration stored in data/demo" + demoIndex + "_XX.csv");
		} catch (IOException e) {
			System.out.println("error while trying to stor data/demo" + demoIndex + "_XX.csv");
			e.printStackTrace();
		}
        demoIndex++;
		
		OptimisationPlan plan = transformCausaOptimisationPlanToCdoOptimisationPlan(
				causaPlan, pdcm, ldcm);

		return plan;
	}
	
	private void initialize(PhysicalDCModel pdcm){
		if (pmIds == null) {
	        SensorActuator.Factory factory = getSensorActuatorFactory();
	        SensorActuator sa = new CausaSensorActuator.Factory().getInstance(new String[] {"6"});
	        State.Accessor stateaccessor = new InMemoryStateAccessor();
	        
			pmIds = getPMIdsFromCdo(pdcm);
			
			PowerControlAlgorithm powercontroller = new ThresholdPowerControl(pmIds);
			pmds = new PlacementMigrationDemoService(powercontroller, stateaccessor);
			
//	        Client client = new Client(sa,sa);
//	        pmds.resetDemoRun();
		}
	}

	public void enact (OptimizationPlan plan)
    {
      if (plan.isEmpty())
        return;
//        throw new IllegalArgumentException("optimization plan empty");

      for (OptimizationPlan.Action action : plan.getActions())
      {
        if (action instanceof OptimizationPlan.Place)
        {
          OptimizationPlan.Place place = (OptimizationPlan.Place)action;
          VirtualMachine.Id vmId = place.getVirtualMachine();
          PhysicalMachine.Id pmId = place.getPhysicalMachine();
          if (!actuator.placeVirtualMachine(vmId.toString(),pmId.toString()))
            throw new IllegalArgumentException("unable to place VM " + vmId + " @ " + pmId);
        }
        else
        if (action instanceof OptimizationPlan.Migrate)
        {
          OptimizationPlan.Migrate migrate = (OptimizationPlan.Migrate)action;
          VirtualMachine.Id vmId = migrate.getVirtualMachine();
          PhysicalMachine.Id srcPMId = migrate.getSourcePhysicalMachine();
          PhysicalMachine.Id dstPMId = migrate.getDestinationPhysicalMachine();
          if (!actuator.migrateVirtualMachine(vmId.toString(),srcPMId.toString(),dstPMId.toString()))
            throw new IllegalArgumentException("unable to migate VM " + vmId + " @ " + srcPMId + " -> " + dstPMId);
        }
        else
        if (action instanceof OptimizationPlan.Terminate)
        {
          OptimizationPlan.Terminate terminate = (OptimizationPlan.Terminate)action;
          VirtualMachine.Id vmId = terminate.getVirtualMachine();
          if (!actuator.terminateVirtualMachine(vmId.toString()))
            throw new IllegalStateException("unable to terminate VM " + vmId);
        }
        else
        if (action instanceof OptimizationPlan.PowerUp)
        {
          PhysicalMachine.Id pmId = ((OptimizationPlan.PowerUp)action).getPhysicalMachine();
          if (!actuator.powerUpPhysicalMachine(pmId.toString()))
            throw new IllegalStateException("unable to power up PM");
        }
        else
        if (action instanceof OptimizationPlan.PowerDown)
        {
          PhysicalMachine.Id pmId = ((OptimizationPlan.PowerDown)action).getPhysicalMachine();
          if (!actuator.powerDownPhysicalMachine(pmId.toString()))
            throw new IllegalStateException("unable to power down PM");
        }

//        update();
//        log.add(action.toString(),phase,datacenter,configuration);
      }
    }

    //----------------------------------------------------------
    private DataCenterConfigurationTuple getState ()
    {
      // NOTE: constraint model defaults to RAM overcommit only
      final Constraint[] constraints = ExperimentSetup.getConstraints();
      final CPU[] cpus = new CPU[] {new CPU(new CPU.Core[] {new CPU.Core(1000)})};

      ArrayList<PhysicalMachine> pmList = new ArrayList<PhysicalMachine>();
      for (PM pm : sensor.getPhysicalMachines())
        if (pm.getPoweredUp())
        {
          PhysicalMachine.Id id = new PhysicalMachine.Id(pm.getId());
          pmList.add(new PhysicalMachine(id,cpus,pm.getRAM(),0,0,0,0));
        }
      PhysicalMachine[] pms = pmList.toArray(new PhysicalMachine[pmList.size()]);
      ArrayList<VirtualMachine> vmList = new ArrayList<VirtualMachine>();
      for (VM vm : sensor.getVirtualMachines())
      {
        VirtualMachine.Id id = new VirtualMachine.Id(vm.getId());
        vmList.add(new VirtualMachine(id,cpus,vm.getRAM(),0,0,0,0));
      }
      VirtualMachine[] vms = vmList.toArray(new VirtualMachine[vmList.size()]);
      DataCenter datacenter = new DataCenter(pms,vms,constraints);

      ArrayList<Configuration.Mapping> mappingList = new ArrayList<DataCenter.Configuration.Mapping>();
      for (VM vm : sensor.getVirtualMachines())
      {
        String pm = vm.getPM();
        if (!pm.isEmpty())
        {
          VirtualMachine.Id vmId = new VirtualMachine.Id(vm.getId());
          PhysicalMachine.Id pmId = new PhysicalMachine.Id(pm);
          mappingList.add(new Configuration.Mapping(vmId,pmId));
        }
      }
      Configuration.Mapping[] mappings = mappingList.toArray(new Configuration.Mapping[mappingList.size()]);
      Configuration configuration = new Configuration(mappings);

      return new DataCenterConfigurationTuple(datacenter,configuration);
    }


    //----------------------------------------------------------
    //----------------------------------------------------------
    private static PhysicalMachine.Id[] getIds (PhysicalMachine[] pms)
    {
      PhysicalMachine.Id[] ids = new PhysicalMachine.Id[pms.length];
      for (int i=0;  i<ids.length; i++)
        ids[i] = pms[i].getId();
      return ids;
    }

    //----------------------------------------------------------
    private static PhysicalMachine.Id[] getIds (PM[] pms)
    {
      PhysicalMachine.Id[] ids = new PhysicalMachine.Id[pms.length];
      for (int i=0;  i<ids.length; i++)
        ids[i] = new PhysicalMachine.Id(pms[i].getId());
      return ids;
    }

    //----------------------------------------------------------
    private static SensorActuator.Factory getSensorActuatorFactory ()
    {
      String value = System.getProperty(PROPERTY_DEMOSENSORACTUATORFACTORY);
      if (value == null)
      {
        System.err.println("ERROR: property '" + PROPERTY_DEMOSENSORACTUATORFACTORY + "' not set");
        System.exit(-1);
      }

      try
      {
        return (SensorActuator.Factory)Class.forName(value).newInstance();
      }
      catch (Exception e)
      {
        System.err.println("ERROR: unable to instantiate factory '" + value + "'");
        e.printStackTrace();
        System.exit(-1);
      }

      throw new IllegalStateException();
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
				if (action instanceof Place) {
					Place causaPlacement = (Place) action;

					String vmId = causaPlacement.getVirtualMachine().getValue();
					String destinationId = causaPlacement.getPhysicalMachine().getValue();

					VmPlacementAction placement = OptimisationplanFactory.eINSTANCE.createVmPlacementAction();

					placement.setTargetHost(getComputeNodeById(destinationId, pdcm).getHypervisor());
					placement.setSequentialSteps(rootStep);
					placement.setExecutionStatus(ExecutionStatus.READY);

					log.info("CDO VmPlacementAction [" + placement.getId() + "]: place " + vmId + " on "
							+ placement.getTargetHost().getId());
				} else if (action instanceof Terminate) {
					Terminate causaTermination = (Terminate) action;

					String vmId = causaTermination.getVirtualMachine().getValue();

					StopVmAction termination = OptimisationplanFactory.eINSTANCE.createStopVmAction();

					termination.setStoppedVm(getVirtualMachineById(vmId, ldcm));
					termination.setSequentialSteps(rootStep);
					termination.setExecutionStatus(ExecutionStatus.READY);

					log.info("CDO StopVmAction [" + termination.getId() + "]: " + termination.getStoppedVm().getName());
				}  else if (action instanceof Migrate) {
					Migrate causaMigration = (Migrate) action;

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

	public static OptimisationPlan transformCausaOptimisationPlanToCdoOptimisationPlan(
			se.umu.cs.ds.causa.models.OptimizationPlan causaOptimizationPlan, PhysicalDCModel pdcm,
			LogicalDCModel ldcm) {

		OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
		log.info("CDO OptimisationPlan [" + plan.getId() + "]: ");
		SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
		plan.setOptimisationStep(rootStep);
		rootStep.setOptimisationPlan(plan);
		rootStep.setExecutionStatus(ExecutionStatus.READY);

		// for (se.umu.cs.ds.causa.models.OptimizationPlan causaOptimizationPlan
		// : causaOptimizationPlans) {
		for (se.umu.cs.ds.causa.models.OptimizationPlan.Action action : causaOptimizationPlan.getActions()) {
			if (action instanceof Place) {
				Place causaPlacement = (Place) action;

				String vmId = causaPlacement.getVirtualMachine().getValue();
				String destinationId = causaPlacement.getPhysicalMachine().getValue();

				VmPlacementAction placement = OptimisationplanFactory.eINSTANCE.createVmPlacementAction();

				placement.setTargetHost(getComputeNodeById(destinationId, pdcm).getHypervisor());
				placement.setSequentialSteps(rootStep);
				placement.setExecutionStatus(ExecutionStatus.READY);

				log.info("CDO VmPlacementAction [" + placement.getId() + "]: place " + vmId + " on "
						+ placement.getTargetHost().getId());
			} else if (action instanceof Migrate) {
				Migrate causaMigration = (Migrate) action;

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
		List<Mapping> mappings = new ArrayList<DataCenter.Configuration.Mapping>();

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

	private static DataCenter getDataCenterModelFromCdo(PhysicalDCModel pdcm, LogicalDCModel ldcm) {
		List<PhysicalMachine> physicalMachines = new ArrayList<PhysicalMachine>();
		List<VirtualMachine> virtualMachines = new ArrayList<VirtualMachine>();

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
						// int totalMemory = 0;
						int totalMemory = -2048; // Buffer for Hypervisor

						EList<ProcessingUnitSpecification> cpuSpecs = computeNode.getCpuSpecifications();

						List<CPU> cpus = new ArrayList<PhysicalMachine.CPU>();

						for (ProcessingUnitSpecification pus : cpuSpecs) {

							log.info("CDO ProcessingUnitSpecification " + pus.getName() + " [" + pus.getId()
									+ "] #cores=" + pus.getNumberOfCores() + ", freq=" + pus.getFrequency());

							noCores += pus.getNumberOfCores();
							List<Core> cores = new ArrayList<Core>();
							// Core core = new
							// Core((int)Math.round(pus.getFrequency().doubleValue(SI.HERTZ)));
							for (int i = 0; i < noCores; i++) {
								Core core = new Core((int) Math.round(pus.getFrequency().getEstimatedValue()));
								cores.add(core);
							}
							CPU cpu = new CPU(cores.toArray(new Core[cores.size()]));
							cpus.add(cpu);
						}

						for (MemorySpecification ms : computeNode.getMemorySpecifications()) {
							log.info("CDO MemorySpecification " + ms.getName() + " [" + ms.getId() + "] size="
									+ ms.getSize());
							totalMemory += ms.getSize().getEstimatedValue();
						}

						int storageCapacity = 0;
						int storagePerformance = 0;
						int networkCapacity = 0;
						int networkPerformance = 0;

						PhysicalMachine pm = new PhysicalMachine(new Id(id), cpus.toArray(new CPU[cpus.size()]),
								totalMemory, storageCapacity, storagePerformance, networkCapacity, networkPerformance);

						for (Hypervisor h : ldcm.getHypervisors()) {
							log.info("CDO Hypervisor [" + h.getId() + "]");
							if ((h.getNode() != null) && (h.getNode().getName().equals(id))) {
								for (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm : h
										.getVirtualMachines()) {
									log.info("CDO VirtualMachine " + vm.getName() + " [" + vm.getId() + "]");

									VirtualMachine.Id vmId = new VirtualMachine.Id(vm.getId());

									long vmMemory = 0;

									List<CPU> vmCPUs = new ArrayList<CPU>();

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
										vmMemory += vmem.getProvisioned().getEstimatedValue();
									}

									// CPU[] vcpus = new CPU[1];
									// vcpus[0] = new CPU(cores.toArray(new
									// Core[cores.size()]));

									VirtualMachine virtualMachine = new VirtualMachine(vmId,
											vmCPUs.toArray(new CPU[vmCPUs.size()]), (int) vmMemory, storageCapacity,
											storagePerformance, networkCapacity, networkPerformance);

									virtualMachines.add(virtualMachine);

									// Mapping mapping = new Mapping(vmId,
									// pmId);
									// mappings.add(mapping);
								}
							}
						}

						physicalMachines.add(pm);
					}
				}
			}
		}

		return new DataCenter(physicalMachines.toArray(new PhysicalMachine[physicalMachines.size()]),
				virtualMachines.toArray(new VirtualMachine[virtualMachines.size()]), new Constraint[] {
						NoCPUCoreOverCommitGlobalConstraint.SINGLETON, NoRAMOverCommitGlobalConstraint.SINGLETON });
	}

	private static Id[] getPMIdsFromCdo(PhysicalDCModel pdcm) {
		List<Id> ids = new ArrayList<PhysicalMachine.Id>();

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

	private static DataCenter getDataCenterModelFromCdo(PhysicalDCModel pdcm, LogicalDCModel ldcm,
			List<eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine> vmsToPlace) {
		List<PhysicalMachine> physicalMachines = new ArrayList<PhysicalMachine>();
		List<se.umu.cs.ds.causa.models.VirtualMachine> virtualMachines = new ArrayList<se.umu.cs.ds.causa.models.VirtualMachine>();

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
						// int totalMemory = 0;
						int totalMemory = -2048; // Buffer for Hypervisor

						EList<ProcessingUnitSpecification> cpuSpecs = computeNode.getCpuSpecifications();

						List<CPU> cpus = new ArrayList<PhysicalMachine.CPU>();

						for (ProcessingUnitSpecification pus : cpuSpecs) {

							log.info("CDO ProcessingUnitSpecification " + pus.getName() + " [" + pus.getId()
									+ "] #cores=" + pus.getNumberOfCores() + ", freq=" + pus.getFrequency());

							noCores += pus.getNumberOfCores();
							List<Core> cores = new ArrayList<Core>();
							// Core core = new
							// Core((int)Math.round(pus.getFrequency().doubleValue(SI.HERTZ)));
							for (int i = 0; i < noCores; i++) {
								Core core = new Core((int) Math.round(pus.getFrequency().getEstimatedValue()));
								cores.add(core);
							}
							CPU cpu = new CPU(cores.toArray(new Core[cores.size()]));
							cpus.add(cpu);
						}

						for (MemorySpecification ms : computeNode.getMemorySpecifications()) {
							log.info("CDO MemorySpecification " + ms.getName() + " [" + ms.getId() + "] size="
									+ ms.getSize());
							totalMemory += ms.getSize().getEstimatedValue();
						}

						int storageCapacity = 0;
						int storagePerformance = 0;
						int networkCapacity = 0;
						int networkPerformance = 0;

						PhysicalMachine pm = new PhysicalMachine(new Id(id), cpus.toArray(new CPU[cpus.size()]),
								totalMemory, storageCapacity, storagePerformance, networkCapacity, networkPerformance);

						for (Hypervisor h : ldcm.getHypervisors()) {
							log.info("CDO Hypervisor [" + h.getId() + "]");
							if ((h.getNode() != null) && (h.getNode().getName().equals(id))) {
								for (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm : h
										.getVirtualMachines()) {
									se.umu.cs.ds.causa.models.VirtualMachine.Id vmId = new se.umu.cs.ds.causa.models.VirtualMachine.Id(
											vm.getId());

									long vmMemory = 0;

									List<CPU> vmCPUs = new ArrayList<CPU>();

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

									// for (VirtualMemory vmem :
									// vm.getVirtualMemoryUnits()) {
									// vmMemory +=
									// vmem.getProvisioned().getEstimatedValue();
									// }

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
										vmMemory += vmem.getProvisioned().getEstimatedValue();
									}

									// CPU[] vcpus = new CPU[1];
									// vcpus[0] = new CPU(cores.toArray(new
									// Core[cores.size()]));

									se.umu.cs.ds.causa.models.VirtualMachine virtualMachine = new se.umu.cs.ds.causa.models.VirtualMachine(
											vmId, vmCPUs.toArray(new CPU[vmCPUs.size()]), (int) vmMemory,
											storageCapacity, storagePerformance, networkCapacity, networkPerformance);

									virtualMachines.add(virtualMachine);
									// try {
									// pm.assignVm(vmToAssign);
									// } catch (PlacementException ex) {
									//
									// }

									// Mapping mapping = new Mapping(vmId,
									// pmId);
									// mappings.add(mapping);
								}
							}
						}

						physicalMachines.add(pm);
					}
				}
			}
		}

		log.info("Virtual Machine waiting for placement");
		for (eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine vm : vmsToPlace) {
			log.info("CDO VirtualMachine " + vm.getName() + " [" + vm.getId() + "]");

			se.umu.cs.ds.causa.models.VirtualMachine.Id vmId = new se.umu.cs.ds.causa.models.VirtualMachine.Id(
					vm.getId());

			long vmMemory = 0;
			int storageCapacity = 0;
			int storagePerformance = 0;
			int networkCapacity = 0;
			int networkPerformance = 0;

			List<CPU> vmCPUs = new ArrayList<CPU>();

			for (VirtualProcessingUnit vpu : vm.getVirtualProcessingUnits()) {
				log.info("CDO VirtualProcessingUnit " + vpu.getName() + " [" + vpu.getId() + "]" + "#virtualCores="
						+ vpu.getVirtualCores());
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

			// if VM doesn't have any CPUs assigned add one
			// (for compatibility with Causa - CPU freq)
			if (vmCPUs.size() == 0) {
				Core vCore = new Core(2000);
				Core[] vCores = new Core[1];
				vCores[0] = vCore;
				CPU vCPU = new CPU(vCores);
				vmCPUs.add(vCPU);
			}

			// for (VirtualMemory vmem : vm.getVirtualMemoryUnits()) {
			// vmMemory += vmem.getProvisioned().getEstimatedValue();
			// }

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
				vmMemory += vmem.getProvisioned().getEstimatedValue();
			}

			// CPU[] vcpus = new CPU[1];
			// vcpus[0] = new CPU(cores.toArray(new
			// Core[cores.size()]));

			se.umu.cs.ds.causa.models.VirtualMachine virtualMachine = new se.umu.cs.ds.causa.models.VirtualMachine(vmId,
					vmCPUs.toArray(new CPU[vmCPUs.size()]), (int) vmMemory, storageCapacity, storagePerformance,
					networkCapacity, networkPerformance);

			virtualMachines.add(virtualMachine);
		}

		PhysicalMachine[] pms = physicalMachines.toArray(new PhysicalMachine[physicalMachines.size()]);
		se.umu.cs.ds.causa.models.VirtualMachine[] vms = virtualMachines
				.toArray(new se.umu.cs.ds.causa.models.VirtualMachine[virtualMachines.size()]);
		Constraint[] constraints = new Constraint[] { NoCPUCoreOverCommitGlobalConstraint.SINGLETON,
				NoRAMOverCommitGlobalConstraint.SINGLETON };
		DataCenter dataCenter = new DataCenter(pms, vms, constraints);

		return dataCenter;
	}
}
