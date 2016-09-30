package eu.cactosfp7.cactoopt.models;
import java.util.Comparator;

public class PhysicalMachineCpuComparator implements Comparator<PhysicalMachine> {
    @Override
    public int compare(PhysicalMachine o1, PhysicalMachine o2) {
    	Double o1CpuLoad = new Double(o1.getCpuUtilization());
    	Double o2CpuLoad = new Double(o2.getCpuUtilization());
    	
        return o1CpuLoad.compareTo(o2CpuLoad);
    }
}
