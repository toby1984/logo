package de.codesourcery;

import java.awt.geom.AffineTransform;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class MyFloatBuffer
{
    private int floatsInBufferCount;
    private FloatBuffer buffer;

    public MyFloatBuffer(int initialCapacity) {
        if ( initialCapacity < 1 ) {
            throw new IllegalArgumentException();
        }
        this.buffer = createOffHeapFloatBuffer( initialCapacity );
    }

    public int capacity() {
        return buffer.capacity();
    }

    public void clear() {
        this.buffer.clear();
    }

    public int size() {
        return floatsInBufferCount;
    }

    public void shrinkToSize(int size)
    {
        final FloatBuffer newBuffer = createOffHeapFloatBuffer( size );
        newBuffer.put( 0 , buffer , 0 , size );
        buffer = newBuffer;
        this.floatsInBufferCount = size;
    }

    private static FloatBuffer createOffHeapFloatBuffer(int floatCount) {
        final ByteBuffer bb = ByteBuffer.allocateDirect( floatCount * 4 );
        return bb.asFloatBuffer();
    }

    public void assertCapacity(int requiredSize)
    {
        if ( requiredSize > buffer.capacity() ) {
            final FloatBuffer newBuffer = createOffHeapFloatBuffer( (int) Math.ceil( Math.max( requiredSize , floatsInBufferCount*1.5f ) ) );
            newBuffer.put( 0 , buffer , 0 , floatsInBufferCount );
            buffer = newBuffer;
            buffer.position( this.floatsInBufferCount );
        }
    }

    public void append(float x1, float y1, float x2, float y2) {

        assertCapacity( floatsInBufferCount + 4 );
        buffer.put( x1 );
        buffer.put( y1 );
        buffer.put( x2 );
        buffer.put( y2 );
        // System.out.println("ADD LINE: ("+x1+", "+y1+") -> ("+x2+", "+y2+")");
        floatsInBufferCount += 4;
    }

    public void visit(LineBuffer.Visitor visitor) {

        final float[] tmp = new float[4];
        buffer.rewind();
        for ( int i = 0; i < floatsInBufferCount; i+=4 ) {
            buffer.get(tmp);
            visitor.visitLine( tmp[0], tmp[1], tmp[2], tmp[3] );
        }
    }

    /**
     *
     * @param destination
     * @param shrinkDestinationIfPossible
     */
    public void copyTo(MyFloatBuffer destination, boolean shrinkDestinationIfPossible)
    {
        if ( destination.capacity() < floatsInBufferCount )
        {
            destination.assertCapacity( floatsInBufferCount );
        } else if ( shrinkDestinationIfPossible && destination.capacity() > floatsInBufferCount ) {
            destination.shrinkToSize( floatsInBufferCount );
        }
        destination.buffer.put( 0, this.buffer, 0, floatsInBufferCount );
    }

    public void transformTo(MyFloatBuffer destination, AffineTransform transform, boolean compactDestinationIfPossible)
    {
        if ( destination.capacity() > floatsInBufferCount ) {
            if ( compactDestinationIfPossible )
            {
                destination.shrinkToSize( floatsInBufferCount );
            }
        } else if ( destination.capacity() < floatsInBufferCount ) {
            destination.assertCapacity( floatsInBufferCount );
        }

        // Create tmp buffer for processing 1000 points at once
        // note: size must be a multiple of 2 as we're going to
        //       call AffineTransform#transform() with the tmp array which
        //       assumes the array contains 2 floats per point
        final float[] block = new float[10000*2];

        destination.floatsInBufferCount = this.floatsInBufferCount;

        destination.buffer.rewind();
        buffer.rewind();

//        System.out.println( "==== transform ====" );
        int floatsRemaining = floatsInBufferCount;
        while ( floatsRemaining > 0 )
        {
            int floatsInBlock = Math.min( floatsRemaining, block.length );
            buffer.get( block, 0, floatsInBlock );
            transform.transform( block, 0,block,0, floatsInBlock / 2 );
            destination.buffer.put( block, 0, floatsInBlock );
            floatsRemaining -= floatsInBlock;
        }
    }
}
