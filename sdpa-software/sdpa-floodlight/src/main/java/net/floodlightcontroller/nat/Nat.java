package net.floodlightcontroller.nat;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFOXMFieldType;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionGotoFP;
import org.openflow.protocol.instruction.OFInstructionGotoTable;
import org.openflow.util.U16;
import org.python.antlr.PythonParser.return_stmt_return;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.AppCookie;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
//import net.floodlightcontroller.firewall.Firewall;
//import net.floodlightcontroller.firewall.FirewallRule;
//import net.floodlightcontroller.firewall.FirewallWebRoutable;
//import net.floodlightcontroller.firewall.IFirewallService;
import net.floodlightcontroller.firewall.NonWildcardsPair;
import net.floodlightcontroller.firewall.RuleWildcardsPair;

import java.util.ArrayList;

import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.IRoutingDecision;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.routing.RoutingDecision;
import net.floodlightcontroller.statefirewall.StateFirewall;
import net.floodlightcontroller.storage.IResultSet;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.storage.StorageException;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.util.OFMessageDamper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sfa.protocol.*;
import org.sfa.protocol.SFAAction.ActType;

/* A prototype application developed by SFA API
 * TODO: No configuration
 * @author zzl
 */
public class Nat implements IOFMessageListener,
IFloodlightModule {
		

	// service modules needed
	protected IFloodlightProviderService floodlightProvider;
	protected IStorageSourceService storageSource;
	protected IRestApiService restApi;
	protected IRoutingService routingEngine;
	protected ITopologyService topology;
	protected static Logger logger;
	protected IDeviceService deviceManager;

	protected static final long switchId = 1;
	public static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 5; // in seconds
    public static short FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite
    protected static int OFMESSAGE_DAMPER_CAPACITY = 10000; // TODO: find sweet spot
    protected static int OFMESSAGE_DAMPER_TIMEOUT = 250; // mss
    protected OFMessageDamper messageDamper = 
    		new OFMessageDamper(OFMESSAGE_DAMPER_CAPACITY,
                    EnumSet.of(OFType.FLOW_MOD),
                    OFMESSAGE_DAMPER_TIMEOUT);
	
	//save the specific firewall switch
	protected IOFSwitch appsSwitch = null;
	protected static final int appId = 2;
	protected static final long bitMap = (1<<15)|(1<<16);
	protected static boolean isInit;
	public static final int STATEFIREWALL_APP_ID = 11;
    static {
        AppCookie.registerApp(STATEFIREWALL_APP_ID, "StateFireWallAPP");
    }
	public static final int NAT_APP_ID = 22;
    static {
        AppCookie.registerApp(NAT_APP_ID, "NatAPP");
    }
    public static final int DDNSRA_APP_ID = 33;
    static {
        AppCookie.registerApp(DDNSRA_APP_ID, "DdnsraAPP");
    }

    protected static String testPrivateIp = "10.0.0.1";
    protected static String testPublicIp  ="10.0.0.11";
		
		//use the original firewall rule
	//protected List<FirewallRule> rules; // protected by synchronized
	protected boolean enabled;
	protected int subnet_mask = IPv4.toIPv4Address("255.255.255.0");
	@Override
	public String getName() {
		return "nat";
	}
		
	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return (type.equals(OFType.PACKET_IN) &&
                (name.equals("statefirewall") &&
                name.equals("ddnsra")));
	}
		
	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return (type.equals(OFType.PACKET_IN) && name.equals("forwarding"));
	}
		
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		//l.add(IFirewallService.class);
		return l;
	}
		
	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
	// We are the class that implements the service
		//m.put(IFirewallService.class, this);
		return m;
	}
		
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {

		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IStorageSourceService.class);
	//	l.add(IRestApiService.class);

		l.add(IRoutingService.class);
		l.add(ITopologyService.class);
		l.add(IDeviceService.class);
		return l;
	}
				
	@Override
	public void init(FloodlightModuleContext context)

		    throws FloodlightModuleException {
		floodlightProvider = context
		        .getServiceImpl(IFloodlightProviderService.class);
		storageSource = context.getServiceImpl(IStorageSourceService.class);
	//	restApi = context.getServiceImpl(IRestApiService.class);
	//	rules = new ArrayList<FirewallRule>();
		logger = LoggerFactory.getLogger(Nat.class);
		

		routingEngine = context.getServiceImpl(IRoutingService.class);
        topology = context.getServiceImpl(ITopologyService.class);
        deviceManager = context.getServiceImpl(IDeviceService.class);
        
        messageDamper = new OFMessageDamper(OFMESSAGE_DAMPER_CAPACITY,
                EnumSet.of(OFType.FLOW_MOD),
                OFMESSAGE_DAMPER_TIMEOUT);

		// start disabled
        enabled = true;
        
        if( enabled == true){
        	logger.info("----- nat has enabled ! -----");
        }else{
        	logger.info("----- nat has turned down !-----");
        }
		
		this.isInit = false;
		

	}
		
	// comment by eric @ 2014
	// In default floodlight sequence , all modules in the listfile will be firstly init,
	// then each startUp of  them will be sequentially called. so we can put the init_msg procedure
	// in startUp
	// In our statefirewall  startup stage we do following things:
	// 1. init the STT table column and content
	// 2. init the ST table bitmap appid , the content can be non
	// 3. init the AT table bitmap , the content can be non
	@Override
	public void startUp(FloodlightModuleContext context) {
	// register REST interface
		//restApi.addRestletRoutable(new FirewallWebRoutable());
			

		System.out.println("ip : " + IPv4.fromIPv4Address(184549386));
		// always place firewall in pipeline at bootup
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		
		// storage, create table and read rules
	//	storageSource.createTable(TABLE_NAME, null);
	//	storageSource.setTablePrimaryKeyName(TABLE_NAME, COLUMN_RULEID);
	//	this.rules = readRulesFromStorage();


		//do send the init msg to spe, we first have to loacte the firewall switch
		// 
		// 1. use floodlightserviceprovider to get all the switch list
		// 2. use the fix name for firewall to locate the firewall node.

		if (!isInit){
			Map<Long,IOFSwitch> switchmap = this.floodlightProvider. getAllSwitchMap();				
			if( switchmap.size() != 0){
				logger.info("-----sfa init the sfa table in startup function use switch 1 as firewall -----");
				appsSwitch = switchmap.get(switchId);
				doSendSfaInitMsg(appsSwitch);
				isInit = true;
			}
			
		}
			
	}
			
	public void doSendSfaInitMsg(IOFSwitch sw){
			
		logger.info("-----send init msg to switch {}, msg type is {}------",sw.getId(),OFType.SFA_CREATE.getTypeValue());
		SFASt st_tmp = new SFASt();
		//int stBitmap = 1 << 15;
		st_tmp.setAppid(appId);
		st_tmp.setBitmap(bitMap);
			
			
		//init at table
		SFAAt at_tmp = new SFAAt();
		//int atBitmap = 1 << 16;
		at_tmp.setBitmap(bitMap);
				
		//init stt table
		SFAStt stt_tmp = new SFAStt();

		STTDATA []sttdat = {new STTDATA(SFAEventType.SFAPARAM_NON, 1, SFAEventType.SFAPARAM_NON, 1, SFAEventOp.OPRATOR_NON,
				NatStatus.SRC_IP.getValue(), NatStatus.SRC_IP.getValue()),
			//				new STTDATA(SFAEventType.SFAPARAM_NON, 1, SFAEventType.SFAPARAM_NON, 1, SFAEventOp.OPRATOR_NON,
		//		NatStatus.SRC_IP.getValue(), NatStatus.DST_IP.getValue()),
							new STTDATA(SFAEventType.SFAPARAM_NON, 1, SFAEventType.SFAPARAM_NON, 1, SFAEventOp.OPRATOR_NON,
				NatStatus.DST_IP.getValue(), NatStatus.DST_IP.getValue())
		//					new STTDATA(SFAEventType.SFAPARAM_NON, 1, SFAEventType.SFAPARAM_NON, 1, SFAEventOp.OPRATOR_NON,
	//			NatStatus.DST_IP.getValue(), NatStatus.SRC_IP.getValue())
				};
			
	
		for(int i = 0; i < sttdat.length; ++i){
			stt_tmp.addSttData(sttdat[i]);
		}
			
		SFACreate sfc = (SFACreate) floodlightProvider.getOFMessageFactory().getMessage(OFType.SFA_CREATE);
			
		sfc.setST(st_tmp);
		sfc.setSTT(stt_tmp);
		sfc.setAT(at_tmp);
		

		//TODO:write the tables	
		try{
			sw.write(sfc, null);
		} catch (IOException e){
			 logger.error("-----sfa fail to init the sfa module ", e);
		}
	}
		
	
	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
	
	if (!this.enabled)
	    return Command.CONTINUE;
	
	if( isInit == false){
		Map<Long,IOFSwitch> switchmap = this.floodlightProvider. getAllSwitchMap();
		if( switchmap.size() != 0){
			appsSwitch = switchmap.get(switchId);
			logger.info("-----sfa nat init the sfa table in first recv function -----");	
			doSendSfaInitMsg(appsSwitch);
			isInit = true;
		}		
		
	}

		switch (msg.getType()) {
		case PACKET_IN:
		    IRoutingDecision decision = null;
		    if (cntx != null) {
		        decision = IRoutingDecision.rtStore.get(cntx,
		                IRoutingDecision.CONTEXT_DECISION);
		
		        return this.processPacketInMessage(sw, (OFPacketIn) msg,
		                decision, cntx);
		    }
		    break;
		default:
		    break;
		}
		
		return Command.CONTINUE;

	}
	
	/**
	* Checks whether an IP address is a broadcast address or not (determines
	* using subnet mask)
	* 
	* @param IPAddress
	*            the IP address to check
	* @return true if it is a broadcast address, false otherwise
	*/
	
	protected boolean IPIsBroadcast(int IPAddress) {
		// inverted subnet mask
		int inv_subnet_mask = ~this.subnet_mask;
		return ((IPAddress & inv_subnet_mask) == inv_subnet_mask);
	}
		
		
	// comment by eric 
	// In receive func we have to do following steps
	// 1. check if the packet match the rule
	// 2. if packet does not match the rule, we just pass the process to original chain. 
	//	  else we should make the st content and at content according to the packet payload
	// 3. push the st update msg and at update msg to firewall switch
	// 4. push goto_fp instruction to firewall switch
	// 
	public Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi,IRoutingDecision decision, FloodlightContext cntx) 
	{
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
		        IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		
		if (eth.isBroadcast() == true) {
		    boolean allowBroadcast = true;
		    // the case to determine if we have L2 broadcast + L3 unicast
		    // don't allow this broadcast packet if such is the case (malformed
		    // packet)
		    if ((eth.getPayload() instanceof IPv4)
		            && this.IPIsBroadcast(((IPv4) eth.getPayload())
		                    .getDestinationAddress()) == false) {
		        allowBroadcast = false;
		    }
		    if (allowBroadcast == true) {

		            logger.info("Allowing broadcast traffic for PacketIn={}",pi);
		                                
		        decision = new RoutingDecision(sw.getId(), pi.getInPort()
		        		, IDeviceService.fcStore.
		                get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
		                IRoutingDecision.RoutingAction.MULTICAST);
		        decision.addToContext(cntx);
		    } else {

		            logger.info(
		                    "Blocking malformed broadcast traffic for PacketIn={}",
		                    pi);
		
		        decision = new RoutingDecision(sw.getId(), pi.getInPort()
		        		, IDeviceService.fcStore.
		                get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
		                IRoutingDecision.RoutingAction.DROP);
		        decision.addToContext(cntx);
		    }
		    return Command.CONTINUE;

		    
		}else if (eth.getEtherType() == Ethernet.TYPE_ARP) {
			
			logger.info("allowing ARP traffic={}",pi); 
			System.out.println("source mac : " + eth.getSourceMACAddress());
			ARP ip = (ARP)eth.getPayload();
			System.out.println("source ip : " + ip.getTargetHardwareAddress());
			decision = new RoutingDecision(sw.getId(), pi.getInPort()
	        		, IDeviceService.fcStore.
	                get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
	                IRoutingDecision.RoutingAction.FORWARD_OR_FLOOD);
			EnumSet<OFOXMFieldType> arpnonWildcards = EnumSet.noneOf(OFOXMFieldType.class);
			arpnonWildcards = EnumSet.of(OFOXMFieldType.IN_PORT, OFOXMFieldType.VLAN_VID,
                     OFOXMFieldType.ETH_SRC, OFOXMFieldType.ETH_DST,
                     OFOXMFieldType.ETH_TYPE );
			decision.setNonWildcards(arpnonWildcards);
			decision.addToContext(cntx); 
			return Command.CONTINUE; 
		}
		else{}

		
		// check if we have a matching rule for this packet/flow
		// and no decision is taken yet
		
		boolean isTCP = ( eth.getPayload() instanceof IPv4 && ((IPv4)eth.getPayload() ).getPayload() instanceof TCP);
		System.out.println(isTCP);
		
		if(isTCP && isInit){
//			int outport = getOutPort(sw, pi, cntx);
//			if (outport == -1){
//			      	logger.info("Some error happened!!!");			        	
//			        return Command.CONTINUE;
//			}
			int routport = pi.getInPort();
			//outport = 2;
		//	logger.info("calculating the port in :{}, out:{}",routport,outport);
			        
			//step 2.2 make st entrys 
			
			String dstIp = "";
			if( eth.getPayload() instanceof IPv4){
				 IPv4 ip = (IPv4)eth.getPayload();
				 dstIp = IPv4.fromIPv4Address(ip.getDestinationAddress());
			}
			dstIp = "10.0.0.2";
			String strFwd = testPrivateIp + dstIp;
			String strBwd = dstIp + "10.0.0.11";
		//	String strFwd = SFAUtil.getFDirectionDataFromPktIn(bitMap, pi, cntx);
		//	String strBwd = SFAUtil.getBDirectionDataFromPktIn(bitMap, pi, cntx);

			STDATA stdat1 = new STDATA(NatStatus.SRC_IP.getValue(),strFwd.getBytes());
			STDATA stdat2 = new STDATA(NatStatus.DST_IP.getValue(),strBwd.getBytes());
			        
			SFAStMod stmodmsg = new SFAStMod();

			stmodmsg.addSTMod(appId, SFAModType.ENTRY_ADD, stdat1);
			stmodmsg.addSTMod(appId,SFAModType.ENTRY_ADD , stdat2);

			try{
				logger.info("-----send st mod msg to switch {}",appsSwitch.getId());		        
				appsSwitch.write(stmodmsg, null);
				sw.flush();
				}catch (IOException e){
			        	 logger.error("Error", e);
			        }
			//step 2.3 make at entrys
			SFAAtMod atmodmsg = new SFAAtMod();

		    ATDATA atdat1 = new ATDATA(NatStatus.SRC_IP.getValue(),strFwd.getBytes(),new SFAAction(ActType.ACT_SETSRCFIELD,"11.0.0.10"));
		    ATDATA atdat2 = new ATDATA(NatStatus.DST_IP.getValue(),strBwd.getBytes(),new SFAAction(ActType.ACT_SETDSTFIELD,"1.0.0.10"));
		    
			        
		    atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat1);
		    atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat2);

			try{
				logger.info("-----send at mod msg to switch {}",appsSwitch.getId());
			    appsSwitch.write(atmodmsg, null);
				sw.flush();
				}catch (IOException e){
					logger.error("Error", e);
				}
			        	
			//goto_fp and goto_table   
			//defualt table id is 0
			List<OFInstruction> instructions = new ArrayList<OFInstruction>();

			/**
			 * push the forward flow entry
			 * 3 entrys
			 * 6 instructions
			 */
			//table 0
			//statefirewall goto_fp(1) goto_table(1)
	        OFFlowMod stateFirewallFm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
	        OFMatch stateFirewallMatch = new OFMatch();
	        stateFirewallMatch.ipMatchLoadFromPacket(pi.getPacketData(), routport);
	        EnumSet<OFOXMFieldType> stateFirewallNonWildcards = EnumSet.of(OFOXMFieldType.IPV4_SRC, OFOXMFieldType.IPV4_DST,OFOXMFieldType.ETH_TYPE);
	        stateFirewallMatch.setNonWildcards(stateFirewallNonWildcards);			        
	        
	        instructions.add((OFInstruction) new OFInstructionGotoFP((byte)1));
	        instructions.add((OFInstruction) new OFInstructionGotoTable((byte)1));
	        long stateFirewallCookie = AppCookie.makeCookie(STATEFIREWALL_APP_ID, 0);
	        stateFirewallFm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
            .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
            .setBufferId(pi.getBufferId())
            .setCookie(stateFirewallCookie)
            .setTableId((byte)0)
            .setMatch(stateFirewallMatch)
            .setPriority((short) 1) //set the gotofp priority highest
            .setCommand(OFFlowMod.OFPFC_ADD)
            .setInstructions(instructions);
            //.setInstructions(actions);					        
	        stateFirewallFm=stateFirewallFm.clone();
	        //end
			
	        //table 1
			//ddnsra goto_fp(3) goto_table(2)
	        OFFlowMod ddnsraFm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
	        OFMatch ddnsraMatch = new OFMatch();
	        ddnsraMatch.ipMatchLoadFromPacket(pi.getPacketData(), routport);
	        EnumSet<OFOXMFieldType> ddnsraNonWildcards = EnumSet.of(OFOXMFieldType.IPV4_SRC, OFOXMFieldType.IPV4_DST, OFOXMFieldType.ETH_TYPE, OFOXMFieldType.IP_PROTO, OFOXMFieldType.UDP_SRC, OFOXMFieldType.UDP_DST);
	        ddnsraMatch.setNonWildcards(ddnsraNonWildcards);			        
	        
	        instructions.clear();
	        instructions.add((OFInstruction) new OFInstructionGotoFP((byte)3));
	        instructions.add((OFInstruction) new OFInstructionGotoTable((byte)2));
	        long ddnsraCookie = AppCookie.makeCookie(DDNSRA_APP_ID, 0);
	        ddnsraFm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
	        .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
	        .setBufferId(pi.getBufferId())
	        .setCookie(ddnsraCookie)
	        .setTableId((byte)1)
	        .setMatch(ddnsraMatch)
	        .setPriority((short) 1) //set the gotofp priority highest
	        .setCommand(OFFlowMod.OFPFC_ADD)
	        .setInstructions(instructions);
	        //.setInstructions(actions);					        
	        ddnsraFm=ddnsraFm.clone();

	        //end
	        		
	        //table 2
			//nat goto_fp(2) output(port)
	        OFFlowMod natFm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
			OFMatch natMatch = new OFMatch();
			natMatch.ipMatchLoadFromPacket(pi.getPacketData(), routport);
			//set the private ip. It.s not nessesary
			int srcIp = IPv4.toIPv4Address(testPrivateIp);
			natMatch.setNetworkSource(srcIp);
			EnumSet<OFOXMFieldType> natNonWildcards = EnumSet.of(OFOXMFieldType.IPV4_SRC, OFOXMFieldType.IPV4_DST,OFOXMFieldType.ETH_TYPE);
			natMatch.setNonWildcards(natNonWildcards);			        
	
			instructions.clear();
//			OFActionOutput action = new OFActionOutput();
//            action.setMaxLength((short) 0);
//            action.setPort(2);       //use the outport of nat
//            List<OFAction> actions = new ArrayList<OFAction>();
//            actions.add(action);
//            OFInstructionApplyActions instruction = new OFInstructionApplyActions(actions);
            
	        instructions.add((OFInstruction) new OFInstructionGotoFP((byte)2));
	        instructions.add((OFInstruction) new OFInstructionGotoTable((byte)3));
	    //    instructions.add(instruction);  //output
			long natCookie = AppCookie.makeCookie(NAT_APP_ID, 0);
			natFm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
	        .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
	        .setBufferId(pi.getBufferId())
	        .setCookie(natCookie)
	        .setTableId((byte)2)
	        .setMatch(natMatch)
	        .setPriority((short) 1)   //set the gotofp priority highest
	        .setCommand(OFFlowMod.OFPFC_ADD)
	        .setInstructions(instructions);
	            //.setInstructions(actions);					        
			natFm=natFm.clone();
			
			//table3
			//output

			OFFlowMod outputFm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
			OFMatch outputMatch = new OFMatch();
			natMatch.ipMatchLoadFromPacket(pi.getPacketData(), routport);
			//set the private ip. It.s not nessesary
			srcIp = IPv4.toIPv4Address(testPrivateIp);
			natMatch.setNetworkSource(srcIp);
			EnumSet<OFOXMFieldType> outputNonWildcards = EnumSet.of(OFOXMFieldType.IPV4_SRC, OFOXMFieldType.IPV4_DST,OFOXMFieldType.ETH_TYPE);
			natMatch.setNonWildcards(natNonWildcards);			        
	
			instructions.clear();
			OFActionOutput action = new OFActionOutput();
            action.setMaxLength((short) 0);
            action.setPort(2);       //use the outport of nat
            List<OFAction> actions = new ArrayList<OFAction>();
            actions.add(action);
            OFInstructionApplyActions instruction = new OFInstructionApplyActions(actions);
            
	      //  instructions.add((OFInstruction) new OFInstructionGotoFP((byte)2));
	        instructions.add(instruction);  //output
			long outputCookie = AppCookie.makeCookie(NAT_APP_ID, 0);
			outputFm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
	        .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
	        .setBufferId(pi.getBufferId())
	        .setCookie(outputCookie)
	        .setTableId((byte)3)
	        .setMatch(outputMatch)
	        .setPriority((short) 1)   //set the gotofp priority highest
	        .setCommand(OFFlowMod.OFPFC_ADD)
	        .setInstructions(instructions);
	            //.setInstructions(actions);					        
			outputFm = outputFm.clone();  
			//end 
			/**
			 * end forward entry
			 */
			
			/**
			 * push the backward flow entry
			 * 3 entrys
			 * 6 instructions
			 */
			
			//nat goto_fp(2) goto_table(1)
			OFFlowMod natRfm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
			OFMatch natRmatch = new OFMatch();
			natRmatch.ipReverseMatchLoadFromPacket(pi.getPacketData(), 2);
			//set the dst ip. It's nessesary
			int dstIp1 = IPv4.toIPv4Address(testPublicIp);
			natRmatch.setNetworkDestination(dstIp1);
			EnumSet<OFOXMFieldType> natRnonWildcards = EnumSet.of(OFOXMFieldType.IPV4_DST, OFOXMFieldType.IPV4_SRC,OFOXMFieldType.ETH_TYPE);
			natRmatch.setNonWildcards(natRnonWildcards);			        
			   
			instructions.clear();
	        instructions.add((OFInstruction) new OFInstructionGotoFP((byte)2));
	        instructions.add((OFInstruction) new OFInstructionGotoTable((byte)1));
			long natRcookie = AppCookie.makeCookie(NAT_APP_ID, 0);
			natRfm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
		        .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
		        .setBufferId(OFPacketOut.BUFFER_ID_NONE)
		        .setCookie(natRcookie)
		        .setTableId((byte)0)
		        .setMatch(natRmatch)
		        .setPriority((short) 1) //set the gotofp priority highest
		        .setCommand(OFFlowMod.OFPFC_ADD)
		        .setInstructions(instructions);				        
			natRfm=natRfm.clone();			        
			
			//end
			
			//ddnsra goto_fp(3) goto_table(2)
	      //send reverse msg
	        OFFlowMod dnsraRfm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
	        OFMatch ddnsraRmatch = new OFMatch();
	        ddnsraRmatch.ipReverseMatchLoadFromPacket(pi.getPacketData(), 2);
	   //     ddnsraRmatch.setNetworkDestination(dstIp1);
	        EnumSet<OFOXMFieldType> ddnsraRnonWildcards = EnumSet.of(OFOXMFieldType.IPV4_DST, OFOXMFieldType.IPV4_SRC, OFOXMFieldType.ETH_TYPE, OFOXMFieldType.IP_PROTO, OFOXMFieldType.UDP_DST, OFOXMFieldType.UDP_SRC);
	        ddnsraRmatch.setNonWildcards(ddnsraRnonWildcards);			        
	        
	        instructions.clear();
	        instructions.add((OFInstruction) new OFInstructionGotoFP((byte)3));
	        instructions.add((OFInstruction) new OFInstructionGotoTable((byte)2));
	        long ddnsraRcookie = AppCookie.makeCookie(DDNSRA_APP_ID, 0);
	        dnsraRfm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
	        .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
	        .setBufferId(OFPacketOut.BUFFER_ID_NONE)
	        .setCookie(ddnsraRcookie)
	        .setTableId((byte)1)
	        .setMatch(ddnsraRmatch)
	        .setPriority((short) 1) //set the gotofp priority highest
	        .setCommand(OFFlowMod.OFPFC_ADD)
	        .setInstructions(instructions);
	        //.setInstructions(actions);					        
	        dnsraRfm = dnsraRfm.clone();
	        
	        //end
			
			//statefirewall goto_fp(1)  output(rport)
	        OFFlowMod stateFirewallRfm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
	        OFMatch stateFirewallRmatch = new OFMatch();
	        stateFirewallRmatch.ipReverseMatchLoadFromPacket(pi.getPacketData(), 2);
	       // stateFirewallRmatch.setNetworkDestination(dstIp1);
	        EnumSet<OFOXMFieldType> stateFireallNonWildcards = EnumSet.of(OFOXMFieldType.IPV4_DST, OFOXMFieldType.IPV4_SRC,OFOXMFieldType.ETH_TYPE);
	        stateFirewallRmatch.setNonWildcards(stateFireallNonWildcards);			        
	        
	        instructions.clear();
//			OFActionOutput action1 = new OFActionOutput();
//            action1.setMaxLength((short) 0);
//            action1.setPort(1);       //use the outport of nat
//            List<OFAction> actions1 = new ArrayList<OFAction>();
//            actions1.add(action1);
           // OFInstructionApplyActions instruction1 = new OFInstructionApplyActions(actions1);
            
	        instructions.add((OFInstruction) new OFInstructionGotoFP((byte)1));
	        instructions.add((OFInstruction) new OFInstructionGotoTable((byte)3));
	   //     instructions.add(instruction1);  //output
	        long stateFirewallRcookie = AppCookie.makeCookie(STATEFIREWALL_APP_ID, 0);
	        stateFirewallRfm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
            .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
            .setBufferId(OFPacketOut.BUFFER_ID_NONE)
            .setCookie(stateFirewallRcookie)
            .setTableId((byte)2)
            .setMatch(stateFirewallRmatch)
            .setPriority((short) 1) //set the gotofp priority highest
            .setCommand(OFFlowMod.OFPFC_ADD)
            .setInstructions(instructions);  //TODO
            //.setInstructions(actions);					        
	        stateFirewallRfm = stateFirewallRfm.clone();
	        //end
	        
	        //output port1  
	        //table3
	        OFFlowMod outputRfm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
	        OFMatch outputRmatch = new OFMatch();
	        outputRmatch.ipReverseMatchLoadFromPacket(pi.getPacketData(), 2);
	     //   outputRmatch.setNetworkDestination(dstIp1);
	        EnumSet<OFOXMFieldType> outputRNonWildcards = EnumSet.of(OFOXMFieldType.IPV4_DST, OFOXMFieldType.IPV4_SRC,OFOXMFieldType.ETH_TYPE);
	        outputRmatch.setNonWildcards(outputRNonWildcards);			        
	        
	        instructions.clear();
			OFActionOutput action1 = new OFActionOutput();
            action1.setMaxLength((short) 0);
            action1.setPort(1);       //use the outport of nat
            List<OFAction> actions1 = new ArrayList<OFAction>();
            actions1.add(action1);
            OFInstructionApplyActions instruction1 = new OFInstructionApplyActions(actions1);
            
	    //    instructions.add((OFInstruction) new OFInstructionGotoFP((byte)1));
	        instructions.add(instruction1);  //output
	        long outputRcookie = AppCookie.makeCookie(STATEFIREWALL_APP_ID, 0);
	        outputRfm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
            .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
            .setBufferId(OFPacketOut.BUFFER_ID_NONE)
            .setCookie(outputRcookie)
            .setTableId((byte)3)
            .setMatch(outputRmatch)
            .setPriority((short) 1) //set the gotofp priority highest
            .setCommand(OFFlowMod.OFPFC_ADD)
            .setInstructions(instructions);  //TODO
            //.setInstructions(actions);					        
	        outputRfm = outputRfm.clone();
	        
	        /**
			 * end forward entry
			 */
			
	        /**
	         * write entrys to flow table
	         */        
	        
	        try{	
	        		logger.info("forward write statefirewall");
	        		appsSwitch.write(stateFirewallFm, null);
	        		logger.info("table0 flow entry1 : src:{}, dst:{}", IPv4.fromIPv4Address(stateFirewallFm.getMatch().getNetworkSource()),
    								  IPv4.fromIPv4Address(stateFirewallFm.getMatch().getNetworkDestination()));
	        		
	        		logger.info("forward write ddnsra");
	        		appsSwitch.write(ddnsraFm, null);
	        		logger.info("table1 flow entry1 : src:{}, dst:{}", IPv4.fromIPv4Address(ddnsraFm.getMatch().getNetworkSource()),
							  IPv4.fromIPv4Address(ddnsraFm.getMatch().getNetworkDestination()));
	        		
	        		logger.info("forward write nat");
	        		appsSwitch.write(natFm, null);
	        	//	appsSwitch.write(outputFm, null);
	        		logger.info("table2 flow entry2 : src:{}, dst:{}", IPv4.fromIPv4Address(natFm.getMatch().getNetworkSource()),
							  IPv4.fromIPv4Address(natFm.getMatch().getNetworkDestination()));
	        		
	        		logger.info("backward write nat");
	        		appsSwitch.write(natRfm, null);
	        		logger.info("table0 flow entry2 : reverse path src:{},dst:{}",IPv4.fromIPv4Address(natRfm.getMatch().getNetworkSource()),
							 IPv4.fromIPv4Address(natRfm.getMatch().getNetworkDestination()));
	        		
	        		logger.info("backward write ddnsra");
	        		appsSwitch.write(dnsraRfm, null);
	        		logger.info("table1 flow entry2 : reverse path src:{},dst:{}",IPv4.fromIPv4Address(dnsraRfm.getMatch().getNetworkSource()),
							 IPv4.fromIPv4Address(dnsraRfm.getMatch().getNetworkDestination()));
	        		
	        		logger.info("backward write statefirewall");
	        		appsSwitch.write(stateFirewallRfm, null);
	        		appsSwitch.write(outputRfm, null);
	        		logger.info("flow entry 2 : reverse path src:{},dst:{}",IPv4.fromIPv4Address(stateFirewallRfm.getMatch().getNetworkSource()),
      						IPv4.fromIPv4Address(stateFirewallRfm.getMatch().getNetworkDestination()));

	        	}catch (IOException e) {
	        		logger.error("Error", e);
	        	}
	        
			 return Command.STOP;
			 }
		    	
		    
		// do the normal output 
		logger.info("-----do the normal output-----!");
		decision = new RoutingDecision(sw.getId(), pi.getInPort()
	    		, IDeviceService.fcStore.
	            get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
	            IRoutingDecision.RoutingAction.FORWARD_OR_FLOOD);
		EnumSet<OFOXMFieldType> ipnonWildcards = EnumSet.noneOf(OFOXMFieldType.class);
		ipnonWildcards = EnumSet.of(OFOXMFieldType.IN_PORT, OFOXMFieldType.VLAN_VID,
	             OFOXMFieldType.ETH_SRC, OFOXMFieldType.ETH_DST,
	             OFOXMFieldType.ETH_TYPE ,OFOXMFieldType.IPV4_SRC, OFOXMFieldType.IPV4_DST);
		decision.setNonWildcards(ipnonWildcards);
	    decision.addToContext(cntx);
	    return Command.CONTINUE;

	}
		
/*	@Override
	public boolean isEnabled() {
		return enabled;
	}
*/		
	public Comparator<SwitchPort> clusterIdComparator =
	           new Comparator<SwitchPort>() {
		@Override
		public int compare(SwitchPort d1, SwitchPort d2) {
			Long d1ClusterId =
					topology.getL2DomainId(d1.getSwitchDPID());
			Long d2ClusterId =
					topology.getL2DomainId(d2.getSwitchDPID());
			return d1ClusterId.compareTo(d2ClusterId);
		}
	};
		
	protected int getOutPort(IOFSwitch sw, OFPacketIn pi,
                FloodlightContext cntx) {
		OFMatch match = new OFMatch();
		match.loadFromPacket(pi.getPacketData(), pi.getInPort());
			
		// Check if we have the location of the destination
		
		IDevice dstDevice =
		IDeviceService.fcStore.
		   get(cntx, IDeviceService.CONTEXT_DST_DEVICE);	
		
		if (dstDevice != null) {
			IDevice srcDevice =
			   IDeviceService.fcStore.
			       get(cntx, IDeviceService.CONTEXT_SRC_DEVICE);
			Long srcIsland = topology.getL2DomainId(sw.getId());
			System.out.println("sw Id" + sw.getId());
			
			if (srcDevice == null) {
				logger.debug("No device entry found for source device");
				return -1;
			}
			if (srcIsland == null) {
				logger.debug("No openflow island found for source {}/{}",
						sw.getStringId(), pi.getInPort());
				return -1;
			}
			
			// Validate that we have a destination known on the same island
			// Validate that the source and destination are not on the same switchport
			boolean on_same_island = false;
			boolean on_same_if = false;
			for (SwitchPort dstDap : dstDevice.getAttachmentPoints()) {
				long dstSwDpid = dstDap.getSwitchDPID();
				Long dstIsland = topology.getL2DomainId(dstSwDpid);
				if ((dstIsland != null) && dstIsland.equals(srcIsland)) {
					on_same_island = true;
					if ((sw.getId() == dstSwDpid) &&
							(pi.getInPort() == dstDap.getPort())) {
						on_same_if = true;
					}
					break;
				}
			}
			
			if (!on_same_island) {
			// Flood since we don't know the dst device
				if (logger.isTraceEnabled()) {
					logger.trace("No first hop island found for destination " +
							"device {}, Action = flooding", dstDevice);
				}
				return -1;
			}
			
			if (on_same_if) {
				if (logger.isTraceEnabled()) {
					logger.trace("Both source and destination are on the same " +
			             "switch/port {}/{}, Action = NOP",
			             sw.toString(), pi.getInPort());
				}
				return -1;
			}
			
			// Install all the routes where both src and dst have attachment
			// points.  Since the lists are stored in sorted order we can
			// traverse the attachment points in O(m+n) time
			SwitchPort[] srcDaps = srcDevice.getAttachmentPoints();    //empty
			Arrays.sort(srcDaps, clusterIdComparator);
			SwitchPort[] dstDaps = dstDevice.getAttachmentPoints();    //empty
			Arrays.sort(dstDaps, clusterIdComparator);
			
			int iSrcDaps = 0, iDstDaps = 0;
			
			while ((iSrcDaps < srcDaps.length) && (iDstDaps < dstDaps.length)) {
				SwitchPort srcDap = srcDaps[iSrcDaps];
				SwitchPort dstDap = dstDaps[iDstDaps];
				
				// srcCluster and dstCluster here cannot be null as
				// every switch will be at least in its own L2 domain.
				Long srcCluster =
				       topology.getL2DomainId(srcDap.getSwitchDPID());
				Long dstCluster =
				       topology.getL2DomainId(dstDap.getSwitchDPID());
				
				int srcVsDest = srcCluster.compareTo(dstCluster);
				if (srcVsDest == 0) {
				   if (!srcDap.equals(dstDap)) {
				       Route route =
				               routingEngine.getRoute(srcDap.getSwitchDPID(),
				                                      srcDap.getPort(),
				                                      dstDap.getSwitchDPID(),
				                                      dstDap.getPort(), 0); //cookie = 0, i.e., default route
				       if (route != null) {
				           if (logger.isTraceEnabled()) {
				               logger.trace("pushRoute match={} route={} " +
				                         "destination={}:{}",
				                         new Object[] {match, route,
				                                       dstDap.getSwitchDPID(),
				                                       dstDap.getPort()});
				           }
				           //only use route
				           List<NodePortTuple> switchPorts = route.getPath();
				           int portId = 0;
				           for (NodePortTuple nodePort : switchPorts){
				        	   if (nodePort.getNodeId() == switchId){
				        		   portId = nodePort.getPortId();
				        		   if (portId != pi.getInPort())
				        			   break;
				        	   }
				           }
				           return portId;
				       }
				   }
				   iSrcDaps++;
				   iDstDaps++;
				} else if (srcVsDest < 0) {
				   iSrcDaps++;
				} else {
				   iDstDaps++;
				}
			}
		} else {
		// Flood since 
			logger.info("we don't know the dst device");
			return -1;
			//doFlood(sw, pi, cntx);
		}
		return 0;
	}		


}

