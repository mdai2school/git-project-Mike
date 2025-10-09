import java.io.*;

public class Test extends Git {
    public static void main(String[] args) throws IOException {
        git = makeFolder("git");
        objects = makeFolder("objects", git);
        index = makeFile("index", git);
        HEAD = makeFile("HEAD", git);
        resetObjects();
        resetIndex();

        new File("projects").mkdir();
        new File("projects/myProgram").mkdir();
        new File("projects/myProgram/scripts").mkdir();

        try (FileOutputStream out = new FileOutputStream("projects/myProgram/scripts/README.md")) {
            out.write("readme\n".getBytes());
        }
        try (FileOutputStream out = new FileOutputStream("projects/myProgram/Hello.txt")) {
            out.write("hello world\n".getBytes());
        }
        try (FileOutputStream out = new FileOutputStream("projects/myProgram/scripts/Hello.txt")) {
            out.write("hello world\n".getBytes());
        }
        try (FileOutputStream out = new FileOutputStream("projects/myProgram/scripts/Cat.java")) {
            out.write("class Cat {}\n".getBytes());
        }

        File readme    = new File("projects/myProgram/scripts/README.md");
        File helloA    = new File("projects/myProgram/Hello.txt");
        File helloB    = new File("projects/myProgram/scripts/Hello.txt");
        File cat       = new File("projects/myProgram/scripts/Cat.java");
        File scripts   = new File("projects/myProgram/scripts");
        File myProgram = new File("projects/myProgram");

        addToIndex(readme);
        addToIndex(helloA);
        addToIndex(helloB);
        addToIndex(cat);

        String scriptsTree = tree(scripts);
        String rootTree    = tree(myProgram);
        System.out.println("trees built: scripts=" + scriptsTree + " root=" + rootTree);

        System.out.println("Working List:");
        workingList();

        new File("projects/myProgram/scripts/README.md").delete();
        new File("projects/myProgram/Hello.txt").delete();
        new File("projects/myProgram/scripts/Hello.txt").delete();
        new File("projects/myProgram/scripts/Cat.java").delete();
        new File("projects/myProgram/scripts").delete();
        new File("projects/myProgram").delete();
        new File("projects").delete();
    }
}
