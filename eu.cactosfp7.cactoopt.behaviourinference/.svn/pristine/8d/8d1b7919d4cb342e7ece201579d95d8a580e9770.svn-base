package eu.cactosfp7.cactoopt.behaviourinference;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.io.File;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.geometry.spherical.oned.ArcsSet.Split;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.apache.commons.math3.util.Pair;

public class MolProOccuranceHistogram {
	
	private List<Integer> occurance;
	private static final Logger log = Logger.getLogger(MolProOccuranceHistogram.class.getName());
	
	private String fname="/home/ahmeda/storedModels/";
	private final File folder = new File(fname);
	private HashMap<String, EmpiricalDistribution> activeModels=new HashMap<String, EmpiricalDistribution>();
	private TreeMap<Integer, IntervalBehaviourValues> queryOutput=new TreeMap<Integer,IntervalBehaviourValues>();
	
	
	/**
	 * Initializing the class tries reading a file that stores the history of the run serialized.
	 * @throws NullArgumentException 
	 */
	public MolProOccuranceHistogram() throws NullArgumentException, IOException{
			
			File hist=null;
			List<String> strs = this.listFilesForFolder(folder);
			for(String s:strs){
				EmpiricalDistribution dist=new EmpiricalDistribution();
				//dist.reSeed(0);
				s=fname.concat(s);
				hist=new File(s);
				dist.load(hist);
				this.activeModels.put(s, dist);
			
			}
	}
	
	/**
	 * Method to list all files in a folder and return the results a list of Strings. Takes as input the folder as a File object.
	 * Now the folder is set using a set variable. 
	 */
	public List<String> listFilesForFolder(final File folder) {
		List<String> strs = new ArrayList<String>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        	strs.add(fileEntry.getName());
	        }
	    }
		return strs;
	}
	
	/**
	 * When the model is updated, i.e., a new run of Molpro with a configuration we know (or do not know) runs, we save the new value 
	 * to the file carrying the  name of the configuration. The model is reloaded to the Hashmap.
	 * @param fileName, name of the file. This should represent the configuration used (including if it is total or interval based length).
	 * @value this is the length of the run.
	 */ 
	
	public void addRunningTime(String fileName, Double value){
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {
		    out.println(value);
		    File hist=new File(fileName);
		    EmpiricalDistribution dist=new EmpiricalDistribution();
		    dist.load(hist);
			//dist.reSeed(0);
			activeModels.put(fileName, dist);
		}catch (IOException e) {
		    log.info("Problem is adding value to the model");
		}
	}

	/**
	 * returns the next value given a configuration for one of the models as a double. 
	 */
//	public Double getModelOutput(String configuration){
//		EmpiricalDistribution model=activeModels.get(configuration);
//		return model.getNextValue();
//		 
//	}
	
	/**
	 * Given an interval, return the expected average usage during this interval for all possible subintervals
	 * @throws IOException 
	 */

	public TreeMap<Double, IntervalBehaviourValues> getIntervalData(String name) {
//		System.out.println(name);
		TreeMap<Double, IntervalBehaviourValues> out=new TreeMap<Double, IntervalBehaviourValues>();
		TreeMap<Double, IntervalBehaviourValues> Newout=new TreeMap<Double, IntervalBehaviourValues>();
		ArrayList<Double> li= new ArrayList<Double>();
		getAllStats(name);
		double previousValue=0;
		for (int m:this.queryOutput.keySet()){
			if (m!=0){
			double timeee=this.queryOutput.get(m).getInterval();
//			System.out.println("Only Time:"+timeee+"  "+m);
			out.put((timeee+previousValue), this.queryOutput.get(m));
			previousValue=timeee+previousValue;}
		}
		double ToAdd=(this.queryOutput.get(0).getTotalTime()-previousValue)/5;
		int counter=0;
		double summer=0;
		for (double m:out.keySet()){
			if (counter!=0){
				summer+=ToAdd;
//			log.info("Almost there: "+m+"  "+ToAdd);
			Newout.put((m+summer), out.get(m));
			}
			counter++;
			}
		
		return Newout;
		
	}
	
	public void getAllStats(String name){
		String model=name;
		
		for (String m  : activeModels.keySet()) {
			if (m.contains(model)){
				String [] splittedFN=m.split("interval");
//				log.info("interval  "+m+"   "+splittedFN[0]);
				String[] measureName=splittedFN[0].split("_");
//				System.out.println(measureName[measureName.length-1]);
				if (splittedFN.length>1){
//					System.out.println(m+" Here again "+(measureName[measureName.length-1]));
					if (this.queryOutput.containsKey(Integer.parseInt(splittedFN[1]))){
						switch (measureName[measureName.length-1]) {
						case "avgcpu":
							this.queryOutput.get(Integer.parseInt(splittedFN[1])).setCpuResourceDemand(activeModels.get(m).getNextValue());
							break;
						case "Length":
							double t=this.activeModels.get(m).getNextValue();
							this.queryOutput.get(Integer.parseInt(splittedFN[1])).setInterval(t,m);
//							log.info("1111111111111111111  "+ measureName[measureName.length-1]+"   "+m);
							break;
						case "avgwrites":
							this.queryOutput.get(Integer.parseInt(splittedFN[1])).setBytesWritten(activeModels.get(m).getNextValue());
							break;
						case "avgreads":
							this.queryOutput.get(Integer.parseInt(splittedFN[1])).setBytesRead(activeModels.get(m).getNextValue());
							break;
						default:
//							log.info("222222222222222  "+ measureName[measureName.length-1]+"   "+m);
							this.queryOutput.get(Integer.parseInt(splittedFN[1])).setTotTime(activeModels.get(m).getNextValue());
							break;
						}
					}
					else {
						IntervalBehaviourValues outIntervals=new IntervalBehaviourValues(m);
						switch (measureName[measureName.length-1]) {
						case "avgcpu":
							outIntervals.setCpuResourceDemand(activeModels.get(m).getNextValue());
							this.queryOutput.put(Integer.parseInt(splittedFN[1]), outIntervals);
							break;
						case "Length":
							double t=this.activeModels.get(m).getNextValue();
//							log.info("333333333333333  "+ measureName[measureName.length-1]+"   "+m);
							outIntervals.setInterval(t,m);
							this.queryOutput.put(Integer.parseInt(splittedFN[1]), outIntervals);
//							log.info("-------------------- "+t+" "+m);
							break;
						case "avgwrites":
							outIntervals.setBytesWritten(activeModels.get(m).getNextValue());
							this.queryOutput.put(Integer.parseInt(splittedFN[1]), outIntervals);
							break;
						case "avgreads":
							outIntervals.setBytesRead(activeModels.get(m).getNextValue());
							this.queryOutput.put(Integer.parseInt(splittedFN[1]), outIntervals);
							break;
						default:
							outIntervals.setTotTime(activeModels.get(m).getNextValue());
							this.queryOutput.put(Integer.parseInt(splittedFN[1]), outIntervals);
//							log.info("444444444444444444  "+ measureName[measureName.length-1]+"   "+m);
							break;
							}
						
//						System.out.println("Here I am: "+this.queryOutput.keySet());
					}
			}
			
			
		}
		
	}
//	public void getAllStats(){
//		log.info("getAllStats");
//		for (String m  : activeModels.keySet()) {
//			System.out.println(m);
//			String [] splittedFN=m.split("interval");
//			String[] measureName=splittedFN[0].split("_");
//			log.info("Inside:");
//			Double val=activeModels.get(m).getNextValue();
////			System.out.println(val);
//			System.out.println(Arrays.toString(splittedFN));
//			try{
//			this.queryOutput.put(splittedFN[1],val);
//			}catch (ArrayIndexOutOfBoundsException e) {
//			    log.info("Total Time file");
//			}
//			
//		}
//		System.out.println(this.queryOutput);
//	}
	}
	}
	
