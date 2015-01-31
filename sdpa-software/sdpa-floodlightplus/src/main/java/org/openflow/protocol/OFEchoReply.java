package org.openflow.protocol;

/**
 * Represents an ofp_echo_reply message
 * 
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */

public class OFEchoReply extends OFEchoRequest {
    public OFEchoReply() {
        super();
        this.type = OFType.ECHO_REPLY;
     }
}
