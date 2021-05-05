import java.net.InetAddress;

//Process to run on machine dc04
public class Process4 {
    public static void main(String args[]) {
        try {
        //get addresses of other three machines
        String address1 = InetAddress.getByName("dc01.utdallas.edu").getHostAddress(); 
        String address2 = InetAddress.getByName("dc02.utdallas.edu").getHostAddress(); 
        String address3 = InetAddress.getByName("dc03.utdallas.edu").getHostAddress(); 
        //Start program to send and receive threads
        new Program(4, new String[]{address1,address2,address3}, 3000);
        }
        catch(Exception e){

        }
   
    }
}
