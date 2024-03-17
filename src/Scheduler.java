import java.time.Clock;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private final LinkedList<UserlandProcess> userlandProcessLinkedList;
    private final LinkedList<PCB> realTimeLinkedList;
    private final LinkedList<PCB> interActiveLinkedList;
    private final LinkedList<PCB> backgroundLinkedList;
    private UserlandProcess currentUserLandProcess;
    private PCB currentUserLandProcessPCB;
    private final ScheduledExecutorService executor;
    private Map<Integer, PCB> waitingList;

    //runs the method to switch user land process
    public Runnable executingStop() {
        Runnable task = this::stoppingPCB;
        executor.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
        return task;
    }

    public void stoppingPCB() {
        if (currentUserLandProcessPCB != null) {
            currentUserLandProcessPCB.stop();
        } else {

        }
    }


    public Scheduler() {
        this.realTimeLinkedList = new LinkedList<>();
        this.interActiveLinkedList = new LinkedList<>();
        this.backgroundLinkedList = new LinkedList<>();
        this.userlandProcessLinkedList = new LinkedList<>();
        this.waitingList = new HashMap<>();

        executor = Executors.newSingleThreadScheduledExecutor();
        executingStop();
//        Timer timer = new Timer();
//        try {
//            timer.schedule(interrupt(), 250, 250);
//        } catch (Exception e) {
//        }
    }

//    private TimerTask interrupt() {
//        return new TimerTask() {
//            @Override
//            public void run() {
//                try {
//                    currentUserLandProcess.requestStop();
//                } catch (Exception e) {
//                }
//            }
//        };
//    }

    public int CreateProcess(UserlandProcess up) {
        userlandProcessLinkedList.add(up);
        if (currentUserLandProcess == null) {
            SwitchProcess();
        }
        return up.hashCode();
    }

    public int CreateProcess(UserlandProcess up, OS.priority priority, int waitTime) {

        PCB pcb = new PCB(up, priority, waitTime);

        switch (priority) {
            case realTime -> realTimeLinkedList.add(pcb);
            case interActive -> interActiveLinkedList.add(pcb);
            case background -> backgroundLinkedList.add(pcb);
        }
        return up.hashCode();
    }

    public void SwitchProcess() {
        if (currentUserLandProcess == null) {
            currentUserLandProcess = userlandProcessLinkedList.getFirst();
            currentUserLandProcess.start();
        } else {
            userlandProcessLinkedList.add(userlandProcessLinkedList.getFirst());
            userlandProcessLinkedList.removeFirst();
            currentUserLandProcess = userlandProcessLinkedList.getFirst();
            currentUserLandProcess.start();
        }
    }

    public void Sleep(int ms) {

        if (currentUserLandProcessPCB != null) {
            realTimeLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());
            interActiveLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());
            backgroundLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());

            currentUserLandProcessPCB.updateWaitTime(ms);

            switch (currentUserLandProcessPCB.getPriority()) {
                case realTime -> realTimeLinkedList.add(currentUserLandProcessPCB);
                case interActive -> interActiveLinkedList.add(currentUserLandProcessPCB);
                case background -> backgroundLinkedList.add(currentUserLandProcessPCB);
            }
        }


        boolean found = false;
        boolean backgroundCheck = false;
        boolean interActiveCheck = false;
        boolean realTimeCheck = false;
        while (!found && !(backgroundCheck && interActiveCheck && realTimeCheck)) {
            Random random = new Random();
            int randomNumber = random.nextInt(10);
            if (randomNumber == 9 && !backgroundCheck) {
                for (PCB process : backgroundLinkedList) {
                    if (process.getWakeUp().isBefore(Clock.systemDefaultZone().instant())) {
                        found = true;
                        currentUserLandProcessPCB = process;
                    }
                }
                backgroundCheck = true;

            } else if (randomNumber >= 6 && !interActiveCheck) {
                for (PCB process : interActiveLinkedList) {
                    if (process.getWakeUp().isBefore(Clock.systemDefaultZone().instant())) {
                        found = true;
                        currentUserLandProcessPCB = process;
                    }
                }
                interActiveCheck = true;

            } else if (randomNumber < 6 && !realTimeCheck) {
                for (PCB process : realTimeLinkedList) {
                    if (process.getWakeUp().isBefore(Clock.systemDefaultZone().instant())) {
                        found = true;
                        currentUserLandProcessPCB = process;
                    }
                }
                realTimeCheck = true;
            }
        }

        if (currentUserLandProcessPCB == null) {
            CreateProcess(new IdleProcess(), OS.priority.background, 0);
        }

        currentUserLandProcessPCB.run();

        realTimeLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());
        interActiveLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());
        backgroundLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());
    }

    //gets the current PID
    public int GetPid() {
        return currentUserLandProcessPCB.getPID();
    }

    //gets the PID of the process with the name
    public int GetPidByName(String name) {
        //returns -1 not found
        int pid = -1;
        //when found, turns true to skip searching all other list
        boolean found = false;

        //search the name in realTime list
        for (PCB receiver : realTimeLinkedList) {
            if (receiver.getUserlandProcess().getClass().getSimpleName().contains(name)) {
                found = true;
                pid = receiver.getPID();
                break;
            }
        }
        //search the name in interactive list
        for (PCB receiver : interActiveLinkedList) {
            if (receiver.getUserlandProcess().getClass().getSimpleName().contains(name) && !found) {
                found = true;
                pid = receiver.getPID();
                break;
            }
        }
        //search the name in background list
        for (PCB receiver : backgroundLinkedList) {
            if (receiver.getUserlandProcess().getClass().getSimpleName().contains(name) && !found) {
                pid = receiver.getPID();
                break;
            }
        }
        return pid;
    }

    //sends message to specific process, the message have the destination PID
    public void SendMessage(KernelMessage message) {
        //when found, turns true to skip searching all other list
        boolean found = false;

        //search the process in the map for processes that have send and is now waiting for messages
        for (Map.Entry<Integer, PCB> receiver : waitingList.entrySet()) {
            //if found then the message is added to PCB and is sent back to the regular priority lists
            if (receiver.getValue().getPID() == message.getReceiverPID()) {
                receiver.getValue().clearMessage();
                receiver.getValue().AddMessage(message);
                switch (receiver.getValue().getPriority()) {
                    case realTime -> realTimeLinkedList.add(receiver.getValue());
                    case interActive -> interActiveLinkedList.add(receiver.getValue());
                    case background -> backgroundLinkedList.add(receiver.getValue());
                }
                //remove the process from the map as it is no longer waiting for a message
                waitingList.remove(receiver.getKey());
                found = true;
                break;
            }
        }

        //searching for process in realTime list
        for (PCB receiver : realTimeLinkedList) {
            if (receiver.getPID() == message.getReceiverPID() && !found) {
                found = true;
                receiver.clearMessage();
                receiver.AddMessage(message);
                waitingList.put(receiver.getPID(), receiver);
                break;
            }
        }
        //searching for process in interActive list
        for (PCB receiver : interActiveLinkedList) {
            if (receiver.getPID() == message.getReceiverPID() && !found) {
                found = true;
                receiver.clearMessage();
                receiver.AddMessage(message);
                waitingList.put(receiver.getPID(), receiver);
                break;
            }
        }
        //searching for process in background list
        for (PCB receiver : backgroundLinkedList) {
            if (receiver.getPID() == message.getReceiverPID() && !found) {
                receiver.clearMessage();
                receiver.AddMessage(message);
                waitingList.put(receiver.getPID(), receiver);
                break;
            }
        }

        //send the current process which sent a message and is now waiting for response into the Map for processes
        //waiting for message
        WaitForMessage();
    }

    //send the current process which have sent a message and is now waiting for a message to the MAP for
    //processes that are waiting for messages
    public KernelMessage WaitForMessage() {
        //puts the current process into the Map and remove the process currently existing in priority list
        if (currentUserLandProcessPCB != null && currentUserLandProcessPCB.getMessages() != null && !currentUserLandProcessPCB.getMessages().isEmpty()) {
            waitingList.put(currentUserLandProcessPCB.getPID(), currentUserLandProcessPCB);

            realTimeLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());
            interActiveLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());
            backgroundLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());

            return currentUserLandProcessPCB.getMessages().getFirst();
        } else {
            return null;
        }
    }

    //gets the message from PCB to user land process
    public LinkedList<KernelMessage> GetMessage() {
        return currentUserLandProcessPCB.getMessages();
    }

    //tells the user land process that the OS is done processing and is okay to run
    public void startUserLand() {
        currentUserLandProcessPCB.getUserlandProcess().start();
    }


    //exist for easier testing
    public PCB getPCBByName(String name) {
        //returns -1 not found
        PCB pcb = null;
        //when found, turns true to skip searching all other list
        boolean found = false;

        //search the name in realTime list
        for (PCB receiver : realTimeLinkedList) {
            if (receiver.getUserlandProcess().getClass().getSimpleName().contains(name)) {
                found = true;
                pcb = receiver;
                break;
            }
        }
        //search the name in interactive list
        for (PCB receiver : interActiveLinkedList) {
            if (receiver.getUserlandProcess().getClass().getSimpleName().contains(name) && !found) {
                found = true;
                pcb = receiver;
                break;
            }
        }
        //search the name in background list
        for (PCB receiver : backgroundLinkedList) {
            if (receiver.getUserlandProcess().getClass().getSimpleName().contains(name) && !found) {
                pcb = receiver;
                break;
            }
        }
        return pcb;
    }
}
