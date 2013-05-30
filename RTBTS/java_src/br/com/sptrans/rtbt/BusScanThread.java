package br.com.sptrans.rtbt;




import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

import br.com.sptrans.rtbt.auxl.Analinha;
import br.com.sptrans.rtbt.auxl.BusPos;
import br.com.sptrans.rtbt.auxl.BusRota;
import br.com.sptrans.rtbt.auxl.LinhaResult;
import br.com.sptrans.rtbt.auxl.Timer;


public class BusScanThread extends Thread {
	
	static boolean proxy=false;
	static String proxyhost ="localhost";
	static int proxyport = 8080;
	static String LE="\r\n";
	
	static int RETRIES = 20;
	
	static int RETRIES_GETPOS = 200;
	
	static int TIMEOUT = 30000;
	static int HORA_START = 6;
	static int HORA_STOP = 23;
	
	static int TCP_TIMEOUT=10000;
	
	private IAppCenter appcenter;
	private String linhaNome;
	

	
	
	
	
	
	public BusScanThread(String linhaNome, IAppCenter appcenter) {
		this.appcenter = appcenter;
		this.linhaNome = linhaNome;
		
		this.start();
			
	}
	


	
	@Override
	public void run() {

		
	LinhaResult	linha1=null;
	LinhaResult	linha2=null;
		
	int count = RETRIES;	
	
	
	while(count >0){
		
			try{
				Vector<LinhaResult> vlinhas = procuraLinha(linhaNome);
				
				if(vlinhas.size() !=2 ){
					appcenter.logError(linhaNome + ":erro return numero de linhas encontrado = " + vlinhas.size());
					return;
				}
				
					linha1=vlinhas.get(0);
					linha2=vlinhas.get(1);
	
				count=0;
				
			} catch (Throwable e){
				count--;
				if(count==0){
					appcenter.logError(linhaNome + ":erro return ao Iniciar BusScanThread " + e);
					return;
				}
			}	
		
		}
	
	
		this.new BusScanLinhaThread(linha1);
		this.new BusScanLinhaThread(linha2);
	
	}
	

	

	/////////////////////////////////////////
	private class BusScanLinhaThread extends Thread {

		LinhaResult linha;
		BusRota[] rotas = new BusRota[3];
		
		public BusScanLinhaThread(LinhaResult linha) {
			this.linha = linha;

			this.start();
		}



		
		@Override
		public void run() {		
			
			
			String idlinha = null;
			
			int count = RETRIES;
			while(count > 0){
				try{
					idlinha = getIDLinha(linha.getLetreiro());
					count =0;
				}catch (Throwable e){
					count--;
					if(count==0){
						appcenter.logError(linhaNome + ":erro return ao Iniciar BusScanLinhaThread getIDLinha " +linha + e);
						return;
					}					
				}
			}	
			

			count = RETRIES;
			while(count > 0){
				try{
					rotas[0]=getRotaBusao(linha.getSentido(), 0, idlinha);
					count =0;
				}catch (Throwable e){
					count--;
					if(count==0){
						appcenter.logError(linhaNome + ":erro return ao Iniciar BusScanLinhaThread getRota 0 " + linha  + e);
						return;
					}					
				}
			}
			

			count = RETRIES;
			while(count > 0){
				try{
					rotas[1]=getRotaBusao(linha.getSentido(), 1, idlinha);
					count =0;
				}catch (Throwable e){
					count--;
					if(count==0){
						appcenter.logError(linhaNome + ":erro warning ao Iniciar BusScanLinhaThread getRota 1 " + linha + e);
						rotas[1]=rotas[0];
					}					
				}
			}

			
			count = RETRIES;
			while(count > 0){
				try{
					rotas[2]=getRotaBusao(linha.getSentido(), 2, idlinha);
					count =0;
				}catch (Throwable e){
					count--;
					if(count==0){
						appcenter.logError(linhaNome + ":erro warning ao Iniciar BusScanLinhaThread getRota 2 " + linha + e);
						rotas[2]=rotas[0];
					}					
				}
			}
			
			boolean criarzipfile = true;
			
			boolean continu=true;
			
			while(true){//loop infinito
				
				if(Timer.getHora()==HORA_STOP)
					continu = false;
				if(Timer.getHora()==HORA_START)
					continu = true;
				
				
				
				//if(Timer.getHora()<HORA_START){//if hora de começar nao chegou espera 10 minutos
				if(!continu){
				
					if(criarzipfile){//criar zip file
						appcenter.log(LoggerThread.ZIPFILE_REQ);
						criarzipfile = false;	// zip file pedido , nao pedir mais 
					}
					
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					
				}else{//else hora de start começou
					criarzipfile= true;// renovando flag de crair zip
					//-->>
					Analinha analinha = new Analinha(rotas[Timer.getDIA()],appcenter);
								
					count = RETRIES_GETPOS;
					
					
					while(continu){//Timer.getHora()>=HORA_START){//loop até meia noite			
						try{					
								Vector<BusPos> posbus = getPosicaoBus(linha.getCodigoLinha());		
								
								analinha.addDado(posbus,linhaNome, Timer.getTime());
								
								Thread.sleep(TIMEOUT);
								count = RETRIES_GETPOS;
								
							}catch (Throwable e){
									count--;
									if(count==0){
									//	count = RETRIES;
										appcenter.logError(linhaNome + ":erro return ao executar getPosicaoBus Verificar Existencia da linha: " + linha  + e);
										return;
									}								
								}	
						
						if(Timer.getHora()==HORA_STOP)
							continu = false;
						if(Timer.getHora()==HORA_START)
							continu = true;

						
						}
					//<<--
					}//else
				}//loop infinito
			
			}
		
		
	}
	///////////////////////////////////////////
	
	
	
	
	
	
	
	
	
	
	static String getIDLinha(String letreiro)throws Throwable{
		
		String HTTPResp = httpGET("200.99.150.170", "/PlanOperWeb/linhaselecionada.asp?Linha="+ letreiro +"&PPD=0&endereco=&numero=&numero_fim=","CdPjOID=",null);
		
		String tmp[] = HTTPResp.split("CdPjOID=");
		tmp= tmp[1].split("&");
		
		String ret = tmp[0];
		//System.out.println("("+letreiro + ")  IDLinha = "+ret);
		return ret;
	}
	
	static String httpGET(String host, String get, String endmark, String failmark) throws Throwable{
		
		Socket srvSoc = null;
		
		if(proxy){
			srvSoc = new Socket(proxyhost,proxyport);
			get = "http://" + host + get;
		}else{
			srvSoc = new Socket(host,80);
		}
		
		srvSoc.setSoTimeout(TCP_TIMEOUT);
		
		OutputStream writer = srvSoc.getOutputStream();
		InputStream reader = srvSoc.getInputStream();

		
		String HTTPReq;		

		HTTPReq = "GET "+ get + " HTTP/1.1" + LE;
		HTTPReq =HTTPReq+ "Host: " + host + LE;
		HTTPReq =HTTPReq + LE+LE;		
		
		writer.write(HTTPReq.getBytes(), 0, HTTPReq.getBytes().length);
		writer.flush();
		
		/////////////////////
		String HTTPResp = "";
	    byte[] buffer = new byte[1024];
		int read = 0;
		
		while ( HTTPResp.indexOf(endmark)<0 &&  (read = reader.read(buffer)) >= 0)
		{
			HTTPResp = HTTPResp + new String(buffer,0,read);
			
			
			if(failmark!=null && (HTTPResp.indexOf(failmark)>0))
				return null;
			
		}
		
		srvSoc.close();
		return HTTPResp;
	}
	
	static BusRota getRotaBusao(int sentido, int dia, String idlinha)throws Throwable{

		String HTTPResp = httpGET("200.99.150.170", "/PlanOperWeb/ABInfSvItiGoogleM.asp?DfSenID=" + sentido + "&CdPjOID=" + idlinha + "&TpDiaID="+ dia , "var first =  true;",null);
		
		String[] tmp = HTTPResp.split(LE);
		String coor=null;
		String distancias =null;
		
		for(int i = 0; i < tmp.length  ; i++){
			if( tmp[i].indexOf("var coor = ") >=0){
				coor = (tmp[i].replaceAll("var coor = ", "")).trim();
				coor = coor.replaceAll("\"","");
				coor = coor.replaceAll(";","");
				
			}
			if( tmp[i].indexOf("var distancias = ") >=0){
				distancias = (tmp[i].replaceAll("var distancias = ", "")).trim();
				distancias = distancias.replaceAll("\"","");
				distancias = distancias.replaceAll(";","");
				distancias = distancias.replaceAll(",", ".");
			}
			if(coor!=null && distancias!= null)
				break;
		}
		
		//try{
			BusRota rota = new BusRota(coor,distancias,sentido,dia);
			return rota;
			
		//}catch (Throwable e){
		//	e.printStackTrace();
		//	return null;
		//}					
		
	}
	
	static Vector<BusPos> getPosicaoBus(String codigoLinha)throws Throwable {
		

		String HTTPResp = httpGET("200.189.189.54", "/InternetServices/PosicaoLinha?cb=jQuery&codigoLinha="+ codigoLinha , "}]}} );",null);
	
		//Mudaram o caminho do serviço
		if(HTTPResp.indexOf("200 OK")<0)
			 HTTPResp = httpGET("200.189.189.54", "/PosicaoServices2/PosicaoLinha?cb=jQuery&codigoLinha="+ codigoLinha , "}]}} );",null);
		
		
		String[] tmp = HTTPResp.split(LE);
		
		
		String strjquery = tmp[tmp.length -1].trim();
		
		//System.out.println(strjquery);		
		

		
		//strjquery = strjquery.replaceAll("\\:","=");		
		//tmp = strjquery.split("PosicaoLinhaResult\\=\\{");
		//strjquery = strjquery.replaceAll("Result","XXXX");		
		

		tmp = strjquery.split("PosicaoLinhaResult\"\\:\\{");
		strjquery = tmp[tmp.length -1].trim();
		
		strjquery = strjquery.replaceAll("\"vs\"\\:","");
		
		strjquery = strjquery.replaceAll("\"","");
		strjquery = strjquery.replaceAll("\\}\\} \\);","");
	
		strjquery = strjquery.replaceAll("\\}","");
		strjquery = strjquery.replaceAll("\\{","");
		strjquery = strjquery.replaceAll("\\]","");
		strjquery = strjquery.replaceAll("\\[","");

		strjquery = strjquery.replaceAll(" ","");

		
		
		String hora="";
		BusPos buspos=null;
		Vector<BusPos> vbuspos = new Vector<BusPos>(0);
		
		tmp = strjquery.split(",");
		for(int i = 0; i< tmp.length ; i++){
			
			
			if(tmp[i].startsWith("hr:")){
				hora=tmp[i].replaceAll("hr\\:", "");
				continue;
			}
					
			if(tmp[i].startsWith("a:")){
				String a =tmp[i].replaceAll("a\\:", "");
				buspos = new BusPos(hora);	
				buspos.setA(Boolean.parseBoolean(a));				
				continue;
			}

			if(tmp[i].startsWith("p:")){
				String p =tmp[i].replaceAll("p\\:", "");
				buspos.setP(p);
				continue;
			}

			if(tmp[i].startsWith("px:")){
				String px =tmp[i].replaceAll("px\\:", "");
				buspos.setPx(Double.parseDouble(px));
				continue;
			}

			if(tmp[i].startsWith("py:")){
				String py=tmp[i].replaceAll("py\\:", "");
				buspos.setPy(Double.parseDouble(py));
				vbuspos.add(buspos);
				buspos=null;
				continue;
			}

			
			
		}
		

			

		
		return vbuspos;
		
	
	}
	
	
	static Vector<LinhaResult> procuraLinha(String linha)throws Throwable{


		String HTTPResp = httpGET("200.189.189.54", "/InternetServices/BuscaLinhasSIM?cb=jQuery&termosBusca=" + linha , "}]} );",null);
		
		String[] tmp = HTTPResp.split(LE);
		
		
		String strjquery = tmp[tmp.length -1].trim();
		
		//System.out.println(strjquery);		
		

		
		//strjquery = strjquery.replaceAll("\\:","=");		
		//tmp = strjquery.split("PosicaoLinhaResult\\=\\{");
		//strjquery = strjquery.replaceAll("Result","XXXX");		
		

		tmp = strjquery.split("BuscaLinhasSIMResult\"\\:\\[\\{");
		strjquery = tmp[tmp.length -1].trim();
		
		strjquery = strjquery.replaceAll("\"vs\"\\:","");
		
		strjquery = strjquery.replaceAll("\"","");
		strjquery = strjquery.replaceAll("\\}\\} \\);","");
	
		strjquery = strjquery.replaceAll("\\}","");
		strjquery = strjquery.replaceAll("\\{","");
		strjquery = strjquery.replaceAll("\\]","");
		strjquery = strjquery.replaceAll("\\[","");

		strjquery = strjquery.replaceAll(" ","");

		strjquery = strjquery.replaceAll("\\);","");

		tmp = strjquery.split(",");
		
		

		LinhaResult linhares=null;
		Vector<LinhaResult> vlinhaResut = new Vector<LinhaResult>(0);
		
		
		for(int i = 0; i< tmp.length ; i++){
			

					
			if(tmp[i].startsWith("Circular:")){
				String Circular =tmp[i].replaceAll("Circular\\:", "");
				linhares = new LinhaResult(Boolean.parseBoolean(Circular));				
				continue;
			}

			if(tmp[i].startsWith("CodigoLinha:")){
				String CodigoLinha =tmp[i].replaceAll("CodigoLinha\\:", "");
				linhares.setCodigoLinha(CodigoLinha);
				continue;
			}

			if(tmp[i].startsWith("DenominacaoTPTS:")){
				String DenominacaoTPTS =tmp[i].replaceAll("DenominacaoTPTS\\:", "");
				linhares.setDenominacaoTPTS(DenominacaoTPTS);
				continue;
			}
			
			if(tmp[i].startsWith("DenominacaoTSTP:")){
				String DenominacaoTSTP =tmp[i].replaceAll("DenominacaoTSTP\\:", "");
				linhares.setDenominacaoTSTP(DenominacaoTSTP);
				continue;
			}

			if(tmp[i].startsWith("Informacoes:")){
				String Informacoes =tmp[i].replaceAll("Informacoes\\:", "");
				linhares.setInformacoes(Informacoes);
				continue;
			}

			if(tmp[i].startsWith("Letreiro:")){
				String Letreiro =tmp[i].replaceAll("Letreiro\\:", "");
				linhares.setLetreiro(Letreiro);
				continue;
			}			
			
			if(tmp[i].startsWith("Sentido:")){
				String Sentido =tmp[i].replaceAll("Sentido\\:", "");
				linhares.setSentido(Integer.parseInt(Sentido));
				continue;
			}	

			if(tmp[i].startsWith("Tipo:")){
				String Tipo =tmp[i].replaceAll("Tipo\\:", "");
				linhares.setTipo(Tipo);
				vlinhaResut.add(linhares);
				linhares=null;
				continue;
			}	
			
		}
		

		
		return vlinhaResut;
	
	}
	
}
