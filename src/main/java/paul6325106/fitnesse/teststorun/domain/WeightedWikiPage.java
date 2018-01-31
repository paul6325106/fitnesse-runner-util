package paul6325106.fitnesse.teststorun.domain;

import fitnesse.wiki.WikiPage;

public class WeightedWikiPage {
    private final WikiPage wikiPage;
    private final long weight;

    public WeightedWikiPage(final WikiPage wikiPage, final long weight) {
        this.wikiPage = wikiPage;
        this.weight = weight;
    }

    public WikiPage getWikiPage() {
        return wikiPage;
    }

    public long getWeight() {
        return weight;
    }
}
