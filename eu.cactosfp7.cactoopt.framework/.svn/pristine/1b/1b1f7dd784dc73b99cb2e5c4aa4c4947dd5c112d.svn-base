package eu.cactosfp7.cactoopt.combinatorics;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.cactosfp7.cactoopt.framework.combinatorics.Subsets;

/**
 * Tests that exercise {@link Subsets}.
 */
public class TestSubsets {

	@Test
	public void onEmptySet() {
		List<String> set = Lists.newArrayList();
		List<String> emptySet = Collections.emptyList();
		Collection<List<String>> empty = Arrays.asList(emptySet);
		assertThat(Subsets.kSubsets(set, 0), is(empty));
	}

	@Test
	public void onSingletonSet() {
		List<String> set = Lists.newArrayList(Arrays.asList("a"));

		List<String> emptySet = Collections.emptyList();
		Collection<List<String>> empty = Arrays.asList(emptySet);
		assertThat(Subsets.kSubsets(set, 0), is(empty));

		assertThat(Subsets.kSubsets(set, 1), is(asList(set("a"))));
	}

	@Test
	public void onTwoElementSet() {
		Set<String> set = Sets.newTreeSet(Arrays.asList("a", "b"));

		List<String> emptySet = Collections.emptyList();
		Collection<List<String>> empty = Arrays.asList(emptySet);
		assertThat(Subsets.kSubsets(set, 0), is(empty));

		assertThat(Subsets.kSubsets(set, 1), is(asList(set("a"), set("b"))));

		assertThat(Subsets.kSubsets(set, 2), is(asList(set("a", "b"))));
	}

	@Test
	public void onMultiElementSet() {
		Set<String> set = Sets.newTreeSet(Arrays.asList("a", "b", "c", "d"));

		List<String> emptySet = Collections.emptyList();
		Collection<List<String>> empty = Arrays.asList(emptySet);
		assertThat(Subsets.kSubsets(set, 0), is(empty));

		assertThat(Subsets.kSubsets(set, 1),
				is(asList(set("a"), set("b"), set("c"), set("d"))));

		assertThat(
				Subsets.kSubsets(set, 2),
				is(asList(set("a", "b"), set("a", "c"), set("a", "d"),
						set("b", "c"), set("b", "d"), set("c", "d"))));

		assertThat(
				Subsets.kSubsets(set, 3),
				is(asList(set("a", "b", "c"), set("a", "b", "d"),
						set("a", "c", "d"), set("b", "c", "d"))));

		assertThat(Subsets.kSubsets(set, 4),
				is(asList(set("a", "b", "c", "d"))));

	}

	@Test(expected = IllegalArgumentException.class)
	public void onTooLargeK() {
		Set<String> set = Sets.newTreeSet(Arrays.asList("a", "b", "c", "d"));
		Subsets.kSubsets(set, 5);
	}

	private List<String> set(String... values) {
		return new ArrayList<>(Arrays.asList(values));
	}
}
