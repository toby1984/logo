package de.codesourcery.impl;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import de.codesourcery.Alphabet;
import de.codesourcery.LSystem;
import de.codesourcery.LSystemRenderer;
import de.codesourcery.ProductionRule;
import de.codesourcery.ProductionRules;
import de.codesourcery.TurtleState;

public class FractalTree implements LSystem
{
    private final Alphabet.Symbol zero = Alphabet.Symbol.symbol( '0' );
    private final Alphabet.Symbol one = Alphabet.Symbol.symbol( '1' );
    private final Alphabet.Symbol bracketOpen = Alphabet.Symbol.constant( '[' );
    private final Alphabet.Symbol bracketClose = Alphabet.Symbol.constant( ']' );

    @Override
    public List<Alphabet.Symbol> create(int iterationCount)
    {
        /*
    variables : 0, 1
    constants: “[”, “]”
    axiom  : 0
    rules  : (1 → 11), (0 → 1[0]0)
         */

        List<Alphabet.Symbol> initialState = List.of( zero );
        ProductionRules rules =new ProductionRules();
        rules.add(
            new ProductionRule(  one, List.of( one, one ) ),
            new ProductionRule( zero, List.of( one, bracketOpen, zero, bracketClose, zero ) )
        );
        List<Alphabet.Symbol> result = initialState;

        for ( int i = 1 ; i <= iterationCount ; i++ )
        {
            result = rules.apply( result );
            System.out.println( i+": "+result.stream().map(x->x.toString()).collect( Collectors.joining()) );
        }
        return result;
    }

    @Override
    public LSystemRenderer createRenderer()
    {
        return (data, system, turtle) -> {
            /*
0: draw a line segment ending in a leaf
1: draw a line segment
[: push position and angle, turn left 45 degrees
]: pop position and angle, turn right 45 degrees
             */
            final float lineLen = 1f;
            turtle.penDown();

            final Stack<TurtleState> stack = new Stack<>();

            for ( int i = 0; i < data.size(); i++ )
            {
                final Alphabet.Symbol symbol = data.get( i );
                if ( zero.matches( symbol ) ) {
                    turtle.forward( lineLen );
                    final TurtleState junction = turtle.state();
                    turtle.turnLeft( 20 ).forward( lineLen / 2 );
                    turtle.recall( junction );
                    turtle.turnRight( 20 ).forward( lineLen / 2 );
                } else if ( one.matches( symbol ) ) {
                    turtle.forward( lineLen );
                }
                else if ( bracketOpen.matches( symbol ) ) {
                    stack.push( turtle.state() );
                    turtle.turnLeft( 45 );
                } else if ( bracketClose.matches( symbol ) ) {
                    turtle.recall( stack.pop() );
                    turtle.turnRight( 45 );
                } else {
                    throw new UnsupportedOperationException( "Unknown symbol: " + symbol );
                }
            }
        };
    }
}
