#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <math.h>
#include <errno.h>

#define MAX_SIZE_LINE 2000





 
char* trim(char *str){
		if(!str)
			return NULL;
		for(int i=0;i<strlen(str);i++)
			if(str[i]=='\n' || str[i]=='\r'  || str[i]=='\t')
				str[i]=' ';		
}


int getDstr(char *str,double *pret){
	 errno = 0;
	 *pret = strtold(str,NULL);
	 return errno;
 }

int getIstr(char *str,int *pret){
	 errno = 0;
	 *pret= strtol(str, NULL, 10);
	 return errno;
 }


/*
CRIA DATA A PARTIR DO FORMATO A-M-D H:M:S.m
*/ 
int createTime_strtok(char* strdata,time_t *created_time){

	if(!strdata)
		return -1;
	
	char* amd_str = strtok(strdata, " ");
	char* hms_str = strtok(NULL, " ");
	
	if(!amd_str || !hms_str)
		return -2;
	
	
	char* ano_str = strtok(amd_str, "-");
	char* mes_str = strtok(NULL, "-");
	char* dia_str = strtok(NULL, "-");
	
	if(!ano_str || !mes_str || !dia_str)
		return -3;
	
	char* hora_str 	 = strtok(hms_str, ":");
	char* minuto_str = strtok(NULL, ":");
	char* segundo_str= strtok(NULL, ":");		

	if(!hora_str || !minuto_str || !segundo_str)
		return -4;
	
	int year,mon,day,hour,min,sec;
	double dsec;
	if(getIstr(ano_str,&year)!=0)
		return -5;
	if(getIstr(mes_str,&mon)!=0)
		return -6;
	if(getIstr(dia_str,&day)!=0)
		return -7;
	if(getIstr(hora_str,&hour)!=0)
		return -8;
	if(getIstr(minuto_str,&min)!=0)
		return -9;
	
	if(getDstr(segundo_str,&dsec)!=0)
		return -10;
	sec = round(dsec);

	struct tm mytime;

	mytime.tm_year	= year - 1900 ;
	mytime.tm_mon	= mon -1;		
	mytime.tm_mday	= day;

	mytime.tm_hour	= hour;
	mytime.tm_min	= min;
	mytime.tm_sec	= sec;


	mytime.tm_wday=0;//IGNORADO
	mytime.tm_yday=0;//IGNORADO
	mytime.tm_isdst=-1;//SEM DST

	*created_time = mktime(&mytime);
	
	if(*created_time == -1)
		return -11;
		
	
	return 0;
	
}

time_t getCurrTime(){
	time_t current_time;
	current_time = time(NULL);
	
	if (current_time == ((time_t)-1)){
       printf("ERRO Ao pegar curr time\n");
       exit(1);
    }
	
}

time_t	mindate; 
time_t	maxdate; 
time_t	lastdate; 

int main(){
	printf("INICIO\n");
	
	char txt[] = "2013-03-01 00:00:00";
	int ret;
	if((ret = createTime_strtok(txt,&mindate))){
		printf("Create time mindate Error  %d\n",ret);
		exit(1);
	}
	maxdate	= getCurrTime();
	lastdate= mindate;
	
	FILE *fp= fopen("./avl15_21.txt","r");
	
	if(!fp){
		printf("ARQUIVO NULO\n");
		exit(0);	
	}
	
	char *line_read = malloc(MAX_SIZE_LINE*sizeof(char));
	
	fgets(line_read,MAX_SIZE_LINE,fp);//HEADER

	int count=0;
	int counterr=0;
	
	while(fgets(line_read,MAX_SIZE_LINE,fp) && count < 100000){
	
		char *lineid 	= strtok(line_read, ",");
		char *data 		= strtok(NULL, ",");
		char *busid 	= strtok(NULL, ",");
		char *lat 		= strtok(NULL, ",");
		char *lon 		= strtok(NULL, ",");
		char *dataavl 	= strtok(NULL, ",");
	
		trim(dataavl);
		trim(data);


		time_t	regdate;	
		if(ret = createTime_strtok(data,&regdate)){
			printf("ERRO createTime_strtok em %s  ret=%d\n",data,ret);
			exit(1);
		}

		//printf("%s ",ctime(&regdate));printf("%s\n",ctime(&lastdate));


		
		
		if( difftime(regdate,lastdate) < 0.0){
			//ERRO SEQUENCIA DE DADOS
			counterr++;
			continue;
		}
		
		lastdate = regdate;
		
		
		/*
		time_t	avldate;	
		if(ret = createTime_strtok(dataavl,&avldate)){
			printf("ERRO createTime_strtok avl em %s  ret=%d\n",dataavl,ret);
			exit(1);
		}*/
		
		count++;
	}
	
	printf("%d %d\n",count,counterr);
	
	fclose(fp);
	printf("FIM\n");
	return 0;
}
