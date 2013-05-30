package br.com.sptrans.tst;

public class Coord {
	double lat;
	double lon;
	public Coord(double lat, double lon) {
		super();
		this.lat = lat;
		this.lon = lon;
	}

	public Coord(String latlog) {
		super();
		String[] tmp = latlog.split(",");
		
		this.lat = Double.parseDouble(tmp[0]);
		this.lon = Double.parseDouble(tmp[1]);
	}

	
	public String toString() {
		return lat + "," + lon;
	}
	
}
