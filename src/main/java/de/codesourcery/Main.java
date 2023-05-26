package de.codesourcery;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import de.codesourcery.impl.FractalTree;

import static java.lang.Math.round;

public class Main extends JFrame
{
    private static final boolean DEBUG = true;

    private final MyPanel panel = new MyPanel();

    public Main()
    {
        super( "Logo" );
        getContentPane().add( panel );
        setLocationRelativeTo( null );
        setPreferredSize( new Dimension( 640, 480 ) );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        pack();
        setVisible( true );
    }

    private static final class Bounds
    {
        public float xMin, yMin;
        public float xMax, yMax;

        public void update(float x, float y) {

            xMin = Math.min( xMin, x );
            xMax = Math.max( xMax, x );

            yMin = Math.min( yMin, y );
            yMax = Math.max( yMax, y );
        }

        /**
         * Returns an affine transform that centers this bounding box
         * around the (0,0) coordinate system origin.
         *
         * @return
         */
        public AffineTransform originTransform() {
            float dx = xMin + width() /2;
            float dy = yMin + height() / 2;
            final float tx = -dx;
            final float ty = -dy;
            System.out.println( this+" has origin transform ("+tx+", "+ty+")");
            return AffineTransform.getTranslateInstance( tx, ty );
        }

        @Override
        public String toString()
        {
            return "Bounds[ ("+xMin+","+yMin+") -> ("+xMax+", "+yMax+") ] = ( "+width()+" x "+height()+" )";
        }

        public float width() {
            return xMax - xMin;
        }

        public float height() {
            return yMax - yMin;
        }

        public void reset() {
            xMin = xMax = yMin = yMax = 0;
        }

        public void update(float x1, float y1, float x2, float y2) {
            update( x1, y1 );
            update( x2, y2 );
        }
    }

    private record Line(float x1, float y1, float x2, float y2) {

        public void updateBoundingBox(Bounds bounds) {
            bounds.update( x1, y1, x2, y2 );
        }
    }

    public static final class MyPanel extends JPanel implements Turtle
    {
        public float cursorX, cursorY;
        public float orientationInDegrees;
        public boolean penIsDown;

        private final float zoomIncrement = 0.6f;
        private float vpCenterX=0.5f, vpCenterY =0.5f;
        private float zoomFactor = 1.0f;

        private final Bounds bounds = new Bounds();
        private final List<Line> lines = new ArrayList<>();

        private Point dragStart;
        private Point2D.Float dragStartCenter;

        public MyPanel()
        {
            setFocusable( true );
            addMouseMotionListener( new MouseMotionAdapter()
            {
                @Override
                public void mouseDragged(MouseEvent e)
                {
                    if ( dragStart != null ) {
                        float dx = e.getPoint().x - dragStart.x;
                        float dy = e.getPoint().y - dragStart.y;
                        float percX = dx / getWidth();
                        float percY = dy / getHeight();
                        vpCenterX = dragStartCenter.x + percX;
                        vpCenterY = dragStartCenter.y + percY;
                        System.out.println( "CenterX = " + vpCenterX + " , CenterY = " + vpCenterY );
                        repaint();
                    }
                }
            } );
            addMouseListener( new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    super.mousePressed( e );
                    if ( e.getButton() == MouseEvent.BUTTON1 && dragStart == null ) {
                        System.out.println( "DRAG START" );
                        dragStart = new Point( e.getX(), e.getY() );
                        dragStartCenter = new Point2D.Float( vpCenterX, vpCenterY );
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e)
                {
                    if ( e.getButton() == MouseEvent.BUTTON1 && dragStart != null ) {
                        System.out.println( "DRAG END" );
                        dragStart = null;
                    }
                }
            } );
            addMouseWheelListener( new MouseAdapter()
            {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e)
                {
                    final int rotation = -e.getWheelRotation();
                    zoomFactor = Math.max(0, zoomFactor + zoomIncrement*rotation );
                    repaint();
                }
            } );
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent( g );

            // 1. transform that centers the data inside model space
            final AffineTransform op1 = bounds.originTransform();

            final float zoomedWidth = zoomFactor * getWidth();
            final float zoomedHeight = zoomFactor * getHeight();

            float sx = zoomedWidth / bounds.width();
            float sy = zoomedHeight / bounds.height();
            float s = Math.min( sx, sy );

            // 2. transform that scales to screen space
            final AffineTransform op2 = AffineTransform.getScaleInstance( s, s );

            final AffineTransform op3 = AffineTransform.getTranslateInstance( zoomedWidth * vpCenterX, zoomedHeight * vpCenterY );

            System.out.println( "screen space: " + getWidth() + " x " + getHeight() );
            System.out.println("Bounds: "+bounds+", sx: "+sx+", sy: "+sy);
            System.out.println( "Drawing " + lines.size() + " lines" );

            // center at origin and then scale to screen space
            op2.concatenate( op1 );
            // translate so that model space (0,0) is at the center of the screen
            op3.concatenate( op2 );

            AffineTransform transform = op3;

            long time = System.nanoTime();
            for ( Line line : lines )
            {
//                if ( DEBUG )
//                {
//                    System.out.println( "LINE: " + line );
//                }
                drawLine( line.x1, line.y1, line.x2, line.y2, g, transform );
            }
            long time2 = System.nanoTime();
            long elapsedMillis = (time2-time)/1_000_000;
            System.out.println( "Frame time: " + elapsedMillis + " ms" );
        }

        private static final float[] points = new float[4];

        private void drawLine(float x1, float y1, float x2, float y2, Graphics gfx, AffineTransform t)
        {
            points[0] = x1;
            points[1] = y1;
            points[2] = x2;
            points[3] = y2;
            t.transform( points, 0, points, 0, points.length/2 );
//            System.out.println("Mapped line ("+x1+", "+y1+") -> ("+x2+","+y2+") to" +
//                " ("+points[0]+", "+points[1]+") -> ("+points[2]+","+points[3]+")");
            gfx.drawLine( round( points[0] ), round( points[1] ), round( points[2] ), round( points[3] ) );

        }

        @Override
        public Turtle reset()
        {
            lines.clear();
            bounds.reset();

            cursorX = getWidth() / 2;
            cursorY = getHeight() / 2;
            orientationInDegrees = 0;
            penIsDown = false;
            return this;
        }

        @Override
        public Turtle penUp() {
            penIsDown = false;
            return this;
        }

        @Override
        public Turtle penDown() {
            penIsDown = true;
            return this;
        }

        @Override
        public Turtle forward(float len)
        {
            final float angleInRad = degToRad( orientationInDegrees-90 );
            final double dx = len * Math.cos( angleInRad );
            final double dy = len * Math.sin( angleInRad );

            float x = (float) (cursorX + dx);
            float y = (float) (cursorY + dy);

            if ( penIsDown ) {
                final Line l = new Line( cursorX, cursorY, x, y );
                l.updateBoundingBox( bounds );
                lines.add( l );
            }
            cursorX = x;
            cursorY = y;
            return this;
        }

        @Override
        public Turtle turnLeft(float angleInDeg) {
            return changeAngle( orientationInDegrees - angleInDeg );
        }

        @Override
        public Turtle turnRight(float angleInDeg) {
            return changeAngle( orientationInDegrees + angleInDeg );
        }

        @Override
        public Turtle recall(TurtleState state)
        {
            this.cursorX = state.location().x;
            this.cursorY = state.location().y;
            this.penIsDown = state.penIsDown();
            this.orientationInDegrees = state.orientationInDegrees();
            return this;
        }

        @Override
        public Turtle goTo(Point p) {
            this.cursorX = p.x;
            this.cursorY = p.y;
            return this;
        }

        @Override
        public TurtleState state() {
            return new TurtleState( new Point2D.Float( cursorX, cursorY ), penIsDown, orientationInDegrees );
        }

        public Turtle changeAngle(float newAngle) {
            while ( newAngle > 360 ) {
                newAngle -= 360;
            }
            while ( newAngle < 0 ) {
                newAngle += 360;
            }
            orientationInDegrees = newAngle;
            return this;
        }

        private static float degToRad(float deg) {
            return (float) (deg * Math.PI/180f);
        }
    }

    public static void main(String[] args) throws InterruptedException, InvocationTargetException
    {
        SwingUtilities.invokeAndWait( () ->
        {
            final Main f = new Main();

            final FractalTree tree = new FractalTree();
            final List<Alphabet.Symbol> data = tree.create(15);

            // final Turtle wrapper = TurtleSpy.wrap( f.turtle(), x -> System.out.println(x) );
            tree.createRenderer().render( data, tree, f.turtle() );

            f.panel.addKeyListener( new KeyAdapter()
            {
                @Override
                public void keyReleased(KeyEvent e)
                {
                    if ( e.getKeyCode() == KeyEvent.VK_SPACE ) {
                        // paint( f.panel );
                    }
                }
            });
            f.panel.requestFocus();
        });
    }

    public Turtle turtle() {
        return panel;
    }
}