all: clean compile jar

clean: 
	rm -f ArtGel_0.9.1.jar
	rm -rf bin
	
compile:
	mkdir bin 
	javac -cp lib/NGSEPcore_3.3.1.jar -d bin src/Geles/*.java src/swinggui/*.java

jar: 
	mkdir dist
	jar -xf lib/NGSEPcore_3.3.1.jar JSci
	mv JSci dist/
	jar -xf lib/NGSEPcore_3.3.1.jar ngsep
	mv ngsep dist/
	cp -r bin/* dist/
	jar -cfe ArtGel_0.9.1.jar swinggui.IntensityProcessorInterface -C dist . 
	rm -rf dist
