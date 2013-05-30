package br.com.sptrans.bus;

import java.util.Vector;

public class BusResult {
	
	
	private Vector<Bus> vbus; 	//Vetor com onibus uteis na linha para o usuario.
	private int distOrigem;		//Distancia do ponto de Origem  a linha em metros.
	private	int distDestino;	//Distancia do ponto de Destino a linha em metros.
	private int tviagem; 		//Tempo estimado de viagem em segundos do ponto de origem ao ponto de destino.
	private int dviagem;		//Distancia do ponto da linha de Origem ao Ponto da linha de destino em metros.
	
	
	
	
	public BusResult(int distOrigem, int distDestino, int tviagem, int dviagem) {
		super();
		
		vbus = new Vector<Bus>(0);
		
		this.distOrigem = distOrigem;
		this.distDestino = distDestino;
		this.tviagem = tviagem;
		this.dviagem = dviagem;
	}


	
	final public Vector<Bus> getVbus() {
		return vbus;
	}

	public int getDistOrigem() {
		return distOrigem;
	}

	public int getDistDestino() {
		return distDestino;
	}


	public int getTviagem() {
		return tviagem;
	}

	public int getDviagem() {
		return dviagem;
	}


	public class Bus{

		private	String busId;	// ID do onibus.
		private boolean acess;	// Acessibilidade para deficiente. 
		private int estim;		// Tempo estimado para chegada do onibus em segundos.
		private int distU;		// Distancia do onibus ao ponto da linha mais proxima do usuário em metros.
		
		
		
		public Bus(String busId, boolean acess, int estim, int distU) {
			super();
			this.busId = busId;
			this.acess = acess;
			this.estim = estim;
			this.distU = distU;
			
			vbus.add(this);
		}

		public String getBusId() {
			return busId;
		}

		public boolean isAcess() {
			return acess;
		}

		public int getEstim() {
			return estim;
		}

		public int getdistU() {
			return distU;
		}
		
		
	}

}
