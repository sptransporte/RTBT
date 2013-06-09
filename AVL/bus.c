//gcc -lm -std=gnu99 -D_FILE_OFFSET_BITS=64  bus.c 

#include "avl.h"
#include "sptrans.h"

typedef struct {
	char strline[MAX_SIZE_LINE];
	time_t enttime;
} avlreg;

typedef struct {
	avlreg *array;
	int size;
} avlregArray;

void loadBusFile(char* file ,avlregArray *pavlarray){

	pavlarray->size=0;
	pavlarray->array=NULL;

	FILE *fp= fopen(file,"rb");
	
	if(!fp){
		printf("ARQUIVO NULO %s\n",file);
		exit(0);	
	}

	char *line_read = malloc(MAX_SIZE_LINE*sizeof(char));
	int ret;

	while(fgets(line_read,MAX_SIZE_LINE,fp) ){

		pavlarray->size++;
		pavlarray->array = realloc(pavlarray->array,sizeof(avlreg)*(pavlarray->size));
		strcpy(pavlarray->array[pavlarray->size -1].strline,line_read);
		
		char *lineid_str 	= strtok(line_read, ",");
		char *data_str 		= strtok(NULL, ",");
		char *lat_str 		= strtok(NULL, ",");
		char *lon_str 		= strtok(NULL, ",");

	
		if(ret = createTime_strtok(data_str,&(pavlarray->array[pavlarray->size -1].enttime))){
			printf("ERRO createTime_strtok em %s  ret=%d\n",data_str,ret);
			exit(1);
		}

	}	

	if(pavlarray->size == 0){
		printf("ARQUIVO VAZIO %s\n",file);
		exit(0);
	}

	free(line_read);
	fclose(fp);

}

void swapAvlArrayIdx(avlregArray *pavlarray,int idx1,int idx2){
	char strline[MAX_SIZE_LINE];
	time_t enttime;

	enttime=pavlarray->array[idx1].enttime;
	strcpy(strline,pavlarray->array[idx1].strline);

	pavlarray->array[idx1].enttime=pavlarray->array[idx2].enttime;
	strcpy(pavlarray->array[idx1].strline,pavlarray->array[idx2].strline);

	pavlarray->array[idx2].enttime=enttime;
	strcpy(pavlarray->array[idx2].strline,strline);

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
		fprintf(fp,"%s",avlarray.array[i].strline);

	fclose(fp);
	avlarray.size=0;
	free(avlarray.array);
}


void checkSeqData(char* file){

	FILE *fp= fopen(file,"rb");
	
	if(!fp){
		printf("checkSeqData ARQUIVO NULO %s\n",file);
		exit(0);	
	}
	char *line_read = malloc(MAX_SIZE_LINE*sizeof(char));
	int ret;

	time_t tant=-1;
	time_t tago;

	COORD cor,corant;

	while(fgets(line_read,MAX_SIZE_LINE,fp) ){

		char *lineid_str 	= strtok(line_read, ",");
		char *data_str 		= strtok(NULL, ",");
		char *lat_str 		= strtok(NULL, ",");
		char *lon_str 		= strtok(NULL, ",");

		
	
		if(ret = createTime_strtok(data_str,&tago)){
			printf("ERRO createTime_strtok em %s  ret=%d\n",data_str,ret);
			exit(1);
		}



		if(ret = getDstr(lat_str,&cor.lat)){
			printf("Erro conv lat_str %s  %d\n",lat_str,ret);
			exit(1);
		}

		trim(lon_str);
		
		if(ret = getDstr(lon_str,&cor.lon)){
			printf("Erro conv lon_str %s  %d\n",lon_str,ret);
			exit(1);
		}


		if(tant!=-1){
			
			double dts = difftime(tago,tant);

			if(dts<0.0){
				printf("Erro sequencia de data in %s \n",file);
				printf("ant %s \n",ctime(&tant));
				printf("agr %s \n",ctime(&tago));
				exit(1);
			}

			double dtm = dist(&corant,&cor);
			printf("%f  %f\n",dtm/dts,3.6*dtm/dts);

		}

		corant.lat=cor.lat;
		corant.lon=cor.lon;
		tant =tago;

	}

	free(line_read);
	fclose(fp);
}

int main(){
	printf("INICIO\n");

	printf("size %d %d\n",sizeof(time_t),sizeof(int));


	char *strdir = "./bus";
	char thefile[100];


	int porc=-1;
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
		//sortfile(thefile);
		checkSeqData(thefile);
	
		buscount++;
		if( round(100.0*buscount/nbus) > porc){
			porc=round(100.0*buscount/nbus);
			fprintf(stderr, "\r|%d|",porc);
		}
		
	}

	closedir(mydir);


	printf("FIM\n");
	return 0;

}
