package eu.cactosfp7.cactoopt.behaviourinference;

/**
 * Encapsulates simple behaviour characteristics of a VM.
 * @author stier, groenda
 *
 */
public class SimpleVMBehaviour {
	/** The CPU demand issued in the interval. The unit is busy CPU cycles. */
	private double cpuResourceDemand;
	/** The amount of bytes requested to be read in the interval. Request does not have to be completed within the interval. */
	private double bytesRead;
	/** The amount of bytes requested to be written in the interval. Request does not have to be completed within the interval. */
	private double bytesWritten;
	
	
	
	/**
	 * Creates an instance representation of a simple VM behaviour for a certain interval.
	 * @param cpuResourceDemand The CPU demand issued in the interval. The unit is busy CPU cycles.
	 * @param bytesRead The amount of bytes requested to be read in the interval. Request does not have to be completed within the interval.
	 * @param bytesWritten The amount of bytes requested to be written in the interval. Request does not have to be completed within the interval.
	 */
	public SimpleVMBehaviour(double cpuResourceDemand, double bytesRead, double bytesWritten) {
		this.cpuResourceDemand = cpuResourceDemand;
		this.bytesRead = bytesRead;
		this.bytesWritten = bytesWritten;
	}
	
	/**
	 * The CPU demand issued in the interval. The unit is busy CPU cycles.
	 * @return The CPU demand issued in the interval.
	 */
	public double getCpuResourceDemand() {
		return cpuResourceDemand;
	}
	
	/**
	 * The amount of bytes requested to be read in the interval. Request does not have to be completed within the interval.
	 * @return The amount of bytes requested to be read in the interval.
	 */
	public double getBytesRead() {
		return bytesRead;
	}
	
	/**
	 * Get The amount of bytes requested to be written in the interval. Request does not have to be completed within the interval.
	 * @return The amount of bytes requested to be written in the interval.
	 */
	public double getBytesWritten() {
		return bytesWritten;
	}
}
