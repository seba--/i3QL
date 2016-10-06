source("preparedata.R")

formatd <- function(x) format(round(x, 0), nsmall=0)
formatp <- function(x) format(round(x, 2), nsmall=2)
formatt <- function(x) format(round(x, 3), nsmall=3)

outfile <- file.path("evaluation", "table.tex")

firstResult <- 0



write("\\begin{table}  \\begin{center}",file=outfile)
write("\\begin{tabular}{ l r r r }",file=outfile,append=TRUE)
# write("& \\multicolumn{1}{ c }{\\bf NO-OPT} & \\multicolumn{2}{ c }{\\bf OPT}\\\\",file=outfile,append=TRUE)
write("\\toprule",file=outfile,append=TRUE)
write("\\multicolumn{1}{r|}{} & Time (no opt.) & Time (with opt.) & Speedup \\\\",file=outfile,append=TRUE)
write("\\midrule",file=outfile,append=TRUE)
i <- 0
for (s in names) {
  i <- i + 1
  
  optType <- "default"
  dataTime <- preparecsv5("time", optType, s, skipLines=0)
#  dataMemory <-  preparecsv5("memory", optType, s, skipLines=1)
  
  optType <- "no-opts"
  dataTimeNoOpts <- preparecsv3("time", optType, s, skipLines=0)
#  dataMemNoOpts <- preparecsv3("memory", optType, s, skipLines=1)
  
  optType <- "no-lms-opts"
  dataTimeNoLMSOpts <- preparecsv3("time", optType, s, skipLines=0)
#  dataMemNoLMSOpts <- preparecsv3("memory", optType, s, skipLines=1)
  
  dataTimeSum <- sum(dataTime$average)
  dataTimeNoOptsSum <- sum(dataTimeNoOpts$average)
  dataTimeNoLMSOptsSum <- sum(dataTimeNoLMSOpts$average)
  
  write(paste("\\multicolumn{1}{r|}{", rawToChar(as.raw(64 + i)), "}",
              #NO-OPTS             
              " & ", formatt(dataTimeNoLMSOptsSum / 1000) ,   
#              " & ", formatd(sum(dataMemNoLMSOpts$average)) ,                         
              #LMS_OPTS
#              " & ", formatt(dataTimeNoOptsSum / 1000) ,
#              " & ", formatp(dataTimeNoLMSOptsSum / dataTimeNoOptsSum),
#              " & ", formatd(sum(dataMemNoOpts$average)) ,
              #ALG-LMS-OPTS
              " & ", formatt(dataTimeSum / 1000),
              " & ", formatp(dataTimeNoLMSOptsSum / dataTimeSum),
#              " & ", formatd(sum(dataMemory$average)),              
              
              " \\\\" ,sep=""),file=outfile,append=TRUE)
  
  firstResult <- firstResult + dataTime$average[1]
  
}

write("\\bottomrule",file=outfile,append=TRUE)
write("\\end{tabular}",file=outfile,append=TRUE)
write("\\end{center}",file=outfile,append=TRUE)
write("{\\tiny FindBugs patterns from \\href{http://findbugs.sourceforge.net/bugDescriptions.html}{http://findbugs.sourceforge.net/bugDescriptions.html}: A=BX\\textunderscore\\allowbreak{}BOXING\\textunderscore\\allowbreak{}IMMEDIATELY\\textunderscore\\allowbreak{}UNBOXED\\textunderscore\\allowbreak{}TO\\textunderscore\\allowbreak{}PERFORM\\textunderscore\\allowbreak{}COERCION, B=CI\\textunderscore\\allowbreak{}CONFUSED\\textunderscore\\allowbreak{}INHERITANCE, C=CN\\textunderscore\\allowbreak{}IDIOM, D=CN\\textunderscore\\allowbreak{}IDIOM\\textunderscore\\allowbreak{}NO\\textunderscore\\allowbreak{}SUPER\\textunderscore\\allowbreak{}CALL, E=DM\\textunderscore\\allowbreak{}GC, F=DM\\textunderscore\\allowbreak{}RUN\\textunderscore\\allowbreak{}FINALIZERS\\textunderscore\\allowbreak{}ON\\textunderscore\\allowbreak{}EXIT, G=DP\\textunderscore\\allowbreak{}DO\\textunderscore\\allowbreak{}INSIDE\\textunderscore\\allowbreak{}DO\\textunderscore\\allowbreak{}PRIVILEGED, H=EQ\\textunderscore\\allowbreak{}ABSTRACT\\textunderscore\\allowbreak{}SELF, I=FI\\textunderscore\\allowbreak{}PUBLIC\\textunderscore\\allowbreak{}SHOULD\\textunderscore\\allowbreak{}BE\\textunderscore\\allowbreak{}PROTECTED, J=IMSE\\textunderscore\\allowbreak{}DONT\\textunderscore\\allowbreak{}CATCH\\textunderscore\\allowbreak{}IMSE, K=MS\\textunderscore\\allowbreak{}PKGPROTECT, L=MS\\textunderscore\\allowbreak{}SHOULD\\textunderscore\\allowbreak{}BE\\textunderscore\\allowbreak{}FINAL, M=SE\\textunderscore\\allowbreak{}BAD\\textunderscore\\allowbreak{}FIELD\\textunderscore\\allowbreak{}INNER\\textunderscore\\allowbreak{}CLASS, N=SW\\textunderscore\\allowbreak{}SWING\\textunderscore\\allowbreak{}METHODS\\textunderscore\\allowbreak{}INVOKED\\textunderscore\\allowbreak{}IN\\textunderscore\\allowbreak{}SWING\\textunderscore\\allowbreak{}THREAD, O=UG\\textunderscore\\allowbreak{}SYNC\\textunderscore\\allowbreak{}SET\\textunderscore\\allowbreak{}UNSYNC\\textunderscore\\allowbreak{}GET\\par}",file=outfile,append=TRUE)
write("\\caption{Total time in seconds and optimization speedup for analyzing the initial revision and replaying all $242$ changes.}",file=outfile,append=TRUE)
write("\\label{tbl:analysisbenchmarks}",file=outfile,append=TRUE)
write("\\end{table}",file=outfile,append=TRUE)

firstResult <- firstResult / length(names)
print(firstResult)
