rm debug.keystore

keytool -genkey -v -keystore debug.keystore -alias iniline -keyalg RSA -VALIDITY 10000
