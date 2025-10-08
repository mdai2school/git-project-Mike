import java.io.*;
import java.nio.file.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Git{
    public static File git;
    public static File objects;
    public static File index;
    public static File HEAD;

    public static boolean COMPRESS_BLOBS = false;

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

    public static String blob(File file) throws IOException{
        if (file == null || !file.isFile()) {
            throw new IllegalArgumentException("blob: source must be an existing file");
        }
        byte[] raw = Files.readAllBytes(file.toPath());
        byte[] pload;
        if (COMPRESS_BLOBS) {
            pload = compress(raw);
        } else {
            pload = raw;
        }
        String blobName = SHA1(pload);
        File blob = makeFile(blobName, objects);
        try (FileOutputStream river = new FileOutputStream(blob)) {
            river.write(pload);
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
        if (hash == null || hash.length() != 40) return false;
        File f = new File(objects, hash);
        return f.exists() && f.isFile();
    }

    public static void resetObjects() throws IOException {
        if (!objects.exists()) return;
        File[] kids = objects.listFiles();
        if (kids == null) return;
        for (File k : kids) k.delete();
    }

    private static byte[] compress(byte[] in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(bos, new Deflater())) {
            dos.write(in);
        }
        return bos.toByteArray();
    }

    private static String relPath(File f) {
        Path bass = Paths.get("").toAbsolutePath().normalize();
        Path p = f.toPath().toAbsolutePath().normalize();
        return bass.relativize(p).toString().replace("\\", "/");
    }

    private static void writeIndexLines(List<String> lines) throws IOException {
        try (FileOutputStream river = new FileOutputStream(index, false)) {
            for (int i = 0; i < lines.size(); i++) {
                if (i > 0) river.write('\n');
                river.write(lines.get(i).getBytes(java.nio.charset.StandardCharsets.UTF_8));
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

        String path = relPath(file);

        List<String> lines = readIndexLines();
        int found = -1;
        String oldHash = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int sp = line.indexOf(' ');
            String hash = line.substring(0, sp);
            String pth = line.substring(sp + 1);
            if (pth.equals(path)) {
                found = i;
                oldHash = hash;
                break;
            }
        }

        if (found >= 0) {
            if (newHash.equals(oldHash)) {
                return newHash;
            } else {
                lines.set(found, newHash + " " + path);
                writeIndexLines(lines);
                File blob = new File(objects, newHash);
                if (!blob.exists()) {
                    blob(file);
                }
                return newHash;
            }
        } else {
            appendIndexLine(newHash + " " + path);
            File blob = new File(objects, newHash);
            if (!blob.exists()) {
                blob(file);
            }
            return newHash;
        }
    }

    private static void appendIndexLine(String line) throws IOException {
        byte[] bytes = line.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        long len = index.length();
        try (FileOutputStream river = new FileOutputStream(index, true)) {
            if (len > 0){
                river.write('\n');
            }
            river.write(bytes);
        }
    }

    // reads everything in index, which tracks all created files

    private static List<String> readIndexLines() throws IOException {
        if (!index.exists()){
            return new ArrayList<>();
        }
        return Files.readAllLines(index.toPath(), java.nio.charset.StandardCharsets.UTF_8);
    }

    public static void resetIndex() throws IOException {
        Files.write(index.toPath(), new byte[0]);
    }

    private static boolean endsWithNewline(File f) throws IOException {
        long len = f.length();
        if (len == 0){
            return false;
        }
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            raf.seek(len - 1);
            int bald = raf.read();
            return bald == '\n' || bald == '\r';
        }
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
            String h = blob(k);
            if (!first){
                content.append('\n');
            }
            content.append("blob " + h + " " + k.getName());
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
        try (FileOutputStream fs = new FileOutputStream(treeBlob)) {
            fs.write(bytes);
        }
    }

    return treeHash;
}

    public static void testHash(){
        String hash1 = SHA1("hello");
        boolean pass1 = hash1.equals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d");
        System.out.println("Test 1!\nexpected:aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d\nreturned:" + hash1);
        if (pass1) { System.out.println("passed!"); } else { System.out.println("failed!"); }

        String hash2 = SHA1("12345");
        boolean pass2 = hash2.equals("8cb2237d0679ca88db6464eac60da96345513964");
        System.out.println("Test 2!\nexpected:8cb2237d0679ca88db6464eac60da96345513964\nreturned:" + hash2);
        if (pass2) { System.out.println("passed!"); } else { System.out.println("failed!"); }

        String hash3 = SHA1("Bald!?");
        boolean pass3 = hash3.equals("b0ac221808a66f8cf0bfcba4a5da29dbcba77e4b");
        System.out.println("Test 3!\nexpected:b0ac221808a66f8cf0bfcba4a5da29dbcba77e4b\nreturned:" + hash3);
        if (pass3) { System.out.println("passed!"); } else { System.out.println("failed!"); }
    }

    public static boolean test() throws IOException{
        // testHash();

        // for (int i = 0; i < 3; i++){
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
        // }

        git = makeFolder("git");
        objects = makeFolder("objects", git);
        if (!index.exists()){
            index = makeFile("index", git);
        }
        if (!HEAD.exists()){
            HEAD  = makeFile("HEAD", git);
        }
        resetObjects();
        resetIndex();

        File myProgram = new File("myProgram");
        myProgram.mkdir();
        File scripts = new File(myProgram, "scripts");
        scripts.mkdir();

        File readme = new File(scripts, "README.md");
        File helloA = new File(myProgram, "Hello.txt");
        File helloB = new File(scripts, "Hello.txt");
        File cat = new File(scripts, "Cat.java");

        try (FileOutputStream r = new FileOutputStream(readme)) {
            r.write("readme\n".getBytes());
        }
        try (FileOutputStream h1 = new FileOutputStream(helloA)){
            h1.write("hello world\n".getBytes());
        }
        try (FileOutputStream h2 = new FileOutputStream(helloB)) {
            h2.write("hello world\n".getBytes());
        }
        try (FileOutputStream c  = new FileOutputStream(cat))    {
            c.write("class Cat {}\n".getBytes());
        }

        String hReadme = addToIndex(readme);
        String hHelloA1 = addToIndex(helloA);
        String hHelloA2 = addToIndex(helloA);
        String hHelloB  = addToIndex(helloB);
        String hCat     = addToIndex(cat);

        if (!hHelloA1.equals(hHelloA2)) {
            System.out.println("note: hashes differ after duplicate add? that shouldn't happen");
        }

        List<String> lines = readIndexLines();
        java.util.Set<String> set = new java.util.HashSet<>(lines);

        String lineReadme = hReadme + " " + ("myProgram/scripts/README.md");
        String lineHelloA = hHelloA1 + " " + ("myProgram/Hello.txt");
        String lineHelloB = hHelloB  + " " + ("myProgram/scripts/Hello.txt");
        String lineCat    = hCat     + " " + ("myProgram/scripts/Cat.java");

        if (!(set.contains(lineReadme) && set.contains(lineHelloA) && set.contains(lineHelloB) && set.contains(lineCat))) {
            System.out.println("failed! expected index entries missing (relative paths or hashes)");
            deleteIfExists(readme, helloA, helloB, cat, scripts, myProgram);
            return false;
        }

        String scriptsTree = tree(scripts);
        String rootTree    = tree(myProgram);
        System.out.println("trees built: scripts=" + scriptsTree + " root=" + rootTree);

        try (FileOutputStream h1m = new FileOutputStream(helloA)) { h1m.write("HELLO MOD\n".getBytes()); }
        String hHelloAnew = addToIndex(helloA);

        List<String> linesAfter = readIndexLines();
        java.util.Map<String,String> pathToHash = new java.util.HashMap<>();
        for (String ln : linesAfter) {
            int sp = ln.indexOf(' ');
            if (sp > 0) {
                pathToHash.put(ln.substring(sp+1), ln.substring(0, sp));
            }
        }
        if (!hHelloAnew.equals(pathToHash.get("myProgram/Hello.txt"))) {
            System.out.println("failed! changed file did not update its line in index :/");
            deleteIfExists(readme, helloA, helloB, cat, scripts, myProgram);
            return false;
        }

        if (endsWithNewline(index)) {
            System.out.println("failed! index should not end with a new line :[");
            deleteIfExists(readme, helloA, helloB, cat, scripts, myProgram);
            return false;
        }

        resetObjects();
        resetIndex();
        deleteIfExists(readme, helloA, helloB, cat, scripts, myProgram);

        System.out.println("All tests passed :D");
        return true;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(test());
    }
}
