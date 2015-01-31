package org.openflow.protocol.factory;

import java.nio.ByteBuffer;
import java.util.List;

import org.openflow.protocol.meter.OFMeterBand;
import org.openflow.protocol.meter.OFMeterBandType;


/**
 * The interface to factories used for retrieving OFMeterBand instances. All
 * methods are expected to be thread-safe.
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFMeterBandFactory {
    /**
     * Retrieves an OFMeterBand instance corresponding to the specified
     * OFMeterBandType
     * @param t the type of the OFMeterBand to be retrieved
     * @return an OFMeterBand instance
     */
    public OFMeterBand getMeterBand(OFMeterBandType t);

    /**
     * Attempts to parse and return all OFMeterBands contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ByteBuffer to parse for OpenFlow OFMeterBands
     * @param length the number of Bytes to examine for OpenFlow OFMeterBands
     * @return a list of OFMeterBand instances
     */
    public List<OFMeterBand> parseMeterBands(ByteBuffer data, int length);

    /**
     * Attempts to parse and return all OFMeterBands contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ByteBuffer to parse for OpenFlow OFMeterBands
     * @param length the number of Bytes to examine for OpenFlow OFMeterBands
     * @param limit the maximum number of OFMeterBands to return, 0 means no limit
     * @return a list of OFMeterBand instances
     */
    public List<OFMeterBand> parseMeterBands(ByteBuffer data, int length, int limit);
}
