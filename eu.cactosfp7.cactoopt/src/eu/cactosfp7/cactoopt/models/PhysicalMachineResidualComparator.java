package eu.cactosfp7.cactoopt.models;
import java.util.Comparator;

public class PhysicalMachineResidualComparator implements Comparator<PhysicalMachine> {
    @Override
    public int compare(PhysicalMachine o1, PhysicalMachine o2) {
    	Double o1Residual = new Double(o1.getResidualEvaluation());
    	Double o2Residual = new Double(o2.getResidualEvaluation());
    	
        return o2Residual.compareTo(o1Residual);
    }
}
