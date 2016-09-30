package eu.cactosfp7.cactoopt.framework.model;

import java.util.List;

import com.google.common.collect.ImmutableList;

import eu.cactosfp7.cactoopt.framework.PlacementMapping;
import eu.cactosfp7.cactoopt.framework.util.PlacementMappingUtils;
import eu.cactosfp7.cactoopt.framework.model.MigrationMove;
import eu.cactosfp7.cactoopt.framework.model.MigrationPath;

/**
 * A model of a migration path, i.e., one that has an initial mapping, a
 * resulting mapping, and a list of the moves that were required to get there.
 *
 * <p>
 * This class is immutable.
 */
public class MigrationPath {
	private final PlacementMapping resultMapping;
	private final PlacementMapping initialMapping;
	private final ImmutableList<MigrationMove> migrationMoves;

	/**
	 * Creates a new instance.
	 *
	 * @param initialMapping
	 *            The initial mapping.
	 * @param resultMapping
	 *            The resulting mapping, after the migration moves are applied.
	 * @param migrationMoves
	 *            The migration moves to apply to transition the initial mapping
	 *            to the resulting mapping.
	 */
	public MigrationPath(PlacementMapping initialMapping,
			PlacementMapping resultMapping, List<MigrationMove> migrationMoves) {
		this.initialMapping = initialMapping;
		this.resultMapping = resultMapping;
		this.migrationMoves = ImmutableList.copyOf(migrationMoves);
	}

	/**
	 * @return The resulting mapping, i.e., the one that applying the migration
	 *         moves to the initial mapping results in.
	 */
	public PlacementMapping getResultMapping() {
		return this.resultMapping;
	}

	/**
	 * @return The initial mapping.
	 */
	public PlacementMapping getInitialMapping() {
		return this.initialMapping;
	}

	/**
	 * @return An immutable list of migration moves.
	 */
	public List<MigrationMove> getMigrationMoves() {
		return this.migrationMoves;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MigrationPath [resultMapping="
				+ PlacementMappingUtils.representMapping(this.resultMapping)
				+ ", initialMapping=" + this.initialMapping
				+ ", migrationMoves=" + this.migrationMoves + "]";
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
		result = prime
				* result
				+ (this.initialMapping == null ? 0 : this.initialMapping
						.hashCode());
		result = prime
				* result
				+ (this.migrationMoves == null ? 0 : this.migrationMoves
						.hashCode());
		result = prime
				* result
				+ (this.resultMapping == null ? 0 : this.resultMapping
						.hashCode());
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
		MigrationPath other = (MigrationPath) obj;
		if (this.initialMapping == null) {
			if (other.initialMapping != null) {
				return false;
			}
		} else if (!this.initialMapping.equals(other.initialMapping)) {
			return false;
		}
		if (this.migrationMoves == null) {
			if (other.migrationMoves != null) {
				return false;
			}
		} else if (!this.migrationMoves.equals(other.migrationMoves)) {
			return false;
		}
		if (this.resultMapping == null) {
			if (other.resultMapping != null) {
				return false;
			}
		} else if (!this.resultMapping.equals(other.resultMapping)) {
			return false;
		}
		return true;
	}

}
