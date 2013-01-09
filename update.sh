#!/bin/sh
cd mosesdecoder
git pull
cd ..
cd tesseract-ocr
git svn rebase
cd ..
cd TesseractTrainer
git pull
cd ..
echo "DONE"
