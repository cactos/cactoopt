package eu.cactosfp7.cactoopt.models;
import java.util.Comparator;

public class VirtualMachineWeightComparator implements Comparator<VirtualMachine> {
    @Override
    public int compare(VirtualMachine o1, VirtualMachine o2) {

    	double alpha = 0.5;
    	
    	Double vm1 = (1-alpha) * o1.getNoCores() + alpha * o1.getMemory();
    	Double vm2 = (1-alpha) * o2.getNoCores() + alpha * o2.getMemory();
    	
        return vm2.compareTo(vm1);
    }
}
