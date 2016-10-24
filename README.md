# cactoopt
The purpose of CactoOpt is to facilitate the optimisation of data centre infrastructures with respect to the efficient provisioning of computational and storage resources to virtual machines in the context of IaaS-based cloud data centres.

# Contact

For questions and support please contact us at: cactoopt@cs.umu.se or contact the main code developers.

# Configuration of CactoOpt
CactoOpt is configured by a set of configuration files located in **eu.cactosfp7.configuration** project.

## Optimisation
The main configuration file for optimisation is **cactoopt_optimisationalgorithm.cfg**. Other configuration files (if apply) are relevant only for particular optimisation services.

### cactoopt_optimisationalgorithm.cfg
This configuration file allows to choose the optimisation service that will be used for migrations, the algorithm for horizontal autoscaling, and enable resource control (vertical scaling). Available options are listed below.
    
    optimisationName = Causa, Random, LoadBalancing, Consolidation, LinKernighan
    autoscalerAlgorithmName = Hist, AKTE, Reg, or React
    resourceControlEnalbed = true, false
    
The description of the autoscaler algorithms is provided in D3.3 Extended Optimization Model (http://www.cactosfp7.eu/wp-content/uploads/2015/11/D3.3-Extended-Optimization-Model.pdf/ pp. 23--24).

### cactoopt_opt_causa.cfg
*Relevant only if* optimisationName = Causa *in* **cactoopt_optimisationalgorithm.cfg**.

Sets the migration algorithm used by Causa optimisation service.

    algorithm = NONE, LOAD_BALANCING, CONSOLIDATION, ENERGY_EFFICIENCY, FRAGMENTATION, CP_LOAD_BALANCING, CP_CONSOLIDATION, GD_LOAD_BALANCING, HIGH_TO_LOW_LOAD_BALANCING, SINGLE_MIGRATION_LOAD_BALANCING, SINGLE_MIGRATION_CONSOLIDATION

Enables / disables the control of powering up/down physical servers.

    managePhysicalNodeActions = true, false
    
The description of the migration algorithms is provided in D3.3 Extended Optimization Model (http://www.cactosfp7.eu/wp-content/uploads/2015/11/D3.3-Extended-Optimization-Model.pdf/ pp. 36--45).

## Placement
The main configuration file for optimisation is **cactoopt_placement.cfg**. Other configuration files (if apply) are relevant only for particular placement services.

### cactoopt_placement.cfg 
This configuration file allows to choose the service that will be used for placement.
    
    placementName = causa, firstFit, bestFitCpu, bestFitMemory, loadBalancing, consolidation

### cactoopt_placement_causa.cfg
*Relevant only if* placementName = causa *in* **cactoopt_placement.cfg**.

Sets the initial placement algorithm used by Causa placement service.

    algorithm = NONE, BEST_FIT,
    LOAD_BALANCING_RAM, CONSOLIDATION_RAM, CONSOLIDATION, FRAGMENTATION, ENERGY_EFFICIENCY,
    MOLPRO_BEST_FIT, MOLPRO_LOAD_BALANCING_RAM, MOLPRO_CONSOLIDATION_RAM

The description of the placement algorithms is provided in D3.3 Extended Optimization Model (http://www.cactosfp7.eu/wp-content/uploads/2015/11/D3.3-Extended-Optimization-Model.pdf/ pp. 36--45).

MOLPRO_* placement algorithms are aware of special requirements of Molpro jobs depending on the application type (e.g., dft jobs require a computational node with a local storage).
