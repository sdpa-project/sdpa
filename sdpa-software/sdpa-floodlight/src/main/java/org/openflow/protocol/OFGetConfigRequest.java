package org.openflow.protocol;

/**
 * Represents an OFPT_GET_CONFIG_REQUEST type message
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFGetConfigRequest extends OFMessage {
    public OFGetConfigRequest() {
        super();
        this.type = OFType.GET_CONFIG_REQUEST;
    }
}
