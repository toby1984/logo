package de.codesourcery;

import java.awt.Point;
import java.util.function.Consumer;

public class TurtleSpy implements Turtle
{
    private final Turtle delegate;
    private final Consumer<String> out;

    public TurtleSpy(Turtle delegate, Consumer<String> out)
    {
        this.delegate = delegate;
        this.out = out;
    }

    private void print(String msg) {
        out.accept( msg );
    }

    @Override
    public Turtle penUp()
    {
        print( "penUp()" );
        return delegate.penUp();
    }

    @Override
    public Turtle penDown()
    {
        print( "penDown()" );
        return delegate.penDown();
    }

    @Override
    public Turtle forward(float len)
    {
        print( "forward(" + len + ")" );
        return delegate.forward( len );
    }

    @Override
    public Turtle turnLeft(float angleInDeg)
    {
        print( "turnLeft(" + angleInDeg + ")" );
        return delegate.turnLeft( angleInDeg );
    }

    @Override
    public Turtle turnRight(float angleInDeg)
    {
        print( "turnRight(" + angleInDeg + ")" );
        return delegate.turnRight( angleInDeg );
    }

    @Override
    public Turtle goTo(Point p)
    {
        print( "goTo(" + p + ")" );
        return delegate.goTo( p );
    }

    @Override
    public TurtleState state()
    {
        print( "state()" );
        return delegate.state();
    }

    @Override
    public Turtle recall(TurtleState state)
    {
        print( "recall()" );
        return delegate.recall( state );
    }

    @Override
    public void repaint()
    {
        print( "repaint()" );
        delegate.repaint();
    }

    @Override
    public Turtle reset()
    {
        print( "reset()" );
        delegate.reset();
        return this;
    }

    static Turtle wrap(Turtle t, Consumer<String> out) {
        return new TurtleSpy( t, out );
    }
}