===========
Band Trackr
===========

This README is a quick rundown of everything you need to get started as a project developer.

Prerequisites
=============

Before you get started, make sure you have the following installed and configured
in your development environment.

- Python version 2.4, at least, for building the project documentation. We recommend 
  that you obtain the latest 2.X production version from the 
  `downloads page <http://www.python.org/download/>`_ (2.7.2 at the time of writing).
- Java SE 6, both the JDK and JRE distributions, which you can grab from the 
  official Java SE `downloads page <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`_.
- The `Android SDK <http://developer.android.com/sdk/index.html>`_. We're developing against the
  level 7 API version, so make sure you obtain the SDK platform for the level 7 API subsequently.
 
Dependencies
============

The following is a list of the Band Trackr project dependencies that you'll need 
to manually obtain and/or configure per development environment.

Sphinx - project documentation
------------------------------

We use `Sphinx <http://sphinx.pocoo.org/>`_ for managing the project documentation. 
You'll need to obtain the Sphinx distribution in order to build the documentation.

Make sure you have Python and the `setuptools <http://pypi.python.org/pypi/setuptools#downloads>`_ 
script already installed for your distribution. You can verify that you have both available
by running the following from the command line and recieving valid output::

    python --version
    easy_install --help

You can install Sphinx by running the following command::

    easy_install sphinx==1.0.8
    
Afterwards, you can go into the documentation folder, located at `/docs`, and run
the following command to build the documentation in your preferred reading format,
typically HTML::

   cd docs
   .\make.bat html
   make html
   
Windows Users
'''''''''''''

On Windows, you'll have to make sure that the Python script folder is on the
system path in order to use setuptool's `easy_install` and Sphinx's `sphinx-build` 
(referenced by ./docs/make.bat and ./docs/makefile) apps from the command line.
   
The scripts folder is typically located at `C:\\Program Files (x86)\\Python\\Scripts`. 
If you receive any errors trying to use `easy_install` or while building the documentation,
that's probably it.
   
lastfm-java - last.fm API client
--------------------------------

`lastfm-java <http://code.google.com/p/lastfm-java/>`_ is a Java client for the 
`last.fm service API <http://www.last.fm/api>`_. 

We have a custom distribution checked-out with the project sources, located at the
top level in the `/lastfm-java` directory. You'll have to make the sources available
on the build path.

NewQuickAction - QuickAction view component
-------------------------------------------

You'll have to obtain the sources for the QuickAction view. 
Check out the source at the top-level directory via::

    git clone git://github.com/lorensiuswlt/NewQuickAction
    
You need to later customize the build properties of NewQuickAction to turn it
into a Android Library Project so that it's resources be properly referenced. Make sure 
the following setting is configured in `./NewQuickAction/default.properties`::

    android.library=true
    
engage.android - Janrain Engage library for Android
---------------------------------------------------

Janrain Engage is a drop-in login and sharing solution for popular social Web 
applications. Check out the sources at the top-level directory via::

    git clone git://github.com/janrain/engage.android
    
Other dependencies
------------------

Other dependencies are checkout out under `./BandTrackr/lib`. Make sure they're 
all correctly referenced when building the project.

Important addresses
===================

- https://github.com/bandtrackr/ - Band Trackr `GitHub <http://github.com/>`_ account.
- https://www.moosucka.com/projects/bandtrackr/ - the issue tracker
- http://www.mybandtrackr.com/ - the Band Trackr website
- http://twitter.com/bandtrackr/ - follow us on Twitter 
