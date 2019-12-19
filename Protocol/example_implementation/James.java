import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class James {
	
	private static final int PORT = 8888;
	
	static final int MAX_MARKERS_PER_MESSAGE = 50; //Currently cannot be more than 127
	static final int BUFFER_SIZE = 4 + 1 + MAX_MARKERS_PER_MESSAGE * (1 + 8 + 8 + 8);
	
	public static void main(String[] args) throws IOException {
		DatagramSocket socket = new DatagramSocket(PORT);
		
		byte[] buf = new byte[BUFFER_SIZE];
		
//		JamesGraphics graphics = new JamesGraphics();
		
		while (true) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			
			ByteBuffer bb = ByteBuffer.wrap(packet.getData());
			bb.order(ByteOrder.LITTLE_ENDIAN);
			
			int time = bb.getInt();
			int nMarkers = bb.get();
			
			Marker[] markers = new Marker[nMarkers];
			for (int i = 0; i < nMarkers; i++) {
				markers[i] = new Marker();
				markers[i].id = bb.get();
				markers[i].x = bb.getDouble();
				markers[i].y = bb.getDouble();
				markers[i].rotation = bb.getDouble();
			}
			
			System.out.println("received:");
			System.out.println("time: " + time);
			System.out.println("markers:");
			for (int i = 0; i < nMarkers; i++) {
				System.out.println(markers[i]);
			}
		}
	}
}
