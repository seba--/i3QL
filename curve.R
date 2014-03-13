source("preparedata.R")

#Histogramm plot
plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$totalresult, type = "h")
plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$average, type = "h")
#Stair step plot
plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$totalresult, type = "s", col = "red") # two lines
lines(x = time_default_cnidiom$totalchanges, y = time_default_dmgc$totalresult, type = "s", col="green", lty=3)

plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$totalresult, type = "s", col = "red")
#Standard plot
plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$average, type = "o") #lines and points

plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$average, type = "l", col ="red") #only lines
lines(x = time_default_dmgc$totalchanges, y = time_default_dmgc$average,col="green")

plot(x = time_default_dmgc$totalchanges, y = time_default_dmgc$average, type = "l", col ="red", axes=FALSE, ann=FALSE) #only lines
#Create two lines
lines(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$average,col="green", lty=3)
#Set axis
axis(1, at=300*0:12)
axis(2, at=40*0:7)
#Set title
title(main="Time of analyses")
#Label axes
title(xlab="Number of changes")
title(ylab="Time (ms)")


plot(x = time_default_cnidiom$totalchanges, y = time_default_cnidiom$average, type = "p", col ="red") #only points
lines(x = time_default_dmgc$totalchanges, y = time_default_dmgc$average,col="green", type = "p")