package br.com.sptrans.tst;

public class BusPos {
	boolean a;
	String p;
	double px;
	double py;
	String hora;
	String horaap;
	String horadp;
	
	Coord coord= null;
	
	long tempoAmostra;
	
	public BusPos(String hora,String horaap, String horadp,long tempoAmostra) {
		this.hora = hora;
		this.horaap=horaap;
		this.horadp=horadp;
		
		this.tempoAmostra=tempoAmostra;
	}

	public boolean getA() {
		return a;
	}
	
	public String getP() {
		return p;
	}

	public void setP(String p) {
		this.p = p;
	}

	public double getPx() {
		return px;
	}

	public void setA(boolean a) {
		this.a = a;
	}
	
	public void setPx(double px) {
		this.px = px;
	}

	public double getPy() {
		return py;
	}

	public void setPy(double py) {
		this.py = py;
	}

	public Coord getCoord(){
		if(coord==null)
			coord = new Coord(py,px);
		
		return coord;
	} 

	public String toString() {
		return "p=" + p +  " a="+a + " px="+ px + " py="+ py  ;
	}
	


}
