package de.codesourcery;

import java.awt.geom.AffineTransform;

public final class LineBuffer
{
    private static final int FLOATS_PER_POINT = 2;
    private static final int FLOATS_PER_LINE = 2*FLOATS_PER_POINT;

    private final MyFloatBuffer buffer = new MyFloatBuffer( 1000*FLOATS_PER_LINE );

    public interface Visitor {
        void visitLine(float x1, float y1, float x2, float y2);
    }

    public int lineCount() {
        return buffer.size() / FLOATS_PER_LINE;
    }

    public void append(float x1, float y1, float x2, float y2) {

        buffer.append( x1, y1, x2, y2 );
    }

    public void clear() {
        buffer.clear();
    }

    public void transform(LineBuffer outputBuffer, AffineTransform transform, boolean compactDestinationIfPossible)
    {
        buffer.transformTo( outputBuffer.buffer, transform, compactDestinationIfPossible );
    }

    public void visit(LineBuffer.Visitor visitor) {
        buffer.visit( visitor );
    }
}