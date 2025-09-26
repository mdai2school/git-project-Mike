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
        
    }
}