public class BruteBenchmark extends Thread {
    long lastCall = 0;
    int lastCount = 0;

    public void run() {
        while (!BrutePasswd.found) {
            if ((System.nanoTime() - lastCall) > 1000000000) {
                System.out.println("Current Pass: " + String.copyValueOf(BrutePasswd.currPass) + " || est. "
                        + ((BrutePasswd.testedPwds - lastCount)) + " Pass/Sec");
                System.out.println();

                lastCall = System.nanoTime();
                lastCount = BrutePasswd.testedPwds;

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
            } else {
                // System.out.println("Too much");
            }
        }
    }
}
