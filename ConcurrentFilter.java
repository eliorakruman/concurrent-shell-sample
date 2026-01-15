package cs131.pa2.filter.concurrent;

import java.util.concurrent.LinkedBlockingQueue;

import cs131.pa2.filter.Filter;

/**
 * This abstract class extends Filter and implements the base functionality of all filters. Each filter
 * should extend this class and implement functionality that is specific to the filter.
 * It implements Runnable and stores input and output for filters in LinkedBlockingQueues.
 * It has a poison pill and error signal implemented as a String passed through the input.
 * 
 * @author Eliora Kruman
 *
 */
public abstract class ConcurrentFilter extends Filter implements Runnable{
	/**
	 * The input queue for this filter
	 */
	protected LinkedBlockingQueue<String> input;
	/**
	 * The output queue for this filter
	 */
	protected LinkedBlockingQueue<String> output;
	
	/**
	 * String to use to tell next filter that there will be no more input.
	 */
	protected final String POISON = "POISION PILL asdfasdfasdf";
	
	/**
	 * String to use to tell next filter that there was an error.
	 */
	protected final String SIGNAL = "DONT PRINT ajsdflkashdfkl;aj";
	
	@Override
	public void setPrevFilter(Filter prevFilter) {
		prevFilter.setNextFilter(this);
	}
	
	@Override
	public void setNextFilter(Filter nextFilter) {
		if (nextFilter instanceof ConcurrentFilter){
			ConcurrentFilter sequentialNext = (ConcurrentFilter) nextFilter;
			this.next = sequentialNext;
			sequentialNext.prev = this;
			if (this.output == null){
				this.output = new LinkedBlockingQueue<String>();
			}
			sequentialNext.input = this.output;
		} else {
			throw new RuntimeException("Should not attempt to link dissimilar filter types.");
		}
	}
	/**
	 * Processes the input queue and passes the result to the output queue
	 * Overridden in some filters
	 */
	public void process() {
		try {
			String line = input.take();
			if(line.equals(SIGNAL)) {	//checks whether there was an error, in which case the input will only contain the SIGNAL
				output.put(SIGNAL);	//passes the signal on to the next filter, without processing the rest of the text
				//important: note that processLine is never called for the signal
			}else {	//if there was no error
				while (!line.equals(POISON)){	//check for more input until the poison pill arrives
					String processedLine = processLine(line);
					if (processedLine != null){
						output.put(processedLine);	//process input and add to output
					}
					line = input.take();
				}
			}
		} catch(InterruptedException e){
			Thread.currentThread().interrupt();	//reset interrupt flag in case it's cleared
		}
	}
	
	@Override
	public boolean isDone() {
		return input.size() == 0;
	}
	
	/**
	 * Starts the thread and calls process. Overridden in some filters
	 */
	public void run() {
		process();
	}
	
	/**
	 * Called by the {@link #process()} method for every encountered line in the input queue.
	 * It then performs the processing specific for each filter and returns the result.
	 * Each filter inheriting from this class must implement its own version of processLine() to
	 * take care of the filter-specific processing.
	 * @param line the line got from the input queue
	 * @return the line after the filter-specific processing
	 */
	protected abstract String processLine(String line);
	
}
