# NO-LOCK SUM OF SQUARES

This web service takes requests to compute the sum
of the squares of the integers between a min and max
submitted by the user, and handles even very large numbers
gracefully.

By the way, I do know that there is a formula for computing
the sum of squares of an interval, but the purpose of this
project is to practice concurrency and multithreading.


## The interesting things here are:

* It performs the computation in a **concurrent**/**multithreaded** way.
* It uses **no-lock techniques**--that is, no lock or synchronization. It
uses **AtomicReference** with **compare-and-set** methods and **AtomicInteger**.
* It is designed to handle very large numbers. It uses Java's BigInteger to handle the large numbers, but of course that does not solve all the problems of big numbers (continue reading).
* It uses a producer-consumer pattern to parcel out work without overloading resources. For details, see the Concurrency Plan below.
* The result will compute the sum of squares for an interval even if this results in a very huge number, given enough time, without overloading system resources. 
* The range for computation is batched. I did some simple testing and so far I found that a batch size of 200_000 works well on my computer, but I plan to do more performance testing and tuning.

## CONCURRENCY PLAN

The app computes and sums the squares of all the numbers in the 
given range.

The plan/pattern used for concurrency is as follows.

There is a fixed thread pool used for the core computations
and a separate pool used for database updates. There are also
two special threads that divide up the jobs into tasks and
then schedule the tasks.

The **WorkLeaseGeneratorThread** thread is a singleton thread 
that handles all
job requests and transforms them into tasks in memory stored
in a finite blocking queue. It will block when the work
queue is full. This limits the total number of tasks in
memory, which is certainly necessary so we don't create
a task object for literally ever number from 1 to 200,000,000
say.

The **WorkScheduler** thread is a singleton thread that 
takes work off the work queue and schedules it against the
fixed-size computation thread pool. Here we use a semaphore
to limit the number of tasks scheduled at a given time. The
semaphore has a configurable limit. A permit is taken before
scheduling a new task, and a permit is released after
completion of each task.

The app uses a counter to keep track of how many tasks are
scheduled and outstanding for a given job. To fire completion
of all tasks for a given job, we watch for that counter to
reach 0.

The app also does keep ComputableFuture instances for tasks
that are scheduled but not completed yet. This is necessary
for cancelling. When a task is complete, the ComputableFuture
is released from memory. When the job is completed entirely,
the job is released from memory.


