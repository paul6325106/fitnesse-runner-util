package paul6325106.fitnesse.listener;

import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JUnitXMLTestListenerTest {

    private static final String SUITE_NAME = "ThisIsThe.SuiteBeingRun";
    private static final String START_TIME = "11:12:13";

    private File outputDir;
    private Map<String, String> properties;
    private TestSystemListener listener;
    private DateAlteringClock clock;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws ParseException, IOException {
        outputDir = temporaryFolder.newFolder("fitnesse-xml-results");
        properties = new HashMap<>();
        listener = new JUnitXMLTestListener(SUITE_NAME, outputDir, properties);
        clock = new DateAlteringClock(new SimpleDateFormat("HH:mm:ss").parse(START_TIME)).freeze();
    }

    @After
    public void tearDown() {
        Clock.restoreDefaultClock();
    }

    @Test
    public void testSuccessfulExecution() throws IOException {
        final TestPage testPageOne = mock(TestPage.class);
        when(testPageOne.getFullPath()).thenReturn(SUITE_NAME + ".TestPageOne");

        final TestPage testPageTwo = mock(TestPage.class);
        when(testPageTwo.getFullPath()).thenReturn(SUITE_NAME + ".TestPageTwo");

        final TestPage testPageThree = mock(TestPage.class);
        when(testPageThree.getFullPath()).thenReturn(SUITE_NAME + ".TestPageThree");

        final TestSummary testSummaryOne = new TestSummary(10, 0, 0, 0);
        final TestSummary testSummaryTwo = new TestSummary(3, 4, 0, 0);
        final TestSummary testSummaryThree = new TestSummary(6, 0, 0, 7);

        listener.testSystemStarted(null);
        clock.elapse(7777);
        listener.testStarted(testPageOne);
        clock.elapse(6666);
        listener.testComplete(testPageOne, testSummaryOne);
        clock.elapse(5555);
        listener.testStarted(testPageTwo);
        clock.elapse(4444);
        listener.testComplete(testPageTwo, testSummaryTwo);
        clock.elapse(3333);
        listener.testStarted(testPageThree);
        clock.elapse(2222);
        listener.testComplete(testPageThree, testSummaryThree);
        clock.elapse(1111);
        listener.testSystemStopped(null, null);

        final String expected = "<testsuite errors=\"1\" skipped=\"0\" tests=\"3\" time=\"31.108\" failures=\"1\" name=\"ThisIsThe.SuiteBeingRun\">\n" +
                "\t<properties></properties>\n" +
                "\t<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPageOne\" time=\"6.666\" name=\"ThisIsThe.SuiteBeingRun.TestPageOne\">\n" +
                "\t</testcase>\n" +
                "\t<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPageTwo\" time=\"4.444\" name=\"ThisIsThe.SuiteBeingRun.TestPageTwo\">\n" +
                "\t\t<failure type=\"java.lang.AssertionError\" message=\" exceptions: 0 wrong: 1\"></failure>\n" +
                "\t</testcase>\n" +
                "\t<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPageThree\" time=\"2.222\" name=\"ThisIsThe.SuiteBeingRun.TestPageThree\">\n" +
                "\t\t<failure type=\"java.lang.AssertionError\" message=\" exceptions: 1 wrong: 0\"></failure>\n" +
                "\t</testcase>\n" +
                "</testsuite>\n";

        assertEquals(expected, getXmlResult());
    }

    @Test
    public void testFailedExecution() throws IOException {
        final TestPage testPage = mock(TestPage.class);
        when(testPage.getFullPath()).thenReturn(SUITE_NAME + ".TestPage");

        final TestSummary testSummary = new TestSummary(10, 0, 0, 0);

        listener.testSystemStarted(null);
        clock.elapse(10000);
        listener.testStarted(testPage);
        clock.elapse(10000);
        listener.testComplete(testPage, testSummary);
        clock.elapse(10000);
        listener.testSystemStopped(null, new RuntimeException("this is the exception message"));

        final String expected = "<testsuite errors=\"0\" skipped=\"0\" tests=\"1\" time=\"30.0\" failures=\"0\" name=\"ThisIsThe.SuiteBeingRun\">\n" +
                "\t<properties></properties>\n" +
                "\t<error>java.lang.RuntimeException: this is the exception message</error>\n" +
                "\t<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPage\" time=\"10.0\" name=\"ThisIsThe.SuiteBeingRun.TestPage\">\n" +
                "\t</testcase>\n" +
                "</testsuite>\n";

        assertEquals(expected, getXmlResult());
    }

    @Test
    public void testProperties() throws IOException {
        properties.put("Apple", "Banana");
        properties.put("Cat", "Dog");

        final TestPage testPage = mock(TestPage.class);
        when(testPage.getFullPath()).thenReturn(SUITE_NAME + ".TestPage");

        final TestSummary testSummary = new TestSummary(10, 0, 0, 0);

        listener.testSystemStarted(null);
        clock.elapse(10000);
        listener.testStarted(testPage);
        clock.elapse(10000);
        listener.testComplete(testPage, testSummary);
        clock.elapse(10000);
        listener.testSystemStopped(null, null);

        final String expected = "<testsuite errors=\"0\" skipped=\"0\" tests=\"1\" time=\"30.0\" failures=\"0\" name=\"ThisIsThe.SuiteBeingRun\">\n" +
                "\t<properties>\n" +
                "\t\t<property name=\"Apple\" value=\"Banana\" />\n" +
                "\t\t<property name=\"Cat\" value=\"Dog\" />\n" +
                "\t</properties>\n" +
                "\t<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPage\" time=\"10.0\" name=\"ThisIsThe.SuiteBeingRun.TestPage\">\n" +
                "\t</testcase>\n" +
                "</testsuite>\n";

        assertEquals(expected, getXmlResult());
    }

    private String getXmlResult() throws IOException {
        final String name = String.format("TEST-%s.xml", SUITE_NAME);
        final File file = new File(outputDir, name);
        return new String(Files.readAllBytes(file.toPath()));
    }

}
