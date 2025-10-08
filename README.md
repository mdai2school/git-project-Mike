# git-project-Mike
Main:
Running the Git.java file creates a folder "git" that contains folder "objects" and files "HEAD" and "index".
It uses the methods makeFolder and makeFile explained later.
If ALL of the files/folders already existed, the program will output "Git Repository Already Exists".
Else, the program will output "Git Repository Created".

The files should not exist already by default because "git" is included in .gitignore, but if they do, the program will output "Git Repository Already Exists"

1. makeFolder
parameters:     (String folderName) or (String folderName, File folder)
what it does:   makes a new folder named folderName that is optionally inside a folder
returns:        the folder created

cases:
if the inputted folder doesn't exist, the code will throw the IllegalArgumentException:
"Attempted to create a folder within a folder that does not exist!";

can update the global boolean AlreadyExists to FALSE if the created folder does NOT exist already


2. makeFile
parameters:     (String fileName) or (String fileName, File folder)
what it does:   makes a new file named fileName that is optionally inside a folder
returns:        the file created

cases:
if the inputted folder doesn't exist, the code will throw the IllegalArgumentException:
"Attempted to create a file within a folder that does not exist!";

can update the global boolean AlreadyExists to FALSE if the created file does NOT exist already


3. SHA1
parameters:     (String message)
what it does:   returns a String that is SHA-1 hash of message
returns:        the hash string

cases:
The output is 20 bytes, or 40 hexadecimal characters.
If the hash is shorter than 40 characters, the program will add "0"'s at the beginning until it is the correct length.
The program can also throw a runtime error if the "SHA-1" algorithm doesn't exist.

How I implemented it:
(with help from: https://www.geeksforgeeks.org/java/sha-1-hash-in-java/)
1) Takes in a String called "message"
2) Creates "md", a MessageDigest that uses the SHA-1 algorithm
3) Creates "digestedMessage", an array of bytes which converts the byte form of "message" into its SHA-1 hash
4) Creates "number", a BigInteger that is the converted number form of "digestedMessage"
5) Creates "hashtext" the hexadecimal String from "number"
6) Ensures that "hashtext" is 40 characters and adds "0" at the start if it is not
7) returns hashtext

Also created testHash(), which has no parameters or outputs.
It simply tests my SHA1 hash for 3 set cases and prints out the results (my SHA1 passed).


4. blob
parameters:     (File file)
what it does:   reads the exact bytes of a given file, computes the SHA-1 hash of its contents (or optionally of a compressed version), and stores that data in a new file inside "git/objects" named after the hash
returns:        the 40-character hash string

cases:
If the input file is invalid, it throws an IllegalArgumentException.
When compression is off, the hash is computed from the raw file contents.
When compression is on, the file is compressed before it is stored (by default we still hash the raw content unless otherwise specified by the assignment).


5. index
The `index` file keeps track of all added files in the repository.
The program adds a line in the format:

<hash> <relative/path/to/file>

cases:
If the same file from the same directory with the same contents is added again, it is ignored.
If the same contents appear in a different folder, both entries are kept.
If a file is modified and added again, the old entry is replaced with the new hash and a new blob is created.
There is exactly one space between the hash and the file path, and no trailing newline at the end of the index file.

Example paths used in testing:
myProgram/Hello.txt
myProgram/scripts/Hello.txt
myProgram/scripts/Cat.java
myProgram/scripts/README.md


6. Compression
We added a global boolean COMPRESS_BLOBS.
If set to true, blob() will compress the file content before writing it into the objects folder.
We used Java’s built-in java.util.zip.Deflater and DeflaterOutputStream to do the compression.

help from: https://www.geeksforgeeks.org/advance-java/java-util-zip-deflateroutputstream-class-java


7. tree (directory trees)
parameters:     (File dir)
what it does:   creates a “tree object” for a directory. For each immediate child:
- files add a line:  blob <SHA1> <name>
- subdirectories add: tree <SHA1> <name>      (the subdirectory is built recursively first)

returns:        the SHA-1 hash of the tree’s text content

details:
- The tree content has no trailing newline.
- The tree text is written to "git/objects/<treeHash>" if it doesn't already exist.
- Child names are relative to the directory (no full paths).
- We do not sort; the order follows File.listFiles() for the OS.
