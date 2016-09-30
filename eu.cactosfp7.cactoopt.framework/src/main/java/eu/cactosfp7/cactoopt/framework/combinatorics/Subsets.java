package eu.cactosfp7.cactoopt.framework.combinatorics;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.concat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * Utility methods that operate on/produce subsets of sets.
 */
public class Subsets {

	/**
	 * Return all subsets of size k from a given set in lexicographical order.
	 *
	 * @param set
	 *            A set of elements.
	 * @param k
	 *            The size of the subsets to generate.
	 * @return Returns a list of all subsets with the specified number of
	 *         elements. All elements of the returned list are lists of size k.
	 */
	public static <T> List<List<T>> kSubsets(Collection<T> set, int k) {
		checkArgument(set.size() >= k,
				"asked for subset of size %s, but set is only of size %s", k,
				set.size());

		List<T> setMembers = Lists.newArrayList(set);
		List<T> subsetMembers = Lists.newArrayList();
		return subsets(setMembers, subsetMembers, k, 0);
	}

	/**
	 * Helper method for the {@link #kSubsets(Set, int)} method, which
	 * recursively produces all subsets of a given size. The
	 *
	 * @param originalSet
	 *            The original set members.
	 * @param subsetMembers
	 *            The members that have been added to the subset in this
	 *            recursion branch. This needs to be an empty list on the first
	 *            call to the method.
	 * @param subsetSize
	 *            The remaining elements that are to be added to
	 *            {@code subsetMembers} before we have gathered a subset of the
	 *            right size.
	 * @param elementIndex
	 *            The current element index of {@code originalSet} being
	 *            considered in this recursion branch.
	 * @return The list of all subsets of {@code originalSet} that are of size
	 *         {@code subsetSize}.
	 */
	private static <T> List<List<T>> subsets(List<T> originalSet,
			List<T> subsetMembers, int subsetSize, int elementIndex) {
		// we have a subset of the right size, add to collection and break
		// recursion
		if (subsetSize <= 0) {
			List<List<T>> singletonList = Arrays.asList(subsetMembers);
			return singletonList;
		}
		// we have reached the end of the set elements without being able to
		// gather K elements. this was an unsuccessful recursion branch.
		if (elementIndex >= originalSet.size()) {
			return Collections.emptyList();
		}

		// gather all subsets where current element is included
		List<T> elementIncluded = Lists.newArrayList(subsetMembers);
		elementIncluded.add(originalSet.get(elementIndex));
		List<List<T>> subsetsWithElementIncluded = subsets(originalSet,
				elementIncluded, subsetSize - 1, elementIndex + 1);

		// gather all subsets where current element is not included
		List<T> elementExcluded = Lists.newArrayList(subsetMembers);
		List<List<T>> subsetsWithElementExcluded = subsets(originalSet,
				elementExcluded, subsetSize, elementIndex + 1);

		return Lists.newArrayList(concat(subsetsWithElementIncluded,
				subsetsWithElementExcluded));
	}

}
