package br.com.sptrans.nt;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import br.com.sptrans.nt.GeoAddr.GeoResult;
import br.com.sptrans.tool.BusPos;
import br.com.sptrans.tool.RotaCrt;




public class RemAcc {
	
	/*Config de propriedades de acesso remoto*/
	public static boolean proxy=false;
	public static String proxyhost ="localhost";
	public static int proxyport = 8080;
	static String LE="\r\n";
	static int TCP_TIMEOUT=5000;
	
	private GeoAddr geoaddr=null;
	private String charencod = System.getProperty("file.encoding","UTF-8");

	public RemAcc() {
		geoaddr= new GeoAddr(proxy,proxyhost,proxyport);
	}

	public Vector<GeoResult> getCoordFromAddr(String logradouro, String numero){
		try{
			
			String address=", São Paulo - SP";
			
			if(numero!=null && !numero.trim().equals("")){
				address= logradouro +" , "+numero+" , São Paulo - SP";
			}else{
				address= logradouro + " , São Paulo - SP";
			}	
			
			return geoaddr.getCordinates(address, "Brasil", charencod);
		}catch (Throwable e){			
			e.printStackTrace();
			return new Vector<GeoResult>(0);
		}
		
	}
	
	
	/*Realiza a procura do ID da ROTA a partir da string letreiro de LinhaInfo 
	*/
	public  void getRotaId(LinhaInfo linhainfo){

			try{
				
				String HTTPResp = httpGET("200.99.150.170", "/PlanOperWeb/linhaselecionada.asp?Linha="+ linhainfo.getLetreiro() +"&PPD=0&endereco=&numero=&numero_fim=","CdPjOID=",null);
				
				String tmp[] = HTTPResp.split("CdPjOID=");
				tmp= tmp[1].split("&");
				
				String ret = tmp[0];
				linhainfo.setIdRotaLinha(ret);
				
			}catch (Throwable e){
				
				System.out.println("Timeout getRotaId = " + linhainfo.getLetreiro());
				return;
			}
				
			
			
		  
	  }

	/*Realiza a procura da ROTA a partir da string idRota de LinhaInfo 
	*/
	  public void getRota(int sentido, int dia, LinhaInfo linhainfo)throws Throwable{

			try{	  
		  
				String HTTPResp = httpGET("200.99.150.170", "/PlanOperWeb/ABInfSvItiGoogleM.asp?DfSenID=" + sentido + "&CdPjOID=" + linhainfo.getIdRotaLinha() + "&TpDiaID="+ dia , "var first =  true;",null);
				
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
			
				if(coor!=null && coor.equals(""))
					coor=null;
				
				
				linhainfo.setupCoord(sentido, dia, new String(coor));
				return ;
				
			}catch (Throwable e){
				System.out.println("Erro lendo rota " + linhainfo.getLinha() + "sent=" + sentido + "  dia=" + dia );
				//e.printStackTrace();

			}	
			
			return ;
		  
		  
	  }
	
	/*Realiza a procura de informações da linha e adiciona em LinhaInfo
	*/
	  public void getLinhaInfo(String linha, LinhaInfo dest)throws Throwable{


			String HTTPResp=null;
			
			
			try {
					HTTPResp = httpGET("200.189.189.54", "/InternetServices/BuscaLinhasSIM?cb=jQuery&termosBusca=" + linha , "}]} );",null);
				}
				catch (java.net.SocketTimeoutException e){
					System.out.println("Timeout " + linha);
					return;
			}
			
			String[] tmp = HTTPResp.split(LE);
			String strjquery = tmp[tmp.length -1].trim();
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

			String Circular=null;
			String CodigoLinha=null;
			String DenominacaoTPTS=null;
			String DenominacaoTSTP=null;
			String Informacoes=null;
			String Letreiro=null;
			String Tipo=null;
			int sentido=-1;
			

			
			for(int i = 0; i< tmp.length ; i++){
				

						
				if(tmp[i].startsWith("Circular:")){
					Circular =tmp[i].replaceAll("Circular\\:", "");
					continue;
				}

				if(tmp[i].startsWith("CodigoLinha:")){
					CodigoLinha =tmp[i].replaceAll("CodigoLinha\\:", "");
					continue;
				}

				if(tmp[i].startsWith("DenominacaoTPTS:")){
					DenominacaoTPTS=tmp[i].replaceAll("DenominacaoTPTS\\:", "");
					continue;
				}
				
				if(tmp[i].startsWith("DenominacaoTSTP:")){
					DenominacaoTSTP =tmp[i].replaceAll("DenominacaoTSTP\\:", "");
					continue;
				}

				if(tmp[i].startsWith("Informacoes:")){
					Informacoes =tmp[i].replaceAll("Informacoes\\:", "");
					continue;
				}

				if(tmp[i].startsWith("Letreiro:")){
					Letreiro =tmp[i].replaceAll("Letreiro\\:", "");
					continue;
				}			
				
				if(tmp[i].startsWith("Sentido:")){
					sentido = Integer.parseInt(tmp[i].replaceAll("Sentido\\:", ""));
					continue;
				}	

				if(tmp[i].startsWith("Tipo:")){
					Tipo=tmp[i].replaceAll("Tipo\\:", "");		
					
					switch (sentido){
					case 1:
						dest.setup1(Circular, CodigoLinha, DenominacaoTPTS, DenominacaoTSTP, Informacoes, Letreiro, Tipo);
						break;
					case 2:
						dest.setup2(Circular, CodigoLinha, DenominacaoTPTS, DenominacaoTSTP, Informacoes, Letreiro, Tipo);
						break;					
					}
					
					continue;
				}	
				
			}
			
		
		}
	  
	/*Wrapper do Servidro da SPtrans para a procura de Linhas realizada pela aplicac~ao
	*/
	  public Hashtable<String,String> procuraLinha(String linha)throws Throwable{
		  
	  		Hashtable<String,String> ret = new Hashtable<String,String>(0); 


			String HTTPResp = httpGET("200.189.189.54", "/InternetServices/BuscaLinhasSIM?cb=jQuery&termosBusca=" + linha , "}]} );","{\"BuscaLinhasSIMResult\":null}");
			if(HTTPResp==null)
				return ret;
			
			//Content-Type: application/json; charset=utf-8\r\n
			//Server: Microsoft-HTTPAPI/2.0
			HTTPResp = arrumacharenc(HTTPResp,"UTF-8");
			
			
			
			String[] tmp = HTTPResp.split(LE);
			String strjquery = tmp[tmp.length -1].trim();
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


			String codigolinha="";
			String letreiro="";
			int sentido=-1;
			String denominacaoTPTS="";
			String denominacaoTSTP="";
			
			for(int i = 0; i< tmp.length ; i++){
				

						
				if(tmp[i].startsWith("Circular:")){
					String Circular =tmp[i].replaceAll("Circular\\:", "");
					//linhares = new LinhaResult(Boolean.parseBoolean(Circular));				
					continue;
				}

				if(tmp[i].startsWith("CodigoLinha:")){
					String CodigoLinha =tmp[i].replaceAll("CodigoLinha\\:", "");
					codigolinha= CodigoLinha;
					continue;
				}

				if(tmp[i].startsWith("DenominacaoTPTS:")){
					String DenominacaoTPTS =tmp[i].replaceAll("DenominacaoTPTS\\:", "");
					denominacaoTPTS=DenominacaoTPTS;
					continue;
				}
				
				if(tmp[i].startsWith("DenominacaoTSTP:")){
					String DenominacaoTSTP =tmp[i].replaceAll("DenominacaoTSTP\\:", "");
					denominacaoTSTP=DenominacaoTSTP;//linhares.setDenominacaoTSTP(DenominacaoTSTP);
					continue;
				}

				if(tmp[i].startsWith("Informacoes:")){
					String Informacoes =tmp[i].replaceAll("Informacoes\\:", "");
					//linhares.setInformacoes(Informacoes);
					continue;
				}

				if(tmp[i].startsWith("Letreiro:")){
					String Letreiro =tmp[i].replaceAll("Letreiro\\:", "");
					letreiro=Letreiro;
					continue;
				}			
				
				if(tmp[i].startsWith("Sentido:")){
					String Sentido =tmp[i].replaceAll("Sentido\\:", "");
					sentido = Integer.parseInt(Sentido);
					continue;
				}	

				if(tmp[i].startsWith("Tipo:")){
					String Tipo =tmp[i].replaceAll("Tipo\\:", "");

						
					String key;
					
					if(sentido==1){
						key=letreiro + "-" +Tipo +"  "+ denominacaoTPTS;
					}else{	
						key=letreiro + "-" +Tipo +"  "+ denominacaoTSTP;
					}

					ret.put(key, codigolinha);
					
					continue;
				}	
				
			}
			


			
			return ret;
		
		}
	  
	  
	  
		public boolean getCurrentPosicaoBus(String codigoLinha ,Hashtable<String,BusPos> vbuspos, long timestamp,RotaCrt rotaCrt)throws Throwable {
			vbuspos.clear();

			String HTTPResp = httpGET("200.189.189.54", "/InternetServices/PosicaoLinha?cb=jQuery&codigoLinha="+ codigoLinha , "}]}} );","\",\"vs\":[]}} );");
		
			if(HTTPResp==null)
				return false;
			
			//Mudaram o caminho do serviço
			if(HTTPResp.indexOf("200 OK")<0)
				 HTTPResp = httpGET("200.189.189.54", "/PosicaoServices2/PosicaoLinha?cb=jQuery&codigoLinha="+ codigoLinha , "}]}} );","\",\"vs\":[]}} );");

			if(HTTPResp==null)
				return false;

			
			
			String[] tmp = HTTPResp.split(LE);
			
			
			String strjquery = tmp[tmp.length -1].trim();

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
			boolean ret=false;
			
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
					if(buspos!=null){
						buspos.setPy(Double.parseDouble(py));
						
						buspos.setTimeStamp(timestamp);
						buspos.setPosMetric(rotaCrt.calcDistPercorrida(buspos.getCoord()));
						//Adicionaro Onibus ID
						vbuspos.put(buspos.getP(), buspos);
						
						
						
						ret=true;
						
						
					}

					
					buspos=null;
					continue;
				}

				
				
			}
			

				

			return ret;
		
		}
	  
		public String getmyip() throws Throwable{
			
			String HTTPResp = httpGET("automation.whatismyip.com", "/n09230945.asp" , "\r\n0\r\n","XX");
			//URL whatismyip = new URL("http://automation.whatismyip.com/n09230945.asp");
		    //BufferedReader inIP = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
		    //String ip = inIP.readLine();
			
			return HTTPResp;
		}
		
		
	  /*
	   * Funcão interna para realizar o http request
	   * */
		private String httpGET(String host, String get, String endmark, String failmark) throws Throwable{
			
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

			try{
			
				while ( HTTPResp.indexOf(endmark)<0 &&  (read = reader.read(buffer)) >= 0)
				{
					HTTPResp = HTTPResp + new String(buffer,0,read);
					
					
					if(failmark!=null && (HTTPResp.indexOf(failmark)>0))
						return null;
					
				}
			}catch (Throwable e){
				System.out.println(HTTPResp);
				throw e;
			}
			
			srvSoc.close();
			return HTTPResp;
		}
		
				
		public void atualizaBD() throws Throwable{
			Hashtable<String,LinhaInfo>  Hlinhas = new Hashtable<String,LinhaInfo>(0) ;

			readSetupFile("../dat/linfo2.txt",Hlinhas);

			Enumeration<String> en = Hlinhas.keys();
					
			int count=0;
			while(en.hasMoreElements()){
				String linha = en.nextElement();
				LinhaInfo linhainfo = Hlinhas.get(linha);

				//getLinhaInfo(linha,linhainfo);
				
				
				if("null".equals(linhainfo.getIdRotaLinha()))
					getRotaId(linhainfo);
					

				if("null".equals(linhainfo.coord_1_0))
					getRota(1, 0,linhainfo);
				if("null".equals(linhainfo.coord_1_1))
					getRota(1, 1,linhainfo);
				if("null".equals(linhainfo.coord_1_2))
					getRota(1, 2,linhainfo);
				if("null".equals(linhainfo.coord_2_0))
					getRota(2, 0,linhainfo);
				if("null".equals(linhainfo.coord_2_1))
					getRota(2, 1,linhainfo);
				if("null".equals(linhainfo.coord_2_2))
					getRota(2, 2,linhainfo);


				
				appendlineTofile("/tmp/linfo_g.txt",Hlinhas.get(linha).toString());
			}

		
			
			
		}
		

		/*Popula o Hashtable <linha,LiinhaInfo) a partir do arquivo BD 
		 * */
		public void readSetupFile(String file,Hashtable<String,LinhaInfo> Hlinhas ) throws Throwable {
				
				try{
					FileInputStream fstreamX = new FileInputStream(file);
					DataInputStream inX = new DataInputStream(fstreamX);
					BufferedReader brX = new BufferedReader(new InputStreamReader(inX));
					
					String strLineX;
						  //Read File Line By Line
						 while ((strLineX = brX.readLine()) != null)   {
							 LinhaInfo linhainfo = LinhaInfo.getLineInfoFromLine(strLineX);
							 Hlinhas.put(linhainfo.getLinha(), linhainfo);

						 }  
					}catch (Throwable e){
						
						System.err.println("path= "+ (new File(".")).getAbsolutePath() + " Erro ao ler arquivo " + file +  " : " + e);
						e.printStackTrace();
						throw e;
					}
				  
			  }  
		

		
		 /*Função para adicionar uma lionha a um arquivo
		  * */
		  public void appendlineTofile(String file,String line) throws Throwable{
				  
					FileOutputStream logwrite = new FileOutputStream(file,true);
					logwrite.write(line.getBytes(),0,line.getBytes().length);
					logwrite.write("\n".getBytes(),0,"\n".getBytes().length);
					logwrite.flush();
					logwrite.close();
			  }
		  
		  
		  
		/*
		 * Dada uma String (ruim) com char encoding encruim, transforma essa string em uma string nova no char encoding do systema
		 * */
		private  String arrumacharenc(String ruim,String encruim){
			try {
					return new String(ruim.getBytes(),encruim);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}		
			return ruim;
		}
			
}
