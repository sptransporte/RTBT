//gcc -std=gnu99 -lm  gtfs.c 
#include "gtfs.h"


int main(){
	printf("GTFS - Data Validation \n");


	

	STOPS *pstops = loadSTOPS("./gtfsdata/stops.txt");

	/*

	printf("XX %d \n",pstops->size);
	for(int i=0;i<pstops->size;i++){
		//printf("%s\n",pstops->stops[i].name);
		//printf("%ld,\"%s\',\"%s\",%lf,%lf\n",pstops->stops[i].stopid,pstops->stops[i].name,pstops->stops[i].desc,pstops->stops[i].coord.lat,pstops->stops[i].coord.lon);

	}
	*/

	

	SHAPES *pshapes = loadSHAPES("./gtfsdata/shapes.txt");
/*

	int count=0;
	for(int i=0;i<pshapes->size;i++){
		count= count + pshapes->shapes[i].size;
//		if(pshapes->shapes[i].shapeid == 17852){
//			criaHTMLTeste(pshapes->shapes[i].shape, pshapes->shapes[i].size,pshapes->shapes[i].shape, pshapes->shapes[i].size);
//		}
	}
	printf("XXX %d    %d\n",pshapes->size,count);
*/




	TRIPS *ptrips = loadTRIPS("./gtfsdata/trips.txt");

/*
	//USD U__ US_

	printf("XXXX %d    \n",ptrips->size);	

	for(int i=0;i<ptrips->size;i++){
		TRIPIDS *tripids = &(ptrips->tripids[i]);

		//printf("%s\n",tripids->routeid);

		for(int j=0;j < tripids->size;j++){
			
			TRIP *mytrip =&(tripids->mytrips[tripids->size -1]);
			//printf("%s %s %s %s %d %ld\n",tripids->routeid,mytrip->serviceid,mytrip->tripid,mytrip->headsign,mytrip->directionid,mytrip->shapeid);
			printf("%s\n",mytrip->serviceid);
		}
	}

*/

	STOPTIMES *pstoptimes = loadSTOPTIMES("./gtfsdata/stop_times.txt");
/*
	printf("XXXX %d    \n",pstoptimes->size);

	for(int i=0;i<pstoptimes->size;i++){
		STOPTIME *pstoptime = &(pstoptimes->stoptimes[i]);
		printf("%s\n",pstoptime->tripid);
		for(int j=0; j<pstoptime->size; j++){
			//printf("%ld\n",pstoptime->stopids[j]);
		}
	}
*/


	

	printf("Loading trips\n");


	VIAGENS* pviagens = loadVIAGENS(pstops,pstoptimes,pshapes,ptrips);

	
	

	for(int i=0;i<pviagens->size;i++){
		VIAGEM* pviagem=&(pviagens->viagens[i]);

		for(int i=0;i<pviagem->sizestops;i++){
			double x =  distminalinha(&(pviagem->coordstops[i]),pviagem->shape,pviagem->sizeshape);
			if(x > 10.0){
				printf("routeid \t %s \t tripid \t %s \t %s \t %lf\n",pviagem->routeid,pviagem->tripid,pviagem->headsign,x);

				//criaHTMLTeste(pviagem->shape, pviagem->sizeshape,pviagem->coordstops, pviagem->sizestops);
				//exit(1);
			}

		}

	}


	printf("END\n");
}

