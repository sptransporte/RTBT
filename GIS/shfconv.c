//gcc shfconv.c libshp.a 
//gcc shfconv.c libshp.a -lm -std=gnu99 -I ../GTFS/
#include <stdlib.h>
#include <string.h>
#include "shapefil.h"
#include "gtfs.h"

/*
libshp.a utilizado do projeto:
http://shapelib.maptools.org/
compilada no kernel 2.6.32-5-686

*/

/*
Limites cidade de São Paulo

Limite a oeste, Embu das artes Cotia :-23.60552,-46.867676
Limite a Leste Suzano : -23.532514,-46.344452
Limite ao norte Franco da Rocha, Mairiporã : -23.320819,-46.658936
Limite ao Sul Itanhaém : -24.022634,-46.715927
		
LAT é de Norte -> Sul	(Y)
LON é de Leste -> Oeste	(X)

-23.320819 
-46.867676
-46.344452
-24.022634

http://metro.teczno.com/#sao-paulo
http://dados.gov.br/dataset/malha-geometrica-dos-municipios-brasileiros/resource/93e3e2f0-e9fd-4cc1-af06-0046af19736f
http://www.fflch.usp.br/centrodametropole/818
http://mapas.mma.gov.br/i3geo/datadownload.htm
*/



SHP_CVSID("$Id: shconv.c,v 0.0 2013-06-10 00:00:00 fwarmerdam Exp $")
#define MAX_SIZE_LINE 100

typedef struct {
	double *vlat;
	double *vlon;
	int size;
} linePoints;



void loadBusFile(char* file ,linePoints *lpoints);
static void shapeFileLine(linePoints *lpoints,const char *pszFilename);
static void createArc(const char *pszFilename);
static void createArc2(const char *pszFilenameSPTRANS,const char *pszFilenameCPTM,const char *pszFilenameMETRO);

int main(){
	printf("Inicio\n");

	//linePoints lpoints;
	//loadBusFile("./bus/8579",&lpoints);
	//shapeFileLine(&lpoints,"/tmp/tst.shp");
	
	//Test_WritePoints( SHPT_POLYGON /*SHPT_ARC*/, "/tmp/tst/test1.shp" );

	//createArc("/tmp/shapelib-1.3.0/sptrans.shp");

	createArc2("/tmp/sptrans.shp","/tmp/cptm.shp","/tmp/metro.shp");

}



void loadBusFile(char* file ,linePoints *lpoints){

	lpoints->size=0;
	lpoints->vlat=NULL;
	lpoints->vlon=NULL;

	FILE *fp= fopen(file,"rb");
	
	if(!fp){
		printf("ARQUIVO NULO %s\n",file);
		exit(0);	
	}

	char *line_read = malloc(MAX_SIZE_LINE*sizeof(char));
	int ret;

	while(fgets(line_read,MAX_SIZE_LINE,fp) ){

		lpoints->size++;
		lpoints->vlat = realloc(lpoints->vlat,sizeof(double)*(lpoints->size));
		lpoints->vlon = realloc(lpoints->vlon,sizeof(double)*(lpoints->size));
		

		
		char *lineid_str 	= strtok(line_read, ",");
		char *data_str 		= strtok(NULL, ",");
		char *lat_str 		= strtok(NULL, ",");
		char *lon_str 		= strtok(NULL, ",");

	

		lpoints->vlat[lpoints->size -1]=atof(lat_str);
		lpoints->vlon[lpoints->size -1]=atof(lon_str);

	}	

	if(lpoints->size == 0){
		printf("ARQUIVO VAZIO %s\n",file);
		exit(0);
	}

	free(line_read);
	fclose(fp);

}





/************************************************************************/
/*                          shapeFileLine()                             */
/*                                                                      */
/*                                                                      */
/************************************************************************/

static void shapeFileLine(linePoints *lpoints,const char *pszFilename)

{

   	

    SHPHandle	hSHPHandle;
    SHPObject	*psShape;


    hSHPHandle = SHPCreate( pszFilename, SHPT_MULTIPOINT );
    psShape = SHPCreateSimpleObject(SHPT_MULTIPOINT,lpoints->size,lpoints->vlon,lpoints->vlat, NULL);
    SHPWriteObject( hSHPHandle, -1, psShape );
    SHPDestroyObject( psShape );


    SHPClose( hSHPHandle );
}


void convCOORDtoVet(double **pvlat,double **pvlon,int size,COORD *vcoord){
	*pvlat=NULL;
	*pvlon=NULL;

	*pvlat = malloc(sizeof(double)*size);
	*pvlon = malloc(sizeof(double)*size);

	for(int i=0;i<size;i++){
		(*pvlat)[i]=vcoord[i].lat;
		(*pvlon)[i]=vcoord[i].lon;
	}

}

void freeConvCOORDtoVet(double *vlat,double *vlon){
	if(!vlat)
		free(vlat);
	if(!vlon)
		free(vlon);
}

static void createArc(const char *pszFilename){

    double	*vlat,*vlon;	

    SHPHandle hSHPHandle = SHPCreate( pszFilename, SHPT_ARC );



    SHAPES *pshapes = loadSHAPES("../GTFS/gtfsdata/shapes.txt");

	for(int i=0;i<pshapes->size;i++){

		convCOORDtoVet(&vlat,&vlon,pshapes->shapes[i].size,pshapes->shapes[i].shape);
		SHPObject	*psShape;
		psShape = SHPCreateSimpleObject(SHPT_ARC,pshapes->shapes[i].size,vlon,vlat, NULL);


		psShape = SHPCreateObject(SHPT_ARC, i,
				0, NULL, NULL,
				pshapes->shapes[i].size,vlon,vlat,
				NULL,NULL);


		SHPWriteObject( hSHPHandle, -1, psShape );
		SHPDestroyObject( psShape );
		freeConvCOORDtoVet(vlat,vlon);

	}









    SHPClose( hSHPHandle );
}

char isCPTM(unsigned long shapeid){

	return (
		shapeid==17846 ||
		shapeid==17847 ||
		shapeid==17848 ||
		shapeid==17849 ||
		shapeid==17850 ||
		shapeid==17851 ||
		shapeid==17852 ||
		shapeid==17853 ||
		shapeid==17854 ||
		shapeid==17855 ||
		shapeid==17856 ||
		shapeid==17857);

}


char isMETRO(unsigned long shapeid){

	return (
		shapeid==17838 ||
		shapeid==17839 ||
		shapeid==17840 ||
		shapeid==17841 ||
		shapeid==17842 ||
		shapeid==17843 ||
		shapeid==40777 ||
		shapeid==40778 ||
		shapeid==17844 ||
		shapeid==17845 );

}


static void createArc2(const char *pszFilenameSPTRANS,const char *pszFilenameCPTM,const char *pszFilenameMETRO){

    double	*vlat,*vlon;	

	SHPHandle hSHPHandle;
	SHPHandle hSHPHandleSPTRANS = SHPCreate( pszFilenameSPTRANS, SHPT_ARC );
	SHPHandle hSHPHandleCPTM = SHPCreate( pszFilenameCPTM, SHPT_ARC );
	SHPHandle hSHPHandleMETRO = SHPCreate( pszFilenameMETRO, SHPT_ARC );



    SHAPES *pshapes = loadSHAPES("../GTFS/gtfsdata/shapes.txt");

	for(int i=0;i<pshapes->size;i++){

		unsigned long shapeid = pshapes->shapes[i].shapeid;

		if(isCPTM(shapeid))
			hSHPHandle=hSHPHandleCPTM;
		else if (isMETRO(shapeid))
			hSHPHandle=hSHPHandleMETRO;
			else
			hSHPHandle=hSHPHandleSPTRANS;

		convCOORDtoVet(&vlat,&vlon,pshapes->shapes[i].size,pshapes->shapes[i].shape);
		SHPObject	*psShape;
		//psShape = SHPCreateSimpleObject(SHPT_ARC,pshapes->shapes[i].size,vlon,vlat, NULL);


		psShape = SHPCreateObject(SHPT_ARC, i,
				0, NULL, NULL,
				pshapes->shapes[i].size,vlon,vlat,
				NULL,NULL);


		SHPWriteObject( hSHPHandle, -1, psShape );
		SHPDestroyObject( psShape );
		freeConvCOORDtoVet(vlat,vlon);

	}









	SHPClose( hSHPHandleSPTRANS );
	SHPClose( hSHPHandleCPTM );
	SHPClose( hSHPHandleMETRO );
}



