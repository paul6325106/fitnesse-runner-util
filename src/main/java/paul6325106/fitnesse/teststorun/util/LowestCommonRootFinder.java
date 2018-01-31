package paul6325106.fitnesse.teststorun.util;

import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageType;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import paul6325106.fitnesse.teststorun.exception.TestsToRunContextGeneratorException;
import paul6325106.fitnesse.teststorun.exception.TestsToRunContextGeneratorReason;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Determines the lowest common root page for a collection of WikiPages.
 * Includes an optional check for non-Suite page root (if desired for correctness).
 * Intended for programmatically determining the "requested page" for history formatting purposes.
 */
public class LowestCommonRootFinder {

    private final PageCrawler pageCrawler;

    public LowestCommonRootFinder(final WikiPage root) {
        this.pageCrawler = root.getPageCrawler();
    }

    /**
     * Gets the lowest ancestor WikiPage common to all WikiPages in a Collection.
     * @param pages WikiPages with at least one common ancestor WikiPage.
     * @param enforceSuite Will only consider Suite pages as roots if true.
     * @return WikiPage identified as lowest common root.
     * @throws TestsToRunContextGeneratorException if unable to find a suitable root page.
     */
    public WikiPage getLowestCommonRoot(final Collection<WikiPage> pages, final boolean enforceSuite)
            throws TestsToRunContextGeneratorException {

        if (pages.isEmpty()) {
            return null;
        }

        final List<String> paths = getPaths(pages);

        String lowestCommonRootPath = paths.remove(0);

        for (final String path : paths) {
            lowestCommonRootPath = getLowestCommonRootPath(lowestCommonRootPath, path);
        }

        WikiPage lowestCommonRoot = getPage(lowestCommonRootPath);

        if (lowestCommonRoot == null) {
            throw new TestsToRunContextGeneratorException(String.format(
                    "Unable to find common root on pathName %s for pages %s: ", lowestCommonRootPath, pages),
                    TestsToRunContextGeneratorReason.UNABLE_TO_FIND_COMMON_ROOT);
        }

        if (enforceSuite) {
            lowestCommonRoot = getLowestSuitePageAncestor(lowestCommonRoot);

            if (lowestCommonRoot == null) {
                throw new TestsToRunContextGeneratorException("Unable to find Suite page as common root: " + pages,
                        TestsToRunContextGeneratorReason.UNABLE_TO_FIND_SUITE_PAGE_AS_COMMON_ROOT);

            }
        }

        return lowestCommonRoot;
    }

    private List<String> getPaths(final Collection<WikiPage> pages) {
        return pages.stream().map(this::getFullPathString).collect(Collectors.toList());
    }

    private String getFullPathString(final WikiPage page) {
        return page.getPageCrawler().getFullPath().toString();
    }

    private String getLowestCommonRootPath(final String path1, final String path2) {
        final String[] words1 = path1.split("\\.");
        final String[] words2 = path2.split("\\.");

        final int min = Math.min(words1.length, words2.length);

        final List<String> matched = new ArrayList<>(min);

        for (int i = 0; i < min; ++i) {
            if (words1[i].equals(words2[i])) {
                matched.add(words1[i]);
            } else {
                break;
            }
        }

        // if no words are matched, then the join will result in an empty String
        // this is a valid path, pointing at absolute root

        return String.join(".", matched);
    }

    private WikiPage getPage(final String path) {
        return pageCrawler.getPage(PathParser.parse(path));
    }

    private WikiPage getLowestSuitePageAncestor(final WikiPage page) {
        if (isSuitePage(page)) {
            return page;
        } else if (page.isRoot()) {
            return null;
        } else {
            return getLowestSuitePageAncestor(page.getParent());
        }
    }

    private boolean isSuitePage(final WikiPage page) {
        return page.getData().hasAttribute(PageType.SUITE.toString());
    }

}
