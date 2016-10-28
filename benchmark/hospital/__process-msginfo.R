files <- c(file.path("2016-10-27", "hospital1-fjd-measure_20000-1", "benchmark-msginfo.csv"),
		   file.path("2016-10-27", "hospital2-fjd-measure_20000-1", "benchmark-msginfo.csv"),
		   file.path("2016-10-27", "hospital3-fjd-measure_20000-1", "benchmark-msginfo.csv"))
colors <- c("blue", "red", "darkgreen")
configs <- c("Config 1", "Config 2", "Config 3")

for (i in 1:3) {
	data <- read.csv(file = files[i] , head=TRUE, sep=",", colClasses = c("numeric","numeric")) 
	
	startTime <- data$sendingTime[1]
	
	x <- data$receivingTime - startTime
	y <- 1:length(x)
	
	if (i == 1) {
		par(cex = 1.5)
		plot(x, y, type="n", xlim = c(0,250), xlab = "Elapsed time (ms)", ylab = "number of events received")
		legend("topleft", legend = configs, lwd=1, col = colors) 
	}
	lines(x, y, col=colors[i])
}
