package org.openflow.protocol;


/**
 * Represents a features request message
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public class OFFeaturesRequest extends OFMessage {
    public OFFeaturesRequest() {
        super();
        this.type = OFType.FEATURES_REQUEST;
    }
}
