#!/bin/sh
cd mosesdecoder
git pull
cd ..
cd tesseract-ocr
git svn rebase
cd ..
