package org.openflow.protocol.factory;

import java.nio.ByteBuffer;
import java.util.List;

import org.openflow.protocol.hello.OFHelloElement;
import org.openflow.protocol.hello.OFHelloElementType;


/**
 * The interface to factories used for retrieving OFHelloElement instances. All
 * methods are expected to be thread-safe.
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFHelloElementFactory {
    /**
     * Retrieves an OFHelloElement instance corresponding to the specified
     * OFHelloElementType
     * @param t the type of the OFHelloElement to be retrieved
     * @return an OFHelloElement instance
     */
    public OFHelloElement getHelloElement(OFHelloElementType t);

    /**
     * Attempts to parse and return all OFHelloElement contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ByteBuffer to parse for OpenFlow hellos
     * @param length the number of Bytes to examine for OpenFlow hellos
     * @return a list of OFHelloElement instances
     */
    public List<OFHelloElement> parseHelloElements(ByteBuffer data, int length);

    /**
     * Attempts to parse and return all OFHelloElement contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ByteBuffer to parse for OpenFlow hellos
     * @param length the number of Bytes to examine for OpenFlow hellos
     * @param limit the maximum number of messages to return, 0 means no limit
     * @return a list of OFHelloElement instances
     */
    public List<OFHelloElement> parseHelloElements(ByteBuffer data, int length, int limit);

}
