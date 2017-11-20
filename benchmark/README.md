#Garbage Collection

##Plagiarized from all over the Web!
This information copied and pasted from some place. Several some places. I do not recall. So thanks to whoever you are!

## Weak Generational Hypothesis

- Data that is very young will not survive long
- Data that is old will continue to exist

Consider a simple Java web server application as it just starts up. The init sequence will load the logging mechanism, the connection pools, perhaps pre-initialize some data/caches, determine what endpoints it will accept and so on. Post warm up, the application receives and fields a never-ending supply of REST calls. Each REST call may include decoding/parsing the http request into Request/Response objects, marshalling of data into json and finally streaming the data through the Response object. One set of data will hang around for the life of the application server, the other will exist for a brief instant.

## Generational Collector
Old and Young generations are designed specifically to handle those two types of data based on their longevity characteristics. The connection pools, logging metadata, endpoint information and caches would exist for long periods of time in the Old Generation, while shorter lived transient data from REST API calls would briefly reside in the Young Generation. Just about every modern garbage collection is a generational collector.

## Minor GC
Cleaning the Young Generation (Eden, Survivor spaces) cause a minor GC pause. The rate at which the objects are created and placed in Eden is called the _allocation rate_. A low allocation rate might mean Eden space is too small and is a bottle neck. Or a low allocation rate might indicate that not many objects are being created.

## Major GC
A major GC affects the Old Generation. It is typically much slower than a minor GC. The frequency and duration of a major GC is affected  by the _promotion rate_. Promotion rate is measured in the amount of data propagated from Young generation to Old generation per time unit. It is often measured in MB/sec, similar to allocation rate.

The more objects promoted to the Old Generation, the faster it is filled. Filling the Old Generation faster means that the frequency of the GC events cleaning Old generation will increase. High promotion rates can surface a symptom of a problem called premature promotion. Weak Generational Hypothesis is true when allocation rate >> promotion rate

## Allocation Failure
Allocation Failure causes a garbage collection. If an object fails be created in Eden, a minor GC is triggered.  If an object fails to be promoted to the Old Generation, a major GC is triggered. of the collection.

## Churn Rate
The churn rate describes the number of allocations of temporary objects per unit of time. Temporary objects are, by definition, short-lived and quickly collected. The higher the churn rate, the more frequently minor GCs happen.

## JMH GC Profiler Metrics
When allocation and churn rates match (even though churn rates will
have much larger errors), you can be measurements are reasonably accurate.

When the allocation rate is lower than churn rate, you know some
allocations are missing from the profiling.

When the allocation rate is higher than churn rate, you know there is
either a memory leak, or some other kind of problem.

###Norm
"norm" is not measured directly. It is a derived metric. The `gc.alloc.rate.norm` measures how many bytes are allocated per operation on average. Lower is better.

A high churn rate alone will slow down the applications because the JVM needs to execute young-generation GCs more frequently. Young-generation GCs are inexpensive only if most objects die! In a high-load situation with many concurrent transactions, many objects will be alive at the time of the GC. This leads to premature promotion and expensive major garbage collections down the road. 