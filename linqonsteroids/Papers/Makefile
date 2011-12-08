NAME=paper-draft
.PHONY: all
all: $(NAME).pdf
%.pdf: %.tex
	pdflatex $<
%.tex: %.txt
	pandoc -s $< -o $@
