package br.com.sptrans.tst;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import br.com.sptrans.bus.BusResult;
import br.com.sptrans.bus.BusResult.Bus;
import br.com.sptrans.bus.BusServer;




public class InitTest {
	
	static boolean proxy=false;
	static String proxyhost ="localhost";
	static int proxyport = 8080;
	static String LE="\r\n";

	
	
	/**
	 * @param args
	 * @throws Throwable 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws Throwable {
		
		
		BusResult br = BusServer.estimaTempoChegadaBR("", 0, 0, 0, 0);
		
		br.getDistDestino();
		br.getDistOrigem();
		br.getDviagem();
		br.getTviagem();
		
		for(int i=0; i< br.getVbus().size(); i++){
			Bus bus = 	(Bus) br.getVbus().get(i);
			bus.getBusId();
			bus.getEstim();
			bus.isAcess();
			bus.getdistU();

		}
		
		
		
		try{
			proxyhost=new String(args[0]);
			proxyport=Integer.parseInt(new String(args[1]));
			proxy=true;
			
		}catch (Throwable e){
			proxy=false;
		}
		System.out.println("proxy = " + proxy);
		if(proxy)
			System.out.println(proxyhost +" "+ proxyport);
		
	
//		procuraLinhaX("477A");
//		System.exit(0);
		
		/*
		//---->Pega detalhes da linha percorrendo o range de ids  e salva em arquivo
		for(int i=1; i< 2200 ; i++){
			
			boolean retry=true;
			
			while(retry){
				try{
					System.out.println(i);
					String result = getDetalheLinha("" + i);
					appendlineTofile("/tmp/dat.txt",i + " # " + result);
					retry=false;
				}catch (java.net.SocketTimeoutException e){
					 //e.printStackTrace();
					retry=true;
				 }
			}
			
			retry=true;
			int j = i+ 32768;
			while(retry){
				try{
					System.out.println(j);
					String result = getDetalheLinha("" + j);
					appendlineTofile("/tmp/dat.txt",j + " # " + result);
					retry=false;
				}catch (java.net.SocketTimeoutException e){
					 //e.printStackTrace();
					retry=true;
				 }
			}
			
			
		}
		System.exit(0);
		//<<-------------
		*/
		
		
		/*
		//->>> PEga o CodigoLinha do arquivo e verifica se tem "sinal"de GPS
		 FileInputStream fstreamX = new FileInputStream("/home/usr/JWS/cod.txt");
		 DataInputStream inX = new DataInputStream(fstreamX);
		 BufferedReader brX = new BufferedReader(new InputStreamReader(inX));
		 String strLineX;
		  //Read File Line By Line
		 while ((strLineX = brX.readLine()) != null)   {
			 
			 	String result="erro";
			 
				 try{
			 
					 
					 Vector<BusPos> posbus = getPosicaoBus(strLineX);
					 result="size= " +posbus.size();
					 
				 }catch (java.net.SocketTimeoutException e){
					 //e.printStackTrace();
					 
				 }
				 System.out.println (strLineX + " # " + result);
				 appendlineTofile("/tmp/dat.txt",strLineX + " # " + result);
		 }
		 System.exit(0);
		//<---------------
		*/
		
		/*
		//PEGA as rota e codigo linha  das linhas-->>>
		 FileInputStream fstream = new FileInputStream("/home/usr/JWS/linhas.txt");
		 DataInputStream in = new DataInputStream(fstream);
		 BufferedReader br = new BufferedReader(new InputStreamReader(in));
		 String strLine;
		  //Read File Line By Line
		 while ((strLine = br.readLine()) != null)   {
			 
			 String lineA=null;
			 String lineB=null;
			 
			 boolean retry =true;
			 while (retry){
				 try{
				 
					  // Print the content on the console
					 System.out.println (strLine);
					 Vector<LinhaResult> vlinhas = procuraLinha(strLine);//
					if(vlinhas.size()!=2){
							System.out.println("Numero delinhas deve ser 2  encontrado:" + vlinhas.size());
							System.exit(0);
					}
					LinhaResult linhaA=vlinhas.get(0);
					LinhaResult linhaB=vlinhas.get(1);
					
					String idlinhaA = getIDLinha(linhaA.getLetreiro());
					String idlinhaB = getIDLinha(linhaB.getLetreiro());
					
					if(!idlinhaA.endsWith(idlinhaB)){
						System.out.println("idlinhas diferentes: " + idlinhaA + "  " + idlinhaB);
						System.exit(0);
					}
					
					
					BusRota busrotaA = getRotaBusao(linhaA.getSentido(), 0, idlinhaA);
					BusRota busrotaB = getRotaBusao(linhaB.getSentido(), 0, idlinhaA);
					
					
					lineA=linhaA + "#" + busrotaA;
					lineB=linhaB + "#" + busrotaB;
					
					retry=false;
					
				 }catch (java.net.SocketTimeoutException e){
					 e.printStackTrace();
					 retry=true; 
				 }
		 	}
			
			appendlineTofile("/tmp/dat.txt",lineA);
			appendlineTofile("/tmp/dat.txt",lineB);
			
			  }
		 System.exit(0);
		 //XXXXXXXXXXXXXXXXXXXXXXXXXXXXX->>
		 */
		 
		
		/*
		//-->BUSCA DE LINHAS NO BANCO DE DADOS 
		int ini=65000;
		try{
			ini = Integer.parseInt(args[0]);
		}catch (Throwable e){
			
		}
		
		
		int i=-1;
		for(i =ini; i< 72001; i++){
			//Thread.sleep(1000);
			System.out.println(i);
			
			boolean cont=true;
			while(cont){
				try{
					getDetalheLinha2(0,i,"/tmp/dados.txt");
					cont=false;
				}catch (Throwable e){
					cont=true;
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("**"+(i-1));
		System.exit(0);		
		//XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX<--
		*/
		
		//[0] linha [1] sentido [2] lat,long ref  
		String[] argx = {"477a","1","-23.53304,-46.734483"};args=argx;
		//String[] argx = {"8060-10","1","-23.53304,-46.734483"};args=argx;
		//String[] argx = {"477a","1","-23.533483,-46.734625"};args=argx;
		//String[] argx = {"7550-10","2","-23.53304,-46.734483"};args=argx;
		
		Vector<LinhaResult> vlinhas = procuraLinha(args[0]);//

		if(vlinhas.size()!=2){
			System.out.println("Numero delinhas deve ser 2  encontrado:" + vlinhas.size());
			System.exit(0);
		}
		
		LinhaResult linha =null;
		
		System.out.println("vlinhas.get(0).getSentido() = " + vlinhas.get(0).getSentido() + "  " + vlinhas.get(0).getLetreiro());
		System.out.println("vlinhas.get(1).getSentido() = " + vlinhas.get(1).getSentido() + "  " + vlinhas.get(1).getLetreiro());
		
		if(vlinhas.get(0).Sentido == Integer.parseInt(args[1]))
			linha=vlinhas.get(0);
		if(vlinhas.get(1).Sentido == Integer.parseInt(args[1]))
			linha=vlinhas.get(1);

		if(linha==null){
			System.out.println("Sentido " + args[1] + " nao encontrado");
			System.exit(0);
		}
		
		
		getDetalheLinha(linha.getCodigoLinha());

		
		String idlinha = getIDLinha(linha.getLetreiro());

		BusRota busrota = getRotaBusao(linha.getSentido(), 0, idlinha);

		//System.out.println(busrota.rawCoord);
		System.out.println("Extencao calculada = " + busrota.getExtencao());
		System.out.println(linha.getDestString() + " sent=" +linha.getSentido());
		
		PositionMetric distRef = busrota.calcDistPercorrida(new Coord(args[2]));
		System.out.println("Dist Referencia : " + distRef.distPercorrida);
		Analinha analinha = new Analinha(busrota,distRef);
		while (true){

			
			Vector<BusPos> posbus = getPosicaoBus(linha.getCodigoLinha());
			
			analinha.addDado(posbus);

			//analinha.getLinhaProjetion();

			Thread.sleep(3000);
		}
		
		//System.exit(0);
		
		
		/*
		Vector LinhaEncontradas = procuraLinha("477A");

		while (true){
		
			Enumeration en = LinhaEncontradas.elements();
			while(en.hasMoreElements()){
		
				LinhaResult linhares =(LinhaResult)en.nextElement();			
				System.out.println("Sentido "+ linhares.getSentido());
				
				String idLinha = getIDLinha(linhares.getLetreiro());			
				BusRota busrota = getRotaBusao(linhares.getSentido(), 0, idLinha);
				
				System.out.println(busrota);
				
				Coord minhapos = new Coord(-23.572877, -46.611021);
				
	
				
				Vector vbuspos = getPosicaoBus(linhares.CodigoLinha);
				busrota.xpto(vbuspos,minhapos,"21611");
				
				}
				Thread.sleep(3000);
			}
			
		*/
		
		
	}	
	

	
	static String httpGET(String host, String get, String endmark, String failmark) throws Throwable{
		
		Socket srvSoc = null;
		
		if(proxy){
			srvSoc = new Socket(proxyhost,proxyport);
			get = "http://" + host + get;
		}else{
			srvSoc = new Socket(host,80);
		}
		
		srvSoc.setSoTimeout(10000);
		
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
		Vector<LinhaResult> vlinhaResut = new Vector(0);
		
		
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
		

		Enumeration en = vlinhaResut.elements();
		while(en.hasMoreElements()){
			linhares =(LinhaResult)en.nextElement();
			System.out.println(linhares);			
		}
		
		return vlinhaResut;
	
	}
	
	
	static Vector<BusPos> getPosicaoBus(String codigoLinha)throws Throwable {

		long tempoAmostra = System.currentTimeMillis()/1000;
		
		String horaap =now();
		String HTTPResp = httpGET("200.189.189.54", "/PosicaoServices2/PosicaoLinha?cb=jQuery&codigoLinha="+ codigoLinha , "}]}} );",null);
		String horadp =now();
		
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
		Vector vbuspos = new Vector(0);
		
		tmp = strjquery.split(",");
		for(int i = 0; i< tmp.length ; i++){
			
			
			if(tmp[i].startsWith("hr:")){
				hora=tmp[i].replaceAll("hr\\:", "");
				continue;
			}
					
			if(tmp[i].startsWith("a:")){
				String a =tmp[i].replaceAll("a\\:", "");
				buspos = new BusPos(hora,horaap,horadp,tempoAmostra);	
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
		

		//System.out.println("hora " + hora);			
		Enumeration en = vbuspos.elements();
		while(en.hasMoreElements()){
			BusPos bpos =(BusPos)en.nextElement();
			//System.out.println(bpos);
			
		}
				

		
		return vbuspos;
		
	
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
		
		try{
			BusRota rota = new BusRota(coor,distancias,sentido,dia);
			return rota;
			
		}catch (Throwable e){
			e.printStackTrace();
			return null;
		}	
		

					
		
	}
	
	
	static String getIDLinha(String letreiro)throws Throwable{
		
		String HTTPResp = httpGET("200.99.150.170", "/PlanOperWeb/linhaselecionada.asp?Linha="+ letreiro +"&PPD=0&endereco=&numero=&numero_fim=","CdPjOID=",null);
		
		String tmp[] = HTTPResp.split("CdPjOID=");
		tmp= tmp[1].split("&");
		
		String ret = tmp[0];
		System.out.println("("+letreiro + ")  IDLinha = "+ret);
		return ret;
	}
	
	static String getDetalheLinha(String codigoLinha)throws Throwable{

		String HTTPResp = httpGET("200.189.189.54", "/InternetServices/CarregaDetalhesLinha?cb=jQuery&codigoLinha=" + codigoLinha,"]} );",null);


		String tmp[] = HTTPResp.split("jQuery");

		try{
			System.out.println(tmp[1]);
			
		}catch (Throwable e){
			System.out.println("/InternetServices/CarregaDetalhesLinha?cb=jQuery&codigoLinha=" + codigoLinha);
			
			System.out.println(HTTPResp);
			
			
			
			throw new Throwable(e);
		}
			
			
		return tmp[1];
	}
	
	static void getDetalheLinha2(int dia, int oid,String filetoappend)throws Throwable{
		
		String HTTPResp = httpGET("200.99.150.170", "/PlanOperWeb/detalheLinha.asp?TpDiaID="+dia+"&CdPjOID="+oid,"</html>","Internal Server Error");

		if(HTTPResp != null){

			String noLinha="";
			String nomeLinha="";
			String areCod="";
			String consorcio="";
			String empresa="";
			
			
			int ini = HTTPResp.indexOf("id=\"noLinha\"");
			if(ini>0){
				int fin = HTTPResp.indexOf("/>", ini);
				noLinha = HTTPResp.substring(ini, fin);
			}
			
			ini = HTTPResp.indexOf("id=\"nomeLinha\"");
			if(ini>0){
				int fin = HTTPResp.indexOf("/>", ini);
				nomeLinha = HTTPResp.substring(ini, fin);
			}
			
			ini = HTTPResp.indexOf("id=\"areCod\"");
			if(ini>0){
				int fin = HTTPResp.indexOf("/>", ini);
				areCod = HTTPResp.substring(ini, fin);
			}
			
			ini = HTTPResp.indexOf("id=\"consorcio\"");
			if(ini>0){
				int fin = HTTPResp.indexOf("/>", ini);
				consorcio = HTTPResp.substring(ini, fin);
			}
			
			ini = HTTPResp.indexOf("id=\"empresa\"");
			if(ini>0){
				int fin = HTTPResp.indexOf("/>", ini);
				empresa = HTTPResp.substring(ini, fin);
			}
			
			String tst = oid + "#" + noLinha+ "#"	+ nomeLinha+ "#" + areCod+ "#" + consorcio+ "#"	+ empresa ;

			System.out.println(tst);
			
			appendlineTofile(filetoappend,tst);
			
			
		}
		
	}
		
	  public static String now() {
		    Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss:SSS");
		    return sdf.format(cal.getTime());

		  }
	  
	  public static void appendlineTofile(String file,String line) throws Throwable{
		  
			FileOutputStream logwrite = new FileOutputStream(file,true);
			logwrite.write(line.getBytes(),0,line.getBytes().length);
			logwrite.write("\n".getBytes(),0,"\n".getBytes().length);
			logwrite.flush();
			logwrite.close();
	  }
	  
	  static Hashtable<String,String> procuraLinhaX(String linha)throws Throwable{
		  
		  		Hashtable<String,String> ret = new Hashtable<String,String>(0); 


				String HTTPResp = httpGET("200.189.189.54", "/InternetServices/BuscaLinhasSIM?cb=jQuery&termosBusca=" + linha , "}]} );",null);
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
							key=letreiro + "-" + denominacaoTPTS;
						}else{	
							key=letreiro + "-" + denominacaoTSTP;
						}
						System.out.println(key + " " + codigolinha);
						ret.put(key, codigolinha);
						
						continue;
					}	
					
				}
				


				
				return ret;
			
			}
	  
	  int estimaTempoChegada(int codlinha,double lat,double lon){
		  
		  return 5*60;
	  }
}




