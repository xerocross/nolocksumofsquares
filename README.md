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
* It uses no-lock techniques--that is, no lock or synchronization. It
uses AtomicReference with compare-and-set methods.
* It is designed to handle very large numbers. It uses Java's BigInteger to handle the large numbers, but of course that does not solve all the problems of big numbers (continue reading).
* It uses a producer-consumer pattern to parcel out work without overloading resources. For details, see the Concurrency Plan below.
* The result will compute the sum of squares for an interval even if this results in a very huge number, given enough time, without overloading system resources. 
* The range for computation is batched. I did some simple testing and so far I found that a batch size of 200_000 works well on my computer, but I plan to do more performance testing and tuning.

## CONCURRENCY PLAN

The app computes and sums the squares of all the numbers in the 
given range.

The plan is as follows.

There is a fixed thread pool used for the core computations
and a separate pool used for database updates. There are also
two special threads that divide up the jobs into tasks and
then schedule the tasks.

One of the goals here is to be able to handle very large
numbers. Because an interval could be very long, if we
created a task for each number or each batch of numbers 
in that interval, we could easily max out the memory. Thus,
one application thread receives requests and creates tasks
out of them and puts them on a work queue, allowing only 
a finite number at a time.

A second thread pulls these work tasks off the queue and
schedules them on the ExecutorService that manages the
main computations.

Thus, all requests will be scheduled by the same fixed 
thread pool ExecutorService, and system resources will
not be overwhelmed even by requests with even very large
numbers of intervals.
