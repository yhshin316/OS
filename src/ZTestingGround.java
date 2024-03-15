import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ZTestingGround {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {

//        OS.StartupPCB(new HelloWorld(), OS.priority.realTime,0);
        OS.CreateProcessPCB(new GoobByeWorld(), OS.priority.realTime,0);
        OS.CreateProcessPCB(new HelloWorld(), OS.priority.realTime,0);
        OS.CreateProcessPCB(new IdleProcess(), OS.priority.background,0);

//        ZTestingGround zTestingGround = new ZTestingGround();
    }

    public ZTestingGround(){
        executingStop();
    }

    public Runnable executingStop() {
        Runnable task = () -> hello();
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS);
        return task;
    }

    public void hello() {
        System.out.println("Hello World");
    }
}
