package org.openflow.protocol.factory;

import java.nio.ByteBuffer;
import java.util.List;

import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionType;


/**
 * The interface to factories used for retrieving OFInstruction instances. All
 * methods are expected to be thread-safe.
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFInstructionFactory {
    /**
     * Retrieves an OFInstruction instance corresponding to the specified
     * OFInstructionType
     * @param t the type of the OFInstruction to be retrieved
     * @return an OFInstruction instance
     */
    public OFInstruction getInstruction(OFInstructionType t);

    /**
     * Attempts to parse and return all OFInstruction contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ByteBuffer to parse for OpenFlow instructions
     * @param length the number of Bytes to examine for OpenFlow instructions
     * @return a list of OFInstruction instances
     */
    public List<OFInstruction> parseInstructions(ByteBuffer data, int length);

    /**
     * Attempts to parse and return all OFInstruction contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ByteBuffer to parse for OpenFlow instructions
     * @param length the number of Bytes to examine for OpenFlow instructions
     * @param limit the maximum number of messages to return, 0 means no limit
     * @return a list of OFInstruction instances
     */
    public List<OFInstruction> parseInstructions(ByteBuffer data, int length, int limit);

    /**
     * Retrieves an OFActionFactory
     * @return an OFActionFactory
     */
    public OFActionFactory getActionFactory();
}
