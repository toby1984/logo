package de.codesourcery;

import java.util.List;

public interface LSystem
{
    List<Alphabet.Symbol> create(int iterationCount);

    LSystemRenderer createRenderer();
}
