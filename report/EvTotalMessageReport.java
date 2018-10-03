/*
 * Copyright 2010-2012 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package report;

/**
 * Records the average buffer occupancy and its variance with format:
 * <p>
 * [Simulation time] [average buffer occupancy % [0..100] ] [variance]
 * </p>
 *
 * <p>
 * The occupancy is calculated as an instantaneous snapshot every nth second as
 * defined by the <code>occupancyInterval</code> setting, not as an average over
 * time.
 * </p>
 *
 * @author	teemuk
 */
import java.util.List;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SimClock;
import core.UpdateListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EvTotalMessageReport extends Report implements UpdateListener, MessageListener {

    private Map<String, Double> creationTimes;
    private List<Double> latencies;
    private List<Integer> hopCounts;
    private List<Double> msgBufferTime;
    private List<Double> rtt; // round trip times
    
    private Map<Integer,Integer> nrofMdelivered; //Tambahan
    
    private int nrofDropped;
    private int nrofRemoved;
    private int nrofStarted;
    private int nrofAborted;
    private int nrofRelayed;
    private int nrofCreated;
    private int nrofResponseReqCreated;
    private int nrofResponseDelivered;
    private int nrofDelivered;

    /**
     * Record occupancy every nth second -setting id ({@value}). Defines the
     * interval how often (seconds) a new snapshot of buffer occupancy is taken
     */
    public static final String MESSAGE_REPORT_INTERVAL = "MessageInterval";
    /**
     * Default value for the snapshot interval
     */
    public static final int DEFAULT_MESSAGE_REPORT_INTERVAL = 3600;

    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    /**
     * Creates a new BufferOccupancyReport instance.
     */
    public EvTotalMessageReport() {
        init();
    }
    
    

    @Override
    protected void init() {
        super.init();
        this.creationTimes = new HashMap<String, Double>();
        this.latencies = new ArrayList<Double>();
        this.msgBufferTime = new ArrayList<Double>();
        this.hopCounts = new ArrayList<Integer>();
        this.rtt = new ArrayList<Double>();
        this.nrofMdelivered = new HashMap<Integer, Integer>();
        this.interval = DEFAULT_MESSAGE_REPORT_INTERVAL;

//		this.nrofDropped = 0;
//		this.nrofRemoved = 0;
        this.nrofStarted = 0;
//		this.nrofAborted = 0;
        this.nrofRelayed = 0;
        this.nrofCreated = 0;
        this.nrofResponseReqCreated = 0;
        this.nrofResponseDelivered = 0;
        this.nrofDelivered = 0;
    }

    @Override
    public void newMessage(Message m) {
        if (isWarmup()) {
			addWarmupID(m.getId());
			return;
		}
		
		this.creationTimes.put(m.getId(), getSimTime());
		this.nrofCreated++;
		if (m.getResponseSize() > 0) {
			this.nrofResponseReqCreated++;
                       
		}
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
       if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofStarted++;
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
			return;
		}
		
		this.nrofAborted++;
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        if (isWarmupID(m.getId())) {
            return;
        }
        this.nrofRelayed++;
        if (finalTarget) {
            this.latencies.add(getSimTime()
                    - this.creationTimes.get(m.getId()));
            this.nrofDelivered++;
            this.hopCounts.add(m.getHops().size() - 1);

            if (m.isResponse()) {
                this.rtt.add(getSimTime() - m.getRequest().getCreationTime());
                this.nrofResponseDelivered++;
            }
        }
    }

       public void done() {
    
        write("NrofDelivered/Time = ");
    String temp = "";
        for (Map.Entry<Integer, Integer> entry : nrofMdelivered.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            temp += key+" "+value+"\n";
        }
        write(temp);
        super.done();
                
    }
       
    public void updated(List<DTNHost> hosts) {
        double simTime = getSimTime();
        if (isWarmup()) {
            return;
        }
        if (simTime - lastRecord >= interval) {
            nrofMdelivered.put((int)simTime, nrofDelivered);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    /**
     * Prints a snapshot of the average buffer occupancy
     *
     * @param hosts The list of hosts in the simulation
     */
 

}
