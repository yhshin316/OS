import java.util.LinkedList;

public class Ping extends UserlandProcess{

    boolean sent = false;
    @Override
    public void main() {

        System.out.println("===========Inside Ping===========");
        while(true){
            if(sent){
                try {
//                    System.out.println("Ping sent and waiting");
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                cooperate();
            }else{
                OS.GetMessage();
                waiting();
                LinkedList<KernelMessage> messageList = (LinkedList<KernelMessage>) OS.returnValue;
                if(!messageList.isEmpty()){
                    System.out.println("Message Found");
                    KernelMessage message = messageList.getFirst();

                    int number = Integer.parseInt(new String(message.getData()));
                    System.out.println("Ping:"+number);
                    OS.GetPid();
                    waiting();
                    int senderPID = (int) OS.returnValue;
                    System.out.println("SenderPID:"+senderPID);
                    OS.GetPidByName("Pong");
                    waiting();
                    int receiverPID = (int) OS.returnValue;
                    System.out.println("ReceiverPID:"+receiverPID);
                    if(senderPID==-1 || receiverPID==-1){
                        System.out.println("No sender or receiver found");
                        continue;
                    }

                    switch (message.getMessage()){
                        case 1:
                            OS.SendMessage(new KernelMessage(senderPID,receiverPID,1,String.valueOf(++number).getBytes()));
                            System.out.println("Sent Message");
                            sent=true;
                            break;
                        case 2:
                            System.out.println("Message: 2");
                            break;
                    }
                }else{
                    System.out.println("No message found");
                    OS.GetPid();
                    waiting();
                    int senderPID = (int) OS.returnValue;
                    System.out.println("SenderPID:"+senderPID);
                    OS.GetPidByName("Pong");
                    waiting();
                    int receiverPID = (int) OS.returnValue;
                    System.out.println("ReceiverPID:"+receiverPID);

                    if(senderPID==-1 || receiverPID==-1){
                        continue;
                    }

                    OS.SendMessage(new KernelMessage(senderPID,receiverPID,1,String.valueOf(0).getBytes()));
                    waiting();
                    System.out.println("Sent Message");
                    sent=true;
                }
            }
        }
    }

    public void waiting(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() {
        sent = false;
        super.start();
    }
}
