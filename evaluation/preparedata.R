
preparecsv5 <- function(benchmarkType, optType, analysisName, skipLines = 0, ordered = FALSE) {
  data <- read.csv(file.path("findbugs-benchmarks",benchmarkType,optType,paste(analysisName,".properties.csv",sep="")), 
                   head=FALSE, 
                   sep=";",
                   skip = skipLines + 1,
                   colClasses = c("character","character","character","numeric","numeric","numeric","numeric","numeric","numeric","numeric","numeric","numeric","numeric","numeric","numeric","numeric")
  ) 
  
  colnames(data) <- c("benchmark", "queries", "location", "timestamp", "changes", "classes", "methods", "fields", "instructions", "warmup", "measure", "result.a", "result.b", "result.c", "result.d", "result.e")
  
  data$average <- (data$result.a + data$result.b + data$result.c + data$result.d + data$result.e) / 5

  data$totalchanges[1] <- data$changes[1]
  for (i in 2:nrow(data)) {
    data$totalchanges[i] <- data$totalchanges[i-1] + data$changes[i]
  }
  
  data$totalresult[1] <- data$average[1]
  for (i in 2:nrow(data)) {
    data$totalresult[i] <- data$totalresult[i-1] + data$average[i]
  }
  
  if (ordered) {
    data <- data[order(data$changes),]
  }
  
  
  return(data)
}

preparecsv3 <- function(benchmarkType, optType, analysisName, skipLines = 0, ordered = FALSE) {
  data <- read.csv(file.path("benchmarks",benchmarkType,optType,paste(analysisName,".properties.csv",sep="")), 
                   head=FALSE, 
                   sep=";",
                   skip = skipLines + 1,
                   colClasses = c("character","character","character","numeric","numeric","numeric","numeric","numeric","numeric","numeric","numeric","numeric","numeric","numeric")
  ) 
  
  colnames(data) <- c("benchmark", "queries", "location", "timestamp", "changes", "classes", "methods", "fields", "instructions", "warmup", "measure", "result.a", "result.b", "result.c")
  
  data$average <- (data$result.a + data$result.b + data$result.c) / 3
  
  data$totalchanges[1] <- data$changes[1]
  for (i in 2:nrow(data)) {
    data$totalchanges[i] <- data$totalchanges[i-1] + data$changes[i]
  }
  
  data$totalresult[1] <- data$average[1]
  for (i in 2:nrow(data)) {
    data$totalresult[i] <- data$totalresult[i-1] + data$average[i]
  }
  
  if (ordered) {
    data <- data[order(data$changes),]
  }
  
  return(data)
}


names <- c(
  "BX_BOXING_IMMEDIATELY_UNBOXED_TO_PERFORM_COERCION", 
  "CI_CONFUSED_INHERITANCE",  
  "CN_IDIOM",  
  "CN_IDIOM_NO_SUPER_CALL", 
  "DM_GC", 
  "DM_RUN_FINALIZERS_ON_EXIT", 
  #"DMI_LONG_BITS_TO_DOUBLE_INVOKED_ON_INT", 
  "DP_DO_INSIDE_DO_PRIVILEGED", 
  "EQ_ABSTRACT_SELF", 
  "FI_PUBLIC_SHOULD_BE_PROTECTED",
  #"FI_USELESS",
  "IMSE_DONT_CATCH_IMSE",
  "MS_PKGPROTECT",
  "MS_SHOULD_BE_FINAL",
  "SE_BAD_FIELD_INNER_CLASS",
  #"SS_SHOULD_BE_STATIC",
  "SW_SWING_METHODS_INVOKED_IN_SWING_THREAD",
  "UG_SYNC_SET_UNSYNC_GET"
)



# time_default_cnidiom <- preparecsv5("time","default","CN_IDIOM")
# time_default_dmgc <- preparecsv5("time","default","DM_GC")





# 
# colnames(data) <- c("benchmark", "queries", "location", "timestamp", "changes", "classes", "methods", "fields", "instructions", "warmup", "measure", "result.a", "result.b", "result.c", "result.d", "result.e")
# 
# data$average <- (data$result.a + data$result.b + data$result.c + data$result.d + data$result.e) / 5
# 
# data$totalchanges[1] <- data$changes[1]
# for (i in 2:nrow(data)) {
#        data$totalchanges[i] <- data$totalchanges[i-1] + data$changes[i]
# }
# 
# data$totaltime[1] <- data$average[1]
# for (i in 2:nrow(data)) {
#   data$totaltime[i] <- data$totaltime[i-1] + data$average[i]
# }






#time_default_ciconfusedinheritance <- read.csv("GitHub\\sae\\benchmarks\\time\\default\\CI_CONFUSED_INHERITANCE.properties.csv", head=FALSE, sep=";")
#preparecsv(time_default_ciconfusedinheritance)

# time_default_cnidiom <- read.csv("GitHub\\sae\\benchmarks\\time\\default\\CN_IDIOM.properties.csv", head=FALSE, sep=";")
# time_default_cnidiom <- time_default_cnidiom[-1, ]
# colnames(time_default_cnidiom) <- c("benchmark", "queries", "location", "timestamp", "changes", "classes", "methods", "fields", "instructions", "warmup", "measure", "result.a", "result.b", "result.c", "result.d", "result.e")
# 
# for (i in nrow(time_default_cnidiom)) {
#   time_default_cnidiom$average[i] <- (time_default_cnidiom$result.a[i] 
#                                    + time_default_cnidiom$result.b[i]
#                                    + time_default_cnidiom$result.c[i] 
#                                    + time_default_cnidiom$result.d[i] 
#                                    + time_default_cnidiom$result.e[i]) / 5 
# }
# 
# 
# print(time_default_cnidiom$result.a[2])

# time_default_ciconfusedinheritance <- read.csv("GitHub\\sae\\benchmarks\\time\\default\\CI_CONFUSED_INHERITANCE.properties.csv", head=FALSE, sep=";")
# time_default_ciconfusedinheritance <- time_default_ciconfusedinheritance[-1, ]
# colnames(time_default_ciconfusedinheritance)<-c("benchmark", "queries", "location", "timestamp", "changes", "classes", "methods", "fields", "instructions", "warmup", "measure", "result.a", "result.b", "result.c", "result.d", "result.e")
# time_default_ciconfusedinheritance$average <- (time_default_ciconfusedinheritance$result.a + 
#                                                  time_default_ciconfusedinheritance$result.b + 
#                                                  time_default_ciconfusedinheritance$result.c + 
#                                                  time_default_ciconfusedinheritance$result.d + 
#                                                  time_default_ciconfusedinheritance$result.e) / 5
# 
# 
# colnames(time_default_ciconfusedinheritance)<-c("benchmark", "queries", "location", "timestamp", "changes", "classes", "methods", "fields", "instructions", "warmup", "measure", "result.a", "result.b", "result.c", "result.d", "result.e")

# time_default <- c(time_default_cnidiom, time_default_ciconfusedinheritance)
# 
# keeps <- c("queries", "changes", "average")
# 
# time_default[keeps]



#raw123<-cbind(raw1,raw2,raw3)

#parseable<-subset(raw123,all.success.a=="true" & all.success.b=="true" & all.success.c=="true")


#p1<-subset(parseable,run==1,select=c(path, time.a, time.b, time.c, size,lines,reftime.a,reftime.b,reftime.c))
#p2<-subset(parseable,run==2,select=c( time.a, time.b, time.c, size,lines))
#p3<-subset(parseable,run==3,select=c( time.a, time.b, time.c, size,lines))
#colnames(p1)<-c("path","time1a","time1b","time1c","size1","lines1","reftime.a","reftime.b","reftime.c")
#colnames(p2)<-c("time2a","time2b","time2c","size2","lines2")
#colnames(p3)<-c("time3a","time3b","time3c","size3","lines3")
#c<-cbind(p1,p2,p3)

#errfun<-function(v) {
#  return(qt(0.95,df=length(v)-1)*sd(v)/sqrt(length(v)))
#}

#t1<-c("time1a","time1b","time1c")
#t2<-c("time2a","time2b","time2c")
#t3<-c("time3a","time3b","time3c")
#tr<-c("reftime.a","reftime.b","reftime.c")

#x=apply(c[,t1],1,FUN=mean)
#x<-cbind(x,apply(c[,t1],1,FUN=sd))
#x<-cbind(x,apply(c[,t1],1,FUN=errfun))
#x<-cbind(x,apply(c[,t2],1,FUN=mean))
#x<-cbind(x,apply(c[,t2],1,FUN=sd))
#x<-cbind(x,apply(c[,t2],1,FUN=errfun))
#x<-cbind(x,apply(c[,t3],1,FUN=mean))
#x<-cbind(x,apply(c[,t3],1,FUN=sd))
#x<-cbind(x,apply(c[,t3],1,FUN=errfun))
#x<-cbind(x,apply(c[,tr],1,FUN=mean))
#x<-cbind(x,apply(c[,tr],1,FUN=sd))
#x<-cbind(x,apply(c[,tr],1,FUN=errfun))
#colnames(x)<-c("mean1","sd1","err1","mean2","sd2","err2","mean3","sd3","err3","mean.r","sd.r","err.r")
#c<-cbind(c,x)
