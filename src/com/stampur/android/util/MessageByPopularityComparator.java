package com.stampur.android.util;

import java.util.Comparator;

import com.stampur.android.Message;

/*
 * Message Comparator - Compares Messages by the number of Votes
 * Note: This Comparator imposes orderings that are inconsistent with equals()
 * => [compare(message1, message2) = 0] does NOT necessarily mean [message1.equals(message2) = true]
 *
 */
public class MessageByPopularityComparator implements Comparator<Message> {

    public static MessageByPopularityComparator comparator;

    private MessageByPopularityComparator() {

    }

    public static MessageByPopularityComparator getInstance() {
        if (null == comparator) {
            comparator = new MessageByPopularityComparator();
        }

        return comparator;
    }

    public int compare(Message message1, Message message2) {
        return (Integer.valueOf(message1.getScore())).compareTo(
                Integer.valueOf(message2.getScore()));
    }

    public boolean equals(Object o) {
        return false;
    }
}
