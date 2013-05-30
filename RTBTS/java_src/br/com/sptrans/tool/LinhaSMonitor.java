package br.com.sptrans.tool;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import br.com.sptrans.bus.BusResult;
import br.com.sptrans.bus.BusResult.Bus;
import br.com.sptrans.nt.LinhaInfo;
import br.com.sptrans.nt.RemAcc;
import br.com.sptrans.tool.RotaCrt.PositionMetric;


public class LinhaSMonitor extends Thread {

	RotaCrt rotaCrt;
	Estimador estim;
	RemAcc remacc;
	String codlinha;
	
	LinhaSMonitor sister = null;
	
	String descr;
	
	public boolean run=true;
	
	private Hashtable<String,BusInfo> Activebuses= new Hashtable<String,BusInfo>(0); 

	private float DISTUSER = (float) 1000.0;
	
	public LinhaSMonitor(LinhaInfo linhainfo,int sentido, int dia,RemAcc remacc) {
		
		this.remacc= remacc;
		rotaCrt = new RotaCrt(linhainfo.getCoordRota(sentido, dia));
		estim = new Estimador(rotaCrt.getComprimento(),(float)50.0,     khToms((float)10.0));
		codlinha = linhainfo.getCodigoLinha(sentido);
		
		descr = linhainfo.getLetreiroSentido(sentido);
		
	}
	
	public void setSister(LinhaSMonitor sister){
		this.sister=sister;
	}
	
	
	public String getDebugDescr(){
		return descr + " VM=" + getDebugVelocidadeMedia();
	}
	
	public String getDebugInFin(){
		return rotaCrt.debugInFin() + "\n" + descr;
	}

	public float getDebugLinhaExt(){
		return estim.DebugExt();
	}

	public float getDebugTotalTime(){
		return estim.DebugTime();
	}
	
	
	public float getDebugVelocidadeMedia(){
		
		return msTokh(estim.DebugExt()/estim.DebugTempFF());
		
	}
	
	public float[] getDebugFunc(){
		
		return estim.DebugFunc();
	}
	
	public float[] getDebugFunc2(){
		
		return estim.DebugFunc2();
	}
	
	
	private float khToms(float kh ){
		return (float) (kh/3.6);
	}

	private float msTokh(float ms ){
		return (float) (3.6*ms);
	}
	
	private long getCurrentSeconds(){
		return System.currentTimeMillis()/1000;
	}

	private boolean ActiveBusescontains(String key){
		
		synchronized(Activebuses){
			
			return Activebuses.containsKey(key);
			
		}
		
	}
	
	
	private void removeBusFromList(String key){
		
		synchronized(Activebuses){
			
			Activebuses.remove(key);
			
		}
		
	}
	
	private void addBusToList(String key,BusPos buspos){
		

		
		
		if(!Activebuses.containsKey(buspos.getP())){//Primeira ocorrencia na linha
			
			Activebuses.put(
					buspos.getP(),
					new BusInfo(buspos.getP(),buspos.getDistPercorr(),buspos.getTimeStamp(), buspos.getA())					
					);
			if(sister.ActiveBusescontains(key)){
				System.out.println("Rem: Apareceu na linha irma");
				sister.removeBusFromList(key);//remove da linha irma
			}
			
		}else{//ja existe pelo menos uma medida desse onibus

			synchronized(Activebuses){
				
				if(buspos.getDistALinha() > 200.0){//Ignora medida se distancia a linha for maior que 200 metros e remove onibus da linha
					System.out.println("Rem: Distancia a linha="+buspos.getDistALinha());
					this.removeBusFromList(key);
					return;
				}
				
			
				BusInfo businf =Activebuses.get(buspos.getP());
	
				if(businf.isEqualDist(buspos.getDistPercorr()))//Se a distancia percorrida for a mesma Ignora	
					return;
				

				
				
				float deltadist = businf.measureDeltaDist(buspos.getDistPercorr());
				float deltatime = businf.measureDeltaTime(buspos.getTimeStamp());
				
				
				if(deltatime <=0 || deltadist<0 ){//Ignora medida se tempo ou distancia percorrida for negativa e remove onibus da lista
					System.out.println("Rem: Distancia dt ou distpercor negativa dt="+deltatime + "  dd="+deltadist);
					this.removeBusFromList(key);
					return;
				}
				
				if(msTokh(deltadist/deltatime) > 100.0){//Se a velocidade do trecho for maior que 100 Kmh ignora e remove onibus da lista
					System.out.println("Rem: Velocidade muito grande=" + msTokh(deltadist/deltatime));
					this.removeBusFromList(key);
					return;
				}

				businf.addMeasure(buspos.getDistPercorr(), buspos.getTimeStamp());
				estim.addAmostra(businf.getA(),businf.getB(), businf.getDt());
			}
			
		}
		
	}

		
	public int estimaTempo(double lat,double lon){
		Coord coord = new Coord(lat,lon); 
		PositionMetric posmetr = rotaCrt.calcDistPercorrida(coord);
		
		if(posmetr.minDistAteRota > DISTUSER)//Retorna  caso a distancia de referencia esta distante mais de 100 metros da rota
			return -2;
	
		float posUsuario = (float)posmetr.distPercorrida;
		
		int tempoestimado = -4;//Nao encontrado onibus no Sentido
		synchronized(Activebuses){
			
			if(Activebuses.size()==0) //retorna se lista de Onibus no sentido estiver vazia
				return -3;
			
			Enumeration keys = Activebuses.keys();
			
			long  agora = getCurrentSeconds();
			
			
			
			while(keys.hasMoreElements()){
				
				BusInfo businfo = Activebuses.get(keys.nextElement());
				
				if(businfo.notReady())//Se amostra nao for a primeira, nao deve ser usada
					continue;
				
				if(businfo.measureDeltaDist(posUsuario)<=0.0)//Se distancia ao usuario for negativa, N~oa serve pois esta a frente
					continue;
				
				long tempocheg = businfo.getTimeStamp() + (long)estim.tempoEstimado(businfo.getB(), posUsuario);
				
				if(tempocheg <= agora)//Onibus ja passou
					continue;
				
			
				if((tempocheg - agora) < tempoestimado || tempoestimado <0)
					tempoestimado = (int) (tempocheg - agora);
				
			}
			
		}
		
		
		return tempoestimado;
	}
	
	
	public String estimaTempoS(double lat,double lon){
		Coord coord = new Coord(lat,lon); 
		PositionMetric posmetr = rotaCrt.calcDistPercorrida(coord);
		
		if(posmetr.minDistAteRota > DISTUSER)//Retorna  caso a distancia de referencia esta distante mais de 100 metros da rota
			return "Usuario Distante mais de "+DISTUSER+" metros da linha.";
	
		float posUsuario = (float)posmetr.distPercorrida;
		int nfound=0;
		String ret="\nOnibus:\n";

		synchronized(Activebuses){

			
			if(Activebuses.size()==0) //retorna se lista de Onibus no sentido estiver vazia
				return "Nenhum onibus encontrado na Linha";
			
			Enumeration keys = Activebuses.keys();
			
			long  agora = getCurrentSeconds();
			

			
			while(keys.hasMoreElements()){
				
				BusInfo businfo = Activebuses.get(keys.nextElement());
				
				if(businfo.notReady())//Se amostra nao for a primeira, nao deve ser usada
					continue;
				
				if(businfo.measureDeltaDist(posUsuario)<=0.0)//Se distancia ao usuario for negativa, N~oa serve pois esta a frente
					continue;
				
				long tempoUltimoStatus = businfo.getTimeStamp();
				float posicaoUltimoStatus = businfo.getB();
				
				float posicaoAgoraEstimada = estim.posicaoEstimada(businfo.getB(),(float)(agora - tempoUltimoStatus));
				
				
				if((posUsuario - posicaoAgoraEstimada)<=0.0)//Se distancia ao usuario for negativa, N~oa serve pois esta a frente
					continue;
				

				
				int tempoestimado = (int)Math.round(estim.tempoEstimado(posicaoAgoraEstimada, posUsuario));
				float distUsuario=(posUsuario - posicaoAgoraEstimada);
				int tempoCego = (int) (agora - tempoUltimoStatus);
				float deltaDeslocEstimadoAtual=(posicaoAgoraEstimada -businfo.getB());
				
				//ret= ret + "ID: " +  businfo.getBusID() + " TE: " + tempoestimado + " DU: " + distUsuario +  " TC: " + tempoCego  + " DE: " + deltaDeslocEstimadoAtual + " dA=" + businfo.getA() + " dB=" + businfo.getB() + " dt=" + businfo.getDt() + "\n";	
				ret= ret + "Onibus:"+ businfo.getBusID() + " - Tempo estimado : " + getTime(tempoestimado)  + "\n"; 
				nfound++;
			}
			
		}
		
		if(nfound==0){
			ret= ret + "Aguardando saida do Terminal\n";	
		}
		
		return ret;
	}
	
	public BusResult estimaTempoBR(double lat,double lon,double lat_d,double lon_d){
		synchronized(Activebuses){
			
		
		Coord coord = new Coord(lat,lon); 
		Coord coord_d = null;
		if(lat_d!=0 && lon_d !=0)
			coord_d = new Coord(lat_d,lon_d); 
		
		PositionMetric posmetr = rotaCrt.calcDistPercorrida(coord);
		int distOrigem = (int) posmetr.minDistAteRota;		//Distancia do ponto de Origem  a linha em metros.
		int metragemOrigem = (int) posmetr.distPercorrida;

		int tviagem=-1; 		//Tempo estimado de viagem em segundos do ponto de origem ao ponto de destino.
		int distDestino=-1;	//Distancia do ponto de Destino a linha em metros.
		int dviagem=-1;		//Distancia do ponto da linha de Origem ao Ponto da linha de destino em metros.
		if(coord_d !=null){
			PositionMetric posmetr_d = rotaCrt.calcDistPercorrida(coord_d);
			int metragemDestino = (int) posmetr_d.distPercorrida;
			if(metragemDestino > metragemOrigem){
				distDestino=(int) posmetr_d.minDistAteRota;
				dviagem=metragemDestino-metragemOrigem;	
				tviagem = (int) estim.tempoEstimado(metragemOrigem, metragemDestino);
			}	
		}
		
		
		BusResult ret = new BusResult(distOrigem, distDestino, tviagem, dviagem);
		 
		Enumeration<String> keys = Activebuses.keys();
		long  agora = getCurrentSeconds();
		while(keys.hasMoreElements()){
			
			BusInfo businfo = Activebuses.get(keys.nextElement());
			
			if(businfo.notReady())//Se amostra nao for a primeira, nao deve ser usada
				continue;
			
			if(businfo.measureDeltaDist(metragemOrigem)<=0.0)//Se distancia ao usuario for negativa, N~oa serve pois esta a frente
				continue;
			
			long tempocheg = businfo.getTimeStamp() + (long)estim.tempoEstimado(businfo.getB(), metragemOrigem);
			
			if(tempocheg <= agora)//Onibus ja passou
				continue;
			
			long tempoUltimoStatus = businfo.getTimeStamp();
			
			float posicaoAgoraEstimada = estim.posicaoEstimada(businfo.getB(),(float)(agora - tempoUltimoStatus));
			
			if((metragemOrigem - posicaoAgoraEstimada)<=0.0)//Se distancia ao usuario for negativa, N~oa serve pois esta a frente
				continue;
			
		
			String busId=businfo.getBusID();	// ID do onibus.
			boolean acess=businfo.getAcess();	// Acessibilidade para deficiente. 
			int estim=(int)  (tempocheg - agora);;		// Tempo estimado para chegada do onibus em segundos.
			int distU=(int) (metragemOrigem - posicaoAgoraEstimada);		// Distancia do onibus ao ponto da linha mais proxima do usuário em metros.		
			ret.new Bus(busId, acess, estim, distU);

		}
		
		
		return ret;
		
		
		}
	}
	
	
	
	
	static public String getTime(int s){
		if(s<0)
			return "--:--";

		if(s > 7200){
			return String.format("%d Horas e %02d Minutos", s/3600, (s%3600)/60);
		}
		
		if(s > 3600){
			return String.format("%d Hora e %02d Minutos", s/3600, (s%3600)/60);
		}

		if(s > 120){
			return String.format("%02d Minutos", Math.round(s/60) );
		}

		
		if(s > 60){
			return String.format("%02d Minuto", Math.round(s/60) );
		}

		
		return "Menos de 1 minuto ("+ s +" )";
		
		
	}
	
	
	public void run() {
		
		Hashtable<String,BusPos> currbuses= new Hashtable<String,BusPos>(0); 
		long currtime=0;
		
		while (run){
			try {


				if(remacc.getCurrentPosicaoBus(codlinha, currbuses,getCurrentSeconds(),rotaCrt)){
					
					Enumeration <String> enk =currbuses.keys();
					while(enk.hasMoreElements()){
						String key = enk.nextElement();
						addBusToList(key,currbuses.get(key));
						
					}				
					
				}else{
					//Nenhum Onibus encontrado					
				}
				
			} catch (Throwable e) {
				e.printStackTrace();
			}
			try {Thread.sleep(10000);} catch (InterruptedException e) {e.printStackTrace();}			
		}	
	}
	
	

}
