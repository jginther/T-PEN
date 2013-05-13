#!/bin/sh
echo "Updating..."
echo
echo "Moses-SMT:"
if [ ! -e moses-smt ]
then
    git submodule add git://github.com/moses-smt/mosesdecoder.git moses-smt
else
    cd mosesdecoder
    git pull git://github.com/moses-smt/mosesdecoder.git master
    cd ..
fi
echo "Tesseract-OCR:"
if [ ! -e tesseract-ocr ]
then
    git svn clone http://tesseract-ocr.googlecode.com/svn tesseract-ocr
else
    cd tesseract-ocr
    git svn rebase
    cd ..
fi
echo "TesseractTrainer:"
if [ ! -e tesseract-trainer ]
then
    git submodule add git://github.com/BaltoRouberol/TesseractTrainer.git tesseract-trainer
else
    cd tesseract-trainer
    git pull git://github.com/BaltoRouberol/TesseractTrainer.git master
    cd ..
fi
echo "Tesseract-GLE-Unical:"
if [ ! -e tesseract-gle-unical ]
then
    git svn clone http://tesseract-gle-uncial.googlecode.com/svn/trunk/ tesseract-gle-uncial
else
    cd tesseract-gle-unical
    git svn rebase
    cd ..
fi
echo
echo "DONE"
