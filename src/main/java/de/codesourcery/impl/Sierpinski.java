package de.codesourcery.impl;

import java.util.List;
import de.codesourcery.Alphabet;
import de.codesourcery.LSystem;
import de.codesourcery.LSystemCalculator;
import de.codesourcery.LSystemRenderer;
import de.codesourcery.ProductionRule;
import de.codesourcery.ProductionRules;
import de.codesourcery.Turtle;

public class Sierpinski implements LSystem
{
    /*
    variables : F G
    constants : + −
    start  : F−G−G
    rules  : (F → F−G+F+G−F), (G → GG)
    angle  : 120°
     */

    private final Alphabet.Symbol F = Alphabet.Symbol.symbol( 'F' );
    private final Alphabet.Symbol G = Alphabet.Symbol.symbol( 'G' );

    private final Alphabet.Symbol plus = Alphabet.Symbol.constant( '+' );
    private final Alphabet.Symbol minus = Alphabet.Symbol.constant( '-' );

    private final List<Alphabet.Symbol> initial = List.of( F, minus, G, minus, G );

    private final ProductionRules rules = new ProductionRules();

    {
        rules.add(
            new ProductionRule( F, List.of( F, minus, G, plus, F, plus, G, minus, F ) ),
            new ProductionRule( G, List.of( G, G ) )
        );
    }
    @Override
    public List<Alphabet.Symbol> create(int iterationCount)
    {
        return new LSystemCalculator( initial, rules ).calculate( iterationCount );
    }

    @Override
    public LSystemRenderer createRenderer()
    {
        return new LSystemRenderer()
        {
            @Override
            public void render(List<Alphabet.Symbol> data, LSystem system, Turtle turtle)
            {
                // Here, F means "draw forward", G means "draw forward", + means "turn left by angle", and − means "turn right by angle".
                turtle.penDown();
                final float angle = 120;
                float len = 2;
                for ( final Alphabet.Symbol symbol : data )
                {
                    if ( symbol.matches( F ) ) {
                        turtle.forward( len );
                    } else if ( symbol.matches( G ) ) {
                        turtle.forward( len );
                    } else if ( symbol.matches( plus ) ) {
                        turtle.turnLeft( angle );
                    } else if ( symbol.matches( minus ) ) {
                        turtle.turnRight( angle );
                    } else {
                        throw new RuntimeException( "Unhandled symbol: " + symbol );
                    }
                }
            }
        };
    }
}
