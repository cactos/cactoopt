/**
 * 
 */
package eu.cactosfp7.cactoopt.behaviourinference;

import eu.cactosfp7.cactoopt.models.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import eu.cactosfp7.cactoopt.behaviourinference.MolProOccuranceHistogram;

import java.util.logging.Logger;

/**
 * @author ahmeda
 *
 */
public class BehavioureInference implements ISimpleVMBehaviourInferrer{
	
	int numberCoresVM;
	int  memSizeVM;
	int counter=0;
	String HDTypeVM;
	String appType;
	double measurementStartTime;
	double measurementEndTime;
	HashMap<String, TreeMap> distBehaviour=new HashMap<String, TreeMap>();
	TreeMap<Double, IntervalBehaviourValues> behaviour=null;
	
	
	private static final Logger log = Logger.getLogger(BehavioureInference.class.getName());
	
	public BehavioureInference(){
		log.info("Starting the Behaviour inference engine");
		
	}
	
	public BehavioureInference(int numberCores, int memSize, String hardDiskType)
	{
		numberCoresVM=numberCores;
		memSizeVM=memSize;
		HDTypeVM=hardDiskType;
	}

	@Override
	public SimpleVMBehaviour getBehaviour(String vmUuuid, double from, double to) {
		return null;
		}
	
	@Override
	public SimpleVMBehaviour getBehaviour( double from, double to) {
			
			return null;
	}
	

	@Override
	public TreeMap<Double, IntervalBehaviourValues> getBehaviour(String vmuuID, int numberCores, int memory,
			String hdType, String appTypeVM) throws IOException {
		numberCoresVM=numberCores;
		memSizeVM=memory;
		HDTypeVM=hdType;
		appType=appTypeVM;
		String vmID=vmuuID;
	//	System.out.println("Something Shitty here:"+distBehaviour.keySet());
		if (distBehaviour.containsKey(vmID)){
			behaviour= getMolproBehaviour(numberCoresVM,memSizeVM,HDTypeVM);
			distBehaviour.put(vmID, behaviour);
			return behaviour;
		} 
		else
		{
		switch (appType) {
		case "Wiki":
			log.info("Wikipedia Application");
			behaviour=getWikiBehaviour(numberCoresVM,memSizeVM,HDTypeVM);
			distBehaviour.put(vmID, behaviour);
			break;
		case "MolPro":
			log.info("Molpro Application");
			behaviour=getMolproBehaviour(numberCoresVM,memSizeVM,HDTypeVM);
			distBehaviour.put(vmID, behaviour);
			System.out.println(behaviour.keySet());
			return behaviour;

		case "Playgen":
			log.info("Playgen Application");
			behaviour=getPlaygenBehaviour(numberCoresVM,memSizeVM,HDTypeVM);
			distBehaviour.put(vmID, behaviour);
			break;
		case "BlackBox":
			log.info("BlackBox Application");
			behaviour=getBlackBoxBehaviour(numberCoresVM,memSizeVM,HDTypeVM);
			distBehaviour.put(vmID, behaviour);
			break;
		default:
			log.info("No known App type");
			break;
		}
		
		
	}
		return behaviour;
	}
	private TreeMap<Double, IntervalBehaviourValues> getBlackBoxBehaviour(int numberCoresVM2,
			int memSizeVM2, String hDTypeVM2) {
		// TODO Need to discuss how to get this right for Simulation. For Opt only tagging is required (classification based on behaviour).
		return null;
	}

	private TreeMap<Double, IntervalBehaviourValues> getPlaygenBehaviour(int numberCoresVM2,
			int memSizeVM2, String hDTypeVM2) {
		// TODO Divide this into different tiers for PlayGen
		return null;
	}

	private TreeMap<Double, IntervalBehaviourValues> getMolproBehaviour(int numberCoresVM2, int memSizeVM2, String hDTypeVM2) throws IOException {
		TreeMap<Double, IntervalBehaviourValues> out=ModelDistributionCreator.INSTANCE.getInterval(numberCoresVM2, memSizeVM2);
		//log.info("INSTANCE 1"+ out);
		 out=ModelDistributionCreator.INSTANCE.getInterval(numberCoresVM2, memSizeVM2);
		return out;//ModelDistributionCreator.INSTANCE.getInterval(numberCoresVM2, memSizeVM2);
	}

	private TreeMap<Double, IntervalBehaviourValues> getWikiBehaviour(int numberCoresVM2,
			int memSizeVM2, String hDTypeVM2) {
		// TODO Auto-generated method stub
		return null;
	}
	

}

