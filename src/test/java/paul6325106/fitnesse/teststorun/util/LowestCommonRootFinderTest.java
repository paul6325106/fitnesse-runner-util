package paul6325106.fitnesse.teststorun.util;

import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageType;
import fitnesse.wiki.WikiPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import paul6325106.fitnesse.teststorun.exception.TestsToRunContextGeneratorException;
import paul6325106.fitnesse.teststorun.exception.TestsToRunContextGeneratorReason;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LowestCommonRootFinderTest {

    private LowestCommonRootFinder finder;

    private WikiPage root;

    @Before
    public void setUp() throws Exception {
        root = FitNesseUtil.makeTestContext().getRootPage();
        finder = new LowestCommonRootFinder(root);
    }

    @Test
    public void testPagesEmpty() throws Exception {
        assertEquals(null, finder.getLowestCommonRoot(Collections.emptyList(), false));
    }

    @Test
    public void testPagesOne() throws Exception {
        final WikiPage testPage = WikiPageUtil.addPage(root, "TestPage", "", PageType.TEST);

        assertEquals(testPage, finder.getLowestCommonRoot(Collections.singletonList(testPage), false));
    }

    @Test
    public void testBasicSuite() throws Exception {
        final WikiPage suitePage = WikiPageUtil.addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage testPageOne = WikiPageUtil.addPage(suitePage, "TestPageOne", "", PageType.TEST);
        final WikiPage testPageTwo = WikiPageUtil.addPage(suitePage, "TestPageTwo", "", PageType.TEST);

        assertEquals(testPageOne, finder.getLowestCommonRoot(Collections.singletonList(testPageOne), false));
        assertEquals(testPageTwo, finder.getLowestCommonRoot(Collections.singletonList(testPageTwo), false));
        assertEquals(suitePage, finder.getLowestCommonRoot(Collections.singletonList(testPageOne), true));
        assertEquals(suitePage, finder.getLowestCommonRoot(Collections.singletonList(testPageTwo), true));
        assertEquals(suitePage, finder.getLowestCommonRoot(Arrays.asList(testPageOne, testPageTwo), false));
    }

    @Test
    public void testNestedSuites() throws Exception {
        final WikiPage suitePageOne = WikiPageUtil.addPage(root, "SuitePageOne", "", PageType.SUITE);
        final WikiPage suitePageTwo = WikiPageUtil.addPage(suitePageOne, "SuitePageTwp", "", PageType.SUITE);
        final WikiPage suitePageThree = WikiPageUtil.addPage(suitePageOne, "SuitePageThree", "", PageType.SUITE);
        final WikiPage testPageOne = WikiPageUtil.addPage(suitePageTwo, "TestPageOne", "", PageType.TEST);
        final WikiPage testPageTwo = WikiPageUtil.addPage(suitePageThree, "TestPageTwo", "", PageType.TEST);

        assertEquals(suitePageOne, finder.getLowestCommonRoot(Arrays.asList(testPageOne, testPageTwo), false));
        assertEquals(suitePageTwo, finder.getLowestCommonRoot(Collections.singletonList(testPageOne), true));
        assertEquals(suitePageThree, finder.getLowestCommonRoot(Collections.singletonList(testPageTwo), true));
    }

    @Test
    public void testStaticRootUnderSuiteRoot() throws Exception {
        final WikiPage suitePage = WikiPageUtil.addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage staticPage = WikiPageUtil.addPage(suitePage, "StaticPage", "", PageType.STATIC);
        final WikiPage testPageOne = WikiPageUtil.addPage(staticPage, "TestPageOne", "", PageType.TEST);
        final WikiPage testPageTwo = WikiPageUtil.addPage(staticPage, "TestPageTwo", "", PageType.TEST);

        assertEquals(suitePage, finder.getLowestCommonRoot(Arrays.asList(testPageOne, testPageTwo), true));
        assertEquals(staticPage, finder.getLowestCommonRoot(Arrays.asList(testPageOne, testPageTwo), false));
    }

    @Test
    public void testUnableToEnforceSuite() throws Exception {
        final WikiPage staticPage = WikiPageUtil.addPage(root, "StaticPage", "", PageType.STATIC);
        final WikiPage testPageOne = WikiPageUtil.addPage(staticPage, "TestPageOne", "", PageType.TEST);
        final WikiPage testPageTwo = WikiPageUtil.addPage(staticPage, "TestPageTwo", "", PageType.TEST);

        assertEquals(staticPage, finder.getLowestCommonRoot(Arrays.asList(testPageOne, testPageTwo), false));

        try {
            finder.getLowestCommonRoot(Arrays.asList(testPageOne, testPageTwo), true);
            fail();
        } catch (final TestsToRunContextGeneratorException e) {
            Assert.assertEquals(TestsToRunContextGeneratorReason.UNABLE_TO_FIND_SUITE_PAGE_AS_COMMON_ROOT, e.getReason());
        }
    }

    @Test
    public void testContextRootAsCommonRoot() throws Exception {
        final WikiPage testPageOne = WikiPageUtil.addPage(root, "TestPageOne", "", PageType.TEST);
        final WikiPage testPageTwo = WikiPageUtil.addPage(root, "TestPageTwo", "", PageType.TEST);

        assertEquals(root, finder.getLowestCommonRoot(Arrays.asList(testPageOne, testPageTwo), false));
    }

    @Test
    public void testRootIsNotContextRoot() throws Exception {
        final WikiPage suitePage = WikiPageUtil.addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage testPageOne = WikiPageUtil.addPage(suitePage, "TestPageOne", "", PageType.TEST);
        final WikiPage testPageTwo = WikiPageUtil.addPage(suitePage, "TestPageTwo", "", PageType.TEST);

        finder = new LowestCommonRootFinder(suitePage);

        try {
            finder.getLowestCommonRoot(Arrays.asList(testPageOne, testPageTwo), true);
            fail();
        } catch (final TestsToRunContextGeneratorException e) {
            assertEquals(TestsToRunContextGeneratorReason.UNABLE_TO_FIND_COMMON_ROOT, e.getReason());
        }
    }

}
