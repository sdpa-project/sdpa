package org.openflow.protocol.factory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionType;
import org.openflow.protocol.queue.OFQueueProperty;
import org.openflow.protocol.queue.OFQueuePropertyType;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.openflow.protocol.hello.OFHelloElement;
import org.openflow.protocol.hello.OFHelloElementType;
import org.openflow.protocol.statistics.OFVendorStatistics;
import org.openflow.protocol.meter.OFMeterBand;
import org.openflow.protocol.meter.OFMeterBandType;
import org.openflow.protocol.statistics.tableFeatures.OFTableFeaturesProperty;
import org.openflow.protocol.statistics.tableFeatures.OFTableFeaturesPropertyType;


/**
 * A basic OpenFlow factory that supports naive creation of both Messages and
 * Actions.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 * @author Srini Seetharaman (srini.seetharaman@gmail.com)
 *
 */
public class BasicFactory implements OFMessageFactory, OFActionFactory,
        OFQueuePropertyFactory, OFStatisticsFactory,
        OFInstructionFactory, OFHelloElementFactory,
        OFMeterBandFactory, OFTableFeaturesPropertyFactory {

    private static final BasicFactory SINGLETON_INSTANCE = new BasicFactory();

    protected BasicFactory() { }

    public static BasicFactory getInstance() {
        return SINGLETON_INSTANCE;
    }

    /**
     * create and return a new instance of a message for OFType t. Also injects
     * factories for those message types that implement the *FactoryAware
     * interfaces.
     *
     * @return a newly created instance that may be modified / used freely by
     *         the caller
     */
    @Override
    public OFMessage getMessage(OFType t) {
        OFMessage message = t.newInstance();
        injectFactories(message);
        return message;
    }

    @Override
    public List<OFMessage> parseMessages(ByteBuffer data) {
        return parseMessages(data, 0);
    }

    @Override
    public List<OFMessage> parseMessages(ByteBuffer data, int limit) {
        List<OFMessage> results = new ArrayList<OFMessage>();
        OFMessage demux = new OFMessage();
        OFMessage ofm;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFMessage.MINIMUM_LENGTH)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining())
                return results;

            ofm = getMessage(demux.getType());
            if (ofm == null)
                return null;

            injectFactories(ofm);
            ofm.readFrom(data);
            if (OFMessage.class.equals(ofm.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position()+(ofm.getLengthU() -
                        OFMessage.MINIMUM_LENGTH));
            }
            results.add(ofm);
        }

        return results;
    }

    protected void injectFactories(OFMessage ofm) {
        if (ofm instanceof OFActionFactoryAware) {
            ((OFActionFactoryAware)ofm).setActionFactory(this);
        }
        if (ofm instanceof OFInstructionFactoryAware) {
            ((OFInstructionFactoryAware)ofm).setInstructionFactory(this);
        }
        if (ofm instanceof OFMessageFactoryAware) {
            ((OFMessageFactoryAware)ofm).setMessageFactory(this);
        }
        if (ofm instanceof OFQueuePropertyFactoryAware) {
            ((OFQueuePropertyFactoryAware)ofm).setQueuePropertyFactory(this);
        }
        if (ofm instanceof OFStatisticsFactoryAware) {
            ((OFStatisticsFactoryAware)ofm).setStatisticsFactory(this);
        }
        if (ofm instanceof OFHelloElementFactoryAware) {
            ((OFHelloElementFactoryAware)ofm).setHelloElementFactory(this);
        }
        if (ofm instanceof OFMeterBandFactoryAware) {
            ((OFMeterBandFactoryAware)ofm).setMeterBandFactory(this);
        }
        if (ofm instanceof OFTableFeaturesPropertyFactoryAware) {
            ((OFTableFeaturesPropertyFactoryAware)ofm).setTableFeaturesPropertyFactory(this);
        }
    }

    @Override
    public OFAction getAction(OFActionType t) {
        return t.newInstance();
    }

    @Override
    public List<OFAction> parseActions(ByteBuffer data, int length) {
        return parseActions(data, length, 0);
    }

    @Override
    public List<OFAction> parseActions(ByteBuffer data, int length, int limit) {
        List<OFAction> results = new ArrayList<OFAction>();
        OFAction demux = new OFAction();
        OFAction ofa;
        int end = data.position() + length;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFAction.MINIMUM_LENGTH ||
                    (data.position() + OFAction.MINIMUM_LENGTH) > end)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining() ||
                    (data.position() + demux.getLengthU()) > end)
                return results;

            ofa = getAction(demux.getType());
            ofa.readFrom(data);
            if (OFAction.class.equals(ofa.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position()+(ofa.getLengthU() -
                        OFAction.MINIMUM_LENGTH));
            }
            results.add(ofa);
        }

        return results;
    }

    @Override
    public OFActionFactory getActionFactory() {
        return this;
    }

    @Override
    public OFInstruction getInstruction(OFInstructionType t) {
        return t.newInstance();
    }

    @Override
    public List<OFInstruction> parseInstructions(ByteBuffer data, int length) {
        return parseInstructions(data, length, 0);
    }

    @Override
    public List<OFInstruction> parseInstructions(ByteBuffer data, int length, int limit) {
        List<OFInstruction> results = new ArrayList<OFInstruction>();
        OFInstruction demux = new OFInstruction();
        OFInstruction ofi;
        int end = data.position() + length;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFInstruction.MINIMUM_LENGTH ||
                    (data.position() + OFInstruction.MINIMUM_LENGTH) > end)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining() ||
                    (data.position() + demux.getLengthU()) > end)
                return results;

            ofi = getInstruction(demux.getType());

            // If actions are embedded in the OFInstruction,
            // then set factory
            if (ofi instanceof OFActionFactoryAware) 
                ((OFActionFactoryAware)ofi).setActionFactory(this);

            ofi.readFrom(data);
            if (OFInstruction.class.equals(ofi.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position()+(ofi.getLengthU() -
                        OFInstruction.MINIMUM_LENGTH));
            }
            results.add(ofi);
        }

        return results;
    }

    @Override
    public OFInstructionFactory getInstructionFactory() {
        return this;
    }

    @Override
    public OFStatistics getStatistics(OFType t, OFStatisticsType st) {
        return st.newInstance(t);
    }

    @Override
    public List<OFStatistics> parseStatistics(OFType t, OFStatisticsType st,
            ByteBuffer data, int length) {
        return parseStatistics(t, st, data, length, 0);
    }

    /**
     * @param t
     *            OFMessage type: should be one of stats_request or stats_reply
     * @param st
     *            type of this statistics message, e.g., DESC, TABLE
     * @param data
     *            buffer to read from
     * @param length
     *            length of records
     * @param limit
     *            number of records to grab; 0 == all
     * 
     * @return list of statistics records
     */

    @Override
    public List<OFStatistics> parseStatistics(OFType t, OFStatisticsType st,
            ByteBuffer data, int length, int limit) {
        List<OFStatistics> results = new ArrayList<OFStatistics>();
        OFStatistics statistics = getStatistics(t, st);

        int start = data.position();
        int count = 0;

        while (limit == 0 || results.size() <= limit) {
            // Create a separate MUX/DEMUX path for vendor stats
            if (statistics instanceof OFVendorStatistics)
                ((OFVendorStatistics)statistics).setLength(length);

            /**
             * can't use data.remaining() here, b/c there could be other data
             * buffered past this message
             */
            if ((length - count) >= statistics.getLength()) {
                if (statistics instanceof OFInstructionFactoryAware)
                    ((OFInstructionFactoryAware)statistics).setInstructionFactory(this);
                else if (statistics instanceof OFTableFeaturesPropertyFactoryAware)
                    ((OFTableFeaturesPropertyFactoryAware)statistics).setTableFeaturesPropertyFactory(this);
                statistics.readFrom(data);
                results.add(statistics);
                count += statistics.getLength();
                statistics = getStatistics(t, st);
            } else {
                if (count < length) {
                    /**
                     * Nasty case: partial/incomplete statistic found even
                     * though we have a full message. Found when NOX sent
                     * agg_stats request with wrong agg statistics length (52
                     * instead of 56)
                     * 
                     * just throw the rest away, or we will break framing
                     */
                    data.position(start + length);
                }
                return results;
            }
        }
        return results; // empty; no statistics at all
    }

    @Override
    public OFQueueProperty getQueueProperty(OFQueuePropertyType t) {
        return t.newInstance();
    }

    @Override
    public List<OFQueueProperty> parseQueueProperties(ByteBuffer data,
            int length) {
        return parseQueueProperties(data, length, 0);
    }

    @Override
    public List<OFQueueProperty> parseQueueProperties(ByteBuffer data,
            int length, int limit) {
        List<OFQueueProperty> results = new ArrayList<OFQueueProperty>();
        OFQueueProperty demux = new OFQueueProperty();
        OFQueueProperty ofqp;
        int end = data.position() + length;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFQueueProperty.MINIMUM_LENGTH ||
                    (data.position() + OFQueueProperty.MINIMUM_LENGTH) > end)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining() ||
                    (data.position() + demux.getLengthU()) > end)
                return results;

            ofqp = getQueueProperty(demux.getType());
            ofqp.readFrom(data);
            if (OFQueueProperty.class.equals(ofqp.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position()+(ofqp.getLengthU() -
                        OFQueueProperty.MINIMUM_LENGTH));
            }
            results.add(ofqp);
        }

        return results;
    }
    @Override
    public OFHelloElement getHelloElement(OFHelloElementType t) {
        return t.newInstance();
    }

    @Override
    public List<OFHelloElement> parseHelloElements(ByteBuffer data,
            int length) {
        return parseHelloElements(data, length, 0);
    }

    @Override
    public List<OFHelloElement> parseHelloElements(ByteBuffer data,
            int length, int limit) {
        List<OFHelloElement> results = new ArrayList<OFHelloElement>();
        OFHelloElement demux = new OFHelloElement();
        OFHelloElement ofqp;
        int end = data.position() + length;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFHelloElement.MINIMUM_LENGTH ||
                    (data.position() + OFHelloElement.MINIMUM_LENGTH) > end)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining() ||
                    (data.position() + demux.getLengthU()) > end)
                return results;

            ofqp = getHelloElement(demux.getType());
            ofqp.readFrom(data);
            if (OFHelloElement.class.equals(ofqp.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position()+(ofqp.getLengthU() -
                        OFHelloElement.MINIMUM_LENGTH));
            }
            results.add(ofqp);
        }

        return results;
    }

    @Override
    public OFMeterBand getMeterBand(OFMeterBandType t) {
        return t.newInstance();
    }

    @Override
    public List<OFMeterBand> parseMeterBands(ByteBuffer data,
            int length) {
        return parseMeterBands(data, length, 0);
    }

    @Override
    public List<OFMeterBand> parseMeterBands(ByteBuffer data,
            int length, int limit) {
        List<OFMeterBand> results = new ArrayList<OFMeterBand>();
        OFMeterBand demux = new OFMeterBand();
        OFMeterBand ofqp;
        int end = data.position() + length;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFMeterBand.MINIMUM_LENGTH ||
                    (data.position() + OFMeterBand.MINIMUM_LENGTH) > end)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining() ||
                    (data.position() + demux.getLengthU()) > end)
                return results;

            ofqp = getMeterBand(demux.getType());
            ofqp.readFrom(data);
            if (OFMeterBand.class.equals(ofqp.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position()+(ofqp.getLengthU() -
                        OFMeterBand.MINIMUM_LENGTH));
            }
            results.add(ofqp);
        }

        return results;
   }

    @Override
    public OFTableFeaturesProperty getTableFeaturesProperty(OFTableFeaturesPropertyType t) {
        return t.newInstance();
    }

    @Override
    public List<OFTableFeaturesProperty> parseTableFeaturesProperties(ByteBuffer data,
            int length) {
        return parseTableFeaturesProperties(data, length, 0);
    }

    @Override
    public List<OFTableFeaturesProperty> parseTableFeaturesProperties(ByteBuffer data,
            int length, int limit) {
        List<OFTableFeaturesProperty> results = new ArrayList<OFTableFeaturesProperty>();
        OFTableFeaturesProperty demux = new OFTableFeaturesProperty();
        OFTableFeaturesProperty oftfp;
        int end = data.position() + length;

        while (limit == 0 || results.size() <= limit) {
            if (data.remaining() < OFTableFeaturesProperty.MINIMUM_LENGTH ||
                    (data.position() + OFTableFeaturesProperty.MINIMUM_LENGTH) > end)
                return results;

            data.mark();
            demux.readFrom(data);
            data.reset();

            if (demux.getLengthU() > data.remaining() ||
                    (data.position() + demux.getLengthU()) > end)
                return results;

            oftfp = getTableFeaturesProperty(demux.getType());
            if (oftfp instanceof OFInstructionFactoryAware) 
                ((OFInstructionFactoryAware)oftfp).setInstructionFactory(this);
            else if (oftfp instanceof OFActionFactoryAware) 
                ((OFActionFactoryAware)oftfp).setActionFactory(this);

            oftfp.readFrom(data);
            if (OFTableFeaturesProperty.class.equals(oftfp.getClass())) {
                // advance the position for un-implemented messages
                data.position(data.position()+(oftfp.getLengthU() -
                        OFTableFeaturesProperty.MINIMUM_LENGTH));
            }
            results.add(oftfp);
        }

        return results;
    }    
}

