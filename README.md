Digtwit
=======

Digtwit is a very simple command-line tool in Java, that checks for a hashtag (#digmia in our case) in a user's friends feed on Twitter and retweets any messages mentioning that hashtag automatically. By following a user, you allow a user to be included in automatic retweets. It is meant to be run from cron. 

Copying
=======

This is a simple retweeting software. Uses
[JTwitter](http://www.winterwell.com/software/jtwitter.php) which
is under LGPL license.

This software (Digtwit) is public domain, developed
by Juraj Bednar (juraj.bednar@digmia.com). It comes under
absolutely no warranty. Test carefully before you use it.

Usage
=====

You can use binaries that can be found under Releases.

Create ```digtwit.properties``` file containing
username, path to id file and string (probably a hashtag) to look
for. Beware, that you have to escape "#" (\#).

Run the application:

    java -jar digtwit.jar

It will produce an URL, that you have to visit to allow Digtwit to access twitter
using your username (note: you don't ever give your twitter password
to digtwit).

Once you visit the page and authorize Digtwit, twitter will give
you a PIN, which you will enter to the application. It will produce
two options, that you need to cut & paste to your ```digtwit.properties```
file.

On twitter, follow users you want to retweet from. This is sort of an access list.

Run the program periodically, e.g. from crontab using

    java -jar digtwit.jar

