
package dissender;

import edu.nps.moves.dis.*;
import edu.nps.moves.spatial.*;
import edu.nps.moves.disutil.*;

import java.io.*;
import java.net.*;

public class DisSender 
{
    /** The broadcast address we send DIS to. multicast address is better
     * but some people use broadcast.
     */
    public static final String BROADCAST_ADDRESS = new String( "10.0.0.255" );
   
    public static void main(String[] args) 
    {
        try
        {
            // This is some obscure code that lets two different
            // programs open a UDP socket on the same port. In
            // this case, it lets us open UDP socket 3000 in both
            // this DisSender application and the DIS Map program.
            // It uses something called so_reuseaddress.
             DatagramSocket socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            InetSocketAddress addr = new InetSocketAddress(InetAddress.getByName("0.0.0.0"),3000);
            socket.bind(addr);
             
            // This creates a "range coordinates" object that acts as a local
            // tangent plane at a given lat/lon/alt. You can move objects around
            // in the local coordinate system, then convert the local coordinates
            // to DIS global geocentric coordinates when you need to send a PDU.
            // Units are in degrees and meters.
             RangeCoordinates rc = new RangeCoordinates(36.6, -121.9, 1);
            
             while(true)
             {
                 // Create a new, empty entity state PDU
                 EntityStatePdu espdu = new EntityStatePdu();
                 
                 // Set varioud fields in it, such as the ID....
                 espdu.getEntityID().setApplication(5);
                 espdu.getEntityID().setSite(17);
                 espdu.getEntityID().setEntity(23);
                 
                 // Set the marking--be careful to not exceed 10 characters
                 espdu.getMarking().setCharacters(new String("Patton").getBytes());
                 
                 // Set entity type fields. The entity type settings can be
                 // found in the SISO EBV document. So you can set the entity
                 // to be a tank, a plane, a dismount, etc.
                 // Type - M1A2 Tank with special package
                 espdu.getEntityType().setEntityKind((short)1);
                 espdu.getEntityType().setDomain((short)1);
                 espdu.getEntityType().setCountry(225);
                 espdu.getEntityType().setCategory((short)1);
                 espdu.getEntityType().setSubcategory((short)1);
                 espdu.getEntityType().setSpec((short)15);
                 espdu.getEntityType().setExtra((short)1);
                 
                 // Convert to the DIS geocentric coodinate system from a 
                 // position at (100, 200, 0) in the local coordinate system.
                 // Since we set up the local coordinate system to have its
                 // origin at (36.6, -121.9, 1) this will result in a DIS
                 // geocentric position a little offset from that.
                 Vector3Double disLocation = rc.DISCoordFromLocalFlat(100, 200, 0);
                 System.out.println();
                 System.out.println("Location in DIS coordinates: x: " + disLocation.getX() + " y: " + disLocation.getY() + " z:" + disLocation.getZ());
                 
                 // Get local coordinate system position (origin at the lat/lon/alt
                 // for whatever you created the RangeCoordinates object as) from
                 // the DIS global, geocentric coordinates. This is more useful
                 // on the receiving side.
                 rc.localCoordFromDis(disLocation.getX(), disLocation.getY(), disLocation.getZ());
                 
                 // This is a different object (from the disutil package) that
                 // just does coordinate conversions, including from DIS coordinates
                 // to lat/lon/alt. We pass in an array of
                 // doubles with the DIS coordinates, and convert that to 
                 // latitude and longitidue. The method calls are a bit different
                 // from the above when working with the range coordinate object.
                 double[] disCoordinatesArray = new double[3];
                 disCoordinatesArray[0] = disLocation.getX();
                 disCoordinatesArray[1] = disLocation.getY();
                 disCoordinatesArray[2] = disLocation.getZ();
                 double[] latLonAlt = CoordinateConversions.xyzToLatLonDegrees(disCoordinatesArray);

                 System.out.println("Location in lat/lon/alt:" + latLonAlt[0] + ", " + latLonAlt[1] + ", " + latLonAlt[2]);

                 // Set the location of the entity (in global coordinates, always.)
                 espdu.getEntityLocation().setX(disLocation.getX());
                 espdu.getEntityLocation().setY(disLocation.getY());
                 espdu.getEntityLocation().setZ(disLocation.getZ());
                 
                 // Convert the Java object to an array of bytes that conforms
                 // to the DIS standard--network byte order, fields in the
                 // correct position and format. The timestamp field is automatically
                 // set for you.
                 byte[] disData = espdu.marshalWithDisAbsoluteTimestamp();
                 
                 // Careful--if you change your network, the broadcast address
                 // will also change. There are ways to find the bcast address
                 // at runtime but it's a little involved for demo code.
                 InetAddress bcast = InetAddress.getByName(BROADCAST_ADDRESS);
                 DatagramPacket packet = new DatagramPacket(disData, disData.length, bcast, 3000);
                 
                 socket.send(packet);
                 System.out.println("Sent packet");
                 
                 Thread.sleep(1000);
             }
             
             
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
    }
    
}
