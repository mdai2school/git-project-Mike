# git-project-Mike
Main:
Running the Git.java file on creates a folder "git" that contains folder "objects" and files "Head" and "index". It uses the methods makeFolder and makeFile explained later. If ALL of the files/folders already existed, the program will output "Git Repository Already Exists".
Else, the program will output "Git Repository Created". 

The files should not exist already by default because "git" is included in .gitignore, but if they do, the program will output "Git Repository Already Exists"

1. makeFolder
parameters:     (String folderName) or (String folderName, File folder)
what it does:   makes a new folder named folderName that is optionally inside a folder
returns:        the folder created

cases:
if the inputted folder doesn't exist, the code will throw the IllegalArgumentException: "Attempted to create a folder within a folder that does not exist!";

can update the global boolean alreadyExists to FALSE if the created folder does NOT exist already


2. makeFile
parameters:     (String fileName) or (String fileName, File folder)
what it does:   makes a new file named fileName that is optionally inside a folder
returns:        the file created

cases:
if the inputted folder doesn't exist, the code will throw the IllegalArgumentException: "Attempted to create a file within a folder that does not exist!";

can update the global boolean alreadyExists to FALSE if the created file does NOT exist already

3. SHA1
parameters:     (String message)
what it does:   returns a String that is SHA-1 hash of message
returns:        the file created

cases:
The output is 20 bytes, or 40 hexadecimal characters.

If the hash happens to be shorter than 40 characters, the program will add "0"'s at the beginning until its the correct length.

The program can also throw a runtime error if the "SHA-1" algorithm doesn't exist

How I implemented it:
(with help from: https://www.geeksforgeeks.org/java/sha-1-hash-in-java/)
1) Takes in a String called "message"
2) Creates "md", a MessageDigest that uses the SHA-1 algorithm
3) Creates "digestedMessage", an array of bytes which converted the byte form of "message" into its SHA-1 hash.
4) Creates "number", a BigInteger that is the converted number form of "digestedMessage"
5) Creates "hashtext" the hexadecimal String from "number"
6) Ensures that "hashtext" is 40 characters and adds "0" at the start if it is not
7) returns hashtext!

Also created testHash(), which has no parameters or outputs. It simply tests my SHA1 hash for 3 set cases and prints out the results (my SHA1 passed). 

4. test
parameters:     none
what it does:   runs the main 3 times, testing that each file exists and deletes them after each attempt
returns:        boolean if the tests succeded/failed

cases:
will stop early and return false if expected files are missing
returns true if it works!