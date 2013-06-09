//gcc -lm -std=gnu99 -D_FILE_OFFSET_BITS=64  avl.c 
#include "avl.h"



time_t	mindate; 
time_t	maxdate; 


int main(){
	printf("INICIO\n");
	
	int nbfiles = dirFiles("./bus");
	if(nbfiles ==-1){
		printf("Diretorio ./bus não existe\n");
		exit(1);
	}
	if(nbfiles > 0){
		printf("Diretorio ./bus não vazio nf = %d\n",nbfiles);
		exit(1);
	}


	char txt[] = "2013-03-01 00:00:00";
	int ret;
	if((ret = createTime_strtok(txt,&mindate))){
		printf("Create time mindate Error  %d\n",ret);
		exit(1);
	}
	maxdate	= getCurrTime();

	
	FILE *fp= fopen("./avl15_21.txt","rb");
	
	if(!fp){
		printf("ARQUIVO NULO\n");
		exit(0);	
	}
	
	char *line_read = malloc(MAX_SIZE_LINE*sizeof(char));
	
	fgets(line_read,MAX_SIZE_LINE,fp);//HEADER

	int count=0;
	int counterr=0;

	double minlat,maxlat,minlon,maxlon;
	minlat = 99999.99;
	minlon = 99999.99;
	maxlat = -99999.99;
	maxlon = -99999.99;

	int minbid=999999999; 
	int maxbid=-999999999;


	time_t	initime=getCurrTime();
	printf("INI:%s\n",ctime(&initime));
	
	while(fgets(line_read,MAX_SIZE_LINE,fp) ){
	
		char *lineid_str 	= strtok(line_read, ",");
		char *data_str 		= strtok(NULL, ",");
		char *busid_str 	= strtok(NULL, ",");
		char *lat_str 		= strtok(NULL, ",");
		char *lon_str 		= strtok(NULL, ",");
		char *dataavl_str 	= strtok(NULL, ",");
	
		trim(dataavl_str);
		trim(data_str);


		time_t	regdate;	
		if(ret = createTime_strtok(data_str,&regdate)){
			printf("ERRO createTime_strtok em %s  ret=%d\n",data_str,ret);
			exit(1);
		}

		if( difftime(maxdate,regdate) < 0.0){
			printf("ERRO data registro (max)  %s \n",data_str);
			printf("regdata %s \n",ctime(&regdate));
			printf("maxdata %s \n",ctime(&maxdate));
			exit(1);
		}

		if( difftime(regdate,mindate) < 0.0){
			printf("ERRO data registro (min)  %s \n",data_str);
			printf("regdata %s \n",ctime(&regdate));
			printf("mindate %s \n",ctime(&mindate));
			exit(1);
		}

	
		
		int busid;
		if(getIstr(busid_str,&busid)!=0){
			printf("Erro conv busid_str %s  \n",busid_str);
			exit(1);
		}

		int lineid;
		if(getIstr(lineid_str,&lineid)!=0){
			printf("Erro conv lineid_str %s  \n",lineid_str);
			exit(1);
		}


		double lat;
		if(getDstr(lat_str,&lat)!=0){
			printf("Erro conv lat_str %s  \n",lat_str);
			exit(1);
		}

		double lon;
		if(getDstr(lon_str,&lon)!=0){
			printf("Erro conv lat_str %s  \n",lon_str);
			exit(1);
		}

		if(lat == 0.0 || lon ==0.0)
			continue;
		

		if(lat > maxlat)
			maxlat=lat;	
		if(lon > maxlon)
			maxlon=lon;
		if(lat < minlat)
			minlat=lat;	
		if(lon < minlon)
			minlon=lon;


		//Limite a oeste, Embu das artes Cotia :-23.60552,-46.867676
		//Limite a Leste Suzano : -23.532514,-46.344452
		//Limite ao norte Franco da Rocha, Mairiporã : -23.320819,-46.658936
		//Limite ao Sul Itanhaém : -24.022634,-46.715927
		
		//LAT é de Norte -> Sul
		//LON é de Leste -> Oeste	

		if(lat > -23.320819 || lat < -24.022634 || lon > -46.344452 || lon < -46.867676)
			continue; // COORDENADAS FORA DA CIDADE DE SÃO PAULO



		if( busid < minbid)
			minbid=busid;
		if( busid > maxbid); 
			maxbid=busid;

		appendToBusfile(busid,lineid,data_str,lat,lon);	
		



		count++;
	}

	time_t	fintime=getCurrTime();
	printf("FIN:%s\n",ctime(&fintime));
	printf("DIFF:%f\n", difftime(fintime,initime));
	printf("lat %f %f\n",minlat,maxlat);
	printf("lon %f %f\n",minlon,maxlon);

	printf("minbid maxbid %d %d\n",minbid,maxbid);
	
	printf("%d %d\n",count,counterr);
	
	fclose(fp);
	printf("FIM\n");
	return 0;
}
