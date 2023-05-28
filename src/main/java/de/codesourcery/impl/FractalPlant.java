package de.codesourcery.impl;

import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import de.codesourcery.Alphabet;
import de.codesourcery.LSystem;
import de.codesourcery.LSystemCalculator;
import de.codesourcery.LSystemRenderer;
import de.codesourcery.ProductionRule;
import de.codesourcery.ProductionRules;
import de.codesourcery.ui.Turtle;
import de.codesourcery.ui.TurtleState;

public class FractalPlant implements LSystem
{
    private final Alphabet.Symbol X = Alphabet.Symbol.symbol( 'X' );
    private final Alphabet.Symbol F = Alphabet.Symbol.symbol( 'F' );

    private final Alphabet.Symbol plus = Alphabet.Symbol.constant( '+' );
    private final Alphabet.Symbol minus = Alphabet.Symbol.constant( '-' );
    private final Alphabet.Symbol bracketOpen = Alphabet.Symbol.constant( '[' );
    private final Alphabet.Symbol bracketClose = Alphabet.Symbol.constant( ']' );

    private final List<Alphabet.Symbol> inital = List.of( X );

    private final ProductionRules rules = new ProductionRules();

    {
        /*
Example 7: Fractal plant
See also: Barnsley fern

    variables : X F
    constants : + − [ ]
    start  : X
    rules  : (X → F+[[X]-X]-F[-FX]+X), (F → FF)
    angle  : 25°
         */

        final Function<Character,Alphabet.Symbol> lookup = c -> switch(c) {
            case 'X' -> X;
            case 'F' -> F;
            case '+' -> plus;
            case '-' -> minus;
            case '[' -> bracketOpen;
            case ']' -> bracketClose;
            default -> throw new IllegalArgumentException("Unhandled >"+c+"<");
        };

        rules.add(
            new ProductionRule( X, Alphabet.parse( "F+[[X]-X]-F[-FX]+X", lookup ) ),
            new ProductionRule( F, List.of( F, F ) )
        );
    }

    @Override
    public List<Alphabet.Symbol> create(int iterationCount)
    {
        return new LSystemCalculator(inital, rules).calculate( iterationCount );
    }

    @Override
    public LSystemRenderer createRenderer()
    {
        return new LSystemRenderer()
        {
            @Override
            public void render(List<Alphabet.Symbol> data, LSystem system, Turtle turtle)
            {
                final float angle = 25;
                final float len = 1;

                /*
                 * F means "draw forward", − means "turn right 25°", and + means "turn left 25°".
                 * X does not correspond to any drawing action and is used to control the evolution of the curve.
                 * The square bracket "[" corresponds to saving the current values for position and angle,
                 * which are restored when the corresponding "]" is executed.
                 */
                final Stack<TurtleState> stack = new Stack<>();

                turtle.penDown();
                for ( final Alphabet.Symbol symbol : data )
                {
                    if ( symbol.matches( X ) ) {
                        // nothing to do
                    } else if ( symbol.matches( F ) ) {
                        turtle.forward( len );
                    } else if ( symbol.matches( minus ) ) {
                        turtle.turnRight( angle );
                    } else if ( symbol.matches( plus ) ) {
                        turtle.turnLeft( angle );
                    } else if ( symbol.matches( bracketOpen ) ) {
                        stack.push( turtle.state() );
                    } else if ( symbol.matches( bracketClose ) ) {
                        turtle.recall( stack.pop() );
                    } else {
                        throw new IllegalArgumentException( "Unhandled symbol: " + symbol );
                    }
                }
            }
        };
    }
}
