package de.codesourcery;

import java.awt.Point;

public interface Turtle
{
    Turtle penUp();

    Turtle penDown();

    Turtle forward(float len);

    Turtle turnLeft(float angleInDeg);

    Turtle turnRight(float angleInDeg);

    Turtle goTo(Point p);

    TurtleState state();

    Turtle recall(TurtleState state);

    void repaint();

    Turtle reset();
}
