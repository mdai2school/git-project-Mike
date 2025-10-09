import java.io.*;
import java.nio.file.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class Git{
    public static File git;
    public static File objects;
    public static File index;
    public static File HEAD;
    public static boolean COMPRESS_BLOBS = false;
    public static boolean AlreadyExists = true;

    static {
        git = makeFolder("git");
        objects = makeFolder("objects", git);
        try {
            index = makeFile("index", git);
            HEAD = makeFile("HEAD", git);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize files", e);
        }
    }

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

    public static String blob(File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("blob: source must be an existing file");
        }
        byte[] raw = Files.readAllBytes(file.toPath());
        byte[] content;
        if (COMPRESS_BLOBS){
            content = compress(raw);
        } else {
            content = raw;
        }
        String blobName = SHA1(content);
        File blob = makeFile(blobName, objects);
        try (OutputStream out = new FileOutputStream(blob)) {
            out.write(content);
        }
        return blobName;
    }


    public static String SHA1(String message){
        return SHA1(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static String SHA1(byte[] bytes){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digestedMessage = md.digest(bytes);
            BigInteger number = new BigInteger(1, digestedMessage);
            String hashtext = number.toString(16);
            while (hashtext.length() < 40){
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    public static boolean blobExists(String hash) {
        // check if blob's hash is valid
        if (hash == null || hash.length() != 40){
            return false;
        }
        File f = new File(objects, hash);
        // checks if the file connected to blob exists and is a file
        return f.exists() && f.isFile();
    }

    public static void resetObjects() throws IOException {
        if (!objects.exists()) {
            return;
        }
        File[] kids = objects.listFiles();
        if (kids == null){
            return;
        } else{
            for (File k : kids){
                k.delete();
            }
        }
    }

    public static byte[] compress(byte[] input) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(bos, new Deflater())) {
            dos.write(input);
        }
        return bos.toByteArray();
    }

    public static String relPath(File f) {
        Path base = Paths.get("").toAbsolutePath().normalize();
        Path p = f.toPath().toAbsolutePath().normalize();
        return base.relativize(p).toString();
    }


    public static void writeIndexLines(List<String> lines) throws IOException {
        try (FileOutputStream out = new FileOutputStream(index, false)) {
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0) {
                    out.write('\n');
                }
                out.write(lines.get(i).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        }
    }


    public static String addToIndex(File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("must come from an existing file");
        }

        byte[] raw = Files.readAllBytes(file.toPath());
        byte[] maybeSquished;
        if (COMPRESS_BLOBS) {
            maybeSquished = compress(raw);
        } else{
            maybeSquished = raw;
        }
        String newHash = SHA1(maybeSquished);

        String givenPath = relPath(file);

        List<String> lines = readIndexLines();
        int existsIndex = -1;
        String oldHash = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int spaceIndex = line.indexOf(' ');
            String hash = line.substring(0, spaceIndex);
            String pth = line.substring(spaceIndex + 1);
            if (pth.equals(givenPath)) {
                existsIndex = i;
                oldHash = hash;
            }
        }

        if (existsIndex >= 0) {
            if (newHash.equals(oldHash)) {
                return newHash;
            } else {
                lines.set(existsIndex, newHash + " " + givenPath);
                writeIndexLines(lines);
                File blob = new File(objects, newHash);
                if (!blob.exists()) {
                    blob(file);
                }
                return newHash;
            }
        } else {
            appendIndexLine(newHash + " " + givenPath);
            File blob = new File(objects, newHash);
            if (!blob.exists()) {
                blob(file);
            }
            return newHash;
        }
    }

    public static void appendIndexLine(String line) throws IOException {
        byte[] bytes = line.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        long len = index.length();
        try (FileOutputStream out = new FileOutputStream(index, true)) {
            if (len > 0){
                out.write('\n');
            }
            out.write(bytes);
        }
    }

    public static List<String> readIndexLines() throws IOException {
        if (!index.exists()){
            return new ArrayList<>();
        }
        return Files.readAllLines(index.toPath(), java.nio.charset.StandardCharsets.UTF_8);
    }

    public static void resetIndex() throws IOException {
        Files.write(index.toPath(), new byte[0]);
    }

    public static void deleteIfExists(File... files) {
        if (files == null) return;
        for (File f : files) {
            if (f == null) continue;
            if (f.isDirectory()) {
                File[] kids = f.listFiles();
                if (kids != null) for (File k : kids) k.delete();
            }
            f.delete();
        }
    }

    public static String tree(File dir) throws IOException {
        if (dir == null || !dir.isDirectory()) {
            throw new IllegalArgumentException("tree must be a directory");
        }

        File[] kids = dir.listFiles();
        if (kids == null){
            kids = new File[0];
        }

        StringBuilder content = new StringBuilder();
        boolean first = true;

        for (File k : kids) {
            if (k.isFile()) {
                String blobHash = blob(k);
                if (!first){
                    content.append('\n');
                }
                content.append("blob " + blobHash + " " + k.getName());
                first = false;
            } else if (k.isDirectory()) {
                String subHash = tree(k);
                if (!first){
                    content.append('\n');
                }
                content.append("tree " + subHash + " " + k.getName());
                first = false;
            }
        }

        byte[] bytes = content.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String treeHash = SHA1(bytes);

        File treeBlob = new File(objects, treeHash);
        if (!treeBlob.exists()) {
            try (FileOutputStream out = new FileOutputStream(treeBlob)) {
                out.write(bytes);
            }
        }

        return treeHash;
    }

    public static int getSlashes(String input){
        int slashes = 0;
        
        for (int i = 0; i < input.length(); i++){
            if (input.charAt(i) == '/'){ // change to '\' if it's not mac
                slashes++;
            }
        }
        return slashes;
    }

    public static void workingList() throws FileNotFoundException, IOException{
        FileReader indexReader = new FileReader(index);
        StringBuilder indexString = new StringBuilder();
        while (indexReader.ready()){
            indexString.append((char)(indexReader.read()));
        }
        indexReader.close();
        
        String[] indexLines = (indexString.toString()).split("\n");
        ArrayList <String> indexArray = new ArrayList<String>(Arrays.asList(indexLines));

        for (int i = 0; i < indexArray.size(); i++){
            String line = indexArray.get(i);
            indexArray.set(i, line.substring(41) + " " + line.substring(0, 40));
        }

        Collections.sort(indexArray); //credit to Zid for teaching me the idea to easily sort

        int maxSlash = 0;
        int maxI = 0;
        while (indexArray.size() > 1){
            for (int i = 0; i < indexArray.size(); i++){
                String line = indexArray.get(i);

                int slashes = getSlashes(line);
                if (slashes > maxSlash){
                    maxSlash = slashes;
                    maxI = i;
                }
            }
            String max = indexArray.get(maxI);
            String path = max.substring(0, max.indexOf(' '));
            // String hash = max.substring(max.length()-40); //check off by one (not needed)
            String parent = "";
            File f = new File(path);
            if (f.exists()){
                parent = f.getParent();
            }
            File parentF = new File(parent);
            String treename = "";
            if (parentF.exists()){
                treename = tree(parentF);
            }
            File parentTree = new File(treename);
            int numbertoremove = 1;
            if (parentTree.exists()){
                FileReader treeReader = new FileReader(index);
                StringBuilder treeIndexString = new StringBuilder();
                while (treeReader.ready()){
                    treeIndexString.append((char)treeReader.read());
                }
                treeReader.close();
                numbertoremove = (treeIndexString.toString()).split("\n").length;
            }
            for (int j = 0; j < numbertoremove; j++){
                indexArray.remove(maxI);
            }
        }

        File workingListFile = new File(git, "workinglist");
        try (FileWriter writer = new FileWriter(workingListFile)) {
            writer.write("tree " + indexArray.get(0).substring(indexArray.get(0).indexOf(' ') + 1) + " (root)");
        }
    }
}
