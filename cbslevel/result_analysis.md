## 1. A* vs MA-CBS

| Level              | Algrithom        | Time Cost(s)         | Space Num         | Solution |
|--------------------|------------------|----------------------|-------------------|----------|
| MAsimple2          | A*               | 0,193                | 3.250             | 30       |
| MAsimple2          | MA-CBS(1)        | 29,630               | 21.000            | 30       |
| MAsimple3          | A*               | 0,130                | 3.193             | 40       |
| MAsimple3          | MA-CBS(1)        | 34,203               | 14.000            | 38       |
| MAsimple4          | A*               | 0,049                | 191               | 6        |
| MAsimple4          | MA-CBS(1)        | 0,293                | 258               | 6        |
| MAsimple1          | A*               | 0,57                 | 21.867            | 17       |
| MAsimple1          | MA-CBS(infinite) | 0.617                | 194/568           | 17       |
| MAsimple1          | MA-CBS(1)        | no conflict,no merge | 194/568           | 17       |
| MAsimple1          | MA-CBS(2)        | no conflict,no merge | 21.000            | 30       |
| MAsimple1-Design   | A*               | 4,371                | 352.069           | 26       |
| MAsimple1-Design   | MA-CBS(infinite) | 2,308                | 276/4.472         | 26       |
| MAsimple1-Design   | MA-CBS(1)        | no conflict,no merge | 276/4.472         | 26       |
| MAsimple1-Design   | MA-CBS(2)        | no conflict,no merge | 276/4.472         | 26       |
| MAsimple1-Design-2 | A*               |                      |
| MAsimple1-Design-2 | MA-CBS(infinite) | 13,403               | 396/4.472/14.494  | 29       |
| MAsimple1-Design-2 | MA-CBS(1-2)      |                      |
| MAsimple1-Design-2 | MA-CBS(5)        | no merge is better   | 396/4.472/14.494  | 29       |
| MAsimple1-Design-3 | MA-CBS(infinite) | 9,133                | 1.574/            | 17       |
| MAsimple1-Design-3 | MA-CBS(5)        | 9,133                | 1.574/961/388/384 | 17       |

