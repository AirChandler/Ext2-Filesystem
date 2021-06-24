import java.io.*;
import java.util.ArrayList;

/**
 * <h1>Handles the reading of a file
 */
public class Ext2File {
    RandomAccessFile stream;
    Helper help = new Helper();
    String file;
    int fileINode;
    int fileStart;
    long fileLength;
    byte[] readBlock;
    ArrayList<String> dirNames;
    ArrayList<Integer> dirINodes;
    Boolean found = false;

    /**
     * Handles reading the files in the system
     * @param dataStream the volume to read from
     * @param fileToFind the file to find and print
     */
    public Ext2File(RandomAccessFile dataStream, String fileToFind){
        stream = dataStream;
        file = fileToFind;
        int[] dataPoints;
        INode rootINode = new INode(2, dataStream, 84, 1712, 8192); //root directory's inode info
        rootINode.readDir();
        findFile(rootINode);
        if(found == true) {
            INode dataINode = new INode(fileINode, stream, 84, 1712, 8192);
            fileLength = dataINode.getFileSize();
            dataPoints = dataINode.getDataBlocks();
            fileStart = dataPoints[0] * 1024;
            try{
                stream.seek(dataPoints[0]*1024);
            }catch(IOException ex){
                System.out.println("Error reading file " + ex);
            }
        }
        else{
            System.out.println("File not found! ");
        }
    }

    /**
     * handles finding a file given the root INode
     * @param node the inode to check which files/directories it contains
     */
    private void findFile(INode node){
        dirNames = node.getDirNames();
        dirINodes = node.getDirINodes();
        found = false;
        for(int i = 0; i < dirNames.size(); i++){
            System.out.println("Directory " + dirNames.get(i));
            if(dirNames.get(i).equals(file)) {
                fileINode = dirINodes.get(i);
                found = true;
                break;
            }
        }
    }

    /**
     * handles reading the file bytes between start and length
     * @param startByte the starting byte of the file to read from
     * @param length the last byte we read
     */
    public byte[] read (long startByte, long length){
        try {
            if(stream.length() < length){
                length = length - (length - fileLength);
            }
            if(fileStart + startByte > fileStart+fileLength || startByte < 0){
                System.out.println("Error startbyte not valid!");
                return readBlock;
            }
            readBlock = new byte[(int) length];
            seek(startByte);
            stream.read(readBlock);
            return readBlock;
        }catch(IOException ex) {
            System.out.println("Error reading file: " + ex);
        }catch(IndexOutOfBoundsException ex){
            System.out.println("Error: Not a valid starting byte to read from");
        }
        return readBlock;
    }

    /**
     * handles reading up to length file bytes
     * @param length the last byte we read
     */
    public byte[] read (long length){
        try {
            if (stream.length() < length) {
                length = length - (length - fileLength);
            }
            readBlock = new byte[(int) length];
            stream.read(readBlock);
            return readBlock;
        }catch (IOException ex){
            System.out.println("Error readin file " + ex);
        }
        return readBlock;
    }

    /**
     * handles moving a position in our file
     * @param position the position of the file to move to
     */
    public void seek (long position){
        try {
            stream.seek(fileStart + position);
        }catch(IOException ex){
            System.out.println("Error reading file: " + ex);
        }catch(IndexOutOfBoundsException ex){
            System.out.println("Error position is outside the range of the file " + ex);
        }
    }

    public long size(){
        return fileLength;
    }
}
