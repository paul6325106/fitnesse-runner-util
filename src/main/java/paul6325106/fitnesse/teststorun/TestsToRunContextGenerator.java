package paul6325106.fitnesse.teststorun;

import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;
import paul6325106.fitnesse.teststorun.domain.SuiteSetUpTearDownPair;
import paul6325106.fitnesse.teststorun.domain.TestsToRunContext;
import paul6325106.fitnesse.teststorun.domain.WeightedWikiPage;
import paul6325106.fitnesse.teststorun.domain.WeightedWikiPageGroup;
import paul6325106.fitnesse.teststorun.exception.TestsToRunContextGeneratorException;
import paul6325106.fitnesse.teststorun.partition.PartitionStrategy;
import paul6325106.fitnesse.teststorun.util.LowestCommonRootFinder;
import paul6325106.fitnesse.teststorun.util.SuiteSetUpTearDownMapper;
import paul6325106.fitnesse.teststorun.util.WeightedWikiPageGroupFactory;
import paul6325106.fitnesse.teststorun.weight.WeightStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates an ordered list of TestsToRunContexts from a collection of WikiPages instances.
 */
public class TestsToRunContextGenerator {

    private final SuiteSetUpTearDownMapper suiteSetUpTearDownMapper;
    private final WeightedWikiPageGroupFactory weightedWikiPageGroupFactory;
    private final LowestCommonRootFinder lowestCommonRootFinder;
    private final PartitionStrategy partitionStrategy;
    private final boolean enforceSuiteRoot;

    public TestsToRunContextGenerator(final FitNesseContext context, final WeightStrategy weightStrategy,
            final PartitionStrategy partitionStrategy, final boolean enforceSuiteRoot) {

        this.suiteSetUpTearDownMapper = new SuiteSetUpTearDownMapper();
        this.lowestCommonRootFinder = new LowestCommonRootFinder(context.getRootPage());
        this.weightedWikiPageGroupFactory = new WeightedWikiPageGroupFactory(weightStrategy);
        this.partitionStrategy = partitionStrategy;
        this.enforceSuiteRoot = enforceSuiteRoot;
    }

    /**
     * Generates an ordered list of TestsToRunContexts from a collection of WikiPages instances. The ordering of the
     * list and the contents of each TestToRunContext is dependent on the strategies provided. TestsToRunContexts have
     * the lowest common root set as the requested page for history formatting purposes.
     * @param pages Pages to partition.
     * @param count Number of sets to create when splitting.
     * @return List of TestsToRunContexts describing original collection of pages, in descending order of total weight.
     * @throws TestsToRunContextGeneratorException when unable to create partitions due to suite structure issues or
     *                                             broken FitNesse interface implementations.
     */
    public List<TestsToRunContext> generate(final Collection<WikiPage> pages, final int count)
            throws TestsToRunContextGeneratorException {

        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map =
                suiteSetUpTearDownMapper.getSuiteSetUpTearDownMap(pages);
        final List<WeightedWikiPageGroup> groups = weightedWikiPageGroupFactory.build(map);
        final Collection<WeightedWikiPageGroup> split = partitionStrategy.partition(groups, count);
        return build(split);
    }

    private List<TestsToRunContext> build(final Collection<WeightedWikiPageGroup> groups)
            throws TestsToRunContextGeneratorException {

        final List<TestsToRunContext> testsToRunContexts = new ArrayList<>();
        for (WeightedWikiPageGroup group : groups) {
            testsToRunContexts.add(build(group));
        }
        return testsToRunContexts;
    }

    private TestsToRunContext build(final WeightedWikiPageGroup group) throws TestsToRunContextGeneratorException {
        final List<WikiPage> pages = group.getWikiPages().stream()
                .map(WeightedWikiPage::getWikiPage)
                .collect(Collectors.toList());

        final WikiPage lowestCommonRoot = lowestCommonRootFinder.getLowestCommonRoot(pages, enforceSuiteRoot);

        return new TestsToRunContext(lowestCommonRoot, pages);
    }

}
