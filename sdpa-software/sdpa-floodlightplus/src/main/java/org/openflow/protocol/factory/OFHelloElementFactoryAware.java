package org.openflow.protocol.factory;

/**
 * Objects implementing this interface are expected to be instantiated with an
 * instance of an OFHelloElementFactory
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface OFHelloElementFactoryAware {
    /**
     * Sets the OFHelloElementFactory
     * @param queuePropertyFactory
     */
    public void setHelloElementFactory(OFHelloElementFactory helloElementFactory);
}
