filesA <- c(file.path("hospital", "2016-11-09", "query1", "default-25000_measure-50000_2", "benchmark-msginfo.csv"),
		   file.path("hospital", "2016-11-09", "query2", "default-25000_measure-50000_1", "benchmark-msginfo.csv"),
		   file.path("hospital", "2016-11-09", "query3", "default-25000_measure-50000_1", "benchmark-msginfo.csv"))

colors <- c("blue", "red", "darkgreen")
configs <- c("Config 1", "Config 2", "Config 3")

for (i in 1:3) {
	dataA <- read.csv(file = filesA[i] , head=TRUE, sep=",", colClasses = c("numeric","numeric")) 

	numberOfEntries <- length(data$receivingTime)
	
	x <- dataA$receivingTime - startTime
	y <- 1:length(x)
	
	if (i == 1) {
		par(cex = 1.5)
		plot(x, y, type="n", xlim = c(0,1500), ylim = c(0,25000), xlab = "Elapsed time (ms)", ylab = "number of events received")
		legend("topleft", legend = configs, lwd=1, col = colors) 
	}
	lines(x, y, col=colors[i])
}

ttr <- matrix(bars,ncol=2,byrow=TRUE)