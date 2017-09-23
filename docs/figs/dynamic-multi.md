|benchmark  |algorithm   | nmethods|   0.05|   0.50|   0.95|   mean| overhead 0.05| overhead 0.50| overhead 0.95| overhead mean| ns per op| overhead ns per op|
|:----------|:-----------|--------:|------:|------:|------:|------:|-------------:|-------------:|-------------:|-------------:|---------:|------------------:|
|diameter   |instanceof  |        2|  14.11|  14.15|  14.17|  14.14|          0.00|          0.00|          0.00|          0.00|     13.48|               0.00|
|diameter   |instancefn  |        2|  15.17|  15.22|  15.24|  15.20|          0.02|          0.02|          0.02|          0.02|     14.50|               1.02|
|diameter   |dynafun     |        2|  22.28|  22.34|  22.39|  22.34|          0.12|          0.12|          0.12|          0.12|     21.30|               7.82|
|diameter   |nohierarchy |        2|  23.74|  23.94|  23.96|  23.86|          0.14|          0.14|          0.14|          0.14|     22.76|               9.27|
|diameter   |signatures  |        2|  38.16|  38.65|  39.03|  38.64|          0.35|          0.36|          0.35|          0.35|     36.85|              23.37|
|diameter   |hashmaps    |        2|  34.84|  35.14|  35.42|  35.15|          0.30|          0.30|          0.30|          0.30|     33.52|              20.04|
|diameter   |protocols   |        2| 140.59| 141.15| 142.79| 141.73|          1.85|          1.84|          1.82|          1.84|    135.17|             121.69|
|diameter   |defmulti    |        2|  82.66|  83.10|  84.75|  83.66|          1.00|          1.00|          1.00|          1.00|     79.79|              66.30|
|diameter   |instanceof  |        3|  27.16|  27.22|  27.25|  27.21|          0.00|          0.00|          0.00|          0.00|     25.95|               0.00|
|diameter   |instancefn  |        3|  32.20|  32.33|  32.34|  32.27|          0.08|          0.08|          0.07|          0.07|     30.78|               4.82|
|diameter   |dynafun     |        3|  38.30|  38.59|  38.94|  38.63|          0.17|          0.17|          0.17|          0.17|     36.85|              10.89|
|diameter   |nohierarchy |        3|  40.51|  40.88|  41.31|  41.00|          0.20|          0.20|          0.20|          0.20|     39.10|              13.14|
|diameter   |signatures  |        3|  51.53|  52.40|  53.09|  52.34|          0.36|          0.37|          0.37|          0.37|     49.91|              23.96|
|diameter   |hashmaps    |        3|  51.66|  52.31|  53.01|  52.38|          0.37|          0.37|          0.37|          0.37|     49.95|              24.00|
|diameter   |protocols   |        3| 166.69| 168.31| 169.96| 168.42|          2.08|          2.08|          2.05|          2.06|    160.62|             134.67|
|diameter   |defmulti    |        3|  94.15|  94.95|  96.97|  95.62|          1.00|          1.00|          1.00|          1.00|     91.19|              65.23|
|diameter   |instanceof  |        7|  34.43|  34.53|  34.64|  34.54|          0.00|          0.00|          0.00|          0.00|     32.94|               0.00|
|diameter   |instancefn  |        7|  39.87|  40.26|  40.40|  40.14|          0.08|          0.09|          0.09|          0.09|     38.28|               5.35|
|diameter   |dynafun     |        7|  49.54|  49.83|  50.74|  50.15|          0.24|          0.24|          0.24|          0.24|     47.83|              14.89|
|diameter   |nohierarchy |        7|  54.26|  55.03|  55.42|  54.85|          0.31|          0.32|          0.31|          0.31|     52.31|              19.37|
|diameter   |signatures  |        7|  67.94|  69.85|  70.51|  69.27|          0.52|          0.55|          0.54|          0.53|     66.06|              33.12|
|diameter   |hashmaps    |        7|  64.02|  63.80|  65.85|  65.02|          0.46|          0.45|          0.47|          0.46|     62.01|              29.07|
|diameter   |protocols   |        7| 192.65| 193.90| 196.12| 194.48|          2.47|          2.46|          2.41|          2.44|    185.47|             152.53|
|diameter   |defmulti    |        7|  98.62|  99.19| 101.52| 100.20|          1.00|          1.00|          1.00|          1.00|     95.56|              62.62|
|contains   |instanceof  |        1|  19.35|  19.42|  19.45|  19.40|          0.00|          0.00|          0.00|          0.00|     18.50|               0.00|
|contains   |instancefn  |        1|  22.70|  22.82|  22.86|  22.78|          0.01|          0.01|          0.01|          0.01|     21.73|               3.23|
|contains   |dynafun     |        1|  20.61|  20.69|  21.02|  20.77|          0.00|          0.00|          0.00|          0.00|     19.80|               1.30|
|contains   |nohierarchy |        1|  62.80|  65.08|  65.31|  64.17|          0.12|          0.13|          0.12|          0.12|     61.19|              42.69|
|contains   |signatures  |        1|  68.30|  69.90|  71.67|  70.13|          0.14|          0.14|          0.14|          0.14|     66.88|              48.38|
|contains   |hashmaps    |        1|  77.89|  79.03|  81.74|  79.80|          0.16|          0.16|          0.17|          0.17|     76.10|              57.60|
|contains   |protocols   |        1|  22.33|  22.42|  22.50|  22.43|          0.01|          0.01|          0.01|          0.01|     21.39|               2.89|
|contains   |defmulti    |        1| 375.90| 384.50| 391.62| 384.13|          1.00|          1.00|          1.00|          1.00|    366.34|             347.83|
|contains   |instanceof  |        4|  33.63|  33.83|  33.84|  33.74|          0.00|          0.00|          0.00|          0.00|     32.18|               0.00|
|contains   |instancefn  |        4|  35.90|  36.14|  36.18|  36.05|          0.00|          0.00|          0.00|          0.00|     34.38|               2.20|
|contains   |dynafun     |        4|  53.34|  53.86|  54.19|  53.78|          0.04|          0.04|          0.04|          0.04|     51.29|              19.11|
|contains   |nohierarchy |        4|  89.29|  90.17|  91.72|  90.48|          0.11|          0.10|          0.11|          0.11|     86.29|              54.11|
|contains   |signatures  |        4|  94.69|  96.95|  97.68|  96.31|          0.12|          0.12|          0.12|          0.12|     91.85|              59.67|
|contains   |hashmaps    |        4| 115.42| 118.06| 121.74| 118.49|          0.15|          0.16|          0.16|          0.16|    113.00|              80.82|
|contains   |protocols   |        4| 143.41| 144.76| 145.33| 144.39|          0.21|          0.21|          0.20|          0.21|    137.70|             105.52|
|contains   |defmulti    |        4| 561.83| 572.40| 579.63| 571.17|          1.00|          1.00|          1.00|          1.00|    544.71|             512.53|
|contains   |instanceof  |        6|  35.54|  35.69|  35.77|  35.67|          0.00|          0.00|          0.00|          0.00|     34.01|               0.00|
|contains   |instancefn  |        6|  34.01|  34.13|  34.24|  34.14|          0.00|          0.00|          0.00|          0.00|     32.56|              -1.46|
|contains   |dynafun     |        6|  55.02|  55.41|  56.10|  55.56|          0.04|          0.04|          0.04|          0.04|     52.99|              18.97|
|contains   |nohierarchy |        6|  94.86|  96.96|  97.68|  96.33|          0.11|          0.11|          0.11|          0.11|     91.87|              57.85|
|contains   |signatures  |        6|  93.36|  94.71|  96.56|  94.90|          0.11|          0.11|          0.11|          0.11|     90.50|              56.49|
|contains   |hashmaps    |        6| 115.64| 118.28| 120.90| 118.41|          0.15|          0.15|          0.15|          0.15|    112.93|              78.91|
|contains   |protocols   |        6| 166.09| 167.71| 168.41| 167.33|          0.25|          0.24|          0.24|          0.24|    159.58|             125.57|
|contains   |defmulti    |        6| 567.97| 586.14| 587.32| 577.57|          1.00|          1.00|          1.00|          1.00|    550.81|             516.80|
|contains   |instanceof  |       42|  63.64|  63.94|  64.20|  63.94|          0.00|          0.00|          0.00|          0.00|     60.97|               0.00|
|contains   |instancefn  |       42|  64.96|  65.43|  65.69|  65.35|          0.00|          0.00|          0.00|          0.00|     62.32|               1.34|
|contains   |dynafun     |       42|  84.62|  85.17|  85.96|  85.27|          0.04|          0.04|          0.04|          0.04|     81.32|              20.34|
|contains   |nohierarchy |       42| 114.00| 116.02| 117.09| 115.61|          0.10|          0.10|          0.10|          0.10|    110.26|              49.28|
|contains   |signatures  |       42| 113.41| 114.80| 116.39| 114.96|          0.10|          0.10|          0.10|          0.10|    109.64|              48.66|
|contains   |hashmaps    |       42| 140.17| 143.44| 145.27| 142.76|          0.15|          0.15|          0.15|          0.15|    136.15|              75.17|
|contains   |protocols   |       42| 202.53| 203.99| 205.99| 204.20|          0.27|          0.27|          0.27|          0.27|    194.74|             133.76|
|contains   |defmulti    |       42| 574.24| 582.91| 593.10| 584.17|          1.00|          1.00|          1.00|          1.00|    557.11|             496.13|
|intersects |instanceof  |        1|  18.06|  18.08|  25.15|  19.01|          0.00|          0.00|          0.00|          0.00|     18.13|               0.00|
|intersects |instancefn  |        1|  18.64|  18.73|  21.80|  19.59|          0.00|          0.00|         -0.01|          0.00|     18.68|               0.55|
|intersects |dynafun     |        1|  19.17|  19.28|  19.34|  19.25|          0.00|          0.00|         -0.01|          0.00|     18.36|               0.23|
|intersects |nohierarchy |        1|  28.54|  28.93|  29.05|  28.80|          0.02|          0.03|          0.01|          0.02|     27.47|               9.33|
|intersects |signatures  |        1|  54.75|  56.14|  57.02|  55.88|          0.09|          0.09|          0.07|          0.09|     53.29|              35.16|
|intersects |hashmaps    |        1|  71.07|  75.35|  76.71|  74.39|          0.13|          0.13|          0.12|          0.13|     70.95|              52.82|
|intersects |protocols   |        1|  19.26|  19.35|  19.40|  19.33|          0.00|          0.00|         -0.01|          0.00|     18.43|               0.30|
|intersects |defmulti    |        1| 442.00| 445.76| 455.32| 448.21|          1.00|          1.00|          1.00|          1.00|    427.45|             409.32|
|intersects |instanceof  |        2|  22.22|  22.29|  22.41|  22.32|          0.00|          0.00|          0.00|          0.00|     21.28|               0.00|
|intersects |instancefn  |        2|  26.85|  26.94|  27.04|  26.94|          0.01|          0.01|          0.01|          0.01|     25.69|               4.41|
|intersects |dynafun     |        2|  44.81|  44.96|  45.59|  45.26|          0.04|          0.04|          0.04|          0.04|     43.16|              21.88|
|intersects |nohierarchy |        2|  46.58|  46.79|  46.99|  46.81|          0.04|          0.04|          0.04|          0.04|     44.65|              23.36|
|intersects |signatures  |        2|  74.06|  75.34|  76.35|  75.30|          0.09|          0.09|          0.09|          0.09|     71.81|              50.53|
|intersects |hashmaps    |        2|  93.44|  94.64|  97.33|  95.36|          0.12|          0.13|          0.13|          0.13|     90.94|              69.66|
|intersects |protocols   |        2| 134.41| 135.56| 136.06| 135.29|          0.20|          0.20|          0.20|          0.20|    129.02|             107.74|
|intersects |defmulti    |        2| 592.96| 595.36| 601.89| 597.27|          1.00|          1.00|          1.00|          1.00|    569.60|             548.32|
|intersects |instanceof  |        9|  44.16|  44.36|  44.57|  44.38|          0.00|          0.00|          0.00|          0.00|     42.33|               0.00|
|intersects |instanceof  |        9|  43.70|  43.81|  44.01|  43.87|          0.00|          0.00|          0.00|          0.00|     41.84|               0.00|
|intersects |instancefn  |        9|  52.35|  52.85|  53.04|  52.71|          0.01|          0.01|          0.01|          0.01|     50.27|               7.94|
|intersects |instancefn  |        9|  51.63|  52.12|  52.25|  51.95|          0.01|          0.01|          0.01|          0.01|     49.55|               7.71|
|intersects |dynafun     |        9|  68.01|  67.88|  69.21|  68.57|          0.04|          0.04|          0.04|          0.04|     65.39|              23.07|
|intersects |dynafun     |        9|  69.40|  69.84|  70.55|  70.00|          0.05|          0.05|          0.05|          0.05|     66.75|              24.92|
|intersects |nohierarchy |        9|  85.77|  86.18|  86.92|  86.34|          0.07|          0.07|          0.07|          0.07|     82.34|              40.02|
|intersects |nohierarchy |        9|  87.47|  88.29|  88.69|  88.13|          0.08|          0.08|          0.08|          0.08|     84.04|              42.21|
|intersects |signatures  |        9| 103.37| 104.49| 106.23| 104.83|          0.10|          0.10|          0.11|          0.10|     99.98|              57.65|
|intersects |signatures  |        9| 103.27| 104.21| 106.54| 105.01|          0.11|          0.11|          0.11|          0.11|    100.14|              58.31|
|intersects |hashmaps    |        9| 123.17| 124.60| 128.68| 126.02|          0.14|          0.14|          0.14|          0.14|    120.18|              77.85|
|intersects |hashmaps    |        9| 124.56| 131.13| 132.60| 127.96|          0.15|          0.16|          0.16|          0.15|    122.03|              80.20|
|intersects |protocols   |        9| 195.39| 196.53| 198.49| 197.07|          0.26|          0.26|          0.26|          0.26|    187.94|             145.61|
|intersects |protocols   |        9| 191.56| 192.26| 193.92| 192.82|          0.27|          0.27|          0.26|          0.27|    183.88|             142.05|
|intersects |defmulti    |        9| 620.58| 626.74| 630.88| 625.67|          1.00|          1.00|          1.00|          1.00|    596.68|             554.35|
|intersects |defmulti    |        9| 600.78| 603.43| 610.95| 605.50|          1.00|          1.00|          1.00|          1.00|    577.45|             535.61|
|axpy       |instanceof  |        1|  32.83|  32.98|  33.17|  33.02|          0.00|          0.00|          0.00|          0.00|     31.49|               0.00|
|axpy       |instancefn  |        1|  33.22|  33.44|  33.57|  33.39|          0.00|          0.00|          0.00|          0.00|     31.85|               0.36|
|axpy       |dynafun     |        1|  36.79|  37.20|  37.79|  37.33|          0.01|          0.01|          0.01|          0.01|     35.60|               4.11|
|axpy       |nohierarchy |        1|  52.66|  53.75|  54.43|  53.48|          0.06|          0.06|          0.06|          0.06|     51.01|              19.52|
|axpy       |signatures  |        1|  71.73|  73.25|  74.90|  73.37|          0.12|          0.12|          0.12|          0.12|     69.97|              38.49|
|axpy       |hashmaps    |        1|  96.72| 100.49| 101.78|  99.23|          0.20|          0.21|          0.21|          0.20|     94.64|              63.15|
|axpy       |protocols   |        1|  49.68|  50.19|  50.70|  50.27|          0.05|          0.05|          0.05|          0.05|     47.94|              16.45|
|axpy       |defmulti    |        1| 356.52| 361.96| 367.70| 361.87|          1.00|          1.00|          1.00|          1.00|    345.10|             313.62|
|axpy       |instanceof  |      216|  88.78|  88.86|  90.82|  89.86|          0.00|          0.00|          0.00|          0.00|     85.70|               0.00|
|axpy       |instanceof  |      216|  88.73|  89.94|  90.64|  89.70|          0.00|          0.00|          0.00|          0.00|     85.55|               0.00|
|axpy       |instancefn  |      216|  90.48|  90.73|  92.51|  91.55|          0.00|          0.00|          0.00|          0.00|     87.31|               1.61|
|axpy       |instancefn  |      216|  90.93|  91.84|  93.55|  92.30|          0.00|          0.00|          0.01|          0.00|     88.02|               2.48|
|axpy       |dynafun     |      216| 167.05| 168.59| 169.91| 168.58|          0.15|          0.15|          0.14|          0.15|    160.77|              75.07|
|axpy       |dynafun     |      216| 164.47| 166.40| 167.17| 165.89|          0.14|          0.14|          0.14|          0.14|    158.20|              72.65|
|axpy       |nohierarchy |      216| 148.13| 150.58| 150.93| 149.67|          0.11|          0.11|          0.11|          0.11|    142.74|              57.04|
|axpy       |nohierarchy |      216| 146.70| 148.30| 148.97| 147.81|          0.11|          0.11|          0.11|          0.11|    140.96|              55.41|
|axpy       |signatures  |      216| 167.90| 171.16| 173.43| 170.69|          0.15|          0.15|          0.15|          0.15|    162.79|              77.08|
|axpy       |signatures  |      216| 170.85| 183.94| 184.24| 175.00|          0.15|          0.18|          0.17|          0.16|    166.90|              81.35|
|axpy       |hashmaps    |      216| 205.13| 207.12| 212.80| 209.21|          0.22|          0.22|          0.22|          0.22|    199.52|             113.81|
|axpy       |hashmaps    |      216| 201.36| 204.50| 207.93| 204.71|          0.21|          0.21|          0.22|          0.22|    195.22|             109.68|
|axpy       |protocols   |      216| 238.74| 240.13| 241.46| 240.10|          0.28|          0.28|          0.28|          0.28|    228.97|             143.27|
|axpy       |protocols   |      216| 247.20| 249.04| 251.55| 249.40|          0.30|          0.30|          0.30|          0.30|    237.85|             152.30|
|axpy       |defmulti    |      216| 624.30| 632.84| 637.17| 630.75|          1.00|          1.00|          1.00|          1.00|    601.53|             515.83|
|axpy       |defmulti    |      216| 618.55| 623.85| 631.24| 624.41|          1.00|          1.00|          1.00|          1.00|    595.49|             509.94|