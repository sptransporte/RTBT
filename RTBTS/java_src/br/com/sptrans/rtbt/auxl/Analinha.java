package br.com.sptrans.rtbt.auxl;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import br.com.sptrans.rtbt.IAppCenter;



public class Analinha {

	Hashtable<String,DadoBusLinha> dados= new Hashtable<String,DadoBusLinha>(0);

	private IAppCenter appcenter;
	
	BusRota busrota;
	
	public static String BD_COL_SEP =  "\t"; 
	
	public Analinha(BusRota busrota,IAppCenter appcenter) {
		super();
		this.appcenter=appcenter;
		this.busrota = busrota;


	}

	
	public void addDado(Vector<BusPos> posbusAmostra,String linhaNome, String realtime){

		if(posbusAmostra.size() <=0){
			//System.out.println("SEM DADOS RECEBIDOS");
			return;
		}
		
		//Pegando o primeiro pois dados de tempos sao iguais para todos
		String time =  posbusAmostra.get(0).hora;

		
		//Se o onibus não aparecer na lista deve informar, setando todos os gps do onibus como not ok, não aparecem, caso os dados do onibus etejam em posbusAmostra gps sera ok para essa polada. 

		
		
		Enumeration<BusPos> en = posbusAmostra.elements();
		//Orientado a onibus;
		while(en.hasMoreElements()){
			BusPos bpos = en.nextElement();
			addDadoBusLinha(bpos);	
		}
		
		//Valida os dados e caso necessário processa os dados
		Enumeration<DadoBusLinha> enpb = dados.elements();
		while(enpb.hasMoreElements()){
			DadoBusLinha dadbl = enpb.nextElement();
			dadbl.validateDado(time,realtime,linhaNome);
		}
		
		
		
	}
	
	void addDadoBusLinha(BusPos bpos ){
		
		PositionMetric distperco = busrota.calcDistPercorrida(bpos.getCoord());
		
		DadoBusLinha dadl = dados.get(bpos.getP());
		if(dadl==null){			
			dadl = new DadoBusLinha(bpos,distperco);
			dados.put(bpos.getP(), dadl);
		}else{
			dadl.addAmostra(bpos,distperco);
		}
		
	}
	

	
		
	private class DadoBusLinha{
		
		
		String lastStringdado;

		boolean gpsok=false;
		BusPos bpos;
		PositionMetric distperco;
		
		public DadoBusLinha(BusPos bpos,PositionMetric distperco) {
			super();
			
			this.gpsok=true;
			this.bpos= bpos;
			this.distperco = distperco;
			
			this.lastStringdado = " ";

			
		}

		
//--->>	Info out		
		private String createStringDado(){
			
			String bpos_a="1";
			if(!bpos.a)
				bpos_a="0";
				
			String ret =  
				busrota.sentido 								+ BD_COL_SEP + 
				busrota.dia 									+ BD_COL_SEP + 
				bpos.p 											+ BD_COL_SEP + 
				bpos_a											+ BD_COL_SEP + 
				String.format("%.9f",bpos.getPy()) 				+ BD_COL_SEP + 
				String.format("%.9f",bpos.getPx()) 				+ BD_COL_SEP + 
				String.format("%.2f",distperco.distPercorrida)	+ BD_COL_SEP + 
				String.format("%.2f",distperco.minDistAteRota) ;
			
			
			
			if(!gpsok)
				ret="GPS NAO ENCONTRADO" + "\t" + ret;		
			
			return ret;
		}		
		
		private void storeDado(String linhaNome,String realtime,String time,String dado){
			
			if(gpsok){
				String trace =  realtime + "\t" + linhaNome +"\t" + time + "\t" + dado;
				appcenter.log(trace);
				
			}
				
			
			//System.out.println(time + "\t" + dado);
		}
		
//<<---		
		public void validateDado(String time,String realtime,String linhaNome){
			
			
			String sdado = createStringDado();
			if(!sdado.equals(this.lastStringdado)){
				this.lastStringdado = sdado;
				storeDado(linhaNome,realtime,time,lastStringdado);
			}
			gpsok=false;
		}
		
		void addAmostra(BusPos bpos,PositionMetric distperco){
			this.gpsok=true;
			this.bpos= bpos;
			this.distperco = distperco;
			

		}
		
	}
}
