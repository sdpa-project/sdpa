package org.openflow.protocol;

import org.openflow.util.U16;

import java.nio.ByteBuffer;
import java.util.List;

import org.openflow.protocol.hello.OFHelloElement;

import org.openflow.protocol.factory.OFHelloElementFactory;
import org.openflow.protocol.factory.OFHelloElementFactoryAware;
/**
 * Represents an ofp_hello message
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFHello extends OFMessage implements OFHelloElementFactoryAware {
    public static int MINIMUM_LENGTH = 8;

    protected List<OFHelloElement> helloElements;
    protected OFHelloElementFactory helloElementFactory;
    /**
     * Construct a ofp_hello message
     */
    public OFHello() {
        super();
        this.type = OFType.HELLO;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    @Override
    public void setHelloElementFactory(OFHelloElementFactory helloElementFactory) {
        this.helloElementFactory = helloElementFactory;
    }

    /**
     * Returns read-only copies of the hello elements contained in this Hello message
     * @return a list of ordered Hello elements
     */
    public List<OFHelloElement> getHelloElements() {
        return this.helloElements;
    }

    /**
     * Sets the list of hello elements this Hello message contains
     * @param helloElements a list of ordered Hello elements
     */
    public OFHello setHelloElements(List<OFHelloElement> helloElements) {
        this.helloElements = helloElements;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        
        if (this.helloElementFactory == null)
            throw new RuntimeException("OFHelloElementFactory not set");
        this.helloElements = this.helloElementFactory.parseHelloElements(data, getLengthU() -
                MINIMUM_LENGTH);
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
    
        if (helloElements != null) {
            for (OFHelloElement helloElement : helloElements) {
                helloElement.writeTo(data);
            }
        }
    }
 
    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        if (helloElements != null) {
            int l = MINIMUM_LENGTH;
            for (OFHelloElement helloElement : helloElements) {
                l += helloElement.getLengthU();
            }
            this.length = U16.t(l);
        }
    }
}
