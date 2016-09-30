package eu.cactosfp7.cactoopt.placementservice;

/**
 * Initial placement service interface. This interface is to be used to determine the initial placement for a VM.
 * @author stier
 *
 */
public interface IPlacementService {
    /**
     * Determines an initial placement for a VM.
     * @param vmUuuid The UUID of the VM for which a placement is to be determined.
     * @return The UUID of the ComputeNode on which the VM is proposed to be placed.
     */
    public PlacementResult determinePlacement(String vmUuuid);
}
