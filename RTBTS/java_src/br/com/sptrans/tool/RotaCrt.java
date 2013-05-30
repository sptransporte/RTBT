package br.com.sptrans.tool;
import java.util.Vector;




public class RotaCrt {

	Vector<Coord> CoordPoints = new Vector(0);//Vetor de coordenadas que seguem a linha do onibus
	Vector<Double> Dist = new Vector(0);//Vetor de distancia percorrida de acordo com a coordenada que o onibus esta
	
	public RotaCrt(String coor) {
		
		
		String[] tmp = coor.split("\\|\\|");
		
		Coord lastCoord=null;
		
		double distPercorr = 0.0;
		
		for(int i =0; i< tmp.length ; i++){
			
			double lat = Double.parseDouble(prepareCoord(tmp[i]));
			i++;
			double lon = Double.parseDouble(prepareCoord(tmp[i]));
			
			if(lastCoord==null || lastCoord.lat !=lat || lastCoord.lon !=lon){ 
				
				Coord currCoord=new Coord(lat,lon);				
				CoordPoints.add(currCoord);
				
				if(lastCoord!=null)
					distPercorr=distPercorr+getDistCord(lastCoord,currCoord);
				
				Dist.add(distPercorr);				
				
				lastCoord = currCoord;
			
			}
			
		}
		
		
	}

	/*Calcula o Comprimento da Linha em metros
	*/
	public float getComprimento(){
		int lastidx = Dist.size() -1;
		double distd = Dist.get(lastidx);		
		return (float)distd;
	}
	
	public String debugInFin(){
		return new String(CoordPoints.get(0).toString() + " --> "  +  CoordPoints.get(CoordPoints.size() -1).toString());
	}
	
	private String prepareCoord(String cord){		
		return cord.substring(0, cord.length()-6) + "."+ cord.substring(cord.length()-6, cord.length());
	}	
	
	private double getDistCord(Coord coordA,Coord coordB){
		return getDist(coordA.lat,coordA.lon,coordB.lat,coordB.lon);
	}
	
	double getDist(double latA,double lonA,double latB, double lonB){
		if(latA==latB && lonA==lonB)
			return 0.0;
		
		int raioterra= 6378100; //metros da internet
		//int raioterra= 6371; //km da internet
		
		double dist = raioterra*Math.acos(Math.cos(Math.PI*(90-latB)/180)*Math.cos((90-latA)*Math.PI/180)+Math.sin((90-latB)*Math.PI/180)*Math.sin((90-latA)*Math.PI/180)*Math.cos((lonA-lonB)*Math.PI/180));
		return dist;
	}
	

	
	public PositionMetric calcDistPercorrida(Coord coordref){

		double mindistpA = 999999999;
		int idxMinDistPointA=-1;
		double mindistpB = 999999999;
		int idxMinDistPointB=-1;

		
		double minAltura = 999999999;
		int idxMinAlturapA = -1;
		double distc1=0.0;
		double distc2=0.0;
		
		for( int i =1; i < CoordPoints.size(); i++){
			
			Coord pAl=CoordPoints.get(i-1);
			Coord pBl=CoordPoints.get(i);
			
			TrechoMetric trmetr =new TrechoMetric( pAl,pBl,coordref );
			
			if(trmetr.c1>=0 && trmetr.c2>=0 && trmetr.altura < minAltura  ){
				minAltura=trmetr.altura;
				idxMinAlturapA=i-1;
				distc1=trmetr.c1;
				distc2=trmetr.c2;
			}	
			
			if(trmetr.a < mindistpA ){
				mindistpA=trmetr.a;
				idxMinDistPointA=i;
			}
			if(trmetr.b < mindistpB ){
				mindistpB=trmetr.b;
				idxMinDistPointB=i;
			}			
			
		}
		
		double hdmin=minAltura;
		int idxA=idxMinAlturapA;
		if(mindistpA<hdmin ){
			hdmin=mindistpA;
			
		
			distc1=0.0;
			
			distc2=0.0;
			if((idxMinDistPointA + 1)< Dist.size() ) //Para casos do extremo final
				distc2=Dist.get(idxMinDistPointA + 1) -Dist.get(idxMinDistPointA);

			idxA=idxMinDistPointA;

			
		}
		if(mindistpB<hdmin){
			hdmin=mindistpB;
			
			distc1=0.0;
			idxA=0;
			if(idxMinDistPointB > 0){//Para casos do extremo começo
				distc1=Dist.get(idxMinDistPointB)     -Dist.get(idxMinDistPointB -1); 
				idxA=idxMinDistPointB -1;
			}
			distc2=0.0;
			

		}
		
		int idxB=idxA;
		if(idxB != ( Dist.size() -1))
			idxB=idxA+1;
			
		PositionMetric ret = new PositionMetric(Dist.get(idxA) + distc1 ,hdmin ,CoordPoints.get(idxA),CoordPoints.get(idxB),coordref);

		return ret;
		
		
	}	
	
	private class TrechoMetric{
		
		double c1=0;
		double c2=0;
	
		double a=0;
		double b=0;
		double c=0;

		double altura=0;
		double mediana = 0;
		public TrechoMetric(Coord coordA,Coord coordB,Coord coordC) {

			c =getDistCord(coordA,coordB);
			a =getDistCord(coordB,coordC);
			b =getDistCord(coordC,coordA);
			
			c1=(-a*a + b*b + c*c)/(2*c);
			c2=( a*a - b*b + c*c)/(2*c);
			
			
			
			altura = (Math.sqrt(Math.abs(b*b -c1*c1)) + Math.sqrt(Math.abs(a*a -c2*c2)))/2.0;
			
			mediana = getDist( (coordA.lat + coordB.lat)/2,(coordA.lon + coordB.lon)/2,  coordC.lat,coordC.lon);
			
		}
		
		
	}
	
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
		
	}
	
}
