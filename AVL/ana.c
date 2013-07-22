//gcc -lm -std=gnu99 -D_FILE_OFFSET_BITS=64  ana.c 

#include "gtfs.h"
#include "avl.h"
#include "ana.h"


void analisa(char* file,CDLINHAV* pcdlinhav){

	avlregArray avlarray;
	loadBusFile(file ,&avlarray);
	
	avl_evento_info avlevtinfo_ultimo;
	char init=false;

	for(int i=0; i< avlarray.size; i++){
		avlreg *pavlr = &(avlarray.array[i]);

		int cd_linha=pavlr->lineid;
		int ll    = (cd_linha)&(~0x8000);
		COORD *pp = &(pavlr->p);
		time_t time=pavlr->enttime;
		
		SHAPE* shapeL = NULL;
		SHAPE* shapeH = NULL;
		shapeL = pcdlinhav->cdlinhav[ll].shapeL;
		shapeH = pcdlinhav->cdlinhav[ll].shapeH;

		if(!init){
			set_avl_evento_info(&avlevtinfo_ultimo,time,cd_linha,*pp,shapeL,shapeH);
			init=true;
			continue;
		}
		avl_evento_info avlevtinfo_atual;
		set_avl_evento_info(&avlevtinfo_atual,time,cd_linha,*pp,shapeL,shapeH);

		
		confrontoDeEventosConsecutivos(&avlevtinfo_ultimo,&avlevtinfo_atual,&init,pcdlinhav);


		avlevtinfo_ultimo=avlevtinfo_atual;



	}
	

	avlarray.size=0;
	free(avlarray.array);//FREE
}



int main(){
	printf("INICIO\n");


	printf("Loading shapes\n");
	SHAPES *pshapes = loadSHAPES("./gtfsdata/shapes.txt");
	printf("Loading trips\n");
	TRIPS *ptrips = loadTRIPS("./gtfsdata/trips.txt");
	printf("Loading cdlinhav\n");
	CDLINHAV* pcdlinhav = loadCDLINHAV("./gtfsdata/v_linha.txt",pshapes,ptrips);


	char *strdir = "./bus";
	char thefile[100];


	int buscount=0;  
	int nbus = dirFiles(strdir); 
	if(nbus ==0){
		printf("Diretorio %s vazio\n",strdir);
		exit(1);	
	}

	DIR *mydir = opendir(strdir);

	if(!mydir){
		printf("Diretorio nao encontrado\n");
		exit(1);	
	}

	struct dirent *entry = NULL;
      	

	//analisa("./bus/44596",pcdlinhav);

	//analisa("./bus/40032",pcdlinhav);

	analisa("./bus/10972",pcdlinhav);


	//analisa("./bus/42278",pcdlinhav);


	/*
	while((entry = readdir(mydir))) {
		if(entry->d_type != DT_REG)
			continue;		
		
		sprintf(thefile,"%s/%s",strdir,entry->d_name);

		//analisa(thefile,pcdlinhav);
		analisaLinha(thefile,pcdlinhav);
	
		buscount++;
		barraProgresso(buscount,nbus);

		
	}
	*/

	closedir(mydir);


	printf("FIM \n");
	return 0;

}


