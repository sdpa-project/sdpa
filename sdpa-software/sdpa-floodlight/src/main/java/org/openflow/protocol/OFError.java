package org.openflow.protocol;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.openflow.protocol.factory.OFMessageFactory;
import org.openflow.protocol.factory.OFMessageFactoryAware;
import org.openflow.util.U16;

/**
 * Represents an ofp_error_msg
 * 
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 */
public class OFError extends OFMessage implements OFMessageFactoryAware {
    public static int MINIMUM_LENGTH = 12;

    public enum OFErrorType {
        OFPET_HELLO_FAILED, OFPET_BAD_REQUEST, OFPET_BAD_ACTION, OFPET_BAD_INSTRUCTION, OFPET_BAD_MATCH, 
        OFPET_FLOW_MOD_FAILED, OFPET_GROUP_MOD_FAILED, OFPET_PORT_MOD_FAILED, OFPET_TABLE_MOD_FAILED, 
        OFPET_QUEUE_OP_FAILED, OFPET_SWITCH_CONFIG_FAILED, OFPET_ROLE_REQUEST_FAILED, OFPET_METER_MOD_FAILED,
        OFPET_TABLE_FEATURES_FAILED, OFPET_VENDOR;

        protected short value;

        private OFErrorType() {
            this.value = (short) this.ordinal();
        }

        private OFErrorType(short value) {
            this.value = value;
        }

        public short getValue() {
            return value;
        }
    }

    public enum OFHelloFailedCode {
        OFPHFC_INCOMPATIBLE, OFPHFC_EPERM
    }

    public enum OFBadRequestCode {
        OFPBRC_BAD_VERSION, OFPBRC_BAD_TYPE, OFPBRC_BAD_STATS, OFPBRC_BAD_VENDOR, OFPBRC_BAD_EXP_TYPE,
        OFPBRC_EPERM, OFPBRC_BAD_LEN, OFPBRC_BUFFER_EMPTY, OFPBRC_BUFFER_UNKNOWN, OFPBRC_BAD_TABLE_ID, 
        OFPBRC_IS_SLAVE, OFPBRC_BAD_PORT, OFPBRC_BAD_PACKET, OFPBRC_STATS_BUFFER_OVERFLOW
    }

    public enum OFBadActionCode {
        OFPBAC_BAD_TYPE, OFPBAC_BAD_LEN, OFPBAC_BAD_VENDOR, OFPBAC_BAD_EXP_TYPE, 
        OFPBAC_BAD_OUT_PORT, OFPBAC_BAD_ARGUMENT, OFPBAC_EPERM, OFPBAC_TOO_MANY, OFPBAC_BAD_QUEUE,
        OFPBAC_BAD_OUT_GROUP, OFPBAC_MATCH_INCONSISTENT, OFPBAC_UNSUPPORTED_ORDER, OFPBAC_BAD_TAG,
        OFPBAC_BAD_SET_TYPE, OFPBAC_BAD_SET_LEN, OFPBAC_BAD_SET_ARGUMENT
    }

    public enum OFBadInstructionCode {
        OFPBIC_UNKNOWN_INST, OFPBIC_UNSUP_INST, OFPBIC_BAD_TABLE_ID, OFPBIC_UNSUP_METADATA, OFPBIC_UNSUP_METADATA_MASK,
        OFPBIC_BAD_VENDOR, OFPBIC_BAD_EXP_TYPE, OFPBIC_BAD_LEN, OFPBIC_EPERM 
    }
    
    public enum OFBadMatchCode {
        OFPBMC_BAD_TYPE, OFPBMC_BAD_LEN, OFPBMC_BAD_TAG, OFPBMC_BAD_DL_ADDR_MASK, OFPBMC_BAD_NW_ADDR_MASK, 
        OFPBMC_BAD_WILDCARDS, OFPBMC_BAD_FIELD, OFPBMC_BAD_VALUE, OFPBMC_BAD_MASK, OFPBMC_BAD_PREREQ,
        OFPBMC_DUP_FIELD, OFPBMC_EPERM
    }
    public enum OFFlowModFailedCode {
        OFPFMFC_UNKNOWN, OFPFMFC_TABLES_FULL, OFPFMFC_BAD_TABLE_ID, OFPFMFC_OVERLAP, 
        OFPFMFC_EPERM, OFPFMFC_BAD_TIMEOUT, OFPFMFC_BAD_COMMAND, OFPFMFC_BAD_FLAGS
    }

    public enum OFGroupModFailedCode {
        OFPGMFC_GROUP_EXISTS, OFPGMFC_INVALID_GROUP, OFPGMFC_WEIGHT_UNSUPPORTED, OFPGMFC_OUT_OF_GROUPS,
        OFPGMFC_OUT_OF_BUCKETS, OFPGMFC_CHAINING_UNSUPPORTED, OFPGMFC_WATCH_UNSUPPORTED, OFPGMFC_LOOP,
        OFPGMFC_UNKNOWN_GROUP, OFPGMFC_CHAINED_GROUP, OFPGMFC_BAD_TYPE, OFPGMFC_BAD_COMMAND, 
        OFPGMFC_BAD_BUCKET, OFPGMFC_BAD_WATCH, OFPGMFC_EPERM
    }
    
    public enum OFPortModFailedCode {
        OFPPMFC_BAD_PORT, OFPPMFC_BAD_HW_ADDR, OFPPMFC_BAD_CONFIG, OFPPMFC_BAD_ADVERTISE, OFPPMFC_EPERM
    }

    public enum OFTableModFailedCode {
        OFPTMFC_BAD_TABLE, OFPTMFC_BAD_CONFIG, OFPTMFC_EPERM
    }
    
    public enum OFMeterModFailedCode {
    	OFPMMFC_UNKNOWN, OFPMMFC_METER_EXISTS, OFPMMFC_INVALID_METER, OFPMMFC_UNKNOWN_METER, 
    	OFPMMFC_BAD_COMMAND, OFPMMFC_BAD_FLAGS, OFPMMFC_BAD_RATE, OFPMMFC_BAD_BURST, 
    	OFPMMFC_BAD_BAND, OFPMMFC_BAD_BAND_VALUE, OFPMMFC_OUT_OF_METERS, OFPMMFC_OUT_OF_BANDS
    }
    
    public enum OFQueueOpFailedCode {
        OFPQOFC_BAD_PORT, OFPQOFC_BAD_QUEUE, OFPQOFC_EPERM
    }

    public enum OFSwitchConfigFailedCode {
        OFPSCFC_BAD_FLAGS, OFPSCFC_BAD_LEN, OFPSCFC_EPERM
    }

    public enum OFRoleRequestFailedCode {
        OFPRRFC_STALE, OFPRRFC_UNSUP, OFPRRFC_BAD_ROLE
    }
    
    public enum OFTableFeaturesFailedCode {
        OFPTFFC_BAD_TABLE, OFPTFFC_BAD_METADATA, OFPTFFC_BAD_TYPE, 
        OFPTFFC_BAD_LEN, OFPTFFC_BAD_ARGUMENT, OFPTFFC_EPERM
    }
    
    protected short errorType;
    protected short errorCode;
    protected OFMessageFactory factory;
    protected byte[] error;
    protected boolean errorIsAscii;

    public OFError() {
        super();
        this.type = OFType.ERROR;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the errorType
     */
    public short getErrorType() {
        return errorType;
    }

    /**
     * @param errorType
     *            the errorType to set
     */
    public OFError setErrorType(short errorType) {
        this.errorType = errorType;
        return this;
    }

    public OFError setErrorType(OFErrorType type) {
        this.errorType = (short) type.ordinal();
        return this;
    }

    /**
     * @return the errorCode
     */
    public short getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode
     *            the errorCode to set
     */
    public OFError setErrorCode(OFHelloFailedCode code) {
        this.errorCode = (short) code.ordinal();
        return this;
    }

    public OFError setErrorCode(short errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public OFError setErrorCode(OFBadRequestCode code) {
        this.errorCode = (short) code.ordinal();
        return this;
    }

    public OFError setErrorCode(OFBadActionCode code) {
        this.errorCode = (short) code.ordinal();
        return this;
    }

    public OFError setErrorCode(OFFlowModFailedCode code) {
        this.errorCode = (short) code.ordinal();
        return this;
    }

    public OFError setErrorCode(OFPortModFailedCode code) {
        this.errorCode = (short) code.ordinal();
        return this;
    }

    public OFError setErrorCode(OFQueueOpFailedCode code) {
        this.errorCode = (short) code.ordinal();
        return this;
    }

    public OFMessage getOffendingMsg() {
        // should only have one message embedded; if more than one, just
        // grab first
        if (this.error == null)
            return null;
        ByteBuffer errorMsg = ByteBuffer.wrap(this.error);
        if (factory == null)
            throw new RuntimeException("MessageFactory not set");
        List<OFMessage> messages = this.factory.parseMessages(errorMsg,
                error.length);
        // OVS apparently sends partial messages in errors
        // need to be careful of that AND can't use data.limit() as
        // a packet boundary because there could be more data queued
        if (messages.size() > 0)
            return messages.get(0);
        else
            return null;
    }

    /**
     * Write this offending message into the payload of the Error message
     * 
     * @param offendingMsg
     */

    public OFError setOffendingMsg(OFMessage offendingMsg) {
        if (offendingMsg == null) {
            super.setLengthU(MINIMUM_LENGTH);
        } else {
            this.error = new byte[offendingMsg.getLengthU()];
            ByteBuffer errorMsg = ByteBuffer.wrap(this.error);
            //data.writerIndex(0);
            offendingMsg.writeTo(errorMsg);
            super.setLengthU(MINIMUM_LENGTH + offendingMsg.getLengthU());
        }
        return this;
    }

    public OFMessageFactory getFactory() {
        return factory;
    }

    @Override
    public void setMessageFactory(OFMessageFactory factory) {
        this.factory = factory;
    }

    /**
     * @return the error
     */
    public byte[] getError() {
        return error;
    }

    /**
     * @param error
     *            the error to set
     */
    public OFError setError(byte[] error) {
        this.error = error;
        return this;
    }

    /**
     * @return the errorIsAscii
     */
    public boolean isErrorIsAscii() {
        return errorIsAscii;
    }

    /**
     * @param errorIsAscii
     *            the errorIsAscii to set
     */
    public OFError setErrorIsAscii(boolean errorIsAscii) {
        this.errorIsAscii = errorIsAscii;
        return this;
    }

    @Override
    public void readFrom(ByteBuffer data) {
        super.readFrom(data);
        this.errorType = data.getShort();
        this.errorCode = data.getShort();
        int dataLength = this.getLengthU() - MINIMUM_LENGTH;
        if (dataLength > 0) {
            this.error = new byte[dataLength];
            data.get(this.error);
            if (this.errorType == OFErrorType.OFPET_HELLO_FAILED.ordinal())
                this.errorIsAscii = true;
        }
    }

    @Override
    public void writeTo(ByteBuffer data) {
        super.writeTo(data);
        data.putShort(errorType);
        data.putShort(errorCode);
        if (error != null)
            data.put(error);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(error);
        result = prime * result + errorCode;
        result = prime * result + (errorIsAscii ? 1231 : 1237);
        result = prime * result + errorType;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFError other = (OFError) obj;
        if (!Arrays.equals(error, other.error))
            return false;
        if (errorCode != other.errorCode)
            return false;
        if (errorIsAscii != other.errorIsAscii)
            return false;
        if (errorType != other.errorType)
            return false;
        return true;
    }

    public String getErrorCodeName(OFErrorType errorType, int errorCode) {
        switch (errorType) {
            case OFPET_HELLO_FAILED:
                return OFHelloFailedCode.values()[errorCode].name();

            case OFPET_BAD_REQUEST:
                return OFBadRequestCode.values()[errorCode].name();
        
            case OFPET_BAD_ACTION:
                return OFBadActionCode.values()[errorCode].name();
        
            case OFPET_BAD_INSTRUCTION:
                return OFBadInstructionCode.values()[errorCode].name();
        
            case OFPET_BAD_MATCH:
                return OFBadMatchCode.values()[errorCode].name();
        
            case OFPET_FLOW_MOD_FAILED:
                return OFFlowModFailedCode.values()[errorCode].name();
        
            case OFPET_GROUP_MOD_FAILED:
                return OFGroupModFailedCode.values()[errorCode].name();
        
            case OFPET_PORT_MOD_FAILED:
                return OFPortModFailedCode.values()[errorCode].name();
        
            case OFPET_TABLE_MOD_FAILED:
                return OFTableModFailedCode.values()[errorCode].name();
        
            case OFPET_QUEUE_OP_FAILED:
                return OFQueueOpFailedCode.values()[errorCode].name();
        
            case OFPET_SWITCH_CONFIG_FAILED:
                return OFSwitchConfigFailedCode.values()[errorCode].name();
        
            case OFPET_ROLE_REQUEST_FAILED:
                return OFRoleRequestFailedCode.values()[errorCode].name();
        
            case OFPET_METER_MOD_FAILED:
                return OFMeterModFailedCode.values()[errorCode].name();
        
            case OFPET_TABLE_FEATURES_FAILED:
                return OFTableFeaturesFailedCode.values()[errorCode].name();
                
            default:
                return null;
        }
    }
    
    @Override
    public String toString() {
        OFErrorType eType = OFErrorType.values()[errorType];
        return "OFError [type=" +  eType.name() + 
                ", code=" + getErrorCodeName(eType, errorCode) + "]";
    }
    
    /* (non-Javadoc)
     * @see org.openflow.protocol.OFMessage#computeLength()
     */
    @Override
    public void computeLength() {
        this.length = U16.t(MINIMUM_LENGTH + ((error != null) ? error.length : 0));
    }
}
