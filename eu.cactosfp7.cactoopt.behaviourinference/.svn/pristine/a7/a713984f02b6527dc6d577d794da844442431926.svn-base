package eu.cactosfp7.cactoopt.behaviourinference;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import eu.cactosfp7.cactoopt.behaviourinference.BehavioureInference;

public class Tester {

	public static void main(String[] args) {
		try {
			BehavioureInference mlp=new BehavioureInference();
			int memSizeVM2=17;
			int numberCoresVM2=1;
			TreeMap<Double, IntervalBehaviourValues> interval=mlp.getBehaviour("232w4",1,17,"hdd","MolPro");
			//Keys represent the end time of each phase in seconds
			System.out.println(interval.keySet());
			//All values in the TreeMap are stored as IntervalBehaviourClasses
			System.out.println(interval.values());
			//Example Usage 
			System.out.println("FirstKey: "+interval.firstKey());
			System.out.println("Average KiloBytes Written per second: "+interval.get(interval.firstKey()).getBytesWritten());
			System.out.println("Average KiloBytes read per second: "+interval.get(interval.firstKey()).getBytesRead());
			System.out.println("Average CPU cycles used per second: "+interval.get(interval.firstKey()).getCpuResourceDemand());
			System.out.println("Get total length: ");
			System.out.println("  "+interval.get(interval.firstKey()).getTotalTime());

			interval=mlp.getBehaviour("232w4",1,100,"hdd","MolPro");
			//Keys represent the end time of each phase in seconds
//			System.out.println("111111111"+interval.keySet());
			//All values in the TreeMap are stored as IntervalBehaviourClasses
			System.out.println(interval.keySet());
		
			
			interval=mlp.getBehaviour("232w4",1,100,"hdd","MolPro");
			//Keys represent the end time of each phase in seconds
//			System.out.println("222222222222"+interval.keySet());
			
			//All values in the TreeMap are stored as IntervalBehaviourClasses
			System.out.println(interval.keySet());
			interval=mlp.getBehaviour("232w4",1,100,"hdd","MolPro");
			//Keys represent the end time of each phase in seconds
//			System.out.println("3333333333"+interval.keySet());
			//All values in the TreeMap are stored as IntervalBehaviourClasses
			System.out.println(interval.keySet());
//			System.out.println("Get total length: ");


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
