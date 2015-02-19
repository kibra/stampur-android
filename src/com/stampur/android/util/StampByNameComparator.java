package com.stampur.android.util;

import java.util.Comparator;

import com.stampur.android.Stamp;

/*
 * Stamp Comparator - Compares Stamps by their Name/Title
 *
 */
public class StampByNameComparator implements Comparator<Stamp> {

    public static StampByNameComparator comparator;

    private StampByNameComparator() {

    }

    public static StampByNameComparator getInstance() {
        if (null == comparator) {
            comparator = new StampByNameComparator();
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
