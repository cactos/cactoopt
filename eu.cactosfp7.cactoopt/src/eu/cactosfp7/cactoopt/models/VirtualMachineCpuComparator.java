package eu.cactosfp7.cactoopt.models;
import java.util.Comparator;

/**
 * 
 * @author jakub
 *
 */
public class VirtualMachineCpuComparator implements Comparator<VirtualMachine> {
    @Override
    public int compare(VirtualMachine o1, VirtualMachine o2) {
    	Integer o1CpuCores = new Integer(o1.getNoCores());
    	Integer o2CpuCores = new Integer(o2.getNoCores());
    	
        return o1CpuCores.compareTo(o2CpuCores);
    }
}
