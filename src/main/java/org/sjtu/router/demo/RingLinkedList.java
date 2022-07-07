package org.sjtu.router.demo;

/**
 * @author hcstart
 * @create 2022-06-18 11:15
 *
 * 环形链表
 *
 */
public class RingLinkedList {
    private Node head;
    private Node tail;
    private int size;

    public RingLinkedList(Node head, Node tail, int size) {
        this.head = head;
        this.tail = tail;
        this.size = size;
    }

    public void add(Node node){
        if(size == 0){
            this.head=node;
            this.tail=node;
        }
    }

    class Node{
        private Object data;
        private Node next;
        private Node pre;

        public Node(Object data, Node next, Node pre) {
            this.data = data;
            this.next = next;
            this.pre = pre;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public Node getPre() {
            return pre;
        }

        public void setPre(Node pre) {
            this.pre = pre;
        }
    }

}
