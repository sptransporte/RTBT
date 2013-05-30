package br.com.sptrans.tst;

public class LinhaResult {
	boolean Circular;
	String CodigoLinha;
	String DenominacaoTPTS;
	String DenominacaoTSTP;
	String Informacoes;
	String Letreiro;
	int Sentido;
	String Tipo;
	
	
	
	public LinhaResult(boolean circular) {
		super();
		Circular = circular;
	}
	public String getCodigoLinha() {
		return CodigoLinha;
	}
	public void setCodigoLinha(String codigoLinha) {
		CodigoLinha = codigoLinha;
	}
	public String getDenominacaoTPTS() {
		return DenominacaoTPTS;
	}
	public void setDenominacaoTPTS(String denominacaoTPTS) {
		DenominacaoTPTS = denominacaoTPTS;
	}
	public String getDenominacaoTSTP() {
		return DenominacaoTSTP;
	}
	public void setDenominacaoTSTP(String denominacaoTSTP) {
		DenominacaoTSTP = denominacaoTSTP;
	}
	public String getInformacoes() {
		return Informacoes;
	}
	public void setInformacoes(String informacoes) {
		Informacoes = informacoes;
	}
	public String getLetreiro() {
		return Letreiro;
	}
	public void setLetreiro(String letreiro) {
		Letreiro = letreiro;
	}
	public int getSentido() {
		return Sentido;
	}
	public void setSentido(int sentido) {
		Sentido = sentido;
	}
	public String getTipo() {
		return Tipo;
	}
	public void setTipo(String tipo) {
		Tipo = tipo;
	}

	public String getDestString(){
		
		String ret="";
		
		if(Sentido==1){
			ret=ret+DenominacaoTPTS;
		}else{
			ret=ret+DenominacaoTSTP;
		}
		
		return ret;
	}

	public String toString() {

		String ret=Letreiro + "-" + Tipo + " ->";
		

		ret=ret + getDestString();

			
		ret = ret + " CodigoLinha=" + CodigoLinha + " sentido=" + Sentido;
		
		return ret;
	}
	
	
	
}
