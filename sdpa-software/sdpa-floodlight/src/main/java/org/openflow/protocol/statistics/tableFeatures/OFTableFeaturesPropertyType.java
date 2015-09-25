package org.openflow.protocol.statistics.tableFeatures;

import java.lang.reflect.Constructor;

import org.openflow.protocol.Instantiable;

/**
 * List of OpenFlow TableFeaturesProperty types and mappings to wire protocol value and
 * derived classes
 *
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 */
public enum OFTableFeaturesPropertyType {
    INSTRUCTIONS            (0, OFTableFeaturesPropertyInstructions.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyInstructions()
        .setType(INSTRUCTIONS);
        }}),
    INSTRUCTIONS_MISS       (1, OFTableFeaturesPropertyInstructions.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyInstructions()
        .setType(INSTRUCTIONS_MISS);
        }}),
    NEXT_TABLES             (2, OFTableFeaturesPropertyNextTables.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyNextTables()
        .setType(NEXT_TABLES);
        }}),
    NEXT_TABLES_MISS        (3, OFTableFeaturesPropertyNextTables.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyNextTables()
        .setType(NEXT_TABLES_MISS);
        }}),
    WRITE_ACTIONS           (4, OFTableFeaturesPropertyActions.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyActions()
        .setType(WRITE_ACTIONS);
        }}),
    WRITE_ACTIONS_MISS      (5, OFTableFeaturesPropertyActions.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyActions()
        .setType(WRITE_ACTIONS_MISS);
        }}),
    APPLY_ACTIONS           (6, OFTableFeaturesPropertyActions.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyActions()
        .setType(APPLY_ACTIONS);
        }}),
    APPLY_ACTIONS_MISS      (7, OFTableFeaturesPropertyActions.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyActions()
        .setType(APPLY_ACTIONS_MISS);
        }}),
    MATCH                   (8, OFTableFeaturesPropertyOXM.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyOXM()
        .setType(MATCH);
        }}),
    WILDCARDS               (10, OFTableFeaturesPropertyOXM.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyOXM()
        .setType(WILDCARDS);
        }}),
    WRITE_SETFIELD          (12, OFTableFeaturesPropertyOXM.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyOXM()
        .setType(WRITE_SETFIELD);
        }}),
    WRITE_SETFIELD_MISS     (13, OFTableFeaturesPropertyOXM.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyOXM()
        .setType(WRITE_SETFIELD_MISS);
        }}),
    APPLY_SETFIELD          (14, OFTableFeaturesPropertyOXM.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyOXM()
        .setType(APPLY_SETFIELD);
        }}),
    APPLY_SETFIELD_MISS     (15, OFTableFeaturesPropertyOXM.class, new Instantiable<OFTableFeaturesProperty>() {
        @Override
        public OFTableFeaturesProperty instantiate() {
            return new OFTableFeaturesPropertyOXM()
        .setType(APPLY_SETFIELD_MISS);
/* TODO
                            }}),
    EXPERIMENTER        (0xfffe, OFTableFeaturesPropertyExperimenter.class, new Instantiable<OFTableFeaturesProperty>() {
                            @Override
                            public OFTableFeaturesProperty instantiate() {
                                return new OFTableFeaturesPropertyExperimenter().setType(EXPERIMENTER);
                            }}),
    EXPERIMENTER_MISS   (0xffff, OFTableFeaturesPropertyExperimenter.class, new Instantiable<OFTableFeaturesProperty>() {
                            @Override
                            public OFTableFeaturesProperty instantiate() {
                                return new OFTableFeaturesPropertyExperimenter().setType(EXPERIMENTER_MISS);
*/
                            }});

    protected static OFTableFeaturesPropertyType[] mapping;

    protected Class<? extends OFTableFeaturesProperty> clazz;
    protected Constructor<? extends OFTableFeaturesProperty> constructor;
    protected Instantiable<OFTableFeaturesProperty> instantiable;
    protected short type;

    /**
     * Store some information about the OpenFlow TableFeaturesProperty type, including wire
     * protocol type number, length, and derrived class
     *
     * @param type Wire protocol number associated with this OFType
     * @param clazz The Java class corresponding to this type of OpenFlow TableFeaturesProperty
     * @param instantiable the instantiable for the OFTableFeaturesProperty this type represents
     */
    OFTableFeaturesPropertyType(int type, Class<? extends OFTableFeaturesProperty> clazz, Instantiable<OFTableFeaturesProperty> instantiable) {
        this.type = (short) type;
        this.clazz = clazz;
        this.instantiable = instantiable;
        try {
            this.constructor = clazz.getConstructor(new Class[]{});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failure getting constructor for class: " + clazz, e);
        }
        OFTableFeaturesPropertyType.addMapping(this.type, this);
    }

    /**
     * Adds a mapping from type value to OFTableFeaturesPropertyType enum
     *
     * @param i OpenFlow wire protocol TableFeaturesProperty type value
     * @param t type
     */
    static public void addMapping(short i, OFTableFeaturesPropertyType t) {
        if (mapping == null)
            mapping = new OFTableFeaturesPropertyType[16];
        // bring higher mappings down to the edge of our array
        if (i < 0)
            i = (short) (16 + i);
        OFTableFeaturesPropertyType.mapping[i] = t;
    }

    /**
     * Given a wire protocol OpenFlow type number, return the OFType associated
     * with it
     *
     * @param i wire protocol number
     * @return OFTableFeaturesPropertyType enum 
     */

    static public OFTableFeaturesPropertyType valueOf(short i) {
        if (i < 0)
            i = (short) (16+i);
        return OFTableFeaturesPropertyType.mapping[i];
    }

    /**
     * @return Returns the wire protocol value corresponding to this
     *         OFTableFeaturesPropertyType
     */
    public short getTypeValue() {
        return this.type;
    }

    /**
     * @return Returns boolean indicating if this is a miss property
     */
    public boolean isMiss() {
        return ((this.type & 0x1) == 0x1);
    }

    /**
     * @return return the OFTableFeaturesProperty subclass corresponding to this OFTableFeaturesPropertyType
     */
    public Class<? extends OFTableFeaturesProperty> toClass() {
        return clazz;
    }

    /**
     * Returns the no-argument Constructor of the implementation class for
     * this OFTableFeaturesPropertyType
     * @return the constructor
     */
    public Constructor<? extends OFTableFeaturesProperty> getConstructor() {
        return constructor;
    }

    /**
     * Returns a new instance of the OFTableFeaturesProperty represented by this OFTableFeaturesPropertyType
     * @return the new object
     */
    public OFTableFeaturesProperty newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<OFTableFeaturesProperty> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable the instantiable to set
     */
    public void setInstantiable(Instantiable<OFTableFeaturesProperty> instantiable) {
        this.instantiable = instantiable;
    }
}
