package de.codesourcery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A set of production rules or productions defining the way variables can be replaced with
 * combinations of constants and other variables.
 * A production consists of two strings, the predecessor and the successor.
 * For any symbol A which is a member of the set V which does not appear on the left hand side of a production in P,
 * the identity production A → A is assumed;
 * These symbols are called constants or terminals. (See Law of identity).
 */
public class ProductionRules
{
    public final List<ProductionRule> rules = new ArrayList<>();

    public List<Alphabet.Symbol> apply(List<Alphabet.Symbol> input)
    {
        List<Alphabet.Symbol> result = new ArrayList<>( Math.round( input.size() * 2.5f) );

outer:
        for ( int i = 0 ; i < input.size() ; i++ )
        {
            final Alphabet.Symbol symbol = input.get( i );
            if ( symbol.isConstant() ) {
                result.add( symbol );
                continue;
            }
            for ( ProductionRule rule : rules )
            {
                if ( rule.matches( symbol ) )
                {
                    result.addAll( rule.replacement );
                    continue outer;
                }
            }
        }
        return result;
    }

    public ProductionRules add(ProductionRule r1, ProductionRule... additional) {
        this.rules.add( r1 );
        Arrays.stream(additional).forEach( this.rules::add );
        return this;
    }
}
