import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Deus {
	
	private static final String HOST = "192.168.4.1";
//	private static final String HOST = "localhost";
	private static final int PORT = 8888;
	
	public static void main(String[] args) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		
		InetAddress address = InetAddress.getByName(HOST);
		
		int time = 42;
		Marker[] markers = {
				new Marker(1, 0.5, 0.6, 1.9),
				new Marker(2, 1.67, 5.43, 3.5),
		};
		
		byte[] buf = preparePacket(time, markers);
		
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, PORT);
		socket.send(packet);
		System.out.println("sent");
		socket.close();
	}
	
	static byte[] preparePacket(int time, Marker[] markers) {
		int n = markers.length;
		byte[] bytes = new byte[4 + 1 + n * (1 + 8 + 8 + 8)];
		
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		bb.putInt(time);
		bb.put((byte)n);
		for (Marker marker : markers) {
			bb.put((byte)marker.id);
			bb.putDouble(marker.x);
			bb.putDouble(marker.y);
			bb.putDouble(marker.rotation);
		}
		
		return bytes;
	}
}
