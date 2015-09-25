package com.bigswitch.floodlight.vendor;

import java.nio.ByteBuffer;
import org.openflow.protocol.action.OFActionVendor;
import org.openflow.protocol.factory.OFVendorActionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OFNiciraVendorActionFactory implements OFVendorActionFactory {
    protected static Logger logger =
            LoggerFactory.getLogger(OFNiciraVendorActionFactory.class);

    static class OFActionNiciraVendorDemux extends OFActionNiciraVendor {
        OFActionNiciraVendorDemux() {
            super((short) 0);
        }
    }

    @Override
    public OFActionVendor readFrom(ByteBuffer data) {
        data.mark();
        OFActionNiciraVendorDemux demux = new OFActionNiciraVendorDemux();
        demux.readFrom(data);
        data.reset();

        switch(demux.getSubtype()) {
            case OFActionNiciraTtlDecrement.TTL_DECREMENT_SUBTYPE:
                OFActionNiciraTtlDecrement ttlAction = new OFActionNiciraTtlDecrement();
                ttlAction.readFrom(data);
                return ttlAction;
            default:
                logger.error("Unknown Nicira vendor action subtype: "+demux.getSubtype());
                return null;
        }
    }

}
