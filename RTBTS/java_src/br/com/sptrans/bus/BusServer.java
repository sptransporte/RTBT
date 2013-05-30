package br.com.sptrans.bus;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


import br.com.sptrans.nt.LinhaInfo;
import br.com.sptrans.nt.RemAcc;
import br.com.sptrans.nt.GeoAddr.GeoResult;
import br.com.sptrans.srv.Server.MyHandler;
import br.com.sptrans.tool.GraphingData;
import br.com.sptrans.tool.LinhaSMonitor;
import br.com.sptrans.tool.Timer;

import com.sun.net.httpserver.HttpServer;




public class BusServer {

	static Hashtable<String,LinhaInfo>  Hlinhas=null;
	static Hashtable<String,LinhaSMonitor>  HlinhasMon=null;
	static RemAcc remacc;
	Timer timerMonitor= null;
	private static boolean sendm=true;

	
	static { 
		
		/// ARQUIVO DE CONFIGURAÇÃO INICIAL -->		
		try{
			FileInputStream fstreamX = new FileInputStream("config.txt");
			DataInputStream inX = new DataInputStream(fstreamX);
			BufferedReader brX = new BufferedReader(new InputStreamReader(inX));
			String strLineX;
				  //Read File Line By Line
				 while ((strLineX = brX.readLine()) != null && !strLineX.trim().equals(""))   {
					 strLineX=strLineX.trim();
					 String[] tmp = strLineX.split("=");
					 if(tmp[0].equalsIgnoreCase("proxy"))remacc.proxy=Boolean.parseBoolean(tmp[1]);
					 if(tmp[0].equalsIgnoreCase("sendm"))sendm=Boolean.parseBoolean(tmp[1]);
					 
				 }  
			}catch (Throwable e){
			}	
		/// <--

		
		remacc = new RemAcc();
	    
		try {
			
			HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		    server.createContext("/", new MyHandler());
		    server.setExecutor(null); // creates a default executor
		    server.start();
		    
		    
		    // Get IP Address
		    if(sendm){
		    	//gc.sendmsg("sobejecto", "endereco " + remacc.getmyip() + " \n");
		    }

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}


		Timer timerMonitor = new Timer(); 
		timerMonitor.start();

		
		
		} 
	
	

	


	
	public static void startupServer(int dia) throws Throwable{

		synchronized (remacc){
			

		
		if(Hlinhas != null || HlinhasMon != null )
			shutDownServer();
		
		Hlinhas = new Hashtable<String,LinhaInfo>(0) ;
		HlinhasMon = new Hashtable<String,LinhaSMonitor>(0) ;
		
		//Para atualizar BD e ajudar a verificar se ouve mudancas nas linhas 
		//remacc.atualizaBD();
		
		System.out.println("Carregando BD Aguarde:");
		remacc.readSetupFile("bd.dat",Hlinhas);
		
		Enumeration<String> keys = Hlinhas.keys();
		while(keys.hasMoreElements()){
			
			String linha = keys.nextElement();
			//System.out.println(linha); 
			//if(linha.startsWith("477A")){
				LinhaInfo linfo = Hlinhas.get(linha);
				
				System.out.println(" Iniciando lss " + linfo.getLinha() + " " + linfo.getCodigoLinha(1) + "  " + linfo.getCodigoLinha(2));
				
				LinhaSMonitor lsm1= new LinhaSMonitor(linfo,1,dia,remacc);
				LinhaSMonitor lsm2= new LinhaSMonitor(linfo,2,dia,remacc);
				lsm1.setSister(lsm2);
				lsm2.setSister(lsm1);
				
				lsm1.start();
				lsm2.start();
				
				HlinhasMon.put(linfo.getCodigoLinha(1), lsm1);
				HlinhasMon.put(linfo.getCodigoLinha(2), lsm2);
				
			//}
			
		}
		
		System.out.println("BD Carregado:");
		}	
	}

	public static void shutDownServer(){
		
	
		if(HlinhasMon!=null){
			
			Enumeration<String> enk = HlinhasMon.keys();
			while(enk.hasMoreElements()){
				String key =enk.nextElement();
				LinhaSMonitor lsm = HlinhasMon.get(key);
				if(lsm!=null)
					lsm.run=false;
				
			}
				
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		HlinhasMon=null;
		
		if(Hlinhas!=null)
			Hlinhas.clear();
		Hlinhas=null;
		System.gc();
	}
	
	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		

		
		// TODO Auto-generated method stub
		System.out.println("encoding = " +  System.getProperty("file.encoding","UTF-8"));
	    
	    Thread.sleep(1000);
		synchronized (remacc){
			
		}
	    ////
		
		String codLinha=null;
		double lat=-1;;
		double lng=-1;
		
		while (true){
			System.out.println("");
			System.out.println("1-procuraLinha");
			System.out.println("2-estimaTempoChegada");
			System.out.println("3-Repete Estimativa");
			System.out.println("4-Debug Linha");
			System.out.println("5-Exit");
			System.out.println("Escolha a comando:");
			int opt = -1;
			
			try{
				opt = Integer.parseInt(readKeyboardLine());
			}catch (Throwable e){
				
			}
			
			switch (opt){
				case 1:
					System.out.println("procuraLinha| Entre com a String de procura");
					String linha = readKeyboardLine();
					Hashtable<String,String> ret = procuraLinha(linha);
					if(ret==null){
						System.out.println("procuraLinha| Erro retorno nulo");
						break;
					}
					if(ret.size() == 0){
						System.out.println("procuraLinha| Erro retorno vazio");
						break;
					}
					
					Enumeration<String> enk = ret.keys();
					while(enk.hasMoreElements()){
						String key = enk.nextElement();
						String value = ret.get(key);
						System.out.println(key+" "+value);						
					}		
					
					break;
				case 2:
					System.out.println("estimaTempoChegada| Entre com : codlinha , Latitude , Longitude");
					String dadolido=null;
					int estim;
					try{
						dadolido = readKeyboardLine(); 
						String tmp[] = dadolido.split(",");
						codLinha = tmp[0];
						lat = Double.parseDouble(tmp[1]);
						lng =  Double.parseDouble(tmp[2]);
						estim = estimaTempoChegada(codLinha,lat,lng);
						System.out.println("estimaTempoChegada| ret = " + estim);
					}catch (Throwable e){
						System.out.println("estimaTempoChegada| Erro no parse de : " + dadolido);
					}
					
					break;
					
				case 3:
					estim = estimaTempoChegada(codLinha,lat,lng);
					System.out.println("estimaTempoChegada| ret = " + estim);
					
					break;


				case 4:
					System.out.println("Debug Linha| Entre com codlinha");
					DebugFuncaoTempo(readKeyboardLine());
					break;
					
					
				case 5:
					System.exit(0);
					break;

			}
			
		}

	    
	}
	
	private static String readKeyboardLine() throws Throwable{
		InputStreamReader isr = new InputStreamReader( System.in );  
        BufferedReader stdin = new BufferedReader( isr );  
        String input = stdin.readLine();  
		return input;
	}
	

	  public static int estimaTempoChegada(String codlinha,double lat,double lon)throws Throwable{
		  
			if(!HlinhasMon.containsKey(codlinha))
				return -1;
			
			return HlinhasMon.get(codlinha).estimaTempo(lat, lon);
	 }
	
	  public static String estimaTempoChegadaS(String codlinha,double lat,double lon){
		  
		if(!HlinhasMon.containsKey(codlinha))
			return "Codigo Linha Não Encontrado";
		
		return HlinhasMon.get(codlinha).estimaTempoS(lat, lon);
 }

	  public static BusResult estimaTempoChegadaBR(String codlinha,double lat,double lon,double lat_d,double lon_d){
		  
		if(!HlinhasMon.containsKey(codlinha))
			return null;
		
		return HlinhasMon.get(codlinha).estimaTempoBR(lat, lon,lat_d,lon_d);
 }
	  
	  
	
	  public static Vector<GeoResult> getCoordFromAddr(String logradouro, String numero){
		  
		  return remacc.getCoordFromAddr(logradouro, numero);
	  }
	  
	  public static Hashtable<String,String> procuraLinha(String linha)throws Throwable{
		
		Hashtable<String,String>  ret;
		
		try {
			ret = remacc.procuraLinha(linha);
		} catch (Throwable e) {
			ret = new Hashtable<String,String>(0);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return ret;
	}
	
	private static void DebugFuncaoTempo(String codlinha){
		if(!HlinhasMon.containsKey(codlinha)){
			System.out.println("Codigo Linha nao encontrado ");
			return;
		}
				

		
		LinhaSMonitor lsm = HlinhasMon.get(codlinha);
		System.out.println("Origem Destino " + lsm.getDebugInFin());
		
		GraphingData.plot(lsm.getDebugDescr() + " DXT",lsm.getDebugFunc(), lsm.getDebugLinhaExt(),"dist percorrida (m)","T");
		GraphingData.plot(lsm.getDebugDescr() + " TXD",lsm.getDebugFunc2(), lsm.getDebugTotalTime(),"tempo percorrido (s)","M");
		
	}

	

	  

	  
}
