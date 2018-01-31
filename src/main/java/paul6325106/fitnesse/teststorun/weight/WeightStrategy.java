package paul6325106.fitnesse.teststorun.weight;

import paul6325106.fitnesse.teststorun.exception.TestsToRunContextGeneratorException;

/**
 * Strategy for assigning a weight value to a WikiPage. This weight value can be used for balancing the workload across
 * multiple executors later.
 */
public interface WeightStrategy {

    /**
     * Assigns a weight value to a WikiPage
     * @param pathName Path name identifying the WikiPage instance.
     * @return weight for WikiPage.
     * @throws TestsToRunContextGeneratorException when unable to determine weight for WikiPage.
     */
    long getWeight(String pathName) throws TestsToRunContextGeneratorException;

}
