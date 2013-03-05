#!/bin/sh
echo "Updating..."
echo
echo "Moses-SMT:"
if [ ! -e mosesdecoder ]
then
    git submodule add git://github.com/moses-smt/mosesdecoder.git
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
if [ ! -e TesseractTrainer ]
then
    git submodule add git://github.com/BaltoRouberol/TesseractTrainer.git
else
    cd TesseractTrainer
    git pull git://github.com/BaltoRouberol/TesseractTrainer.git master
    cd ..
fi
echo
echo "DONE"
