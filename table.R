source("preparedata.R")

formatd <- function(x) format(round(x, 2), nsmall=2)

outfile <- file.path("evaluation", "table.tex")





write("\\begin{table*} \\begin{center}",file=outfile)
write("\\begin{tabular}{ l  r  r | r r r | r r r }",file=outfile,append=TRUE)
write("& \\multicolumn{2}{ c }{OPT} & \\multicolumn{3}{ c }{NO-ALG-OPT}  & \\multicolumn{3}{ c }{NO-LMS-OPT}\\\\",file=outfile,append=TRUE)
write("\\toprule",file=outfile,append=TRUE)
write("\\multicolumn{1}{r|}{} & Time & Memory & Time & SpeedUp & Memory & Time & SpeedUp & Memory \\\\",file=outfile,append=TRUE)
write("\\midrule",file=outfile,append=TRUE)
i <- 0
for (s in names) {
  i <- i + 1
  optType <- "default"
  dataTime <- preparecsv5("time", optType, s)
  dataMemory <-  preparecsv5("memory", optType, s)
  optType <- "no-opts"
  dataTimeNoOpts <- preparecsv3("time", optType, s)
  dataMemNoOpts <- preparecsv3("memory", optType, s)
  optType <- "no-lms-opts"
  dataTimeNoLMSOpts <- preparecsv3("time", optType, s)
  
  dataTimeSum <- sum(dataTime$average)
  dataTimeNoOptsSum <- sum(dataTimeNoOpts$average)
  dataTimeNoLMSOptsSum <- sum(dataTimeNoLMSOpts$average)
  
  write(paste("\\multicolumn{1}{r|}{", rawToChar(as.raw(64 + i)), "}",
              " & ",formatd(dataTimeSum),
              " & ", formatd(sum(dataMemory$average)),
              " & ", formatd(dataTimeNoOptsSum) ,
              " & ", formatd((dataTimeNoOptsSum / dataTimeSum) * 100), "\\%",
              " & ",  formatd(sum(dataMemNoOpts$average)) ,
              " & ",  formatd(dataTimeNoLMSOptsSum) ,
              " & ", formatd((dataTimeNoLMSOptsSum / dataTimeSum) * 100), "\\%",
              " & ",  "mem" ,
              " \\\\" ,sep=""),file=outfile,append=TRUE)
  
}

write("\\bottomrule",file=outfile,append=TRUE)
write("\\end{tabular}",file=outfile,append=TRUE)
write("\\\\ {\\small Bug patterns of the analyses similar to FindBugs: A = BX\\_BOXING\\_IMMEDIATELY\\_UNBOXED\\_TO\\_PERFORM\\_COERCION, B = CI\\_CONFUSED\\_INHERITANCE, C = CN\\_IDIOM, D = CN\\_IDIOM\\_NO\\_SUPER\\_CALL, E = DM\\_GC, F = DM\\_RUN\\_FINALIZERS\\_ON\\_EXIT, G = DP\\_DO\\_INSIDE\\_DO\\_PRIVILEGED, H = EQ\\_ABSTRACT\\_SELF, I = FI\\_PUBLIC\\_SHOULD\\_BE\\_PROTECTED, J = IMSE\\_DONT\\_CATCH\\_IMSE, K = MS\\_PKGPROTECT, L = MS\\_SHOULD\\_BE\\_FINAL, M = SE\\_BAD\\_FIELD\\_INNER\\_CLASS, N = SW\\_SWING\\_METHODS\\_INVOKED\\_IN\\_SWING\\_THREAD, O = UG\\_SYNC\\_SET\\_UNSYNC\\_GET}",file=outfile,append=TRUE)
write("\\end{center}",file=outfile,append=TRUE)
write("\\caption{Total duration (in ms) and total memory usage (in KiB) for the static program analyses, as well as the time speed up relative to the analysis with optimizations.}",file=outfile,append=TRUE)
write("\\label{tbl:analysisbenchmarks}",file=outfile,append=TRUE)
write("\\end{table*}",file=outfile,append=TRUE)
