package editor;

import javafx.scene.text.Text;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TextBufferTest {
    @Test
    public void someTest() {
        TextBuffer tb = new TextBuffer();
        tb.addText(new Text("a"));
        tb.addText(new Text("b"));
        tb.addText(new Text("c"));
        assertEquals("a", tb.get(0));
        assertEquals("b", tb.get(1));
        assertEquals("c", tb.get(2));
        assertEquals(3, tb.getCurrentPos());
        tb.deleteChar();
        assertEquals(2, tb.getCurrentPos());
        tb.deleteChar();
        assertEquals(1, tb.getCurrentPos());
        tb.deleteChar();
        assertEquals(0, tb.getCurrentPos());
        tb.deleteChar();
        assertEquals(0, tb.getCurrentPos());

        tb.addText(new Text("a"));
        tb.addText(new Text("b"));
        tb.addText(new Text("c"));
        assertEquals(3, tb.getCurrentPos());
        tb.moveCurrentPosLeft();
        tb.deleteChar();
        assertEquals(1, tb.getCurrentPos());
        assertEquals("c", tb.get(1));
        tb.deleteChar();
        assertEquals(0, tb.getCurrentPos());
        tb.deleteChar();
        assertEquals(0, tb.getCurrentPos());
        assertEquals("c", tb.get(0));
    }

    /** Calls tests for ArrayRingBuffer. */
    public static void main(String[] args) {
        jh61b.junit.textui.runClasses(TextBufferTest.class);
    }

}