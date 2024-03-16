import java.util.Arrays;

public class KernelMessage {

    private int senderPID;
    private int receiverPID;
    private int message;
    private byte[] data;

    public KernelMessage(KernelMessage kernelMessage){
        this.senderPID = kernelMessage.senderPID;
        this.receiverPID = kernelMessage.receiverPID;
        this.message = kernelMessage.message;
        this.data = kernelMessage.data;
    }

    public KernelMessage(int senderPID, int receiverPID, int message, byte[] data){
        this.senderPID = senderPID;
        this.receiverPID = receiverPID;
        this.message = message;
        this.data = data;
    }

    public int getSenderPID() {
        return senderPID;
    }

    public int getReceiverPID() {
        return receiverPID;
    }

    public int getMessage() {
        return message;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "KernelMessage{" +
                "senderPID=" + senderPID +
                ", receiverPID=" + receiverPID +
                ", message=" + message +
                ", data=" + new String(data) +
                '}';
    }
}
