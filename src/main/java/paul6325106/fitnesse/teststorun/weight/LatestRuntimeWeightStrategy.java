package paul6325106.fitnesse.teststorun.weight;

import fitnesse.FitNesseContext;
import fitnesse.reporting.history.ExecutionReport;
import fitnesse.reporting.history.InvalidReportException;
import fitnesse.reporting.history.PageHistory;
import fitnesse.reporting.history.TestHistory;
import fitnesse.reporting.history.TestResultRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import paul6325106.fitnesse.teststorun.exception.TestsToRunContextGeneratorException;
import paul6325106.fitnesse.teststorun.exception.TestsToRunContextGeneratorReason;
import util.FileUtil;

import java.io.IOException;
import java.util.Date;

/**
 * Strategy for determining a WikiPage's weight by the runtime of the latest test results. A default weight of 1 is used
 * if no test results can be found.
 */
public class LatestRuntimeWeightStrategy implements WeightStrategy {

    private static Logger LOGGER = LoggerFactory.getLogger(LatestRuntimeWeightStrategy.class);

    private final TestHistory history;

    public LatestRuntimeWeightStrategy(final FitNesseContext context) {
        history = new TestHistory(context.getTestHistoryDirectory());
    }

    @Override
    public long getWeight(final String pathName) throws TestsToRunContextGeneratorException {
        final PageHistory pageHistory = history.getPageHistory(pathName);

        if (pageHistory == null) {
            LOGGER.warn("No page history available: " + pathName);
            return 1L;
        }

        final Date latestDate = pageHistory.getLatestDate();
        final TestResultRecord testResultRecord = pageHistory.get(latestDate);

        final String content;
        try {
            content = FileUtil.getFileContent(testResultRecord.getFile());
        } catch (final IOException e) {
            throw new TestsToRunContextGeneratorException("Unable to read test result record: " +
                    testResultRecord.getFile(), e, TestsToRunContextGeneratorReason.UNABLE_TO_GET_WIKI_PAGE_WEIGHT);
        }

        final ExecutionReport executionReport;
        try {
            executionReport = ExecutionReport.makeReport(content);
        } catch (final InvalidReportException e) {
            throw new TestsToRunContextGeneratorException("Unable to make execution report", e,
                    TestsToRunContextGeneratorReason.UNABLE_TO_GET_WIKI_PAGE_WEIGHT);
        }

        return executionReport.getTotalRunTimeInMillis();
    }

}
