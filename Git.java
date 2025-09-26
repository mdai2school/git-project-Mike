import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

    public static boolean test() throws IOException{
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
        
    }
}