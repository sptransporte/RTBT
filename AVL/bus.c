//gcc -lm -std=gnu99 -D_FILE_OFFSET_BITS=64  bus.c 

#include "sptrans.h"
#include "avl.h"



void swapAvlArrayIdx(avlregArray *pavlarray,int idx1,int idx2){

	int lineid;
	time_t enttime;
	double lat;
	double lon;

	lineid=pavlarray->array[idx1].lineid;
	enttime=pavlarray->array[idx1].enttime;
	lat=pavlarray->array[idx1].p.lat;
	lon=pavlarray->array[idx1].p.lon;


	pavlarray->array[idx1].lineid=pavlarray->array[idx2].lineid;
	pavlarray->array[idx1].enttime=pavlarray->array[idx2].enttime;
	pavlarray->array[idx1].p.lat=pavlarray->array[idx2].p.lat;
	pavlarray->array[idx1].p.lon=pavlarray->array[idx2].p.lon;


	pavlarray->array[idx2].lineid=lineid;
	pavlarray->array[idx2].enttime=enttime;
	pavlarray->array[idx2].p.lat=lat;
	pavlarray->array[idx2].p.lon=lon;


}


   // left is the index of the leftmost element of the subarray
   // right is the index of the rightmost element of the subarray (inclusive)
   //   number of elements in subarray = right-left+1
int qs_partition(avlregArray *pavlarray, int left, int right, int pivotIndex){

	time_t pivotValue = pavlarray->array[pivotIndex].enttime;

	swapAvlArrayIdx(pavlarray,pivotIndex,right);// Move pivot to end	

	int storeIndex = left;
	for(int i=left; i< right; i++){
		if( difftime(pavlarray->array[i].enttime,pivotValue) < 0.0 ){
			swapAvlArrayIdx(pavlarray,i,storeIndex);
			storeIndex = storeIndex + 1;
		}
	}

	swapAvlArrayIdx(pavlarray,storeIndex,right);// Move pivot to its final place
	return storeIndex;
}	

void quicksortBusFile(avlregArray *pavlarray, int left, int right){
 
	// If the list has 2 or more items
	if (left < right){
 
		// See "Choice of pivot" section below for possible choices
		//choose any pivotIndex such that left ≤ pivotIndex ≤ right
		int pivotIndex = left + (right-left)/2;
	 
		// Get lists of bigger and smaller items and final position of pivot
		int pivotNewIndex = qs_partition(pavlarray, left, right, pivotIndex);
	 
		// Recursively sort elements smaller than the pivot
		quicksortBusFile(pavlarray, left, pivotNewIndex - 1);
	 
		// Recursively sort elements at least as big as the pivot
		quicksortBusFile(pavlarray, pivotNewIndex + 1, right);
	}
}

void sortfile(char* file){

	avlregArray avlarray;
	loadBusFile(file ,&avlarray);

	quicksortBusFile(&avlarray, 0, avlarray.size -1);


	FILE *fp = fopen(file,"w");
	if(!fp){
		printf("sortfile ARQUIVO erro %s\n",file);
		exit(0);	
	}

	for(int i=0; i< avlarray.size ; i++)
		writeAvlEventToFile(fp,avlarray.array[i].lineid,avlarray.array[i].enttime,avlarray.array[i].p.lat,avlarray.array[i].p.lon);

	fclose(fp);
	avlarray.size=0;
	free(avlarray.array);//FREE
}


void checkSeqData(char* file){

	FILE *fp= fopen(file,"rb");
	
	if(!fp){
		printf("checkSeqData ARQUIVO NULO %s\n",file);
		exit(0);	
	}
	avlreg ravlreg;
	int ret;

	time_t tant=-1;
	time_t tago;

	COORD cor,corant;

	while(readAvlEventFromFile(fp,&(ravlreg.lineid),&(ravlreg.enttime),&(ravlreg.p.lat),&(ravlreg.p.lon))){

		tago =  ravlreg.enttime;
		cor.lat=ravlreg.p.lat;
		cor.lon=ravlreg.p.lon;


		if(tant!=-1){
			
			double dts = difftime(tago,tant);

			if(dts<0.0){
				printf("Erro sequencia de data in %s \n",file);
				printf("ant %s \n",ctime(&tant));
				printf("agr %s \n",ctime(&tago));
				exit(1);
			}

			double dtm = dist(&corant,&cor);
			//printf("%f  %f\n",dtm/dts,3.6*dtm/dts);

		}

		corant.lat=cor.lat;
		corant.lon=cor.lon;
		tant =tago;

	}

	fclose(fp);
}

int main(){
	printf("INICIO\n");

	printf("size %d %d\n",sizeof(time_t),sizeof(int));


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
      	
    
	while((entry = readdir(mydir))) {
		if(entry->d_type != DT_REG)
			continue;		
		
		sprintf(thefile,"%s/%s",strdir,entry->d_name);

		//printf("%s  %d	%s\n ", entry->d_name,entry->d_type,thefile);
		sortfile(thefile);
		checkSeqData(thefile);
	
		buscount++;
		barraProgresso(buscount,nbus);

		
	}

	closedir(mydir);


	printf("FIM\n");
	return 0;

}
