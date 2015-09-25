package net.floodlightcontroller.ddnsra;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.core.util.AppCookie;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
//import net.floodlightcontroller.firewall.FirewallWebRoutable;
//smport net.floodlightcontroller.firewall.IFirewallService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.IRoutingDecision;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.util.OFMessageDamper;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFOXMFieldType;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFType;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionGotoFP;
import org.sfa.protocol.ATDATA;
import org.sfa.protocol.SFAAction;
import org.sfa.protocol.SFAAction.ActType;
import org.sfa.protocol.SFAAt;
import org.sfa.protocol.SFAAtMod;
import org.sfa.protocol.SFACreate;
import org.sfa.protocol.SFAEventOp;
import org.sfa.protocol.SFAEventType;
import org.sfa.protocol.SFAModType;
import org.sfa.protocol.SFASt;
import org.sfa.protocol.SFAStMod;
import org.sfa.protocol.SFAStt;
import org.sfa.protocol.SFAUtil;
import org.sfa.protocol.STDATA;
import org.sfa.protocol.STTDATA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import net.floodlightcontroller.firewall.Firewall;


/**
 * DNS reflection attack defense implemented based on sfa.
 * 
 * @author sc@2014.12.06
 * 
 * Description:
 * 
 * 
 */
public class Ddnsra implements IOFMessageListener,IFloodlightModule, IFloodlightService {

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
    public static boolean ddnsraPushed = false;
	
    //save the specific ddnsra switch
  	protected IOFSwitch ddnsra = null;
    protected static final int appId = 3;
	protected static final long bitMap = (1<<15)|(1<<16)|(1<<25)|(1<<26);
    
	protected static boolean isInit;
	protected static boolean enabled;
//	public static final int DDNSRA_APP_ID = 34;
//    static {
//        AppCookie.registerApp(DDNSRA_APP_ID, "DdnsraAPP");
//    }
		
	@Override
	public String getName() {
		return "ddnsra";
	}
		
	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return (type.equals(OFType.PACKET_IN) &&
                (name.equals("topology") &&
                name.equals("devicemanager") && 
                name.equals("statefirewall")));
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
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {

		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IStorageSourceService.class);
		l.add(IRestApiService.class);

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
		logger = LoggerFactory.getLogger(Ddnsra.class);
		

		routingEngine = context.getServiceImpl(IRoutingService.class);
        topology = context.getServiceImpl(ITopologyService.class);
        deviceManager = context.getServiceImpl(IDeviceService.class);
        
        messageDamper = new OFMessageDamper(OFMESSAGE_DAMPER_CAPACITY,
                EnumSet.of(OFType.FLOW_MOD),
                OFMESSAGE_DAMPER_TIMEOUT);

		// start disabled
        enabled = true;
        
        if( enabled == true){
        	logger.info("----- ddnsra has enabled ! -----");
        }else{
        	logger.info("----- ddnsra has turned down !-----");
        }
	}
		
	// comment by sc @ 2014
	@Override
	public void startUp(FloodlightModuleContext context) {
	// register REST interface
	//	restApi.addRestletRoutable(new FirewallWebRoutable());
			

		// always place firewall in pipeline at bootup
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		
		//do send the init msg to spe, we first have to loacte the firewall switch
		// 
		// 1. use floodlightserviceprovider to get all the switch list
		// 2. use the fix name for firewall to locate the firewall node.

		if (!isInit){
			Map<Long,IOFSwitch> switchmap = this.floodlightProvider. getAllSwitchMap();				
			if( switchmap.size() != 0){
				logger.info("-----sfa ddnsra inits -----");
				ddnsra = switchmap.get(switchId);
				doSendSfaInitMsg(ddnsra);
				isInit = true;
			}
			
		}
			
	}
			
	public void doSendSfaInitMsg(IOFSwitch sw){
			
		logger.info("Inside ddnsra: -----send init msg to switch {}, msg type is {}------",sw.getId(),OFType.SFA_CREATE.getTypeValue());
		SFASt st_tmp = new SFASt();
		st_tmp.setAppid(appId);
		st_tmp.setBitmap(bitMap);
			
			
		//init at table
		SFAAt at_tmp = new SFAAt();
		at_tmp.setBitmap(bitMap);
				
		//init stt table
		SFAStt stt_tmp = new SFAStt();

		// stt table
		STTDATA []sttdat = {
				//new STTDATA(SFAEventType.SFAPARAM_TP_DST, 1, SFAEventType.SFAPARAM_CONST, 53, SFAEventOp.OPRATOR_ISEQUAL,
				//		DdnsraState.DDRA_STATE_START.getValue(), DdnsraState.DDRA_STATE_REQUEST_SENT.getValue()),
				new STTDATA(SFAEventType.SFAPARAM_TP_SRC, 1, SFAEventType.SFAPARAM_CONST, 53, SFAEventOp.OPRATOR_ISEQUAL,
						DdnsraState.DDRA_STATE_REQUEST_SENT.getValue(), DdnsraState.DDRA_STATE_START.getValue())
				};
			
	
		for(int i = 0; i < sttdat.length; ++i){
			stt_tmp.addSttData(sttdat[i]);
		}
			
		SFACreate sfc = (SFACreate) floodlightProvider.getOFMessageFactory().getMessage(OFType.SFA_CREATE);
			
		sfc.setST(st_tmp);
		sfc.setSTT(stt_tmp);
		sfc.setAT(at_tmp);
		
		try{
			sw.write(sfc, null);
		} catch (IOException e){
			 logger.error("-----sfa fail to init the ddnsra module ", e);
		}
	}
		
	
	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
	
	if (!this.enabled)
	    return Command.CONTINUE;

	if (!isInit){
		Map<Long,IOFSwitch> switchmap = this.floodlightProvider. getAllSwitchMap();				
		if( switchmap.size() != 0){
			logger.info("-----sfa ddnsra init in first recv function -----");
			ddnsra = switchmap.get(switchId);
			doSendSfaInitMsg(ddnsra);
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
		
	// comment by sc 
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
		
		//Map<Long,IOFSwitch> switchmap = this.floodlightProvider. getAllSwitchMap();
		
		    	
		// if the action is allow ,we have to calculate the route		    	      
        //step 2.1  calculate the route in order to get the output port
    //    int outport = getOutPort(sw, pi, cntx);
  //      if (outport == -1){
   //     	logger.info("Some error happened!!!");			        	
//	        return Command.CONTINUE;
 //       }
    //    int routport = pi.getInPort();
   //     logger.info("calculating the port in :{}, out:{}",routport,outport);
        
        //judge whether this packet is a DNS request packet
        if(pi.getMatch().getNetworkProtocol() != 17 || pi.getMatch().getTransportDestination() != 53)
        {
        	logger.info("This is not a DNS request packet");
        	return Command.CONTINUE;
        }
        	
        
        //TODO: finish the ST and AT entries
        //step 2.2 make st entrys 
        String strFwd = SFAUtil.getFDirectionDataFromPktIn(bitMap, pi, cntx);
        String strBwd = SFAUtil.getBDirectionDataFromPktIn(bitMap, pi, cntx);

        STDATA stdat1 = new STDATA(DdnsraState.DDRA_STATE_START.getValue(),strFwd.getBytes());
        STDATA stdat2 = new STDATA(DdnsraState.DDRA_STATE_REQUEST_SENT.getValue(),strBwd.getBytes());
        
        SFAStMod stmodmsg = new SFAStMod();

        stmodmsg.addSTMod(appId, SFAModType.ENTRY_ADD, stdat1);
        stmodmsg.addSTMod(appId, SFAModType.ENTRY_ADD, stdat2);		        
        
        try{
	        logger.info("-----send st mod msg to switch {}",ddnsra.getId());		        
	        ddnsra.write(stmodmsg, null);
	        //sw.flush();
        }catch (IOException e){
        	 logger.error("Error", e);
        }
        
        //step 2.3 make at entrys
        SFAAtMod atmodmsg = new SFAAtMod();

        int notUsedPort = 0;
        ATDATA atdat1 = new ATDATA(DdnsraState.DDRA_STATE_START.getValue(),strFwd.getBytes(),new SFAAction(ActType.ACT_OUTPUT,notUsedPort));
        ATDATA atdat2 = new ATDATA(DdnsraState.DDRA_STATE_START.getValue(),strBwd.getBytes(),new SFAAction(ActType.ACT_OUTPUT,notUsedPort));
        ATDATA atdat3 = new ATDATA(DdnsraState.DDRA_STATE_DEFAULT_ERROR.getValue(),strFwd.getBytes(),new SFAAction(ActType.ACT_DROP,0));
        ATDATA atdat4 = new ATDATA(DdnsraState.DDRA_STATE_DEFAULT_ERROR.getValue(),strBwd.getBytes(),new SFAAction(ActType.ACT_DROP,0));
        
        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat1);
        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat2);
        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat3);
        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat4);
        //atmodmsg.sendmsg(sw);
        try{
        	logger.info("-----send at mod msg to switch {}",ddnsra.getId());
        	ddnsra.write(atmodmsg, null);
	        //sw.flush();
	        }catch (IOException e){
	        	 logger.error("Error", e);
	        }
        
        //step 2.4 make flow entries (both directions)
        //logger.info("----- sfa make goto_fp msg -----");
  /*      OFFlowMod fm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
        OFMatch match = new OFMatch();
        match.ipMatchLoadFromPacket(pi.getPacketData(), routport);
        EnumSet<OFOXMFieldType> mynonWildcards = EnumSet.of(OFOXMFieldType.IPV4_SRC, OFOXMFieldType.IPV4_DST, OFOXMFieldType.ETH_TYPE, OFOXMFieldType.IP_PROTO, OFOXMFieldType.UDP_SRC, OFOXMFieldType.UDP_DST);
        match.setNonWildcards(mynonWildcards);			        
        
        long cookie = AppCookie.makeCookie(DDNSRA_APP_ID, 0);
        fm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
        .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
        .setBufferId(pi.getBufferId())
        .setCookie(cookie)
        .setMatch(match)
        .setPriority((short) 1) //set the gotofp priority highest
        .setCommand(OFFlowMod.OFPFC_ADD)
        .setInstructions(Arrays.asList((OFInstruction) new OFInstructionGotoFP((byte)1) ));
        //.setInstructions(actions);					        
        fm=fm.clone();
        
        logger.info("buffer id :{}",pi.getBufferId());
        		
        //send reverse msg
        OFFlowMod rfm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
        OFMatch rmatch = new OFMatch();
        rmatch.ipReverseMatchLoadFromPacket(pi.getPacketData(), outport);
        EnumSet<OFOXMFieldType> rmynonWildcards = EnumSet.of(OFOXMFieldType.IPV4_DST, OFOXMFieldType.IPV4_SRC, OFOXMFieldType.ETH_TYPE, OFOXMFieldType.IP_PROTO, OFOXMFieldType.UDP_DST, OFOXMFieldType.UDP_SRC);
        match.setNonWildcards(rmynonWildcards);			        
        
        long rcookie = AppCookie.makeCookie(DDNSRA_APP_ID, 0);
        rfm.setIdleTimeout(FLOWMOD_DEFAULT_IDLE_TIMEOUT)
        .setHardTimeout(FLOWMOD_DEFAULT_HARD_TIMEOUT)
        .setBufferId(OFPacketOut.BUFFER_ID_NONE)
        .setCookie(cookie)
        .setMatch(rmatch)
        .setPriority((short) 1) //set the gotofp priority highest
        .setCommand(OFFlowMod.OFPFC_ADD)
        .setInstructions(Arrays.asList((OFInstruction) new OFInstructionGotoFP((byte)1) ));
        //.setInstructions(actions);					        
        rfm=rfm.clone();
        
        
        try{			        	
        	ddnsra.write(fm, null);
        	ddnsra.write(rfm, null);
        	logger.info("-----send gotofp msg to switch {}",ddnsra.getId());
        	logger.info("flow entry 1 : src:{}, dst:{}", IPv4.fromIPv4Address(fm.getMatch().getNetworkSource()),
        								  IPv4.fromIPv4Address(fm.getMatch().getNetworkDestination()));
        	logger.info("flow entry 2 : reverse path src:{},dst:{}",IPv4.fromIPv4Address(rfm.getMatch().getNetworkSource()),
        											 IPv4.fromIPv4Address(rfm.getMatch().getNetworkDestination()));

        }catch (IOException e) {
            logger.error("Error", e);
        }
   */     
        return Command.STOP;
		    
	// do the normal output 
//	logger.info("-----do the normal output-----!");
//	decision = new RoutingDecision(sw.getId(), pi.getInPort()
//    		, IDeviceService.fcStore.
//            get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
//            IRoutingDecision.RoutingAction.FORWARD_OR_FLOOD);
//	EnumSet<OFOXMFieldType> ipnonWildcards = EnumSet.noneOf(OFOXMFieldType.class);
//	ipnonWildcards = EnumSet.of(OFOXMFieldType.IN_PORT, OFOXMFieldType.VLAN_VID,
//             OFOXMFieldType.ETH_SRC, OFOXMFieldType.ETH_DST,
//             OFOXMFieldType.ETH_TYPE ,OFOXMFieldType.IPV4_SRC, OFOXMFieldType.IPV4_DST);
//	decision.setNonWildcards(ipnonWildcards);
//    decision.addToContext(cntx);
//    return Command.CONTINUE;

	}
		
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
				//statefirewall goto_fp(1)  output(port)
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

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
		// We are the class that implements the service
			m.put(Ddnsra.class, this);
			return m;
	}		


}
