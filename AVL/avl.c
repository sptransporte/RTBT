//gcc -lm -std=gnu99 -D_FILE_OFFSET_BITS=64  avl.c 

#include "sptrans.h"
#include "avl.h"



time_t	mindate; 
time_t	maxdate; 


int main(){
	printf("INICIO\n");
	
	checkBusFileStructure();


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
	


	fseeko(fp, 0L, SEEK_END);
	long long int EndScale = ftello(fp);
	fseeko(fp, 0L, SEEK_SET);
	printf("SIZE %lld \n",EndScale);

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


	int LINE_SMASK = 0x00008000;
	int minLS1=999999999;
	int minLS2=999999999;
	int maxLS1=0;
	int maxLS2=0;


	time_t	initime=getCurrTime();
	printf("INI:%s\n",ctime(&initime));
	
	while(fgets(line_read,MAX_SIZE_LINE,fp) ){

		
		barraProgresso(ftello (fp),EndScale);

	
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


		/*
		Utilizando os limites da cidade de são paulo
		*/
		if(lat > -23.320819 || lat < -24.022634 || lon > -46.344452 || lon < -46.867676)
			continue; // COORDENADAS FORA DA CIDADE DE SÃO PAULO



		if(lineid & LINE_SMASK){//SENTIDO 2
				if(lineid >maxLS2)
					maxLS2=lineid;
				if(lineid <minLS2)
					minLS2=lineid;					
		}else{//SENTIDO 1
				if(lineid >maxLS1)
					maxLS1=lineid;
				if(lineid <minLS1)
					minLS1=lineid;	
		}


		if( busid < minbid)
			minbid=busid;
		if( busid > maxbid); 
			maxbid=busid;

		//utilizar esta função juntamente com a verificação inicial de checkBusFileStructure()
		appendToBusfile(busid,lineid,regdate,lat,lon);	
		



		count++;
	}

	time_t	fintime=getCurrTime();
	printf("FIN:%s\n",ctime(&fintime));
	printf("DIFF:%f\n", difftime(fintime,initime));
	printf("lat %f %f\n",minlat,maxlat);
	printf("lon %f %f\n",minlon,maxlon);

	printf("minbid maxbid %d %d\n",minbid,maxbid);
	
	printf("minLS1 maxLS1 %d %d\n",minLS1,maxLS1);
	printf("minLS2 maxLS2 %d %d\n",minLS2,maxLS2);

	printf("%d %d\n",count,counterr);
	
	fclose(fp);
	printf("FIM\n");
	return 0;
}
