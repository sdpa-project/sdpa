package org.openflow.protocol.instruction;

import java.lang.reflect.Constructor;

import org.openflow.protocol.Instantiable;
import org.openflow.util.LRULinkedHashMap;
import java.util.Map;

/**
 * List of OpenFlow Instruction types and mappings to wire protocol value and
 * derived classes
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public enum OFInstructionType {
    GOTO_TABLE          (1, OFInstructionGotoTable.class, new Instantiable<OFInstruction>() {
                            @Override
                            public OFInstruction instantiate() {
                                return new OFInstructionGotoTable();
                            }}),
    WRITE_METADATA      (2, OFInstructionWriteMetaData.class, new Instantiable<OFInstruction>() {
                            @Override
                            public OFInstruction instantiate() {
                                return new OFInstructionWriteMetaData();
                            }}),
    WRITE_ACTIONS       (3, OFInstructionWriteActions.class, new Instantiable<OFInstruction>() {
                            @Override
                            public OFInstruction instantiate() {
                                return new OFInstructionWriteActions();
                            }}),
    APPLY_ACTIONS       (4, OFInstructionApplyActions.class, new Instantiable<OFInstruction>() {
                            @Override
                            public OFInstruction instantiate() {
                                return new OFInstructionApplyActions();
                            }}),
    CLEAR_ACTIONS       (5, OFInstructionClearActions.class, new Instantiable<OFInstruction>() {
                            @Override
                            public OFInstruction instantiate() {
                                return new OFInstructionClearActions();
                            }}),
    //@sc
    GOTO_FP             (7, OFInstructionGotoFP.class, new Instantiable<OFInstruction>() {
                                @Override
                                public OFInstruction instantiate() {
                                    return new OFInstructionGotoFP();
                                }}),
    METER               (6, OFInstructionMeter.class, new Instantiable<OFInstruction>() {
                            @Override
                            public OFInstruction instantiate() {
                                return new OFInstructionMeter();
/* TODO
                            }}),
    VENDOR        (0xffff, OFInstructionVendor.class, new Instantiable<OFInstruction>() {
                            @Override
                            public OFInstruction instantiate() {
                                return new OFInstructionVendor();
*/
                            }});

    protected static Map<Short, OFInstructionType> mapping;
    private static final int MAX_ENTRIES = 10;

    protected Class<? extends OFInstruction> clazz;
    protected Constructor<? extends OFInstruction> constructor;
    protected Instantiable<OFInstruction> instantiable;
    protected short type;

    /**
     * Store some information about the OpenFlow Instruction type, including wire
     * protocol type number, length, and derrived class
     *
     * @param type Wire protocol number associated with this OFInstructionType
     * @param clazz The Java class corresponding to this type of OpenFlow Instruction
     * @param instantiable the instantiable for the OFInstruction this type represents
     */
    OFInstructionType(int type, Class<? extends OFInstruction> clazz, Instantiable<OFInstruction> instantiable) {
        this.type = (short) type;
        this.clazz = clazz;
        this.instantiable = instantiable;
        try {
            this.constructor = clazz.getConstructor(new Class[]{});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failure getting constructor for class: " + clazz, e);
        }
        OFInstructionType.addMapping(this.type, this);
    }

    /**
     * Adds a mapping from type value to OFInstructionType enum
     *
     * @param i OpenFlow wire protocol Instruction type value
     * @param t type
     */
    static public void addMapping(short i, OFInstructionType t) {
        if (mapping == null)
            mapping = new LRULinkedHashMap<Short, OFInstructionType>(MAX_ENTRIES);
        mapping.put(i, t);
    }

    /**
     * Given a wire protocol OpenFlow type number, return the OFInstructionType associated
     * with it
     *
     * @param i wire protocol number
     * @return OFInstructionType enum type
     */

    static public OFInstructionType valueOf(short i) {
        return mapping.get(i);
    }

    /**
     * @return Returns the wire protocol value corresponding to this
     *         OFInstructionType
     */
    public short getTypeValue() {
        return this.type;
    }

    /**
     * @return return the OFInstruction subclass corresponding to this OFInstructionType
     */
    public Class<? extends OFInstruction> toClass() {
        return clazz;
    }

    /**
     * Returns the no-argument Constructor of the implementation class for
     * this OFInstructionType
     * @return the constructor
     */
    public Constructor<? extends OFInstruction> getConstructor() {
        return constructor;
    }

    /**
     * Returns a new instance of the OFInstruction represented by this OFInstructionType
     * @return the new object
     */
    public OFInstruction newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<OFInstruction> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable the instantiable to set
     */
    public void setInstantiable(Instantiable<OFInstruction> instantiable) {
        this.instantiable = instantiable;
    }
}
