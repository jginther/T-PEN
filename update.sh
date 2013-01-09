#!/bin/sh
echo "Updating..."
echo
cd mosesdecoder
echo "Moses-SMT:"
git pull
cd ..
cd tesseract-ocr
echo "Tesseract-OCR:"
git svn rebase
cd ..
cd TesseractTrainer
echo "TesseractTrainer:"
git pull
cd ..
echo
echo "DONE"
