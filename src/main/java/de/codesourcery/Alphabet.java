package de.codesourcery;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The alphabet is a set of symbols containing both elements that can be replaced (variables)
 * and those which cannot be replaced ("constants" or "terminals").
 *
 * @author tobias.gierke@code-sourcery.de
 */
public class Alphabet
{
    public static class Symbol {

        public final char symbol;
        private final boolean isConstant;

        public Symbol(char symbol) {
            this( symbol, false );
        }

        public Symbol(char symbol, boolean isConstant)
        {
            this.symbol = symbol;
            this.isConstant = isConstant;
        }

        public boolean isConstant() {
            return isConstant;
        }

        public boolean matches(Symbol other) {
            return this == other || (this.symbol == other.symbol && this.isConstant == other.isConstant);
        }

        @Override
        public String toString()
        {
            return String.valueOf( symbol );
        }

        public static Symbol symbol(char c) {
            return new Symbol( c );
        }
        public static Symbol constant(char c) {
            return new Symbol( c, true );
        }
    }

    public static List<Symbol> parse(String s, Function<Character,Symbol> lookup) {

        s = s.trim().replaceAll( " ", "" );
        final List<Symbol> result = new ArrayList<>();
        for ( final char c : s.toCharArray() )
        {
            result.add( lookup.apply( c ) );
        }
        return result;
    }
}
