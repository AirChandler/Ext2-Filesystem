import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <h1>Handles the methods for reading the requested INode data
 */
public class INode {
    //Volume data
    RandomAccessFile dataStream;
    ByteBuffer INodeBuffer;
    Helper help = new Helper();
    ArrayList<INode> inodeList = new ArrayList<INode>();
    byte[] INodeBlock = new byte[128];
    int INodeBlockPos;
    int INodeOffsetPos;
    int numGroupINodes;
    int numGroupBlocks;
    Directory directory = new Directory();
    ArrayList<String> dirNames = new ArrayList<String>();
    ArrayList<Integer> dirINodes = new ArrayList<Integer>();
    //INode data
    short fileMode;
    short userID;
    short groupID;
    int lastModifiedTime;
    String lastModString = "";
    int hardLinks;
    int iPointer;
    int dIPointer;
    int tIPointer;
    int lowFileSize;
    int upperFileSize;
    long fileSize;
    /**
     * INode data block positions
     */
    int[] dataBlocks = new int[12];

    /**
     * Holds the hex values for identifying the User, Group and Other
     * read, write and execute permissions.
     */
    static final int[] perms = {0x0100, 0x0080, 0x0040, 0x0020, 0x0010, 0x0008, 0x0004, 0x0002, 0x0001};
    String permissions = "";
    static final int socket = 0xC000;
    static final int symbLink = 0xA000;
    static final int file = 0x8000;
    static final int blockDevice = 0x6000;
    static final int dir = 0x4000;
    static final int charDevice = 0x2000;
    static final int fifo = 0x1000;
    char fileType;

    /**
     * Handles the position of where the INode lies,
     * reading the INode's 128 bytes into a ByteBuffer as well as calling the
     * method to read the INode's bytes.
     * @param inodeNumber the INode we wish to read
     * @param stream the reference to our EXT2 volume
     * @param INodeBlockNum the reference to which block the INode is in
     * @param numGroupI the number of INodes in a group
     * @param numGroupB the number of blocks in a group
     */
    public INode(int inodeNumber, RandomAccessFile stream, int INodeBlockNum, int numGroupI, int numGroupB){
        numGroupINodes = numGroupI;
        numGroupBlocks = numGroupB;
        if(inodeNumber/numGroupINodes > 0){
            INodeBlockNum += (inodeNumber/numGroupINodes)*numGroupB;
            if(inodeNumber/numGroupINodes > 1){
                INodeBlockNum = 16387;
            }
            inodeNumber = inodeNumber%numGroupINodes;
        }
        dataStream = stream;
        INodeBlockPos = INodeBlockNum*1024;
        INodeOffsetPos = (inodeNumber-1)*128;
        INodeBuffer = ByteBuffer.allocate(128);
        INodeBuffer.order(ByteOrder.LITTLE_ENDIAN);
        try {
            dataStream.seek(INodeBlockPos + INodeOffsetPos);
            dataStream.read(INodeBlock);
            INodeBuffer.put(INodeBlock);
        }catch(IOException ex){
            System.out.println("Error reading file " + ex);
        }
        //System.out.println("INode " + inodeNumber + " data:"); Print out debug
        //help.dumpHexBytes(INodeBlock);
        readINodeData();
    }


    /**
     * Handles reading any files in the current INode's directory
     */
    public void readDir(){
        byte[] blockData = new byte[1024];
        int counter = 0;
        ByteBuffer blockBuffer = ByteBuffer.allocate(1024);
        blockBuffer.order(ByteOrder.LITTLE_ENDIAN);
        try {
            dataStream.seek(1024 * dataBlocks[0]);
            dataStream.read(blockData);
            blockBuffer.put(blockData);
        }catch(IOException ex){
            System.out.println("Error reading file " + ex);
        }
        while(counter < blockData.length){
            content(counter, blockBuffer);
            counter += blockBuffer.getShort(counter+4);
        }
    }

    /**
     * Handles reading the content of a file in a directory
     * @param counter the position of the current file's data in the current INode data block
     * @param blockBuffer the buffer which holds the INode data block
     */
    private void content(int counter, ByteBuffer blockBuffer){
        int INodeNum = 0;
        int nameSizePos = 6;
        int nameLength;
        int type = 0;
        byte[] nameBytes;
        String fileName = "";
        INode dataINode;

        nameSizePos += counter;
        nameLength = blockBuffer.get(nameSizePos);
        nameBytes = new byte[nameLength];
        INodeNum = blockBuffer.getInt(counter + 0);
        type = blockBuffer.get(counter+7);
        for(int i = 0; i<nameLength; i++) {
            nameBytes[i] = blockBuffer.get(counter + 8 + i);
        }
        fileName = new String(nameBytes);
        dirNames.add(fileName);
        dirINodes.add(INodeNum);
        dataINode = new INode(INodeNum, dataStream, 84, numGroupINodes, numGroupBlocks);
        inodeList.add(dataINode);
        directory.add(new StringBuilder(dataINode.getFileType() + dataINode.getPermissions() + " | " + dataINode.getUserID() + " | " + dataINode.getGroupID() + " | " + dataINode.getFileSize() + " | " + dataINode.getLastModString() + " | " + fileName).toString());
    }

    /**
     * Handles returning the contents of a directory
     * @return the directory contents
     */
    public Directory getDir(){
        return directory;
    }

    /**
     * Handles the reading of all the INode's data, including it's file type
     * and user, group and other's permissions to read, write and execute the
     * file.
     */
    private void readINodeData(){
        //INode permissions
        fileMode = INodeBuffer.getShort(0);

        //INode user & group ID
        userID = INodeBuffer.getShort(2);
        groupID = INodeBuffer.getShort(24);

        //INode last modified time (epoch format)
        lastModifiedTime = INodeBuffer.getInt(16);
        long timeStamp = (long) lastModifiedTime * 1000;
        Date date = new Date(timeStamp);
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm");
        lastModString = formatter.format(date);

        //INode ref links
        hardLinks = INodeBuffer.getInt(26);
        for (int i = 0; i < dataBlocks.length; i++) {
            dataBlocks[i] = INodeBuffer.getInt(40 + (i * 4));
        }
        iPointer = INodeBuffer.getInt(88);
        dIPointer = INodeBuffer.getInt(92);
        tIPointer = INodeBuffer.getInt(96);

        //INode file size
        lowFileSize = INodeBuffer.getInt(4);
        upperFileSize = INodeBuffer.getInt(108);
        byte[] low16 = ByteBuffer.allocate(4).putInt(lowFileSize).array();
        byte[] upper16 = ByteBuffer.allocate(4).putInt(upperFileSize).array();
        byte[] fileSizeBytes = new byte[8];
        for(int i = 0; i < fileSizeBytes.length; i++){
            if(i<=3){
                fileSizeBytes[i] = upper16[i];
            }
            else{
                fileSizeBytes[i] = low16[i-4];
            }
        }
        fileSize = new BigInteger(fileSizeBytes).longValue();

        //INode permissions
        for(int i = 0; i < perms.length; i++){
            if((fileMode & perms[i])==perms[i]){
                if(i%3 == 0){
                    permissions += 'r';
                }
                else if(i%3 == 1){
                    permissions += 'w';
                }
                else if(i%3 == 2){
                    permissions += 'x';
                }
            }
            else{
                permissions += '-';
            }
        }

        //INode file type
        if((fileMode & socket) == socket){
            fileType = 's';
        }
        else if((fileMode & symbLink) == symbLink){
            fileType = 'l';
        }
        else if((fileMode & file) == file){
            fileType = '-';
        }
        else if((fileMode & blockDevice) == blockDevice){
            fileType = 'b';
        }
        else if((fileMode & dir) == dir){
            fileType = 'd';
        }
        else if((fileMode & charDevice) == charDevice){
            fileType = 'c';
        }
        else if((fileMode & fifo) == fifo){
            fileType = 'p';
        }
    }

    public int[] getDataBlocks(){
        return dataBlocks;
    }
    /**
     * Handles returning the file type of a INode
     * @return the file type
     */
    public char getFileType(){
        return fileType;
    }

    /**
     * Handles returning the access permissions of a file/directory
     * @return the access permissions
     */
    public String getPermissions(){
        return permissions;
    }

    /**
     * Handles returning the userID of a file/directory
     * @return the userID
     */
    public int getUserID(){
        return userID;
    }

    /**
     * Handles returning the groupID of a file/directory
     * @return the groupID
     */
    public int getGroupID(){
        return groupID;
    }

    /**
     * Handles returning the last modified date of a file/directory
     * @return the last modified date
     */
    public String getLastModString(){
        return lastModString;
    }

    /**
     * Handles returning the last size of a file/directory
     * @return the file size
     */
    public long getFileSize(){
        return fileSize;
    }

    /**
     * Handles returning the inode numbers of each file in the directory
     * @return the INode of each file in the directory
     */
    public ArrayList<Integer> getDirINodes(){
        return dirINodes;
    }

    /**
     * Handles returning the names of each file in the directory
     * @return each file name in the directory
     */
    public ArrayList<String> getDirNames(){
        return dirNames;
    }

}