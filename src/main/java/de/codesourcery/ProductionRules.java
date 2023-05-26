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
        List<Alphabet.Symbol> tmp = input;

        for ( int i = 0 ; i < tmp.size() ; i++ )
        {
            for ( ProductionRule rule : rules )
            {
                if ( ! tmp.get(i).isConstant() )
                {
                    if ( rule.matches( tmp, i ) )
                    {
                        final int oldLen = tmp.size();
                        tmp = rule.apply( tmp, i );
                        if ( oldLen < tmp.size() )
                        {
                            i += (tmp.size() - oldLen);
                        }
                        break;
                    }
                }
            }
        }
        return tmp;
    }

    public ProductionRules add(ProductionRule r1, ProductionRule... additional) {
        this.rules.add( r1 );
        Arrays.stream(additional).forEach( this.rules::add );
        return this;
    }
}
