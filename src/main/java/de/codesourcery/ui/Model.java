package de.codesourcery.ui;

public class Model<T> implements IModel<T>
{
    private T value;

    public Model()
    {
    }

    public Model(T value)
    {
        this.value = value;
    }

    @Override
    public T getObject()
    {
        return value;
    }

    @Override
    public void setObject(T object)
    {
        this.value = object;
    }

    public static <T> Model<T> of(T value) {
        return new Model<>( value );
    }
}
