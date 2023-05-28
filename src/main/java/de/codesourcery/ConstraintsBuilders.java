package de.codesourcery;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class ConstraintsBuilders
{
    private final GridBagConstraints cnstrs = new GridBagConstraints();

    public ConstraintsBuilders(int x, int y, int gridWidth, int gridHeight) {
        cnstrs.gridx = x;
        cnstrs.gridy = y;
        cnstrs.gridwidth = gridWidth;
        cnstrs.gridheight = gridHeight;
        cnstrs.fill = GridBagConstraints.BOTH;
        cnstrs.insets = new Insets( 5, 5, 5, 5 );
    }

    public ConstraintsBuilders weightX(double w) {
        cnstrs.weightx = w;
        return this;
    }

    public ConstraintsBuilders weightY(double w) {
        cnstrs.weighty = w;
        return this;
    }

    public ConstraintsBuilders fixedSize() {
        return weightX( 0 ).weightY( 0 ).fill(GridBagConstraints.NONE);
    }

    public ConstraintsBuilders fill(int fill) {
        cnstrs.fill = fill;
        return this;
    }

    public GridBagConstraints build() {
        return cnstrs;
    }
}
