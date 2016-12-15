source("benchmark-utils.R")

#Parameters
benchmarkGroup <- file.path("company", "paper")
benchmarkA <- "default_measure-4000-all"
benchmarkB <- "default_measure-4000-client"
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
summaryA <- readSummaries(benchmarkA)
summaryB <- readSummaries(benchmarkB)

data <- summaryA / summaryB
for (i in 1:length(data)) {
	if (data$usedMemory[i] < 0)
		data$usedMemory[i] = 0
}
	
plotData <- matrix(c(data$runtime, data$usedMemory), ncol = length(queryNumbers), byrow = TRUE)

color1 <- rgb(215, 25, 28, 255,  maxColorValue = 255)
color2 <- rgb(171, 217, 233,  maxColorValue = 255)

par(cex=2.0)
par(mar=c(1,2,1.3,0))
barplot(
	height = plotData, 
	names.arg = queryNumbers,
	beside = TRUE, 
	col = c(color2, color1), 
	xlab = "Query identifier", 
	ylim = c(0,1.5),
	yaxt = "n"
)
axis(2, at = seq(0.0, 1.5, by = 0.25))

lines(x = c(0, length(queryNumbers) * 4 + 1), y = c(1.0, 1.0), lt = "dotted")


