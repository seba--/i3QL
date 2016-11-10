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

par(cex=1.6)
#Plot delay
barplot(height = delayBars, names.arg = c("50%", "5%"), beside = TRUE, col = c("darkseagreen2", "darksalmon", "cornflowerblue"), ylab = "median latency (ms)", ylim = c(0,1200))
legend(x = 6.33, y = 1500, col = c("darkseagreen2", "darksalmon", "cornflowerblue"), pch = 19, legend = c("Config1" , "Config2", "Config3"))

#Plot runtime
barplot(height = runtimeBars, names.arg = c("50%", "5%"), beside = TRUE, col = c("darkseagreen2", "darksalmon", "cornflowerblue"), ylab = "runtime (ms)", ylim = c(0,2000))
legend(x = 6.33, y = 2400, col = c("darkseagreen2", "darksalmon", "cornflowerblue"), pch = 19, legend = c("Config1" , "Config2", "Config3"))


#Plot throughput
#barplot(height = throughput, names.arg = c("Config 1", "Config 2", "Config 3"), col = "darkslategray3", ylab = "events per second", ylim = c(0,12000))

#Plot runtime
#barplot(height = runtime, names.arg = c("Config 1", "Config 2", "Config 3"), col = "darkslategray3", ylab = "time to receive (ms)", ylim = c(0,1000))
