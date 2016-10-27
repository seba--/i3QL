#INPUT
datafiles <- c(file.path("2016-10-27", "hospital2-fjd-measure_20000-1","benchmark-mem.csv"))
              # file.path("hospital3-measure_20000-2","benchmark-mem.csv"),
               #file.path("hospital3-measure_20000-3","benchmark-mem.csv"))

#OUTPUT
mem <- numeric(4)
for (i in 1:length(datafiles)) {
	temp <- read.csv(file = datafiles[i] , head=TRUE, sep=",", colClasses = c("character","character","numeric","numeric","numeric"))
	temp <- temp[order(temp$host),]
	
	mem <- mem + (temp$memoryAfter - temp$memoryBefore)
}

mem <- mem / length(datafiles)
mem <- mem /1024 / 1024

hosts <- temp$host
hosts <- gsub("client", "Client", hosts)
hosts <- gsub("knowledge-db", "KnowledgeDB", hosts)
hosts <- gsub("patient-db", "PatientDB", hosts)
hosts <- gsub("person-db", "PersonDB", hosts)

par(cex=1.6)
barplot(
	height = mem, 
	names.arg = hosts, 
	col = "darkslategray3",
	ylab = "memory consumption (MiB)", 
	#xlim = c(0, 4), # width of bars
	ylim = c(-5, 10), 
	axes = TRUE,
	space = 0.2, #space between bars
	las = 1 #label rotation
	)
