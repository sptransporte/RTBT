package br.com.sptrans.nt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GeoAddr {

	/**
	 * @param args
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	

	
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

        GeoAddr tst = new GeoAddr(false,null,0);


        
        String addr = "Rua merda , São Paulo - SP";//"R. Augosta,12 - Rep�blica, Sao Paulo - Sao Paulo";//"CEP 02222-999";//
        
        Vector<GeoResult> vres =  tst.getCordinates(addr,"Brasil",System.getProperty("file.encoding","UTF-8"));
        
        System.out.println("size= " + vres.size());
        
        for(int i=0; i< vres.size() ; i++){
        	GeoResult geores = vres.elementAt(i);
        	System.out.println(i+ "|" + geores);
        }
        
		
		
	}
	
	public GeoAddr(boolean proxy, String host, int port) {

		if(proxy){
			String proxyHost =host;  
	        String proxyPort = port+"";  
	        System.setProperty("http.proxyHost", proxyHost);  
	        System.setProperty("http.proxyPort", proxyPort);  
		}
	}

	
	public Vector<GeoResult> getCordinates(String address,String county,String charencod) throws IOException, ParserConfigurationException, SAXException{
		//address = StringUtils.replaceSpecial(address);
		//county = StringUtils.replaceSpecial(county);
		

		
		String thisLine;

	    address = address.replace(",", "+");
	    address = address.replace(" ", "+");
	    county = county.replace(" ", "");

	    String fullAddress = address+"+"+county;
	    fullAddress = java.net.URLEncoder.encode(fullAddress,"UTF-8");
	    
	    //System.out.println(fullAddress);

	    URL url = new URL("http://maps.google.com.br/maps/geo?hl=pt-BR&tab=wl&q="+fullAddress+"&output=xml&key=ABQIAAAANGTAqDyDam_07aWkklK2NBSD41w" +
	            "X8VhCBpuiDVjGbFNuXE31lhQB8Gkwy-wmYbmaHIbJtfnlR9I_9A");
	    
	    
	    BufferedReader theHTML = new BufferedReader(new InputStreamReader(url.openStream()));

	    FileWriter fstream = new FileWriter("url.xml");
	    BufferedWriter out = new BufferedWriter(fstream);
	    while ((thisLine = theHTML.readLine()) != null){
	    	thisLine= new String(thisLine.getBytes(charencod));//("iso-8859-1"));
	    	thisLine.replaceAll("encoding=\"UTF-8\"","encoding=\""+charencod+"\"");//thisLine.replaceAll("encoding=\"UTF-8\"","encoding=\"iso-8859-1\"");
	    	
	    	//System.out.println(thisLine);
	        out.write(thisLine);
	    }
	    out.close();

	    File file = new File("url.xml");
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document doc = db.parse(file);
	    doc.getDocumentElement().normalize();
	    NodeList nl = doc.getElementsByTagName("code");
	    
	    
	    Element n = (Element)nl.item(0);
	    String st = n.getFirstChild().getNodeValue();

	    int nr = doc.getElementsByTagName("Placemark").getLength();
	    Vector<GeoResult> result=new Vector<GeoResult>(nr);
	    
	    if (st.equals("200"))
	    {


	    	
	    	for(int i=0;i<nr;i++){
	    		
		    	NodeList nx = doc.getElementsByTagName("address");
		    	Element nxx = (Element)nx.item(i);
		    	String stx = nxx.getFirstChild().getNodeValue();
	    		
		        NodeList n2 = doc.getElementsByTagName("coordinates");
		        Element nn = (Element)n2.item(i);
		        String st1 = nn.getFirstChild().getNodeValue();
		    
				GeoResult resulti=new GeoResult();
		        String[] tmp= st1.split(",");
		        resulti.lat=tmp[1];
		        resulti.lon=tmp[0];
		        resulti.endereco=stx;

		        result.add(resulti);
	    		
	    	}
	    	
	        
	        return result;
	    }
	    else
	    {
	        return new Vector<GeoResult>(0);
	    }


	}

		
	public class GeoResult {
		public String lat="";
		public String lon="";
		public String endereco="";
		@Override
		public String toString() {
			return  lat + "," + lon + "|" + endereco;
		}
		
		
	}
	

	
}

