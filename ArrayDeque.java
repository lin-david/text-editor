package editor;

public class ArrayDeque<Item> {
    private Item[] items;
    private int size;
    private int nextFirst;
    private int nextLast;

    private static int RFactor = 2;
    private static double usageRatio = .25;

    public ArrayDeque() {
        size = 0;
        items = (Item[]) new Object[8];
        nextFirst = 0;
        nextLast = 1;
    }

    private void increaseSize(int capacity) {
        Item[] a = (Item[]) new Object[capacity];
        System.arraycopy(items, 0, a, 0, size);

        if (getFirstIndex() > getLastIndex()) {
            int t = getFirstIndex();

            while (t != items.length) {
                a[a.length-1-(items.length-1-t)] = items[t];
                t += 1;
            }

            nextFirst = a.length-1-(items.length-1-getFirstIndex())-1;
        } else  if (nextLast == 0) {
            nextFirst = a.length-1;
            nextLast = getLastIndex()+1;
        }

        items = a;
    }

    private void decreaseSize(int capacity) {
        Item[] a = (Item[]) new Object[capacity];

        if (getFirstIndex() > getLastIndex()) {
            System.arraycopy(items, getFirstIndex(), a, 0, items.length-getFirstIndex());
            System.arraycopy(items, 0, a, items.length-getFirstIndex(), getLastIndex()+1);

            nextFirst = a.length-1;
            nextLast = size;
        } else {
            System.arraycopy(items, getFirstIndex(), a, 0, size);
            nextFirst = a.length-1;
            nextLast = size;
        }

        items = a;
    }

    private int getFirstIndex() {
        if (nextFirst == items.length-1) {
            return 0;
        } else {
            return nextFirst+1;
        }
    }

    private int getLastIndex() {
        if (nextLast == 0) {
            return items.length-1;
        } else {
            return nextLast-1;
        }
    }

    /* Adds an item to the front of the Deque */
    public void addFirst(Item x) {
        if (size == items.length) {
            increaseSize(size * RFactor);
        }

        items[nextFirst] = x;

        if (nextFirst == 0) {
            nextFirst = items.length-1;
        } else {
            nextFirst -= 1;
        }
        size += 1;
    }


    /* Adds an item to the back of the Deque */
    public void addLast(Item x) {
        if (size == items.length) {
            increaseSize(size * RFactor);
        }

        items[nextLast] = x;

        if (nextLast == items.length-1) {
            nextLast = 0;
        } else {
            nextLast += 1;
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
        for (Item x : items) {
            System.out.print(x + " ");
        }
        System.out.println();
    }

    /* Removes and returns the item at the front of the Deque */
    public Item removeFirst() {
        nextFirst = getFirstIndex();
        Item oldFirst = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;

        while (items.length >= 16 && ((double) size/items.length) < usageRatio) {
            decreaseSize(items.length/2);
        }

        return oldFirst;
    }

    /* Removes and returns the item at the back of the Deque */
    public Item removeLast() {
        nextLast = getLastIndex();
        Item oldLast = items[nextLast];
        items[nextLast] = null;
        size -= 1;

        while (items.length >= 16 && ((double) size/items.length) < usageRatio) {
            decreaseSize(items.length/2);
        }

        return oldLast;
    }

    /* Gets the item at the given index */
    public Item get(int index) {
        if (index > size) {
            return null;
        }

        if (index + getFirstIndex() > items.length-1) {
            return items[index - (items.length - getFirstIndex())];
        } else {
            return items[getFirstIndex() + index];
        }
    }
}
