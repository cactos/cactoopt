package eu.cactosfp7.cactoopt.behaviourinference;

import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.physicaldc.architecturetype.ArchitectureTypeRepository;

/**Interface for a behaviour inference algorithm.
 * @author hgroenda
 *
 */
public interface IBehaviourInferenceAlgorithm {
	
	/**
	 * Infer behaviour for the provided virtual machine.
	 * <p/>
	 * The behaviour is added to the virtual machine description. The behaviour
	 * states the architecture type for which the resource demand is inferred.
	 * The type is taken from the repository. Its created there if it does not
	 * exist yet. Uses {@link ArchitectureTypeRepository}.
	 * 
	 * @param vm
	 *            The virtual machine.
	 * @param architectureTypeRepo
	 *            The architecture type repository.
	 */
	public void inferBehaviour(VirtualMachine vm, ArchitectureTypeRepository architectureTypeRepo);
}
