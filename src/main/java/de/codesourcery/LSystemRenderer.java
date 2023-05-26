package de.codesourcery;

import java.util.List;

public interface LSystemRenderer
{
    void render(List<Alphabet.Symbol> data, LSystem system, Turtle turtle);
}
