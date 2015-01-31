package org.openflow.protocol.factory;

import java.nio.ByteBuffer;
import java.util.List;

import org.openflow.protocol.statistics.tableFeatures.OFTableFeaturesProperty;
import org.openflow.protocol.statistics.tableFeatures.OFTableFeaturesPropertyType;


/**
 * The interface to factories used for retrieving OFTableFeaturesProperty instances. All
 * methods are expected to be thread-safe.
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public interface OFTableFeaturesPropertyFactory {
    /**
     * Retrieves an OFTableFeaturesProperty instance corresponding to the specified
     * OFTableFeaturesPropertyType
     * @param t the type of the OFTableFeaturesProperty to be retrieved
     * @return an OFTableFeaturesProperty instance
     */
    public OFTableFeaturesProperty getTableFeaturesProperty(OFTableFeaturesPropertyType t);

    /**
     * Attempts to parse and return all OFTableFeaturesProperties contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ByteBuffer to parse for OpenFlow OFTableFeaturesProperties
     * @param length the number of Bytes to examine for OpenFlow OFTableFeaturesProperties
     * @return a list of OFTableFeaturesProperty instances
     */
    public List<OFTableFeaturesProperty> parseTableFeaturesProperties(ByteBuffer data, int length);

    /**
     * Attempts to parse and return all OFTableFeaturesProperties contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ByteBuffer to parse for OpenFlow OFTableFeaturesProperties
     * @param length the number of Bytes to examine for OpenFlow OFTableFeaturesProperties
     * @param limit the maximum number of OFTableFeaturesProperties to return, 0 means no limit
     * @return a list of OFTableFeaturesProperty instances
     */
    public List<OFTableFeaturesProperty> parseTableFeaturesProperties(ByteBuffer data, int length, int limit);
}
