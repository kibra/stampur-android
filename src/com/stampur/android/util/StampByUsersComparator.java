package com.stampur.android.util;

import java.util.Comparator;

import com.stampur.android.Stamp;

/*
 * Stamp Comparator - Compares Stamps by their Name/Title
 *
 * Note: This Comparator imposes orderings that are inconsistent with equals()
 * => [compare(stamp1, stamp2) = 0] does NOT necessarily mean [stamp1.equals(stamp2) = true]
 *
 */
public class StampByUsersComparator implements Comparator<Stamp> {

    public static StampByUsersComparator comparator;

    private StampByUsersComparator() {

    }

    public static StampByUsersComparator getInstance() {
        if (null == comparator) {
            comparator = new StampByUsersComparator();
        }

        return comparator;
    }

    public int compare(Stamp stamp1, Stamp stamp2) {
        return (Integer.valueOf(stamp1.getNumUsers())).compareTo(Integer.valueOf(stamp2.getNumUsers()));
    }

    public boolean equals(Object o) {
        return false;
    }
}
