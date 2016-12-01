package editor;

import javafx.scene.text.Text;

public class TextBuffer {
    private LinkedListDeque<Text> enteredText = new LinkedListDeque<Text>();
    private int currentPos = 0;

    public void addText(Text t) {

        if (currentPos == 0) {
            enteredText.addFirst(t);
        } else if (currentPos == enteredText.size()) {
            enteredText.addLast(t);
        } else {
            enteredText.insert(t, currentPos-1);
        }

        currentPos += 1;
    }

    public Text deleteChar() {
        Text deleted;

        if (currentPos == 0) {
            return null;
        } else if (currentPos == 1) {
            deleted = enteredText.removeFirst();
        } else if (currentPos == enteredText.size()) {
            deleted = enteredText.removeLast();
        } else {
            deleted = enteredText.delete(currentPos-1);
        }

        currentPos -= 1;
        return deleted;
    }

    public Text get(int i) {
        return enteredText.get(i);
    }

    public int getCurrentPos() {
        return currentPos;
    }

    public int size() {
        return enteredText.size();
    }

    public void changeCurrentPos(int i) {
        currentPos = i;
    }

    public boolean moveCurrentPosLeft() {
        if (currentPos != 0) {
            currentPos -= 1;
            return true;
        }
        return false;
    }

    public boolean moveCurrentPosRight() {
        if (currentPos != enteredText.size() - 1) {
            currentPos += 1;
            return true;
        }
        return false;
    }

    public void printTextBuffer() {
        enteredText.printDeque();
    }
}
