public class SmartWordlistBenchmark extends Thread {
    long lastCall = 0;
    int lastCount = 0;
    long sleepTime = 5000L;

    public void run() {
        while (!SmartWordlistPasswd.found && !SmartWordlistPasswd.allPwdsTested) {
            if ((System.nanoTime() - lastCall) > sleepTime * 1000000L) {

                // convert string array to string
                StringBuilder sb = new StringBuilder();
                for (String s : SmartWordlistProducer.lastComboProduced) {
                    sb.append(s);
                }
                String lastTested = sb.toString();

                System.out.printf("Current Pass: %s || est. %.0f Pass/sec%n", lastTested,
                        (SmartWordlistPasswd.testedPwds - lastCount) / (sleepTime * 0.001));

                lastCall = System.nanoTime();
                lastCount = SmartWordlistPasswd.testedPwds;

                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {

                }
            }
        }
    }
}
