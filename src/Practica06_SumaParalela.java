public class Practica06_SumaParalela {
    // LÍMITE superior de la suma (fijo por el enunciado)
    private static final int N_MAX = 1_000_000;

    public static void main(String[] args) {
        // Si el usuario pasa un número como argumento, lo usamos como N (validado más abajo)
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

    // Ejecuta y muestra un experimento con N hilos y valida la entrada
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

    // Estructura simple para retornar resultado y tiempo
    private static class ExperimentResult {
        long sum;
        long timeNs;
        ExperimentResult(long sum, long timeNs) { this.sum = sum; this.timeNs = timeNs; }
    }

    // Mide el tiempo de cálculo usando nThreads hilos
    private static ExperimentResult measure(int nThreads) {
        // Validaci\u00f3n de nThreads
        if (nThreads <= 0) throw new IllegalArgumentException("nThreads debe ser > 0");

        final java.util.concurrent.atomic.AtomicLongArray partials = new java.util.concurrent.atomic.AtomicLongArray(nThreads);
        Thread[] threads = new Thread[nThreads];

        // Divisi\u00f3n de trabajo: distribuimos el resto entre los primeros r hilos
        int base = N_MAX / nThreads;
        int rem = N_MAX % nThreads; // algunos hilos har\u00e1n 1 elemento extra

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
                // escribir resultado parcial en estructura segura para hilos
                partials.set(idx, localSum);
            }, "Worker-" + i);
            start = end + 1;
        }

        // Comprueba que cubrimos todo el rango sin omisiones ni duplicados
        // (comprobaci\u00f3n por seguridad)
        if (start != N_MAX + 1) {
            throw new IllegalStateException("Error en la asignaci\u00f3n de rangos: no se cubre todo el intervalor");
        }

        long t0 = System.nanoTime();
        // Iniciar hilos
        for (Thread th : threads) th.start();
        // Esperar a que terminen
        for (Thread th : threads) {
            try { th.join(); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); System.err.println("Main interrumpido"); }
        }
        long t1 = System.nanoTime();

        // Ensamblar resultado (no hay condiciones de carrera porque todos los hilos ya terminaron y join() establece happens-before)
        long total = 0L;
        for (int i = 0; i < partials.length(); i++) total += partials.get(i);

        return new ExperimentResult(total, t1 - t0);
    }

    // Definici\u00f3n de f(i) -- modificar aqu\u00ed si la funci\u00f3n fuera otra
    private static long computeFi(int i) {
        // Interpretaci\u00f3n usada: f(i) = i^2 + 3*i + 1
        long li = i;
        return li * li + 3L * li + 1L;
    }
}
