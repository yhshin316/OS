import java.util.LinkedList;

public class Pong extends UserlandProcess{

    boolean sent = false;
    @Override
    public void main() {

        while(true){
            //if sent then just wait until the stop signal
            if(sent){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                cooperate();
                //if not sent then create a message and send
            }else{
                //tell OS to get the message stored in the PCB that holds this user land process
                OS.GetMessage();
                //waiting until OS is finished
                waiting();
                LinkedList<KernelMessage> messageList;
                //check if the data type is correct from the OS else restart the loop
                if((OS.returnValue instanceof LinkedList)){
                    messageList = (LinkedList<KernelMessage>) OS.returnValue;
                }else{
                    continue;
                }
                //if there is not empty then process the message and send a message
                if(!messageList.isEmpty()){
                    KernelMessage message = messageList.getFirst();
                    //get the data and prints out class and the data inside, the number should be a number that increase
                    //as the time goes on
                    int number = Integer.parseInt(new String(message.getData()));
                    System.out.println("Pong:"+number);
                    //tell OS to get PID for this
                    OS.GetPid();
                    //wait for the OS to finish
                    waiting();
                    int senderPID;
                    //check if the data type is correct from the OS else restart the loop
                    if((OS.returnValue instanceof Integer)){
                        senderPID = (int) OS.returnValue;
                    }else{
                        continue;
                    }
                    //tell OS to get PID for the Pong
                    OS.GetPidByName("Ping");
                    //wait for the OS to finish
                    waiting();
                    int receiverPID;
                    //check if the data type is correct from the OS else restart the loop
                    if((OS.returnValue instanceof Integer)){
                        receiverPID = (int) OS.returnValue;
                    }else{
                        continue;
                    }
                    //restart the loop if one of the 2 PID is not found
                    if(senderPID==-1 || receiverPID==-1){
                        continue;
                    }

                    //switch for message, 1 would send another message and 2 would just print "Message: 2"
                    switch (message.getMessage()){
                        case 1:
                            //send the message
                            OS.SendMessage(new KernelMessage(senderPID,receiverPID,1,String.valueOf(++number).getBytes()));
                            //turn sent to true to prevent sending additional messages
                            sent=true;
                            break;
                        case 2:
                            System.out.println("Message: 2");
                            break;
                    }
                    //if message is not found then create and send the message, starting the message loop between Ping and Pong
                }else{
                    OS.GetPid();
                    waiting();
                    int senderPID;
                    //check if the data type is correct from the OS else restart the loop
                    if((OS.returnValue instanceof Integer)){
                        senderPID = (int) OS.returnValue;
                    }else{
                        continue;
                    }
                    OS.GetPidByName("Ping");
                    waiting();
                    int receiverPID;
                    //check if the data type is correct from the OS else restart the loop
                    if((OS.returnValue instanceof Integer)){
                        receiverPID = (int) OS.returnValue;
                    }else{
                        continue;
                    }
                    //restart the loop if one of the 2 PID is not found
                    if(senderPID==-1 || receiverPID==-1){
                        continue;
                    }
                    //send the message
                    OS.SendMessage(new KernelMessage(senderPID,receiverPID,1,String.valueOf(0).getBytes()));
                    //turn sent to true to prevent sending additional messages
                    sent=true;
                }
            }
        }
    }

    //telling to wait until OS has finished processing
    public void waiting(){
        try {
            this.semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //restart with sent turned to false, to send another message
    @Override
    public void start() {
        sent = false;
        super.start();
    }
}
