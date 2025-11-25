Tabla de comparación de tiempos

| Hilos | Tiempo (ms) |
| ----- | ----------- |
| 1     | 23.019 ms   |
| 2     | 10.725 ms   |
| 4     | 4.396 ms    |
| 8     | 18.475 ms   |

Ejectado en https://www.jdoodle.com/online-java-compiler

Reflexión breve 
- ¿Se volvió más rápido el programa?
Depende de la máquina y de cuántos núcleos físicos tenga. En mi prueba local fue más rápida con 1 hilo tardó 23 ms, con 2 hilos bajó a 10 ms y con 4 hilos a 4 ms.
Solo con 8 hilos subió porque mi CPU no soporta tantos, así que usar más hilos no siempre acelera.

- ¿Qué problemas encontraste al manejar datos compartidos?
Si los hilos escriben al mismo tiempo, la suma se arruina.
Por eso usé sincronización para que cada hilo guardara su parte sin pisar a los otros.

- ¿Qué aprendiste sobre programas con más de un hilo?
Los hilos mejoran el rendimiento solo si hay núcleos suficientes y poco trabajo de sincronización; si no, pueden hasta hacerlo más lento.
