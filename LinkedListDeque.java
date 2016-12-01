package editor;

public class LinkedListDeque<Item> {
    public class Node {
        public Item item;
        public Node prev;
        public Node next;

        public Node(Item i, Node p, Node n) {
            item = i;
            prev = p;
            next = n;
            }
        }

    private Node sentinel;
    private int size;

    public LinkedListDeque() {
        size = 0;
        sentinel = new Node(null,null,null);
    }

    /* Adds an item to the front of the Deque */
    public void addFirst(Item x) {
        if (size == 0) {
            sentinel.next = new Node(x, sentinel, null);
            sentinel.prev = sentinel.next;
        } else {
            sentinel.next = new Node(x, sentinel.prev, sentinel.next);
            sentinel.next.next.prev = sentinel.next;
            sentinel.prev.next = sentinel.next;
        }

        size += 1;
    }


    /* Adds an item to the back of the Deque */
    public void addLast(Item x) {
        if (size == 0) {
            sentinel.next = new Node(x, sentinel, null);
            sentinel.prev = sentinel.next;
        } else {
            sentinel.prev.next = new Node(x, sentinel.prev, sentinel.next);
            sentinel.prev = sentinel.prev.next;
            sentinel.next.prev = sentinel.prev;
        }

        size += 1;
    }

    /* Inserts the item at the given index */
    public void insert(Item x, int w) {
        Node n = this.getNode(w);

        if (size == 0) {
            sentinel.next = new Node(x, sentinel, null);
            sentinel.prev = sentinel.next;
        } else {
            n.next = new Node(x, n, n.next);
            n.next.next.prev = n.next;
        }

        size += 1;
    }

     /* Returns true if deque is empty, false otherwise */
     public boolean isEmpty() {
         if (size == 0) {
             return true;
         } else {
             return false;
         }
     }

    /* Returns the number of items in the Deque */
    public int size() {
        return size;
    }

    /* Prints the items in the Deque from first to last */
    public void printDeque() {
        Node x = sentinel.next;
        while (x != sentinel.prev) {
            System.out.print(x.item + " ");
            x = x.next;
        }
        System.out.println(x.item);
    }

    /* Removes and returns the item at the front of the Deque */
    public Item removeFirst() {
        if (size == 0) {
            return null;
        } else if (size == 1) {
            Node oldFirst = sentinel.next;
            sentinel.prev = null;
            sentinel.next = null;
            size -= 1;

            oldFirst.prev = null;
            return oldFirst.item;
        } else {
            Node oldFirst = sentinel.next;
            sentinel.next = sentinel.next.next;
            sentinel.prev.next = sentinel.next;
            size -= 1;

            oldFirst.next = null;
            oldFirst.prev = null;
            return oldFirst.item;
        }
    }

    /* Removes and returns the item at the back of the Deque */
    public Item removeLast() {
        if (size == 0) {
            return null;
        } else if (size == 1) {
            Node oldLast = sentinel.prev;
            sentinel.prev = null;
            sentinel.next = null;
            size -= 1;

            oldLast.prev = null;
            return oldLast.item;
        } else {
            Node oldLast = sentinel.prev;
            sentinel.prev = sentinel.prev.prev;
            sentinel.prev.next = sentinel.next;
            size -= 1;

            oldLast.next = null;
            oldLast.prev = null;
            return oldLast.item;
        }
    }

    /* Deletes the item at the given index */
    public Item delete(int w) {
        if (size == 0) {
            return null;
        } else if (size == 1) {
            Node oldNode = sentinel.prev;
            sentinel.prev = null;
            sentinel.next = null;
            size -= 1;

            oldNode.prev = null;
            return oldNode.item;
        } else {
            Node oldNode = this.getNode(w);
            oldNode.next.prev = oldNode.prev;
            oldNode.prev.next = oldNode.next;
            size -= 1;

            oldNode.next = null;
            oldNode.prev = null;
            return oldNode.item;
        }
    }

    /* Gets the node at the given index */
    private Node getNode(int index) {
        Node x = sentinel;

        if (index > size-1) {
            return null;
        } else {
            for (int i = 0; i <= index; i++) {
                x = x.next;
            }
            return x;
        }
    }

    /* Gets the item at the given index */
    public Item get(int index) {
        Node x = sentinel;

        if (index > size-1) {
            return null;
        } else {
            for (int i = 0; i <= index; i++) {
                x = x.next;
            }
            return x.item;
        }
    }

    public Item getRecursive(int index) {
        if (index > size-1) {
            return null;
        } else {
            return getRecursiveHelper(sentinel.next, index);
        }
    }

    private Item getRecursiveHelper(Node x,int i) {
        if (i == 0) {
            return x.item;
        } else {
            return getRecursiveHelper(x.next, i-1);
        }
    }
}