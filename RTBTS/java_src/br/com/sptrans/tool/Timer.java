package br.com.sptrans.tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import br.com.sptrans.bus.BusServer;

public class Timer extends Thread {

	
	
	public Timer() {

	}

	@Override
	public void run() {


		
		try {
			System.out.println("StartUp : " + getTime());
			BusServer.startupServer(getDIA());

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		boolean limbo = true;

		
	
		while(true){
			try {Thread.sleep(20000);} catch (InterruptedException e) {e.printStackTrace();System.exit(0);	}
			
			if(!limbo && getHora()==3){
				
				System.out.println("ShutDown : " + getTime());
				BusServer.shutDownServer();
				
				try {
					System.out.println("StartUp : " + getTime());
					BusServer.startupServer(getDIA());
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.exit(0);
				}
				limbo=true;
			}else if(getHora()>=4){
				limbo=false;
				
			}
			
		}
		
		
		
	
	
	
	}
	
	
	
	static String[][] FERIADOS = {
			{"01",	"01",	"Confraternizacao Universal (feriado nacional)"},
			{"25",	"01",	"Aniversario da cidade de Sao Paulo(feriado municipal)"},
			{"21",	"04",	"Tiradentes (feriado nacional)"},
			{"01",	"05",	"Dia Mundial do Trabalho (feriado nacional)"},
			{"09",	"07",	"Revolucao Constitucionalista de 1932(feriado estadual)"},
			{"07",	"09",	"Independencia do Brasil (feriado nacional)"},
			{"12",	"10",	"Nossa Senhora Aparecida (feriado nacional)"},
			{"02",	"11",	"Finados (feriado nacional)"},
			{"15",	"11",	"Proclamacao da Republica (feriado nacional)"},
			{"20",	"11",	"Dia da Consciencia Negra(feriado municipal)"},
			{"25",	"12",	"Natal  (feriado nacional)"}
		};
	
	
	public static int getHora(){
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		String hora = sdf.format(calendar.getTime());
		return Integer.parseInt(hora);
	
	}
	
	public static int getDIA(){
		
	
		Calendar calendar = Calendar.getInstance();
		
		
		int day     = calendar.get(Calendar.DAY_OF_MONTH);
		int month   = calendar.get(Calendar.MONTH) + 1;
		int weekday = calendar.get(Calendar.DAY_OF_WEEK); 
        
		boolean isFeriado = false;
		for(int i=0; i < FERIADOS.length; i++){
			int fday  = Integer.parseInt(FERIADOS[i][0]);
			int fmonth= Integer.parseInt(FERIADOS[i][1]);
			if(fday == day && fmonth==month){
				isFeriado = true;
				System.out.println("FERIADO : " + FERIADOS[i][2]);
				break;
			}			
		}
		
		if(isFeriado)
			return 2;
		
		if(weekday == Calendar.SUNDAY)
			return 2;
		
		if(weekday == Calendar.SATURDAY)
			return 1;
		

		return 0;
	}

	public static String getTime(){
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
	    return sdf.format(cal.getTime());
	}
	public static String getDateZipName(){
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
	    return sdf.format(cal.getTime());
	}
	
}
