package paul6325106.fitnesse.teststorun.partition;

import paul6325106.fitnesse.teststorun.domain.WeightedWikiPageGroup;

import java.util.Collection;
import java.util.List;

/**
 * Strategy for partitioning groups of WikiPages. WikiPages are initially split into groups by their SuiteSetUp and
 * SuiteTearDown combination, i.e. groups of tests that can be safely executed together. But it is desirable to
 * partition the WikiPages further or differently to make better use of multithreading.
 */
public interface PartitionStrategy {

    /**
     * Reconfigures groups of WikiPages to better make use of multiple threads. Groups are initially by SuiteSetUp and
     * SuiteTearDown combination. Groups and individual pages have weights assigned.
     * @param groups Groups of WikiPages, by SuiteSetUp/SuiteTearDown combination, with weights.
     * @param count Number of sets to create.
     * @return Reconfigured groups of WikiPages.
     */
    Collection<WeightedWikiPageGroup> partition(List<WeightedWikiPageGroup> groups, int count);

}
