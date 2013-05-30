
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>

#define RAIOTERRA 6378100.0

#define STEP 60.0 //STEP EM METROS DA PARAMETRIZAÇÃO DO TRAJETO PELA DISTANCIA PERCORRIDA [32-100 M: 56,4] 



typedef struct {
      double lat;
      double lon;
} COORD;



typedef struct {
	int dia;
	int sentido;
	int id;
	char lid[20];
	char cdestino[100];
	COORD  *vcoords;
	double *vdstper;
	int size;
	//
	int sizestep;
	COORD  *vcoordssteps;
	
	

} TRAJETO;

typedef struct {
	TRAJETO **vptraj;
	int size;
} MALHA;



double dist(COORD *ppa,COORD *ppb){

	if(ppa->lat == ppb->lat && ppa->lon == ppb->lon )
		return 0.0;
	
	double dist = RAIOTERRA*
	acos(
		cos((90-ppb->lat)*M_PI/180.0)*
		cos((90-ppa->lat)*M_PI/180.0)+
		sin((90-ppb->lat)*M_PI/180.0)*
		sin((90-ppa->lat)*M_PI/180.0)*
		cos((ppa->lon-ppb->lon)*M_PI/180.0)
	);


	return dist;
}



typedef struct {
	double distmin;
	double altura;
	double froma;
	double fromb;
} METRIC;

METRIC distmind(double dab,double dac, double dbc){

	METRIC ret;
	ret.distmin=0.0;
	ret.froma=0.0;
	ret.fromb=0.0;
	ret.altura=0.0;


	if(roundf(dac)==0.0 ){
		ret.fromb=-dab;
		return ret;
	}

	if(roundf(dbc)==0.0 ){
		ret.froma=dab;
		return ret;
	}



	double x = ( (dac*dac) -(dbc*dbc) + (dab*dab) )/(2.0*dab);
	double y = sqrt(fabs((dac*dac) - x*x));

	double lamb = x/dab;

	ret.froma=x;
	ret.fromb=x-dab;
	ret.altura=y;

	if(lamb < 0.0){//Mais proximo do ponto A

		ret.distmin=dac;
		return ret;
	}

	if(lamb > 1.0){//Mais proximo do ponto B

		ret.distmin=dbc;
		return ret;
	}
	//lamb [0,1]
	
	ret.distmin= sqrt(x*x -2.0*x*lamb*dab + lamb*lamb*dab*dab + y*y);
	return ret;
}


METRIC distmin(COORD *ppa,COORD *ppb,COORD *ppc){
	double dab=dist(ppa,ppb);
	double dac=dist(ppa,ppc);
	double dbc=dist(ppb,ppc);

	return distmind(dab,dac,dbc);
}


double distminalinha(COORD* pp,COORD *vcoord,int size){
	double ret=100*RAIOTERRA;

	for(int i=1;i<size;i++){
		METRIC metr = distmin(&vcoord[i-1],&vcoord[i],pp);
		if(metr.distmin < ret )
			ret = metr.distmin;
	}

	return ret;
}


void initMALHA(MALHA *pmalha){
	pmalha->vptraj=0;
	pmalha->size=0;	
}


void addTRAJETO(MALHA *pmalha,TRAJETO *ptraj){

	pmalha->vptraj = realloc(pmalha->vptraj, sizeof(TRAJETO*)*(pmalha->size + 1) );
	pmalha->size++;

	(pmalha->vptraj)[pmalha->size -1]= ptraj;

}

void addCOORD(TRAJETO *ptraj,double lat,double lon){
	
	if(ptraj->size == 0){//primeira coord
		
		ptraj->vcoords=malloc(sizeof(COORD));
		(ptraj->vcoords)[ptraj->size].lat=lat;	
		(ptraj->vcoords)[ptraj->size].lon=lon;


		ptraj->vdstper=malloc(sizeof(double));
		(ptraj->vdstper)[ptraj->size]=0.0;	

		ptraj->size++;	
		return;	
	}	

	COORD *cordlast=&((ptraj->vcoords)[ptraj->size -1]);

	if((cordlast->lat == lat) && (cordlast->lon == lon))//COORD repetida
		return;
	
	ptraj->vcoords = realloc(ptraj->vcoords, sizeof(COORD)*(ptraj->size + 1) );
	(ptraj->vcoords)[ptraj->size].lat=lat;	
	(ptraj->vcoords)[ptraj->size].lon=lon;

	ptraj->vdstper=realloc(ptraj->vdstper,sizeof(double)*(ptraj->size + 1));
	(ptraj->vdstper)[ptraj->size]=(ptraj->vdstper)[ptraj->size -1] + dist(&((ptraj->vcoords)[ptraj->size]),&((ptraj->vcoords)[ptraj->size -1]));	


	ptraj->size++;
	return;

}


void getMiddleCoord(COORD *pc1,COORD *pc2, double d1,double d2,double dm,COORD* pcm){
		
	pcm->lat= dm*(pc2->lat - pc1->lat)/(d2 - d1) + (pc1->lat -d1*(pc2->lat - pc1->lat)/(d2 - d1));
	pcm->lon= dm*(pc2->lon - pc1->lon)/(d2 - d1) + (pc1->lon -d1*(pc2->lon - pc1->lon)/(d2 - d1));

}

void createTRAJETO(int dia,int sentido, int id,char* lid,char *cdestino,char* scoords,TRAJETO *ptraj){

	ptraj->dia=dia;
	ptraj->sentido=sentido;
	sprintf(ptraj->lid,"%s",lid);
	sprintf(ptraj->cdestino,"%s",cdestino);
	ptraj->id=id;
	ptraj->vcoords=0;	
	ptraj->vdstper=0;	
	ptraj->size=0;


	char *token;
	char *search = "||";
	token = strtok(scoords, search);

	while(token){

		
		int ilat=atoi(token);
		double lat=ilat*1.0/1000000;

		token = strtok(NULL, search);
		int ilon=atoi(token);
		double lon=ilon*1.0/1000000;
			
		addCOORD(ptraj,lat,lon);


		token = strtok(NULL, search);
	}

	//->

	double disttot = ptraj->vdstper[ptraj->size-1];
	ptraj->sizestep = (int)floor(disttot/(STEP)) + 1;
	ptraj->vcoordssteps=malloc(sizeof(COORD)*(ptraj->sizestep));


	//SETA COORDENADA INICIAL
	ptraj->vcoordssteps[0].lat=ptraj->vcoords[0].lat;
	ptraj->vcoordssteps[0].lon=ptraj->vcoords[0].lon;
	double pd=STEP;

	int pi=0;
	int idst=1;

	while(pi < ptraj->sizestep -1 ){	

		char found=0;

		for(int i=idst;i<ptraj->size;i++)
			if( ptraj->vdstper[i-1] <= pd &&  pd <= ptraj->vdstper[i]){//ENCONTREI O TRECHO 		
				found=1;
				idst=i;
				pi++;
				getMiddleCoord(&(ptraj->vcoords[i-1]),&(ptraj->vcoords[i]), ptraj->vdstper[i-1],ptraj->vdstper[i],pd,&(ptraj->vcoordssteps[pi]));		
				pd=pd+STEP;	
				break;		
			}

		if(!found){
			printf("ERRO EM STEP\n");
			exit(1);
		}

		
	}

	if(pd != (ptraj->sizestep)*(STEP)){
			printf("ERRO 2 EM STEP\n");
			exit(1);
	}

	if(pi != (ptraj->sizestep -1)){
			printf("ERRO 3 EM STEP\n");
			exit(1);
	}

	//<-


}




MALHA* readfile(char *file){



	FILE* fp = fopen(file,"r");

	if(!fp){
		printf("Verify Base da dados nula\n");
		return 0;	
	}

	int STRMAXSIZE=200*BUFSIZ;
	//char line_buffer[STRMAXSIZE];
	char *line_buffer=malloc(STRMAXSIZE);

	int count=0;

	MALHA* pmalha=malloc(sizeof(MALHA));	
	initMALHA(pmalha);

	while(1){
		
		if(fscanf(fp,"%s",line_buffer) == EOF)break;
		count++;
		
	
		//printf("%s\n",line_buffer);
		int size= strlen(line_buffer);
		if(size >= (STRMAXSIZE -1)){
			printf("STRING MAIOR QUE BUFFER %d  MAX SIZE= %d\n",size,STRMAXSIZE);
			exit(1);
		}

		char *token;
		char *search = "#";
		char lid[20];
		char cdestino1[100];
		char cdestino2[100];

		token = strtok(line_buffer, search);//primeiro item
		sprintf(lid,"%s",token);

		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);

		char circular = !strcmp(token,"true");		

		token = strtok(NULL, search);
		int id1=atoi(token);

		token = strtok(NULL, search);
		sprintf(cdestino1,"%s",token);
		
		token = strtok(NULL, search);
		sprintf(cdestino2,"%s",token);

		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);

		int id2=atoi(token);

		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);
		token = strtok(NULL, search);
		

		char *s10,*s11,*s12;
		//SENTIDO A
		token = strtok(NULL, search);s10=malloc(strlen(token)+1);sprintf(s10,"%s",token);
		token = strtok(NULL, search);s11=malloc(strlen(token)+1);sprintf(s11,"%s",token);
		token = strtok(NULL, search);s12=malloc(strlen(token)+1);sprintf(s12,"%s",token);

		char *s20,*s21,*s22;
		//SENTIDO B
		token = strtok(NULL, search);s20=malloc(strlen(token)+1);sprintf(s20,"%s",token);
		token = strtok(NULL, search);s21=malloc(strlen(token)+1);sprintf(s21,"%s",token);
		token = strtok(NULL, search);s22=malloc(strlen(token)+1);sprintf(s22,"%s",token);


		if(!strcmp(s10,s11)) {//DIA 1 == DIA 0

		}

		if(!strcmp(s10,s12) ) {//DIA 2 == DIA 0
			
		}else if(!strcmp(s11,s12) ) {//DIA 2 == DIA 1
		
		}

		if(!strcmp(s10,s20) || !strcmp(s11,s21) || !strcmp(s12,s22) ) {
			//printf("sasasasasas\n");
		}

		TRAJETO *ptraj1=malloc(sizeof(TRAJETO));
		createTRAJETO(0,1,id1,lid,cdestino1,s10,ptraj1);
		addTRAJETO(pmalha,ptraj1);

		
		TRAJETO *ptraj2=malloc(sizeof(TRAJETO));
		createTRAJETO(0,2,id2,lid,cdestino2,s20,ptraj2);
		addTRAJETO(pmalha,ptraj2);
		

		
		//exit(1);

		free(s10);
		free(s11);
		free(s12);
		free(s20);
		free(s21);
		free(s22);

	}


	//printf("%d\n",count);

	fclose(fp);
	free(line_buffer);

	return pmalha;
}







void flushHTMLpolyLine(FILE* fp,char* plname,char* plcor,COORD* rota, int size){

	//->
 	fprintf(fp,"       var %sCoordinates = [\n",plname);
	for(int i=0;i<size;i++){
		if(i!=0)		
	 		fprintf(fp,",\n");

 		fprintf(fp,"		new google.maps.LatLng(%lf, %lf)",rota[i].lat,rota[i].lon);
	}
 	fprintf(fp,"        ];\n");


	fprintf(fp,"        var %s = new google.maps.Polyline({\n          path: %sCoordinates,\n          strokeColor: '%s',\n          strokeOpacity: 1.0,\n          strokeWeight: 2\n        });\n",plname,plname,plcor);

	fprintf(fp,"        %s.setMap(map);\n",plname);

	//<-

}

void criaHTMLTeste(COORD* rota1, int size1,COORD* rota2, int size2){
	FILE *fp = fopen("out.html","w");

	fprintf(fp,"<!DOCTYPE html>\n<html>\n  <head>\n    <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\">\n   <meta charset=\"utf-8\">\n    <title>Google Maps JavaScript API v3 Example: Polyline Simple</title>\n    <!--<link href=\"https://google-developers.appspot.com/maps/documentation/javascript/examples/default.css\" rel=\"stylesheet\">-->\n<style type=\"text/css\">\n		html, body {height: 100%;margin: 0; padding: 0;}\n		#map_canvas {height: 100%;}\n		@media print {html, body {height: auto;}#map_canvas {height: 650px;}}\n</style>\n    <script src=\"https://maps.googleapis.com/maps/api/js?sensor=false\"></script>\n    <script>\n      function initialize() {\n");

	double mylat = rota1[0].lat;
	double mylng = rota1[0].lon;
	fprintf(fp,"var myLatLng = new google.maps.LatLng(%lf, %lf);\n",mylat,mylng);

	fprintf(fp,"var mapOptions = {\n          zoom: 15,\n          center: myLatLng,\n          //mapTypeId: google.maps.MapTypeId.TERRAIN\n	  scaleControl: true,\n          mapTypeId: google.maps.MapTypeId.ROADMAP\n        };\n        var map = new google.maps.Map(document.getElementById('map_canvas'), mapOptions);\n");

	
	flushHTMLpolyLine(fp,"mypl1","#FF0000",rota1, size1);

	flushHTMLpolyLine(fp,"mypl2","#00FF00",rota2, size2);

	double orglat = rota1[0].lat;
	double orglng = rota1[0].lon;
	double dstlat = rota1[size1-1].lat;
	double dstlng = rota1[size1-1].lon;

	fprintf(fp,"var morg = new google.maps.Marker({position:  new google.maps.LatLng(%lf, %lf),map: map,title: 'Origem' });\n",orglat,orglng);
	fprintf(fp,"var mdst = new google.maps.Marker({position:  new google.maps.LatLng(%lf, %lf),map: map,title: 'Destino' })\n;",dstlat,dstlng);

	fprintf(fp,"}\n    </script>\n  </head>\n  <body onload=\"initialize()\">\n    <div id=\"map_canvas\"></div>\n  </body>\n</html>\n");

	fclose(fp);
}



