package br.com.sptrans.rtbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import br.com.sptrans.rtbt.auxl.Timer;



public class LoggerThread extends Thread {
	  //private static final LoggerThread instance = new LoggerThread();
	  private static LoggerThread instance = null;
	  private FileOutputStream logwrite = null;
	  private static IAppCenter appCenter = null;
	  
	  private String logfile;
	  
	  public static String NEW_LINE="\n";

	  public static LoggerThread getLogger(String logFile,IAppCenter appc) throws Throwable  {
		if(instance == null){
			instance = new LoggerThread(logFile);
			appCenter = appc;
		}	  
		  
	    return instance;
	  }
	  private LoggerThread(String logFile) throws Throwable {
		  	this.logfile=logFile;
			this.logwrite = new FileOutputStream(logFile,true);
			instance =this;
			start();
		
	  }
	  
	  private LinkedBlockingQueue<String> itemsToLog = new LinkedBlockingQueue<String>();

	  public static final String SHUTDOWN_REQ = "SHUTDOWN";
	  public static final String ZIPFILE_REQ = "ZIPTHISFILE";
	  
	  private volatile boolean shuttingDown, loggerTerminated;
	  
	  // Sit in a loop, pulling strings off the queue and logging
	  public void run() {
	    try {
	      String item;
	      while ((item = itemsToLog.take()) != SHUTDOWN_REQ) {
	    	  
	    	  if(item == ZIPFILE_REQ){
	    		  zipLogFile(); //Pedido para zipar arquivo.
	    	  }else{

	    		logwrite.write(item.getBytes(),0,item.getBytes().length);
				logwrite.write(NEW_LINE.getBytes(),0,NEW_LINE.getBytes().length);
				logwrite.flush();
	    	  }
	    	  
	      }
	    } catch (Throwable iex) {
	    	System.err.println("Erro ao escrever log " + iex);
	    } finally {
	    	
	      loggerTerminated = true;
	      try {	logwrite.close();} catch (IOException e) {e.printStackTrace();}
	      
	    }
	  }

	  
	  public void log(String str)  {
		    if (shuttingDown || loggerTerminated) return;
		    try {
		      itemsToLog.put(str);
		    } catch (Throwable iex) {
		      appCenter.logError("erro ao adicionar log na fila " + iex);	
		      Thread.currentThread().interrupt();
		      throw new RuntimeException("Unexpected interruption");
		    }
		  }
	  
	  public void shutDown() throws InterruptedException {
		  shuttingDown = true;
		  itemsToLog.put(SHUTDOWN_REQ);
		}
	  
	  private void zipLogFile(){
		  int BUFFER = 2048;
          String zipfilename =Timer.getDateZipName() + ".zip";
          String logfilename =Timer.getDateZipName() + ".txt";
          
          boolean sucesso = false;

	      if( new File(zipfilename).exists()){
	        	 appCenter.logError("ERRO arquivo de log zipado  " + zipfilename + " já existe." );
	        	 return;
	      }

          
		  try {
			  //Fechandp arquivo de log atual;
			  logwrite.close();
			  
			  File atuallogfile =  new File(this.logfile);
			  atuallogfile.renameTo(new File(logfilename));
			  
			  
			  
	          BufferedInputStream origin = null;
	          
	          
	          FileOutputStream dest = new 
	            FileOutputStream(zipfilename);
	          ZipOutputStream out = new ZipOutputStream(new 
	            BufferedOutputStream(dest));
	          //out.setMethod(ZipOutputStream.DEFLATED);
	          byte data[] = new byte[BUFFER];
	          // get a list of files from current directory

	          String filetozip= logfilename;//this.logfile;
	          
	          FileInputStream fi = new 
	          FileInputStream(filetozip);
	          origin = new 
	          BufferedInputStream(fi, BUFFER);
	          ZipEntry entry = new ZipEntry(filetozip);
	          out.putNextEntry(entry);
	          int count;
	            while((count = origin.read(data, 0, 
	               BUFFER)) != -1) {
	                out.write(data, 0, count);
	             }
	             origin.close();

	          out.close();
	          sucesso = ( new File(zipfilename).exists());
	       } catch(Throwable e) {
	          e.printStackTrace();
	          appCenter.logError("ERRO ao criar o arquivo de log zipado " + zipfilename + " " + e );
	          
	       }
		  

		  //Arquivo zipado criado com sucesso
		  //Deletar log atual
		  if(sucesso){
			  
			  (new File(logfilename)).delete();
			  
		  }		  
		  
		  //TEntado recriar o logwrite;
		  
		  try {
			this.logwrite = new FileOutputStream(this.logfile,true);
			} catch (Throwable  e) {
				// TODO Auto-generated catch block
				appCenter.logError("ERRO ao recriar o logwrite " + this.logfile + " apos zip : " + e );
				e.printStackTrace();			
				
			}
		  
	  }

	  
	}

