# NO-LOCK SUM OF SQUARES

This web service takes requests to compute the sum
of the squares of the integers between a min and max
submitted by the user, and handles even very large numbers
gracefully.

By the way, I do know that there is a formula for computing
the sum of squares of an interval, but the purpose of this
project is to practice concurrency and multithreading.

## CURRENTLY IN PROGRESS (Oct 16, 2024)

I realized that my concurrency design didn't make sense for handling
multiple requests in parallel. That is--is applied concurrency for
breaking down a single request to make use of multiple processor cores,
but the pattern I was using would probably have resulted in unnecessary
thrashing with multiple requests coming in at once.

I am refactoring to queue the jobs against two ExecutorService instances,
one with a fixed thread pool for performing core computations and another
that uses virtual threads for performing IOs. I'll be happy to
explain those choices if interested.




## The interesting things here are:

* It performs the computation in a **concurrent**/**multithreaded** way.
* It uses no-lock techniques--that is, no lock or synchronization. It
uses AtomicReference with compare-and-set methods.
* It is designed to handle very large numbers. It uses Java's BigInteger to handle the large numbers, but of course that does not solve all the problems of big numbers.
* It uses a producer-consumer model to limit the memory usage as follows: if a large number interval is given, and if we immediately created a task for each number, this could easily overload the memory. Instead, we limit the number of tasks given at a time using a finite BlockingQueue. One thread loads numbers onto the queue, and consumer threads compute the squares and add them to the running total.
* The result will compute the sum of squares for an interval even if this results in a very huge number, given enough time, without overloading system resources. 
* The range for computation is batched. I did some simple testing and so far I found that a batch size of 200_000 works well on my computer, but I plan to do more performance testing and tuning.