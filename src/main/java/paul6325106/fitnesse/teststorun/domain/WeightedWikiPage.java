package paul6325106.fitnesse.teststorun.domain;

import fitnesse.wiki.WikiPage;

import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final WeightedWikiPage that = (WeightedWikiPage) o;
        return weight == that.weight && Objects.equals(wikiPage, that.wikiPage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wikiPage, weight);
    }

    @Override
    public String toString() {
        return "WeightedWikiPage{" +
                "wikiPage=" + wikiPage +
                ", weight=" + weight +
                '}';
    }

}
