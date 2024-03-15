public class GoobByeWorld extends UserlandProcess{
    @Override
    public void main() {
        while(true){
            System.out.println("GoodBye world");
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cooperate();
        }
    }
}
