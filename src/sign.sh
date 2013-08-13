jarsigner -verbose -keystore ./keystore/debug.keystore -storepass iniline -keypass iniline \
	-signedjar ./bin/Moojigae-release-signed.apk \
	./bin/Moojigae-release-unsigned.apk \
	iniline
