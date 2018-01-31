package paul6325106.fitnesse.teststorun.domain;

import java.util.List;

public class WeightedWikiPageGroup {
    private final SuiteSetUpTearDownPair suiteSetUpTearDownPair;
    private final List<WeightedWikiPage> wikiPages;
    private final long setUpWeight;
    private final long tearDownWeight;
    private final long pagesWeight;
    private final long totalWeight;

    public WeightedWikiPageGroup(final SuiteSetUpTearDownPair suiteSetUpTearDownPair,
            final List<WeightedWikiPage> wikiPages, final long setUpWeight, final long tearDownWeight,
            final long pagesWeight) {

        this.suiteSetUpTearDownPair = suiteSetUpTearDownPair;
        this.wikiPages = wikiPages;
        this.setUpWeight = setUpWeight;
        this.tearDownWeight = tearDownWeight;
        this.pagesWeight = pagesWeight;
        this.totalWeight = setUpWeight + tearDownWeight + pagesWeight;
    }

    public SuiteSetUpTearDownPair getSuiteSetUpTearDownPair() {
        return suiteSetUpTearDownPair;
    }

    public List<WeightedWikiPage> getWikiPages() {
        return wikiPages;
    }

    public long getSetUpWeight() {
        return setUpWeight;
    }

    public long getTearDownWeight() {
        return tearDownWeight;
    }

    public long getPagesWeight() {
        return pagesWeight;
    }

    public long getTotalWeight() {
        return totalWeight;
    }

    @Override
    public String toString() {
        return "WeightedWikiPageGroup{" +
                "suiteSetUpTearDownPair=" + suiteSetUpTearDownPair +
                ", wikiPages=" + wikiPages +
                ", setUpWeight=" + setUpWeight +
                ", tearDownWeight=" + tearDownWeight +
                ", pagesWeight=" + pagesWeight +
                ", totalWeight=" + totalWeight +
                '}';
    }

}
