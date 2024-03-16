import java.util.concurrent.Semaphore;

public abstract class UserlandProcess implements Runnable {

    private boolean Quantum_Expired;
    private final Thread thread;
    public final Semaphore semaphore;

    public UserlandProcess(){
        this.Quantum_Expired = false;
        this.thread = new Thread(this);
        this.semaphore = new Semaphore(0);
        thread.start();
    }
    public void requestStop(){
        Quantum_Expired=true;
    }
    public abstract void main();
    public boolean isStopped(){
        return semaphore.availablePermits() == 0;
    }
    public boolean isDone(){
        return !this.thread.isAlive();
    }
    public void start(){
        semaphore.release();
    }
    public void stop() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void run(){
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        main();
    }
    public void cooperate(){
        if(Quantum_Expired){
            Quantum_Expired=false;
//            OS.SwitchProcess();
//            OS.Sleep(0);
            stop();
        }
    }


}
