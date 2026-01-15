# concurrent-shell-sample
Sample of a concurrent Unix-style shell pipeline using threads and blocking queues, implemented in Java.
This is a portion of a larger project and is not expected to run on its own.

This project implements a concurrent shell where each subcommand executes in its own thread.
Commands can form pipelines, and each stage runs concurrently with blocking queues.
The REPL supports foreground/background jobs, plus `kill` and `repl_jobs` commands.

### Classes

- **ConcurrentREPL**: Main implementation of the read-eval-print-loop, handles user input
- **ConcurrentFilter**: Abstract filter implementing the pipeline logic
- **UniqFilter**: Example filter extending ConcurrentFilter that outputs only unique lines
