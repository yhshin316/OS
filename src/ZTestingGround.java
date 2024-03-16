import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ZTestingGround {

    public static void main(String[] args) {

//        OS.StartupPCB(new HelloWorld(), OS.priority.realTime,0);
//        OS.CreateProcessPCB(new GoodByeWorld(), OS.priority.realTime,0);
//        OS.CreateProcessPCB(new HelloWorld(), OS.priority.realTime,0);
//        OS.CreateProcessPCB(new IdleProcess(), OS.priority.background,0);

//        OS.CreateProcessPCB(new HelloWorld(), OS.priority.realTime,0);
//        OS.GetPidByName("HelloWorld");
//        OS.GetPid();
//        System.out.println(OS.returnValue);

//        OS.CreateProcessPCB(new GoodByeWorld(), OS.priority.realTime,0);
//        OS.GetPidByName("GoodByeWorld");
//        System.out.println(OS.returnValue);

        ;

        OS.CreateProcessPCB(new Ping(), OS.priority.realTime,0);
        OS.CreateProcessPCB(new Pong(), OS.priority.realTime,0);
//        int pid = (int) OS.returnValue;
        OS.Sleep(0);
//        OS.SendMessage(new KernelMessage(pid,pid,2,"0".getBytes()));
//        OS.CreateProcessPCB(new Pong(), OS.priority.realTime,0);

    }
}
