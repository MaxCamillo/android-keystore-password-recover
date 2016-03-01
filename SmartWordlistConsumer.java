package AndroidKeystoreBrute;

/** ravensbane
* 
* @version 1.0 on 20.2.2016
* @author
*/
import java.util.concurrent.LinkedTransferQueue;

public class SmartWordlistConsumer implements Runnable {
  private final LinkedTransferQueue<String> queueRef;

  public SmartWordlistConsumer(LinkedTransferQueue<String> queue) {
    this.queueRef = queue;
  }

  @Override
  public void run() {
    String comboToTest = new String();

    while (!SmartWordlistPasswd.found && !SmartWordlistPasswd.allPwdsTested) {
      try {
        comboToTest = queueRef.take();
      } catch (InterruptedException e) {

      }

      if (AndroidKeystoreBrute.minlength > 0 && comboToTest.length() < AndroidKeystoreBrute.minlength) {
        // faster not to check length if -l arg isn't specified
      } else {
        if (SmartWordlistPasswd.keyIsRight(comboToTest.toCharArray())) {
          SmartWordlistPasswd.found = true;
          SmartWordlistPasswd.complete(comboToTest);
          break;
        }
      }

      SmartWordlistPasswd.testedPwds++;
    }
  }
}