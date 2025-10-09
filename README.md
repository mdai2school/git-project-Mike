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
When global boolean compression is off, the hash is computed from the raw file contents.
When compression is on, the file is compressed before it is stored (by default we still hash the raw content unless otherwise specified by the assignment).


5. blobExists
parameters:     (String hash)
what it does:   checks if the blob file with a given hash exists inside the objects folder
returns:        true/false depending on existence


6. resetObjects
parameters:     none
what it does:   deletes all files inside the "objects" folder to reset it
returns:        nothing


7. compress
parameters:     (byte[] input)
what it does:   compresses an input byte array using Java’s Deflater class
returns:        compressed byte array

help from:
https://www.geeksforgeeks.org/advance-java/java-util-zip-deflateroutputstream-class-java


8. relPath
parameters:     (File f)
what it does:   converts the absolute path of a file into a relative path from the current working directory
returns:        relative path string


9. writeIndexLines
parameters:     (List<String> lines)
what it does:   overwrites the index file with all lines in the given list, separated by newline characters
returns:        nothing


10. appendIndexLine
parameters:     (String line)
what it does:   appends one line to the index file, adds a newline before if it’s not empty
returns:        nothing


11. readIndexLines
parameters:     none
what it does:   reads all lines from the index file and stores them in a list
returns:        List<String> of index entries


12. resetIndex
parameters:     none
what it does:   clears all contents of the index file (makes it empty)
returns:        nothing


13. deleteIfExists
parameters:     (File... files)
what it does:   deletes a list of files or folders (including all their contents if they’re directories)
returns:        nothing


14. addToIndex
parameters:     (File file)
what it does:   adds a file to the index by computing its hash and storing it in the "objects" folder
returns:        the hash of the file contents

specifically:
- adds one line per file in the format: "<hash> <path>"
- if the file was already added and unchanged, it will not be duplicated
- if the file was modified, the hash and blob will be updated
- if the file doesn’t exist, an error is thrown


15. index
The index file keeps track of all added files in the repository.
The program adds a line in the format:

<hash> <relative/path/to/file>

cases:
If the same file from the same directory with the same contents is added again, it is ignored.
If the same contents appear in a different folder, both entries are kept.
If a file is modified and added again, the old entry is replaced with the new hash and a new blob is created.
There is exactly one space between the hash and the file path, and no trailing newline at the end of the index file.

16. tree
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


17. getSlashes
parameters:     (String input)
what it does:   counts how many forward slashes "/" exist in the string (used to find nesting depth)
returns:        number of slashes as an integer


18. workingList
parameters:     none
what it does:   generates the working list of the repository from the index file and writes it into "git/workinglist"
returns:        nothing

steps:
1) reads the index file and stores each line
2) reorders each line to "<path> <hash>"
3) sorts all entries alphabetically
4) iteratively finds the deepest file paths to collapse into trees
5) writes a final line in "git/workinglist" in the format:
   "tree <rootTreeHash> (root)"
