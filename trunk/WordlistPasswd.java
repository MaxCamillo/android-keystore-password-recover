/**
  *
  * Beschreibung
  *
  * @version 1.0 vom 26.11.2011
  * @author
  */
import java.io.*;
import java.util.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.spec.SecretKeySpec;

public class WordlistPasswd {
  
  static String alias = "";
  static JKS j;
  static boolean found = false;
  static String currentPass = "";
  static String passwd = null;
  static int testedPwds = 0;
  
  
  
  public static void doit(String keystore,String wordlist) throws Exception{
    
    InputStream in = new FileInputStream(keystore);
    char[] pass = new char[1];
    
    
    try{
      j = new JKS();
      j.engineLoad(in,pass);
      System.out.println("Number of keys in keystore: "+j.engineSize());
      Enumeration e = j.engineAliases();
      while(e.hasMoreElements()){
        String a = (String) e.nextElement();
        System.out.println("Found alias: "+a);
        System.out.println("Creation Date: "+j.engineGetCreationDate(a));
        alias = a;
      }
      in.close();
      
      BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(wordlist)));
      System.out.println("\r\nStart dictionary attack on key!!\r\n");
      long initTime = System.currentTimeMillis();
      new WordlistBenchmark().start();
      in = new FileInputStream(keystore);
      WordlistPasswd.engineLoad(in,pass);
      while((currentPass != null) & (!found)){
        currentPass = file.readLine();
        try{
          testedPwds++;
          //if this throws an Exception; pwd  is false
          if(keyIsRight(currentPass.toCharArray())){
            //if no Exception was thrown we have the password
            found = true;
            passwd =  currentPass;
            break;
          }
          
        }catch(Exception ex){
          //passwd was wrong
          
        }
      }
      
      
      if(found){
        //We are lucky
        System.out.println("Got Password in "+((System.currentTimeMillis() - initTime))/1000+" seconds");
        System.out.println("Password is: "+passwd+" for alias "+alias);
        
        if (AndroidKeystoreBrute.saveNewKeystore) {
          j.engineStore(new FileOutputStream(keystore+"_recovered"),new String(passwd).toCharArray());
          System.out.println("Saved new keystore to: "+ keystore+"_recovered");
        } // end of if
        
        AndroidKeystoreBrute.found = true;
      }else{
        System.out.println("No matching key in wordlist; try an other wordlist!!");
      }
      
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
  //--------------------------------JKS Methods------------------------------------------
  private static final int MAGIC = 0xFEEDFEED;
  static byte[] encoded;
  static Certificate[] chain;
  static MessageDigest sha;
  static byte[] key;
  static byte[] keystream;
  static byte[] encr;
  static byte[] check;
  
  private static final int PRIVATE_KEY  = 1;
  private static final int TRUSTED_CERT = 2;
  
  public static void engineLoad(InputStream in, char[] passwd)
  throws IOException, NoSuchAlgorithmException, CertificateException
  {
    MessageDigest md = MessageDigest.getInstance("SHA");
    md.update(charsToBytes(passwd));
    md.update("Mighty Aphrodite".getBytes("UTF-8")); // HAR HAR
    DataInputStream din = new DataInputStream(new DigestInputStream(in, md));
    if (din.readInt() != MAGIC)
    throw new IOException("not a JavaKeyStore");
    din.readInt();  // version no.
    final int n = din.readInt();
    if (n < 0)
    throw new IOException("negative entry count");
    
    int type = din.readInt();
    WordlistPasswd.alias = din.readUTF();
    din.readLong(); //Skip Date
    switch (type)
    {
      case PRIVATE_KEY:
      int len = din.readInt();
      encoded = new byte[len];
      din.read(encoded);
      //privateKeys.put(alias, encoded);
      int count = din.readInt();
      chain = new Certificate[count];
      for (int j = 0; j < count; j++)
      chain[j] = readCert(din);
      //certChains.put(alias, chain);
      break;
      
      case TRUSTED_CERT:
      //trustedCerts.put(alias, readCert(din));
      break;
      
      default:
      throw new IOException("malformed key store");
    }
    
    encr = new EncryptedPrivateKeyInfo(encoded).getEncryptedData();
    keystream = new byte[20];
    System.arraycopy(encr, 0, keystream, 0, 20);
    check = new byte[20];
    System.arraycopy(encr, encr.length-20, check, 0, 20);
    key = new byte[encr.length - 40];
    sha = MessageDigest.getInstance("SHA1");
    
    byte[] hash = new byte[20];
    din.read(hash);
    if (MessageDigest.isEqual(hash, md.digest()))
    throw new IOException("signature not verified");
  }
  
  public static boolean keyIsRight(char[] password){
    
    try
    {
      return decryptKey(charsToBytes(password));
      
    }
    catch (Exception x)
    {
      return false;
    }
    
  }
  
  private static byte[] charsToBytes(char[] passwd)
  {
    byte[] buf = new byte[passwd.length * 2];
    for (int i = 0, j = 0; i < passwd.length; i++)
    {
      buf[j++] = (byte) (passwd[i] >>> 8);
      buf[j++] = (byte)  passwd[i];
    }
    return buf;
  }
  
  
  
  
  private static boolean decryptKey( byte[] passwd){
    try
    {
      System.arraycopy(encr, 0, keystream, 0, 20);
      int count = 0;
      while (count < key.length)
      {
        sha.reset();
        sha.update(passwd);
        sha.update(keystream);
        sha.digest(keystream, 0, keystream.length);
        
        for (int i = 0; i < keystream.length && count < key.length; i++)
        {
          key[count] = (byte) (keystream[i] ^ encr[count+20]);
          count++;
        }
      }
      sha.reset();
      sha.update(passwd);
      sha.update(key);
      if (MessageDigest.isEqual(check, sha.digest()))
      return true;
      
      return false;
    }
    catch (Exception x)
    {
      return false;
    }
  }
  
  private static Certificate readCert(DataInputStream in)
  throws IOException, CertificateException, NoSuchAlgorithmException
  {
    String type = in.readUTF();
    int len = in.readInt();
    byte[] encoded = new byte[len];
    in.read(encoded);
    CertificateFactory factory = CertificateFactory.getInstance(type);
    return factory.generateCertificate(new ByteArrayInputStream(encoded));
  }
  
}
