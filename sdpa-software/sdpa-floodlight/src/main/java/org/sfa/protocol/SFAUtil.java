package org.sfa.protocol;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;

import org.openflow.protocol.OFPacketIn;

public class SFAUtil {
	/*
	 * Description		this function will map the match field bitmap to real match fields
	 *                  and put together those fields as a char*
	 *                  here we could use the packet parsing code
	 
			struct flow {                                                                bitmap id
			    // L1 
			    struct flow_tnl tunnel;     // Encapsulating tunnel parameters.          1 << 0
			    ovs_be64 metadata;          // OpenFlow Metadata.                        1 << 1
			    uint32_t regs[FLOW_N_REGS]; // Registers.                                1 << 2
			    uint32_t skb_priority;      // Packet priority for QoS.                  1 << 3
			    uint32_t pkt_mark;          // Packet mark.                              1 << 4
			    union flow_in_port in_port; // Input port.                               1 << 5
			
			    // L2 
			    uint8_t dl_src[6];          // Ethernet source address.                  1 << 6
			    uint8_t dl_dst[6];          // Ethernet destination address.             1 << 7
			    ovs_be16 dl_type;           // Ethernet frame type.                      1 << 8
			    ovs_be16 vlan_tci;          // If 802.1Q, TCI | VLAN_CFI; otherwise 0.   1 << 9
			
			    // L3 
			    ovs_be32 mpls_lse;          // MPLS label stack entry.                   1 << 10
			    struct in6_addr ipv6_src;   // IPv6 source address.                      1 << 11
			    struct in6_addr ipv6_dst;   // IPv6 destination address.                 1 << 12
			    struct in6_addr nd_target;  // IPv6 neighbor discovery (ND) target.      1 << 13
			    ovs_be32 ipv6_label;        // IPv6 flow label.                          1 << 14
			    ovs_be32 nw_src;            // IPv4 source address.                      1 << 15
			    ovs_be32 nw_dst;            // IPv4 destination address.                 1 << 16
			    uint8_t nw_frag;            // FLOW_FRAG_* flags.                        1 << 17
			    uint8_t nw_tos;             // IP ToS (including DSCP and ECN).          1 << 18
			    uint8_t nw_ttl;             // IP TTL/Hop Limit.                         1 << 19
			    uint8_t nw_proto;           // IP protocol or low 8 bits of ARP opcode.  1 << 20
			    uint8_t arp_sha[6];         // ARP/ND source hardware address.           1 << 21
			    uint8_t arp_tha[6];         // ARP/ND target hardware address.           1 << 22
			    ovs_be16 tcp_flags;         // TCP flags. With L3 to avoid matching L4.  1 << 23
			    ovs_be16 pad;               // Padding.                                  1 << 24
			    // L4 
			    ovs_be16 tp_src;            // TCP/UDP/SCTP source port.                 1 << 25
			    ovs_be16 tp_dst;            // TCP/UDP/SCTP destination port.            1 << 26
			                                 * Keep last for the BUILD_ASSERT_DECL below 
			};
			
			*/
	
	//!!!Be careful to use getFDirectionDataFromPktIn and getBDirectionDataFromPkIn , these functions 
	// are only used for simple demo, the Forward Direction means from tcp requester to tcp responsor,
	// commonly known as client to server. the Back Direction otherwise. Here , we just simply change 
	// the ip src and ip dst, tcp src port and tcp dst port which mean we assume that the rule only 
	// include ip src and ip dst.
	public static String getFDirectionDataFromPktIn(long bitmap,OFPacketIn pi,FloodlightContext cntx){
		
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
		        IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		String str = "" ;
		
		for( int i = 0 ; i < 64 ; i++)
		{
			 if( ( bitmap & (1 << i)) == 0 )
				 continue;
			 switch(i){
			 case 5:
				 // openflowflow in port
				 str = str+String.valueOf(pi.getInPort());
				 break;
			 case 6:
				 // ethernet src addr
				 str = str+eth.getSourceMAC().toString();
				 break;
			 case 7:
				 // ethernet dst addr
				 str = str+eth.getDestinationMAC().toString();
				 break;
			 case 8:
				 // ethernet type
				 str = str+String.valueOf(eth.getEtherType());
				 break;
			 case 15:
				 // ipv4 str addr,in order to see loger human readable 
				 // we store the data in xxx.xxx.xxx.xxx form,so in ovs we can print it easily
				 // in future we can store the int to save space.
				 if( eth.getPayload() instanceof IPv4){
					 IPv4 ip = (IPv4)eth.getPayload();
					 str = str + IPv4.fromIPv4Address(ip.getSourceAddress());
				 } 
				 break;
			 case 16:
				 // ipv4 dst addr
				 if( eth.getPayload() instanceof IPv4){
					 IPv4 ip = (IPv4)eth.getPayload();
					 str = str + IPv4.fromIPv4Address(ip.getDestinationAddress());
				 }
				 break;
			 case 25:
				 //tcp src port
				 if( eth.getPayload() instanceof IPv4 && ((IPv4)eth.getPayload()).getPayload() instanceof TCP){
					 TCP tcp = (TCP) ((IPv4)eth.getPayload()).getPayload();
					 str = str + String.valueOf(tcp.getSourcePort());
				 }
				 break;
			 case 26:
				 //tcp dst port
				 if( eth.getPayload() instanceof IPv4 && ((IPv4)eth.getPayload()).getPayload() instanceof TCP){
					 TCP tcp = (TCP) ((IPv4)eth.getPayload()).getPayload();
					 str = str + String.valueOf(tcp.getDestinationPort());
				 }
				 break;
				 
			 }
			
		}
		
		
		return str;
		
	}


	public static String getBDirectionDataFromPktIn(long bitmap,OFPacketIn pi,FloodlightContext cntx){

		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
		        IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		String str = "" ;
		
		for( int i = 0 ; i < 64 ; i++)
		{
			 if( ( bitmap & (1 << i)) == 0 )
				 continue;
			 switch(i){
			 case 5:
				 // openflowflow in port
				 str = str+String.valueOf(pi.getInPort());
				 break;
			 case 6:
				 // ethernet src addr
				 str = str+eth.getDestinationMAC().toString();
				 break;
			 case 7:
				 // ethernet dst addr
				 str = str+eth.getSourceMAC().toString();
				 break;
			 case 8:
				 // ethernet type
				 str = str+String.valueOf(eth.getEtherType());
				 break;
			 case 15:
				 // ipv4 str addr,in order to see loger human readable 
				 // we store the data in xxx.xxx.xxx.xxx form,so in ovs we can print it easily
				 // in future we can store the int to save space.
				 if( eth.getPayload() instanceof IPv4){
					 IPv4 ip = (IPv4)eth.getPayload();
					 str = str + IPv4.fromIPv4Address(ip.getDestinationAddress());
				 } 
				 break;
			 case 16:
				 // ipv4 dst addr
				 if( eth.getPayload() instanceof IPv4){
					 IPv4 ip = (IPv4)eth.getPayload();
					 str = str + IPv4.fromIPv4Address(ip.getSourceAddress());
				 }
				 break;
			 case 25:
				 //tcp src port
				 if( eth.getPayload() instanceof IPv4 && ((IPv4)eth.getPayload()).getPayload() instanceof TCP){
					 TCP tcp = (TCP) ((IPv4)eth.getPayload()).getPayload();
					 str = str + String.valueOf(tcp.getDestinationPort());
				 }
				 break;
			 case 26:
				 //tcp dst port
				 if( eth.getPayload() instanceof IPv4 && ((IPv4)eth.getPayload()).getPayload() instanceof TCP){
					 TCP tcp = (TCP) ((IPv4)eth.getPayload()).getPayload();
					 str = str + String.valueOf(tcp.getSourcePort());
				 }
				 break;
				 
			 }
			
		}
		
		return str;
	}
	
	
}
		
		
		
		
		
	
