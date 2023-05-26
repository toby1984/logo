package de.codesourcery;

import java.util.List;

public class LSystemCalculator
{
    private final List<Alphabet.Symbol> initialState;
    private final ProductionRules rules;

    public LSystemCalculator(List<Alphabet.Symbol> initialState, ProductionRules rules)
    {
        this.initialState = initialState;
        this.rules = rules;
    }

    public List<Alphabet.Symbol> calculate(int iterationCount)
    {
        List<Alphabet.Symbol> result = initialState;
        for ( int i = 1 ; i <= iterationCount ; i++ )
        {
            long start = System.nanoTime();
            result = rules.apply( result );
            long end = System.nanoTime();
            final float delta = (end-start)/1_000_000f;
            System.out.println( "Iteration " + i + " took " + delta + " ms" );
        }
        return result;
    }
}
