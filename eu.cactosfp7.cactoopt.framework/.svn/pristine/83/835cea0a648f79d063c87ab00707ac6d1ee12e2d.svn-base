package eu.cactosfp7.cactoopt.framework.model;

import com.google.common.base.Preconditions;

import eu.cactosfp7.cactoopt.framework.model.Migration;
import eu.cactosfp7.cactoopt.framework.model.PhysicalMachine;

/**
 * A model of a migration from one {@link PhysicalMachine} to another.
 *
 * <p>
 * This class is immutable.
 */
public final class Migration {
	/**
	 * The source of the migration.
	 */
	private final PhysicalMachine source;

	/**
	 * The destination of the migration.
	 */
	private final PhysicalMachine destination;

	/**
	 * Creates a new instance.
	 *
	 * @param source
	 *            The source physical machine.
	 * @param destination
	 *            The destination physical machine.
	 */
	public Migration(PhysicalMachine source, PhysicalMachine destination) {
		Preconditions.checkArgument(!source.equals(destination),
				"Source cannot equal destination");

		this.source = source;
		this.destination = destination;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (this.destination == null ? 0 : this.destination.hashCode());
		result = prime * result
				+ (this.source == null ? 0 : this.source.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Migration other = (Migration) obj;
		if (this.destination == null) {
			if (other.destination != null) {
				return false;
			}
		} else if (!this.destination.equals(other.destination)) {
			return false;
		}
		if (this.source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!this.source.equals(other.source)) {
			return false;
		}
		return true;
	}

	/**
	 * @return The source {@link PhysicalMachine}.
	 */
	public PhysicalMachine getSource() {
		return this.source;
	}

	/**
	 * @return The destination {@link PhysicalMachine}.
	 */
	public PhysicalMachine getDestination() {
		return this.destination;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.source.getId() + " -> " + this.destination.getId();
	}

}
