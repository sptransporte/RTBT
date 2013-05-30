package br.com.sptrans.rtbt.auxl;

public class PositionMetric {
	double distPercorrida; //Distancia percorrida desde o inicio da rota (considerando onibus na linha da rota)
	double minDistAteRota; //Distancia minima da posicáo atual da linha da rota/ Pode ser a altura ou a distancia de pA ou pB
	
	Coord pA;
	Coord pB;
	Coord pC;
	
	public PositionMetric(double distPercorrida, double minDistAteRota,Coord pA,Coord pB,Coord pC) {
		super();
		this.distPercorrida = distPercorrida;
		this.minDistAteRota = minDistAteRota;
		
		this.pA=pA;
		this.pB=pB;
		this.pC=pC;
		
	}

	public String toString() {
		// TODO Auto-generated method stub
		return distPercorrida + "\t" + minDistAteRota + "\t"+ pA + "\t" + pB + "\t" + pC;  
	}

	
	
}
