package sae.java.test.code;

/**
 * Author: Ralf Mitschke
 * Created: 29.08.11 12:42
 */
public class WorkerImpl extends AbstractWorker
{
    @Override
    protected void doWorkInternal()
    {
        System.out.println("working");
    }

    @Override
    protected boolean hasWork()
    {
        // this implementation always has work
        return true;
    }
}
