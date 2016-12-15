filesA <- c(file.path("hospital", "2016-11-09", "query1", "default-25000_measure-50000_4", "benchmark-summary.csv"),
			file.path("hospital", "2016-11-09", "query2", "default-25000_measure-50000_4", "benchmark-summary.csv"),
			file.path("hospital", "2016-11-09", "query3", "default-25000_measure-50000_4", "benchmark-summary.csv"))
filesB <- c(file.path("hospital", "2016-11-09", "query1", "default-2500_measure-50000_4", "benchmark-summary.csv"),
			file.path("hospital", "2016-11-09", "query2", "default-2500_measure-50000_4", "benchmark-summary.csv"),
			file.path("hospital", "2016-11-09", "query3", "default-2500_measure-50000_4", "benchmark-summary.csv"))


delays <- numeric(6)
runtimes <- numeric(6)

for (i in 1:3) {
	dataA <- read.csv(file = filesA[i] , head=TRUE, sep=",") 
	dataB <- read.csv(file = filesB[i] , head=TRUE, sep=",") 
	
	delays[2 * i - 1] <- dataA$medianDelay
	delays[2 * i] <- dataB$medianDelay
	
	runtimes[2 * i - 1] <- dataA$runtime
	runtimes[2 * i] <- dataB$runtime

}

delayBars <- matrix(delays, ncol = 2, byrow = TRUE)
runtimeBars <- matrix(runtimes, ncol = 2, byrow = TRUE)

color1 <- rgb(127, 205, 187, 255,  maxColorValue = 255)
color2 <- rgb(44, 127, 184, 255,  maxColorValue = 255)
color3 <- rgb(237, 248, 177, 255,  maxColorValue = 255)

#Plot delay
pdf(file = "latency-plot.pdf", width = 4, height = 4)
	par(cex=1.6, mar=c(1,2,1.3,0))
	barplot(height = delayBars, beside = TRUE, col = c(color1, color2, color3), yaxt = "n", ylim = c(0,1200))
	axis(2, at = seq(0.0, 1200, by = 300))
	#legend(x = 6.33, y = 1500, bty = "n", col = c("darkseagreen2", "darksalmon", "cornflowerblue"), pch = 19, legend = c("Config1" , "Config2", "Config3"))
dev.off()

#Plot runtime
pdf(file = "runtime-plot.pdf", width = 7, height = 2.5)
	par(cex=1.6, mar=c(2,0.3,0,1))
	barplot(height = runtimeBars, horiz=TRUE, names.arg = c("50%", "5%"), beside = TRUE,
			col = c(color1, color2, color3), xaxt = "n", xlim = c(0,1800))
	axis(1, at = seq(0.0, 1800, by = 600))
	#legend(x = 6.33, y = 2400, bty = "n", col = c("darkseagreen2", "darksalmon", "cornflowerblue"), pch = 19, legend = c("Config1" , "Config2", "Config3"))
dev.off()


#Plot throughput
#barplot(height = throughput, names.arg = c("Config 1", "Config 2", "Config 3"), col = "darkslategray3", ylab = "events per second", ylim = c(0,12000))

#Plot runtime
#barplot(height = runtime, names.arg = c("Config 1", "Config 2", "Config 3"), col = "darkslategray3", ylab = "time to receive (ms)", ylim = c(0,1000))
