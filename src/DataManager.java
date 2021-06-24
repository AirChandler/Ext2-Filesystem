import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * <h1>Handles the reading & storing of our superblock, group descriptor data
 * as well as the creation of our INode class.
 */
public class DataManager {
    RandomAccessFile dataStream;
    Helper helper = new Helper();
    INode rootINode;
    Directory dir;

    //Superblock
    byte[] sbBytes = new byte[1024];
    ByteBuffer sbBuffer;
    byte[] ext2Num = new byte[2];
    int ext2NumEndPos = 57;
    int numInodes;
    int numGroupInodes;
    int inodeSize;
    int numBlocks;
    int numGroupBlocks;
    int volLabelPos = 120;
    byte[] volumeBytes = new byte[16];
    String volumeLabel;

    //Group Descriptor
    byte[] gdBytes = new byte[32];
    ByteBuffer gdBuffer;
    int firstInodeBlock;

    /**
     * Identifies where we want to start in the filesystem, 2 is at the very top (root)
     */
    int rootINodeID = 2;

    /**
     * Handles the calls and print formatting of reading our Superblock and
     * Group descriptor data as well as calling the class to create a INode.
     * @param file The reference to our RandomAccessFile which reads the EXT2 volume.
     */
    public DataManager(RandomAccessFile file){
        dataStream = file;
        System.out.println("Superblock data: ");
        readSB();
        System.out.println("-----------------------------------------------------------");
        System.out.println("Group descriptor data: ");
        readGD();
        System.out.println("-----------------------------------------------------------");
        readIN();
        System.out.println("FileType/Permissions | UserID | GroupID | FileSize | Date | Name");
        readDir();
        System.out.println("-----------------------------------------------------------");
    }

    /**
     * Handles the instantiation of our INode class and reading it's data.
     */
    private void readIN(){
        rootINode = new INode(rootINodeID, dataStream, firstInodeBlock, numGroupInodes, numGroupBlocks); //root directory's inode info
    }

    /**
     * Handles adding each directory item of the root inode to a directory
     * and also prints the directory info out.
     */
    private void readDir(){
        rootINode.readDir();
        dir = rootINode.getDir();
        dir.getFileInfo();
    }

    /**
     * Handles the reading of the Group Descriptor data
     */
    private void readGD(){
        gdBuffer = ByteBuffer.allocate(gdBytes.length);
        gdBuffer.order(ByteOrder.LITTLE_ENDIAN);
        try{
            dataStream.read(gdBytes);
            helper.dumpHexBytes(gdBytes);
        }catch(IOException ex){
            System.out.println("Error reading file " + ex);
        }
        gdBuffer.put(gdBytes);
        firstInodeBlock = gdBuffer.getInt(8);
        System.out.println("Block containing first INode: " + firstInodeBlock);
    }
    /**
     * Handles the reading of the Superblock data
     */
    private void readSB(){
        sbBuffer = ByteBuffer.allocate(sbBytes.length);
        sbBuffer.order(ByteOrder.LITTLE_ENDIAN);
        try {
            dataStream.seek(1024); //Skip boot block
            dataStream.read(sbBytes);   //Read Superblock
        }catch(IOException ex){
            System.out.println("Error reading file " + ex);
        }
        helper.dumpHexBytes(sbBytes);
        sbBuffer.put(sbBytes);
        //ext2 filesystem number
        for(int i = 0; i < ext2Num.length; i++){
            ext2Num[i] = sbBuffer.get(ext2NumEndPos-i);         //Convert to ordered hex value (big endian)
        }
        System.out.println("ext2 filesystem unique number: ");
        helper.dumpHexBytes(ext2Num);

        //INode data
        numInodes = sbBuffer.getInt(0);
        System.out.println("Number of INodes in ext2 filesystem: " + numInodes);
        numBlocks = sbBuffer.getInt(4);
        numGroupInodes = sbBuffer.getInt(40);
        System.out.println("Number of INodes in each block group: " + numGroupInodes);
        inodeSize = sbBuffer.getInt(88);
        System.out.println("Size of each INode in ext2 filesystem: " + inodeSize);

        //Block data
        System.out.println("Number of blocks in ext2 filesystem: " + numBlocks);
        numGroupBlocks = sbBuffer.getInt(32);
        System.out.println("Number of blocks in each block group: " + numGroupBlocks);

        //Volume name
        for(int i = 0; i<volumeBytes.length; i++){
            volumeBytes[i] = sbBuffer.get(volLabelPos+i); //In a 8-bit-per-character system the string chars will always be stored in order,
        }                                                 //independent of the endian order of the higher-level architecture. see: https://stackoverflow.com/questions/1568057/ascii-strings-and-endianness
        volumeLabel = new String(volumeBytes);
        System.out.println("Ext2 volume name: " + volumeLabel);
    }
}
