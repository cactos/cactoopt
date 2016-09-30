package eu.cactosfp7.cactoopt.framework.model.exceptions;

import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;
import eu.cactosfp7.cactoopt.framework.model.VirtualMachine;

/**
 * Thrown if there are any issues with placing a {@link VirtualMachine} on a
 * {@link PhysicalMachine}.
 */
public class PlacementException extends RuntimeException {
	private static final long serialVersionUID = 0xDEADC0DE;

	public PlacementException() {
		super();
	}

	public PlacementException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PlacementException(String message, Throwable cause) {
		super(message, cause);
	}

	public PlacementException(String message) {
		super(message);
	}

	public PlacementException(Throwable cause) {
		super(cause);
	}

}
