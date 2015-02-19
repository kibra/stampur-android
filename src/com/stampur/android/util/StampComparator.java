package com.stampur.android.util;

import java.util.Comparator;

import com.stampur.android.Stamp;

/*
 * Stamp Comparator - Compares Stamps
 *                    (Delegates comparison to the Stamp class since it implements Comparable)
 * Note: This Comparator imposes orderings that are consistent with equals()
 *       => [compare(stamp1, stamp2) = 0] implies that [stamp1.equals(stamp2) = true]
 *
 */
public class StampComparator implements Comparator<Stamp> {

    public static StampComparator comparator;

    private StampComparator() {

    }

    public static StampComparator getInstance() {
        if (null == comparator) {
            comparator = new StampComparator();
        }

        return comparator;
    }

    public int compare(Stamp stamp1, Stamp stamp2) {
        return stamp1.compareTo(stamp2);
    }

    public boolean equals(Object o) {
        return false;
    }
}
