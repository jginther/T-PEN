# T-PEN.org backend
# Update Script
#
# © Thom Hastings 2013
# Saint Louis University
# Advance Technology Group
# Information Technology Services
#
#!/bin/sh
echo
echo "-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-"
echo " _____     ____  _____ _   _       T-PEN.org "
echo "|_   _|   |  _ \\| ____| \\ | |  ___  _ __ __ _"
echo "  | |_____| |_) |  _| |  \\| | / _ \\| '__/ _\` |"
echo "  | |_____|  __/| |___| |\\  || (_) | | | (_| | "
echo "  |_|     |_|   |_____|_| \\_(_)___/|_|  \\__, |"
echo "     T-PEN.org backend Update Script    |___/ "
echo "-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-"
echo "Welcome to the T-PEN.org backend Update Script"
echo "-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-=+=-"
echo
echo "_(¡) Updating Components!:…"
echo "…"
echo "+(1) Statistical Machine Translation ...… Moses-SMT:…"
if [ ! -e moses-smt ]
then
	echo "+) adding submodule"
    git submodule add git://github.com/moses-smt/mosesdecoder.git moses-smt
else
	echo "+) updating submodule"
    cd moses-smt
    git pull git://github.com/moses-smt/mosesdecoder.git master
    cd ..
fi
echo "…"
echo "+(2) Optical Character Recognition ...… Tesseract-OCR:…"
if [ ! -e tesseract-ocr ]
then
	echo "+) cloning via git-svn"
    git svn clone http://tesseract-ocr.googlecode.com/svn tesseract-ocr
else
	echo "+) git-svn rebase"
    cd tesseract-ocr
    git svn rebase
    cd ..
fi
echo "…"
echo "=(3) Toolset for training Tesseract data ...… TesseractTrainer:…"
if [ ! -e tesseract-trainer ]
then
	echo "=) adding submodule"
    git submodule add git://github.com/BaltoRouberol/TesseractTrainer.git tesseract-trainer
else
	echo "=) updating submodule"
    cd tesseract-trainer
    git pull git://github.com/BaltoRouberol/TesseractTrainer.git master
    cd ..
fi
echo "…"
echo "-(4) Example data (courtesy of Kevin Scannell) ...… Tesseract-GLE-Unical:…"
if [ ! -e tesseract-gle-unical ]
then
	echo "-) cloning via git-svn"
    git svn clone http://tesseract-gle-uncial.googlecode.com/svn/trunk/ tesseract-gle-uncial
else
	echo "-) git-svn rebase"
    cd tesseract-gle-unical
    git svn rebase
    cd ..
fi
echo
echo "_(¡) commiting changes!"
git commit -a -m "ran update.sh"
echo
echo "…(i) DONE with update script..."
echo
echo "~{¿} Do you want to git push?"
echo "This saves the updated code (to github.com)"
echo " [y/N]: "
read $INPUT
if [ $INPUT = "Y" ] or[ $INPUT = "y" ]
then
    git push origin master -u
fi
echo "_(¡) push complete!"
echo
echo "_(¡) Goodbye!"
