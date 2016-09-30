package eu.cactosfp7.cactoopt.optimisationservice.linkernighan;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

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
import eu.cactosfp7.cactoopt.framework.model.MigrationMove;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
import eu.cactosfp7.cactoopt.util.CDOModelHelper;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.OptimisationPlan;

/**
 * Lin-Kernighan optimisation algorithm.
 * @author jakub
 *
 */
public class LinKernighanOptimisationAlgorithm implements IOptimisationAlgorithm {

    /** The logger for this class. */
    private static final Logger log = Logger.getLogger(LinKernighanOptimisationAlgorithm.class.getName());
    
    @Override
    public OptimisationPlan generateOptimizationPlan(PhysicalDCModel pdcm,
            LogicalDCModel ldcm, PhysicalLoadModel plm, LogicalLoadModel llm) {
    	
    	log.info("LinKernighan algorithm starts");
        
        List<PhysicalMachine> hosts = CDOModelHelper.getFrameworkPhysicalMachinesFromCdoModel(pdcm, ldcm);

        log.info("Initial DC state");
        for(PhysicalMachine pm : hosts) {
            log.info(pm.toString());
        }   
        
        List<DoubleMigrationMoveStrategy> doubleMigrationMoveStrategies = Arrays
                .<DoubleMigrationMoveStrategy> asList(new RotationMigrationMoveStrategy());
        
        MigrationAwareObjectiveFunction objectiveFunction = new MigrationAwareObjectiveFunction(
                new MemoryLoadBalancingEvaluationFunction(),
                new MemoryPageMigrationCostFunction(0, 0.0), 0.0);

        IterativePlacementOptimizer optimizer = new IterativePlacementOptimizer(
                objectiveFunction, MigrationSelectionStrategies.best(),
                new TryAllSingleMoveMigrationStrategy(),
                doubleMigrationMoveStrategies, StopStrategies.unlimited());
        
        PlacementMapping initialMapping = new PlacementMapping(hosts);
        MigrationPath optimisationResult = optimizer.optimizePlacement(initialMapping);
        List<MigrationMove> migrations = optimisationResult.getMigrationMoves();
        
        log.info("After migration");
        for(PhysicalMachine pm : optimisationResult.getResultMapping().getPhysicalMachines()) {
            log.info(pm.toString());
        }   
        
        return CDOModelHelper.tranformListOfMigrationMovesToOptimisationPlan(pdcm, ldcm, migrations);
    }

}
