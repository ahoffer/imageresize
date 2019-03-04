install -s  mvn:com.github.jai-imageio/jai-imageio-jpeg2000/1.3.1_CODICE_3
install -s mvn:com.google.guava/guava/26.0-jre
install -s mvn:org.apache.commons/commons-exec/1.3


// Needed for Open JPEG
sudo apt-get install libopenjp2-7

// Install GDAL
sudo add-apt-repository -r ppa:ubuntugis/ppa && sudo apt-get update
