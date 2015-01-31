package org.openflow.protocol.factory;

/**
 * Objects implementing this interface are expected to be instantiated with an
 * instance of an OFTableFeaturesPropertyFactory
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public interface OFTableFeaturesPropertyFactoryAware {
    /**
     * Sets the OFTableFeaturesPropertyFactory
     * @param tableFeaturesPropertyFactory
     */
    public void setTableFeaturesPropertyFactory(OFTableFeaturesPropertyFactory tableFeaturesPropertyFactory);
}
