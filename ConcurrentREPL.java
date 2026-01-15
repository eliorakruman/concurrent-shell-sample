package cs131.pa2.filter.concurrent;

import cs131.pa2.filter.Message;
import java.util.Scanner;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;

/**
 * This class implements a concurrent shell where each subcommand executes in its own thread.
 * It is the main implementation of the REPL (read-eval-print-loop). It reads pipeline commands from the user,
 * parses them, executes them, and displays the result. Commands can be background or foreground jobs.
 * Contains implementations for kill (job interrupts) and repl_jobs (printing list of jobs).
 *
 * @author Eliora Kruman
 *
 */
public class ConcurrentREPL {

    /**
     * The path of the current working directory
     */
    public static String currentWorkingDirectory;
    public static final String PATH_SEPARATOR = System.getProperty("file.separator");
    /**
     * Count of background jobs
     */
    public static int bgJobCount;
    /**
     * Each background command has a thread per filter stored in a stack. Each stack is stored in this ArrayList
     */
    public static ArrayList<Stack<Thread>> bgThreads;
    /**
     * ArrayList storing background commands with ids
     */
    public static ArrayList<String> bgCommands;

    /**
     * The main method that will execute the REPL loop
     *
     * @param args not used
     */
    public static void main(String[] args) {
   	 currentWorkingDirectory = System.getProperty("user.dir");
   	 
   	 bgThreads = new ArrayList<Stack<Thread>>();	//initialize background (bg) thread storage
   	 bgCommands = new ArrayList<String>();	//initialize bg command storage
   	 bgJobCount = 0;	//set bgjob count to 0
   	 
   	 Scanner consoleReader = new Scanner(System.in);
   	 System.out.print(Message.WELCOME);

   	 while (true) {
   		 System.out.print(Message.NEWCOMMAND);

   		 // Read user command. If it's just whitespace, skip to next command
   		 String userInput = consoleReader.nextLine().trim();
   		 if (userInput.isEmpty()) {
   			 continue;
   		 }
   		 // Exit the REPL if the command is "exit".
   		 if (userInput.equals("exit")) {
   			 break;
   		 }
   		 // Kill implementation
   		 if (userInput.startsWith("kill ")) {
   			 String[] jobNum = userInput.split("kill ");
   			 if (jobNum[1].equals("")) { // make sure that there's a parameter
   				 System.out.print(Message.REQUIRES_PARAMETER.with_parameter(userInput));
   			 }
   			 int num = Integer.parseInt(jobNum[1]) - 1;	//find the id number of the command to be killed
   			 if (num >= 0 && num < bgJobCount) {    //makes sure that the command being killed has a valid id number
   				 while (!bgThreads.get(num).isEmpty()) {	//interrupts every thread (filter) for the command
   						 bgThreads.get(num).pop().interrupt();
   				 }
   			 }
   		 }
   		 //REPL_JOBS implementation
   		 else if (userInput.equals("repl_jobs")) {
   			 for (int i = 0; i < bgJobCount; i++) {	//loop through all bgJobs
   				 if(!bgThreads.get(i).isEmpty()) {
	   				 Thread t = bgThreads.get(i).peek();
	   				 if(t.isAlive()) {	//only print out currently active jobs!
	   					 System.out.println("\t" + bgCommands.get(i));
	   				 }
   				 }
   			 }
   		 } else {
   			 boolean bg = false;	//default is foreground job (fg)
   			 // background jobs implementation
   			 if (userInput.endsWith(" &")) {
   				 userInput = userInput.substring(0, userInput.length() - 2);	//remove " &" from command
   				 bg = true;	//set bg to true
   			 }
   			 List<ConcurrentFilter> filters = ConcurrentCommandBuilder.createFiltersFromCommand(userInput);
   			 if (filters != null) {
   				 if (bg) {	//if it's a bg job, update bgCommands and bgThreads
   					 bgJobCount++; // increment bg jobs only if they exist
   					 userInput = bgJobCount + ". " + userInput + " &";	//add the id and " &" to the command for repl printing
   					 bgCommands.add(userInput);	//store in bgCommands
   					 Stack<Thread> currentThreads = new Stack<Thread>();	//initialize stack to be stored in bgThreads
   					 bgThreads.add(currentThreads);
   				 }
   				 try {
   					 Stack<Thread> currentThreads = new Stack<Thread>();	//initialize stack to store threads (bg and fg)
   					 for (ConcurrentFilter filter : filters) {	//for each filter
   						 Thread t = new Thread(filter); // create instance of thread
   						 currentThreads.push(t);	//add to stack holding threads
   						 t.start(); // start thread
   					 }
   					 if(bg) {	//add all bg jobs to the stack in bgThreads
   						 bgThreads.get(bgJobCount-1).addAll(currentThreads);
   					 }
   					 else { // foreground tasks keep main method from running
   						 currentThreads.peek().join(); // make it so that main method won't run until the final thread is done
   					 }
   				 } catch (Exception e) {
   					 // One of the filters threw an exception. Print error and loop to wait for a new
   					 // command.
   					 System.out.print(e.getMessage());
   				 }
   			 }
   		 }
   	 }
   	 System.out.print(Message.GOODBYE);
   	 consoleReader.close();
    }

}

