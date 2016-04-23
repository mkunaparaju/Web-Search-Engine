The Crawler can be found at: src\main\java\Crawler

I have used an external dependency Jcommander for parsing the input arguments. this needs to be imported to compile my code and a html parser JSoup.  


The build.gradle file has been included. This has all the dependencies included in it. This can be used to build the whole project in eclipse.  

Instructions for running the project:

gradle build
-- this will build the whole project

gradle crawlerjar
--create the jar file for the crawler

java -jar crawler.jar -u http://cs.nyu.edu/courses/spring16/CSCI-GA.2580-001/MarineMammal/PolarBear.html -q ocean -docs "<URL>" -m 7 -t
-- the arguments from -u onwards are customizable. 
