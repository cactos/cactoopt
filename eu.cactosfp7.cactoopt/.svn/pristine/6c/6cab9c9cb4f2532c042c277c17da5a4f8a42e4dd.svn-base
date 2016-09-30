package eu.cactosfp7.cactoopt.models;
import java.util.Comparator;

public class PhysicalMachineMemoryComparator implements Comparator<PhysicalMachine> {
    @Override
    public int compare(PhysicalMachine o1, PhysicalMachine o2) {
    	Double o1MemoryLoad = new Double(o1.getMemoryUtilization());
    	Double o2MemoryLoad = new Double(o2.getMemoryUtilization());
    	
        return o1MemoryLoad.compareTo(o2MemoryLoad);
    }
}
