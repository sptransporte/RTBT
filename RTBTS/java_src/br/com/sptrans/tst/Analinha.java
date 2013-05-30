package br.com.sptrans.tst;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class Analinha {

	Hashtable<String,DadoBusLinha> dados= new Hashtable(0);

	Hashtable<String,ProjBusPonto> dadosT= new Hashtable(0);
	
	BusRota busrota;
	PositionMetric distRef;
	
	public Analinha(BusRota busrota,PositionMetric distRef) {
		super();
		this.busrota = busrota;
		this.distRef=distRef;
	}

	public void getLinhaProjetion(){
		
		
		Enumeration<DadoBusLinha> enpb = dados.elements();
		while(enpb.hasMoreElements()){
			DadoBusLinha dadbusl = enpb.nextElement();
			if(dadbusl.distperco.distPercorrida > distRef.distPercorrida){ //Se Onibus j� passou do ponto esquece
				dadosT.remove(dadbusl.bpos.getP());
				continue;
			}
			
			boolean busok=false;
			
			double vmax=0;
			double vmin=80/3.6; //60 km/h
			double vmedia=0; //60 km/h

			long firsttime=-1;
			double firstdist=0;
			
			long lasttime=-1;
			double lastdist=0;
			
			Enumeration<Long> enl = dadbusl.FotosBusao.keys();
			while(enl.hasMoreElements()){
				long time = enl.nextElement();
				double dist = dadbusl.FotosBusao.get(time);
				if(dist >= 100){//Distancia maior que 100 para garantir saida do Ponto Final
					
					if(lasttime>=0){//N�o � primeira amostra
						if(((dist-lastdist)/(time-lasttime))>vmax)
							vmax=((dist-lastdist)/(time-lasttime));
						
						if(((dist-lastdist)/(time-lasttime))<vmin)
							vmin=((dist-lastdist)/(time-lasttime));
						
						busok=true;
						
					}else{//primeira amostra
						firsttime=time;
						firstdist=dist;
											
						
					}					
					
					lasttime=time;
					lastdist=dist;
				}			
			}			
			
			//lasttime
			//lastdist
			
			vmedia=(lastdist-firstdist)/(lasttime-firsttime);
			
			if(busok){
				//System.out.println(dadbusl.bpos.getP()+ " "+  vmax + " " + vmin + " " + vmedia + " ");
				
				ProjBusPonto projbus = new ProjBusPonto(vmax,vmin,vmedia,lasttime,lastdist,distRef.distPercorrida,dadbusl.bpos.getP());
				
				dadosT.put(dadbusl.bpos.getP(), projbus);
				
			}
			
			///////////////////////////////
		}	
			if(dadosT.size()>0)
			System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
			
			Enumeration<ProjBusPonto> enn = dadosT.elements();
			while(enn.hasMoreElements()){
				ProjBusPonto projb = enn.nextElement();
				System.out.println(projb.getDado());
				
			}
			
			
			
			
		//}
		
	}
	
	class ProjBusPonto{
		private double vmax;
		private double vmin; 
		private double vmedia;
		
		private long lasttime;
		private double lastdist;

		private double refdist;
		
		private String pbus;

		public ProjBusPonto(double vmax, double vmin, double vmedia,
				long lasttime, double lastdist, double refdist,String pbus) {
			super();
			this.vmax = vmax;
			this.vmin = vmin;
			this.vmedia = vmedia;
			this.lasttime = lasttime;
			this.lastdist = lastdist;
			this.refdist = refdist;
			
			this.pbus = pbus;
		}
		
		private double[] getTempoParaChegar(){
			
			long agora = System.currentTimeMillis()/1000;
			
			double maxt = (this.lasttime +  ((this.refdist - this.lastdist)/vmax))   - agora ;
			double medt = (this.lasttime +  ((this.refdist - this.lastdist)/vmedia)) - agora ;
			double mint = (this.lasttime +  ((this.refdist - this.lastdist)/vmin))   - agora ;
			
			double[] ret = {maxt,medt,mint,agora}; 
			
			return ret ;
		}
		
		private String getDado(){
			
			double[] tt = getTempoParaChegar();
			
			
			return pbus + " " + tt[0] + " " + tt[1] + " " + tt[2]+ " " + tt[3];
		}
	}
	
	public void addDado(Vector<BusPos> posbusAmostra){

		if(posbusAmostra.size() <=0){
			System.out.println("SEM DADOS RECEBIDOS");
			return;
		}
		
		//Pegando o primeiro pois dados de tempos sao iguais para todos
		String time =  posbusAmostra.get(0).horaap +"\t"+posbusAmostra.get(0).horadp +"\t"+posbusAmostra.get(0).hora;

		
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
			dadbl.validateDado(time);
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
	
	static double getTime(String time){
		String tmp[] = time.split(":");
		double ret = 60*60*Double.parseDouble(tmp[0]);//HORA
		ret = ret + 60*Double.parseDouble(tmp[1]);//MIN
		ret = ret + Double.parseDouble(tmp[2]);//SEG
		ret = ret + (Double.parseDouble(tmp[3]))/1000;//
		return ret;
		
	}
	
	static String getStringTime(double time){
		
		double minS =  (time%(60*60));
		int hora = (int) ((time-minS)/(60*60));
		
		double sec = minS%60;
		int min = (int) ((minS-sec)/60);
	
				
		return ""+hora+":" +min+":"+sec;
	}
	
	private class DadoBusLinha{
		
		Hashtable<Long,Double> FotosBusao= new Hashtable(0);
		
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
			String ret = distperco.distPercorrida + "\t" +busrota.sentido + "\t" + busrota.dia + "\t"+ bpos.p + "\t"+ bpos.a + "\t" + distperco ;
			if(!gpsok)
				ret="GPS NAO ENCONTRADO" + "\t" + ret;		
			
			return ret;
		}		
		
		private void storeDado(String time,String dado,long tempoAmostra,double posicaoAmostra){
			
			if(gpsok)//Caso Dado do GPS esteja OK entao foto do tempo e posicao do busao � armazenado
				FotosBusao.put(tempoAmostra, posicaoAmostra);
			
			System.out.println(time + "\t" + dado);
		}
		
//<<---		
		public void validateDado(String time){
			
			
			String sdado = createStringDado();
			if(!sdado.equals(this.lastStringdado)){
				this.lastStringdado = sdado;
				storeDado(time,lastStringdado,bpos.tempoAmostra,this.distperco.distPercorrida);
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
