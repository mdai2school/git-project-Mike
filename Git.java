import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Git{
    public static boolean AlreadyExists = true;

    public static File makeFolder(String folderName){
        File dir = new File(folderName);
        if (!dir.exists()){
            AlreadyExists = false;
        }
        dir.mkdir();
        return dir;
    }

    public static File makeFolder(String folderName, File folder){
        if (!folder.exists()){
            throw new IllegalArgumentException("Attempted to create a folder within a folder that does not exist!");
        }
        File dir = new File(folder.getPath(),folderName);
        if (!dir.exists()){
            AlreadyExists = false;
        }
        dir.mkdir();
        return dir;
    }

    public static File makeFile(String fileName) throws IOException{
        File file = new File(fileName);
        if (!file.exists()){
            AlreadyExists = false;
        }
        file.createNewFile();
        return file;
    }

    public static File makeFile(String fileName, File folder) throws IOException{
        if (!folder.exists()){
            throw new IllegalArgumentException("Attempted to create a file within a folder that does not exist!");
        }
        File file = new File(folder.getPath(), fileName);
        if (!file.exists()){
            AlreadyExists = false;
        }
        file.createNewFile();
        return file;
    }

    public static String SHA1(String message){
        try { 
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            
            byte[] digestedMessage = md.digest(message.getBytes());

            BigInteger number = new BigInteger(1,digestedMessage);

            String hashtext = number.toString(16);

            while (hashtext.length() < 40){
                hashtext = "0" + hashtext;
            }

            return hashtext;

        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }

    }

    public static void testHash(){
        String hash1 = SHA1("hello");
        boolean pass1 = hash1.equals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d");

        System.out.println("Test 1!\nexpected:aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d\nreturned:" + hash1);
        if (pass1){
            System.out.println("passed!");
        } else{
            System.out.println("failed!");
        }

        String hash2 = SHA1("12345");
        boolean pass2 = hash2.equals("8cb2237d0679ca88db6464eac60da96345513964");

        System.out.println("Test 2!\nexpected:8cb2237d0679ca88db6464eac60da96345513964\nreturned:" + hash2);
        if (pass2){
            System.out.println("passed!");
        } else{
            System.out.println("failed!");
        }

        String hash3 = SHA1("Bald!?");
        boolean pass3 = hash3.equals("b0ac221808a66f8cf0bfcba4a5da29dbcba77e4b");

        System.out.println("Test 3!\nexpected:b0ac221808a66f8cf0bfcba4a5da29dbcba77e4b\nreturned:" + hash3);
        if (pass3){
            System.out.println("passed!");
        } else{
            System.out.println("failed!");
        }
    }

    public static boolean test() throws IOException{
        testHash();
        
        for (int i = 0; i < 3; i++){
            File git = makeFolder("git");
            File objects = makeFolder("objects", git);
            File index = makeFile("index", git);
            File HEAD = makeFile("HEAD", git);
            if (AlreadyExists){
                System.out.println("Git Repository Already Exists");
            } else{
                System.out.println("Git Repository Created");
            }

            File gitTest = new File("git");
            if (!gitTest.exists()){
                System.out.println("failed! " + gitTest.getName() + " doesn't exist!");
                return false;
            }

            File objectsTest = new File(git.getPath(),"objects");
            if (!objectsTest.exists()){
                System.out.println("failed! " + objectsTest.getName() + " doesn't exist!");
                return false;
            }

            File HEADTest = new File(git.getPath(),"HEAD");
            if (!HEADTest.exists()){
                System.out.println("failed! " + HEADTest.getName() + " doesn't exist!");
                return false;
            }

            File indexTest = new File(git.getPath(),"index");
            if (!indexTest.exists()){
                System.out.println("failed! " + indexTest.getName() + " doesn't exist!");
                return false;
            }
            objects.delete();
            HEAD.delete();
            index.delete();
            git.delete();
        }
        return true;

    }

    public static void main(String[] args) throws IOException {
        File git = makeFolder("git");
        File objects = makeFolder("objects", git);
        File index = makeFile("index", git);
        File HEAD = makeFile("HEAD", git);
        
        if (AlreadyExists){
            System.out.println("Git Repository Already Exists");
        } else{
            System.out.println("Git Repository Created");
        }

        System.out.println(test());

        testHash();
        
    }
}