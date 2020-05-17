
# Performing DataBase Ingester

> Scheduler + Bulker + AsyncLogger

### Hardware

![From CPU To Hard Disk](src/main/resources/CPU2HD.gif)

***The computer have a CPU i7 3630QM, 8 GB RAM and Hard Disk SATA II.***

---
### Functionalities

![Functionalities](src/main/resources/Functionalities.JPG)

***Functionalities of less than 300 code rows in this project.***

---
## Performances

With the aforesaid computer and the **300 code rows**, the 3 packages (***Scheduler***, ***Bulker*** and 
***AsyncLogger***) allow to insert **23000 rows per second** in the 3 MySQL tables and the deletion 
every **90 seconds**.

---
## Summary

These 3 packages allow:

- **Easiness** and **smartness** of writing code 
- **Thread optimization** through the ***Scheduler*** package
- **Insert** and **delete optimization** through the ***Bulk*** package
- **Logs optimization** through the ***AsyncLogger*** package

---
