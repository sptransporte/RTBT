package br.com.sptrans.tool;

import br.com.sptrans.tool.RotaCrt.PositionMetric;

public class BusPos {
	boolean a;
	String p;
	double px;
	double py;
	String hora;
	long timestamp;
	
	PositionMetric posMetrc = null;
	Coord coord= null;

	
	public BusPos(String hora) {
		this.hora = hora;

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

	public void setTimeStamp (long time){
		timestamp= time;
	}

	public void setPosMetric (PositionMetric posMetrc){
		this.posMetrc=posMetrc;
	}
	
	public float getDistALinha(){
		return (float)posMetrc.minDistAteRota;
	}

	public float getDistPercorr(){
		return (float)posMetrc.distPercorrida;
	}
	
	public long getTimeStamp (){
		return timestamp;
	}
	
	
	public String toString() {
		return "p=" + p +  " a="+a + " px="+ px + " py="+ py  ;
	}
	


}
