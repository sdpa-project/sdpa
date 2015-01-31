/**
 *
 */
package org.openflow.protocol.action;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.openflow.protocol.Instantiable;
import org.openflow.util.LRULinkedHashMap;

/**
 * List of OpenFlow Action types and mappings to wire protocol value and
 * derived classes
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public enum OFActionType {
    OUTPUT              (0, OFActionOutput.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionOutput();
                            }}),
   COPY_TTL_OUT         (11, OFActionCopyTTLOut.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionCopyTTLOut();
                            }}),
   COPY_TTL_IN          (12, OFActionCopyTTLIn.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionCopyTTLIn();
                            }}),
   SET_MPLS_TTL         (15, OFActionSetMPLSTTL.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionSetMPLSTTL();
                            }}),
   DEC_MPLS_TTL         (16, OFActionDecrementMPLSTTL.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionDecrementMPLSTTL();
                            }}),
    PUSH_VLAN           (17, OFActionPushVLAN.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionPushVLAN();
                            }}),
    POP_VLAN            (18, OFActionPopVLAN.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionPopVLAN();
                            }}),
    PUSH_MPLS           (19, OFActionPushMPLS.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionPushMPLS();
                            }}),
    POP_MPLS            (20, OFActionPopMPLS.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionPopMPLS();
                            }}),
    SET_QUEUE           (21, OFActionSetQueue.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionSetQueue();
                            }}),
    GROUP               (22, OFActionGroup.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionGroup();
                            }}),
    SET_NW_TTL          (23, OFActionSetNwTTL.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionSetNwTTL();
                            }}),
    DEC_NW_TTL          (24, OFActionDecrementNwTTL.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionDecrementNwTTL();
                            }}),
    SET_FIELD           (25, OFActionSetField.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionSetField();
                            }}),
    PUSH_PBB            (26, OFActionPushPBB.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionPushPBB();
                            }}),
    POP_PBB             (27, OFActionPopPBB.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionPopPBB();
                            }}),
    VENDOR        (0xffff, OFActionVendor.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionVendor();
                            }});

    protected static Map<Short, OFActionType> mapping;
    private static final int MAX_ENTRIES = 32;

    protected Class<? extends OFAction> clazz;
    protected Constructor<? extends OFAction> constructor;
    protected Instantiable<OFAction> instantiable;
    protected short type;
    
    /**
     * Store some information about the OpenFlow Action type, including wire
     * protocol type number, length, and derrived class
     *
     * @param clazz The Java class corresponding to this type of OpenFlow Action
     * @param instantiable the instantiable for the OFAction this type represents
     */
    OFActionType(int type, Class<? extends OFAction> clazz, Instantiable<OFAction> instantiable) {
        this.type = (short) type;
        this.clazz = clazz;
        this.instantiable = instantiable;
        try {
            this.constructor = clazz.getConstructor(new Class[]{});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failure getting constructor for class: " + clazz, e);
        }
        OFActionType.addMapping(this.type, this);
    }

    /**
     * Adds a mapping from type value to OFActionType enum
     *
     * @param i OpenFlow wire protocol Action type value
     * @param t type
     */
    static public void addMapping(short i, OFActionType t) {
        if (mapping == null)
            mapping = new LRULinkedHashMap<Short, OFActionType>(MAX_ENTRIES);

        mapping.put(i, t);
    }

    /**
     * Given a wire protocol OpenFlow type number, return the OFActionType associated
     * with it
     *
     * @param i wire protocol number
     * @return OFActionType enum type
     */

    static public OFActionType valueOf(short i) {
        return mapping.get(i);
    }

    /**
     * @return Returns the wire protocol value corresponding to this
     *         OFActionType
     */
    public short getTypeValue() {
        return this.type;
    }

    /**
     * @return return the OFAction subclass corresponding to this OFActionType
     */
    public Class<? extends OFAction> toClass() {
        return clazz;
    }

    /**
     * Returns the no-argument Constructor of the implementation class for
     * this OFActionType
     * @return the constructor
     */
    public Constructor<? extends OFAction> getConstructor() {
        return constructor;
    }

    /**
     * Returns a new instance of the OFAction represented by this OFActionType
     * @return the new object
     */
    public OFAction newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<OFAction> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable the instantiable to set
     */
    public void setInstantiable(Instantiable<OFAction> instantiable) {
        this.instantiable = instantiable;
    }
}
