source("benchmark-utils.R")

#Parameters
benchmarkGroup <- file.path("company", "paper-aws")
benchmarkA1 <- "default_aws-4000-np"
benchmarkA2 <- "default_aws-4000-all"
benchmarkB <- "default_aws-4000-client"
queryNumbers <- c(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
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
summaryA1 <- readSummaries(benchmarkA1)
summaryA2 <- readSummaries(benchmarkA2)
summaryB <- readSummaries(benchmarkB)

data1 <- summaryA1 / summaryB
for (i in 1:length(data1)) {
	if (data1$usedMemory[i] < 0)
		data1$usedMemory[i] = 0
}

data2 <- summaryA2 / summaryB
for (i in 1:length(data2)) {
	if (data2$usedMemory[i] < 0)
		data2$usedMemory[i] = 0
}
	
#Initialize data that should be plotted
plotDataMem <- matrix(c(data1$usedMemory, data2$usedMemory), ncol = length(queryNumbers), byrow = TRUE)
plotDataTime <- matrix(c(data1$runtime, data2$runtime), ncol = length(queryNumbers), byrow = TRUE)

#Set plot parameters
par(cex=2.0)
par(mar=c(1,2,1.3,0))

#Plot memory data
colorMem1 <- rgb(178, 171, 210, maxColorValue = 255)
colorMem2 <- rgb(94, 60, 153, maxColorValue = 255)

barplot(
	height = plotDataMem, 
	names.arg = queryNumbers,
	beside = TRUE, 
	col = c(colorMem1, colorMem2), 
	xlab = "Query identifier", 
	ylim = c(0,1.5),
	yaxt = "n"
)
axis(2, at = seq(0.0, 1.5, by = 0.25))
lines(x = c(0, length(queryNumbers) * 4 + 1), y = c(1.0, 1.0), lt = "dotted")

#Plot runtime data
colorTime1 <- rgb(253, 184, 99, maxColorValue = 255)
colorTime2 <- rgb(230, 97, 1, maxColorValue = 255)

barplot(
	height = plotDataTime, 
	names.arg = queryNumbers,
	beside = TRUE, 
	col = c(colorTime1, colorTime2), 
	xlab = "Query identifier", 
	ylim = c(0,1.5),
	yaxt = "n"
)
axis(2, at = seq(0.0, 1.5, by = 0.25))
lines(x = c(0, length(queryNumbers) * 4 + 1), y = c(1.0, 1.0), lt = "dotted")


