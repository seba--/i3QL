source("preparedata.R")

changes <- 0

totalaverage <- 0
totalerror <- 0

#Options
plotsize <- 350
type <-  "time"
skip <- 1


#Initialization
sx = "Number of changes"  
if (type == "memory") {
  t = "Memory usage of analysis"
  sy = "Memory (KiB)"
} else if (type == "time") {
  t = "Duration of analysis"
  sy = "Time (ms)"
} else {
  t = "Results of analysis"
  sy = "Result"
}


plotAnalysisResults <- function(benchmarkType, analysisName) {
    
  dataDefault <- preparecsv5(benchmarkType, "default", analysisName, skipLines = skip)
  dataNoOpts <- preparecsv3(benchmarkType, "no-opts", analysisName, skipLines = skip)
  
  #Calculate total average
  totalaverage <<- totalaverage + dataDefault$average
  
  #Plot the data
  changes <<- dataDefault$changes  
  
  plot(x = dataNoOpts$changes, y = dataNoOpts$average, type = "l", col ="red", lty = 3, xlim = c(0,plotsize), ylim = c(0,plotsize),xlab=sx, ylab=sy)
 # title(main=paste(t,analysisName,sep="\n"))
  lines(x = dataDefault$changes, y = dataDefault$average, col ="red")
  lines(x = 0:plotsize, y = 0:plotsize, lty=2)
  
}



for (s in names) {
  plotAnalysisResults(type, s)
}

totalaverage <- totalaverage / length(names)

for (s in names) {
  data <- preparecsv5(type, "default", s, skipLines = skip)
  totalerror <- totalerror + abs(data$average - totalaverage)
}

totalerror <- totalerror / length(names)

plot(x = 0:plotsize, y = 0:plotsize, lty=2, type="l", xlim = c(0,plotsize), ylim = c(0,plotsize),ylab=sy, xlab=sx)
lines(x = changes, y = (totalaverage + totalerror), type = "l", col ="gray", lty = 1)
lines(x = changes, y = (totalaverage - totalerror), type = "l", col ="gray", lty = 1)
lines(x = changes, y = totalaverage, col="red")






# #Histogramm plot
# plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$totalresult, type = "h")
# plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$average, type = "h")
# #Stair step plot
# plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$totalresult, type = "s", col = "red") # two lines
# lines(x = time_default_cnidiom$totalchanges, y = time_default_dmgc$totalresult, type = "s", col="green", lty=3)
# 
# plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$totalresult, type = "s", col = "red")
# #Standard plot
# plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$average, type = "o") #lines and points
# 
# plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$average, type = "l", col ="red") #only lines
# lines(x = time_default_dmgc$totalchanges, y = time_default_dmgc$average,col="green")
# 
# plot(x = time_default_dmgc$totalchanges, y = time_default_dmgc$average, type = "l", col ="red", axes=FALSE, ann=FALSE) #only lines
# #Create two lines
# lines(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$average,col="green", lty=3)
# #Set axis
# axis(1, at=300*0:12)
# axis(2, at=40*0:7)
# #Set title
# title(main="Time of analyses")
# #Label axes
# title(xlab="Number of changes")
# title(ylab="Time (ms)")
# 
# 
# plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$average, type = "p", col ="red") #only points
# lines(x = time_default_dmgc$totalchanges, y = time_default_dmgc$average,col="green", type = "p")



