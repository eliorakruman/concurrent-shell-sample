package cs131.pa2.filter.concurrent;

import java.util.HashSet;

/**
 * This filter that only outputs unique lines of input. This filter requires input.
 * Implements Runnable. Since it has input, it checks for an error signal and poison pill. Since it has
 * output, it sends a poison pill when finished running.
 * Duplicates are skipped by storing previously seen lines in a HashSet.
 * 
 * @author Eliora Kruman
 *
 */
public class UniqFilter extends ConcurrentFilter {
	
	private HashSet<String> prevLines;
	
	public UniqFilter() {
		prevLines = new HashSet<>();
	}
	
	/**
	 * Overrides process method in run to add poison pill implementation
	 */
	@Override
	public void process() {
		try{
			super.process();
			output.put(POISON);	//add poison pill when finished
		}catch(InterruptedException e) {
			//Do nothing
		}
	}
	
	/**
	 * Returns null if the given line has already been output by this filter.
	 */
	@Override
	protected String processLine(String line) {
		if (prevLines.contains(line)) {
			return null;
		}
		prevLines.add(line);
		return line;
	}
}
