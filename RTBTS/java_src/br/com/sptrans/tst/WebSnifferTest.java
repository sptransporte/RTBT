package br.com.sptrans.tst;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class WebSnifferTest {
	static String LE="\r\n";
	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		// TODO Auto-generated method stub
		
	  	System.out.println("Default Charset=" + java.nio.charset.Charset.defaultCharset());
	  	//System.setProperty("file.encoding", "Latin-1");
	  	System.out.println("file.encoding=" + System.getProperty("file.encoding"));
	  	System.out.println("Default Charset=" + java.nio.charset.Charset.defaultCharset());
	  	System.out.println("Default Charset in Use=" + (new OutputStreamWriter(new ByteArrayOutputStream())).getEncoding());
		
		  
		
	  	String resp;
	  	String[] tst;
		
		String host,get,cookie;
		int port;

		//host="200.189.189.54";
		//port=80;
		//get="/InternetServices/PosicaoLinha?cb=jQuery&codigoLinha=418";
		
		
		//host="localhost";
		//port=8080;
		//get="/test-app/hello";
		

		

		host="200.99.150.170";
		port=80;
		get="/PlanOperWeb/ABInfGrConGWEB.asp?MODULO=WEB";
		
		
		resp = httpGET(host,port,get,null);
		tst = resp.split("Set-Cookie: ");
		tst = tst[1].split("; path=/");
		cookie=tst[0];
		
		

		
		
		
		
		
		
		
		
		host="200.99.150.170";
		port=80;
		get="/PlanOperWeb/ABInfSvGoogleM.asp?";
		//get= get + "MODULO=WEB&";
		//get= get + "CdPjoDataVig=03/29/2013&";
		//get= get + "Tipo=Mapa&" ;
		//get= get + "OPER=1&" ;
		//get= get + "TIPO_CONS=1&" ;
		//get= get + "SEXO_USUAR=undefined&";
		//get= get + "SvLinCodigo=477A&" ;
		get= get + "TpDiaID=0&" ; //OBRIGATORIO 0 ou 1 ou 2   0-Util 1-Sábado 2-Dom/Feriados
		//get= get + "TpLinID=10&" ;
		get= get + "CdPjOID=77672&" ; //OBRIGATORIO
		//get= get + "Editar=&" ;
		get= get + "DfSenID=1"; // 1 ou 2 Default 1		
		
		
		resp = httpGET(host,port,get,cookie);
		
		
		
		//System.out.println(resp);
		
		tst = resp.split("var coor");
		tst = tst[1].split("var distancias");
		System.out.println(tst[0]);


	}
	
	
	
	static String httpGET(String host,int port, String get, String cookie) throws Throwable{
		
		Socket srvSoc = null;
		

		srvSoc = new Socket(host,port);
		
		
		srvSoc.setSoTimeout(10000);
		
		OutputStream writer = srvSoc.getOutputStream();
		InputStream reader = srvSoc.getInputStream();

		
		String HTTPReq;		

		HTTPReq = "GET "+ get + " HTTP/1.1" + LE;
		HTTPReq =HTTPReq+ "Host: " + host + LE;
		HTTPReq =HTTPReq+ "Connection: close" + LE;
		if(cookie!=null)
			HTTPReq =HTTPReq+ "Cookie: "+ cookie+ LE;
		
		
		HTTPReq =HTTPReq + LE+LE;		
		
		writer.write(HTTPReq.getBytes(), 0, HTTPReq.getBytes().length);
		writer.flush();
		
		/////////////////////
		String HTTPResp = "";
	    byte[] buffer = new byte[1024];
		int read = 0;
		
		while ( (read = reader.read(buffer)) >= 0)
		{
			HTTPResp = HTTPResp + new String(buffer,0,read);
			
		}
		
		srvSoc.close();
		return HTTPResp;
	}
		


	
}