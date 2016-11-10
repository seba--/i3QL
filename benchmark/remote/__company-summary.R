source("benchmark-utils.R")

#Parameters
benchmarkGroup <- file.path("company", "2016-11-10")
benchmarkA <- "default_measure-4000-all"
benchmarkB <- "default_measure-4000-client"
queryNumbers <- c(1, 2, 3, 4, 7)
benchmarkNumbers <- c(1, 2, 3)

#Definitions
readSummaries <- function(benchmark) {
	for (i in 1:length(queryNumbers)) {
		f <- file.path(benchmarkGroup, paste("query", queryNumbers[i], sep = ""), benchmark)
		tempA <- averageSummary(f, benchmarkNumbers)
		tempB <- averageMemory(f, benchmarkNumbers, "client")
		
		temp <- tempA
		temp$usedMemory <- tempB$usedMemory

		if (i == 1)
			d <- temp
		else
			d[i,] <- temp[1,]
	}
	d
}

#Begin
summaryA <- readSummaries(benchmarkA)
summaryB <- readSummaries(benchmarkB)

data <- summaryA / summaryB
plotData <- matrix(c(data$medianDelay, data$runtime, data$usedMemory), ncol = length(queryNumbers), byrow = TRUE)

barplot(
	height = plotData, 
	names.arg = queryNumbers,
	beside = TRUE, 
	col = c("darksalmon", "cornflowerblue", "darkseagreen2"), 
	xlab = "Query identifier", 
	ylim = c(0,2.0)
)
lines(x = c(0, length(queryNumbers) * 4 + 1), y = c(1.0, 1.0), lt = "dotted")
legend(x = 16, y = 2.0, col = c("darksalmon", "cornflowerblue", "darkseagreen2"), pch = 19, legend = c("Latency", "Runtime", "Memory"), bty = "n")


