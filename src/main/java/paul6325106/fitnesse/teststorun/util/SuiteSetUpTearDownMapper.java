package paul6325106.fitnesse.teststorun.util;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import paul6325106.fitnesse.teststorun.domain.SuiteSetUpTearDownPair;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static paul6325106.fitnesse.teststorun.util.WikiPageUtil.getFullPathName;

/**
 * Maps WikiPages to their SuiteSetUp/SuiteTearDown pairs.
 */
public class SuiteSetUpTearDownMapper {

    /**
     * Gets a mapping of the SuiteSetUp and SuiteTearDown used by WikiPages when executed.
     * A SuiteSetUp and/or SuiteTearDown may be null if no such page is available to a WikiPage.
     * @param wikiPages WikiPages to map.
     * @return Map of SuiteSetUp/SuiteTearDown pairs to WikiPages.
     */
    public Map<SuiteSetUpTearDownPair, List<WikiPage>> getSuiteSetUpTearDownMap(final Collection<WikiPage> wikiPages) {
        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map = new HashMap<>();

        for (final WikiPage wikiPage : getUnique(wikiPages)) {
            final SuiteSetUpTearDownPair pair = getSuiteSetUpTearDownPair(wikiPage);

            if (map.containsKey(pair)) {
                map.get(pair).add(wikiPage);
            } else {
                final List<WikiPage> list = new LinkedList<>();
                map.put(pair, list);
                list.add(wikiPage);
            }
        }

        return map;
    }

    private Set<WikiPage> getUnique(final Collection<WikiPage> wikiPages) {
        final Set<WikiPage> unique = new TreeSet<>();
        unique.addAll(wikiPages);
        return unique;
    }

    private SuiteSetUpTearDownPair getSuiteSetUpTearDownPair(final WikiPage wikiPage) {
        final PageCrawler pageCrawler = wikiPage.getPageCrawler();
        final WikiPage suiteSetUp = pageCrawler.getClosestInheritedPage(PageData.SUITE_SETUP_NAME);
        final WikiPage suiteTearDown = pageCrawler.getClosestInheritedPage(PageData.SUITE_TEARDOWN_NAME);
        return new SuiteSetUpTearDownPair(getFullPathName(suiteSetUp), getFullPathName(suiteTearDown));
    }

}
