#INPUT
files <- c(file.path("hospital", "2016-11-09", "query1", "default-2500_measure-50000_4", "benchmark-mem.csv"),
		   file.path("hospital", "2016-11-09", "query2", "default-2500_measure-50000_4", "benchmark-mem.csv"),
		   file.path("hospital", "2016-11-09", "query3", "default-2500_measure-50000_4", "benchmark-mem.csv"))

#OUTPUT
mem <- numeric(12)
for (i in 1:length(files)) {
	data <- read.csv(file = files[i] , head=TRUE, sep=",")
	data <- data[order(data$host),]
	
	
	temp <- data$memoryAfter - data$memoryBefore
	x <- 4 * i - 3
	y <- i * 4
	mem[x:y] <- temp 
	
}

mem <- mem /1024 / 1024

memBars <- matrix(mem, ncol = 4, byrow = TRUE)

# hosts <- data$host
# hosts <- gsub("client", "Client", hosts)
# hosts <- gsub("knowledge-db", "KnowledgeDB", hosts)
# hosts <- gsub("patient-db", "PatientDB", hosts)
# hosts <- gsub("person-db", "PersonDB", hosts)
# 
 par(cex=1.6)
 barplot(
 	height = memBars, 
 	beside = TRUE,
 	names.arg = c("Config 1", "Config 2", "Config 3"), 
 	col = "darkslategray3",
 	ylab = "memory consumption (MiB)", 
 	xlim = c(0, 4), # width of bars
 	ylim = c(-10, 10), 
 	axes = TRUE,
 	space = 0.2, #space between bars
 	las = 1 #label rotation
 	)
