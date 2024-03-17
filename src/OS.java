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
        getMessage,
        startUserland
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
    }

    public static void CreateProcessPCB(UserlandProcess up, OS.priority priority, int time) {
        parameters.clear();
        parameters.add(up);
        parameters.add(priority);
        parameters.add(time);
        callType = currentCall.createProcess;
        kernel.start();
        waitForKernel();
    }

    public static void SwitchProcess() {
        parameters.clear();
        callType = currentCall.switchProcess;
        kernel.start();
        waitForKernel();
    }

    public static void Startup(UserlandProcess up) {
        CreateProcess(up);

        CreateProcess(new IdleProcess());

        Sleep(0);
    }

    public static void StartupPCB(UserlandProcess up, OS.priority priority, int time) {
        CreateProcessPCB(up, priority, time);

        CreateProcessPCB(new IdleProcess(), OS.priority.background, time);

        Sleep(0);
    }

    public static void Sleep(int ms) {
        parameters.clear();
        parameters.add(ms);
        callType = currentCall.sleep;
        kernel.start();
        waitForKernel();
    }

    //waiting for the Kernel to finish to prevent other OS functions to override the parameters
    public static void waitForKernel() {
        try {
            synchronized (kernel) {
                kernel.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //get the pid of the current process
    public static void GetPid(){
        parameters.clear();
        callType = currentCall.getSenderPID;
        kernel.start();
        waitForKernel();
        startUserLand();
    }

    //get the pid of the process by name
    public static void GetPidByName(String name){
        parameters.clear();
        parameters.add(name);
        callType = currentCall.getReceieverPID;
        kernel.start();
        waitForKernel();
        startUserLand();
    }

    //send the message
    public static void SendMessage(KernelMessage km){
        parameters.clear();
        parameters.add(new KernelMessage(km));
        callType = currentCall.sendMessage;
        kernel.start();
        waitForKernel();
        startUserLand();
    }

    //get the received message
    public static void GetMessage(){
        parameters.clear();
        callType = currentCall.getMessage;
        kernel.start();
        waitForKernel();
        startUserLand();
    }

    //telling the user land process that the OS has finished processing and is okay to continue on
    public static void startUserLand(){
        callType = currentCall.startUserland;
        kernel.start();
        waitForKernel();
    }

    //exist for testing purposes
    public static Kernel getKernel() {
        return kernel;
    }
}
