import java.util.ArrayList;

/**
 * <h1>Handles the methods for managing the contents of a directory
 */
public class Directory {
    /**
     * Holds the information of each directory item of the current directory
     */
    ArrayList<String> info = new ArrayList<String>();

    /**
     * Handles adding a directory file
     * @param file the file
     */
    public void add(String file){
        info.add(file);
    }

    /**
     * Handles printing out the information of a directory
     */
    public void getFileInfo(){
        for(int i = 0; i < info.size(); i++){
            System.out.println(info.get(i));
        }
    }
}
