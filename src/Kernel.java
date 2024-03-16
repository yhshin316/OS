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
                case getSenderPID:
                    OS.returnValue = scheduler.GetPid();
                    break;
                case getReceieverPID:
                    OS.returnValue = scheduler.GetPidByName((String) OS.parameters.getFirst());
                    break;
                case sendMessage:
                    scheduler.SendMessage((KernelMessage) OS.parameters.getFirst());
                    break;
                case getMessage:
                    OS.returnValue = scheduler.GetMessage();
                    break;
            }

            synchronized (this) {
                notify();
            }
        }
    }

//    public void GetPid() {
//        OS.returnValue = scheduler.GetPid();
//    }
//
//    public void GetPidByName(){
//        OS.returnValue = scheduler.GetPidByName((String) OS.parameters.getFirst());
//    }
//
//    public void SendMessage(){
//        scheduler.SendMessage((KernelMessage) OS.parameters.getFirst());
//    }
//
//    public void GetMessage(){
//        OS.returnValue = scheduler.GetMessage();
//    }
}
