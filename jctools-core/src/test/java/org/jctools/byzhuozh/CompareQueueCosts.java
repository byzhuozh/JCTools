package org.jctools.byzhuozh;

import org.jctools.queues.MpscChunkedArrayQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * 比较队列的消耗情况
 *
 * Producers: 2		Consumers: 1	Capacity: 512	LinkedBlockingQueue: 0.738s		MpscUnboundedArrayQueue: 0.078s		MpscChunkedArrayQueue: 0.099s
 * Producers: 2		Consumers: 1	Capacity: 1024	LinkedBlockingQueue: 0.297s		MpscUnboundedArrayQueue: 0.037s		MpscChunkedArrayQueue: 0.08s
 * Producers: 2		Consumers: 1	Capacity: 2048	LinkedBlockingQueue: 0.281s		MpscUnboundedArrayQueue: 0.06s		MpscChunkedArrayQueue: 0.063s
 *
 * Producers: 4		Consumers: 1	Capacity: 512	LinkedBlockingQueue: 0.487s		MpscUnboundedArrayQueue: 0.074s		MpscChunkedArrayQueue: 0.122s
 * Producers: 4		Consumers: 1	Capacity: 1024	LinkedBlockingQueue: 0.679s		MpscUnboundedArrayQueue: 0.075s		MpscChunkedArrayQueue: 0.135s
 * Producers: 4		Consumers: 1	Capacity: 2048	LinkedBlockingQueue: 0.278s		MpscUnboundedArrayQueue: 0.067s		MpscChunkedArrayQueue: 0.142s
 *
 * Producers: 8		Consumers: 1	Capacity: 512	LinkedBlockingQueue: 1.775s		MpscUnboundedArrayQueue: 0.081s		MpscChunkedArrayQueue: 0.211s
 * Producers: 8		Consumers: 1	Capacity: 1024	LinkedBlockingQueue: 1.262s		MpscUnboundedArrayQueue: 0.084s		MpscChunkedArrayQueue: 0.293s
 * Producers: 8		Consumers: 1	Capacity: 2048	LinkedBlockingQueue: 2.918s		MpscUnboundedArrayQueue: 0.081s		MpscChunkedArrayQueue: 0.407s
 *
 * Producers: 16	Consumers: 1	Capacity: 512	LinkedBlockingQueue: 12.59s		MpscUnboundedArrayQueue: 0.093s		MpscChunkedArrayQueue: 0.925s
 * Producers: 16	Consumers: 1	Capacity: 1024	LinkedBlockingQueue: 6.028s		MpscUnboundedArrayQueue: 0.085s		MpscChunkedArrayQueue: 1.116s
 * Producers: 16	Consumers: 1	Capacity: 2048	LinkedBlockingQueue: 3.04s		MpscUnboundedArrayQueue: 0.089s		MpscChunkedArrayQueue: 0.886s
 *
 * Producers: 32	Consumers: 1	Capacity: 512	LinkedBlockingQueue: 46.468s	MpscUnboundedArrayQueue: 0.069s		MpscChunkedArrayQueue: 1.567s
 * Producers: 32	Consumers: 1	Capacity: 1024	LinkedBlockingQueue: 13.06s		MpscUnboundedArrayQueue: 0.095s		MpscChunkedArrayQueue: 2.243s
 * Producers: 32	Consumers: 1	Capacity: 2048	LinkedBlockingQueue: 7.941s		MpscUnboundedArrayQueue: 0.092s		MpscChunkedArrayQueue: 1.837s
 *
 * Producers: 64	Consumers: 1	Capacity: 512	LinkedBlockingQueue: 152.502s	MpscUnboundedArrayQueue: 0.064s		MpscChunkedArrayQueue: 7.594s
 * Producers: 64	Consumers: 1	Capacity: 1024	LinkedBlockingQueue: 105.645s	MpscUnboundedArrayQueue: 0.106s		MpscChunkedArrayQueue: 3.326s
 * Producers: 64	Consumers: 1	Capacity: 2048	LinkedBlockingQueue: 24.122s	MpscUnboundedArrayQueue: 0.109s		MpscChunkedArrayQueue: 4.471s
 *
 *
 * 通过结果打印耗时可以明显看到MpscUnboundedArrayQueue耗时几乎大多数都是不超过0.1s的，这添加、删除的操作效率不是一般的高，
 * 这也难怪人家netty要舍弃自己写的队列框架了；
 *
 * CompareQueueCosts代码里面我将ArrayList、LinkedList注释掉了，那是因为队列数量太大，List的操作太慢，效率低下，
 * 所以在大量并发的场景下，大家还是能避免则尽量避免，否则就遭殃了；
 *
 *
 * @author weiqi
 * @version $Id: CompareQueueCosts.java, v 0.1 2019/7/18 16:30 weiqi Exp $
 * @describe:  demo 来源：https://www.jianshu.com/p/119a03332619
 */
public class CompareQueueCosts {

    /**
     * 生产者数量
     */
    private static int COUNT_OF_PRODUCER = 2;

    /**
     * 消费者数量
     */
    private static final int COUNT_OF_CONSUMER = 1;

    /**
     * 准备添加的任务数量值
     */
    private static final int COUNT_OF_TASK = 1 << 20;

    /**
     * 线程池对象
     */
    private static ExecutorService executorService;

    public static void main(String[] args) throws Exception {
        for (int j = 1; j < 7; j++) {
            COUNT_OF_PRODUCER = (int) Math.pow(2, j);
            executorService = Executors.newFixedThreadPool(COUNT_OF_PRODUCER * 2);

            int basePow = 8;
            int capacity = 0;
            for (int i = 1; i <= 3; i++) {
                capacity = 1 << (basePow + i);
                System.out.print("Producers: " + COUNT_OF_PRODUCER + "\t\t");
                System.out.print("Consumers: " + COUNT_OF_CONSUMER + "\t\t");
                System.out.print("Capacity: " + capacity + "\t\t");
                System.out.print("LinkedBlockingQueue: " + testQueue(new LinkedBlockingQueue<Integer>(capacity), COUNT_OF_TASK) + "s" + "\t\t");
                // System.out.print("ArrayList: " + testQueue(new ArrayList<Integer>(capacity), COUNT_OF_TASK) + "s" + "\t\t");
                // System.out.print("LinkedList: " + testQueue(new LinkedList<Integer>(), COUNT_OF_TASK) + "s" + "\t\t");
                System.out.print("MpscUnboundedArrayQueue: " + testQueue(new MpscUnboundedArrayQueue<Integer>(capacity), COUNT_OF_TASK) + "s" + "\t\t");
                System.out.print("MpscChunkedArrayQueue: " + testQueue(new MpscChunkedArrayQueue<Integer>(capacity), COUNT_OF_TASK) + "s" + "\t\t");
                System.out.println();
            }
            System.out.println();

            executorService.shutdown();
        }

    }

    static Double testQueue(final Collection<Integer> queue, final int taskCount) throws Exception {
        CompletionService<Long> completionService = new ExecutorCompletionService<Long>(executorService);

        long start = System.currentTimeMillis();
        for (int i = 0; i < COUNT_OF_PRODUCER; i++) {
            executorService.submit(new Producer(queue, taskCount / COUNT_OF_PRODUCER));
        }
        for (int i = 0; i < COUNT_OF_CONSUMER; i++) {
            completionService.submit((new Consumer(queue, taskCount / COUNT_OF_CONSUMER)));
        }

        for (int i = 0; i < COUNT_OF_CONSUMER; i++) {
            completionService.take().get();
        }

        long end = System.currentTimeMillis();
        return Double.parseDouble("" + (end - start)) / 1000;
    }

    static class Producer implements Runnable {
        private Collection<Integer> queue;
        private int taskCount;

        public Producer(Collection<Integer> queue, int taskCount) {
            this.queue = queue;
            this.taskCount = taskCount;
        }

        @Override
        public void run() {
            // Queue队列
            if (this.queue instanceof Queue) {
                Queue<Integer> tempQueue = (Queue<Integer>) this.queue;
                while (this.taskCount > 0) {
                    if (tempQueue.offer(this.taskCount)) {
                        this.taskCount--;
                    } else {
                        // System.out.println("Producer offer failed.");
                    }
                }
            }

            // List列表
            else if (this.queue instanceof List) {
                List<Integer> tempList = (List<Integer>) this.queue;
                while (this.taskCount > 0) {
                    if (tempList.add(this.taskCount)) {
                        this.taskCount--;
                    } else {
                        // System.out.println("Producer offer failed.");
                    }
                }
            }
        }
    }

    static class Consumer implements Callable<Long> {
        private Collection<Integer> queue;
        private int taskCount;

        public Consumer(Collection<Integer> queue, int taskCount) {
            this.queue = queue;
            this.taskCount = taskCount;
        }

        @Override
        public Long call() {
            // Queue队列
            if (this.queue instanceof Queue) {
                Queue<Integer> tempQueue = (Queue<Integer>) this.queue;
                while (this.taskCount > 0) {
                    if ((tempQueue.poll()) != null) {
                        this.taskCount--;
                    }
                }
            }
            // List列表
            else if (this.queue instanceof List) {
                List<Integer> tempList = (List<Integer>) this.queue;
                while (this.taskCount > 0) {
                    if (!tempList.isEmpty() && (tempList.remove(0)) != null) {
                        this.taskCount--;
                    }
                }
            }
            return 0L;
        }
    }
}