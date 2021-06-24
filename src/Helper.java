import java.util.Formatter;

/**
 * <h1>Handles the methods for printing out contents of a volume in hexadecimal format
 */
public class Helper {

    /**
     * Handles printing out an array of byte's in hex with extra formatting
     * @param bytes the bytes to convert to hex and print
     */
    public void dumpHexBytes(byte[] bytes){

        //Hex print out
        int wordCount = 0;
        int n = 0;
        for(int i = 0; i < bytes.length; i++){
            Formatter formatter = new Formatter();
            if(n == 8){
                System.out.print("| " + formatter.format("%02X", bytes[i]).toString() + " ");
            }
            else if(n == 15){
                n = -1;
                System.out.print(formatter.format("%02X", bytes[i]).toString() + " | ");
                wordCount += 16;
                for(int x = wordCount-16; x < wordCount; x++){
                    if(bytes[x] < 128) {
                        System.out.print((char) bytes[x]);
                    }
                    else{
                        System.out.print(".");
                    }
                }
                System.out.println();
            }
            else{
                System.out.print(formatter.format("%02X", bytes[i]).toString() + " ");
            }
            n++;
        }
        if(n != 0){
            for(int x = n; x < 16; x++){
                if(x == 8){
                    System.out.print("| XX ");
                }
                else if(x == 15){
                    System.out.println("XX");
                }
                else{
                    System.out.print("XX ");
                }
            }
        }

    }
}
