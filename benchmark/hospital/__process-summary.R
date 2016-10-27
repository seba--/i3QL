datafile <- c(file.path("2016-10-27", "hospital1-fjd-measure_20000-1","benchmark-summary.csv"),
			  file.path("2016-10-27", "hospital2-fjd-measure_20000-1","benchmark-summary.csv"),
			  file.path("2016-10-27", "hospital3-fjd-measure_20000-1","benchmark-summary.csv"))

delay <- numeric(3)
throughput <- numeric(3)
runtime <- numeric(3)


for (i in 1:3) {
	data <- read.csv(file = datafile[i] , head=TRUE, sep=",", colClasses = c("character","character","character","numeric","numeric","numeric","numeric","numeric","numeric")) 
	delay[i] <- data$averageDelay
	throughput[i] <- data$eventsPerSecond
	runtime[i] <- data$timeToReceive
	
}

par(cex=1.6)
#Plot delay
barplot(height = delay, names.arg = c("Config 1", "Config 2", "Config 3"), col = "darkslategray3", ylab = "time (ms)", ylim = c(0,200))

#Plot throughput
barplot(height = throughput, names.arg = c("Config 1", "Config 2", "Config 3"), col = "darkslategray3", ylab = "events per second", ylim = c(0,10000))

#Plot runtime
barplot(height = runtime, names.arg = c("Config 1", "Config 2", "Config 3"), col = "darkslategray3", ylab = "time to receive (ms)", ylim = c(0,250))
