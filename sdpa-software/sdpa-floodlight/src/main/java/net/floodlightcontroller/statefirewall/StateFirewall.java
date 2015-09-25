package net.floodlightcontroller.statefirewall;


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
import net.floodlightcontroller.firewall.FirewallRule;
import net.floodlightcontroller.firewall.FirewallWebRoutable;
import net.floodlightcontroller.firewall.IFirewallService;
import net.floodlightcontroller.firewall.NonWildcardsPair;
import net.floodlightcontroller.firewall.RuleWildcardsPair;

import java.util.ArrayList;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.IRoutingDecision;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.routing.RoutingDecision;
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


/**
 * Stateful firewall implemented based on sfa.
 * Configuration done through REST API
 * 
 * @author eric@2014.11.2
 * 
 * Description:
 * 
 * 
		 

 */
public class StateFirewall implements IFirewallService, IOFMessageListener,
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
    public static boolean stateFirewallPushed = false;
	
	//save the specific firewall switch
	protected IOFSwitch statFirewall = null;
	protected static final int appId = 1;
	protected static final long bitMap = (1<<15)|(1<<16);
	protected static boolean isInit;
	/*public static final int STATEFIREWALL_APP_ID = 33;
    static {
        AppCookie.registerApp(STATEFIREWALL_APP_ID, "StateFireWallAPP");
    } */

		
		//use the original firewall rule
	protected List<FirewallRule> rules; // protected by synchronized
	protected boolean enabled;
	protected int subnet_mask = IPv4.toIPv4Address("255.255.255.0");
		
	// constant strings for storage/parsing
	public static final String TABLE_NAME = "controller_statefirewall";
	public static final String COLUMN_RULEID = "ruleid";
	public static final String COLUMN_DPID = "dpid";
	public static final String COLUMN_IN_PORT = "in_port";
	public static final String COLUMN_DL_SRC = "dl_src";
	public static final String COLUMN_DL_DST = "dl_dst";
	public static final String COLUMN_DL_TYPE = "dl_type";
	public static final String COLUMN_NW_SRC_PREFIX = "nw_src_prefix";
	public static final String COLUMN_NW_SRC_MASKBITS = "nw_src_maskbits";
	public static final String COLUMN_NW_DST_PREFIX = "nw_dst_prefix";
	public static final String COLUMN_NW_DST_MASKBITS = "nw_dst_maskbits";
	public static final String COLUMN_NW_PROTO = "nw_proto";
	public static final String COLUMN_TP_SRC = "tp_src";
	public static final String COLUMN_TP_DST = "tp_dst";
	public static final String COLUMN_WILDCARD_DPID = "wildcard_dpid";
	public static final String COLUMN_WILDCARD_IN_PORT = "wildcard_in_port";
	public static final String COLUMN_WILDCARD_DL_SRC = "wildcard_dl_src";
	public static final String COLUMN_WILDCARD_DL_DST = "wildcard_dl_dst";
	public static final String COLUMN_WILDCARD_DL_TYPE = "wildcard_dl_type";
	public static final String COLUMN_WILDCARD_NW_SRC = "wildcard_nw_src";
	public static final String COLUMN_WILDCARD_NW_DST = "wildcard_nw_dst";
	public static final String COLUMN_WILDCARD_NW_PROTO = "wildcard_nw_proto";
	public static final String COLUMN_WILDCARD_TP_SRC = "wildcard_tp_src";
	public static final String COLUMN_WILDCARD_TP_DST = "wildcard_tp_dst";
	public static final String COLUMN_PRIORITY = "priority";
	public static final String COLUMN_ACTION = "action";
	public static String ColumnNames[] = { COLUMN_RULEID, COLUMN_DPID,
		COLUMN_IN_PORT, COLUMN_DL_SRC, COLUMN_DL_DST, COLUMN_DL_TYPE,
		COLUMN_NW_SRC_PREFIX, COLUMN_NW_SRC_MASKBITS, COLUMN_NW_DST_PREFIX,
		COLUMN_NW_DST_MASKBITS, COLUMN_NW_PROTO, COLUMN_TP_SRC,
		COLUMN_TP_DST, COLUMN_WILDCARD_DPID, COLUMN_WILDCARD_IN_PORT,
		COLUMN_WILDCARD_DL_SRC, COLUMN_WILDCARD_DL_DST,
		COLUMN_WILDCARD_DL_TYPE, COLUMN_WILDCARD_NW_SRC,
		COLUMN_WILDCARD_NW_DST, COLUMN_WILDCARD_NW_PROTO, COLUMN_PRIORITY,
		COLUMN_ACTION };
		
	@Override
	public String getName() {
		return "statefirewall";
	}
		
	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return (type.equals(OFType.PACKET_IN) &&
                (name.equals("topology") ||
                name.equals("devicemanager")));
	}
		
	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return (type.equals(OFType.PACKET_IN) && name.equals("forwarding"));
	}
		
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFirewallService.class);
		return l;
	}
		
	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
	// We are the class that implements the service
		m.put(IFirewallService.class, this);
		return m;
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
		
	/**
	* Reads the rules from the storage and creates a sorted arraylist of
	* FirewallRule from them.
	* 
	* Similar to getStorageRules(), which only reads contents for REST GET and
	* does no parsing, checking, nor putting into FirewallRule objects
	* 
	* @return the sorted arraylist of FirewallRule instances (rules from
	*         storage)
	*/
	protected ArrayList<FirewallRule> readRulesFromStorage() {
		ArrayList<FirewallRule> l = new ArrayList<FirewallRule>();
		
		try {
				Map<String, Object> row;
		
				// (..., null, null) for no predicate, no ordering
				IResultSet resultSet = storageSource.executeQuery(TABLE_NAME,
		            ColumnNames, null, null);
		
				// put retrieved rows into FirewallRules
				for (Iterator<IResultSet> it = resultSet.iterator(); it.hasNext();) {
					row = it.next().getRow();
					// now, parse row
					FirewallRule r = new FirewallRule();
					if (!row.containsKey(COLUMN_RULEID)
		                || !row.containsKey(COLUMN_DPID)) {
						logger.error(
		                    "skipping entry with missing required 'ruleid' or 'switchid' entry: {}",
		                    row);
						return l;
					}
		        
					try {
						r.ruleid = Integer
		                    .parseInt((String) row.get(COLUMN_RULEID));
						r.dpid = Long.parseLong((String) row.get(COLUMN_DPID));
		
						for (String key : row.keySet()) {
							if (row.get(key) == null)
								continue;
							if (key.equals(COLUMN_RULEID)
									|| key.equals(COLUMN_DPID)
									|| key.equals("id")) {
								continue; // already handled
							}
		                
			                else if (key.equals(COLUMN_IN_PORT)) {
			                    r.in_port = Short.parseShort((String) row
			                            .get(COLUMN_IN_PORT));
			                } 
			                
			                else if (key.equals(COLUMN_DL_SRC)) {
			                    r.dl_src = Long.parseLong((String) row
			                            .get(COLUMN_DL_SRC));
			                } 
			                
			                else if (key.equals(COLUMN_DL_DST)) {
			                    r.dl_dst = Long.parseLong((String) row
			                            .get(COLUMN_DL_DST));
			                } 
			                
			                else if (key.equals(COLUMN_DL_TYPE)) {
			                    r.dl_type = Short.parseShort((String) row
			                            .get(COLUMN_DL_TYPE));
			                } 
			                
			                else if (key.equals(COLUMN_NW_SRC_PREFIX)) {
			                    r.nw_src_prefix = Integer.parseInt((String) row
			                            .get(COLUMN_NW_SRC_PREFIX));
			                } 
			                
			                else if (key.equals(COLUMN_NW_SRC_MASKBITS)) {
			                    r.nw_src_maskbits = Integer.parseInt((String) row
			                            .get(COLUMN_NW_SRC_MASKBITS));
			                } 
			                
			                else if (key.equals(COLUMN_NW_DST_PREFIX)) {
			                    r.nw_dst_prefix = Integer.parseInt((String) row
			                            .get(COLUMN_NW_DST_PREFIX));
			                } 
			                
			                else if (key.equals(COLUMN_NW_DST_MASKBITS)) {
			                    r.nw_dst_maskbits = Integer.parseInt((String) row
			                            .get(COLUMN_NW_DST_MASKBITS));
			                } 
			                
			                else if (key.equals(COLUMN_NW_PROTO)) {
			                    r.nw_proto = Short.parseShort((String) row
			                            .get(COLUMN_NW_PROTO));
			                } 
			                
			                else if (key.equals(COLUMN_TP_SRC)) {
			                    r.tp_src = Short.parseShort((String) row
			                            .get(COLUMN_TP_SRC));
			                } 
			                
			                else if (key.equals(COLUMN_TP_DST)) {
			                    r.tp_dst = Short.parseShort((String) row
			                            .get(COLUMN_TP_DST));
			                } 
			                
			                else if (key.equals(COLUMN_WILDCARD_DPID)) {
			                    r.wildcard_dpid = Boolean.parseBoolean((String) row
			                            .get(COLUMN_WILDCARD_DPID));
			                } 
			                
			                else if (key.equals(COLUMN_WILDCARD_IN_PORT)) {
			                    r.wildcard_in_port = Boolean
			                            .parseBoolean((String) row
			                                    .get(COLUMN_WILDCARD_IN_PORT));
			                } 
			                
			                else if (key.equals(COLUMN_WILDCARD_DL_SRC)) {
			                    r.wildcard_dl_src = Boolean
			                            .parseBoolean((String) row
			                                    .get(COLUMN_WILDCARD_DL_SRC));
			                } 
			                
			                else if (key.equals(COLUMN_WILDCARD_DL_DST)) {
			                    r.wildcard_dl_dst = Boolean
			                            .parseBoolean((String) row
			                                    .get(COLUMN_WILDCARD_DL_DST));
			                } 
			                
			                else if (key.equals(COLUMN_WILDCARD_DL_TYPE)) {
			                    r.wildcard_dl_type = Boolean
			                            .parseBoolean((String) row
			                                    .get(COLUMN_WILDCARD_DL_TYPE));
			                } 
			                
			                else if (key.equals(COLUMN_WILDCARD_NW_SRC)) {
			                    r.wildcard_nw_src = Boolean
			                            .parseBoolean((String) row
			                                    .get(COLUMN_WILDCARD_NW_SRC));
			                } 
			                
			                else if (key.equals(COLUMN_WILDCARD_NW_DST)) {
			                    r.wildcard_nw_dst = Boolean
			                            .parseBoolean((String) row
			                                    .get(COLUMN_WILDCARD_NW_DST));
			                } 
			                
			                else if (key.equals(COLUMN_WILDCARD_NW_PROTO)) {
			                    r.wildcard_nw_proto = Boolean
			                            .parseBoolean((String) row
			                                    .get(COLUMN_WILDCARD_NW_PROTO));
			                } 
			                
			                else if (key.equals(COLUMN_PRIORITY)) {
			                    r.priority = Integer.parseInt((String) row
			                            .get(COLUMN_PRIORITY));
			                } 
			                
			                else if (key.equals(COLUMN_ACTION)) {
			                    int tmp = Integer.parseInt((String) row.get(COLUMN_ACTION));
			                    if (tmp == FirewallRule.FirewallAction.DENY.ordinal())
			                        r.action = FirewallRule.FirewallAction.DENY;
			                    else if (tmp == FirewallRule.FirewallAction.ALLOW.ordinal())
			                        r.action = FirewallRule.FirewallAction.ALLOW;
			                    else {
			                        r.action = null;
			                        logger.error("action not recognized");
			                    }
			                }
			            }
					} catch (ClassCastException e) {
						logger.error(
								"skipping rule {} with bad data : "
										+ e.getMessage(), r.ruleid);
					}
					if (r.action != null)
						l.add(r);
				}
			} catch (StorageException e) {
				logger.error("failed to access storage: {}", e.getMessage());
				// if the table doesn't exist, then wait to populate later via
				// setStorageSource()
			}
		
			// now, sort the list based on priorities
			Collections.sort(l);
		
			return l;
	}
		
	@Override
	public void init(FloodlightModuleContext context)

		    throws FloodlightModuleException {
		floodlightProvider = context
		        .getServiceImpl(IFloodlightProviderService.class);
		storageSource = context.getServiceImpl(IStorageSourceService.class);
		restApi = context.getServiceImpl(IRestApiService.class);
		rules = new ArrayList<FirewallRule>();
		logger = LoggerFactory.getLogger(StateFirewall.class);
		

		routingEngine = context.getServiceImpl(IRoutingService.class);
        topology = context.getServiceImpl(ITopologyService.class);
        deviceManager = context.getServiceImpl(IDeviceService.class);
        
        messageDamper = new OFMessageDamper(OFMESSAGE_DAMPER_CAPACITY,
                EnumSet.of(OFType.FLOW_MOD),
                OFMESSAGE_DAMPER_TIMEOUT);

		// start disabled
        enabled = true;
        
        if( enabled == true){
        	logger.info("----- sfa firewall has enabled ! -----");
        }else{
        	logger.info("----- sfa firewall has turned down !-----");
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
		restApi.addRestletRoutable(new FirewallWebRoutable());
			

		// always place firewall in pipeline at bootup
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		
		// storage, create table and read rules
		storageSource.createTable(TABLE_NAME, null);
		storageSource.setTablePrimaryKeyName(TABLE_NAME, COLUMN_RULEID);
		this.rules = readRulesFromStorage();


		//do send the init msg to spe, we first have to loacte the firewall switch
		// 
		// 1. use floodlightserviceprovider to get all the switch list
		// 2. use the fix name for firewall to locate the firewall node.

		if (!isInit){
			Map<Long,IOFSwitch> switchmap = this.floodlightProvider. getAllSwitchMap();				
			if( switchmap.size() != 0){
				logger.info("-----sfa init the sfa table in startup function use switch 1 as firewall -----");
				statFirewall = switchmap.get(switchId);
				doSendSfaInitMsg(statFirewall);
				isInit = true;
			}
			
		}
			
	}
			
	public void doSendSfaInitMsg(IOFSwitch sw){
			
		logger.info("-----send init msg to switch {}, msg type is {}------",sw.getId(),OFType.SFA_CREATE.getTypeValue());
		SFASt st_tmp = new SFASt();
		st_tmp.setAppid(appId);
		st_tmp.setBitmap(bitMap);
			
			
		//init at table
		SFAAt at_tmp = new SFAAt();
		at_tmp.setBitmap(bitMap);
				
		//init stt table
		SFAStt stt_tmp = new SFAStt();

		STTDATA []sttdat = {new STTDATA(SFAEventType.SFAPARAM_TP_FLAG, 1, SFAEventType.SFAPARAM_CONST, 2, SFAEventOp.OPRATOR_BITAND,
				StateFirewallStatus.SFW_STATUS_REQUESTER_NONE.getValue(), StateFirewallStatus.SFW_STATUS_REQUESTER_SYN_SENT.getValue()),
							new STTDATA(SFAEventType.SFAPARAM_TP_FLAG, 1, SFAEventType.SFAPARAM_CONST, 2, SFAEventOp.OPRATOR_BITAND,
				StateFirewallStatus.SFW_STATUS_RESPONSER_NONE.getValue(), StateFirewallStatus.SFW_STATUS_RESPONSER_ESTABLISH.getValue()),
							new STTDATA(SFAEventType.SFAPARAM_TP_FLAG, 1, SFAEventType.SFAPARAM_CONST, 16, SFAEventOp.OPRATOR_BITAND,
				StateFirewallStatus.SFW_STATUS_REQUESTER_SYN_SENT.getValue(), StateFirewallStatus.SFW_STATUS_REQUESTER_ESTABLISH.getValue()),
//							new STTDATA(SFAEventType.SFAPARAM_TP_FLAG, 1, SFAEventType.SFAPARAM_CONST, 1, SFAEventOp.OPRATOR_BITAND,
//				StateFirewallStatus.SFW_STATUS_REQUESTER_ESTABLISH.getValue(), StateFirewallStatus.SFW_STATUS_REQUESTER_FIN_SENT.getValue()),
							new STTDATA(SFAEventType.SFAPARAM_TP_FLAG, 1, SFAEventType.SFAPARAM_CONST, 1, SFAEventOp.OPRATOR_BITAND,
				StateFirewallStatus.SFW_STATUS_REQUESTER_ESTABLISH.getValue(), StateFirewallStatus.SFW_STATUS_REQUESTER_NONE.getValue()),
							new STTDATA(SFAEventType.SFAPARAM_TP_FLAG, 1, SFAEventType.SFAPARAM_CONST, 1, SFAEventOp.OPRATOR_BITAND,
				StateFirewallStatus.SFW_STATUS_RESPONSER_ESTABLISH.getValue(), StateFirewallStatus.SFW_STATUS_RESPONSER_NONE.getValue()),
//							new STTDATA(SFAEventType.SFAPARAM_TP_FLAG, 1, SFAEventType.SFAPARAM_CONST, 16, SFAEventOp.OPRATOR_BITAND,
//				StateFirewallStatus.SFW_STATUS_REQUESTER_FIN_SENT.getValue(), StateFirewallStatus.SFW_STATUS_REQUESTER_NONE.getValue()),
							new STTDATA(SFAEventType.SFAPARAM_TP_FLAG, 1, SFAEventType.SFAPARAM_CONST, 16, SFAEventOp.OPRATOR_BITAND,
				StateFirewallStatus.SFW_STATUS_REQUESTER_ESTABLISH.getValue(), StateFirewallStatus.SFW_STATUS_REQUESTER_ESTABLISH.getValue()),
							new STTDATA(SFAEventType.SFAPARAM_TP_FLAG, 1, SFAEventType.SFAPARAM_CONST, 16, SFAEventOp.OPRATOR_BITAND,
				StateFirewallStatus.SFW_STATUS_RESPONSER_ESTABLISH.getValue(), StateFirewallStatus.SFW_STATUS_RESPONSER_ESTABLISH.getValue())
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
			statFirewall = switchmap.get(switchId);
			logger.info("-----sfa init the sfa table in first recv function -----");	
			doSendSfaInitMsg(statFirewall);
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
		
	@Override
	public void enableFirewall(boolean enabled) {
		logger.info("Setting firewall to {}", enabled);
		this.enabled = enabled;
	}
		
	@Override
	public List<FirewallRule> getRules() {
		return this.rules;
	}
		
	// Only used to serve REST GET
	// Similar to readRulesFromStorage(), which actually checks and stores
	// record into FirewallRule list
	@Override
	public List<Map<String, Object>> getStorageRules() {

		ArrayList<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		try {
		    // null1=no predicate, null2=no ordering
		    IResultSet resultSet = storageSource.executeQuery(TABLE_NAME,
		            ColumnNames, null, null);
		    for (Iterator<IResultSet> it = resultSet.iterator(); it.hasNext();) {
		        l.add(it.next().getRow());
		    }
		} catch (StorageException e) {
		    logger.error("failed to access storage: {}", e.getMessage());
		    // if the table doesn't exist, then wait to populate later via
		    // setStorageSource()
		}
		return l;

	}
		
	@Override
	public String getSubnetMask() {
		return IPv4.fromIPv4Address(this.subnet_mask);
	}
		
	@Override
	public void setSubnetMask(String newMask) {
		if (newMask.trim().isEmpty())
		    return;
		this.subnet_mask = IPv4.toIPv4Address(newMask.trim());
	}
		
	@Override
	public synchronized void addRule(FirewallRule rule) {

		
		// generate random ruleid for each newly created rule
		// may want to return to caller if useful
		// may want to check conflict
		rule.ruleid = rule.genID();
		
		int i = 0;
		// locate the position of the new rule in the sorted arraylist
		for (i = 0; i < this.rules.size(); i++) {
		    if (this.rules.get(i).priority >= rule.priority)
		        break;
		}
		// now, add rule to the list
		if (i <= this.rules.size()) {
		    this.rules.add(i, rule);
		} else {
		    this.rules.add(rule);
		}
		// add rule to database
		Map<String, Object> entry = new HashMap<String, Object>();
		entry.put(COLUMN_RULEID, Integer.toString(rule.ruleid));
		entry.put(COLUMN_DPID, Long.toString(rule.dpid));
		entry.put(COLUMN_IN_PORT, Short.toString(rule.in_port));
		entry.put(COLUMN_DL_SRC, Long.toString(rule.dl_src));
		entry.put(COLUMN_DL_DST, Long.toString(rule.dl_dst));
		entry.put(COLUMN_DL_TYPE, Short.toString(rule.dl_type));
		entry.put(COLUMN_NW_SRC_PREFIX, Integer.toString(rule.nw_src_prefix));
		entry.put(COLUMN_NW_SRC_MASKBITS, Integer.toString(rule.nw_src_maskbits));
		entry.put(COLUMN_NW_DST_PREFIX, Integer.toString(rule.nw_dst_prefix));
		entry.put(COLUMN_NW_DST_MASKBITS, Integer.toString(rule.nw_dst_maskbits));
		entry.put(COLUMN_NW_PROTO, Short.toString(rule.nw_proto));
		entry.put(COLUMN_TP_SRC, Integer.toString(rule.tp_src));
		entry.put(COLUMN_TP_DST, Integer.toString(rule.tp_dst));
		entry.put(COLUMN_WILDCARD_DPID,
		        Boolean.toString(rule.wildcard_dpid));
		entry.put(COLUMN_WILDCARD_IN_PORT,
		        Boolean.toString(rule.wildcard_in_port));
		entry.put(COLUMN_WILDCARD_DL_SRC,
		        Boolean.toString(rule.wildcard_dl_src));
		entry.put(COLUMN_WILDCARD_DL_DST,
		        Boolean.toString(rule.wildcard_dl_dst));
		entry.put(COLUMN_WILDCARD_DL_TYPE,
		        Boolean.toString(rule.wildcard_dl_type));
		entry.put(COLUMN_WILDCARD_NW_SRC,
		        Boolean.toString(rule.wildcard_nw_src));
		entry.put(COLUMN_WILDCARD_NW_DST,
		        Boolean.toString(rule.wildcard_nw_dst));
		entry.put(COLUMN_WILDCARD_NW_PROTO,
		        Boolean.toString(rule.wildcard_nw_proto));
		entry.put(COLUMN_WILDCARD_TP_SRC,
		        Boolean.toString(rule.wildcard_tp_src));
		entry.put(COLUMN_WILDCARD_TP_DST,
		        Boolean.toString(rule.wildcard_tp_dst));
		entry.put(COLUMN_PRIORITY, Integer.toString(rule.priority));
		entry.put(COLUMN_ACTION, Integer.toString(rule.action.ordinal()));
		storageSource.insertRow(TABLE_NAME, entry);

	}
		
	@Override
	public synchronized void deleteRule(int ruleid) {

		Iterator<FirewallRule> iter = this.rules.iterator();
		while (iter.hasNext()) {
		    FirewallRule r = iter.next();
		    if (r.ruleid == ruleid) {
		        // found the rule, now remove it
		        iter.remove();
		        break;
		    }
		}
		// delete from database
		storageSource.deleteRow(TABLE_NAME, Integer.toString(ruleid));

	}
		
	/**
	* Iterates over the firewall rules and tries to match them with the
	* incoming packet (flow). Uses the FirewallRule class's matchWithFlow
	* method to perform matching. It maintains a pair of wildcards (allow and
	* deny) which are assigned later to the firewall's decision, where 'allow'
	* wildcards are applied if the matched rule turns out to be an ALLOW rule
	* and 'deny' wildcards are applied otherwise. Wildcards are applied to
	* firewall decision to optimize flows in the switch, ensuring least number
	* of flows per firewall rule. So, if a particular field is not "ANY" (i.e.
	* not wildcarded) in a higher priority rule, then if a lower priority rule
	* matches the packet and wildcards it, it can't be wildcarded in the
	* switch's flow entry, because otherwise some packets matching the higher
	* priority rule might escape the firewall. The reason for keeping different
	* two different wildcards is that if a field is not wildcarded in a higher
	* priority allow rule, the same field shouldn't be wildcarded for packets
	* matching the lower priority deny rule (non-wildcarded fields in higher
	* priority rules override the wildcarding of those fields in lower priority
	* rules of the opposite type). So, to ensure that wildcards are
	* appropriately set for different types of rules (allow vs. deny), separate
	* wildcards are maintained. Iteration is performed on the sorted list of
	* rules (sorted in decreasing order of priority).
	* 
	* @param sw
	*            the switch instance
	* @param pi
	*            the incoming packet data structure
	* @param cntx
	*            the floodlight context
	* @return an instance of RuleWildcardsPair that specify rule that matches
	*         and the wildcards for the firewall decision
	*/
	protected RuleWildcardsPair matchWithRule(IOFSwitch sw, OFPacketIn pi,

		    FloodlightContext cntx) {
		FirewallRule matched_rule = null;
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
		        IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		NonWildcardsPair nonWildcards = new NonWildcardsPair();
		
		synchronized (rules) {
		    Iterator<FirewallRule> iter = this.rules.iterator();
		    FirewallRule rule = null;
		    // iterate through list to find a matching firewall rule
		    while (iter.hasNext()) {
		        // get next rule from list
		        rule = iter.next();
		
		        // check if rule matches
		        if (rule.matchesFlow(sw.getId(), pi.getInPort(), eth, nonWildcards) == true) {
		            matched_rule = rule;
		            break;
		        }
		    }

	}		
		// make a pair of rule and wildcards, then return it
		// TODO: Fix rule such that it only drops this flow,
		// and not blanket block this switch
		RuleWildcardsPair ret = new RuleWildcardsPair();
		ret.rule = matched_rule;
		if (matched_rule == null || matched_rule.action == FirewallRule.FirewallAction.DENY) {
		    ret.nonWildcards = nonWildcards.drop;
		} else {
		    ret.nonWildcards = nonWildcards.allow;
		}
		return ret;

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
		
		//Map<Long,IOFSwitch> switchmap = this.floodlightProvider. getAllSwitchMap();
		
		
		// Allowing L2 broadcast + ARP broadcast request (also deny malformed
		// broadcasts -> L2 broadcast + L3 unicast)
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
			
			//logger.info("allowing ARP traffic={}",pi); 
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

		
		// check if we have a matching rule for this packet/flow
		// and no decision is taken yet
		
		boolean isTCP = ( eth.getPayload() instanceof IPv4 && ((IPv4)eth.getPayload() ).getPayload() instanceof TCP);
		
		if (decision == null) {
		    RuleWildcardsPair match_ret = this.matchWithRule(sw, pi, cntx);
		    FirewallRule rule = match_ret.rule;
		
		    if ( rule!=null && rule.action == FirewallRule.FirewallAction.DENY  ) 
		    {
		    	// if the rule said to deny, we must nonwildcards drop
		        decision = new RoutingDecision(sw.getId(), pi.getInPort()
		        		, IDeviceService.fcStore.
		                get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
		                IRoutingDecision.RoutingAction.DROP);
		        decision.setNonWildcards(match_ret.nonWildcards);
		        decision.addToContext(cntx);
		        logger.info("Deny rule={} match for PacketIn={}",rule, pi);
		        return Command.CONTINUE;
		        
		    }else if( rule == null && isTCP == true ){
		    	// if tcp pkt but match no rule , we should drop
		    	 logger.info("-----sfa firewall drop the tcp pkt but match no rule ");
		    	 decision = new RoutingDecision(sw.getId(), pi.getInPort()
			        		, IDeviceService.fcStore.
			                get(cntx, IDeviceService.CONTEXT_SRC_DEVICE),
			                IRoutingDecision.RoutingAction.DROP);
			        decision.addToContext(cntx);
			        return Command.CONTINUE;
		    	
		    }else if( ( rule != null && 
		    		   rule.action == FirewallRule.FirewallAction.ALLOW && 
		    		   isTCP && 
		    		   isInit ) == true){
		    	
		    		// if the action is allow ,we have to calculate the route		    	      
			        //step 2.1  calculate the route in order to get the output port
//			        int outport = getOutPort(sw, pi, cntx);
//			        if (outport == -1){
//			        	logger.info("Some error happened!!!");			        	
//				        return Command.CONTINUE;
//			        }
			        int routport = pi.getInPort();
			        //outport = 2;
			     //   logger.info("calculating the port in :{}, out:{}",routport,outport);
			        
			        //logger.info("-----sfa firewall inport: {} , outport : {} ",routport,outport);
			        // to see StateFirewallStatus.java for the content of st stt and at table 
			        
			        //step 2.2 make st entrys 
			        String strFwd = SFAUtil.getFDirectionDataFromPktIn(bitMap, pi, cntx);
			        String strBwd = SFAUtil.getBDirectionDataFromPktIn(bitMap, pi, cntx);

			        STDATA stdat1 = new STDATA(StateFirewallStatus.SFW_STATUS_REQUESTER_NONE.getValue(),strFwd.getBytes());
			        STDATA stdat2 = new STDATA(StateFirewallStatus.SFW_STATUS_RESPONSER_NONE.getValue(),strBwd.getBytes());
			        
			        SFAStMod stmodmsg = new SFAStMod();

			        stmodmsg.addSTMod(appId, SFAModType.ENTRY_ADD, stdat1);
			        stmodmsg.addSTMod(appId,SFAModType.ENTRY_ADD , stdat2);

			        
			        
			        try{
				        logger.info("-----send st mod msg to switch {}",statFirewall.getId());		        
				        statFirewall.write(stmodmsg, null);
				        sw.flush();
			        }catch (IOException e){
			        	 logger.error("Error", e);
			        }
			        
			        //step 2.3 make at entrys
			        SFAAtMod atmodmsg = new SFAAtMod();
			        
			        int notUsedPort = 0;

			        ATDATA atdat1 = new ATDATA(StateFirewallStatus.SFW_STATUS_REQUESTER_NONE.getValue(),strFwd.getBytes(),new SFAAction(ActType.ACT_OUTPUT,notUsedPort));
			        ATDATA atdat2 = new ATDATA(StateFirewallStatus.SFW_STATUS_RESPONSER_NONE.getValue(),strBwd.getBytes(),new SFAAction(ActType.ACT_OUTPUT,notUsedPort));
			       
			        ATDATA atdat3 = new ATDATA(StateFirewallStatus.SFW_STATUS_REQUESTER_SYN_SENT.getValue(),strFwd.getBytes(),new SFAAction(ActType.ACT_OUTPUT,notUsedPort));
			        ATDATA atdat4 = new ATDATA(StateFirewallStatus.SFW_STATUS_RESPONSER_ESTABLISH.getValue(),strBwd.getBytes(),new SFAAction(ActType.ACT_OUTPUT,notUsedPort));
			        ATDATA atdat5 = new ATDATA(StateFirewallStatus.SFW_STATUS_REQUESTER_ESTABLISH.getValue(),strFwd.getBytes(),new SFAAction(ActType.ACT_OUTPUT,notUsedPort));
			        ATDATA atdat6 = new ATDATA(StateFirewallStatus.SFW_STATUS_REQUESTER_FIN_SENT.getValue(),strFwd.getBytes(),new SFAAction(ActType.ACT_OUTPUT,notUsedPort));
			        ATDATA atdat7 = new ATDATA(StateFirewallStatus.SFW_STATUS_DEFAULT_ERROR.getValue(),strFwd.getBytes(),new SFAAction(ActType.ACT_DROP,0));
			        ATDATA atdat8 = new ATDATA(StateFirewallStatus.SFW_STATUS_DEFAULT_ERROR.getValue(),strBwd.getBytes(),new SFAAction(ActType.ACT_DROP,0));
			        
			        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat1);
			        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat2);
			        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat3);
			        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat4);
			        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat5);
			        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat6);
			        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat7);
			        atmodmsg.addATMod(appId, SFAModType.ENTRY_ADD, atdat8);

			        try{
			        	logger.info("-----send at mod msg to switch {}",statFirewall.getId());
			        	statFirewall.write(atmodmsg, null);
			        	stateFirewallPushed = true;
				        sw.flush();
				        }catch (IOException e){
				        	 logger.error("Error", e);
				        }
			        
			        
			        //logger.info("----- sfa make goto_fp msg -----");
	/*		        OFFlowMod fm =(OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);				        
			        OFMatch match = new OFMatch();
			        match.ipMatchLoadFromPacket(pi.getPacketData(), routport);
			        EnumSet<OFOXMFieldType> mynonWildcards = EnumSet.of(OFOXMFieldType.IPV4_SRC, OFOXMFieldType.IPV4_DST,OFOXMFieldType.ETH_TYPE);
			        match.setNonWildcards(mynonWildcards);			        
			        
			        long cookie = AppCookie.makeCookie(STATEFIREWALL_APP_ID, 0);
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
			        EnumSet<OFOXMFieldType> rmynonWildcards = EnumSet.of(OFOXMFieldType.IPV4_DST, OFOXMFieldType.IPV4_SRC,OFOXMFieldType.ETH_TYPE);
			        match.setNonWildcards(rmynonWildcards);			        
			        
			        long rcookie = AppCookie.makeCookie(STATEFIREWALL_APP_ID, 0);
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
			        	statFirewall.write(fm, null);
			        	statFirewall.write(rfm, null);
			        	sw.flush();
			        	logger.info("-----send gotofp msg to switch {}",statFirewall.getId());
			        	logger.info("flow entry 1 : src:{}, dst:{}", IPv4.fromIPv4Address(fm.getMatch().getNetworkSource()),
			        								  IPv4.fromIPv4Address(fm.getMatch().getNetworkDestination()));
			        	logger.info("flow entry 2 : reverse path src:{},dst:{}",IPv4.fromIPv4Address(rfm.getMatch().getNetworkSource()),
			        											 IPv4.fromIPv4Address(rfm.getMatch().getNetworkDestination()));

			        }catch (IOException e) {
			            logger.error("Error", e);
			        }  */
			        //Packet out 
			        //output port --> ofp_table
			        
			        //OFPacketIn pi = (OFPacketIn) msg;
		/*	        OFPacketOut po = (OFPacketOut) floodlightProvider.getOFMessageFactory()
			                .getMessage(OFType.PACKET_OUT);
			        po.setBufferId(pi.getBufferId())
			            .setInPort(pi.getInPort());

			        // set actions
			        OFActionOutput action = new OFActionOutput()
			            .setPort(OFPort.OFPP_TABLE.getValue());
			        po.setActions(Collections.singletonList((OFAction)action));
			        po.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);

			        // set data if is is included in the packetin
			        logger.info("pi = {}", pi);
			        if (pi.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
			        	logger.info("bufferId -----> {} ", pi.getBufferId());
			            byte[] packetData = pi.getPacketData();
			            po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
			                    + po.getActionsLength() + packetData.length));
			            po.setPacketData(packetData);
			        } else {
			            po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
			                    + po.getActionsLength()));
			        }
//			        try {
//			   //         sw.write(po, cntx);
//			        } catch (IOException e) {
//			            logger.error("Failure writing PacketOut", e);
//			        }   */
			        return Command.STOP;

		    	}
		    	
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
		
	@Override
	public boolean isEnabled() {
		return enabled;
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

