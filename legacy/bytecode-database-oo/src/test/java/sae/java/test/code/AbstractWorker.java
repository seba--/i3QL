package sae.java.test.code;

/**
 * Author: Ralf Mitschke
 * Created: 29.08.11 12:42
 */
public abstract class AbstractWorker implements IWorker
{

    public void doWork()
    {
        if( hasWork() )
            doWorkInternal();
    }


    protected abstract  void doWorkInternal();

    protected abstract boolean hasWork();
}
