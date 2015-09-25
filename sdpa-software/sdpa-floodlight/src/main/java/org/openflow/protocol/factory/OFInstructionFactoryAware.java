package org.openflow.protocol.factory;

/**
 * Objects implementing this interface are expected to be instantiated with an
 * instance of an OFInstructionFactory
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFInstructionFactoryAware {
    /**
     * Sets the OFInstructionFactory
     * @param instructionFactory
     */
    public void setInstructionFactory(OFInstructionFactory instructionFactory);
}
