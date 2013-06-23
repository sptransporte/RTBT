#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <math.h>
#include <errno.h>

#include <dirent.h>

#define MAX_SIZE_LINE 100

 
char* trim(char *str){
		if(!str)
			return NULL;
		for(int i=0;i<strlen(str);i++)
			if(str[i]=='\n' || str[i]=='\r'  || str[i]=='\t')
				str[i]=' ';		
}


int getDstr(char *str,double *pret){
	 errno = 0;
	 char *endptr;	
	 *pret = strtold(str,&endptr);


	 if (endptr == str) {
        	//printf("No digits were found\n");
        	return -1;
	    }

	 if (*endptr != '\0' && *endptr != ' '){
	        //printf("Further characters after number: %s\n", endptr);
		return -2;
	 }


	 return errno;
 }

int getIstr(char *str,int *pret){
	 errno = 0;
	 char *endptr;

	 *pret= strtol(str, &endptr, 10);

	 if (endptr == str) {
        	//printf("No digits were found\n");
        	return -1;
	    }

	 if (*endptr != '\0'){
	        //printf("Further characters after number: %s\n", endptr);
		return -2;
	 }

	 return errno;
 }


int dirFiles(char* mystrdir) {

    DIR *mydir = opendir(mystrdir);

    if(!mydir)
	return -1;//Diretorio nao existe	

    struct dirent *entry = NULL;
    int nfcount=0;        	
    
    while((entry = readdir(mydir))) /* If we get EOF, the expression is 0 and
                                     * the loop stops. */
    {
	if(entry->d_type != DT_REG)//if(strcmp(entry->d_name,".")==0 || strcmp(entry->d_name,"..")==0)
		continue;		
        //printf("%s\n", entry->d_name);
	nfcount++;
    }

    closedir(mydir);

    return nfcount;

}

//-->
int BARRA_PROGRESSO_PORC=-1;
void barraProgresso(long long int catual, long long int EndScale){

	int porc = round(100.0*catual/EndScale);
	if( porc > BARRA_PROGRESSO_PORC){
		BARRA_PROGRESSO_PORC=porc;
		fprintf(stderr, "\r|%d|",BARRA_PROGRESSO_PORC);
	}


	if(EndScale==-1)
		 BARRA_PROGRESSO_PORC=-1;

}
//<--

void checkBusFileStructure(){
	int nbfiles = dirFiles("./bus");
	if(nbfiles ==-1){
		printf("Diretorio ./bus não existe\n");
		exit(1);
	}
	if(nbfiles > 0){
		printf("Diretorio ./bus não vazio nf = %d\n",nbfiles);
		exit(1);
	}
}

void appendToBusfile(int busid,int lineid,char* data_str,double lat,double lon){
	char filename[100];
	sprintf(filename,"./bus/%d",busid);

	FILE *fp =fopen(filename,"a");
	if(!fp){
		printf("Error appendToBusfile abrindo %s\n",filename);
		exit(1);
	}
	fprintf(fp,"%d,%s,%f,%f\n",lineid,data_str,lat,lon);
	
	fclose(fp);
}



/*
CRIA DATA A PARTIR DO FORMATO A-M-D H:M:S.m
*/ 
int createTime_strtok(char* strdata_org,time_t *created_time){

	if(!strdata_org)
		return -1;

	char strdata[100];
	if( (strlen(strdata_org) +1) > sizeof(strdata))
		return -9999;

	strcpy(strdata,strdata_org);
	
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

