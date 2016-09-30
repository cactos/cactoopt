package eu.cactosfp7.cactoopt.behaviourinference;

import java.util.logging.Logger;

/**
 * Encapsulates simple behaviour characteristics of a VM.
 * @author stier, groenda
 *
 */
public class IntervalBehaviourValues {
	/** The interval length. The unit is seconds. */
	private double intervalLength=0;
	/** Total interval length. The unit is seconds. */
	private double TotalTime=0;
	/** The CPU demand issued in the interval. The unit is busy CPU cycles. */
	private double cpuResourceDemand=0;
	/** The amount of bytes requested to be read in the interval. Request does not have to be completed within the interval. */
	private double bytesRead=0;
	/** The amount of bytes requested to be written in the interval. Request does not have to be completed within the interval. */
	private double bytesWritten=0;
	
	private String modelName=null;
	private static final Logger log = Logger.getLogger(IntervalBehaviourValues.class.getName());
	
	public IntervalBehaviourValues(String name) {
//		log.info("Constructing Interval model:  "+name);
	}
	
//	/**
//	 * Creates an instance representation of a simple VM behaviour for a certain interval.
//	 * @param cpuResourceDemand The CPU demand issued in the interval. The unit is busy CPU cycles.
//	 * @param bytesRead The amount of bytes requested to be read in the interval. Request does not have to be completed within the interval.
//	 * @param bytesWritten The amount of bytes requested to be written in the interval. Request does not have to be completed within the interval.
//	 */
//	public IntervalBehaviourValues(double intervalLength,double cpuResourceDemand, double bytesRead, double bytesWritten) {
//		this.cpuResourceDemand = cpuResourceDemand;
//		this.bytesRead = bytesRead;
//		this.bytesWritten = bytesWritten;
//	}
	
	/**
	 * The length of the interval. The unit is seconds.
	 */
	public double getInterval() {
		return this.intervalLength;
	}
	
	/**
	 * The CPU demand issued in the interval. The unit is busy CPU cycles.
	 * @return The CPU demand issued in the interval.
	 */
	public double getCpuResourceDemand() {
		return this.cpuResourceDemand;
	}
	
	/**
	 * The amount of bytes requested to be read per second. Request does not have to be completed within the interval.
	 * @return The amount of bytes requested to be read in the interval.
	 */
	public double getBytesRead() {
		return this.bytesRead;
	}
	
	/**
	 * set The amount of bytes requested to be written per second. 
	 */
	public double getBytesWritten() {
		return this.bytesWritten;
	}
	
	public String getModelName() {
		return this.modelName;
	}
	public double getTotalTime() {
		return this.TotalTime;
	}
	/**
	 * The length of the interval. The unit is seconds.
	 */
	public void setInterval(double inetrvalLen, String name) {
		this.intervalLength=inetrvalLen*15;
//		log.info("IntervalLen   "+inetrvalLen+"    "+this.intervalLength);	
		}
	
	/**
	 * Total interval. The unit is seconds.
	 */
	public void setTotTime(double inetrvalLen) {
		this.TotalTime=inetrvalLen*15;
//		log.info("---------------------------------------------------------------------------------------Total Run Length"+this.TotalTime);	
		}
	/**
	 * Set the CPU demand issued in the interval. The unit is busy CPU cycles.
	 */
	public void setCpuResourceDemand(double cpu) {
		this.cpuResourceDemand=cpu*26000000;
	}
	
	/**
	 * The amount of bytes requested to be read per second. Request does not have to be completed within the interval.
	 * @return The amount of bytes requested to be read in the interval.
	 */
	public void setBytesRead(double re) {
		this.bytesRead=re;
	}
	
	/**
	 * Get The amount of bytes requested to be written per second. Request does not have to be completed within the interval.
	 * @return The amount of bytes requested to be written in the interval.
	 */
	public void setBytesWritten(double wr) {
		this.bytesWritten=wr;
	}
	
	public void setmodelName(String mn) {
		this.modelName=mn;
	}
}
