package paul6325106.fitnesse.teststorun.util;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class WikiPageUtil {

    /**
     * Gets the full path name of a WikiPage, or null if the WikiPage is null.
     * @param wikiPage WikiPage to read.
     * @return String describing full path name or null.
     */
    public static String getFullPathName(final WikiPage wikiPage) {
        return wikiPage == null ? null : wikiPage.getPageCrawler().getFullPath().toString();
    }

    /**
     * Similar to fitnesse.wiki.WikiPageUtil#addPage, except the path can be a String and page type can be overridden.
     * @see fitnesse.wiki.WikiPageUtil#addPage(WikiPage, WikiPagePath, String)
     * @param type PageType to set to added page.
     * @return created WikiPage instance.
     */
    public static WikiPage addPage(final WikiPage parent, final String name, final String content,
            final PageType type) {

        final WikiPage child = fitnesse.wiki.WikiPageUtil.addPage(parent, PathParser.parse(name), content);
        final PageData data = child.getData();
        data.setAttribute(type.toString());
        child.commit(data);
        return child;
    }

    /**
     * Returns true if the WikiPage is of the specified PageType.
     * Includes a check for the older method of setting PageTypes.
     * @param page WikiPage to check.
     * @param type PageType to match.
     * @return true if the WikiPage is of the specified PageType, false otherwise.
     */
    public static boolean isPageType(final WikiPage page, final PageType type) {
        final PageData data = page.getData();
        return data.hasAttribute(type.toString()) || data.hasAttribute(PageData.PAGE_TYPE_ATTRIBUTE)
                && data.getAttribute(PageData.PAGE_TYPE_ATTRIBUTE).equals(type.toString());
    }

}
