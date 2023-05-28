package de.codesourcery;

import java.util.List;
import de.codesourcery.ui.Turtle;

public interface LSystemRenderer
{
    void render(List<Alphabet.Symbol> data, LSystem system, Turtle turtle);
}
