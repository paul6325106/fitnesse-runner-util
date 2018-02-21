package paul6325106.fitnesse.listener;

import fitnesse.testsystems.ExceptionResult;
import fitnesse.testsystems.ExecutionResult;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
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

        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testsuite errors=\"1\" failures=\"1\" name=\"ThisIsThe.SuiteBeingRun\" skipped=\"0\" tests=\"3\" time=\"31.108\">" +
                "<properties/>" +
                "<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPageOne\" name=\"ThisIsThe.SuiteBeingRun.TestPageOne\" time=\"6.666\"/>" +
                "<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPageTwo\" name=\"ThisIsThe.SuiteBeingRun.TestPageTwo\" time=\"4.444\">" +
                "<failure message=\" exceptions: 0 wrong: 1\" type=\"java.lang.AssertionError\"/>" +
                "</testcase>" +
                "<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPageThree\" name=\"ThisIsThe.SuiteBeingRun.TestPageThree\" time=\"2.222\">" +
                "<failure message=\" exceptions: 1 wrong: 0\" type=\"java.lang.AssertionError\"/>" +
                "</testcase>" +
                "</testsuite>";

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

        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testsuite errors=\"0\" failures=\"0\" name=\"ThisIsThe.SuiteBeingRun\" skipped=\"0\" tests=\"1\" time=\"30.0\">" +
                "<properties/>" +
                "<error>java.lang.RuntimeException: this is the exception message</error>" +
                "<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPage\" name=\"ThisIsThe.SuiteBeingRun.TestPage\" time=\"10.0\"/>" +
                "</testsuite>";

        assertEquals(expected, getXmlResult());
    }

    @Test
    public void testEnvironmentProperties() throws IOException {
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

        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testsuite errors=\"0\" failures=\"0\" name=\"ThisIsThe.SuiteBeingRun\" skipped=\"0\" tests=\"1\" time=\"30.0\">" +
                "<properties>" +
                "<property name=\"Apple\" value=\"Banana\"/>" +
                "<property name=\"Cat\" value=\"Dog\"/>" +
                "</properties>" +
                "<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPage\" name=\"ThisIsThe.SuiteBeingRun.TestPage\" time=\"10.0\"/>" +
                "</testsuite>";

        assertEquals(expected, getXmlResult());
    }

    @Test
    public void testAssertionFailureInTest() throws IOException {
        final TestPage testPage = mock(TestPage.class);
        when(testPage.getFullPath()).thenReturn(SUITE_NAME + ".TestPage");

        final TestSummary testSummary = new TestSummary(10, 0, 0, 0);

        listener.testSystemStarted(null);
        clock.elapse(10000);
        listener.testStarted(testPage);
        clock.elapse(10000);
        listener.testAssertionVerified(null, getFailTestResult("Apple", "Banana", "Message!"));
        clock.elapse(10000);
        listener.testAssertionVerified(null, getFailTestResult("Cat", "Dog", "Another Message!"));
        clock.elapse(10000);
        listener.testComplete(testPage, testSummary);
        clock.elapse(10000);
        listener.testSystemStopped(null, null);

        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testsuite errors=\"0\" failures=\"0\" name=\"ThisIsThe.SuiteBeingRun\" skipped=\"0\" tests=\"1\" time=\"50.0\">" +
                "<properties/>" +
                "<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPage\" name=\"ThisIsThe.SuiteBeingRun.TestPage\" time=\"30.0\">" +
                "<failure message=\"[Apple] expected [Banana]\" type=\"java.lang.AssertionError\"/>" +
                "</testcase>" +
                "</testsuite>";

        assertEquals(expected, getXmlResult());
    }

    @Test
    public void testExceptionInTest() throws IOException {
        final TestPage testPage = mock(TestPage.class);
        when(testPage.getFullPath()).thenReturn(SUITE_NAME + ".TestPage");

        final TestSummary testSummary = new TestSummary(10, 0, 0, 0);

        listener.testSystemStarted(null);
        clock.elapse(10000);
        listener.testStarted(testPage);
        clock.elapse(10000);
        listener.testExceptionOccurred(null, getExceptionResult("Error message"));
        clock.elapse(10000);
        listener.testExceptionOccurred(null, getExceptionResult("Another error message"));
        clock.elapse(10000);
        listener.testComplete(testPage, testSummary);
        clock.elapse(10000);
        listener.testSystemStopped(null, null);

        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testsuite errors=\"0\" failures=\"0\" name=\"ThisIsThe.SuiteBeingRun\" skipped=\"0\" tests=\"1\" time=\"50.0\">" +
                "<properties/>" +
                "<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPage\" name=\"ThisIsThe.SuiteBeingRun.TestPage\" time=\"30.0\">" +
                "<failure message=\"Error message\" type=\"java.lang.Exception\"/>" +
                "</testcase>" +
                "</testsuite>";

        assertEquals(expected, getXmlResult());
    }

    @Test
    public void testSlimExceptionInTest() throws IOException {
        final TestPage testPage = mock(TestPage.class);
        when(testPage.getFullPath()).thenReturn(SUITE_NAME + ".TestPage");

        final TestSummary testSummary = new TestSummary(10, 0, 0, 0);

        listener.testSystemStarted(null);
        clock.elapse(10000);
        listener.testStarted(testPage);
        clock.elapse(10000);
        listener.testExceptionOccurred(null, new SlimExceptionResult(null, "alpha message:<<bravo>> charlie"));
        clock.elapse(10000);
        listener.testComplete(testPage, testSummary);
        clock.elapse(10000);
        listener.testSystemStopped(null, null);

        final String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<testsuite errors=\"0\" failures=\"0\" name=\"ThisIsThe.SuiteBeingRun\" skipped=\"0\" tests=\"1\" time=\"40.0\">" +
                "<properties/>" +
                "<testcase classname=\"ThisIsThe.SuiteBeingRun.TestPage\" name=\"ThisIsThe.SuiteBeingRun.TestPage\" time=\"20.0\">" +
                "<failure message=\"bravo\" type=\"java.lang.Exception\">" +
                "alpha message:&lt;&lt;bravo&gt;&gt; charlie" +
                "</failure>" +
                "</testcase>" +
                "</testsuite>";

        assertEquals(expected, getXmlResult());
    }

    private String getXmlResult() throws IOException {
        final String name = String.format("TEST-%s.xml", SUITE_NAME);
        final File file = new File(outputDir, name);
        return new String(Files.readAllBytes(file.toPath()));
    }

    private TestResult getFailTestResult(final String actual, final String expected, final String message) {
        return new TestResult() {

            @Override
            public boolean doesCount() {
                return true;
            }

            @Override
            public boolean hasActual() {
                return actual != null;
            }

            @Override
            public String getActual() {
                return actual;
            }

            @Override
            public boolean hasExpected() {
                return expected != null;
            }

            @Override
            public String getExpected() {
                return expected;
            }

            @Override
            public boolean hasMessage() {
                return message != null;
            }

            @Override
            public String getMessage() {
                return message;
            }

            @Override
            public ExecutionResult getExecutionResult() {
                return ExecutionResult.FAIL;
            }

            @Override
            public Map<String, ?> getVariablesToStore() {
                return null;
            }
        };
    }

    private ExceptionResult getExceptionResult(final String message) {
        return new ExceptionResult() {

            @Override
            public ExecutionResult getExecutionResult() {
                return ExecutionResult.ERROR;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

}
