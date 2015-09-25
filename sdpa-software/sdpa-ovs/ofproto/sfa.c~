/*
 * 	Auhtor : eric
 * 	Description : sfa module
 */


#include "sfa.h"
#include "connmgr.h"

#include "util.h"



//#ifdef __DEBUG__
//#define printf(STR) printf(STR)
//#endif


/*
 * Description		called in sfa_msg_handle procedure, the function will process the sfa init msg
 * 					it will init the status table, status transition table, action table. we now define
 * 					one status table for one application. in future one application can have more than one
 * 					status table, these table will be listed by List. Moreover, we define one status transition
 * 					table and one action table associate with the status table.
 * Input 			ofconn openflow connection
 * 					ofp_header pointer to the openflow header, the data is just behind the header, you can find the
 * 					payload by "pointer to header" + sizeof( struct ofp_header )
 * Output			1	means error in initialize the status table
 * 					0	means the pkt is successfully initialize the status table
 */
enum sfaerr sfa_msg_init(struct ofconn *ofconn, const struct ofp_header *sfah )
{
	printf("enter sfa msg init op!\n");


	int i = 0; //inner use
	char * cursor = NULL;
	uint32_t len = 0 ;


	if( g_apps.islistinit == false )
	{
		list_init(&g_apps.appslist);
		g_apps.islistinit = true;
	}

	//alloc app
	struct CONTROLLAPP* app = xmalloc( sizeof(struct CONTROLLAPP) );
	struct sfa_msg_init_st* pst =(struct sfa_msg_init_st*) ( (char*)sfah+sizeof( struct ofp_header));
	cursor = (char*)pst+sizeof( *pst );
	//set appid
	app->appid = ntohl(pst->aid);

	//alloc st table
	struct STATUS_TABLE* st = xmalloc( sizeof(*st) );
	//init hmap st_entrys
	hmap_init( &st->st_entrys );
	int loop_ct = ntohl(pst->counts);
	//get & set match_bitmap
	st->match_bitmap = ntohll(pst->mmp);

	if( loop_ct > 0 )
	{
		for( ; i < loop_ct ; i++)
			{
				struct ST_MATCH_ENTRY* sme = xmalloc( sizeof(*sme));
				sme->last_status = ntohl(*(uint32_t*)cursor);
				cursor = cursor+sizeof(uint32_t);
				len =ntohl(*(uint32_t*)cursor);
				char* data = xmalloc( len + 2 );
				data[len+1]='\0';
				data[len] ='\0';
				memcpy((void*)data,(void*)(cursor+sizeof(uint32_t)),len);
				sme->data = data;
				cursor = cursor+sizeof(uint32_t)+len;
				printf("add to st stat is : %ld,data is %s \n",sme->last_status,sme->data);
				hmap_insert(&st->st_entrys,&sme->node,hash_string(sme->data,0));
			}
	}

	//cursor point to the stt and alloc stt
	struct STATUS_TRANZ_TABLE* stt = xmalloc( sizeof(*stt));
	list_init(&stt->stt_entrys);
	uint32_t stt_count = ntohl(*(uint32_t*)cursor);
	struct sfa_msg_init_stt* stt_tmp = (struct sfa_msg_init_stt*)(cursor+sizeof(uint32_t));
	i = 0;
	for( ; i < stt_count;i++)
	{
		struct STT_MATCH_ENTRY* sttme = xmalloc( sizeof(*sttme) );
		sttme->cur_status = ntohl(stt_tmp->cur_status);
		sttme->oprator = ntohl(stt_tmp->oprator);
		sttme->param_left = ntohll(stt_tmp->param_left);
		sttme->param_left_type = ntohl(stt_tmp->param_left_type);
		sttme->param_right = ntohll(stt_tmp->param_right);
		sttme->param_right_type = ntohl(stt_tmp->param_right_type);
		sttme->last_status = ntohl(stt_tmp->last_status);
		printf("add to stt leftype: %ld, left param is %lld, right type : %ld, right param is %lld,op : %ld, last: %ld next : %ld \n",
				sttme->param_left_type,sttme->param_left,sttme->param_right_type,sttme->param_right,sttme->oprator,sttme->last_status,sttme->cur_status);
		list_insert(&stt->stt_entrys,&sttme->node);
		stt_tmp++;
	}

	//cur point to the at and alloc at
	cursor = (char*)stt_tmp;
	struct ACTION_TABLE* at = xmalloc( sizeof(*at) );
	hmap_init(&at->at_entrys);
	struct sfa_msg_init_at* at_tmp = (struct sfa_msg_init_at* )cursor;
	loop_ct = ntohl(at_tmp->counts);
	at->bitmap = ntohll(at_tmp->bitmap);
	cursor = cursor+sizeof(struct sfa_msg_init_at);
	i = 0 ;
	for( ; i < loop_ct ; i++)
	{
		struct AT_MATCH_ENTRY* ame = xmalloc( sizeof(*ame));
		ame->act.actype = ntohl( ((struct SFA_ACTION*)cursor)->actype);
		ame->act.acparam = ntohl(((struct SFA_ACTION*)cursor)->acparam);
		ame->last_status = ntohl(*(uint32_t*)(cursor+sizeof( struct SFA_ACTION )));
		len = ntohl(*(uint32_t*)(cursor+sizeof( struct SFA_ACTION )+sizeof(uint32_t)));
		//alloc the data to store data string
		ame->data = xmalloc(len+2);
		ame->data[len-1] = '\0';
		ame->data[len] = '\0';
		memcpy(ame->data,cursor+sizeof(struct SFA_ACTION)+2*sizeof(uint32_t) , len);
		printf("add to at action type : %ld, action param : %ld , last status : %ld,data is %s \n",
				ame->act.actype,ame->act.acparam,ame->last_status,ame->data);
		hmap_insert( &at->at_entrys, &ame->node,hash_string(ame->data,0));
		cursor += sizeof(struct SFA_ACTION)+ 2*sizeof(uint32_t)+len;
	}

	//add st stt at to appcontroll
	app->pat = at;
	app->pst = st;
	app->pstt = stt;

	// insert app into g_apps;
	list_insert(&(g_apps.appslist) , &(app->node));

	return SFA_MSG_PROCESS_OK;


}

enum sfaerr sfa_msg_st_mod(struct ofconn *ofconn, const struct ofp_header *sfah)
{
	printf("enter sfa st mod !\n");

	struct sfa_msg_mod* pmsg = (char*)sfah+sizeof(struct ofp_header);

	uint32_t aid = ntohl(pmsg->appid);

	if(g_apps.islistinit == false )
		return SFA_MSG_PROCESS_ERROR;

	struct CONTROLLAPP* app = NULL;
	bool bfound = false;

	LIST_FOR_EACH(app , node, &g_apps.appslist)
	{
		if( app->appid == aid)
		{
			bfound = true;
			break;
		}
	}

	if( bfound == false )
		return SFA_MSG_PROCESS_ERROR;

	struct STATUS_TABLE* st = app->pst;

	int count = ntohl(pmsg->count);
	char* cursor = (char*)pmsg+2*sizeof(uint32_t);

	int i = 0;
	uint32_t status_tmp = 0 ;
	uint32_t len_tmp = 0 ;
	char * data_tmp = NULL;
	struct ST_MATCH_ENTRY* stmatch_tmp = NULL;
	for( ; i < count;i++){

		int tmt_tmp = ntohl(*( int *)cursor);
		status_tmp = 0 ;
		len_tmp = 0;
		data_tmp= NULL;
		if( tmt_tmp == ENTRY_ADD )
		{
			//do add entry
			status_tmp = ntohl(*(uint32_t*)( cursor+sizeof(uint32_t)));
			len_tmp = ntohl(*(uint32_t*)( cursor+2*sizeof(uint32_t)));
			data_tmp = xmalloc(len_tmp+2);
			data_tmp[len_tmp+1] = '\0';
			data_tmp[len_tmp] = '\0';
			memcpy(data_tmp , cursor+3*sizeof(uint32_t),len_tmp);
			if( hmap_first_with_hash(&(st->st_entrys),hash_string(data_tmp,0)) != NULL)
				{
				printf("BUG--CHECK find dup entry while adding !\n");
				free(data_tmp);
				data_tmp = NULL;
				cursor =cursor+ 3*sizeof(uint32_t)+len_tmp;
				continue;
				}
			stmatch_tmp = xmalloc(sizeof(*stmatch_tmp));
			stmatch_tmp->last_status = status_tmp;
			stmatch_tmp->data = data_tmp;
			printf("st-mod  op is add , is %s ,  status is: %ld",data_tmp,status_tmp);
			hmap_insert(&(st->st_entrys),&(stmatch_tmp->node),hash_string(data_tmp,0));
			cursor =cursor+ 3*sizeof(uint32_t)+len_tmp;

		}else if( tmt_tmp == ENTRY_UPDATE)
		{
			//do update
			status_tmp = ntohl(*(uint32_t*)( cursor+sizeof(uint32_t)));
			len_tmp = ntohl(*(uint32_t*)( cursor+2*sizeof(uint32_t)));
			data_tmp = xmalloc(len_tmp+2);
			data_tmp[len_tmp+1] = '\0';
			data_tmp[len_tmp] = '\0';
			memcpy(data_tmp , cursor+3*sizeof(uint32_t),len_tmp);
			stmatch_tmp = (struct ST_MATCH_ENTRY* )hmap_first_with_hash(&(st->st_entrys),hash_string(data_tmp,0));
			if( stmatch_tmp == NULL)
			{
				printf("BUG--CHECK can not find entry while update st!\n");
				free(data_tmp);
				data_tmp = NULL;
				cursor =cursor+ 3*sizeof(uint32_t)+len_tmp;
				continue;
			}
			printf("st-mod  op is update , data is %s , new status is: %ld",data_tmp,status_tmp);
			stmatch_tmp->last_status = status_tmp;
			cursor =cursor+ 3*sizeof(uint32_t)+len_tmp;

		}else if(tmt_tmp == ENTRY_DEL){
			//do del
			len_tmp = ntohl(*(uint32_t*)( cursor+2*sizeof(uint32_t)));
			data_tmp = xmalloc(len_tmp+2);
			data_tmp[len_tmp+1] = '\0';
			data_tmp[len_tmp] = '\0';
			memcpy(data_tmp , cursor+3*sizeof(uint32_t),len_tmp);
			stmatch_tmp = (struct ST_MATCH_ENTRY* )hmap_first_with_hash(&(st->st_entrys),hash_string(data_tmp,0));
			if( stmatch_tmp == NULL)
			{
				printf("BUG--CHECK can not find entry while del st!\n");
				free(data_tmp);
				data_tmp = NULL;
				cursor =cursor+ 3*sizeof(uint32_t)+len_tmp;
				continue;
			}
			printf("st-mod  op is del , data is %s",data_tmp);
			hmap_remove(&(st->st_entrys),stmatch_tmp);
			cursor =cursor+ 3*sizeof(uint32_t)+len_tmp;

		}else
		{
			printf("BUG-CHECK! invalid st mode type!\n");
			break;
		}

	}

	printf("leaving st mod !\n");

}

enum sfaerr sfa_msg_at_mod(struct ofconn *ofconn, const struct ofp_header *sfah)
{
	printf("enter sfa at mod !\n");

		struct sfa_msg_mod* pmsg = (char*)sfah+sizeof(struct ofp_header);
		uint32_t aid = ntohl(pmsg->appid);

		if(g_apps.islistinit == false )
			return SFA_MSG_PROCESS_ERROR;

		struct CONTROLLAPP* app = NULL;
		bool bfound = false;

		LIST_FOR_EACH(app , node, &g_apps.appslist)
		{
			if( app->appid == aid)
			{
				bfound = true;
				break;
			}
		}

		if( bfound == false )
			return SFA_MSG_PROCESS_ERROR;

		struct ACTION_TABLE* at = app->pat;

		uint32_t count = ntohl(pmsg->count);
		char* cursor = (char*)pmsg+2*sizeof(uint32_t);

		int i = 0;
		uint32_t status_tmp = 0 ;
		uint32_t len_tmp = 0 ;
		char * data_tmp = NULL;
		struct AT_MATCH_ENTRY* atmatch_tmp = NULL;
		struct SFA_ACTION* sact_tmp = NULL;
		for( ; i < count;i++){

			uint32_t tmt_tmp = *(uint32_t*)cursor;
			status_tmp = 0 ;
			len_tmp = 0;
			data_tmp= NULL;
			if( tmt_tmp == ENTRY_ADD )
			{
				//do add entry
				sact_tmp = (struct SFA_ACTION*)(cursor+sizeof(uint32_t));
				status_tmp = ntohl(*(uint32_t*)( cursor+sizeof(uint32_t)+ sizeof(struct SFA_ACTION)));
				len_tmp = ntohl(*(uint32_t*)( cursor+sizeof(struct SFA_ACTION)+2*sizeof(uint32_t)));
				data_tmp = xmalloc(len_tmp+2);
				data_tmp[len_tmp+1] = '\0';
				data_tmp[len_tmp] = '\0';
				memcpy(data_tmp , cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t),len_tmp);
				//data_tmp = cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t);
				if( hmap_first_with_hash(&(at->at_entrys),hash_string(data_tmp,0)) != NULL)
					{
					printf("BUG--CHECK find dup entry while adding Action Table !\n");
					free(data_tmp);
					data_tmp=NULL;
					cursor = cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t)+len_tmp;
					continue;
					}
				atmatch_tmp = xmalloc(sizeof(*atmatch_tmp));
				atmatch_tmp->last_status = status_tmp;
				atmatch_tmp->act.actype = ntohl(sact_tmp->actype);
				atmatch_tmp->act.acparam = ntohl(sact_tmp->acparam);
				atmatch_tmp->data = data_tmp;
				printf("at-mod  op is add , new data is %s , new status is: %ld,new actype : %ld , new actparam : %ld",
						atmatch_tmp->data,atmatch_tmp->last_status,atmatch_tmp->act.actype,atmatch_tmp->act.acparam);
				hmap_insert(&(at->at_entrys),&(atmatch_tmp->node),hash_string(data_tmp,0));
				cursor = cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t)+len_tmp;

			}else if( tmt_tmp == ENTRY_UPDATE)
			{
				//do update
				sact_tmp = (struct SFA_ACTION*)(cursor+sizeof(uint32_t));
				status_tmp = ntohl(*(uint32_t*)( cursor+sizeof(uint32_t)+ sizeof(struct SFA_ACTION)));
				len_tmp = ntohl(*(uint32_t*)( cursor+sizeof(struct SFA_ACTION)+2*sizeof(uint32_t)));
				data_tmp = xmalloc(len_tmp+2);
				data_tmp[len_tmp+1] = '\0';
				data_tmp[len_tmp] = '\0';
				memcpy(data_tmp , cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t),len_tmp);
				//data_tmp = cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t);
				atmatch_tmp = (struct AT_MATCH_ENTRY* )hmap_first_with_hash(&(at->at_entrys),hash_string(data_tmp,0));
				if( atmatch_tmp == NULL)
				{
					printf("BUG--CHECK can not find entry while update Action Table!\n");
					free(data_tmp);
					data_tmp=NULL;
					cursor = cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t)+len_tmp;
					continue;
				}
				printf("at-mod  op is update , data is %s , new status is: %ld,new actype : %ld , new actparam : %ld",
						data_tmp,status_tmp,ntohl(sact_tmp->actype),ntohl(sact_tmp->acparam));
				atmatch_tmp->last_status = status_tmp;
				atmatch_tmp->act.acparam = ntohl(sact_tmp->acparam);
				atmatch_tmp->act.actype = ntohl(sact_tmp->actype);
				cursor = cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t)+len_tmp;

			}else if(tmt_tmp == ENTRY_DEL){
				//do del
				//data_tmp = cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t);
				len_tmp = ntohl(*(uint32_t*)( cursor+sizeof(struct SFA_ACTION)+2*sizeof(uint32_t)));
				data_tmp = xmalloc(len_tmp+2);
				data_tmp[len_tmp+1] = '\0';
				data_tmp[len_tmp] = '\0';
				memcpy(data_tmp , cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t),len_tmp);
				atmatch_tmp = (struct AT_MATCH_ENTRY*)hmap_first_with_hash(&at->at_entrys,hash_string(data_tmp,0));
				if( atmatch_tmp == NULL)
				{
					printf("BUG--CHECK can not find entry while del Action Table!\n");
					free(data_tmp);
					data_tmp=NULL;
					cursor = cursor+sizeof(struct SFA_ACTION)+3*sizeof(uint32_t)+len_tmp;
					continue;
				}
				printf("at-mod  op is del , data is %s ",data_tmp);
				hmap_remove(&(at->at_entrys),&(atmatch_tmp->node));

			}else
			{
				printf("BUG-CHECK! invalid at mode type!\n");
				continue;
			}

		}

		printf("leaving at mod !\n");


}

/*
 * Description		called in handle_openflow__ procedure, the function first check the msg type
 * 					if it is sfamsg , then process the msg else it returns . The error of sfaerror
 * 					type will not pass out, it will only be used by inner function to do self-check.
 * Input 			ofconn openflow connection
 * 					ofpbuf the puf point to the openflow msg
 * Output			1	means the pkt should be handled by original openflow procedure
 * 					0	means the pkt is successfully handled by sfa module
 */
int sfa_handle_pkt(struct ofconn *ofconn, const struct ofpbuf *msg)
{
		const struct ofp_header *oh = msg->data;
	    enum sfaerr error;

	    //printf("In sfa_handle_pkt! type is %d \n",oh->type);

	    printf("----type is %d----\n",oh->type);

	    switch( oh->type )
	    {
	    case OFPTYPE_SFA_TABLE_CREATE:
	    	error = sfa_msg_init(ofconn,oh);
	    	break;
	    case OFPTYPE_SFA_ST_ENTRY_MOD:
	    	error = sfa_msg_st_mod(ofconn,oh);
	    	break;
	    case OFPTYPE_SFA_AT_ENTRY_MOD:
	    	error = sfa_msg_at_mod(ofconn,oh);
	    	break;
	    default:
	    	//printf("non-sfa msg return to openflow!\n");
	    	return -1 ;
	    }

	    if( error == 0 )
	    {
	    	printf("successfully handle the sfa msg\n");
	    	return 0;
	    }else
	    {
	    	printf("###BUG-CHECK### fail to  handle the sfa msg\n");
	    	return -1;
	    }



}


