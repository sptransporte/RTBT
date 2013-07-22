
#include <stdio.h>
#include <stdarg.h>

//[1 5599] linha id 
// 1333 linhas Sem Caixa Alta


#define true 1
#define false 0
#define DAL 20.0
#define DTERM 300.0
#define DSTOPED 20.0

enum 
  { 
	sem_trajeto_cadastrado,	//0
	afastado_das_linhas,	//1
	proximo_TA,		//2	
	proximo_TB,		//3	
	proximo_linha_0,	//4	
	proximo_linha_1,	//5
	proximo_ambas_linhas,	//6


/*
	avl_erro,

	desconhecido,
	indeterminado,
	fora_da_linha,
	estaem_TA,
	sgpara_TA,
	estaem_TB,
	sgpara_TB,
    	
	sem_trajeto,
	emlinha_low,
	emlinha_high,
	empontocomum,		

*/
	
 };


void notify_AVL_error(char* origem, const char* format, ... ){

  va_list arglist;

  printf("%s,",origem);
  va_start( arglist, format );
  vprintf( format, arglist );
  va_end( arglist );
  printf("\n");

}


typedef struct {
	int	idx; 	//idx, se <0 sem info da linha
	double dmax; 	//distancia máxima do percurso
	double dpe;	//distancia percorrida 
	double dal;     //distancia a linha
	double dati; 	//distancia ao terminal inicial
	double datf;	//distancia ao terminal final
} linha_sent_info_evt;

void set_linha_sent_info_evt(linha_sent_info_evt *lsie,COORD *pp,SHAPE* shape){
	lsie->idx=-1;
	if(!shape)
		return;


	lsie->dmax=shape->dst[shape->size -1];
	lsie->idx = distminalinha2(pp,shape->shape,shape->size,&(lsie->dpe),&(lsie->dal));	

	//ALTO CUSTO COMPUTACIONAL
	lsie->dati = dist(pp,&(shape->shape[0]));
	lsie->datf = dist(pp,&(shape->shape[shape->size -1]));
	
}


int get_status_from_linha_sent_info_evt(linha_sent_info_evt *lsie, char isL){

	if( lsie->dal > DAL ){
		
		if(lsie->dati < DTERM){
			if(isL)
			  return proximo_TA;
				
			return proximo_TB;
			}
			
			
		if(lsie->datf < DTERM){
			if(isL)
			  return proximo_TB;
				
			return proximo_TA;
		}

		return afastado_das_linhas;
	}

	if( lsie->dpe < DTERM ){
		if(isL)
		  return proximo_TA;
				
		return proximo_TB;
	}

	if( lsie->dpe > lsie->dmax - DTERM  ){
		if(isL)
		  return proximo_TB;
				
		return proximo_TA;
	}
	
	if(isL)
		return proximo_linha_0;
				
	return proximo_linha_1;
}


typedef struct {
	int nsent;			//numero de sentidos 0-2
	linha_sent_info_evt vlinfo[2];	//info da linha em cada sentido
	int status_linha_evt;		//status estimado da linha
} linha_info_evt;


void set_linha_info_evt(linha_info_evt *lie,COORD *pp,SHAPE* shapeL,SHAPE* shapeH){

	set_linha_sent_info_evt(&(lie->vlinfo[0]),pp,shapeL);
	set_linha_sent_info_evt(&(lie->vlinfo[1]),pp,shapeH);

	if(lie->vlinfo[0].idx < 0 && lie->vlinfo[1].idx < 0){
		lie->nsent=0;
		lie->status_linha_evt = sem_trajeto_cadastrado;
		notify_AVL_error("set_linha_info_evt","LINHA SEM TRAJETO CADASTRADO");
		return;
	}

	if(lie->vlinfo[0].idx >= 0 && lie->vlinfo[1].idx < 0){
		lie->nsent=1;
		lie->status_linha_evt = get_status_from_linha_sent_info_evt(&(lie->vlinfo[0]),true);
		return;
	}

	if(lie->vlinfo[0].idx < 0 && lie->vlinfo[1].idx >= 0){
		lie->nsent=0;
		lie->status_linha_evt = sem_trajeto_cadastrado;
		notify_AVL_error("set_linha_info_evt","LINHA APENAS COM HIGH DESCOSIDERAR TRAJETO");
		return;
	}


	lie->nsent=2;
	int stsll = get_status_from_linha_sent_info_evt(&(lie->vlinfo[0]),true);	
	int stslh = get_status_from_linha_sent_info_evt(&(lie->vlinfo[1]),false);	

	if(stsll == stslh){
		lie->status_linha_evt = stsll;
		return;
	}
	

	if( (stsll == proximo_TA || stsll == proximo_TB) &&	
	    (stslh != proximo_TA && stslh != proximo_TB) ){
		lie->status_linha_evt = stsll;
		return;
	}


	if( (stslh == proximo_TA || stslh == proximo_TB) &&	
	    (stsll != proximo_TA && stsll != proximo_TB) ){
		lie->status_linha_evt = stslh;
		return;
	}

	if( (stsll == proximo_TA && stslh == proximo_TB) ||	
	    (stsll == proximo_TB && stslh == proximo_TA) ){

		notify_AVL_error("set_linha_info_evt","PROXIMO DE TA E TB AO MESMO TEMPO");
		lie->status_linha_evt = afastado_das_linhas;
		return;
	}	


	if(stsll == proximo_linha_0 && stslh == proximo_linha_1){
		lie->status_linha_evt = proximo_ambas_linhas;
		return;
	}
	

	if(stsll == proximo_linha_0 && stslh != proximo_linha_1){
		lie->status_linha_evt = stsll;
		return;
	}

	if(stsll != proximo_linha_0 && stslh == proximo_linha_1){
		lie->status_linha_evt = stslh;
		return;
	}


		
	notify_AVL_error("set_linha_info_evt","ESTADO NAO IDENTIFICADO  %d %d",stsll,stslh);
	lie->status_linha_evt = afastado_das_linhas;
	return;	
	
}


typedef struct {
  	time_t time;			//data em que evento chegou
	int cd_linha;			//código de linha enviado pelo AVL
	int ll;				//cód linha low
	char isl;			//se linha indicada é low
	COORD pos;			//coordenada posicional enviado pelo AVL

	int status_linha_evt;	    //status estimado da linha

	//PARA GUARDAS DADOS DE BAIXO NIVEL
  	linha_info_evt  linfoevt;   //Informacao da linha calculada	

} avl_evento_info;

void set_avl_evento_info(avl_evento_info *pavlei,time_t time,int cd_linha,COORD pos , SHAPE* shapeL, SHAPE* shapeH){
		pavlei->time=time;
		pavlei->cd_linha=cd_linha;
		pavlei->ll  = (pavlei->cd_linha)&(~0x8000);
		pavlei->isl = (pavlei->cd_linha < 0x8000);
		pavlei->pos=pos;

		set_linha_info_evt(&(pavlei->linfoevt),&(pavlei->pos),shapeL,shapeH);

		pavlei->status_linha_evt=pavlei->linfoevt.status_linha_evt;
}


void confrontoDeEventosConsecutivos(avl_evento_info *pavlei_ant, avl_evento_info *pavlei, char *pinit,CDLINHAV* pcdlinhav){

	


	if(pavlei->status_linha_evt == sem_trajeto_cadastrado){
		//SEM TRAJETO CADASTRADO, RESET
		*pinit=false;
		return;
	}


	if(pavlei->ll != pavlei_ant->ll){
		//ONIBUS MUDOU DE LINHA, RESET
		*pinit=false;
		return;
	}

/*

	if(pavlei_ant->status_linha_evt != afastado_das_linhas  && pavlei->status_linha_evt == afastado_das_linhas){
		//ESTAVA NA LINHA E AGORA ESTA FORA// DESCARTAR POR HORA, pode ser erro de leitura do AVL
		*pinit=false;
		return;
	}

	if(pavlei_ant->status_linha_evt == afastado_das_linhas  && pavlei->status_linha_evt == afastado_das_linhas){
		//ESTA FORA DA LINHA, NÃO INTERESSA ,POR ENQUANTO
		*pinit=false;
		return;
	}


	if(pavlei_ant->status_linha_evt == afastado_das_linhas  && pavlei->status_linha_evt != afastado_das_linhas){
		//ESTAVA FORA E AGORA ENTROU NA LINHA
		return;
	}

*/

	double dt = difftime(pavlei->time,pavlei_ant->time);
	double ds = dist(&(pavlei->pos),&(pavlei_ant->pos));

	if( dt < 0.0){
		printf("TEMPO NEGATIVO  dt %lf ds %lf  \n",dt,ds);
		exit(0);
	}


//	if( dt < 10.0){
//		//EVENTO COM CURTO ESPACO DE TEMPO, MAS DENTRO DA LINHA
//		*pinit=false;
//		return ;
//	}

	//printf("%s",ctime(&(pavlei->time)));

//	if(ds <= DSTOPED){
//		//PARADO, ATUALIZA ULTIMO STATUS 
//		return ;
//	}


	double kmh = 3.6*(ds/dt);	
	if(kmh > 100.0){
		//printf("VELO ABS  dt %lf ds %lf   kmh %lf    %d   %d   %s\n",dt,ds,kmh,pavlei_ant->status_linha_evt,pavlei->status_linha_evt,ctime(&(pavlei->time)));
		//reset status
		*pinit=false;
		return ;
	}

	//printf("%s %s \n",pcdlinhav->cdlinhav[pavlei->ll].routeid,pcdlinhav->cdlinhav[pavlei->ll].headsignL);
	
/*
	if(pavlei_ant->status_linha_evt != proximo_TA && pavlei->status_linha_evt == proximo_TA)
		printf("->TA %s",ctime(&(pavlei->time)));

	if(pavlei_ant->status_linha_evt != proximo_TB && pavlei->status_linha_evt == proximo_TB)
		printf("->TB %s",ctime(&(pavlei->time)));


	if(pavlei_ant->status_linha_evt == proximo_TA && pavlei->status_linha_evt != proximo_TA)
		printf("TA-> %s",ctime(&(pavlei->time)));

	if(pavlei_ant->status_linha_evt == proximo_TB && pavlei->status_linha_evt != proximo_TB)
		printf("TB-> %s",ctime(&(pavlei->time)));

*/

	

}

typedef struct {
	int status_onibus;	//status estimado do onibus 
	double deltam_status;	//deslocamento no status atual
	time_t time_status;	//tempo de inicio do status atual	


	double deltam_evta;	//deslocamento entre  evt anterior e ultimo
	double deltas_evta;	//tempo entre evt anterior e ultimo
	time_t time_evt;	//tempo do ultimo evento

	char *linha;		//linha do onibus
	int sentido;		//sentido do onibus
} onibus_info;


void set_onibus_info(onibus_info *poi,avl_evento_info *pavlei_a ,avl_evento_info *pavlei){
	poi->deltam_evta = dist(&(pavlei->pos),&(pavlei_a->pos));
	poi->deltas_evta = difftime(pavlei->time,pavlei_a->time);

	poi->time_evt = pavlei->time;



}

