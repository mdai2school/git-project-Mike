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