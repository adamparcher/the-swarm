package the.swarm.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class SwarmTimer {
	static private HashMap<String, Long> timingCounts = new HashMap<String, Long>();
	static private HashMap<String, Long> starts = new HashMap<String, Long>();
	static private HashMap<String, Long> times = new HashMap<String, Long>();
	public static void start(String s) {
		starts.put(s, System.nanoTime());
	}
	public static void end(String s) {
		Long currentCount = timingCounts.get(s);
		if(currentCount == null) {
			currentCount = 0l;
		}
		timingCounts.put(s,  currentCount + 1l);
		
		Long currentTime = times.get(s);
		if(currentTime == null) {
			currentTime = 0l;
		}
		times.put(s,  currentTime + System.nanoTime() - starts.get(s));
		
	}
	public static String getResults() {
		StringBuffer sb = new StringBuffer();
		for(Iterator<Entry<String, Long>> it = times.entrySet().iterator(); it.hasNext(); ) {
			Entry<String, Long> e = it.next();
			sb.append(e.getKey() + ": count=" + timingCounts.get(e.getKey()) + ", time=" + times.get(e.getKey()) + "\n");
		}
		return sb.toString();
	}
}
