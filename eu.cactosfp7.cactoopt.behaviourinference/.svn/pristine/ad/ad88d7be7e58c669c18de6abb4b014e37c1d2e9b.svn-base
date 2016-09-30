package eu.cactosfp7.cactoopt.behaviourinference;

import java.util.TreeMap;

public enum ModelDistributionCreator {

	INSTANCE;
	
	private MolProOccuranceHistogram stats;
	
	private ModelDistributionCreator() {
		MolProOccuranceHistogram temp;
		try {
			temp = new MolProOccuranceHistogram();
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
		stats = temp;
	}
		
	public TreeMap<Double, IntervalBehaviourValues> getInterval(int numberCoresVM2, int memSizeVM2) {
		String name="lccsd_con1_"+numberCoresVM2+"core_"+memSizeVM2+"gig";
		return stats.getIntervalData(name);
	}
	
	// add methods here //
	
}

