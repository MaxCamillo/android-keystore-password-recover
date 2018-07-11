import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import java.util.Enumeration;

import javax.crypto.EncryptedPrivateKeyInfo;

import java.util.concurrent.LinkedTransferQueue;

public class SmartWordlistPasswd extends Thread {
    static String alias = "";
    static JKS j;
    static String keystoreFileName;
    static String dictFileName;
    static boolean found = false;
    static boolean allPwdsTested = false;
    static int testedPwds = 0;

    public static void doit(String keystore, String dict) throws Exception {
        String pass = "a";

        InputStream in = new FileInputStream(keystore);

        try {
            j = new JKS();
            j.engineLoad(in, pass.toCharArray());
            System.out.println("\r\nNumber of keys in keystore: " + j.engineSize());

            @SuppressWarnings("rawtypes")
            Enumeration e = j.engineAliases();

            while (e.hasMoreElements()) {
                String a = (String) e.nextElement();
                System.out.println("Found alias: " + a);
                System.out.println("Creation Date: " + j.engineGetCreationDate(a));
                alias = a;
            }

            in.close();

            // call our version of these jks methods to perform setup (won't work without this)
            in = new FileInputStream(keystore);
            SmartWordlistPasswd.engineLoad(in, pass.toCharArray());
            in.close();

            keystoreFileName = keystore;
            dictFileName = dict;

            System.out.println("\r\nStarting smart wordlist attack on key!!");
            if (AndroidKeystoreBrute.permutations)
                System.out.println("Using common replacements");
            else if (AndroidKeystoreBrute.onlyLowerCase == false)
                System.out.println("Trying variations with first letter capitalized\r\n");

            int numberOfThreads = Runtime.getRuntime().availableProcessors();
            System.out.println("Firing up " + numberOfThreads + " threads\r\n");

            // we'll use this queue to hold password combinations we're waiting to test
            LinkedTransferQueue<String> queue = new LinkedTransferQueue<String>();

            // start producer thread (adds password combinations to the queue)
            Thread producer = new Thread(new SmartWordlistProducer(queue, dict));
            producer.start();

            // start consumer threads (removes password combinations from the queue and tests them)
            for (int i = 0; i < numberOfThreads; i++) {
                Thread consumer = new Thread(new SmartWordlistConsumer(queue));
                consumer.start();
            }

            // start benchmark and auto-save threads
            new SmartWordlistBenchmark().start();
            new SmartWordlistResume().start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void complete(String passwd) {
        if (found) {
            // We are lucky
            System.out.println("Got Password!");
            System.out.println("Password is: " + passwd + " for alias " + alias);

            try {
                if (AndroidKeystoreBrute.saveNewKeystore) {
                    j.engineStore(new FileOutputStream(keystoreFileName + "_recovered"), new String(passwd).toCharArray());

                    System.out.println("Saved new keystore to: " + keystoreFileName + "_recovered");
                } // end of if
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No matching key combination in wordlist; try another wordlist.");
        }

        AndroidKeystoreBrute.quit();
    }

    // --------------------------------JKS Methods------------------------------------------
    // ravensbane: these have been modified to be thread-safe
    static volatile byte[] encr;
    static volatile byte[] check;
    private static final int MAGIC = 0xFEEDFEED;
    private static final int PRIVATE_KEY = 1;
    private static final int TRUSTED_CERT = 2;

    public static void engineLoad(InputStream in, char[] passwd)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(charsToBytes(passwd));
        md.update("Mighty Aphrodite".getBytes("UTF-8")); // HAR HAR

        DataInputStream din = new DataInputStream(new DigestInputStream(in, md));

        if (din.readInt() != MAGIC) {
            throw new IOException("not a JavaKeyStore");
        }

        din.readInt(); // version no.

        final int n = din.readInt();

        if (n < 0) {
            throw new IOException("negative entry count");
        }

        int type = din.readInt();
        alias = din.readUTF();
        din.readLong(); // Skip Date

        byte[] encoded = null;
        Certificate[] chain = null;

        switch (type) {
            case PRIVATE_KEY:

                int len = din.readInt();
                encoded = new byte[len];
                din.read(encoded);

                // privateKeys.put(alias, encoded);
                int count = din.readInt();
                chain = new Certificate[count];

                for (int j = 0; j < count; j++)
                    chain[j] = readCert(din);

                // certChains.put(alias, chain);
                break;

            case TRUSTED_CERT:

                // trustedCerts.put(alias, readCert(din));
                break;

            default:
                throw new IOException("malformed key store");
        }

        encr = new EncryptedPrivateKeyInfo(encoded).getEncryptedData();
        byte[] keystream = new byte[20];
        System.arraycopy(encr, 0, keystream, 0, 20);
        check = new byte[20];
        System.arraycopy(encr, encr.length - 20, check, 0, 20);

        // make the above writes to these byte arrays volatile
        encr = encr;
        check = check;

        byte[] hash = new byte[20];
        din.read(hash);

        if (MessageDigest.isEqual(hash, md.digest())) {
            throw new IOException("signature not verified");
        }
    }

    public static boolean keyIsRight(char[] password) {
        try {
            return decryptKey(charsToBytes(password));
        } catch (Exception x) {
            return false;
        }
    }

    private static byte[] charsToBytes(char[] passwd) {
        byte[] buf = new byte[passwd.length * 2];

        for (int i = 0, j = 0; i < passwd.length; i++) {
            buf[j++] = (byte) (passwd[i] >>> 8);
            buf[j++] = (byte) passwd[i];
        }

        return buf;
    }

    private static boolean decryptKey(byte[] passwd) throws NoSuchAlgorithmException {
        // create thread-safe local variables
        MessageDigest sha = MessageDigest.getInstance("SHA1");
        byte[] keystream = new byte[20];
        byte[] key = new byte[encr.length - 40];

        try {
            System.arraycopy(encr, 0, keystream, 0, 20);

            int count = 0;

            while (count < key.length) {
                sha.reset();
                sha.update(passwd);
                sha.update(keystream);
                sha.digest(keystream, 0, keystream.length);

                for (int i = 0; (i < keystream.length) && (count < key.length); i++) {
                    key[count] = (byte) (keystream[i] ^ encr[count + 20]);
                    count++;
                }
            }

            sha.reset();
            sha.update(passwd);
            sha.update(key);

            if (MessageDigest.isEqual(check, sha.digest())) {
                return true;
            }

            return false;
        } catch (Exception x) {
            return false;
        }
    }

    private static Certificate readCert(DataInputStream in)
            throws IOException, CertificateException, NoSuchAlgorithmException {
        String type = in.readUTF();
        int len = in.readInt();
        byte[] encoded = new byte[len];
        in.read(encoded);

        CertificateFactory factory = CertificateFactory.getInstance(type);

        return factory.generateCertificate(new ByteArrayInputStream(encoded));
    }
}
