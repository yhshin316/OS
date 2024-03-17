public class IdleProcess extends UserlandProcess{
    @Override
    public void main() {
        while(true){
            System.out.println("Idle Process world");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cooperate();
        }
    }
}
