package paul6325106.fitnesse.teststorun.domain;

import fitnesse.wiki.WikiPage;

import java.util.List;

public class TestsToRunContext {

    private final WikiPage requestedPage;
    private final List<WikiPage> testsToRun;

    public TestsToRunContext(final WikiPage requestedPage, final List<WikiPage> testsToRun) {
        this.requestedPage = requestedPage;
        this.testsToRun = testsToRun;
    }

    public WikiPage getRequestedPage() {
        return requestedPage;
    }

    public List<WikiPage> getTestsToRun() {
        return testsToRun;
    }

    @Override
    public String toString() {
        return "TestsToRunContext{" +
                "requestedPage=" + requestedPage +
                ", testsToRun=" + testsToRun +
                '}';
    }
}
