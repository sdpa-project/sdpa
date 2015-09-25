package org.openflow.protocol;

/**
 * Represents an OFPT_BARRIER_REPLY message
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFBarrierReply extends OFMessage {
    public OFBarrierReply() {
        super();
        this.type = OFType.BARRIER_REPLY;
    }
}
