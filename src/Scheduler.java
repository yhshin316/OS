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
    private Map<Integer,PCB> waitingList;

    public Runnable executingStop() {
        Runnable task = this::stoppingPCB;
        executor.scheduleAtFixedRate(task, 2, 2, TimeUnit.SECONDS);
        return task;
    }

    public void stoppingPCB() {
        if (currentUserLandProcessPCB != null) {
            currentUserLandProcessPCB.stop();
        }else{

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

//        if (currentUserLandProcessPCB == null) {
//            Sleep(waitTime);
//        }
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
    }

    public int GetPid() {
        return currentUserLandProcessPCB.getPID();
    }

    public int GetPidByName(String name) {
        int pid = -1;
        boolean found = false;

        for (PCB receiver : realTimeLinkedList) {
            if (receiver.getUserlandProcess().getClass().getSimpleName().contains(name)) {
                found = true;
                pid = receiver.getPID();
                break;
            }
        }
        for (PCB receiver : interActiveLinkedList) {
            if (receiver.getUserlandProcess().getClass().getSimpleName().contains(name) && !found) {
                found = true;
                pid = receiver.getPID();
                break;
            }
        }
        for (PCB receiver : backgroundLinkedList) {
            if (receiver.getUserlandProcess().getClass().getSimpleName().contains(name) && !found) {
                pid = receiver.getPID();
                break;
            }
        }
        return pid;
    }

    public void SendMessage(KernelMessage message) {
        boolean found = false;

        for(Map.Entry<Integer, PCB> receiver: waitingList.entrySet()){
            if(receiver.getValue().getPID() == message.getReceiverPID()){
                receiver.getValue().clearMessage();
                receiver.getValue().AddMessage(message);
                switch (receiver.getValue().getPriority()){
                    case realTime -> realTimeLinkedList.add(receiver.getValue());
                    case interActive -> interActiveLinkedList.add(receiver.getValue());
                    case background -> backgroundLinkedList.add(receiver.getValue());
                }
                waitingList.remove(receiver.getKey());
                found=true;
                break;
            }
        }

        for (PCB receiver : realTimeLinkedList) {
            if (receiver.getPID()==message.getReceiverPID() && !found) {
                found = true;
                receiver.clearMessage();
                receiver.AddMessage(message);
                waitingList.put(receiver.getPID(), receiver);
                break;
            }
        }
        for (PCB receiver : interActiveLinkedList) {
            if (receiver.getPID()==message.getReceiverPID() && !found) {
                found = true;
                receiver.clearMessage();
                receiver.AddMessage(message);
                waitingList.put(receiver.getPID(), receiver);
                break;
            }
        }
        for (PCB receiver : backgroundLinkedList) {
            if (receiver.getPID()==message.getReceiverPID() && !found) {
                receiver.clearMessage();
                receiver.AddMessage(message);
                waitingList.put(receiver.getPID(), receiver);
                break;
            }
        }

        WaitForMessage();


    }

    public void WaitForMessage(){
        waitingList.put(currentUserLandProcessPCB.getPID(),currentUserLandProcessPCB);

        realTimeLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());
        interActiveLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());
        backgroundLinkedList.removeIf(process -> currentUserLandProcessPCB.getUserlandProcess() == process.getUserlandProcess());
//        return currentUserLandProcessPCB.getMessages().getFirst();
    }

    public LinkedList<KernelMessage> GetMessage() {
        return currentUserLandProcessPCB.getMessages();
    }
}
