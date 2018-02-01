package paul6325106.fitnesse.teststorun;

import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageType;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import paul6325106.fitnesse.teststorun.domain.SuiteSetUpTearDownPair;
import paul6325106.fitnesse.teststorun.domain.WeightedWikiPage;
import paul6325106.fitnesse.teststorun.domain.WeightedWikiPageGroup;
import paul6325106.fitnesse.teststorun.weight.WeightStrategy;
import paul6325106.fitnesse.util.WikiPageUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WeightedWikiPageGroupFactoryTest {

    private WikiPage root;

    @Mock
    private WeightStrategy strategy;

    @InjectMocks
    private WeightedWikiPageGroupFactory factory;

    @Before
    public void setUp() throws Exception {
        root = FitNesseUtil.makeTestContext().getRootPage();
    }

    @Test
    public void testBuild() throws Exception {
        WikiPageUtil.addPage(root, "SuiteSetUp", "", PageType.TEST);
        WikiPageUtil.addPage(root, "SuiteTearDown", "", PageType.TEST);
        final WikiPage pageOne = WikiPageUtil.addPage(root, "PageOne", "", PageType.TEST);
        final WikiPage pageTwo = WikiPageUtil.addPage(root, "PageTwo", "", PageType.TEST);
        final WikiPage pageThree = WikiPageUtil.addPage(root, "PageThree", "", PageType.TEST);

        when(strategy.getWeight("SuiteSetUp")).thenReturn(110000L);
        when(strategy.getWeight("SuiteTearDown")).thenReturn(201000L);
        when(strategy.getWeight("PageOne")).thenReturn(300100L);
        when(strategy.getWeight("PageTwo")).thenReturn(400010L);
        when(strategy.getWeight("PageThree")).thenReturn(500001L);

        final Map<SuiteSetUpTearDownPair, List<WikiPage>> map = new HashMap<>();
        map.put(new SuiteSetUpTearDownPair("SuiteSetUp", "SuiteTearDown"), Arrays.asList(pageOne, pageTwo, pageThree));

        final WeightedWikiPageGroup group = factory.build(map).get(0);

        assertEquals(group.getSuiteSetUpTearDownPair(), new SuiteSetUpTearDownPair("SuiteSetUp", "SuiteTearDown"));

        assertEquals(group.getWikiPages(), Arrays.asList(
                new WeightedWikiPage(pageOne, 300100L),
                new WeightedWikiPage(pageTwo, 400010L),
                new WeightedWikiPage(pageThree, 500001L)
        ));

        assertEquals(group.getSetUpWeight(), 110000L);
        assertEquals(group.getTearDownWeight(), 201000L);
        assertEquals(group.getPagesWeight(), 1200111L);
        assertEquals(group.getTotalWeight(), 1511111L);
    }

}
