package eu.cactosfp7.cactoopt.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.functions.impl.MemoryLoadBalancingEvaluationFunction;
import eu.cactosfp7.cactoopt.framework.functions.impl.MemoryPageMigrationCostFunction;
import eu.cactosfp7.cactoopt.framework.functions.impl.MigrationAwareObjectiveFunction;
import eu.cactosfp7.cactoopt.framework.functions.impl.MigrationSelectionStrategies;
import eu.cactosfp7.cactoopt.framework.functions.impl.StopStrategies;
import eu.cactosfp7.cactoopt.framework.impl.IterativePlacementOptimizer;
import eu.cactosfp7.cactoopt.framework.migrationstrategies.DoubleMigrationMoveStrategy;
import eu.cactosfp7.cactoopt.framework.migrationstrategies.impl.RotationMigrationMoveStrategy;
import eu.cactosfp7.cactoopt.framework.migrationstrategies.impl.TryAllSingleMoveMigrationStrategy;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;
import eu.cactosfp7.cactoopt.framework.util.PlacementMappingUtils;

public class TestIterativePlacementOptimizer {
	private static final Logger log = LoggerFactory
			.getLogger(TestIterativePlacementOptimizer.class);
	private IterativePlacementOptimizer optimizer;
	private MigrationAwareObjectiveFunction objectiveFunction;

	private static final AtomicLong hostIdGenerator = new AtomicLong();
	private static final AtomicLong vmIdGenerator = new AtomicLong();

	@Before
	public void before() {
		hostIdGenerator.set(0);
		vmIdGenerator.set(0);

		List<DoubleMigrationMoveStrategy> doubleMigrationMoveStrategies = Arrays
				.<DoubleMigrationMoveStrategy> asList(new RotationMigrationMoveStrategy());

		this.objectiveFunction = new MigrationAwareObjectiveFunction(
				new MemoryLoadBalancingEvaluationFunction(),
				new MemoryPageMigrationCostFunction(0, 0.0001), 0.1);

		this.optimizer = new IterativePlacementOptimizer(
				this.objectiveFunction, MigrationSelectionStrategies.best(),
				new TryAllSingleMoveMigrationStrategy(),
				doubleMigrationMoveStrategies, StopStrategies.unlimited());
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingPhysicalMachines() {
		log.debug(this.optimizer.optimizePlacement(emptyDataCenter())
				.toString());
	}

	@Test
	public void trivialLoadBalancing() {
		List<PhysicalMachine> hosts = physicalMachines(10, 10, 1000);

		// two hosts loaded to 50% in terms of cores and memory, initially
		hosts.get(0).assignVm(vm(1, 100));
		hosts.get(0).assignVm(vm(1, 100));
		hosts.get(0).assignVm(vm(1, 100));
		hosts.get(0).assignVm(vm(1, 100));
		hosts.get(0).assignVm(vm(1, 100));

		hosts.get(1).assignVm(vm(1, 100));
		hosts.get(1).assignVm(vm(1, 100));
		hosts.get(1).assignVm(vm(1, 100));
		hosts.get(1).assignVm(vm(1, 100));
		hosts.get(1).assignVm(vm(1, 100));

		final int vmCount = 10;

		final PlacementMapping initialMapping = new PlacementMapping(hosts);

		log.debug("Evaluation of initial mapping: {}", this.objectiveFunction
				.getEvaluationFunction().evaluateMapping(initialMapping));

		PlacementMapping newMapping = this.optimizer
				.optimizePlacement(initialMapping).getResultMapping();

		log.debug(
				"Evaluation of new mapping: {} and is {}",
				this.objectiveFunction.getEvaluationFunction().evaluateMapping(
						newMapping),
				PlacementMappingUtils.representMapping(newMapping));

		assertEquals(vmCount, newMapping.getVirtualMachines().size());

	}

	@Test
	public void heterogeneousTrivialLoadBalancing() {
		List<PhysicalMachine> hosts = physicalMachines(10, 10, 1000);

		hosts.get(0).assignVm(vm(2, 200));
		hosts.get(0).assignVm(vm(1, 100));
		hosts.get(0).assignVm(vm(2, 200));

		hosts.get(1).assignVm(vm(1, 100));
		hosts.get(1).assignVm(vm(1, 100));
		hosts.get(1).assignVm(vm(1, 100));
		hosts.get(1).assignVm(vm(1, 100));
		hosts.get(1).assignVm(vm(1, 100));

		hosts.get(2).assignVm(vm(1, 100));
		hosts.get(2).assignVm(vm(1, 100));

		final int vmCount = 10;

		final PlacementMapping initialMapping = new PlacementMapping(hosts);

		log.debug("Evaluation of initial mapping: {}", this.objectiveFunction
				.getEvaluationFunction().evaluateMapping(initialMapping));

		PlacementMapping newMapping = this.optimizer
				.optimizePlacement(initialMapping).getResultMapping();

		log.debug(
				"Evaluation of new mapping: {} and is {}",
				this.objectiveFunction.getEvaluationFunction().evaluateMapping(
						newMapping),
				PlacementMappingUtils.representMapping(newMapping));

		assertEquals(vmCount, newMapping.getVirtualMachines().size());
	}

	@Test
	public void heterogeneousComplexLoadBalancing() {
		List<PhysicalMachine> hosts = physicalMachines(10, 10, 1000);

		hosts.get(0).assignVm(vm(8, 800));
		hosts.get(0).assignVm(vm(2, 200));

		hosts.get(1).assignVm(vm(1, 100));
		hosts.get(1).assignVm(vm(2, 200));
		hosts.get(1).assignVm(vm(2, 200));
		hosts.get(1).assignVm(vm(2, 200));
		hosts.get(1).assignVm(vm(1, 100));

		hosts.get(2).assignVm(vm(1, 100));
		hosts.get(2).assignVm(vm(1, 100));

		final int vmCount = 9;

		final PlacementMapping initialMapping = new PlacementMapping(hosts);

		log.debug("Evaluation of initial mapping: {}", this.objectiveFunction
				.getEvaluationFunction().evaluateMapping(initialMapping));

		PlacementMapping newMapping = this.optimizer
				.optimizePlacement(initialMapping).getResultMapping();

		log.debug(
				"Evaluation of new mapping: {} and is {}",
				this.objectiveFunction.getEvaluationFunction().evaluateMapping(
						newMapping),
				PlacementMappingUtils.representMapping(newMapping));

		assertEquals(vmCount, newMapping.getVirtualMachines().size());
	}

	/**
	 * Verifies a case where load balancing cannot be achieved by a single move
	 * strategy.
	 */
	@Test
	public void trivialRotationLoadBalancing() {
		List<PhysicalMachine> hosts = physicalMachines(3, 10, 1000);

		hosts.get(0).assignVm(vm(2, 200));
		hosts.get(0).assignVm(vm(2, 200));

		hosts.get(1).assignVm(vm(1, 100));
		hosts.get(1).assignVm(vm(2, 200));

		hosts.get(2).assignVm(vm(1, 100));
		hosts.get(2).assignVm(vm(1, 100));

		final int vmCount = 6;

		final PlacementMapping initialMapping = new PlacementMapping(hosts);

		log.debug("Evaluation of initial mapping: {}", this.objectiveFunction
				.getEvaluationFunction().evaluateMapping(initialMapping));

		PlacementMapping newMapping = this.optimizer
				.optimizePlacement(initialMapping).getResultMapping();

		log.debug(
				"Evaluation of new mapping: {} and is {}",
				this.objectiveFunction.getEvaluationFunction().evaluateMapping(
						newMapping),
				PlacementMappingUtils.representMapping(newMapping));

		assertEquals(vmCount, newMapping.getVirtualMachines().size());
	}

	private PlacementMapping emptyDataCenter() {
		return new PlacementMapping(physicalMachines(0, 0, 0));
	}

	private List<PhysicalMachine> physicalMachines(int count, int numCores,
			int memory) {
		List<PhysicalMachine> machines = new ArrayList<PhysicalMachine>(count);
		for (int i = 0; i < count; i++) {
			machines.add(new PhysicalMachine("host-"
					+ hostIdGenerator.getAndIncrement(), numCores, memory));
		}
		return machines;
	}

	private List<VirtualMachine> virtualMachines(int count, int requiredCores,
			long requiredMemory) {
		List<VirtualMachine> vms = new ArrayList<VirtualMachine>(count);

		for (int i = 0; i < count; i++) {
			vms.add(vm(requiredCores, requiredMemory));
		}

		return vms;
	}

	private VirtualMachine vm(int requiredCores, long requiredMemory) {
		return new VirtualMachine("vm-" + vmIdGenerator.getAndIncrement(),
				requiredCores, requiredMemory);
	}
}
