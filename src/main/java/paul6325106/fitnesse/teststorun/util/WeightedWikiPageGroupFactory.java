package paul6325106.fitnesse.teststorun.util;

import fitnesse.wiki.WikiPage;
import paul6325106.fitnesse.teststorun.domain.SuiteSetUpTearDownPair;
import paul6325106.fitnesse.teststorun.domain.WeightedWikiPage;
import paul6325106.fitnesse.teststorun.domain.WeightedWikiPageGroup;
import paul6325106.fitnesse.teststorun.exception.TestsToRunContextGeneratorException;
import paul6325106.fitnesse.teststorun.weight.WeightStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeightedWikiPageGroupFactory {

    private final WeightStrategy weightStrategy;

    public WeightedWikiPageGroupFactory(final WeightStrategy weightStrategy) {
        this.weightStrategy = weightStrategy;
    }

    public List<WeightedWikiPageGroup> build(final Map<SuiteSetUpTearDownPair, List<WikiPage>> map)
            throws TestsToRunContextGeneratorException {

        final List<WeightedWikiPageGroup> groups = new ArrayList<>();

        for (Map.Entry<SuiteSetUpTearDownPair, List<WikiPage>> entry : map.entrySet()) {
            groups.add(build(entry.getKey(), entry.getValue()));
        }

        return groups;
    }

    private WeightedWikiPageGroup build(final SuiteSetUpTearDownPair pair, final List<WikiPage> pages)
            throws TestsToRunContextGeneratorException {

        final long suiteSetUpWeight = getWeight(pair.getSuiteSetUpPath());
        final long suiteTearDownWeight = getWeight(pair.getSuiteTearDownPath());

        final List<WeightedWikiPage> weightedPages = new ArrayList<>(pages.size());
        long pagesWeight = 0;

        for (final WikiPage page : pages) {
            final long pageWeight = getWeight(page);
            weightedPages.add(new WeightedWikiPage(page, pageWeight));
            pagesWeight += pageWeight;
        }

        return new WeightedWikiPageGroup(pair, weightedPages, suiteSetUpWeight, suiteTearDownWeight, pagesWeight);
    }

    private long getWeight(final String pathName) throws TestsToRunContextGeneratorException {
        return weightStrategy.getWeight(pathName);
    }

    private long getWeight(final WikiPage page) throws TestsToRunContextGeneratorException {
        return weightStrategy.getWeight(WikiPageUtil.getFullPathName(page));
    }

}
