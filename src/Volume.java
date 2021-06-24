import java.io.*;
/**
 * <h1>Handles the creation of a RandomAccessFile,
 * the object we used to read bytes from our EXT2 file.
 */
public class Volume {
    RandomAccessFile ourVolume;

    /**
     * Handles the creation of a RandomAccessFile,
     * the object we used to read bytes from our EXT2 file.
     * @param volumeFile holds where our volume is stored in String format.
     */
    public Volume(String volumeFile){
        try {
            ourVolume = new RandomAccessFile(volumeFile, "r");
        }catch(FileNotFoundException ex){
            System.out.println("Error file not found: " + ex);
        }catch(Exception ex){
            System.out.println("Unknown error: " + ex);
        }
    }

    /**
     * Handles returning a reference to our RandomAccessFile
     * @return returns our RandomAccessFile reading our volume data.
     */
    public RandomAccessFile getFile(){
        return ourVolume;
    }

}
