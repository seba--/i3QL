source("preparedata.R")

formatd <- function(x) format(round(x, 2), nsmall=2)

outfile <- file.path("evaluation", "table.tex")




write("\\documentclass{article}",file=outfile)
write("\\begin{document}",file=outfile,append=TRUE)
write("\\begin{center} \\makebox[\\textwidth]{",file=outfile,append=TRUE)
write("\\begin{tabular}{ p{5cm} || r | r | r | r | r | r |}",file=outfile,append=TRUE)
write("& \\multicolumn{2}{| l |}{with optimizations} & \\multicolumn{2}{| l |}{without optimizations}  & \\multicolumn{2}{| l |}{without LMS optimizations}\\\\",file=outfile,append=TRUE)
write(" & Time (ms) & Memory (KiB) & Time (ms) & Memory (KiB)  & Time (ms) & Memory (KiB) \\\\ \\hline \\hline",file=outfile,append=TRUE)

for (s in names) {
  optType <- "default"
  dataTime <- preparecsv5("time", optType, s)
  dataMemory <-  preparecsv5("memory", optType, s)
  optType <- "no-opts"
  dataTimeNoOpts <- preparecsv3("time", optType, s)
  dataMemNoOpts <- preparecsv3("memory", optType, s)
  write(paste(gsub("_", " ",s)," & ",formatd(sum(dataTime$average)),
              " & ", formatd(sum(dataMemory$average)),
              " & ", formatd(sum(dataTimeNoOpts$average)) ,
              " & ",  formatd(sum(dataMemNoOpts$average)) ,
              " & ",  "time" ,
              " & ",  "mem" ,
              " \\\\ \\hline" ,sep=""),file=outfile,append=TRUE)
  
}

write("\\end{tabular}}",file=outfile,append=TRUE)
write("\\end{center}",file=outfile,append=TRUE)
write("\\end{document}",file=outfile,append=TRUE)