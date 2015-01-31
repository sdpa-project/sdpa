package org.openflow.protocol.factory;

/**
 * Objects implementing this interface are expected to be instantiated with an
 * instance of an OFMeterBandFactory
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFMeterBandFactoryAware {
    /**
     * Sets the OFMeterBandFactory
     * @param meterBandFactory
     */
    public void setMeterBandFactory(OFMeterBandFactory meterBandFactory);
}
