package eu.cactosfp7.cactoopt.behaviourinference;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.io.File;

import org.apache.commons.math3.random.EmpiricalDistribution;

public class OccuranceHistogram {
	
	private List<Integer> occurance;
	private static final Logger log = Logger.getLogger(OccuranceHistogram.class.getName());
	private EmpiricalDistribution dist=new EmpiricalDistribution();
	private final File folder = new File("./storedModels");
	private HashMap<String, EmpiricalDistribution> activeModels=new HashMap<String, EmpiricalDistribution>();
	
	
	/**
	 * Initializing the class tries reading a file that stores the history of the run serialized.
	 * @throws IOException 
	 */
	public OccuranceHistogram() throws IOException{
		
			File hist=null;
			List<String> strs = this.listFilesForFolder(folder);
			for(String s:strs){
				hist=new File(s);
				dist.load(hist);
			//	dist.reSeed(0);
				activeModels.put(s, dist);
			}
	}
	
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
		    dist.load(hist);
			//dist.reSeed(0);
			activeModels.put(fileName, dist);
		}catch (IOException e) {
		    log.info("Problem is adding value to the model");
		}
	}

	public Double getRunningTime(String configuration){
		EmpiricalDistribution model=activeModels.get(configuration);
		return model.getNextValue();
		 
	}
}
