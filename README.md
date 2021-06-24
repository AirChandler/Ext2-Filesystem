# Ext2-Filesystem
Second Year SCC 211 Operating Systems Coursework 

A filesystem is just a structured storage format/ data structure for files, typically on a disk. We commonly think of this as a tree structure formed of directories/ folders, with files as the leaves of the tree.

Project is to read and display contents of a ext2 filesystem. Read (Main detail https://andrew-scott.uk/ext2/ & extra detail https://andrew-scott.uk/ext2/files/linuxkernelch17.pdf)

Key points:
Overview:
-multi-byte numbers are held in little endian format.

-Each file treated the same and each file has a specified length in bytes and will be held in a number of fixed sized blocks (wasted space if memory block not fully taken up).

-We find the set of blocks that make up a file by looking at a special data structure known as an inode. Each file has an inode that holds information about the file, including its size, ownership, access permissions, creation and last modified times, and pointers to all the blocks that form the file.

Directories/INodes
-Directories can be considered as regular files. Directories are held in a set of blocks and have an inode just as any normal file.

-The content of a directory 'file' is in a well defined format that allows us to identify the names of the files in the directory and importantly, to locate the inodes for each of these files.

-The root directory is always found in the data blocks referenced by inode 2 (2 is the identifier for the the inode)
-References to the current directory '.' and parent directory '..' appear in the directory
-All filenames are padded to a multiple of 4 bytes with zero bytes
-If we know the starting position (in bytes) of one entry/ 'row' we can find the start of the next entry by adding the value in the length field
-The file type field in the example below tells us we have one regular 'text' file (test) and four directories (., .., lost+found, home)
-The values in the length field total to a multiple of the block size, in this case 1024 bytes: (12 + 12 + 20 + 12 + 968 = 1024)
-Entries should not span two blocks. The previous entry should be padded to the end of a block so the next can start in a new block.
-See inode diagram for what it contains


Volumes-
-See volume layout

-The Group Descriptor is a list of first block numbers for each field/ part of the block group. For example, if the Inode Table Pointer field held the value 6 we would know that the Inode Table starts with block 6.

-The process to find a file now becomes:

	Find Block Group 0 (starts at byte 1024)
	Find the Group Descriptor
	Read the Inode Table Pointer value to find the block containing the first inode
	Read Inode 2 (the root directory/ top of the filesystem) -- the length of each inode is 	given in the Superblock
	Traverse the filesystem as shown above to locate the file and its contents/ data blocks
1712 inodes in each block group, 1712*128 total inode table

directly linked files
-inode points to data not another inode
