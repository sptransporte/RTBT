package br.com.sptrans.rtbt;

public interface IAppCenter {
	public void shutdown();
	public void logError(String error);	
	public void log(String trace);
}
