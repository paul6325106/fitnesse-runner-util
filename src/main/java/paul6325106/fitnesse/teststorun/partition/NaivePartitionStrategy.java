package paul6325106.fitnesse.teststorun.partition;

import paul6325106.fitnesse.teststorun.domain.WeightedWikiPage;
import paul6325106.fitnesse.teststorun.domain.WeightedWikiPageGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Sets which are considered 'large' will be split by the number of available executors. The intended use-case is
 * for a single large suite or a single large set of tests with a common SuiteSetUp and SuiteTearDown.
 */
public class NaivePartitionStrategy implements PartitionStrategy {

    private final long large;

    public NaivePartitionStrategy(final long large) {
        this.large = large;
    }

    @Override
    public Collection<WeightedWikiPageGroup> partition(final List<WeightedWikiPageGroup> groups, final int count) {
        groups.sort(Comparator.comparingLong(WeightedWikiPageGroup::getTotalWeight).reversed());

        for (final WeightedWikiPageGroup group : groups) {
            if (group.getTotalWeight() > large) {
                groups.remove(group);
                groups.addAll(split(group, count));
            }
        }

        return groups;
    }

    private Collection<WeightedWikiPageGroup> split(final WeightedWikiPageGroup group, final int count) {

        final int min = Math.min(count, group.getWikiPages().size());

        final List<List<WeightedWikiPage>> bins = new ArrayList<>(min);
        final List<WeightedWikiPage> pages = new ArrayList<>(group.getWikiPages());
        pages.sort(Comparator.comparingLong(WeightedWikiPage::getWeight).reversed());
        pages.forEach(page -> getSmallestBin(bins).add(page));

        final List<WeightedWikiPageGroup> split = new ArrayList<>(min);

        for (final List<WeightedWikiPage> bin : bins) {
            split.add(new WeightedWikiPageGroup(group.getSuiteSetUpTearDownPair(), bin, group.getSetUpWeight(),
                    group.getTearDownWeight(), getBinTotalWeight(bin)));
        }

        return split;
    }

    private long getBinTotalWeight(final List<WeightedWikiPage> pages) {
        // TODO memoise
        return pages.stream().mapToLong(WeightedWikiPage::getWeight).sum();
    }

    private List<WeightedWikiPage> getSmallestBin(final List<List<WeightedWikiPage>> bins) {
        return bins.stream().min(Comparator.comparingLong(this::getBinTotalWeight)).orElse(null);
    }

}
