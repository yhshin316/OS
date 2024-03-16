import java.time.Clock;
import java.time.Instant;
import java.util.LinkedList;

public class PCB {
    private UserlandProcess userlandProcess;
    private static int nextPID;
    private int PID;
    private Instant wakeUp;
    private OS.priority priority;
    private int demotion;
    private LinkedList<KernelMessage> messages;

    public PCB(UserlandProcess up,OS.priority priority, int waitTime){
        this.userlandProcess = up;
        this.priority= priority;
        this.wakeUp = Clock.systemDefaultZone().instant().plusMillis(waitTime);
        this.messages = new LinkedList<>();
        PID = up.hashCode();
    }

    public void stop(){
        demoter();
        OS.Sleep(0);
        userlandProcess.requestStop();
    }

    public boolean isDone(){
        return userlandProcess.isDone();
    }

    public void run(){
        userlandProcess.start();
    }

    public void updateWaitTime(int waitTime){
        this.wakeUp = Clock.systemDefaultZone().instant().plusMillis(waitTime);
    }

    public void demoter(){
        demotion++;
        if(demotion==5){
            //real time to interactive when counter is 5
            if(priority == OS.priority.realTime){
                priority = OS.priority.interActive;
                demotion = 0;
                //ineractive to background at 5 counter
            } else if (priority == OS.priority.interActive) {
                priority = OS.priority.background;
                demotion = 0;
                //just reset as it cannot go lower
            }else{
                demotion = 0;
            }
        }
    }

    public void AddMessage(KernelMessage message){
        System.out.println(this.userlandProcess.getClass().getSimpleName()+": Got the message: "+message);
        messages.add(message);
    }

    public void clearMessage(){
        messages.clear();
    }

    public UserlandProcess getUserlandProcess() {
        return userlandProcess;
    }

    public int getPID() {
        return PID;
    }

    public Instant getWakeUp() {
        return wakeUp;
    }

    public OS.priority getPriority() {
        return priority;
    }

    public LinkedList<KernelMessage> getMessages() {
        return messages;
    }
}
