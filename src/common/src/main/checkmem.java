package common.src.main;

public class checkmem {

    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();

        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        System.out.println("free memory: " + freeMemory / 1024);
        System.out.println("allocated memory: " + allocatedMemory / 1024);
        System.out.println("max memory: " + maxMemory /1024);
        System.out.println("total free memory: " +
                (freeMemory + (maxMemory - allocatedMemory)) / 1024);

    }
}
