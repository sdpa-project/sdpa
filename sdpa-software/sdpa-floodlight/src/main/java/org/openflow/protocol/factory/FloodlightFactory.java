package org.openflow.protocol.factory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;
import org.openflow.protocol.action.OFActionVendor;
import org.openflow.protocol.vendor.OFByteArrayVendorData;
import org.openflow.protocol.vendor.OFVendorData;
import org.openflow.protocol.vendor.OFVendorDataType;
import org.openflow.protocol.vendor.OFVendorId;

/**
 * the extended floodlight factory that supports vendor factories.
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 *
 */
public class FloodlightFactory extends BasicFactory
    implements OFVendorDataFactory {

	private static final FloodlightFactory SINGLETON_INSTANCE = new FloodlightFactory();
    private final OFVendorActionRegistry vendorActionRegistry;
    
    private FloodlightFactory() {
        vendorActionRegistry = OFVendorActionRegistry.getInstance();
    }

    public static FloodlightFactory getInstance() {
        return SINGLETON_INSTANCE;
    }

    @Override
    protected void injectFactories(OFMessage ofm) {
        super.injectFactories(ofm);
        if (ofm instanceof OFVendorDataFactoryAware) {
            ((OFVendorDataFactoryAware)ofm).setVendorDataFactory(this);
        }
    }

    @Override
    public List<OFAction> parseActions(ByteBuffer data, int length, int limit) {
        List<OFAction> results = new ArrayList<OFAction>();
        OFAction demux = new OFAction();
        OFAction ofa;
        int end = data.position() + length;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFAction.MINIMUM_LENGTH ||
                    (data.position() + OFAction.MINIMUM_LENGTH) > end)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining() ||
                    (data.position() + demux.getLengthU()) > end)
                return results;

            OFActionType type = demux.getType();
            ofa = getAction(type);
            ofa.readFrom(data);

            if(type == OFActionType.VENDOR) {
                OFActionVendor vendorAction = (OFActionVendor) ofa;

                OFVendorActionFactory vendorActionFactory = vendorActionRegistry.get(vendorAction.getVendor());

                if(vendorActionFactory != null) {
                    // if we have a specific vendorActionFactory for this vendor id,
                    // delegate to it for vendor-specific reparsing of the message
                    data.reset();
                    OFActionVendor newAction = vendorActionFactory.readFrom(data);
                    if(newAction != null)
                        ofa = newAction;
                }
            }

            if (OFAction.class.equals(ofa.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position()+(ofa.getLengthU() -
                        OFAction.MINIMUM_LENGTH));
            }
            results.add(ofa);
        }

        return results;
    }

    public OFVendorData getVendorData(OFVendorId vendorId,
                                      OFVendorDataType vendorDataType) {
        if (vendorDataType == null)
            return null;

        return vendorDataType.newInstance();
    }

    /**
     * Attempts to parse and return the OFVendorData contained in the given
     * ChannelBuffer, beginning right after the vendor id.
     * @param vendor the vendor id that was parsed from the OFVendor message.
     * @param data the ChannelBuffer from which to parse the vendor data
     * @param length the length to the end of the enclosing message.
     * @return an OFVendorData instance
     */
    public OFVendorData parseVendorData(int vendor, ByteBuffer data,
            int length) {
        OFVendorDataType vendorDataType = null;
        OFVendorId vendorId = OFVendorId.lookupVendorId(vendor);
        if (vendorId != null) {
            data.mark();
            vendorDataType = vendorId.parseVendorDataType(data, length);
            data.reset();
        }

        OFVendorData vendorData = getVendorData(vendorId, vendorDataType);
        if (vendorData == null)
            vendorData = new OFByteArrayVendorData();

        vendorData.readFrom(data, length);

        return vendorData;
    }

    public static String dumpBuffer(ByteBuffer data) {
        // NOTE: Reads all the bytes in buffer from current read offset.
        // Set/Reset ReaderIndex if you want to read from a different location
        int len = data.remaining();
        StringBuffer sb = new StringBuffer();
        for (int i=0 ; i<len; i++) {
            if (i%32 == 0) sb.append("\n");
            if (i%4 == 0) sb.append(" ");
            sb.append(String.format
            		("%02x", data.get(i)));
        }
        return sb.toString();
    }
}
