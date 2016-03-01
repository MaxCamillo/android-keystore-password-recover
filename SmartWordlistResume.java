package AndroidKeystoreBrute;

/** ravensbane
	* 
* @version 1.0 on 20.2.2016
* @author
*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class SmartWordlistResume extends Thread {
  static long lastCall = 0;
  static final String resumeFile = "AndroidKeystoreBrute_Resume"; // the file name we'll resume from

  // periodically save progress to a file
  public void run() {
    long sleepTime = 30000L;

    try {
      // don't try to save immediately when the thread spins up
      Thread.sleep(sleepTime);
    } catch (Exception e) {

    }

    File file = new File(resumeFile);

    while (!SmartWordlistPasswd.found && !SmartWordlistPasswd.allPwdsTested) {
      if ((System.nanoTime() - lastCall) > sleepTime * 1000000L) {

        try {
          // get the last produced combo that was picked up by a consumer and wait
          // briefly to make sure it gets consumed before we write it to a file
          String[] localS = SmartWordlistProducer.lastComboProduced;
          Thread.sleep(100);

          BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

          // keystore
          writer.write(SmartWordlistPasswd.keystoreFileName);
          writer.newLine();

          // dictionary
          writer.write(SmartWordlistPasswd.dictFileName);
          writer.newLine();

          // write last combo to file, one word per line
          int len = localS.length;
          for (int i = 1; i < len; ++i) {
            writer.write(localS[i]);
            writer.newLine();
          }

          writer.close();

          System.out.println("Progress saved in file: " + resumeFile + "\r\n");
        } catch (Exception e) {
          System.out.println("Unable to write to file: " + resumeFile + "\r\n");
        }

        lastCall = System.nanoTime();

        try {
          Thread.sleep(sleepTime);
        } catch (Exception e) {

        }
      }
    }

    try {
      file.delete();
    } catch (Exception e) {
      System.out.println("Unable to delete file: " + resumeFile);
    }
  }

  // read saved progress from file to use as a resume point
  public static ArrayList<String> getResumePoint(Map<String, String> words) throws IOException {
    ArrayList<String> resumePoint = new ArrayList<String>();
    BufferedReader file;

    try {
      file = new BufferedReader(new InputStreamReader(new FileInputStream(resumeFile)));
    } catch (Exception e) {
      return resumePoint;
    }

    String word = "";

    // the first line should contain the same keystore file name as the one specified in args
    word = file.readLine();
    if (word == null || !word.equals(SmartWordlistPasswd.keystoreFileName)) {
      file.close();
      throw new IOException(resumeFile + " does not contain data for this keystore");
    }

    // the second line should contain the same dictionary file name as the one specified in args
    word = file.readLine();
    if (word == null || !word.equals(SmartWordlistPasswd.dictFileName)) {
      file.close();
      throw new IOException(resumeFile + " does not contain data from this dictionary");
    }

    // the other lines should contain the words we last checked from the dictionary
    while ((word = file.readLine()) != null) {
      // skip empty String
      if (word.equals(""))
        continue;

      // make sure the word is a permutation of a word in the dictionary
      if (words.containsKey(word)) {
        resumePoint.add(word);
      } else {
        file.close();
        throw new IOException(
            resumeFile + " contains a word that does not match any allowed permutation of dictionary words)");
      }
    }

    file.close();

    return resumePoint;
  }
}
