package eu.cactosfp7.cactoopt.framework.model.exceptions;

import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;
import eu.cactosfp7.cactoopt.framework.model.exceptions.PlacementException;

/**
 * Thrown if a {@link PhysicalMachine} has too few CPU cores available to host a
 * given {@link VirtualMachine}.
 */
public class CoresExhaustedException extends PlacementException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2768664877295234035L;

	public CoresExhaustedException() {
		super();
	}

	public CoresExhaustedException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CoresExhaustedException(String message, Throwable cause) {
		super(message, cause);
	}

	public CoresExhaustedException(String message) {
		super(message);
	}

	public CoresExhaustedException(Throwable cause) {
		super(cause);
	}

}
