public class Practica06_SumaParalela {
    private static final int N_MAX = 1_000_000;

    public static void main(String[] args) {
        if (args.length == 1) {
            try {
                int nThreads = Integer.parseInt(args[0]);
                runSingleExperiment(nThreads);
            } catch (NumberFormatException e) {
                System.err.println("Error: el argumento debe ser un entero positivo. Uso: java Practica06_SumaParalela [N] ");
            }
        } else if (args.length == 0) {
            // Modo automático: medir 1,2,4,8 hilos y mostrar tabla
            int[] tests = {1, 2, 4, 8};
            System.out.println("Mediciones automáticas para 1, 2, 4 y 8 hilos sobre suma de i^2 + 3i + 1 (i=1..1_000_000)");
            System.out.printf("Total elementos: %,d\n", N_MAX);
            System.out.println("------------------------------------------------------");
            System.out.printf("%8s | %20s | %16s\n", "Hilos", "Tiempo (ms)", "Resultado (long)");
            System.out.println("------------------------------------------------------");
            for (int t : tests) {
                ExperimentResult res = measure(t);
                System.out.printf("%8d | %20.3f | %,16d\n", t, res.timeNs / 1_000_000.0, res.sum);
            }
            System.out.println("------------------------------------------------------");
            System.out.println("Si deseas usar un N distinto (ej: 6), ejecuta: java Practica06_SumaParalela 6");
        } else {
            System.err.println("Uso: java Practica06_SumaParalela [N]\n  - Sin argumentos: ejecuta mediciones para 1,2,4,8 hilos.\n  - Con un argumento N: ejecuta con ese número de hilos.");
        }
    }

    private static void runSingleExperiment(int nThreads) {
        if (nThreads <= 0) {
            System.err.println("Error: n debe ser un entero positivo.");
            return;
        }
        System.out.printf("Ejecutando experimento con %d hilos...\n", nThreads);
        ExperimentResult r = measure(nThreads);
        System.out.printf("Resultado final: %,d\n", r.sum);
        System.out.printf("Tiempo: %.3f ms\n", r.timeNs / 1_000_000.0);
    }

    private static class ExperimentResult {
        long sum;
        long timeNs;
        ExperimentResult(long sum, long timeNs) { this.sum = sum; this.timeNs = timeNs; }
    }

    private static ExperimentResult measure(int nThreads) {
        if (nThreads <= 0) throw new IllegalArgumentException("nThreads debe ser > 0");

        final java.util.concurrent.atomic.AtomicLongArray partials = new java.util.concurrent.atomic.AtomicLongArray(nThreads);
        Thread[] threads = new Thread[nThreads];
 
        int base = N_MAX / nThreads;
        int rem = N_MAX % nThreads; 

        int start = 1;
        for (int i = 0; i < nThreads; i++) {
            int chunk = base + (i < rem ? 1 : 0);
            int end = start + chunk - 1;
            final int s = start;
            final int e = end;
            final int idx = i;
            threads[i] = new Thread(() -> {
                long localSum = 0L;
                for (int j = s; j <= e; j++) {
                    localSum += computeFi(j);
                }
                partials.set(idx, localSum);
            }, "Worker-" + i);
            start = end + 1;
        }
      
        if (start != N_MAX + 1) {
            throw new IllegalStateException("Error en la asignaci\u00f3n de rangos: no se cubre todo el intervalor");
        }

        long t0 = System.nanoTime();
        for (Thread th : threads) th.start();
        for (Thread th : threads) {
            try { th.join(); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); System.err.println("Main interrumpido"); }
        }
        long t1 = System.nanoTime();

        long total = 0L;
        for (int i = 0; i < partials.length(); i++) total += partials.get(i);

        return new ExperimentResult(total, t1 - t0);
    }
  
    private static long computeFi(int i) {
        long li = i;
        return li * li + 3L * li + 1L;
    }
}
