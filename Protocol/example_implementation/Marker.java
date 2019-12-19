public class Marker {
	
	public Marker() {}
	
	public Marker(int id, double x, double y, double rotation) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.rotation = rotation;
	}
	
	public int id;
	public double x;
	public double y;
	public double rotation;
	
	@Override
	public String toString() {
		return "Marker{" +
				"id=" + id +
				", x=" + x +
				", y=" + y +
				", rotation=" + rotation +
				'}';
	}
}
