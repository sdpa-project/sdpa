/*
 * 	Module Name : sdpa module
 * 	Description :
 * 	Author 		: eric
 * 	Date		:2014
 *
 */


#ifndef SDPA_H_ERIC
#define SDPA_H_ERIC 1

#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>

#include "list.h"
#include "ofpbuf.h"
#include "util.h"
#include "hmap.h"
#include "ofp-msgs.h"




#define MAX_ENTRY 500

enum sdpaerr{
	SDPA_MSG_PROCESS_OK,
	SDPA_MSG_PROCESS_ERROR,
};

/* SDPA extension. */
enum sdpatype{

	OFPTYPE_SDPA_TABLE_CREATE = 90,	/* sdpa_craete_table msg type*/
	OFPTYPE_SDPA_ST_ENTRY_MOD = 91,	/* sdpa_st_entry_mode msg type*/
	OFPTYPE_SDPA_AT_ENTRY_MOD = 92,	/* sdpa_at_entry_mode msg type*/

};



// table set is the table structure in ovs

//****** STATUS TARANZ TABLE SET *****************************/
enum PARAM_TYPE
{
	PARAM_NON = 0,
	SDPAPARAM_IN_PORT     = 1 ,  /* Switch input port. */
	SDPAPARAM_DL_VLAN     = 2,  /* VLAN id. */
	SDPAPARAM_DL_VLAN_PCP = 3,  /* VLAN priority. */
	SDPAPARAM_DL_TYPE     = 4,  /* Ethernet frame type. */
	SDPAPARAM_NW_TOS      = 5,  /* IP ToS (DSCP field, 6 bits). */
	SDPAPARAM_NW_PROTO    = 6,  /* IP protocol. */
	SDPAPARAM_TP_SRC      = 7,  /* TCP/UDP/SCTP source port. */
	SDPAPARAM_TP_DST      = 8,  /* TCP/UDP/SCTP destination port. */
	SDPAPARAM_TP_FLAG	 = 9,  /* TCP FLAG  */
	SDPAPARAM_DL_SRC      = 10, /* Ethernet source address. */
	SDPAPARAM_DL_DST      = 11, /* Ethernet destination address. */
	SDPAPARAM_NW_SRC      = 12, /* IP source address. */
	SDPAPARAM_NW_DST      = 13, /* IP destination address. */
	SDPAPARAM_METADATA    = 14, /* Upper level data	*/
	SDPAPARAM_CONST		 = 15,
} ;
enum OPRATOR
{
	OPRATOR_NON = 0,
	OPRATOR_ISEQUAL,	/* = */
	OPRATOR_ADD,		/* + */
	OPRATOR_SUB, 		/* - */
	OPRATOR_BITAND,		/* & */
	OPRATOR_BITOR, 		/* | */
	OPRATOR_BITWISE,	/* ^ */
	OPRATOR_GREATER,    /* > */
	OPRATOR_LESS,		/* < */
	OPRATOR_EQUALGREATER,	/* >= */
	OPRATOR_EQUALLESS,		/* <= 8 */
};

struct STT_MATCH_ENTRY{
	struct list node;
	enum PARAM_TYPE param_left_type;
	uint64_t param_left;
	enum OPRATOR oprator;
	enum PARAM_TYPE param_right_type;
	uint64_t param_right;
	uint32_t last_status;
	uint32_t cur_status;
} ;
struct STATUS_TRANZ_TABLE
{
	struct list stt_entrys; //store STT_MATCH_ENTRYs
};
//******** STATUS TARANZ TABLE SET  END *********************



//********** ACTION TABLE SET ******************************/
//SDPA_ACTION IS ENUM+PARAM
//AT_MATCH_ENTRY is like ST_MATCH_ENTRY
struct SDPA_ACTION
{
	enum SDPA_ACTION_TYPE
	{
        SAT_NON,
		SAT_OUTPUT,
		SAT_DROP,
		SAT_TOOPENFLOW,
	} actype;
	uint32_t acparam;
};
struct AT_MATCH_ENTRY{
	struct hmap_node node;
	char* data;
	uint32_t last_status;
	struct SDPA_ACTION act;
};

struct ACTION_TABLE
{
	uint64_t bitmap;
	struct hmap at_entrys;  // store AT_MATCH_ENTRYS

};
//********** ACTION TABLE SET END **************************/



//*********** STATUS TABLE SET**************************/
//st table entry use hmap_node to store hash info
// hmap is stored in st, one st has one hmap

struct ST_MATCH_ENTRY{
	struct hmap_node node;
	char* data;
	uint32_t last_status;
};

struct STATUS_TABLE{
	uint64_t match_bitmap;
	struct hmap st_entrys;	// store ST_MATCH_ENTRYs
};
//*********** STATUS TABLE SET END *********************/


//************ CONTROLLERAPP SET ***********************/
struct CONTROLLAPP
{
	struct list node;
	uint32_t appid;
	struct STATUS_TABLE* pst;
	struct STATUS_TRANZ_TABLE* pstt;
	struct ACTION_TABLE* pat;

};

//************* CONTROLLERAPP SET END ********************/


struct APPS
{
	//private vars
	bool islistinit;
	struct list appslist; //point to CONTROLLAPPs
}g_apps;


//************  MSG PROTOCOL *******************************


struct sdpa_msg_init_st{
	uint32_t aid;
	uint64_t mmp;
	uint32_t counts;
	//uint32_t lenth;
	//byte[] data;

//	if counts is 0 means no st entry else the next ele is
//	#counts of struct data{
//		uint32_t lenth;
//		byte[] data}

}; //align to 8 BYTE

/*
struct sdpa_msg_init_stt{
	enum PARAM_TYPE param_left_type;
	uint64_t param_left;
	enum PARAM_TYPE param_right_type;
	uint64_t param_right;
	enum OPRATOR oprator;
	uint32_t last_status;
	uint32_t cur_status;
};

struct sdpa_msg_init_stt{
	uint32_t counts;

*/
struct sdpa_msg_init_stt
{
	enum PARAM_TYPE param_left_type;
	uint64_t param_left;
	enum PARAM_TYPE param_right_type;
	uint64_t param_right;
	enum OPRATOR oprator;
	uint32_t last_status;
	uint32_t cur_status;
};


struct sdpa_msg_init_at{
	uint64_t bitmap;
	uint32_t counts;
};
/*
	#counts of structure
	{
	struct SDPA_ACTION act;
	uint32_t last_status;
	uint32_t data_len;
	byte[] data;

	}
}

*/


//  mode type
enum MOD_TYPE
{
	ENTRY_ADD,
	ENTRY_UPDATE, //for action table update means update the action or status, for status table ,update means update status
	ENTRY_DEL,
};


struct sdpa_msg_mod{
	uint32_t appid;
	uint32_t count;
	/*
		#counts of structure if is action table mod
		{
		enum TABLE_MOD_TYPE mod type;
		struct SDPA_ACTION act;
		uint32_t last_status;
		uint32_t data_len;
		byte[] data;
		}
	*/
	/*
		#counts of structure if is status table mod
		{
		enum TABLE_MOD_TYPE mod type;
		uint32_t last_status;
		uint32_t data_len;
		byte[] data;
		}
		*/


};




/*	 sdpa init msg pattern
 *
 *
 *  |--------------------------32bit ------------------------------|
 * 	+--------------------------------------------------------------+
 *  + version(8)   +   type(8)=90 +           length(16)           +
 *  + -------------------------------------------------------------+
 *  +						xid(32)								   +
 *  +--------------------------------------------------------------+
 *  +						appid(32)
 *  +--------------------------------------------------------------+
 *  +				status table.bitmap(32/64)
 *  +--------------------------------------------------------------+
 *  +				status table.bitmap(64/64)
 *  +--------------------------------------------------------------+
 *  + 				status table data.counts(32)
 *  +--------------------------------------------------------------+
 *  + 				if counts != 0 , see struct data{}
 *  +--------------------------------------------------------------+
 *  +            status transition table counter(32)               +
 *  +--------------------------------------------------------------+
 *  + 			    event  left param type (32)
 *  +--------------------------------------------------------------+
 *  +				event left param value(32/64)
 *  +--------------------------------------------------------------+
 *  +				event left param value(64/64)
 *  +--------------------------------------------------------------+
 *  + 			    event right param type (32)
 *  +--------------------------------------------------------------+
 *  +				event right param value(32/64)
 *  +--------------------------------------------------------------+
 *  +				event right param value(64/64)
 *  +--------------------------------------------------------------+
 *  +               event param operator(32)
 *  +---------------------------------------------------------------
 *  +				status transition table.laststatus(32)
 *  +--------------------------------------------------------------+
 *  +               status transition table.nextstatus(32)
 *  +---------------------------------------------------------------
 *  +          .... #(counter-1) of stt entrys.......
 *  +---------------------------------------------------------------
 *  +             action table bitmap(32/64)
 *  +----------------------------------------------------------------
 *  + 			  	action table bitmap(64/64)
 *  +--------------------------------------------------------------+
 *  +				action table.count(32)
 *  +--------------------------------------------------------------+
 *  +			...#(count-1) of at entrys ....
 *  +--------------------------------------------------------------+
 *  + 			    action table.type(32)
 *  +--------------------------------------------------------------+
 *  +             	action table.param(32)
 *  +-----------------------------------------------------------------
 *  +             	action table.laststatus(32)
 *  +-----------------------------------------------------------------
 *  +             	action table.data_len(32)
 *  +-----------------------------------------------------------------
 *  +             	action table.data
 *  +-----------------------------------------------------------------
 *  +             .........................
 *  +-----------------------------------------------------------------

 */
//
//enum sdpaerr sdpa_msg_init(struct ofconn *ofconn, const struct ofp_header *sdpah );
//enum sdpaerr sdpa_msg_st_mod(struct ofconn *ofconn, const struct ofp_header *sdpah);
//enum sdpaerr sdpa_msg_at_mod(struct ofconn *ofconn, const struct ofp_header *sdpah);
//int sdpa_handle_pkt(struct ofconn *ofconn, const struct ofpbuf *msg);
//
//







#endif
