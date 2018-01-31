package paul6325106.fitnesse.teststorun.util;

import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageType;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;
import paul6325106.fitnesse.teststorun.domain.SuiteSetUpTearDownPair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static paul6325106.fitnesse.teststorun.util.WikiPageUtil.addPage;
import static paul6325106.fitnesse.teststorun.util.WikiPageUtil.getFullPathName;

public class SuiteSetUpTearDownMapperTest {

    private SuiteSetUpTearDownMapper mapper;

    private WikiPage root;

    @Before
    public void setUp() throws Exception {
        root = FitNesseUtil.makeTestContext().getRootPage();
        mapper = new SuiteSetUpTearDownMapper();
    }

    private SuiteSetUpTearDownPair fromWikiPages(final WikiPage suiteSetUp, final WikiPage suiteTearDown) {
        return new SuiteSetUpTearDownPair(getFullPathName(suiteSetUp), getFullPathName(suiteTearDown));
    }

    @Test
    public void testNoSuiteSetUpAndNoSuiteTearDown() throws Exception {
        final WikiPage testPage = addPage(root, "TestPage", "", PageType.TEST);

        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map =
                mapper.getSuiteSetUpTearDownMap(Collections.singletonList(testPage));

        assertTrue(map.containsKey(new SuiteSetUpTearDownPair(null, null)));

        final List<WikiPage> mapped = map.get(new SuiteSetUpTearDownPair(null, null));

        assertEquals(1, mapped.size());
        assertEquals(testPage, mapped.get(0));
    }

    @Test
    public void testRegularSetUpAndRegularTearDown() throws Exception {
        final WikiPage suitePage = addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage testPage = addPage(suitePage, "TestPage", "", PageType.TEST);
        addPage(suitePage, "SetUp", "", PageType.STATIC);
        addPage(suitePage, "TearDown", "", PageType.STATIC);

        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map =
                mapper.getSuiteSetUpTearDownMap(Collections.singletonList(testPage));

        assertTrue(map.containsKey(new SuiteSetUpTearDownPair(null, null)));

        final List<WikiPage> mapped = map.get(new SuiteSetUpTearDownPair(null, null));

        assertEquals(1, mapped.size());
        assertEquals(testPage, mapped.get(0));
    }

    @Test
    public void testSuiteSetUpWithoutSuiteTearDown() throws Exception {
        final WikiPage suitePage = addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage suiteSetUp = addPage(suitePage, "SuiteSetUp", "", PageType.STATIC);
        final WikiPage testPage = addPage(suitePage, "TestPage", "", PageType.TEST);

        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map = mapper.getSuiteSetUpTearDownMap(
                Collections.singletonList(testPage));

        assertTrue(map.containsKey(fromWikiPages(suiteSetUp, null)));

        final List<WikiPage> mapped = map.get(fromWikiPages(suiteSetUp, null));

        assertEquals(1, mapped.size());
        assertTrue(mapped.contains(testPage));
    }

    @Test
    public void testSuiteTearDownWithoutSuiteSetUp() throws Exception {
        final WikiPage suitePage = addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage suiteTearDown = addPage(suitePage, "SuiteTearDown", "", PageType.STATIC);
        final WikiPage testPage = addPage(suitePage, "TestPage", "", PageType.TEST);

        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map = mapper.getSuiteSetUpTearDownMap(
                Collections.singletonList(testPage));

        assertTrue(map.containsKey(fromWikiPages(null, suiteTearDown)));

        final List<WikiPage> mapped = map.get(fromWikiPages(null, suiteTearDown));

        assertEquals(1, mapped.size());
        assertTrue(mapped.contains(testPage));
    }

    @Test
    public void testSuiteSetUpAndSuiteTearDown() throws Exception {
        final WikiPage suitePage = addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage suiteSetUp = addPage(suitePage, "SuiteSetUp", "", PageType.STATIC);
        final WikiPage suiteTearDown = addPage(suitePage, "SuiteTearDown", "", PageType.STATIC);
        final WikiPage testPage = addPage(suitePage, "TestPage", "", PageType.TEST);

        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map = mapper.getSuiteSetUpTearDownMap(
                Collections.singletonList(testPage));

        assertTrue(map.containsKey(fromWikiPages(suiteSetUp, suiteTearDown)));

        final List<WikiPage> mapped = map.get(fromWikiPages(suiteSetUp, suiteTearDown));

        assertEquals(1, mapped.size());
        assertTrue(mapped.contains(testPage));
    }

    @Test
    public void testDifferentSuiteSetUpSameSuiteTearDown() throws Exception {
        final WikiPage suitePage = addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage suitePageOne = addPage(suitePage, "SuitePageOne", "", PageType.SUITE);
        final WikiPage suitePageTwo = addPage(suitePage, "SuitePageTwo", "", PageType.SUITE);
        final WikiPage suiteTearDown = addPage(suitePage, "SuiteTearDown", "", PageType.STATIC);
        final WikiPage suiteSetUpOne = addPage(suitePageOne, "SuiteSetUp", "", PageType.STATIC);
        final WikiPage suiteSetUpTwo = addPage(suitePageTwo, "SuiteSetUp", "", PageType.STATIC);
        final WikiPage testPageOne = addPage(suitePageOne, "TestPageOne", "", PageType.TEST);
        final WikiPage testPageTwo = addPage(suitePageTwo, "TestPageTwo", "", PageType.TEST);

        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map = mapper.getSuiteSetUpTearDownMap(
                Arrays.asList(testPageOne, testPageTwo));

        assertTrue(map.containsKey(fromWikiPages(suiteSetUpOne, suiteTearDown)));
        assertTrue(map.containsKey(fromWikiPages(suiteSetUpTwo, suiteTearDown)));

        final List<WikiPage> mappedFiveOne = map.get(fromWikiPages(suiteSetUpOne, suiteTearDown));
        final List<WikiPage> mappedFiveTwo = map.get(fromWikiPages(suiteSetUpTwo, suiteTearDown));

        assertEquals(1, mappedFiveOne.size());
        assertTrue(mappedFiveOne.contains(testPageOne));

        assertEquals(1, mappedFiveTwo.size());
        assertTrue(mappedFiveTwo.contains(testPageTwo));
    }

    @Test
    public void testSameSuiteSetUpDifferentSuiteTearDown() throws Exception {
        final WikiPage suitePage = addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage suitePageOne = addPage(suitePage, "SuitePageOne", "", PageType.SUITE);
        final WikiPage suitePageTwo = addPage(suitePage, "SuitePageTwo", "", PageType.SUITE);
        final WikiPage suiteSetUp = addPage(suitePage, "SuiteSetUp", "", PageType.STATIC);
        final WikiPage suiteTearDownOne = addPage(suitePageOne, "SuiteTearDown", "", PageType.STATIC);
        final WikiPage suiteTearDownTwo = addPage(suitePageTwo, "SuiteTearDown", "", PageType.STATIC);
        final WikiPage testPageOne = addPage(suitePageOne, "TestPageOne", "", PageType.TEST);
        final WikiPage testPageTwo = addPage(suitePageTwo, "TestPageTwo", "", PageType.TEST);

        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map = mapper.getSuiteSetUpTearDownMap(
                Arrays.asList(testPageOne, testPageTwo));

        assertTrue(map.containsKey(fromWikiPages(suiteSetUp, suiteTearDownOne)));
        assertTrue(map.containsKey(fromWikiPages(suiteSetUp, suiteTearDownTwo)));

        final List<WikiPage> mappedOne = map.get(fromWikiPages(suiteSetUp, suiteTearDownOne));
        final List<WikiPage> mappedTwo = map.get(fromWikiPages(suiteSetUp, suiteTearDownTwo));

        assertEquals(1, mappedOne.size());
        assertTrue(mappedOne.contains(testPageOne));

        assertEquals(1, mappedTwo.size());
        assertTrue(mappedTwo.contains(testPageTwo));
    }

    @Test
    public void testOverwritten() throws Exception {
        // highest level suite, declares SU1 and TD1
        final WikiPage suitePageOne = addPage(root, "SuitePageOne", "", PageType.SUITE);
        final WikiPage suiteSetUpOne = addPage(suitePageOne, "SuiteSetUp", "", PageType.STATIC);
        final WikiPage suiteTearDownOne = addPage(suitePageOne, "SuiteTearDown", "", PageType.STATIC);
        final WikiPage testPageOne = addPage(suitePageOne, "TestPageOne", "", PageType.TEST);
        final SuiteSetUpTearDownPair pairOne = fromWikiPages(suiteSetUpOne, suiteTearDownOne);

        // middle suite, shares SU1, overwrites TD1
        final WikiPage suitePageTwo = addPage(suitePageOne, "SuitePageTwo", "", PageType.SUITE);
        final WikiPage suiteTearDownTwo = addPage(suitePageTwo, "SuiteTearDown", "", PageType.STATIC);
        final WikiPage testPageTwo = addPage(suitePageTwo, "TestPageTwo", "", PageType.TEST);
        final SuiteSetUpTearDownPair pairTwo = fromWikiPages(suiteSetUpOne, suiteTearDownTwo);

        // bottom suite, shares SU1, overwrites TD2
        final WikiPage suitePageThree = addPage(suitePageTwo, "SuitePageThree", "", PageType.SUITE);
        final WikiPage suiteTearDownThree = addPage(suitePageThree, "SuiteTearDown", "", PageType.STATIC);
        final WikiPage testPageThree = addPage(suitePageThree, "TestPageThree", "", PageType.TEST);
        final SuiteSetUpTearDownPair pairThree = fromWikiPages(suiteSetUpOne, suiteTearDownThree);

        // bottom suite, overwrites SU1, shares TD2
        final WikiPage suitePageFour = addPage(suitePageTwo, "SuitePageFour", "", PageType.SUITE);
        final WikiPage suiteSetUpFour = addPage(suitePageFour, "SuiteSetUp", "", PageType.STATIC);
        final WikiPage testPageFour = addPage(suitePageFour, "TestPageFour", "", PageType.TEST);
        final SuiteSetUpTearDownPair pairFour = fromWikiPages(suiteSetUpFour, suiteTearDownTwo);

        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map = mapper.getSuiteSetUpTearDownMap(
                Arrays.asList(testPageOne, testPageTwo, testPageThree, testPageFour));

        assertTrue(map.containsKey(pairOne));
        assertTrue(map.containsKey(pairTwo));
        assertTrue(map.containsKey(pairThree));
        assertTrue(map.containsKey(pairFour));

        final List<WikiPage> mappedOne = map.get(pairOne);
        final List<WikiPage> mappedTwo = map.get(pairTwo);
        final List<WikiPage> mappedThree = map.get(pairThree);
        final List<WikiPage> mappedFour = map.get(pairFour);

        assertEquals(1, mappedOne.size());
        assertTrue(mappedOne.contains(testPageOne));

        assertEquals(1, mappedTwo.size());
        assertTrue(mappedTwo.contains(testPageTwo));

        assertEquals(1, mappedThree.size());
        assertTrue(mappedThree.contains(testPageThree));

        assertEquals(1, mappedFour.size());
        assertTrue(mappedFour.contains(testPageFour));
    }

    @Test
    public void testEachTestPageMappedOnlyOnce() throws Exception {
        final WikiPage testPageOne = addPage(root, "TestPageOne", "", PageType.TEST);
        final WikiPage testPageTwo = addPage(root, "TestPageTwo", "", PageType.TEST);
        final WikiPage suitePage = addPage(root, "SuitePage", "", PageType.SUITE);
        final WikiPage suiteSetUp = addPage(suitePage, "SuiteSetUp", "", PageType.STATIC);
        final WikiPage suiteTearDown = addPage(suitePage, "SuiteTearDown", "", PageType.STATIC);
        final WikiPage testPageThree = addPage(suitePage, "TestPageThree", "", PageType.TEST);

        // repeat test pages in list
        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map = mapper.getSuiteSetUpTearDownMap(
                Arrays.asList(testPageOne, testPageTwo, testPageThree, testPageTwo, testPageTwo, testPageOne));

        final SuiteSetUpTearDownPair withoutSetUpTearDown = new SuiteSetUpTearDownPair(null, null);
        final SuiteSetUpTearDownPair withSetUpTearDown = fromWikiPages(suiteSetUp, suiteTearDown);

        assertTrue(map.containsKey(withoutSetUpTearDown));
        assertTrue(map.containsKey(withSetUpTearDown));

        final List<WikiPage> mappedWithoutSetUpTearDown = map.get(withoutSetUpTearDown);
        final List<WikiPage> mappedWithSetUpTearDown = map.get(withSetUpTearDown);

        assertEquals(2, mappedWithoutSetUpTearDown.size());
        assertTrue(mappedWithoutSetUpTearDown.contains(testPageOne));
        assertTrue(mappedWithoutSetUpTearDown.contains(testPageTwo));

        assertEquals(1, mappedWithSetUpTearDown.size());
        assertTrue(mappedWithSetUpTearDown.contains(testPageThree));
    }

}
