#!/usr/bin/perl
# ocrfix.pl
# by Kevin Scannell
# GPLv3+

use strict;
use warnings;
use utf8;

my %ga;
my %twograms;

if ($#ARGV != 0) {
    die "Usage: cat file.txt | $0 xx > corrected.txt";
}
my $teanga = $ARGV[0];
my @queue;   # 3-gram of words to process.  

open(GRAMS, "<:utf8", "twograms-$teanga.txt") or die "Could not open n-grams file: $!";
while (<GRAMS>) {
	chomp;
	/^ *([0-9]+) (.+)$/;
	$twograms{$2} = log($1);
}
close GRAMS;

# this has "personal" dict included too, for personal names, etc.
open(GA, "<:utf8", "aspell-$teanga.txt") or die "Could not open $teanga dictionary: $!";
while (<GA>) {
	chomp;
	$ga{$_}++;
}
close GA;

my $personal = '/home/kps/.hunspell_ga_IE';
$personal =~ s/ga_IE/en_US/ if ($teanga eq 'en');
$personal =~ s/ga_IE/bo_TH/ if ($teanga eq 'bo');
open(GALT, "<:utf8", $personal) or die "Could not open hunspell personal dictionary $personal: $!";
while (<GALT>) {
	chomp;
	$ga{$_}++;
}
close GALT;

my @orig;
my @fix;
my @replprobs;
#open(SUBS, "<:utf8", "subs-$teanga.txt") or die "Could not open list of OCR corrections for $teanga: $!";
open(SUBS, "<:utf8", "sub-stats-$teanga.txt") or die "Could not open list of OCR corrections for $teanga: $!";
while (<SUBS>) {
	chomp;
#	(my $l, my $r) = /^ *[0-9]* ([^:]+):(.+)$/;
	(my $l, my $r, my $count, my $tot) = /^([^:]+):([^ ]+) ([0-9]+) ([0-9]+)$/;
	unless ($l =~ m/(\[|\])/ or $r =~ m/(\[|\])/) {
		if (abs(length($l) - length($r)) < 3 and $tot > 0) {
			push @orig,$l;
			push @fix,$r;
			push @replprobs, sprintf("%.5f", log($count)-log($tot));
		}
	}
}
close SUBS;

# word $w passed in might have had some subs applied already
sub applyone
{
	(my $w, my $replprsofar, my $l, my $r, my $prepend, my $fillref) = @_;
	if ($w =~ m/^(.*?)$l(.*)$/) {
		my $pre=$1;
		my $post=$2;
		if ($pre =~ /\[[^\]]*$/) {
			$prepend .= $pre.$l;
			if ($post =~ /^([^\]]*\])/) {
				my $tobracket = $1;
				$post =~ s/^[^\]]*\]//; 
				$prepend .= $tobracket;
			}
			else {
				print "PROBLEM: w=$w, l=$l, r=$r, prepend=$prepend, pre=$pre, post=$post\n";
			}
		}
		else {
			my $repl=$prepend.$pre."[$r]".$post.':'.$replprsofar;
			push @$fillref, $repl;
			$prepend .= $pre.$l;
		}
		applyone($post, $replprsofar, $l, $r, $prepend, $fillref);
	}
}

# word passed in might have had some subs applied already
# in which case it looks like eos[á]n - skip stuff in brackets
# full string $w looks like "dhcanamh:0.0", where the stuff after the colon is 
# a log prob of whatever replacements have already been made
sub applyeach
{
	(my $w, my $fillref) = @_;
	(my $ww, my $replpr) = $w =~ /^([^:]+):(.+)$/;
	my $count = 0;
	foreach my $l (@orig) {
		applyone($ww, $replpr+$replprobs[$count], $l, $fix[$count], '', $fillref);
		$count++;
	}
}

sub fixcase {
	(my $lower, my $org) = @_;
	if ($org =~ m/^(\p{Lu}|['-])+$/) {
		return uc($lower);
	}
	elsif ($org =~ m/^\p{Ll}\p{Lu}(\p{Ll}|['-])*$/) {
		(my $a, my $b, my $rest) = $lower =~ /^(.)(.)(.*)$/;
		return $a.uc($b).$rest;
	}
	elsif ($org =~ m/^\p{Lu}(\p{Ll}|['-])*$/) {
		return ucfirst($lower);
	}
	else {
		return $lower;
	}
}

# gets a word as argument, returns best guess for that word
sub processword
{
	(my $w) = @_;
	push @queue, $w unless ($w eq 'f' or $w eq 'm'); # genders in EID
	shift @queue if (scalar @queue > 3);
	if ($teanga eq 'ga' or $teanga eq 'bo') {
		return 'atá' if ($w eq 'ata');
		return 'Bhí' if ($w eq 'Bhi'); # else Bhá
		return 'bhfuil' if ($w eq 'bhruil');
		return 'cárta' if ($w eq 'carta');
		return 'chárta' if ($w eq 'charta');
		return 'eile' if ($w eq 'cile');
		return 'fírinne' if ($w eq 'firinne');
		return 'fhírinne' if ($w eq 'fhirinne');
		return 'Ní' if ($w eq 'Ni'); # else Mí
		return 'Ón' if ($w eq 'On');
		return 'rópa' if ($w eq 'ropa');
	}
	elsif ($teanga eq 'en') {
	}
	my $lcw = lc($w);
	return $w if (exists($ga{$w}) or exists($ga{$lcw}));
	return $w if (scalar @queue < 3);
	my %cands;
	my @wsubs;
	applyeach("$w:0", \@wsubs);
	my @wsubsubs;
	foreach my $walt (@wsubs) {
		applyeach($walt, \@wsubsubs);
	}
	push @wsubs, @wsubsubs;
	foreach my $walt (@wsubs) {
		my $wcopy=$walt;
		$wcopy =~ s/:.+$//;
		$wcopy =~ s/[\]\[]//g;
		$cands{$walt}++ if (exists($ga{$wcopy}));
	}
	my $bestw;
	my $bestscore=-1000000;
	foreach my $cand (keys %cands) {
#		print "considering candidate $cand...\n";
		my $candraw = $cand;
		$candraw =~ s/:(.+)$//;
		my $candreplprob = $1;
		$candraw =~ s/[\]\[]//g;
		my $twog = $queue[1].' '.$candraw;
#		print "two-gram = $twog...\n";
		my $logprob = -0.69;
		if (exists($twograms{$twog})) {
			$logprob = $twograms{$twog};
		}
#		print "logprob = $logprob, 2-grams only...\n";
		$logprob += $candreplprob;
#		print "logprob = $logprob, after adding repl probs...\n";
		if ($logprob > $bestscore) {
			$bestscore = $logprob;
			$bestw = $candraw;
		}
	}
	return $bestw if ($bestscore > -1000000);
	if ($w ne $lcw) {
		my $lcans = processword($lcw);
		return fixcase($lcans,$w) if ($lcw ne $lcans);
	}
	return $w;
}


binmode STDIN, ":utf8";
binmode STDOUT, ":utf8";
binmode STDERR, ":utf8";

my $curr='';
my $toktype=0; # 0=word, 1=number, 2=other (punc, etc.)

sub flushcurr {
	if ($curr ne '') {
		if ($toktype==0) {
			# first block is pair of balanced parens, but not initial/final
			# e.g. analytic(al)
			if ($curr =~ m/^..*\(.+\)/ or $curr =~ m/\(.+\).*.$/) {
				print $curr;
				$curr = '';
				return;
			}
			if ($curr =~ m/^([(]+)([^()]*[)]?)$/) {  # "(Don't" or "(good)"
				print $1;
				$curr = $2;
			}
			(my $post) = $curr =~ m/(['()-]*)$/;
			$curr =~ s/(['()-]*)$//;
			unless ($curr eq '') {
				$curr = processword($curr);
				print $curr;
			}
			print $post;
		}
		else {
			push @queue, $curr;
			shift @queue if (scalar @queue > 3);
			print $curr;
		}
		$curr = '';
	}
}

while (<STDIN>) {
    while (/(.)/gs) {
        my $c=$1;
        if ($c =~ /^(\p{L}|\p{M}|['()-])$/) {
			flushcurr() unless ($toktype==0);
			$toktype=0;
        }
#        elsif ($c =~ /^['-]$/) {
#			unless ($toktype==0 and $curr ne '') {
#				flushcurr();
#				$toktype=2;
#			}
#        }
		elsif ($c =~ /^[0-9]$/) {
			flushcurr() unless ($toktype==1);
			$toktype=1;
		}
        else {
			flushcurr();
			$toktype=2;
        }
		if ($c =~ /^\s$/) {
			print $c;
		}
		elsif ($c !~ /^\x{AD}$/) {
			$curr .= $c;
		}
    }
}
exit 0;
