all: sigproc-sp.pdf

%.pdf: %.tex
	xelatex sigproc-sp.tex
	bibtex sigproc-sp.aux
	xelatex sigproc-sp.tex
	xelatex sigproc-sp.tex
	evince sigproc-sp.pdf > /dev/null &

clean:
	rm -f *.aux *.log *.out *.bbl *.blg sigproc-sp.pdf
