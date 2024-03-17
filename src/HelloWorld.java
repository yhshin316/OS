public class HelloWorld extends UserlandProcess{
    @Override
    public void main() {
        while(true){
            System.out.println("Hello world");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            cooperate();
        }
    }
}
