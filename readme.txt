The PageRank can be found at: src\main\java\PageRank

I have used an external dependency Jcommander for parsing the input arguments. this needs to be imported to compile my code and a html parser JSoup.  


The build.gradle file has been included. This has all the dependencies included in it. This can be used to build the whole project in eclipse.  

Instructions for running the project:

gradle build
-- this will build the whole project

gradle PageRank
--create the jar file for the crawler

java -jar PageRank.jar -docs PageRankFiles -f 0.1
-- the arguments from -u onwards are customizable. 
