package de.codesourcery;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ConfigPanel<T> extends JPanel
{
    private enum Mode {
        EDIT_NEW,
        EDIT_EXISTING;
    }

    public interface IModelConverter<T> {
        String toString(T value);
        T fromString(String value);
    }

    private enum Action {
        ADD, UPDATE, DELETE
    }

    private final DefaultListModel<T> listModel;
    private final IModelConverter<T> converter;

    public ConfigPanel(DefaultListModel<T> listModel, IModelConverter<T> converter)
    {
        this.listModel = listModel;
        this.converter = converter;
        setLayout( new GridBagLayout() );
        add( listPanel( "Symbols" ) );
    }

    private JComponent listPanel(String label)
    {
        final AtomicBoolean listSelectionListenerEnabled = new AtomicBoolean( true );

        final AtomicReference<Mode> mode = new AtomicReference<>(Mode.EDIT_NEW);
        final AtomicReference<Integer> indexBeingEdited = new AtomicReference<>();

        final IModel<T> tfModel = Model.of( null );
        final LabelledInput textField = inputWithLabel( label, tfModel );
        final JPanel inputPanel = new JPanel();
        inputPanel.setLayout( new FlowLayout() );
        inputPanel.add( textField.panel );

        final JButton addEditButton = new JButton( "Add" );

        final Runnable setAddMode = () -> {
            textField.textField.setText( null );
            tfModel.setObject( null );
            indexBeingEdited.set( -1 );
            addEditButton.setText( "Add" );
            mode.set( Mode.EDIT_NEW );
        };

        addEditButton.addActionListener( ev -> {

            final T newValue = tfModel.getObject();
            if ( newValue != null )
            {
                listSelectionListenerEnabled.set( false );
                try
                {
                    if ( mode.get() == Mode.EDIT_NEW )
                    {
                        System.out.println( "Add >" + newValue + "<" );
                        listModel.addElement( newValue );
                    }
                    else
                    {
                        System.out.println( "Edit >" + indexBeingEdited.get() + "< => >" + newValue + "<" );
                        final int idx = indexBeingEdited.get();
                        listModel.remove( idx );
                        listModel.add( idx, newValue );
                        setAddMode.run();
                    }
                }
                finally
                {
                    listSelectionListenerEnabled.set( true );
                }
            }
        });
        inputPanel.add( addEditButton );

        final JList list = new JList( listModel );
        list.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
        list.setPreferredSize( new Dimension( 200, 100 ) );

        final JPanel result = new JPanel();
        result.setLayout( new GridBagLayout() );
        result.add( inputPanel, cnstrs(0,0,2,1).build() );

        GridBagConstraints cnstrs = cnstrs( 0, 1, 1, 1 ).fill( GridBagConstraints.RELATIVE ).build();
        result.add( list, cnstrs );

        final JButton delButton = new JButton("Delete");
        delButton.addActionListener( ev -> {
            final int idx = list.getSelectedIndex();
            if ( idx != -1 ) {
                System.out.println("Removing at index "+idx);
                listModel.remove( idx );
                setAddMode.run();
            }
        });
        list.getSelectionModel().addListSelectionListener( event -> {
            if ( listSelectionListenerEnabled.get() )
            {
                final int idx = event.getFirstIndex();
                if ( idx >= 0 && idx < listModel.getSize() )
                {
                    System.out.println( "List selection changed to " + idx );
                    final T value = listModel.get( idx );
                    tfModel.setObject( value );

                    addEditButton.setText( "Apply" );
                    mode.set( Mode.EDIT_EXISTING );
                    indexBeingEdited.set( idx );
                    textField.textField.setText( converter.toString( value ) );
                }
            }
        } );
        result.add( delButton, cnstrs(1,1,1,1).fixedSize().build() );
        result.setBorder( BorderFactory.createTitledBorder( label ) );
        return result;
    }

    private record LabelledInput(JPanel panel, JTextField textField) {}

    private LabelledInput inputWithLabel(String label, IModel<T> model) {

        final JPanel p = new JPanel();
        p.setLayout( new FlowLayout() );
        final JLabel jLabel = new JLabel( label );
        p.add( jLabel );
        final JTextField tf = new JTextField();
        tf.getDocument().addDocumentListener( new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                notifyChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                notifyChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                notifyChange();
            }

            private void notifyChange() {
                final String text = tf.getText();
                model.setObject( converter.fromString( text ) );
            }
        } );
        tf.setColumns( 20 );
        tf.setText( converter.toString( model.getObject() ) );
        p.add( tf );
        return new LabelledInput( p, tf );
    }

    private static ConstraintsBuilders cnstrs(int x, int y, int width, int height)
    {
        return new ConstraintsBuilders( x, y, width, height );
    }

    public static void main(String[] args) throws InterruptedException, InvocationTargetException
    {
        SwingUtilities.invokeAndWait( () -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            DefaultListModel<String> model = new DefaultListModel<>();

            IModelConverter<String> converter = new IModelConverter<String>()
            {
                @Override
                public String toString(String value)
                {
                    return value;
                }

                @Override
                public String fromString(String value)
                {
                    return value;
                }
            };
            final ConfigPanel<String> p = new ConfigPanel<>( model , converter );
            p.setPreferredSize( new Dimension( 640, 480 ) );
            f.getContentPane().add( p );
            f.setLocationRelativeTo( null );
            f.pack();
            f.setVisible( true );
        } );
    }
}