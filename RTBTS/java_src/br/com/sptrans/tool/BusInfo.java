package br.com.sptrans.tool;

public class BusInfo {
	
	private String busID;
	
	private float lastDistPerc=0;
	private long lastTimeStamp=0;

	private float currDistPerc=0;
	private long currTimeStamp=0;

	private boolean acess=false;
	
	boolean initial =true;

	public BusInfo(String busID, float currDistPerc, long currTimeStamp , boolean acess) {
		super();
		this.busID = busID;
		this.currDistPerc = currDistPerc;
		this.currTimeStamp = currTimeStamp;
		this.acess=acess;
		initial =true;
	}
	
	public boolean getAcess(){
		return acess;
	}
	
	public boolean notReady(){
		return initial;
	}
	
	public void addMeasure( float currDistPerc, long currTimeStamp){
		this.lastDistPerc=this.currDistPerc;
		this.lastTimeStamp=this.currTimeStamp;
		
		this.currDistPerc=currDistPerc;
		this.currTimeStamp=currTimeStamp;
		
		initial = false;
	}
	
	public String getBusID(){
		return new String(this.busID );
	}
	
	 public boolean isEqualDist( float nextDist){
		 return  nextDist==	this.currDistPerc;	 
	 }
	 
	
	public float measureDeltaDist(float nextDist){
		return nextDist-this.currDistPerc;
	}
	public float measureDeltaTime(long nextTime){
		
		return (float)(nextTime - this.currTimeStamp);
	}
	
	public float getA(){
		if(initial){
			System.out.println("ERRO GRAVE em BUS INFO getA");
			System.exit(0);
		}
		
		return this.lastDistPerc;
	}
	
	public float getB(){
		
		return this.currDistPerc;
	}
	
	public float getDt(){
		if(initial){
			System.out.println("ERRO GRAVE em BUS INFO getDt");
			System.exit(0);
		}
		return (float)(this.currTimeStamp - this.lastTimeStamp);
	}	

	public long getTimeStamp(){
		return currTimeStamp;
	}
	
}
