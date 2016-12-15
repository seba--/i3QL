#INPUT
dir <- file.path("2016-11-03")
f <- file.path("hospital", "2016-11-09", "query3", "default-2500_measure-50000_4", "benchmark-cpu.csv")
from <- 0
to <- 750

#OUTPUT
cpuData <- read.csv(file = f , head=TRUE, sep=",", colClasses = c("character","character","numeric","numeric","numeric")) 
colnames(cpuData) <- c("type", "node", "date", "cputime", "cpu")

cpuData <- cpuData[from:to, ]

firstDate <- cpuData$date[1]
cpuData$date <- cpuData$date - firstDate

personData <- subset(cpuData, node == "person-node")
patientData <- subset(cpuData, node == "patient-node")
knowledgeData <- subset(cpuData, node == "knowledge-node")
clientData <- subset(cpuData, node == "client")

par(cex=3.0)
par(mar=c(2,2,0.1,0.5))
plot(clientData$date, clientData$cpu, type="n", yaxt="n", xlim=c(0,2000), ylim=c(0.0,1.0), xlab = "", ylab = "")
axis(2, at = seq(0.0, 1.0, by = 0.5), labels = c(0, 0.5, 1.0))
lines(clientData$date, clientData$cpu, col="blue")
lines(personData$date, personData$cpu, col="red")
lines(patientData$date, patientData$cpu, col="green")
lines(knowledgeData$date, knowledgeData$cpu, col="purple")
#legend(x = 1500, y = 1.4, legend = c("Client", "Person", "Patient", "Knowledge"), lwd=3, col = c("blue","red", "green", "purple")) 

dir.create(file.path("r"))
write.table(personData, file = file.path("r", "person-cpu.csv"), row.names=FALSE, col.names=TRUE, quote=FALSE, sep=",")
write.table(patientData, file = file.path("r", "patient-cpu.csv"), row.names=FALSE, col.names=TRUE, quote=FALSE, sep=",")
write.table(knowledgeData, file = file.path("r", "knowledge-cpu.csv"), row.names=FALSE, col.names=TRUE, quote=FALSE, sep=",")
write.table(clientData, file = file.path("r", "client-cpu.csv"), row.names=FALSE, col.names=TRUE, quote=FALSE, sep=",")