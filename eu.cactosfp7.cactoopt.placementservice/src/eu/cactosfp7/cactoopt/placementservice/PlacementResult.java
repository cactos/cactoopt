package eu.cactosfp7.cactoopt.placementservice;

public class PlacementResult {
    
    public PlacementResult(Status status, String uuid) {
        this.status = status;
        this.uuid = uuid;
    }
    
    public PlacementResult(Status status, String uuid, String planId) {
        this.status = status;
        this.uuid = uuid;
        this.planId = planId;
    }
    
    public enum Status {
        SUCCESSFUL, FAILED_CONCURRENT_OPTIMISATION, FAILED_IMPOSSIBLE, FAILED_TRANSACTION_EXCEPTION
    }
    
    private Status status;
    
    private String uuid;
    
    private String planId; 
    
    public String getUuid() {
        return this.uuid;
    }
    
    public Status getStatus() {
        return this.status;
    }
    
    public String getPlanId() {
        return this.planId;
    }
}
