source("preparedata.R")

changes <- 0

totalaverage <- 0
totalerror <- 0

#Options
plotsizeX <- 310
plotsizeY <- 250
plotsizeMax <- max(plotsizeX, plotsizeY)
type <-  "time"
skip <- 1


#Initialization
sx = "Change size (in number of affected classes)"  
if (type == "memory") {
  t = "Memory usage of analysis"
  sy = "Memory change (KiB)"
} else if (type == "time") {
  t = "Duration of analysis"
  sy = "Time (ms)"
} else {
  t = "Results of analysis"
  sy = "Result"
}


plotAnalysisResults <- function(benchmarkType, analysisName) {
    
  dataDefault <- preparecsv5(benchmarkType, "default", analysisName, skipLines = skip, ordered = TRUE)
  dataNoOpts <- preparecsv3(benchmarkType, "no-opts", analysisName, skipLines = skip, ordered = TRUE)
  
  #Calculate total average
  totalaverage <<- totalaverage + dataDefault$average
  
  #Plot the data
  changes <<- dataDefault$changes  
  
  plot(x = dataNoOpts$changes, y = dataNoOpts$average, type = "l", col ="red", lty = 3, xlim = c(0,plotsizeX), ylim = c(0,plotsizeY),xlab=sx, ylab=sy)
 # title(main=paste(t,analysisName,sep="\n"))
  lines(x = dataDefault$changes, y = dataDefault$average, col ="red")
  lines(x = 0:plotsizeMax, y = 0:plotsizeMax, lty=2)
  
}



for (s in names) {
  plotAnalysisResults(type, s)
}

totalaverage <- totalaverage / length(names)

for (s in names) {
  data <- preparecsv5(type, "default", s, skipLines = skip, ordered = TRUE)
  totalerror <- totalerror + abs(data$average - totalaverage)
}

totalerror <- totalerror / length(names)

plot(x = -100:(plotsizeMax+100), y = rep(0, 201 + plotsizeMax), col = "gray", lty=3, type="l", xlim = c(0,plotsizeX), ylim = c(-0,plotsizeY),ylab=sy, xlab=sx)
#lines(x = changes, y = (totalaverage + totalerror), type = "l", col ="gray", lty = 1)
#lines(x = changes, y = (totalaverage - totalerror), type = "l", col ="gray", lty = 1)
lines(x = changes, y = totalaverage, col="red")

plot(x = -100:(plotsizeMax+100), y = -100:(plotsizeMax+100) , lty=3, type="l", xlim = c(0,plotsizeX), ylim = c(-0,plotsizeY),ylab=sy, xlab=sx)
#lines(x = changes, y = (totalaverage + totalerror), type = "l", col ="gray", lty = 1)
#lines(x = changes, y = (totalaverage - totalerror), type = "l", col ="gray", lty = 1)
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



