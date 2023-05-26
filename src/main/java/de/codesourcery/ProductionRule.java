package de.codesourcery;

import java.util.ArrayList;
import java.util.List;

/**
 *  A production consists of two strings, the predecessor and the successor.
 *  For any symbol A which is a member of the set V which does not appear on the left hand side of a production in P,
 *  the identity production A → A is assumed;
 *  These symbols are called constants or terminals. (See Law of identity).
 */
public class ProductionRule
{
    public final Alphabet.Symbol expected;
    public final List<Alphabet.Symbol> replacement;

    public ProductionRule(Alphabet.Symbol expected, List<Alphabet.Symbol> replacement)
    {
        this.expected = expected;
        this.replacement = replacement;
    }

    public List<Alphabet.Symbol> apply(List<Alphabet.Symbol> input,int offset)
    {
        List<Alphabet.Symbol> result = new ArrayList<>(input.size() -1 + replacement.size());
        result.addAll( input.subList( 0, offset ) );
        result.addAll( offset, replacement );
        result.addAll( input.subList( offset + 1, input.size() ) );
        return result;
    }

    public boolean matches(Alphabet.Symbol symbol) {
        return symbol == expected;
    }

    @Override
    public String toString()
    {
        return expected + " -> " + replacement;
    }
}
