package eu.cactosfp7.cactoopt.optimisationservice.randomwithconstraints;

import java.util.List;
import java.util.Random;

import eu.cactosfp7.cactoopt.models.PhysicalMachine;
import eu.cactosfp7.cactoopt.models.VirtualMachineMigrationAction;
import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
import eu.cactosfp7.cactoopt.util.CDOModelHelper;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationplanFactory;
import eu.cactosfp7.optimisationplan.SequentialSteps;
import eu.cactosfp7.optimisationplan.VmMigrationAction;

/**
 * Generates random migrations that do not violate the capability constraints of physical machines
 * @author jakub
 *
 */
public class RandomWithConstraintsOptimisationAlgorithm implements IOptimisationAlgorithm {

	public RandomWithConstraintsOptimisationAlgorithm() {
		super();
	}

	@Override
	public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm,
			LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm) {

		OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
		SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
		plan.setOptimisationStep(rootStep);
		rootStep.setOptimisationPlan(plan);
		
		List<PhysicalMachine> pms = CDOModelHelper.getPhysicalMachinesFromCdoModel(pdcm, ldcm);
		VirtualMachineMigrationAction migrationSuggested = null;
		
		System.out.println("Initial DC state");
		for(PhysicalMachine pm : pms) {
			System.out.println(pm.toString());
		}	
		
		do
		{
			migrationSuggested = migrationRandom(pms);
			
			if (migrationSuggested != null) {
				VmMigrationAction migration = OptimisationplanFactory.eINSTANCE.createVmMigrationAction();
				migration.setMigratedVm(CDOModelHelper.getVirtualMachineById(migrationSuggested.getVm().getId(), ldcm));
				migration.setSourceHost(CDOModelHelper.getComputeNodeById(migrationSuggested.getSource().getId(), pdcm).getHypervisor());
				migration.setTargetHost(CDOModelHelper.getComputeNodeById(migrationSuggested.getTarget().getId(), pdcm).getHypervisor());
				migration.setSequentialSteps(rootStep);
				
				System.out.println("After migration");
				for(PhysicalMachine pm : pms) {
					System.out.println(pm.toString());
				}	
			}
		} while (migrationSuggested != null);
		
		return plan;
	}
	
	private VirtualMachineMigrationAction migrationRandom(List<PhysicalMachine> pms) {
		VirtualMachineMigrationAction migration = null;
		
		Random rnd = new Random();
		
		int suggestMigration = rnd.nextInt(100);
		
		// Probability of generating a migration (25%)
		if (suggestMigration >= 25) {
			PhysicalMachine source = pms.get(rnd.nextInt(pms.size()));
			List<eu.cactosfp7.cactoopt.models.VirtualMachine> vms = source.getVms();
			eu.cactosfp7.cactoopt.models.VirtualMachine vm = vms.get(rnd.nextInt(vms.size()));
			PhysicalMachine target;
			
			do {
				target = pms.get(rnd.nextInt(pms.size()));
			} while (target.getId() == source.getId() && target.isPoweredOn());
			
			if (target.assignVm(vm)) {
				source.unassignVm(vm);
				System.out.println("Migrate " + vm.getId() + " from " + source.getId() + " to " + target.getId());
				
				migration = new VirtualMachineMigrationAction(vm, source, target);	
			}
		}
		
		return migration;
	}	
}