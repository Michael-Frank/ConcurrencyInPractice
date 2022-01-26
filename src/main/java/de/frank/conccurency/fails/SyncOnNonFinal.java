package de.frank.conccurency.fails;

import lombok.AllArgsConstructor;
import lombok.Data;

public class SyncOnNonFinal {
    @Data
    @AllArgsConstructor
    public static class Node {
        Object element;
        Node previous;
    }

    public static class List {
        private Node head;
        private int size;

        public void add(Object o) {
            synchronized (head) {
                head = new Node(o, head);//add obj as new head
                size = size + 1;
            }
        }

        public void reset() {
            head = null;
            size = 0;
        }

    }
}
