package editor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.ArrayList;


public class Editor extends Application {
    private static Group root;
    private static String filename;
    int windowWidth = 500;
    int windowHeight = 500;
    private static int cursorX = 5;
    private static int cursorY = 0;
    private static String fontName = "Verdana";
    private static int fontSize = 12;
    public static TextBuffer buffer = new TextBuffer();
    private static Text reference = new Text("");
    private final Rectangle cursor = new Rectangle(1, reference.getLayoutBounds().getHeight());
    ArrayDeque<Integer> newlineIndices = new ArrayDeque<>();

    /** An EventHandler to handle keys that get pressed. */
    private class KeyEventHandler implements EventHandler<KeyEvent> {

        public KeyEventHandler(final Group root, int windowWidth, int windowHeight) {
            reference.setFont(Font.font (fontName, fontSize));
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            if (keyEvent.isShortcutDown()) {
                if (keyEvent.getCode() == KeyCode.S) {
                    saveFile();
                } else if (keyEvent.getCode() == KeyCode.P) {
                    System.out.println(cursorX + ", " + cursorY);
                } else if (keyEvent.getCode() == KeyCode.PLUS || keyEvent.getCode() == KeyCode.EQUALS) {
                    fontSize += 4;
                    reference.setFont(Font.font(fontName, fontSize));
                    renderText(buffer.getCurrentPos());
                } else if (keyEvent.getCode() == KeyCode.MINUS) {
                    if (fontSize != 4) {
                        fontSize -= 4;
                        reference.setFont(Font.font(fontName, fontSize));
                        renderText(buffer.getCurrentPos());
                    }
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_TYPED) {
                // Use the KEY_TYPED event rather than KEY_PRESSED for letter keys, because with
                // the KEY_TYPED event, javafx handles the "Shift" key and associated
                // capitalization.

                String characterTyped = keyEvent.getCharacter();
                if (characterTyped.equals(" ")) {
                    Text space = new Text(" ");
                    buffer.addText(space);
                    renderText(buffer.getCurrentPos());
                } else if (characterTyped.equals("\r")) {
                    Text newline = new Text("\n");
                    buffer.addText(newline);
                    renderText(buffer.getCurrentPos());
                } else if (characterTyped.length() > 0 && characterTyped.charAt(0) != 8) {
                    // Ignore control keys, which have non-zero length, as well as the backspace key, which is
                    // represented as a character of value = 8 on Windows.
                    Text enteredChar = new Text(characterTyped);
                    buffer.addText(enteredChar);
                    renderText(buffer.getCurrentPos());
                    root.getChildren().add(enteredChar);
                    keyEvent.consume();
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                // Arrow keys should be processed using the KEY_PRESSED event, because KEY_PRESSED
                // events have a code that we can check (KEY_TYPED events don't have an associated
                // KeyCode).
                KeyCode code = keyEvent.getCode();
                if (code == KeyCode.UP) {
                    if (cursorY != 0) {
                        cursorY -= reference.getLayoutBounds().getHeight();
                        int closestPos = findClosestPosition(cursorX, cursorY);
                        cursorUpdate(closestPos);
                    }
                } else if (code == KeyCode.DOWN) {
                    if (cursorY < (buffer.get(buffer.size()-1).getY())) {
                        cursorY += Math.round(reference.getLayoutBounds().getHeight());
                        int closestPos = findClosestPosition(cursorX, cursorY);
                        cursorUpdate(closestPos);
                    }
                } else if (code == KeyCode.LEFT) {
                    boolean action = buffer.moveCurrentPosLeft();

                    if (!action) {
                        return;
                    }
                } else if (code == KeyCode.RIGHT) {
                    boolean action = buffer.moveCurrentPosRight();

                    if (!action) {
                        return;
                    }
                } else if (code == KeyCode.BACK_SPACE) {
                    Text deleted = buffer.deleteChar();

                    if (deleted != null) {
                        cursorX -= (int) Math.round(deleted.getLayoutBounds().getWidth());
                        renderText(buffer.getCurrentPos());

                        root.getChildren().remove(deleted);
                    }
                }
            }
            cursorUpdate(buffer.getCurrentPos());
        }
    }

    private void renderText(int originalPosition) {

        cursorX= 5;
        cursorY = 0;
        int lastSpacePosition = 0;
        newlineIndices = new ArrayDeque<Integer>();
        newlineIndices.addLast(0);

        for (int j = originalPosition; j > 0; j -= 1) {
            if (buffer.get(j).getText() == " ") {
                lastSpacePosition = j;
                break;
            }
        }

        for (int i = 0; i < buffer.size(); i += 1) {
            if (buffer.get(i).getText() == "\n") {
                buffer.get(i).setTextOrigin(VPos.TOP);
                buffer.get(i).setFont(Font.font(fontName, fontSize));
                buffer.get(i).setX(cursorX);
                buffer.get(i).setY(cursorY);

                cursorY += (int) Math.round(reference.getLayoutBounds().getHeight());
                cursorX = 5;
                newlineIndices.addLast(i+1);
            } else if (buffer.get(i).getText() == " ") {
                lastSpacePosition = i;

                buffer.get(i).setTextOrigin(VPos.TOP);
                buffer.get(i).setFont(Font.font(fontName, fontSize));
                buffer.get(i).setX(cursorX);
                buffer.get(i).setY(cursorY);
                cursorX += (int) Math.round(buffer.get(i).getLayoutBounds().getWidth());
            } else {
                if (cursorX + (int) Math.round(buffer.get(i).getLayoutBounds().getWidth()) > (windowWidth - 5)) {
                    i = lastSpacePosition;
                    cursorY += (int) Math.round(reference.getLayoutBounds().getHeight());
                    cursorX = 5;
                    newlineIndices.addLast(i+1);
                } else {
                    buffer.get(i).setTextOrigin(VPos.TOP);
                    buffer.get(i).setFont(Font.font(fontName, fontSize));
                    buffer.get(i).setX(cursorX);
                    buffer.get(i).setY(cursorY);
                    cursorX += (int) Math.round(buffer.get(i).getLayoutBounds().getWidth());
                }
            }
        }
        //cursorX = (int) buffer.get(originalPosition).getX();
        //cursorY = (int) buffer.get(originalPosition).getY();
    }

    private int findClosestPosition (double coordinateX, double coordinateY) {
        int closestIndex = 0;
        int closestPosY = 0;
        int closestPosX = 0;

        for (int i = 0; i < newlineIndices.size(); i += 1) {
            if (Math.abs(coordinateY - buffer.get(newlineIndices.get(i)).getY()) < Math.abs(coordinateY - closestPosY)) {
                closestPosY = (int) buffer.get(newlineIndices.get(i)).getY();
                closestIndex = newlineIndices.get(i);
            }
        }

        closestPosX = (int) buffer.get(closestIndex).getX();

        for (int k = closestIndex; buffer.get(k).getY() == closestPosY; k += 1) {
            if (Math.abs(coordinateX - buffer.get(k).getX()) < Math.abs(coordinateX - closestPosX)) {
                closestPosX = (int) buffer.get(k).getX();
                closestIndex = k;
            }
        }

        cursorX = (int) buffer.get(closestIndex).getX();
        cursorY = (int) buffer.get(closestIndex).getY();

        return closestIndex;
    }

    private void resetCursor() {
        cursorX = 5;
        cursorY = 0;
        cursor.setX(cursorX);
        cursor.setY(cursorY);
        buffer.changeCurrentPos(0);
    }

    private void cursorUpdate(int position) {

        buffer.changeCurrentPos(position);
        double textHeight = reference.getLayoutBounds().getHeight();
        cursor.setHeight(textHeight);

        cursorX = (int) buffer.get(position).getX();
        cursorY = (int) buffer.get(position).getY();

        // For rectangles, the position is the upper left corner.
        cursor.setX(cursorX);
        cursor.setY(cursorY);
    }

    private class CursorBlinkEventHandler implements EventHandler<ActionEvent> {
        private int currentColorIndex = 0;
        private Color[] boxColors =
                {Color.BLACK, Color.WHITE};

        CursorBlinkEventHandler() {
            changeColor();
        }

        private void changeColor() {
            cursor.setFill(boxColors[currentColorIndex]);
            currentColorIndex = (currentColorIndex + 1) % boxColors.length;
        }

        @Override
        public void handle(ActionEvent event) {
            changeColor();
        }
    }

    /** Makes the cursor blink every .5 seconds. */
    public void makeCursorBlink() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        CursorBlinkEventHandler cursorChange = new CursorBlinkEventHandler();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), cursorChange);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    /** An event handler that displays the current position of the mouse whenever it is clicked. */
    private class MouseClickEventHandler implements EventHandler<MouseEvent> {
        /** A Text object that will be used to print the current mouse position. */
        Text positionText;

        MouseClickEventHandler(Group root) {
            // For now, since there's no mouse position yet, just create an empty Text object.
            positionText = new Text("");
            // We want the text to show up immediately above the position, so set the origin to be
            // VPos.BOTTOM (so the x-position we assign will be the position of the bottom of the
            // text).
            positionText.setTextOrigin(VPos.BOTTOM);

            // Add the positionText to root, so that it will be displayed on the screen.
            root.getChildren().add(positionText);
        }


        @Override
        public void handle(MouseEvent mouseEvent) {
            // Because we registered this EventHandler using setOnMouseClicked, it will only called
            // with mouse events of type MouseEvent.MOUSE_CLICKED.  A mouse clicked event is
            // generated anytime the mouse is pressed and released on the same JavaFX node.
            double mousePressedX = mouseEvent.getX();
            double mousePressedY = mouseEvent.getY() - (reference.getLayoutBounds().getHeight()/2);

            int closestPos = findClosestPosition(mousePressedX, mousePressedY);

            cursorUpdate(closestPos);
        }
    }

    private void renderFromFile() {
        cursorX= 5;
        cursorY = 0;
        int lastSpacePosition = 0;
        newlineIndices = new ArrayDeque<Integer>();
        newlineIndices.addLast(0);

        for (int i = 0; i < buffer.size(); i += 1) {
            if (buffer.get(i).getText() == "\n") {
                buffer.get(i).setTextOrigin(VPos.TOP);
                buffer.get(i).setFont(Font.font(fontName, fontSize));
                buffer.get(i).setX(cursorX);
                buffer.get(i).setY(cursorY);

                cursorY += (int) Math.round(reference.getLayoutBounds().getHeight());
                cursorX = 5;
                newlineIndices.addLast(i+1);
            } else if (buffer.get(i).getText() == " ") {
                lastSpacePosition = i;

                buffer.get(i).setTextOrigin(VPos.TOP);
                buffer.get(i).setFont(Font.font(fontName, fontSize));
                buffer.get(i).setX(cursorX);
                buffer.get(i).setY(cursorY);
                cursorX += (int) Math.round(buffer.get(i).getLayoutBounds().getWidth());
            } else {
                if (cursorX + (int) Math.round(buffer.get(i).getLayoutBounds().getWidth()) > (windowWidth - 5)) {
                    i = lastSpacePosition;
                    cursorY += (int) Math.round(reference.getLayoutBounds().getHeight());
                    cursorX = 5;
                    newlineIndices.addLast(i+1);
                } else {
                    buffer.get(i).setTextOrigin(VPos.TOP);
                    buffer.get(i).setFont(Font.font(fontName, fontSize));
                    buffer.get(i).setX(cursorX);
                    buffer.get(i).setY(cursorY);
                    cursorX += (int) Math.round(buffer.get(i).getLayoutBounds().getWidth());
                }
            }
        }
        for (int i = 0; i < buffer.size(); i += 1) {
            root.getChildren().add(buffer.get(i));
        }
        resetCursor();
    }

    private void renderTextFromFile(int originalPosition) {
        cursorX= 5;
        cursorY = 0;
        int lastSpacePosition = 0;

        for (int j = originalPosition; j > 0; j -= 1) {
            if (buffer.get(j).getText() == " ") {
                lastSpacePosition = j;
                break;
            }
        }

        for (int i = 0; i < buffer.size(); i += 1) {
            if (buffer.get(i).getText() == "\n") {
                buffer.get(i).setTextOrigin(VPos.TOP);
                buffer.get(i).setFont(Font.font(fontName, fontSize));
                buffer.get(i).setX(cursorX);
                buffer.get(i).setY(cursorY);

                cursorY += (int) Math.round(reference.getLayoutBounds().getHeight());
                cursorX = 5;
            } else if (buffer.get(i).getText() == " ") {
                lastSpacePosition = i;

                buffer.get(i).setTextOrigin(VPos.TOP);
                buffer.get(i).setFont(Font.font(fontName, fontSize));
                buffer.get(i).setX(cursorX);
                buffer.get(i).setY(cursorY);
                cursorX += (int) Math.round(buffer.get(i).getLayoutBounds().getWidth());
            } else {
                if (cursorX + (int) Math.round(buffer.get(i).getLayoutBounds().getWidth()) > (windowWidth - 5)) {
                    i = lastSpacePosition;
                    cursorY += (int) Math.round(reference.getLayoutBounds().getHeight());
                    cursorX = 5;
                } else {
                    buffer.get(i).setTextOrigin(VPos.TOP);
                    buffer.get(i).setFont(Font.font(fontName, fontSize));
                    buffer.get(i).setX(cursorX);
                    buffer.get(i).setY(cursorY);
                    cursorX += (int) Math.round(buffer.get(i).getLayoutBounds().getWidth());
                }
            }
        }
    }

    private static void saveFile() {
        try {
            FileWriter writer = new FileWriter(filename);

            for (int i = 0; i < buffer.size(); i += 1) {
                char charRead = buffer.get(i).getText().charAt(0);
                writer.write(charRead);
            }

            System.out.println("Successfully saved file " + filename);

            writer.close();
        } catch (IOException ioException) {
            System.out.println("Error; exception was: " + ioException);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a Node that will be the parent of all things displayed on the screen.
        root = new Group();
        // The Scene represents the window: its height and width will be the height and width
        // of the window displayed.
        windowWidth = 500;
        windowHeight = 500;
        Scene scene = new Scene(root, windowWidth, windowHeight, Color.WHITE);

        // To get information about what keys the user is pressing, create an EventHandler.
        // EventHandler subclasses must override the "handle" function, which will be called
        // by javafx.
        EventHandler<KeyEvent> keyEventHandler =
                new KeyEventHandler(root, windowWidth, windowHeight);
        // Register the event handler to be called for all KEY_PRESSED and KEY_TYPED events.
        scene.setOnKeyTyped(keyEventHandler);
        scene.setOnKeyPressed(keyEventHandler);
        scene.setOnMouseClicked(new MouseClickEventHandler(root));

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenWidth,
                    Number newScreenWidth) {
                primaryStage.setWidth(newScreenWidth.intValue());
                windowWidth = newScreenWidth.intValue();
                renderText(buffer.getCurrentPos());
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(
                    ObservableValue<? extends Number> observableValue,
                    Number oldScreenHeight,
                    Number newScreenHeight) {
                windowHeight = newScreenHeight.intValue();
            }
        });

        cursor.setX(cursorX);
        root.getChildren().add(cursor);
        makeCursorBlink();

        renderFromFile();
        renderTextFromFile(buffer.getCurrentPos());
        resetCursor();

        primaryStage.setTitle("Text Editor");

        // This is boilerplate, necessary to setup the window where things are displayed.
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Expected usage: Editor <source or new filename>");
            System.exit(1);
        } else {
            filename = args[0];

            try {
                File inputFile = new File(filename);

                if (!inputFile.exists()) {
                    buffer.addText(new Text(""));
                    launch(filename);
                } else {
                    FileReader reader = new FileReader(inputFile);
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    int intRead = -1;

                    while ((intRead = bufferedReader.read()) != -1) {
                        char readChar = (char) intRead;
                        Text enteredChar;
                        if (readChar == ' ') {
                            enteredChar = new Text(" ");
                        } else if (readChar == '\n') {
                            enteredChar = new Text("\n");
                        } else {
                            String characterTyped = Character.toString(readChar);
                            enteredChar = new Text(characterTyped);
                        }

                        buffer.addText(enteredChar);
                    }

                    bufferedReader.close();
                    launch(filename);
                }
            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println("File not found! Exception was: " + fileNotFoundException);
            } catch (IOException ioException) {
                System.out.println("Error; exception was: " + ioException);
            }
        }
    }
}