import java.net.InetAddress;

//Process to run on machine dc02
public class Process2 {
    
    public static void main(String args[]) {
        //10.122.206.107
        try {
            //get addresses of other three machines
            String address1 = InetAddress.getByName("dc01.utdallas.edu").getHostAddress(); 
            String address2 = InetAddress.getByName("dc03.utdallas.edu").getHostAddress(); 
            String address3 = InetAddress.getByName("dc04.utdallas.edu").getHostAddress(); 
            //Start program to send and receive threads
            new Program(2, new String[]{address1,address2,address3}, 3000);
            }
            catch(Exception e){
    
            }
    }
}
