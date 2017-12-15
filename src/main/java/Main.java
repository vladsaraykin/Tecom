import moduls.Agent;
import moduls.NetworkPrinter;
import moduls.Router;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        Agent networkPrinter = new NetworkPrinter("Network Printer HP", "Saransk");

        networkPrinter.start();
        networkPrinter.checkStart();
        Thread.sleep(5000);
        networkPrinter.stop();
        networkPrinter.checkStart();

        Agent router = new Router("TP-link", "Saransk");

        router.start();
        router.checkStart();
        Thread.sleep(6000);
        router.stop();
        router.checkStart();


    }
}
