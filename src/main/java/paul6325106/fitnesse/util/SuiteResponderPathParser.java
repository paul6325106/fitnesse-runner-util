package paul6325106.fitnesse.util;

import fitnesse.FitNesseContext;
import fitnesse.testrunner.SuiteFilter;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static java.net.URLDecoder.decode;

/**
 * Modification of logic from fitnesse.responders.run.SuiteResponder to allow parsing of suite request urls directly
 * from Strings.
 */
public class SuiteResponderPathParser {

    private static final String NOT_FILTER_ARG = "excludeSuiteFilter";
    private static final String AND_FILTER_ARG = "runTestsMatchingAllTags";
    private static final String OR_FILTER_ARG_1 = "runTestsMatchingAnyTag";
    private static final String OR_FILTER_ARG_2 = "suiteFilter";
    private static final String START_TEST_ARG = "firstTest";

    private final FitNesseContext context;

    public SuiteResponderPathParser(final FitNesseContext context) {
        this.context = context;
    }

    public WikiPage getRequestedPage(final String path) throws URISyntaxException {
        return context.getRootPage().getPageCrawler().getPage(PathParser.parse(new URI(path).getPath()));
    }

    public SuiteFilter getSuiteFilter(final String path) throws URISyntaxException, UnsupportedEncodingException {
        final URI uri = new URI(path);

        final Map<String, String> params = splitQuery(uri);

        return new SuiteFilter(getOrTags(params), getNotTags(params), getAndTags(params),
                getFirstTest(uri.getPath(), params));
    }

    private String getOrTags(final Map<String, String> params) {
        return params.containsKey(OR_FILTER_ARG_1) ? params.get(OR_FILTER_ARG_1) : params.get(OR_FILTER_ARG_2);
    }

    private String getNotTags(final Map<String, String> params) {
        return params.get(NOT_FILTER_ARG);
    }

    private String getAndTags(final Map<String, String> params) {
        return params.get(AND_FILTER_ARG);
    }

    private String getFirstTest(final String suiteName, final Map<String, String> params) {
        if (!params.containsKey(START_TEST_ARG)) {
            return null;
        }

        final String startTest = params.get(START_TEST_ARG);

        if (startTest.indexOf(suiteName) != 0) {
            return suiteName + "." + startTest;
        } else {
            return startTest;
        }
    }

    private Map<String, String> splitQuery(final URI uri) throws UnsupportedEncodingException {
        final Map<String, String> params = new HashMap<>();

        if (uri.getQuery() == null) {
            return params;
        }

        for (final String pair : uri.getQuery().split("&")) {
            int idx = pair.indexOf("=");
            params.put(decode(pair.substring(0, idx), "UTF-8"), decode(pair.substring(idx + 1), "UTF-8"));
        }

        return params;
    }

}
