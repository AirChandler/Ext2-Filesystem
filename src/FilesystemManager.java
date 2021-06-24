//EXT2 volume reader, Chandler Keep
/**
 * <h1>Handles the instantiation of the classes used to to read
 * a EXT2 volume & it's files and directories</h1>
 */
public class FilesystemManager {
    Helper help = new Helper();
    /**
     * Initialises the instantiation of FilesystemManager.
     * @param args unused
     */
    public static void main(String args[]){
        FilesystemManager ourFileSystem = new FilesystemManager();
    }

    /**
     * Handles the instantiation of our volume, data and file classes.
     */
    public FilesystemManager(){
        Volume  vol = new Volume("Filesystem/ext2fs");
        DataManager data = new DataManager(vol.getFile()); //Read data inc. superblock, group descriptor and root inode
        Ext2File  f = new Ext2File (vol.getFile(), "two-cities");
        byte buf[ ] = f.read(1, 10);
        help.dumpHexBytes(buf);
    }

}
