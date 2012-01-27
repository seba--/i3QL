NAME=paper-draft
BIB=~/Documents/Research/PS-Repo/Paolo/DB.bib

.PHONY: all FORCE

.PRECIOUS: $(NAME).tex

all: $(NAME).pdf oopsla12.pdf
%.pdf: %.tex
	pdflatex $<
	bibtex $*
	pdflatex $<
	pdflatex $<

%.tex: %.txt FORCE
	pandoc --template latex.template --natbib -s $< -o $@
#pandoc --natbib --bibliography $(BIB) -s $< -o $@
