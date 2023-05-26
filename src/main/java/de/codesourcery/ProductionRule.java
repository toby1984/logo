package de.codesourcery;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *  A production consists of two strings, the predecessor and the successor.
 *  For any symbol A which is a member of the set V which does not appear on the left hand side of a production in P,
 *  the identity production A → A is assumed;
 *  These symbols are called constants or terminals. (See Law of identity).
 */
public class ProductionRule
{
    public final Alphabet.Symbol predecessor; // expected term
    public final List<Alphabet.Symbol> successor; // replacement

    public ProductionRule(Alphabet.Symbol predecessor, List<Alphabet.Symbol> successor)
    {
        this.predecessor = predecessor;
        this.successor = successor;
    }

    public List<Alphabet.Symbol> apply(List<Alphabet.Symbol> input,int offset)
    {
        List<Alphabet.Symbol> result = new ArrayList<>(input.size() -1 + successor.size());
        result.addAll( input.subList( 0, offset ) );
        result.addAll( offset, successor );
        result.addAll( input.subList( offset + 1, input.size() ) );
        return result;
    }

    public boolean matches(List<Alphabet.Symbol> input, int offset) {
        return input.get( offset ) == predecessor;
    }

    @Override
    public String toString()
    {
        return predecessor + " -> " + successor;
    }
}
