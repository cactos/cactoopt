package eu.cactosfp7.cactoopt.placementservice.registry;

import eu.cactosfp7.cactoopt.placementservice.IPlacementService;
import eu.cactosfp7.cactoopt.placementservice.PlacementResult;

public class PlacementServiceRegistry implements IPlacementService {

    @Override
    public PlacementResult determinePlacement(String vmUuuid) {
        IPlacementService service = PlacementSettings.SELECTED_PLACEMENT;
        if(service == null){
        	throw new RuntimeException("PlacementServiceRegistry cannot determinePlacement since no PlacementService is found!");
        }
        PlacementResult result = service.determinePlacement(vmUuuid);
        return result;
    }

}
