/* 
 * 
 * 
 */
package report;

/**
 * Records the average buffer occupancy and its variance with format:
 * <p>
 * <Simulation time> <average buffer occupancy % [0..100]> <variance>
 * </p>
 *
 *
 */
import java.util.*;
//import java.util.List;
//import java.util.Map;

import core.DTNHost;
import core.Settings;
import core.SimClock;
import core.UpdateListener;

public class BufferOccupancyNodeTimeReport extends Report implements UpdateListener {

    /**
     * Record occupancy every nth second -setting id ({@value}). Defines the
     * interval how often (seconds) a new snapshot of buffer occupancy is taken
     * previous:5
     */
    public static final String BUFFER_REPORT_INTERVAL = "occupancyInterval";
    /**
     * Default value for the snapshot interval
     */
    public static final int DEFAULT_BUFFER_REPORT_INTERVAL = 3600;

    private double lastRecord = Double.MIN_VALUE;
    private int interval;

    private Map<DTNHost, ArrayList<Double>> bufferCounts = new HashMap<DTNHost, ArrayList<Double>>();
    private TreeMap<Integer, Double> npt = new TreeMap<Integer, Double>();
    private Map<DTNHost, Double> bpnpu = new TreeMap<DTNHost, Double>();

    //private Map<DTNHost, Double> bufferCounts = new HashMap<DTNHost, Double>();
    private int updateCounter = 0;  //new added

    public BufferOccupancyNodeTimeReport() {
        super();

        Settings settings = getSettings();
        if (settings.contains(BUFFER_REPORT_INTERVAL)) {
            interval = settings.getInt(BUFFER_REPORT_INTERVAL);
        } else {
            interval = -1;
            /* not found; use default */
        }

        if (interval < 0) {
            /* not found or invalid value -> use default */
            interval = DEFAULT_BUFFER_REPORT_INTERVAL;
        }
    }

    public void updated(List<DTNHost> hosts) {
        if (getSimTime() - lastRecord >= interval) {
            lastRecord = getSimTime();
            for (DTNHost h : hosts) {
                double temp = h.getBufferOccupancy();
                temp = (temp <= 100.0) ? (temp) : (100.0);
                if (bpnpu.containsKey(h)) {
                    bpnpu.replace(h, temp);
                } else {
                    bpnpu.put(h, temp);
                }
            }
            printLine(hosts);
            //write("Buffer Occupancy PerNode/Update : ");
        }

//        double simTime = getSimTime();
//        if (isWarmup()) {
//            return;
//        }
//
//        if (simTime - lastRecord >= interval) {
//            //lastRecord = SimClock.getTime();
//            printLine(hosts);
//            System.out.println(updateCounter);
//            updateCounter++; // new added
//            this.lastRecord = simTime - simTime % interval;
//        }
    }

    /**
     * Prints a snapshot of the average buffer occupancy
     *
     * @param hosts The list of hosts in the simulation
     */
    private void printLine(List<DTNHost> hosts) {
        for (DTNHost h : hosts) {
            ArrayList<Double> bufferList = new ArrayList<Double>();
            double temp = h.getBufferOccupancy();
            temp = (temp <= 100.0) ? (temp) : (100.0);
            if (bufferCounts.containsKey(h)) {
                //bufferCounts.put(h, (bufferCounts.get(h)+temp)/2); seems WRONG
                //bufferCounts.put(h, bufferCounts.get(h)+temp);
                //write (""+ bufferCounts.get(h));
                bufferList = bufferCounts.get(h);
                bufferList.add(temp);
                bufferCounts.put(h, bufferList);
            } else {
                bufferCounts.put(h, bufferList);
                //write (""+ bufferCounts.get(h));
            }
        }
    }

    @Override
    public void done() {
        	write("\n"+"Buffer Occupancy PerNode/Update : ");
		write("Current Interval  : "+(int)getSimTime());
		String statsText1 = "" ;
        for (Map.Entry<DTNHost, Double> entry : bpnpu.entrySet()) {
            statsText1 += entry.getKey()+"\t\t"+format(entry.getValue())+"\n";
        }
		write(statsText1);
                 super.done();
	}
        
//        for (Map.Entry<DTNHost, ArrayList<Double>> entry : bufferCounts.entrySet()) {
//            /*DTNHost a = entry.getKey();
//            Integer b = a.getAddress();
//            Double avgBuffer = entry.getValue()/updateCounter;*/
//            String printHost = "Node " + entry.getKey().getAddress() + "\t";
//            for (Double bufferList : entry.getValue()) {
//                printHost = printHost + "\t" + bufferList;
//            }
//            write(printHost);
//            //write("" + b + ' ' + entry.getValue());
//        }
      
    }

