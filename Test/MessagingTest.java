import org.junit.Test;

import static org.junit.Assert.*;

//please test one by one, testing all cause some to fail because of race conditions and other factors like sleep
//starting a program randomly by priority and the program is designed to keep running and not stopped.
public class MessagingTest {

    //test for proper message sending, can run in main to see the ping, pong, and incrementing number
    //adding too many userlandprocesses makes the output buggy
    @Test
    public void DisplayingDataAndBackAndForth(){
        OS.CreateProcessPCB(new Ping(), OS.priority.realTime,0);
        OS.CreateProcessPCB(new Pong(), OS.priority.realTime,0);
        OS.Sleep(0);
    }

    //get the current pid, which should be set by Sleep(0)
    @Test
    public void GetingPid(){
        OS.CreateProcessPCB(new Ping(), OS.priority.realTime,0);
        OS.CreateProcessPCB(new Pong(), OS.priority.realTime,0);
        OS.Sleep(0);
        OS.GetPid();
        assertEquals((int)OS.returnValue,OS.getKernel().getScheduler().GetPid());
        System.out.println(OS.returnValue+ " " +OS.getKernel().getScheduler().GetPid());

    }

    //the return value in OS has the latest pid from create process and GetPidByName should match the return value
    @Test
    public void GettingPidByName(){
        OS.CreateProcessPCB(new Ping(), OS.priority.realTime,0);
        OS.CreateProcessPCB(new Pong(), OS.priority.realTime,0);
        assertEquals((int)OS.returnValue,OS.getKernel().getScheduler().GetPidByName("Pong"));
        System.out.println(OS.returnValue+ " " + OS.getKernel().getScheduler().GetPidByName("Pong"));
    }

    /*checks if the message has been sent correctly, the list should not be empty as the message is sent
    should be able to see the String of the message that was sent
    The message(3rd parameter) can be either 1 or 2 depending on the race condition of between ping and this function
    both sending messages to pong, the main program should work better as there are some flow control unlike here
    which is directly sending the message*/
    @Test
    public void sendMessage(){
        OS.CreateProcessPCB(new Ping(), OS.priority.realTime,0);
        int ping = (int) OS.returnValue;
        OS.CreateProcessPCB(new Pong(), OS.priority.realTime,0);
        int pong = (int) OS.returnValue;
        OS.Sleep(0);
        OS.SendMessage(new KernelMessage(ping, pong,2,"Hello".getBytes()));
        assertFalse(OS.getKernel().getScheduler().getPCBByName("Pong").getMessages().isEmpty());
        System.out.println(OS.getKernel().getScheduler().getPCBByName("Pong").getMessages().getFirst().toString());
    }
}
