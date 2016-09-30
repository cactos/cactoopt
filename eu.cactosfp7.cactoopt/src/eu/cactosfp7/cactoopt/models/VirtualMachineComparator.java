package eu.cactosfp7.cactoopt.models;
import java.util.Comparator;

import javax.measure.quantity.DataAmount;
import javax.measure.unit.NonSI;

import org.eclipse.emf.common.util.EList;
import org.jscience.physics.amount.Amount;

import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMemory;


public class VirtualMachineComparator implements Comparator<VirtualMachine> {
    @Override
    public int compare(VirtualMachine o1, VirtualMachine o2) {
    	EList<VirtualMemory> vMemoryUnits1 = o1.getVirtualMemoryUnits();
    	EList<VirtualMemory> vMemoryUnits2 = o2.getVirtualMemoryUnits();
    	
    	Amount<DataAmount> provisionedMemory1 = Amount.valueOf(0, NonSI.BYTE);
    	Amount<DataAmount> provisionedMemory2 = Amount.valueOf(0, NonSI.BYTE);
    	
    	for (VirtualMemory vMem : vMemoryUnits1) {
    			provisionedMemory1 = provisionedMemory1.plus(vMem.getProvisioned());
    	}
    	
    	for (VirtualMemory vMem : vMemoryUnits2) {
			provisionedMemory2 = provisionedMemory2.plus(vMem.getProvisioned());
    	}
    	
        return provisionedMemory2.compareTo(provisionedMemory1);
    }
}