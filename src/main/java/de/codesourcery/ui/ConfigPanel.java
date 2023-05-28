package de.codesourcery.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.lang.reflect.InvocationTargetException;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ConfigPanel extends JPanel
{
    public ConfigPanel()
    {
        setLayout( new GridLayout( 2,1 ) );

        // symbols
        final DefaultListModel<String> symbolModel = new DefaultListModel<>();
        final ConfigPartPanel<String> symbols = new ConfigPartPanel<>( "Symbols", symbolModel, ConfigPartPanel.IModelConverter.of(
            x -> x, y -> y ) ) {
            @Override
            protected void validate(IValidatable<String> validatable)
            {
                final String v = validatable.getValue();
                if ( v == null || v.isBlank() || v.length() > 1 ) {
                    validatable.error( "Symbols must have exactly one character" );
                }
            }
        };
        add( symbols );

        // constants
        final DefaultListModel<String> constantsModel = new DefaultListModel<>();
        final ConfigPartPanel<String> constants = new ConfigPartPanel<>( "Constants", constantsModel, ConfigPartPanel.IModelConverter.of(
            x -> x, y -> y ) );
        add( constants );
    }

    public static void main(String[] args) throws InterruptedException, InvocationTargetException
    {
        SwingUtilities.invokeAndWait( () -> {
            final ConfigPanel p = new ConfigPanel();
            p.setPreferredSize( new Dimension( 640, 480 ) );
            JFrame f = new JFrame();
            f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            f.getContentPane().add( p );
            f.setLocationRelativeTo( null );
            f.pack();
            f.setVisible( true );
        } );
    }
}
