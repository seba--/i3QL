source("preparedata.R")

formatd <- function(x) format(round(x, 0), nsmall=0)
formatp <- function(x) format(round(x, 2), nsmall=2)

outfile <- file.path("evaluation", "table.tex")

firstResult <- 0



write("\\begin{table*} \\begin{center}",file=outfile)
write("\\begin{tabular}{ l  r  r | r r r | r r r }",file=outfile,append=TRUE)
write("& \\multicolumn{2}{ c }{NO-OPT} & \\multicolumn{3}{ c }{LMS-OPT}  & \\multicolumn{3}{ c }{ALG-LMS-OPT}\\\\",file=outfile,append=TRUE)
write("\\toprule",file=outfile,append=TRUE)
write("\\multicolumn{1}{r|}{} & Time & Memory & Time & Speedup & Memory & Time & Speedup & Memory \\\\",file=outfile,append=TRUE)
write("\\midrule",file=outfile,append=TRUE)
i <- 0
for (s in names) {
  i <- i + 1
  
  optType <- "default"
  dataTime <- preparecsv5("time", optType, s, skipLines = 0)
  dataMemory <-  preparecsv5("memory", optType, s, skipLines = 1)
  
  optType <- "no-opts"
  dataTimeNoOpts <- preparecsv3("time", optType, s, skipLines = 0)
  dataMemNoOpts <- preparecsv3("memory", optType, s, skipLines = 1)
  
  optType <- "no-lms-opts"
  dataTimeNoLMSOpts <- preparecsv3("time", optType, s, skipLines = 0)
  dataMemNoLMSOpts <- preparecsv3("memory", optType, s, skipLines = 1)
  
  dataTimeSum <- sum(dataTime$average)
  dataTimeNoOptsSum <- sum(dataTimeNoOpts$average)
  dataTimeNoLMSOptsSum <- sum(dataTimeNoLMSOpts$average)
  
  write(paste("\\multicolumn{1}{r|}{", rawToChar(as.raw(64 + i)), "}",
              #NO-OPTS             
              " & ", formatd(dataTimeNoLMSOptsSum) ,   
              " & ", formatd(sum(dataMemNoLMSOpts$average)) ,                         
              #LMS_OPTS
              " & ", formatd(dataTimeNoOptsSum) ,
              " & ", formatp(dataTimeNoLMSOptsSum / dataTimeNoOptsSum),
              " & ", formatd(sum(dataMemNoOpts$average)) ,
              #ALG-LMS-OPTS
              " & ", formatd(dataTimeSum),
              " & ", formatp(dataTimeNoLMSOptsSum / dataTimeSum),
              " & ", formatd(sum(dataMemory$average)),              
              
              " \\\\" ,sep=""),file=outfile,append=TRUE)
  
  firstResult <- firstResult + dataTime$average[1]
  
}

write("\\bottomrule",file=outfile,append=TRUE)
write("\\end{tabular}",file=outfile,append=TRUE)
write("\\end{center}",file=outfile,append=TRUE)
write("{\\small Bug patterns of the analyses similar to FindBugs: A = BX\\_BOXING\\_IMMEDIATELY\\_UNBOXED\\_TO\\_PERFORM\\_COERCION, B = CI\\_CONFUSED\\_INHERITANCE, C = CN\\_IDIOM, D = CN\\_IDIOM\\_NO\\_SUPER\\_CALL, E = DM\\_GC, F = DM\\_RUN\\_FINALIZERS\\_ON\\_EXIT, G = DP\\_DO\\_INSIDE\\_DO\\_PRIVILEGED, H = EQ\\_ABSTRACT\\_SELF, I = FI\\_PUBLIC\\_SHOULD\\_BE\\_PROTECTED, J = IMSE\\_DONT\\_CATCH\\_IMSE, K = MS\\_PKGPROTECT, L = MS\\_SHOULD\\_BE\\_FINAL, M = SE\\_BAD\\_FIELD\\_INNER\\_CLASS, N = SW\\_SWING\\_METHODS\\_INVOKED\\_IN\\_SWING\\_THREAD, O = UG\\_SYNC\\_SET\\_UNSYNC\\_GET}",file=outfile,append=TRUE)
write("\\caption{Total duration (in ms) and total memory usage (in KiB) for the static program analyses, as well as the time speed up relative to NO-OPT.}",file=outfile,append=TRUE)
write("\\label{tbl:analysisbenchmarks}",file=outfile,append=TRUE)
write("\\end{table*}",file=outfile,append=TRUE)

firstResult <- firstResult / length(names)
print(firstResult)
