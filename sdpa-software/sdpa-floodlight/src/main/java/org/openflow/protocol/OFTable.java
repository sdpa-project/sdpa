package org.openflow.protocol;

import java.util.EnumSet;
import java.util.Set;

public class OFTable {
    public static final byte OFPTT_MAX = ((byte)0xfe);
    public static final byte OFPTT_ALL = ((byte)0xff);

    public enum OFTableConfig {
        OFPTC_DEPRECATED_MASK (3);

        protected int value;

        private OFTableConfig(int value) {
            this.value = value;
        }

        /**
         * Given a table config value, return the set of OFTableConfig enums
         * associated with it
         *
         * @param i table config bitmap
         * @return EnumSet<OFTableConfig>
         */
        public static EnumSet<OFTableConfig> valueOf(int i) {
            EnumSet<OFTableConfig> configs = EnumSet.noneOf(OFTableConfig.class);
            for (OFTableConfig value: OFTableConfig.values()) {
                if ((i & value.getValue()) == value.getValue())
                    configs.add(value);
            }
            return configs;
        }

        /**
         * Given a set of OFTableConfig enums, convert to bitmap value
         *
         * @param configs Set<OFTableConfig>
         * @return bitmap value
         */
        public static int toBitmap(Set<OFTableConfig> configs) {
            int bitmap = 0;
            for (OFTableConfig config: configs) 
                bitmap |= config.getValue();
            return bitmap;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }
}
