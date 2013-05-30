package br.com.sptrans.nt;

public class LinhaInfo {

	String linha;
	
	boolean sent1ini=false;
	boolean sent2ini=false;
	boolean multini1=false;
	boolean multini2=false;
	

	boolean Circular_1;
	String CodigoLinha_1;
	String DenominacaoTPTS_1;
	String DenominacaoTSTP_1;
	String Informacoes_1;
	String Letreiro_1;
	String Tipo_1;


	

	boolean Circular_2;
	String CodigoLinha_2;
	String DenominacaoTPTS_2;
	String DenominacaoTSTP_2;
	String Informacoes_2;
	String Letreiro_2;
	String Tipo_2;
	

	String idRotaLinha="null";
	
	String coord_1_0="null";
	String coord_1_1="null";
	String coord_1_2="null";
	String coord_2_0="null";
	String coord_2_1="null";
	String coord_2_2="null";
	
	
	
	public LinhaInfo(String linha) {
		this.linha = new String(linha);
	}
	
	public String getCoordRota(int sentido,int dia){
		
		if(sentido != 1 && sentido != 2 && dia != 0 && dia!=1 && dia!=2)
			return null;
		
		switch(10*sentido + dia ){

		case 12:
			if(coord_1_2!=null && !coord_1_2.equals("")&& !coord_1_2.equals("null"))
				return new String(coord_1_2);
			
		case 11:
			if(coord_1_1!=null && !coord_1_1.equals("")&& !coord_1_1.equals("null"))
				return new String(coord_1_1);
			
		case 10:
			return new String(coord_1_0);

		case 22:
			if(coord_2_2!=null && !coord_2_2.equals("")&& !coord_2_2.equals("null"))
				return new String(coord_2_2);
		
		case 21:
			if(coord_2_1!=null && !coord_2_1.equals("")&& !coord_2_1.equals("null"))
				return new String(coord_2_1);
			
		case 20:			
			return new String(coord_2_0);

		}
		
		
		return null;
	}
	
	public String getLinha(){
		return new String(linha);
	}
	
	public String getLetreiro(){
		return new String(Letreiro_1);
	}
	
	public void setIdRotaLinha(String idrota){
		idRotaLinha = new String(idrota);
	}
	public String getIdRotaLinha(){
		return new String(idRotaLinha);
	}
	
	public String getCodigoLinha(int sentido){
		
		if(sentido==1)
			return new String(CodigoLinha_1);
		
		if(sentido==2)
			return new String(CodigoLinha_2);
		
		return null;
		
	}
	
	public void setup1(String Circular,String CodigoLinha,String DenominacaoTPTS,String DenominacaoTSTP,String Informacoes,String Letreiro,String Tipo){
		
	
		Circular_1=Boolean.parseBoolean(Circular);
		CodigoLinha_1=new String(CodigoLinha);
		DenominacaoTPTS_1=new String(DenominacaoTPTS);
		DenominacaoTSTP_1=new String(DenominacaoTSTP);
		Informacoes_1=new String(Informacoes);
		Letreiro_1=new String(Letreiro);
		Tipo_1=new String(Tipo);

		if(sent1ini)
			multini1=true;
			sent1ini=true;

		
	}
	
	public void setup2(String Circular,String CodigoLinha,String DenominacaoTPTS,String DenominacaoTSTP,String Informacoes,String Letreiro,String Tipo){
		

		Circular_2=Boolean.parseBoolean(Circular);
		CodigoLinha_2=new String(CodigoLinha);
		DenominacaoTPTS_2=new String(DenominacaoTPTS);
		DenominacaoTSTP_2=new String(DenominacaoTSTP);
		Informacoes_2=new String(Informacoes);
		Letreiro_2=new String(Letreiro);
		Tipo_2=new String(Tipo);

		if(sent2ini)
			multini2=true;
			sent2ini=true;

		
		
	}

	public void setupCoord(int sentido,int dia, String coord){
		
		if(sentido==1 && dia == 0)
			coord_1_0=new String(coord);
		if(sentido==1 && dia == 1)
			coord_1_1=new String(coord);
		if(sentido==1 && dia == 2)
			coord_1_2=new String(coord);
		if(sentido==2 && dia == 0)
			coord_2_0=new String(coord);
		if(sentido==2 && dia == 1)
			coord_2_1=new String(coord);
		if(sentido==2 && dia == 2)
			coord_2_2=new String(coord);
		
		
		
	}
	
	public String toString() {

		return ""+
		linha +"#"+	
				
		"1" +"#"+
		sent1ini +"#"+
		multini1 +"#"+
		Circular_1 +"#"+
		CodigoLinha_1 +"#"+
		DenominacaoTPTS_1 +"#"+
		DenominacaoTSTP_1 +"#"+
		Informacoes_1 +"#"+
		Letreiro_1 +"#"+
		Tipo_1 +"#"+

		"2" +"#"+
		sent2ini +"#"+
		multini2 +"#"+		
		Circular_2 +"#"+
		CodigoLinha_2 +"#"+
		DenominacaoTPTS_2 +"#"+
		DenominacaoTSTP_2 +"#"+
		Informacoes_2 +"#"+
		Letreiro_2 +"#"+
		Tipo_2 +"#" +
		
		idRotaLinha +"#" +		
			
		coord_1_0 +"#" +	
		coord_1_1 +"#" +	
		coord_1_2 +"#" +	
		coord_2_0 +"#" +	
		coord_2_1 +"#" +	
		coord_2_2 +"#" +	
		
		
		"";
		
	}
	
	/*Realiza o parse de uma linha do BD que correspone a um LinhaInfo
	 * */	  
	static public  LinhaInfo getLineInfoFromLine(String line){

			  LinhaInfo ret;	
			  
			  
			  String[] lvals = line.split("#");
			  int i=0;
			  ret = new LinhaInfo(lvals[i++]);
			  
			  int sent1=Integer.parseInt(lvals[i++]);
			  boolean sent1ini=Boolean.parseBoolean(lvals[i++]);
			  boolean multini1=Boolean.parseBoolean(lvals[i++]);
			  String Circular_1=lvals[i++];
			  String CodigoLinha_1=lvals[i++];
			  String DenominacaoTPTS_1=lvals[i++];
			  String DenominacaoTSTP_1=lvals[i++];
			  String Informacoes_1=lvals[i++];
			  String Letreiro_1=lvals[i++];
			  String Tipo_1=lvals[i++];
			  ret.setup1(Circular_1, CodigoLinha_1, DenominacaoTPTS_1, DenominacaoTSTP_1, Informacoes_1, Letreiro_1, Tipo_1);
			  ret.sent1ini=sent1ini;
			  ret.multini1= multini1;	  
			  
			  int sent2=Integer.parseInt(lvals[i++]);
			  boolean sent2ini=Boolean.parseBoolean(lvals[i++]);
			  boolean multini2=Boolean.parseBoolean(lvals[i++]);
			  String Circular_2=lvals[i++];
			  String CodigoLinha_2=lvals[i++];
			  String DenominacaoTPTS_2=lvals[i++];
			  String DenominacaoTSTP_2=lvals[i++];
			  String Informacoes_2=lvals[i++];
			  String Letreiro_2=lvals[i++];
			  String Tipo_2=lvals[i++];
			  ret.setup2(Circular_2, CodigoLinha_2, DenominacaoTPTS_2, DenominacaoTSTP_2, Informacoes_2, Letreiro_2, Tipo_2);
			  ret.sent2ini=sent2ini;
			  ret.multini2= multini2;
			  
			  ret.setIdRotaLinha(lvals[i++]);
			  
			  ret.setupCoord(1, 0, lvals[i++]);
			  ret.setupCoord(1, 1, lvals[i++]);
			  ret.setupCoord(1, 2, lvals[i++]);
			  ret.setupCoord(2, 0, lvals[i++]);
			  ret.setupCoord(2, 1, lvals[i++]);
			  ret.setupCoord(2, 2, lvals[i++]);		  
			  
			  return ret;
		  }
	
	
	public String getLetreiroSentido(int sentido){
		
		if(sentido==1){//Origem --> Destino
			return new String(Letreiro_1 + " (" + DenominacaoTSTP_1 + " -> " + DenominacaoTPTS_1 + ")");
		}else{	
			return new String(Letreiro_1 + " (" + DenominacaoTPTS_1 + " -> " + DenominacaoTSTP_1 + ")");
		}

		
	}
	
	
}
