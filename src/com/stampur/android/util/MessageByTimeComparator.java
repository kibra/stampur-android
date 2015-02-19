package com.stampur.android.util;

import java.util.Comparator;

import com.stampur.android.Message;

/*
 * Message Comparator - Compares Messages by their Timestamp
 * Note: This Comparator imposes orderings that are inconsistent with equals()
 * => [compare(message1, message2) = 0] does NOT necessarily mean [message1.equals(message2) = true]
 *
 */
public class MessageByTimeComparator implements Comparator<Message> {

    public static MessageByTimeComparator comparator;

    private MessageByTimeComparator() {

    }

    public static MessageByTimeComparator getInstance() {
        if (null == comparator) {
            comparator = new MessageByTimeComparator();
        }

        return comparator;
    }

    public int compare(Message message1, Message message2) {
        return message1.getTime().compareTo(message2.getTime());
    }

    public boolean equals(Object o) {
        return false;
    }
}
