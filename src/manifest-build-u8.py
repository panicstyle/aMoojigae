#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
import re
import codecs

def main():
	# parse command line options
	filename = sys.argv[1]
	buildnum = sys.argv[2]
	
	f = codecs.open(filename, "r", "utf_8")
#	f = open(filename, "r")
	ff = f.read()
	f.close()
	
	str0='android:versionName="'
	a = ff.find(str0)
	print (a)
	b = ff.find ('"', a+len(str0))
	print (b)
	c = ff[a+len(str0):b]
	print (c)
	d = c.split('.')
	str1 = str0 + d[0] + '.' + d[1] + '.' + d[2]
	str2 = str0 + d[0] + '.' + d[1] + '.' + buildnum
	print (str1)
	print (str2)
	ff = str.replace(ff, str1, str2)

	f = codecs.open(filename, "w", "utf_8")
#	f = open(filename, "w")
	f.write(ff)
	f.close()

if __name__ == "__main__":
    main()