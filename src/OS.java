import java.util.ArrayList;

public class OS {

    private static final Kernel kernel = new Kernel();
    public static ArrayList<Object> parameters = new ArrayList<>();
    public static currentCall callType;
    public static Object returnValue;
    public static priority priority;

    public enum currentCall {
        createProcess,
        switchProcess,
        sleep,
        getSenderPID,
        getReceieverPID,
        sendMessage,
        getMessage
    }

    public enum priority {
        realTime,
        interActive,
        background
    }

    public static void CreateProcess(UserlandProcess up) {
        parameters.clear();
        parameters.add(up);
        callType = currentCall.createProcess;
        kernel.start();
        waitForKernel();
        System.out.println("Created"+returnValue.toString());
    }

    public static void CreateProcessPCB(UserlandProcess up, OS.priority priority, int time) {
        parameters.clear();
        parameters.add(up);
        parameters.add(priority);
        parameters.add(time);
        callType = currentCall.createProcess;
        kernel.start();
        waitForKernel();
        System.out.println("Created: "+up.getClass().getSimpleName()+" "+returnValue.toString());
    }

    public static void SwitchProcess() {
        parameters.clear();
        callType = currentCall.switchProcess;
        System.out.println("Switched");
        kernel.start();
        waitForKernel();
    }

    public static void Startup(UserlandProcess up) {
        CreateProcess(up);

        CreateProcess(new IdleProcess());
    }

    public static void StartupPCB(UserlandProcess up, OS.priority priority, int time) {
        CreateProcessPCB(up, priority, time);

        CreateProcessPCB(new IdleProcess(), OS.priority.background, time);
    }

    public static void Sleep(int ms) {
        parameters.clear();
        parameters.add(ms);
        callType = currentCall.sleep;
        System.out.println("Sleep");
        kernel.start();
        waitForKernel();
    }

    public static void waitForKernel() {
        try {
            synchronized (kernel) {
                kernel.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void GetPid(){
        parameters.clear();
        callType = currentCall.getSenderPID;
        System.out.println("Sender Pid");
        kernel.start();
        waitForKernel();
    }

    public static void GetPidByName(String name){
        parameters.clear();
        parameters.add(name);
        callType = currentCall.getReceieverPID;
        System.out.println("Receiver Pid");
        kernel.start();
        waitForKernel();
    }

    public static void SendMessage(KernelMessage km){
        parameters.clear();
        parameters.add(km);
        callType = currentCall.sendMessage;
        System.out.println("Send Message");
        kernel.start();
        waitForKernel();
    }

    public static void GetMessage(){
        parameters.clear();
        callType = currentCall.getMessage;
        System.out.println("Retrieving Message");
        kernel.start();
        waitForKernel();
    }
}
