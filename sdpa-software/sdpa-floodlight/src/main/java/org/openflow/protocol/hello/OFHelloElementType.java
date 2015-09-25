package org.openflow.protocol.hello;

import java.lang.reflect.Constructor;

import org.openflow.protocol.Instantiable;
import org.openflow.protocol.hello.OFHelloElement;

/**
 * Represents an ofp_hello_element_type enum
 *
 * @author Srini Seetharaaman (srini.seetharaman@gmail.com)
 */
public enum OFHelloElementType {
    VERSION_BITMAP  ((short)1, OFHelloElementVersionBitmap.class, new Instantiable<OFHelloElement>() {
                    @Override
                    public OFHelloElement instantiate() {
                        return new OFHelloElementVersionBitmap();
                    }});

    protected short type;
    protected static OFHelloElementType[] mapping;
    protected Class<? extends OFHelloElement> clazz;
    protected Constructor<? extends OFHelloElement> constructor;
    protected Instantiable<OFHelloElement> instantiable;
    
    OFHelloElementType(short type, Class<? extends OFHelloElement> clazz, Instantiable<OFHelloElement> instantiable) {
        this.type = type;
        this.clazz = clazz;
        this.instantiable = instantiable;
        try {
            this.constructor = clazz.getConstructor(new Class[]{});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failure getting constructor for class: " + clazz, e);
        }
        OFHelloElementType.addMapping(this.type, this);
    }

    /**
     * Adds a mapping from type value to OFHelloElementType enum
     *
     * @param i OpenFlow wire protocol HelloElement type value
     * @param t type
     */
    static public void addMapping(short i, OFHelloElementType t) {
        if (mapping == null)
            mapping = new OFHelloElementType[5];
        OFHelloElementType.mapping[i] = t;
    }

    /**
     * Given a wire protocol OpenFlow type number, return the
     * OFHelloElementType associated with it
     *
     * @param i wire protocol number
     * @return OFHelloElementType enum 
     */

    static public OFHelloElementType valueOf(short i) {
        return OFHelloElementType.mapping[i];
    }

    /**
     * @return the value
     */
    public short getTypeValue() {
        return type;
    }

    /**
     * Returns a new instance of the OFHelloElement represented by this OFHelloElementType
     * @return the new object
     */
    public OFHelloElement newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<OFHelloElement> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable the instantiable to set
     */
    public void setInstantiable(Instantiable<OFHelloElement> instantiable) {
        this.instantiable = instantiable;
    }
}
