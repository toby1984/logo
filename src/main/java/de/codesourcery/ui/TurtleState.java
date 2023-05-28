package de.codesourcery.ui;

import java.awt.geom.Point2D;

public record TurtleState(Point2D.Float location, boolean penIsDown, float orientationInDegrees) { }
