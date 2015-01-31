package org.openflow.protocol.meter;

import java.lang.reflect.Constructor;

import org.openflow.protocol.Instantiable;
import org.openflow.util.LRULinkedHashMap;
import java.util.Map;

/**
 * List of OpenFlow meter band types and mappings to wire protocol value and
 * derived classes
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public class OFMeterBandType {
    public static OFMeterBandType DROP = new OFMeterBandType(1, "DROP",
            OFMeterBandDrop.class, new Instantiable<OFMeterBand>() {
                @Override
                public OFMeterBand instantiate() {
                    return new OFMeterBandDrop();
                }
            });

    public static OFMeterBandType DSCP_REMARK = new OFMeterBandType(2, "DSCP_REMARK",
            OFMeterBandDSCPRemark.class, new Instantiable<OFMeterBand>() {
                @Override
                public OFMeterBand instantiate() {
                    return new OFMeterBandDSCPRemark();
                }
            });

/* TODO
    public static OFMeterBandType VENDOR = new OFMeterBandType(1, "VENDOR",
            OFMeterBandVendor.class, new Instantiable<OFMeterBand>() {
                @Override
                public OFMeterBand instantiate() {
                    return new OFMeterBandVendor();
                }
            });
*/
    
    protected Class<? extends OFMeterBand> clazz;
    protected Constructor<? extends OFMeterBand> constructor;
    protected Instantiable<OFMeterBand> instantiable;
    protected String name;
    protected short type;
    protected static Map<Short, OFMeterBandType> mapping;
    private static final int MAX_ENTRIES = 10;

    /**
     * Store some information about the OpenFlow meter band type, including wire
     * protocol type number, length, and derived class
     *
     * @param type Wire protocol number associated with this OFMeterBandType
     * @param name The name of this type
     * @param clazz The Java class corresponding to this type of OpenFlow meter band
     * @param instantiable the instantiable for the OFMeterBand this type represents
     */
    public OFMeterBandType(int type, String name, Class<? extends OFMeterBand> clazz, Instantiable<OFMeterBand> instantiable) {
        this.type = (short) type;
        this.name = name;
        this.clazz = clazz;
        this.instantiable = instantiable;
        try {
            this.constructor = clazz.getConstructor(new Class[]{});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failure getting constructor for class: " + clazz, e);
        }
        OFMeterBandType.addMapping(this.type, this);
    }

    /**
     * Adds a mapping from type value to OFMeterBandType enum
     *
     * @param i OpenFlow wire protocol Action type value
     * @param t type
     */
    static public void addMapping(short i, OFMeterBandType t) {
        if (mapping == null)
            mapping = new LRULinkedHashMap<Short, OFMeterBandType>(MAX_ENTRIES);
        mapping.put(i, t);
    }

    /**
     * Given a wire protocol OpenFlow type number, return the OFMeterBandType associated
     * with it
     *
     * @param i wire protocol number
     * @return OFMeterBandType enum type
     */

    static public OFMeterBandType valueOf(short i) {
        return mapping.get(i);
    }

    /**
     * @return Returns the wire protocol value corresponding to this
     *         OFMeterBandType
     */
    public short getTypeValue() {
        return this.type;
    }

    /**
     * @return return the OFMeterBand subclass corresponding to this OFMeterBandType
     */
    public Class<? extends OFMeterBand> toClass() {
        return clazz;
    }

    /**
     * Returns the no-argument Constructor of the implementation class for
     * this OFMeterBandType
     * @return the constructor
     */
    public Constructor<? extends OFMeterBand> getConstructor() {
        return constructor;
    }

    /**
     * Returns a new instance of the OFMeterBand represented by this OFMeterBandType
     * @return the new object
     */
    public OFMeterBand newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<OFMeterBand> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable the instantiable to set
     */
    public void setInstantiable(Instantiable<OFMeterBand> instantiable) {
        this.instantiable = instantiable;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
