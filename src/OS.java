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
        sleep
    }

    public enum priority {
        realTime,
        interActive,
        background
    }

    public static int CreateProcess(UserlandProcess up) {
        parameters.clear();
        parameters.add(up);
        callType = currentCall.createProcess;
        kernel.start();
        waitForKernel();
        return (int) returnValue;
    }

    public static int CreateProcessPCB(UserlandProcess up, OS.priority priority, int time) {
        parameters.clear();
        parameters.add(up);
        parameters.add(priority);
        parameters.add(time);
        callType = currentCall.createProcess;
        kernel.start();
        waitForKernel();
        return (int) returnValue;
    }

    public static void SwitchProcess() {
        parameters.clear();
        callType = currentCall.switchProcess;
        kernel.start();
        waitForKernel();
    }

    public static void Startup(UserlandProcess up) {
        CreateProcess(up);
        waitForKernel();

        CreateProcess(new IdleProcess());
        waitForKernel();
    }

    public static void StartupPCB(UserlandProcess up, OS.priority priority, int time) {
        CreateProcessPCB(up, priority, time);

        CreateProcessPCB(new IdleProcess(), OS.priority.background, time);
    }

    public static void Sleep(int ms) {
        parameters.clear();
        parameters.add(ms);
        callType = currentCall.sleep;
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
}
