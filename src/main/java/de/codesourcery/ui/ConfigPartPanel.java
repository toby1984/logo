package de.codesourcery.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ConfigPartPanel<T> extends JPanel
{
  private enum Mode
  {
    EDIT_NEW,
    EDIT_EXISTING;
  }

  public interface IModelConverter<T>
  {
    String toString(T value);

    T fromString(String value);

    static <T> IModelConverter<T> of(Function<T, String> toString, Function<String, T> fromString)
    {
      return new IModelConverter<T>()
      {
        @Override
        public String toString(T value)
        {
          return toString.apply( value );
        }

        @Override
        public T fromString(String value)
        {
          return fromString.apply( value );
        }
      };
    }
  }

  private final DefaultListModel<T> listModel;
  private final IModelConverter<T> converter;

  private boolean validationEnabled = true;
  private boolean isInError;

  private JButton addEditButton;

  public ConfigPartPanel(String label, DefaultListModel<T> listModel, IModelConverter<T> converter)
  {
    this.listModel = listModel;
    this.converter = converter;
    setLayout( new GridBagLayout() );
    add( createPanel( label ) );
  }

  private void doWithoutValidation(Runnable cb)
  {
    validationEnabled = false;
    try
    {
      cb.run();
    }
    finally
    {
      validationEnabled = true;
    }
  }

  private JComponent createPanel(String label)
  {
    final AtomicBoolean listSelectionListenerEnabled = new AtomicBoolean( true );

    final AtomicReference<Mode> mode = new AtomicReference<>( Mode.EDIT_NEW );
    final AtomicReference<Integer> indexBeingEdited = new AtomicReference<>();

    final IModel<T> tfModel = Model.of( null );
    final JTextField textField = inputWithLabel( tfModel );
    final JPanel inputPanel = new JPanel();
    inputPanel.setLayout( new FlowLayout() );
    inputPanel.add( textField );

    addEditButton = new JButton( "Add" );

    final Runnable setAddMode = () -> {
      doWithoutValidation( () -> {
        textField.setText( null );
        tfModel.setObject( null );
        indexBeingEdited.set( -1 );
        addEditButton.setText( "Add" );
        setInError( false );
        mode.set( Mode.EDIT_NEW );
      } );
    };

    addEditButton.addActionListener( ev ->
    {
      final T newValue = tfModel.getObject();
      if ( newValue != null )
      {
        listSelectionListenerEnabled.set( false );
        try
        {
          if ( mode.get() == Mode.EDIT_NEW )
          {
            listModel.addElement( newValue );
          }
          else
          {
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
    } );
    inputPanel.add( addEditButton );

    final JList list = new JList( listModel );
    list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    list.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
    list.setPreferredSize( new Dimension( 200, 100 ) );

    final JPanel result = new JPanel();
    result.setLayout( new GridBagLayout() );
    result.add( inputPanel, cnstrs( 0, 0, 2, 1 ).build() );

    result.add( list, cnstrs( 0, 1, 1, 1 ).fill( GridBagConstraints.RELATIVE ).build() );

    final JButton delButton = new JButton( "Delete" );
    delButton.addActionListener( ev -> {
      final int idx = list.getSelectedIndex();
      if ( idx != -1 )
      {
        doWithoutValidation( () -> {
          listModel.remove( idx );
          setAddMode.run();
        } );
      }
    } );
    list.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    list.getSelectionModel().addListSelectionListener( event -> {
      if ( listSelectionListenerEnabled.get() )
      {
        final int[] selection = list.getSelectedIndices();
        final int idx = selection.length == 0 ? -1 : selection[0];
        if ( idx >= 0 && idx < listModel.getSize() )
        {
          doWithoutValidation( () -> {
            final T value = listModel.get( idx );
            tfModel.setObject( value );
            setInError( false );
            addEditButton.setText( "Apply" );
            mode.set( Mode.EDIT_EXISTING );
            indexBeingEdited.set( idx );
            textField.setText( converter.toString( value ) );
          } );
        }
      }
    } );
    result.add( delButton, cnstrs( 1, 1, 1, 1 ).fixedSize().build() );
    result.setBorder( BorderFactory.createTitledBorder( label ) );
    return result;
  }

  private void setInError(boolean yesNo)
  {
    isInError = yesNo;
    addEditButton.setEnabled( !isInError );
  }

  private JTextField inputWithLabel(IModel<T> model)
  {
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

      private void notifyChange()
      {
        final String text = tf.getText();
        if ( text == null ) {
          model.setObject( null );
          return;
        }
        List<String> errors = new ArrayList<>();
        final IConvertible<T> convertible = new IConvertible<T>()
        {
          @Override
          public String getRawValue()
          {
            return text;
          }

          @Override
          public void error(String message)
          {
            errors.add( message );
          }
        };
        Optional<T> converted = convert( convertible );
        if ( converted == null || converted.isPresent() || !validationEnabled )
        {
          if ( validationEnabled )
          {
            final IValidatable<T> v = new IValidatable<T>()
            {
              @Override
              public T getValue()
              {
                return converted == null ? null : converted.get();
              }

              @Override
              public void error(String message)
              {
                errors.add( message );
              }
            };
            validate( v );
          }
          if ( errors.isEmpty() || !validationEnabled )
          {
            model.setObject( converted == null || converted.isEmpty() ? null : converted.get() );
            setInError( false );
          }
          else
          {
            if ( !isInError )
            {
              setInError( true );
              JOptionPane.showMessageDialog( null, "Invalid input: " + errors.stream().collect( Collectors.joining( ", " ) ) );
            }
          }
        }
      }
    } );
    tf.setColumns( 20 );
    tf.setText( converter.toString( model.getObject() ) );
    return tf;
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

      final IModelConverter<String> converter = IModelConverter.of( x -> x, y -> y );
      final ConfigPartPanel<String> p = new ConfigPartPanel<>( "Symbols", model, converter );
      p.setPreferredSize( new Dimension( 640, 480 ) );
      f.getContentPane().add( p );
      f.setLocationRelativeTo( null );
      f.pack();
      f.setVisible( true );
    } );
  }

  protected interface IValidatable<T>
  {
    T getValue();

    void error(String message);
  }

  protected interface IConvertible<T>
  {
    String getRawValue();

    void error(String message);
  }

  /**
   * @param input
   * @return <code>null</code> if the conversion returned null, <code>Optional.empty()</code>
   * on conversion errors, otherwise converted value
   */
  protected Optional<T> convert(IConvertible<T> input)
  {
    try
    {
      final T result = converter.fromString( input.getRawValue() );
      return result == null ? null : Optional.of( result );
    }
    catch( Exception ex )
    {
      input.error( ex.getMessage() );
      return Optional.empty();
    }
  }

  protected void validate(IValidatable<T> validatable)
  {
  }
}