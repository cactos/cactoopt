/**
 * Package containing the behaviour inference tooling.
 */
package eu.cactosfp7.cactoopt.behaviourinference;

import java.io.IOException;
import java.util.TreeMap;

/**
 * Simple interface specifying interactions between CactoSim and the behaviour model inference tooling.
 * @author stier, groenda
 *
 */
public interface ISimpleVMBehaviourInferrer {
	/**
	 * Get the behaviour of the specified VM, assuming a simplified resource model. The behaviour is fetched for the interval [<code>from</code>,<code>to</code>). 
	 * The times are specified in simulation time seconds relative to the starting point of the simulation (0s).
	 * @param vmUuuid The UUID of the VM in the CACTOS infrastructure model.
	 * @param from Start time of the time interval for which the behaviour is fetched. Must be greater or equal to zero.
	 * @param to End time of the time interval for which the behaviour is fetched. Must be greater than <code>from</code>.
	 * @return The behaviour, containing utilization values for CPU and HDD for the given interval.
	 */
	public SimpleVMBehaviour getBehaviour(String vmUuuid, double from, double to);
	
	
	/**
	 * Get the behaviour of a VM with Specs stated in the instantiation of the Behaviour inference engine, assuming a simplified resource model.
	 *  The behaviour is fetched for the interval [<code>from</code>,<code>to</code>). 
	 * The times are specified in simulation time seconds relative to the starting point of the simulation (0s).
	 * @param from Start time of the time interval for which the behaviour is fetched. Must be greater or equal to zero.
	 * @param to End time of the time interval for which the behaviour is fetched. Must be greater than <code>from</code>.
	 * @return The behaviour, containing utilization values for CPU and HDD for the given interval.
	 */
	public SimpleVMBehaviour getBehaviour(double from, double to);
	
	
	/**
	 * Get the behaviour of a VM with Specs stated in the instantiation of the Behaviour inference engine, assuming a simplified resource model.
	 *  The behaviour is fetched for the whole expected time of the VM running. 
	 * The times are specified in simulation time seconds relative to the starting point of the simulation (0s).
	 * @param numberCores is the number of cores in the VM
	 * @param memory is the size of the memory allocated to the VM
	 * @param hdType is the HD type, should evaluate to either "HDD" or "SSD"
	 * @param appType is the Application for which the model is needed. Should evaluate to either "Wiki", "MolPro", "Playgen" or "BlackBox"
	 * @return a TreeMap, containing utilization values for CPU and HDD for the whole life time of the simulation. The key is the when the interval ends, i.e., if the first 
	 * entry in the map is 100:IntervalBehaviourValues, then the first phase ends after 100 simulated seconds. During this period, the average CPU and I/O is as in the value object.
	 * Notes: Reason to do this is that the start period and the end period if provided as originally suggested in the Interface can overlap with multiple phases and will probably 
	 * make their interpretation very hard. This interface is valid for Molpro now. We need to discuss if this is how we would like to have it for the simulator.  
	 * 
	 * @throws IOException 
	 */
	public  TreeMap<Double, IntervalBehaviourValues> getBehaviour(String vmUuid,int numberCores, int memory, String hdType, String appType) throws IOException;
}
