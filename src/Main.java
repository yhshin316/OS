public class Main {

    //Having too many userlandprocesses can make the code very buggy, it can usually work well with just ping and pong.
    //can uncomment the goodbyeworld, it can be buggy but should be able to work
    public static void main(String[] args) {

        OS.CreateProcessPCB(new Ping(), OS.priority.realTime,0);
        OS.CreateProcessPCB(new Pong(), OS.priority.realTime,0);
        OS.CreateProcessPCB(new GoodByeWorld(), OS.priority.realTime, 0);
        OS.Sleep(0);

    }
}
