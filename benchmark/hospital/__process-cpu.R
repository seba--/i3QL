#INPUT
dir <- file.path("2016-10-27")
f <- file.path(dir, "hospital2-fjd-measure_20000-1","benchmark-cpu.csv") 
from <- 0
to <- 360

#OUTPUT
cpuData <- read.csv(file = f , head=TRUE, sep=",", colClasses = c("character","character","numeric","numeric","numeric")) 
colnames(cpuData) <- c("type", "node", "date", "cputime", "cpu")

cpuData <- cpuData[from:to, ]

firstDate <- cpuData$date[1]
cpuData$date <- cpuData$date - firstDate

personData <- subset(cpuData, node == "person-db")
patientData <- subset(cpuData, node == "patient-db")
knowledgeData <- subset(cpuData, node == "knowledge-db")
clientData <- subset(cpuData, node == "client")

par(cex=1.6)
plot(clientData$date, clientData$cpu, type="n", yaxt="n", ylim=c(0.0,1.0), xlab = "Time elapsed (ms)", ylab = "CPU load")
axis(2, at = seq(0.0, 1.0, by = 0.1))
lines(clientData$date, clientData$cpu, col="blue")
lines(personData$date, personData$cpu, col="red")
lines(patientData$date, patientData$cpu, col="green")
lines(knowledgeData$date, knowledgeData$cpu, col="purple")
legend("topright", legend = c("Client", "PersonDB", "PatientDB", "KnowledgeDB"), lwd=1, col = c("blue","red", "green", "purple")) 

dir.create(file.path("r"))
write.table(personData, file = file.path("r", "person-cpu.csv"), row.names=FALSE, col.names=TRUE, quote=FALSE, sep=",")
write.table(patientData, file = file.path("r", "patient-cpu.csv"), row.names=FALSE, col.names=TRUE, quote=FALSE, sep=",")
write.table(knowledgeData, file = file.path("r", "knowledge-cpu.csv"), row.names=FALSE, col.names=TRUE, quote=FALSE, sep=",")
write.table(clientData, file = file.path("r", "client-cpu.csv"), row.names=FALSE, col.names=TRUE, quote=FALSE, sep=",")