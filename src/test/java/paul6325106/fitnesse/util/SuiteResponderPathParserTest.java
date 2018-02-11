package paul6325106.fitnesse.util;

import fitnesse.FitNesseContext;
import fitnesse.testrunner.SuiteFilter;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageType;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SuiteResponderPathParserTest {

    private WikiPage root;
    private SuiteResponderPathParser parser;

    @Before
    public void setUp() throws Exception {
        final FitNesseContext context = FitNesseUtil.makeTestContext();
        root = context.getRootPage();
        parser = new SuiteResponderPathParser(context);
    }

    @Test
    public void testEmpty() throws Exception {
        final WikiPage suitePage = WikiPageUtil.addPage(root, "SuitePage", "", PageType.SUITE);
        WikiPageUtil.addPage(suitePage, "TestPage", "", PageType.TEST);

        final String input = "";
        final WikiPage requestedPage = parser.getRequestedPage(input);
        final SuiteFilter suiteFilter = parser.getSuiteFilter(input);

        assertEquals(root, requestedPage);
        assertEquals("", suiteFilter.toString());
    }

    @Test
    public void testValidWikiPageWithoutFilter() throws Exception {
        final WikiPage suitePage = WikiPageUtil.addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage testPage = WikiPageUtil.addPage(suitePage, "TestPageOne", "", PageType.TEST);

        final String input = "SuitePage.TestPageOne";
        final WikiPage requestedPage = parser.getRequestedPage(input);
        final SuiteFilter suiteFilter = parser.getSuiteFilter(input);

        assertEquals(testPage, requestedPage);
        assertEquals("", suiteFilter.toString());
    }

    @Test
    public void testValidWikiPageWithFilter() throws Exception {
        final WikiPage suitePage = WikiPageUtil.addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage testPage = WikiPageUtil.addPage(suitePage, "TestPageOne", "", PageType.TEST);

        final String input = "SuitePage.TestPageOne" +
                "?excludeSuiteFilter=tagOne,tagTwo" +
                "&runTestsMatchingAllTags=tagThree";
        final WikiPage requestedPage = parser.getRequestedPage(input);
        final SuiteFilter suiteFilter = parser.getSuiteFilter(input);

        assertEquals(testPage, requestedPage);
        assertEquals("matches all of 'tagThree' & doesn't match 'tagOne,tagTwo'", suiteFilter.toString());
    }

    @Test
    public void testUnknownWikiPage() throws Exception {
        final WikiPage suitePage = WikiPageUtil.addPage(root, "SuitePage", "", PageType.SUITE);
        WikiPageUtil.addPage(suitePage, "TestPageOne", "", PageType.TEST);

        assertNull(parser.getRequestedPage("SuitePage.TestPageOne.WhatIsThis"));
    }

    @Test
    public void testFirstTestAbsolute() throws Exception {
        final WikiPage suitePage = WikiPageUtil.addPage(root, "SuitePage", "", PageType.SUITE);
        WikiPageUtil.addPage(suitePage, "TestPageOne", "", PageType.TEST);

        final String input = "SuitePage?firstTest=SuitePage.TestPageOne";
        final WikiPage requestedPage = parser.getRequestedPage(input);
        final SuiteFilter suiteFilter = parser.getSuiteFilter(input);

        assertEquals(suitePage, requestedPage);
        assertEquals("starts with test 'SuitePage.TestPageOne'", suiteFilter.toString());
    }

    @Test
    public void testFirstTestRelative() throws Exception {
        final WikiPage suitePage = WikiPageUtil.addPage(root, "SuitePage", "", PageType.SUITE);
        WikiPageUtil.addPage(suitePage, "TestPageOne", "", PageType.TEST);

        final String input = "SuitePage?firstTest=TestPageOne";
        final WikiPage requestedPage = parser.getRequestedPage(input);
        final SuiteFilter suiteFilter = parser.getSuiteFilter(input);

        assertEquals(suitePage, requestedPage);
        assertEquals("starts with test 'SuitePage.TestPageOne'", suiteFilter.toString());
    }

}
