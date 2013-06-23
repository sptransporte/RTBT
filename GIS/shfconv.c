//gcc shfconv.c libshp.a 
#include <stdlib.h>
#include <string.h>
#include "shapefil.h"

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

int main(){
	printf("Inicio\n");

	linePoints lpoints;

	loadBusFile("./bus/8579",&lpoints);

	shapeFileLine(&lpoints,"/tmp/tst.shp");
	
	//Test_WritePoints( SHPT_POLYGON /*SHPT_ARC*/, "/tmp/tst/test1.shp" );

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



