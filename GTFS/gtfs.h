#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>

#include "sptrans.h"

#define LINESTRMAXSIZE 1000


/*
GTFS STOP INFO
*/

typedef struct {
	unsigned long  stopid;
	char *name;
	char *desc;
	COORD coord;
	
} STOP;

typedef struct {
	STOP* stops;
	int size;	
} STOPS;

/*
GTFS SHAPE INFO
*/


typedef struct {
	unsigned long  shapeid;
	COORD *shape;
	double *dst;
	int size;

} SHAPE;

typedef struct {
	SHAPE* shapes;
	int size;	
} SHAPES;


/*
GTFS TRIP INFO
*/

typedef struct {
	char *serviceid;
	char *tripid;
	char *headsign;
	unsigned char directionid;
	unsigned long  shapeid;
} TRIP;


typedef struct {
	char *routeid;

	int size;
	TRIP *mytrips;

} TRIPIDS;

typedef struct {
	TRIPIDS* tripids;
	int size;	
} TRIPS;



/*
GTFS STOP TIMES INFO
*/
typedef struct {
	char *tripid;
	unsigned long  *stopids;
	int size;
	
} STOPTIME;

typedef struct {
	STOPTIME* stoptimes;
	int size;	
} STOPTIMES;

/////////////////////////////////////////////////////


typedef struct {
	char *routeid;	//TRIPIDS
	char *tripid;	//TRIP
	char *headsign; //TRIP

	COORD *shape;	//SHAPE
	double *dst;	//SHAPE
	int sizeshape;	//SHAPE

	int sizestops;	//STOPTIME
	COORD* coordstops;	//STOP

} VIAGEM;


typedef struct {
	VIAGEM* viagens;
	int size; //SHAPES

} VIAGENS;


VIAGENS* loadVIAGENS(STOPS *pstops,STOPTIMES *pstoptimes,SHAPES *pshapes,TRIPS *ptrips){
	VIAGENS *pviagens = malloc(sizeof(VIAGENS));
	pviagens->size=pshapes->size;
	pviagens->viagens = malloc(sizeof(VIAGEM)*(pviagens->size));

	for(int is=0;is<pshapes->size;is++){

		pviagens->viagens[is].routeid 	= 0;
		pviagens->viagens[is].tripid 	= 0;
		pviagens->viagens[is].shape 	= 0;
		pviagens->viagens[is].dst 	= 0;
		pviagens->viagens[is].sizeshape = 0;
		pviagens->viagens[is].sizestops = 0;
		pviagens->viagens[is].coordstops= 0;


		SHAPE* pshape = &(pshapes->shapes[is]);
		pviagens->viagens[is].shape	= pshape->shape;
		pviagens->viagens[is].dst	= pshape->dst;	
		pviagens->viagens[is].sizeshape	= pshape->size;

		for(int it=0;it<ptrips->size;it++){
			TRIPIDS* ptripisa = &(ptrips->tripids[it]);
			for(int itps=0; itps < ptripisa->size; itps++){
				TRIP* ptrip=&(ptripisa->mytrips[itps]);
				if(ptrip->shapeid == pshape->shapeid){
					pviagens->viagens[is].routeid = ptripisa->routeid;
					pviagens->viagens[is].tripid = 	 ptrip->tripid;	
					pviagens->viagens[is].headsign = ptrip->headsign;
					
					break;
				}				
			}

		}

		if(!pviagens->viagens[is].routeid || !pviagens->viagens[is].tripid || !pviagens->viagens[is].headsign){
			printf("shapeid %ld não encontrado\n", pshape->shapeid);	
			exit(1);
		}


		for(int ists=0; ists < pstoptimes->size; ists++)
			if(!strcmp(pviagens->viagens[is].tripid,pstoptimes->stoptimes[ists].tripid)){
				STOPTIME* pstoptime=&(pstoptimes->stoptimes[ists]);
				pviagens->viagens[is].sizestops=pstoptime->size;
				pviagens->viagens[is].coordstops= malloc(sizeof(COORD)*pstoptime->size);

				for(int istpt=0; istpt < pstoptime->size; istpt++){
					unsigned long  stopid = pstoptime->stopids[istpt];
					COORD *pcoord=0;
					for(int istop=0;istop < pstops->size; istop++)
						if(pstops->stops[istop].stopid == stopid){
							pcoord=&(pstops->stops[istop].coord);		
							break;
						}

					if(!pcoord){
						printf("stopid %ld não encontrado\n", stopid);	
						exit(1);
					}		
										
					pviagens->viagens[is].coordstops[istpt].lat=pcoord->lat;
					pviagens->viagens[is].coordstops[istpt].lon=pcoord->lon;
					 
				}

			}	
		
		
		if(!pviagens->viagens[is].sizestops || !pviagens->viagens[is].coordstops){
			printf("STOP de shapeid %ld não encontrado\n", pshape->shapeid);	
			exit(1);
		}

		
	}

	return pviagens;
}


typedef struct {
	SHAPE **rota_U;
	SHAPE **rota_S;
	SHAPE **rota_D;
	SHAPE **rota_X;
	char *headsign;

} ROTA;

typedef struct {
	ROTA rota_1;
	ROTA rota_2;
	char *linha;	

} LINHA;








//######## CSV ######################
void TA(char *str){//REMOVE ASPAS
	int strsize= strlen(str);
	char inasp=0;
	for(int i=0;i<strsize;i++){

		if(str[i]=='\n' || str[i]=='\r'){
			str[i]=' ';
			continue;
		}

		if(str[i]=='\"' && !inasp){
			inasp=1;
			continue;
		}
		
		if(str[i]=='\"' && inasp){
			inasp=0;
			continue;
		}

		if(inasp && str[i]==',')		
			str[i]=0x07;
	}
}

void PA(char *str){//POE ASPAS E RETIRA \n
	int strsize= strlen(str);
	if(strsize==0)
		return;	

	for(int i=0;i<strsize;i++){
		if(str[i]==0x07)
			str[i]=',';

		if(str[i]=='"')
			str[i]=' ';

		}
}

char* copyStr(char *token){
	char *cdst;
	cdst=malloc(strlen(token)+1);		
	memcpy(cdst,token, strlen(token)+1);
	return cdst;
}

STOPS *loadSTOPS(char *file){

	char *line_buffer=malloc(LINESTRMAXSIZE);

	STOPS *pstops=malloc(sizeof(STOPS));


	pstops->size=0;
	pstops->stops=NULL;

	FILE* fp = fopen(file,"r");

	if(!fp){
		printf("Verify Base da dados nula - %s\n",file);
		exit(1);	
	}


	//HEADER
	fgets(line_buffer,LINESTRMAXSIZE,fp);


	while(fgets(line_buffer,LINESTRMAXSIZE,fp)){


		if(strlen(line_buffer) >= (LINESTRMAXSIZE -1)){
				printf("STRING MAIOR QUE BUFFER %d  MAX SIZE= %d\n",strlen(line_buffer),LINESTRMAXSIZE);
				exit(1);
		}
		TA(line_buffer);

		pstops->size++;
		pstops->stops=realloc(pstops->stops,sizeof(STOP)*(pstops->size));

		STOP *pstop=&(pstops->stops[pstops->size-1]);

		char *token;

		token = strtok(line_buffer, ",");//primeiro item
		PA(token);
		pstop->stopid = atol(token);

		token = strtok(NULL,",");
		PA(token);
		pstop->name=copyStr(token);


		token = strtok(NULL,",");
		PA(token);
		pstop->desc=copyStr(token);
		

		token = strtok(NULL,",");
		PA(token);
		pstop->coord.lat=atof(token);

		token = strtok(NULL,",");
		PA(token);
		pstop->coord.lon=atof(token);


		if(strtok(NULL,",")!= NULL ){
			printf("FORMATO INVALIDO\n");
			exit(1);
		}

	}

	free(line_buffer);
	fclose(fp);
	return pstops;	
}


SHAPES *loadSHAPES(char *file){

	char *line_buffer=malloc(LINESTRMAXSIZE);

	SHAPES *pshapes=malloc(sizeof(SHAPES));

	pshapes->size=0;
	pshapes->shapes=NULL;

	FILE* fp = fopen(file,"r");

	if(!fp){
		printf("Verify Base da dados nula - %s\n",file);
		exit(1);	
	}


	//HEADER
	fgets(line_buffer,LINESTRMAXSIZE,fp);


	while(fgets(line_buffer,LINESTRMAXSIZE,fp)){


		if(strlen(line_buffer) >= (LINESTRMAXSIZE -1)){
				printf("STRING MAIOR QUE BUFFER %d  MAX SIZE= %d\n",strlen(line_buffer),LINESTRMAXSIZE);
				exit(1);
		}
		TA(line_buffer);

		char *token;

		token = strtok(line_buffer, ",");//primeiro item
		PA(token);
		unsigned long shapeid = atol(token);


		token = strtok(NULL,",");
		PA(token);
		double lat=atof(token);

		token = strtok(NULL,",");
		PA(token);
		double lon=atof(token);

		token = strtok(NULL,",");
		PA(token);
		unsigned int seq=atoi(token);

		token = strtok(NULL,",");
		PA(token);
		double dst=atof(token);
		
	
		if(strtok(NULL,",")!= NULL ){
			printf("FORMATO INVALIDO\n");
			exit(1);
		}

		SHAPE *pshape;

		if(seq==1){
			pshapes->size++;
			pshapes->shapes=realloc(pshapes->shapes,sizeof(SHAPE)*(pshapes->size));
			pshape=&(pshapes->shapes[pshapes->size-1]);

			pshape->shapeid=shapeid;
			pshape->shape = NULL;
			pshape->dst = NULL;
			pshape->size = 0;

		}else			
			pshape=&(pshapes->shapes[pshapes->size-1]);
			

		if(pshape->shapeid!=shapeid){
			printf("FORMATO INVALIDO shapeid\n");
			exit(1);
		}


		if(pshape->size!=seq-1){
			printf("FORMATO INVALIDO seq\n");
			exit(1);
		}

		//XXX EXISTEM DIVERGENCIAS DA DISTANCIA PERCORRIDA ASSUMINDO QUE AS COORDENADAS ESTÃO OK , USANDO O MEU CALCULO DE DISTANCIA XXX
		if(pshape->size == 0)
			dst=0.0;
		else{
			COORD pa,pb;
			pa.lat=pshape->shape[pshape->size -1].lat;
			pa.lon=pshape->shape[pshape->size -1].lon ;
			pb.lat=lat;
			pb.lon=lon;
			dst = dist(&pa,&pb) + pshape->dst[pshape->size -1];
		}		
		//XXX  -------------------------------------    XXX

		if(pshape->size > 0 && pshape->dst[pshape->size -1] >= dst ){
			printf("FORMATO INVALIDO dst %lf  %lf  shapeid = %ld  seq = %d \n", pshape->dst[pshape->size -1],dst,shapeid,seq);
			exit(1);
		}
		

	
		
		if(pshape->size > 0 && pshape->shape[pshape->size -1].lat == lat && pshape->shape[pshape->size -1].lon == lon){
			printf("FORMATO INVALIDO coord \n");
			exit(1);
		}


		pshape->size++;
		pshape->shape = realloc(pshape->shape,  sizeof(COORD)*(pshape->size));
		pshape->dst   = realloc(pshape->dst, sizeof(double)*(pshape->size));	
		
		pshape->shape[pshape->size -1].lat = lat;
		pshape->shape[pshape->size -1].lon = lon;
		pshape->dst[pshape->size -1] = dst; 		


	}

	free(line_buffer);
	fclose(fp);
	return pshapes;	
}


TRIPS *loadTRIPS(char *file){

	char *line_buffer=malloc(LINESTRMAXSIZE);

	TRIPS *ptrips=malloc(sizeof(TRIPS));

	ptrips->size=0;
	ptrips->tripids=NULL;

	FILE* fp = fopen(file,"r");

	if(!fp){
		printf("Verify Base da dados nula - %s\n",file);
		exit(1);	
	}


	//HEADER
	fgets(line_buffer,LINESTRMAXSIZE,fp);


	while(fgets(line_buffer,LINESTRMAXSIZE,fp)){


		if(strlen(line_buffer) >= (LINESTRMAXSIZE -1)){
				printf("STRING MAIOR QUE BUFFER %d  MAX SIZE= %d\n",strlen(line_buffer),LINESTRMAXSIZE);
				exit(1);
		}
		TA(line_buffer);


		char *l_routeid;

		char *l_serviceid;
		char *l_tripid;
		char *l_headsign;
		unsigned char l_directionid;
		unsigned long  l_shapeid;



		char *token;

		token = strtok(line_buffer, ",");//primeiro item
		PA(token);
		l_routeid=copyStr(token);

		token = strtok(NULL,",");
		PA(token);
		l_serviceid=copyStr(token);

		token = strtok(NULL,",");
		PA(token);
		l_tripid=copyStr(token);

		token = strtok(NULL,",");
		PA(token);
		l_headsign=copyStr(token);

		token = strtok(NULL,",");
		PA(token);
		l_directionid=atoi(token);

		token = strtok(NULL,",");
		PA(token);
		l_shapeid=atol(token);


		if(strtok(NULL,",")!= NULL ){
			printf("FORMATO INVALIDO\n");
			exit(1);
		}


		TRIPIDS *ptripids;
		TRIP	*ptrip;

		if(ptrips->size != 0 && !strcmp(l_routeid, ptrips->tripids[ptrips->size-1].routeid) ){
			free(l_routeid);
			ptripids=&(ptrips->tripids[ptrips->size-1]);

		}else{

			ptrips->size++;
			ptrips->tripids=realloc(ptrips->tripids,sizeof(TRIPIDS)*(ptrips->size));
			ptripids=&(ptrips->tripids[ptrips->size-1]);
			ptripids->routeid=l_routeid;
			ptripids->size=0;
			ptripids->mytrips=NULL;
			
		}


		ptripids->size++;
		ptripids->mytrips=realloc(ptripids->mytrips,sizeof(TRIP)*(ptripids->size));
		ptrip = &(ptripids->mytrips[ptripids->size -1]);			



		//ptripids->routeid=l_routeid;
		ptrip->serviceid=l_serviceid;
		ptrip->tripid=l_tripid;
		ptrip->headsign=l_headsign;
		ptrip->directionid=l_directionid;
		ptrip->shapeid=l_shapeid;



	}

	free(line_buffer);
	fclose(fp);
	return ptrips;	
}


STOPTIMES *loadSTOPTIMES(char *file){

	char *line_buffer=malloc(LINESTRMAXSIZE);

	STOPTIMES *pstoptimes=malloc(sizeof(STOPTIMES));

	pstoptimes->size=0;
	pstoptimes->stoptimes=NULL;

	FILE* fp = fopen(file,"r");

	if(!fp){
		printf("Verify Base da dados nula - %s\n",file);
		exit(1);	
	}


	//HEADER
	fgets(line_buffer,LINESTRMAXSIZE,fp);


	while(fgets(line_buffer,LINESTRMAXSIZE,fp)){


		if(strlen(line_buffer) >= (LINESTRMAXSIZE -1)){
				printf("STRING MAIOR QUE BUFFER %d  MAX SIZE= %d\n",strlen(line_buffer),LINESTRMAXSIZE);
				exit(1);
		}
		TA(line_buffer);

		char *l_tripid;
		unsigned long  l_stopids;
		int l_seq;

		char *token;

		token = strtok(line_buffer, ",");//primeiro item
		PA(token);
		l_tripid=copyStr(token);


		token = strtok(NULL,",");
		token = strtok(NULL,",");


		token = strtok(NULL,",");
		PA(token);
		l_stopids=atol(token);


		token = strtok(NULL,",");
		PA(token);
		l_seq=atoi(token);


		if(strtok(NULL,",")!= NULL ){
			printf("FORMATO INVALIDO\n");
			exit(1);
		}


		STOPTIME *pstoptime;

		if(pstoptimes->size != 0 && !strcmp(l_tripid,pstoptimes->stoptimes[pstoptimes->size-1].tripid) ){
			free(l_tripid);
			pstoptime=&(pstoptimes->stoptimes[pstoptimes->size-1]);

		}else{
			pstoptimes->size++;
			pstoptimes->stoptimes=realloc(pstoptimes->stoptimes,sizeof(STOPTIME)*(pstoptimes->size));
			pstoptime=&(pstoptimes->stoptimes[pstoptimes->size-1]);
			pstoptime->tripid=l_tripid;		
			pstoptime->size=0;
			pstoptime->stopids=NULL;

		}

		pstoptime->size++;
		pstoptime->stopids = realloc(pstoptime->stopids, sizeof(double)*(pstoptime->size));	
		pstoptime->stopids[pstoptime->size -1]=l_stopids;


		if(l_seq != (pstoptime->size)){
			printf("FORMATO INVALIDO seq \n");
			exit(1);
		}
		
	}

	free(line_buffer);
	fclose(fp);
	return pstoptimes;	
}



//######## CSV ######################

