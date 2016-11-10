averageSummary <- function(filesPrefix, fileNumbers) {

	for (i in fileNumbers) {
		
		dir <- paste(filesPrefix, "_", i, sep = "")
		f <- file.path(dir, "benchmark-summary.csv") 

		data <- read.csv(file = f , head=TRUE, sep=",") 
		data$type <- NULL
		data$benchmarkName <- NULL
		data$time <- NULL
		
		if (i == fileNumbers[1])
			average <- data
		else
			average <- data + average
	}
	
	average <- average / length(fileNumbers)
	
	average
}

averageMemory <-  function(filesPrefix, fileNumbers, hostName) {
	
	for (i in fileNumbers) {
		
		dir <- paste(filesPrefix, "_", i, sep = "")
		f <- file.path(dir, "benchmark-mem.csv") 
		
		data <- read.csv(file = f , head=TRUE, sep=",") 
		data <- data[data$host == hostName,]
		data$type <- NULL
		data$host <- NULL
		data$time <- NULL
		data$usedMemory <- data$memoryAfter - data$memoryBefore

		if (i == fileNumbers[1])
			average <- data
		else
			average <- data + average
	}
	
	average <- average / length(fileNumbers)
	
	average
}