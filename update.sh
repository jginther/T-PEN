# T-PEN.org backend
# Update Script
#
# © Thom Hastings 2013
# Saint Louis University
# Advance Technology Group
# Information Technology Services
#
#!/bin/sh
echo "Updating..."
echo
echo "Statistical Machine Translation… Moses-SMT:"
if [ ! -e moses-smt ]
then
	echo "adding submodule"
    git submodule add git://github.com/moses-smt/mosesdecoder.git moses-smt
else
	echo "updating submodule"
    cd mosesdecoder
    git pull git://github.com/moses-smt/mosesdecoder.git master
    cd ..
fi
echo "Optical Character Recognition… Tesseract-OCR:"
if [ ! -e tesseract-ocr ]
then
	echo "cloning via git-svn"
    git svn clone http://tesseract-ocr.googlecode.com/svn tesseract-ocr
else
	echo "git-svn rebase"
    cd tesseract-ocr
    git svn rebase
    cd ..
fi
echo "Toolset for training Tesseract data… TesseractTrainer:"
if [ ! -e tesseract-trainer ]
then
	echo "adding submodule"
    git submodule add git://github.com/BaltoRouberol/TesseractTrainer.git tesseract-trainer
else
	echo "updating submodule"
    cd tesseract-trainer
    git pull git://github.com/BaltoRouberol/TesseractTrainer.git master
    cd ..
fi
echo "Example data (courtesy of Kevin Scannell)… Tesseract-GLE-Unical:"
if [ ! -e tesseract-gle-unical ]
then
	echo "cloning via git-svn"
    git svn clone http://tesseract-gle-uncial.googlecode.com/svn/trunk/ tesseract-gle-uncial
else
	echo "git-svn rebase"
    cd tesseract-gle-unical
    git svn rebase
    cd ..
fi
echo
echo "DONE"

