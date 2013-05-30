package br.com.sptrans.rtbt;

public class ShutdownInterceptor extends Thread{
	
	IAppCenter icallback;
	
	public ShutdownInterceptor(IAppCenter icallback) {
		super();
		this.icallback = icallback;
	}

	public void run() {
		icallback.shutdown();		
		}

}
