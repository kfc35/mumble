Tmumble
======
This is reverted back to 2.5.

The reason nothing worked is because my environment 
  somehow only uses 2.5, it doesn't use 3.0

Which meant that I needed to find a workaround/hack: 
  create an index.html file
  add the CS5300PROJ1SESSION servlet to to web.xml
  whenever index.html is hit, use the servlet instead

However, this means that the build path must now be
  WebContent/WEB-INF/classes/

Yet when I deploy this, beanstalk complains that I
used the wrong minor version. Turns out that beanstalk 
only uses java 1.6, and normal development uses 1.7 
now, so I needed another trick:
  So I added the .ebextensions folder to WebContent
  This causes the war file to compile with the folder
  Write a script that installs java 1.7 whenever a
    new machine/environment is yup and refer to 1.7
    as the java instance.
  PUSH AND DEPLOYYYYYYYYYYYYYYYYYYYYYYYY

======
