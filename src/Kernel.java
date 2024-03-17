import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class Kernel implements Runnable{

    private final Thread thread;
    private final Semaphore semaphore;
    private final Scheduler scheduler;

    public Kernel(){
        this.thread = new Thread(this);
        this.semaphore = new Semaphore(0);
        this.scheduler = new Scheduler();
        thread.start();
    }

    public void start(){
        semaphore.release();
    }

    @Override
    public void run() {
        while(true){
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //switch to tell kernel to do specific action based on the callType of OS.
            switch (OS.callType){
                case createProcess:
                    OS.returnValue = scheduler.CreateProcess((UserlandProcess) OS.parameters.getFirst(), (OS.priority) OS.parameters.get(1), (Integer) OS.parameters.get(2));
                    break;
                case switchProcess:
                    scheduler.SwitchProcess();
                    break;
                case sleep:
                    scheduler.Sleep((int)OS.parameters.getFirst());
                    break;
                    //get the pid of the current user land process
                case getSenderPID:
                    OS.returnValue = scheduler.GetPid();
                    break;
                    //get the PID for the user land process that has a specific name
                case getReceieverPID:
                    OS.returnValue = scheduler.GetPidByName((String) OS.parameters.getFirst());
                    break;
                    //send the message to the PCB that has the user land process name
                case sendMessage:
                    scheduler.SendMessage((KernelMessage) OS.parameters.getFirst());
                    break;
                    //get the message in the PCB of the userlandprocess
                case getMessage:
                    OS.returnValue = scheduler.GetMessage();
                    break;
                    //starts the user land process
                case startUserland:
                    scheduler.startUserLand();
            }

            //tells OS that kernel finished and is okay to continue
            synchronized (this) {
                notify();
            }
        }
    }

    //exist for testing purposes
    public Scheduler getScheduler() {
        return scheduler;
    }
}
