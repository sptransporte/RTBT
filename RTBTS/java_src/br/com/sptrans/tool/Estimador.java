package br.com.sptrans.tool;

import java.util.Arrays;

public class Estimador {

	/*
	 * ESTIMADOR	->>>>>
	 */
	
	float COMP;
	float DRES;
	int SIZE;
	float[] TFD;
	
	float[] DFT;




	public Estimador(float COMP, float DRES, float vmms){
		this.COMP=COMP;
		this.DRES=DRES;
		this.SIZE=Math.round(this.COMP/this.DRES) + 1;
		TFD= new float[SIZE];
		for(int i =(SIZE-1) ; i>=0 ; i--){
			float distToFim = COMP - idxTodist(i);
			TFD[i]=distToFim/vmms;
		}
		//
		DFT= new float[SIZE];
		atualizaDFTfromTFD();
		//
		

	}

	private void atualizaDFTfromTFD(){
		//idxt=Math.round((SIZE-1)*T/TFD[0])
		//T=TFD[0]*idxt/(SIZE-1)
		int lastidxatulizado=0;
		for(int i=0 ; i < SIZE ; i++){
			float dist=idxTodist(i);
			float time=TFD[0] - TFD[i] ;
			int currentidx=timeToidxt(time);
			DFT[currentidx]=dist;
			for(int j=lastidxatulizado +1; j < currentidx; j++ ){
				DFT[j]=funcIntLin(DFT[lastidxatulizado],DFT[currentidx],lastidxatulizado, currentidx, j);
				//DFT[j]=(((DFT[lastidxatulizado]*currentidx - DFT[currentidx]*lastidxatulizado) + j*(DFT[currentidx]-DFT[lastidxatulizado])  )/(currentidx-lastidxatulizado));  
			}
			lastidxatulizado=currentidx;
		}

		
	}
	
    public static float funcIntLin(float fa,float fb,int idxa, int idxb, int i){
    	float A=(fb -fa)/(idxb - idxa);
    	float B=(fa*idxb - fb*idxa)/(idxb - idxa);    	
    	return A*i + B;    	
    }
    public static int IfuncIntLin(float fa,float fb,int idxa, int idxb, float fi){
    	float A=(fb -fa)/(idxb - idxa);
    	float B=(fa*idxb - fb*idxa)/(idxb - idxa);
    	if(A==0.0)
    		return (int) Math.round( (idxb + idxa)/2.0 );
    		
    	return (int)Math.round((fi - B)/A);
    }
	
	
	private int timeToidxt(float time){
		return Math.round((SIZE-1)*time/TFD[0]);
	}
	private float idxtTotime(int idxt){
		return TFD[0]*idxt/(SIZE-1);
	}

	private int distToIdx(float dist){				
		return Math.round((SIZE -1)*dist/COMP);
	}
	private float idxTodist(int idx){
		return idx*COMP/(SIZE -1);
	}
	
	//Estima o tempo demorado de ir de A para B
	public float tempoEstimado(float distA,float distB){

		float ret;
		synchronized (TFD){
			
			int idxa=distToIdx(distA);
			int idxb=distToIdx(distB);

			ret =  TFD[idxa] - TFD[idxb];
		}			
		return ret;
	}
	
	//Dada uma posicão A e um Delta Tempo, estima a posicão B 
	public float posicaoEstimada(float distA,float deltaT){
		
		
		synchronized (TFD){
			int idxa  = distToIdx(distA);
			int idxtb = timeToidxt(TFD[0]-TFD[idxa] + deltaT);
			if(idxtb > (DFT.length +1))
					return COMP;
			
			return DFT[idxtb];
		}			
		
		
	}
	
	public void addAmostra(float distA,float distB, float dt){
		synchronized (TFD){
		
			int idxa=distToIdx(distA);
			int idxb=distToIdx(distB);

			float vmms = (distB - distA)/dt;
			float TBtoF = TFD[idxb];
			float TAtoF = TFD[idxa];
			for(int i =(idxb -1) ; i>=idxa ; i--){
				float distToB = distB - idxTodist(i);
				TFD[i]=distToB/vmms + TBtoF;
			}
			float backsum = TFD[idxa] - TAtoF;
			for(int i =0 ; i<idxa ; i++){
				TFD[i]=TFD[i] + backsum;
			}
			//
			atualizaDFTfromTFD();
			//
		}
		
		
	}

	/*
	 * ESTIMADOR	<<<<<<<<----
	 */
	
	public float DebugExt(){
		return COMP;
	}

	public float DebugTime(){
		synchronized (TFD){
			return TFD[0];
		}
	}
	
	
	public float[] DebugFunc(){
		
		float[] ret= null;
		synchronized (TFD){
			ret = Arrays.copyOf(TFD, TFD.length);
		}
		
		return ret;
	}
	
	public float[] DebugFunc2(){
		
		float[] ret= null;
		synchronized (TFD){
			ret = Arrays.copyOf(DFT, DFT.length);
		}
		
		return ret;
	}
	
	public float DebugTempFF(){
		
		float ret= (float) -100.0;
		synchronized (TFD){
			ret = TFD[0];
		}
		
		return ret;
	}
	
}
