package br.com.sptrans.srv;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import br.com.sptrans.bus.BusResult;
import br.com.sptrans.bus.BusServer;
import br.com.sptrans.bus.BusResult.Bus;
import br.com.sptrans.nt.LinhaInfo;
import br.com.sptrans.nt.GeoAddr.GeoResult;
import br.com.sptrans.rtbt.des.Analisa;
import br.com.sptrans.tool.LinhaSMonitor;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;




public class Server {
	

	static private String charencod = System.getProperty("file.encoding","UTF-8");
	
	
    public static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {

        	if(!t.getRequestMethod().equalsIgnoreCase("get") ){// || !t.getRequestURI().toString().startsWith("/scanbus")){
        		return;
        	}
        	//System.out.println(t.getRequestMethod());	
        	//System.out.println(t.getRequestURI());
        	//printHeaders(t.getRequestHeaders());
        	
        	
        	if(t.getRequestURI().toString().startsWith("/scanbusp")){
        		String linhaquery = t.getRequestURI().toString().replaceFirst("/scanbusp", "");
        		httpProcuraLinha(t,linhaquery);
        		return;
        	}

        	
        	
        	if(t.getRequestURI().toString().startsWith("/scanbuse")){
        		String estima = t.getRequestURI().toString().replaceFirst("/scanbuse", "");
        		httpEstimaLinha(t,estima);
        		return;
        	}

        	//Converter endereco em coordenada
        	if(t.getRequestURI().toString().startsWith("/scanbusgp")){
        		String linhaquery = t.getRequestURI().toString().replaceFirst("/scanbusgp", "");
        		httpProcuraCoordFromAddr(t,linhaquery);
        		return;
        	}
        	

        	//Procura Linha De onibus
        	if(t.getRequestURI().toString().startsWith("/scanbusbp")){
        		String linhaquery = t.getRequestURI().toString().replaceFirst("/scanbusbp", "");
        		String dec = java.net.URLDecoder.decode(linhaquery, charencod);
        		
        		String lat = dec.split("&")[0];
        		String lon = dec.split("&")[1];
        		
        		String latd = "";
            	try{latd=(dec.split("&")[2]).trim();}catch (Throwable e){}
        		String lond = "";
            	try{lond=(dec.split("&")[3]).trim();}catch (Throwable e){}
        		
        		String response =             		      
                    			
               	    	"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">" + "\n" +
               	    	"<html>" + "\n" +
               	    	"<head>" + "\n" +
            			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset="+charencod+"\" />" + "\n" +
                    	"<form method=\"get\" action=\"scanbusp\">" + "\n" +
                    	"<label for=\"textarea\">Procure a linha do onibus que deseja :</label>" + "\n" +
            			"<br>" + "\n" +
                    	"<input name=\"strproc\" type=\"text\" size=\"25\" value=\"\">" + "\n" +
                    	//"<textarea name=\"strproc\" cols=\"25\" rows=\"1\">" + "\n" +
                    	//"\n" +
                    	//"</textarea><br>" + "\n" +
                    	"<input type=\"hidden\" name=\"lat\" value=\""+lat+"\">" + "\n" +
                    	"<input type=\"hidden\" name=\"lon\" value=\""+lon+"\">" + "\n" +
                    	"<input type=\"hidden\" name=\"latd\" value=\""+latd+"\">" + "\n" +
                    	"<input type=\"hidden\" name=\"lond\" value=\""+lond+"\">" + "\n" +
                    	"<input type=\"submit\" value=\"Procurar\" />" + "\n" +
                    	"</form>"+ "\n" +
                    	"</body>" + "\n" +
                    	"</html>" + "\n" ;      
            	
            	sendResponse(t,response);
                    	
        		return;
        	}

        	
        	
        	String response = 
      

        			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">" + "\n" +
        			"<html>" + "\n" +
        			"<head>" + "\n" +
        			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset="+charencod+"\" />" + "\n" +
        			"<form method=\"get\" action=\"scanbusgp\">" + "\n" +
        			"<label for=\"textarea\">Entre com o logradouro e numero de origem</label>" + "\n" +
        			"<br>" + "\n" +
        			"<input name=\"in_logra\" type=\"text\" size=\"30\" value=\"\">" + "\n" +
        			"," + "\n" +
        			"<input name=\"in_numero\" type=\"text\" size=\"4\" value=\"\">" + "\n" +
        			"<br>" + "\n" +
        			
        			"<label for=\"textarea\">Entre com o logradouro e numero de destino (opcional) </label>" + "\n" +
        			"<br>" + "\n" +
        			"<input name=\"in_lograd\" type=\"text\" size=\"30\" value=\"\">" + "\n" +
        			"," + "\n" +
        			"<input name=\"in_numerod\" type=\"text\" size=\"4\" value=\"\">" + "\n" +
        			
        			"<br>" + "\n" +        			
        			"<input type=\"submit\" value=\"Procurar\" />" + "\n" +
        			"</form>" + "\n" +
        			"</body>" + "\n" +
        			"</html>";
        			
        			
        			
        			
        	sendResponse(t,response);

        }
    }
    
    static void sendResponse(HttpExchange t, String response) throws IOException{
    	t.sendResponseHeaders(200,0); //response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.flush();        
            
        os.close();
    }
    
    static void printHeaders(Headers header){
    	Collection<List<String>> col = header.values();
    	
    	Iterator it = col.iterator();
    	while(it.hasNext()){
    		List lx = (List)it.next();
    		
    		Iterator itlx = lx.iterator();
    		while(itlx.hasNext()){
    			System.out.println(itlx.next());
    		}
    	}
    	
    }

    
    static void httpProcuraCoordFromAddr(HttpExchange t, String linhaquery) throws IOException {
    	
    	String dec = java.net.URLDecoder.decode(linhaquery, charencod);
    	
    	boolean isDest=false;
    	try{isDest = ((dec.split("&")[0]).split("=")[0]).trim().equals("?lat"); }catch (Throwable e){}
    	
    	String logra = "";
    	try{logra = ((dec.split("&")[0]).split("=")[1]).trim(); }catch (Throwable e){}
    	String numero = "";
    	try{numero=(((dec.split("&")[1])).split("=")[1]).trim();}catch (Throwable e){}
    
    	String lograd = "";
    	try{lograd = ((dec.split("&")[2]).split("=")[1]).trim(); }catch (Throwable e){}
    	String numerod = "";
    	try{numerod=(((dec.split("&")[3])).split("=")[1]).trim();}catch (Throwable e){}
    	
    	boolean temDest=false;
    	if(!(lograd+numerod).trim().equals(""))
    		temDest=true;
    	
    	String lato="";
    	String lono="";
	
    	if(isDest){
    		
        	lato=logra;
        	lono=numero;
        	logra=lograd;
        	numero=numerod;	
    	}
    	
    	Vector<GeoResult> ret;
		
    	ret = BusServer.getCoordFromAddr(logra, numero);
			

    	String info="Selecione o local de origem";
    	if(isDest)
    		info="Selecione o local de destino";
    	
    	String response = "<head> \n <hr size=1> \n";
    	response = response +  "<meta http-equiv=\"Content-Type\" content=\"text/html; charset="+charencod+"\" />" + "\n";
    	response = response + info+" :\n <hr size=1> \n";
    	
    	for(int i=0; i< ret.size(); i++){
    		
    		String endereco= ret.get(i).endereco;
    		String lat= ret.get(i).lat;
    		String lon= ret.get(i).lon;
    		
    		String strtoenc="";
    		String proxpag="";
    		
    		if(isDest){//E destino
    			proxpag="scanbusbp";
    			strtoenc=lato+ "&" + lono + "&"  + lat+ "&" + lon;
    			
    		}else{//Não é destino, é Origem
    		
	    		if(temDest){//tem destino
	    			proxpag="scanbusgp";
	    			strtoenc="?lat="+lat+"&lon="+lon+"&in_lograd="+lograd+"&in_numerod=" + numerod;
	    		}else{//não tem destino
	    			proxpag="scanbusbp";
	    			strtoenc=lat+ "&" + lon;
	    		}
    		}	
	    		
    		String codquery=java.net.URLEncoder.encode(strtoenc, charencod) ;
    		
    		response = response + 
    				"<hr size=1>\n" +
    				"<a href=\""+proxpag + codquery + "\">" + endereco+ "</a>\n <hr size=1> \n";
    		    		
    		
    	}
    	
    	response = response + "</head>";
    	
    	
    	sendResponse(t,response);
    	
    }
    
    
    static void httpProcuraLinha(HttpExchange t, String linhaquery) throws IOException {
    	
    	String dec = java.net.URLDecoder.decode(linhaquery, charencod);
    	
    	String linha = ((dec.split("&")[0]).split("=")[1]).trim();
    	String lat = ((dec.split("&")[1]).split("=")[1]).trim();
    	String lon = ((dec.split("&")[2]).split("=")[1]).trim();
    	String latd = "";
    	try{latd=((dec.split("&")[3]).split("=")[1]).trim();}catch (Throwable e){}
    	String lond = "";
    	try{lond=((dec.split("&")[4]).split("=")[1]).trim();}catch (Throwable e){}    			

    	
    	
    	
    	Hashtable<String, String> ret;
		try {
			ret = BusServer.procuraLinha(linha);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ret = new Hashtable<String, String>(0);
		}
    	
    	Enumeration<String> enk = ret.keys();
    	String response = "<head> \n <hr size=1> \n";
    	response = response +  "<meta http-equiv=\"Content-Type\" content=\"text/html; charset="+charencod+"\" />" + "\n";
    	response = response + "Selecione a linha desejada : \n <hr size=1> \n";
    	
    	while(enk.hasMoreElements()){
    		
    		String key= enk.nextElement();
    		String codlinha = ret.get(key);
    		
    		String codquery=java.net.URLEncoder.encode(codlinha+ "&" + key + "&" +lat +"&" + lon + "&" +latd +"&" + lond   , charencod) ;
    		
    		response = response + 
    				"<hr size=1>\n" +
    				"<a href=\"scanbuse" + codquery + "\">" + key+ "</a>\n <hr size=1> \n";
    		    		
    		
    	}
    	
    	response = response + "</head>";
    	
    	
    	sendResponse(t,response);
    	
    }
    

    
    static void httpEstimaLinha(HttpExchange t, String estima) throws IOException{
    
    	String tmp[] = java.net.URLDecoder.decode(estima, charencod).split("&");
    	String codlinha=tmp[0];
    	String descrl = tmp[1];
    	double lat=Double.parseDouble(tmp[2]);
    	double lon=Double.parseDouble(tmp[3]);

    	double lat_d=0;
    	double lon_d=0;
    	
    	try{
        	 lat_d=Double.parseDouble(tmp[4]);
        	 lon_d=Double.parseDouble(tmp[5]);
    	}catch(Throwable e){
        	lat_d=0;
        	lon_d=0;
    	}	
    	
    	BusResult br = BusServer.estimaTempoChegadaBR(codlinha, lat, lon, lat_d, lon_d);

    	String infog = "Monitorando ";
		String infobus = "";
    	
		
		if(br==null){
			infog="Linha Nao Monitorada \n";
			infobus = "Linha Nao Monitorada <hr color=blue size=5 >\n";
		}
		else{
		
			infobus="Distancia a linha = " + br.getDistOrigem() + "m <hr>\n";
		    	
		    	if(br.getDistDestino() >0 && br.getTviagem() > 0 && br.getDviagem() > 0 ){
		    		infobus=infobus + "Tempo viagem = " + LinhaSMonitor.getTime(br.getTviagem()) + "<hr>\n";
		    		infobus=infobus + "Distancia viagem = " + br.getDviagem() + " m <hr>\n";		
		    		infobus=infobus + "Distancia a linha em destino = " + br.getDistDestino() + " m <hr>\n";
		    	}    	
		    infobus = infobus + "<hr color=blue size=5 >\n";	
		    	
		    	
		    	if(br.getVbus().size()==0){
		    		infobus=infobus + "Aguardando saida do terminal <hr color=blue size=5 >\n";
		    	}else{
		
			    	for(int i=0; i< br.getVbus().size(); i++){
			    		Bus bus = br.getVbus().get(i);
			    		
			    		
			    		infobus = infobus + "<hr size=5>" + "\n" + "Onibus : "+ bus.getBusId() + " - Estimativa : " + LinhaSMonitor.getTime(bus.getEstim()) +  "  A=" + bus.isAcess() + " (" + bus.getdistU() + " m)";
			    		
			    	}
		    	}
    	
		}
    	
    	String response = 

    	    	"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">" + "\n" +
    	    	    	"<html>" + "\n" +
    	    	    	"<head>" + "\n" +
    	    			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset="+charencod+"\" />" + "\n" +
    	    	    	"<meta http-equiv=\"refresh\" content=\"5;URL=scanbuse"+ estima +"\">" + "\n" +
    	    	    	"<title>Monitor Onibus</title>" + "\n" +
    	    	    	//"<meta http-equiv=\"author\" content=\"Willy the Wonca\";charset="+charencod+"\">" + "\n" +
    	    	    	"</head>" + "\n" +
    	    	    	"<body text=#0000FF bgcolor=white link=#0000FF vlink=#A020F0>" + "\n" +
    	    	    	"<font face=\"Comic Sans MS,Verdana,Arial\">" + "\n" +
    	    	    	"<font size=5><b>Scan Bus</b></font>" + "\n" +
    	    	    	infog + "\n" +
    	    	    	"Onibus : " + descrl+ "\n" +
    	    	    	"<hr color=blue size=5 >" + "\n" +
    	    	    	infobus;
    	    	    	

    			
    			
    	/*
    	
    	String estim = BusServer.estimaTempoChegadaS(codlinha, lat, lon);
    	
		 String response = 
    		      
    	"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">" + "\n" +
    	"<html>" + "\n" +
    	"<head>" + "\n" +
		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset="+charencod+"\" />" + "\n" +
    	"<meta http-equiv=\"refresh\" content=\"5;URL=scanbuse"+ estima +"\">" + "\n" +
    	"<title>Monitor Onibus</title>" + "\n" +
    	//"<meta http-equiv=\"author\" content=\"Willy the Wonca\";charset="+charencod+"\">" + "\n" +
    	"</head>" + "\n" +
    	"<body text=#0000FF bgcolor=white link=#0000FF vlink=#A020F0>" + "\n" +
    	"<font face=\"Comic Sans MS,Verdana,Arial\">" + "\n" +
    	"<font size=5><b>Scan Bus</b></font>" + "\n" +
    	"Monitor Onibus : " + descrl+ "\n" +
    	"<hr color=blue size=5 >" + "\n";
    	
    	String tmpE[] = estim.split("\n");
    	for(int i=0; i< tmpE.length; i++)
    		response = response + "<hr size=5>" + "\n" + tmpE[i];
    	
    	//"Tempo estimado :" + estim + "\n" +
    	*/
    	
    	
    	response = response +
    	"<hr size=5>" + "\n" +
    	"</font>" + "\n" +
    	"</body>" + "\n" +
    	"</html>" + "\n" ;
    	
    	
    	sendResponse(t,response);
    	
    	
    }
    
    


}
