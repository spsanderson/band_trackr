=============
Testing Guide
=============

We're focused on writing unit and integration tests, with plans on introducing 
performance tests in the future. The focus and importance of our tests is 
twofold: first, they should prove that our code is working as expected; second, 
they should provide a great example of how certain components are expected to be 
used in various contexts at varying degrees of detail.

What to test
============

You should be concerned with testing everything but static presentation, since
our requirements specifications never go into such excrutiating details. Not 
testing third-party and framework components goes without saying; we're expecting 
that code we adopt from external sources is well tested and that it exhibits 
proper behavior.

Everything else is good game. Test activities and their behavior, test 
any kind of behavior of custom view components, test the internal and external 
behavior of any other high-level components, test any other classes you build. 
In a nutshell, test anything you believe the application expects and anticipate 
special cases and unexpected behavior.

Make sure everything that you write functions and behaves properly in any 
possible manner that the application may use them.

When to write tests
===================

Although you can write tests at any time, preferably you'll do it before you 
being to introduce any new code. You know the benefits, but let's reiterate on 
them once more for the sake of corectness: not only does it help you understand 
what is it that your actually suppose to introduce, it allows you to readily 
prove whether or not your requirements are satisfied, in a quick and automated 
manner.

The definitive point when you tests should be introduced is immediately when 
checking new code into the master branch. Work on your topical branch in any 
manner that you fancy the most, but don't throw it on the pile before you can 
back the behavior up with tests.

How tests are organized
=======================

The tests are located in the application project under `/BandTrackr/tests`. The
structure follows the Android platform guidelines for a test project.

The `~/tests/src/` directory contains the sources under the package 
`ba.genijalno.bandtrackr.test`. As a minimum, you'll find `%CLASS%Test.java` 
equivalents for every application classes relative to the actual path of the 
application sources in the test package.

In addition to that, you can supply any additional test cases in new test classes 
when it makes sense to do so, such as providing a generic feature test case, 
or test cases that test code in specific contexts, such as large integration tests 
that don't tests components in isolation. 

Just make sure that it's easy to find tests for actual application classes and 
that the test class names convey enough information about what's going on with 
the tests contained therein.

Running tests
=============

We're exclusively relying on Android's intrumentation test runner to run the 
tests, although simple components that aren't dependent on the Android platform 
can be executed with the JUnit test runner for tests in isolation.

While you can focus on running tests only for the current test cases your 
working on, don't be satisifed with your tests until you run the full suite of 
application tests and seeing all greens. 

.. topic:: Important resources

	`Testing introduction <http://developer.android.com/resources/tutorials/testing/helloandroid_test.html>`_ - 
	is the official Android introduction to testing.
	
	`Testing fundamentals <http://developer.android.com/guide/topics/testing/testing_android.html>`_ - 
	is a topical guide on writing tests for the Android platform.
	
	`Introduction to testing <http://dtmilano.blogspot.com/2011/08/linuxcon-2011-north-america.html>`_ - 
	is a presentation packed with slides providing a great, in-depth introduction to writing tests on 
	the Android platform.
	
	`Android Application Testing Guide <http://www.amazon.com/Android-Application-Testing-Torres-Milano/dp/1849513503>`_ - 
	is currently the only book on the market exclusively devoted to the topic. 
	It's the most definitive, well-rounded guide to writing applicaiton tests on 
	the Android platform.
	
	`Test classes <http://developer.android.com/reference/android/test/package-summary.html>`_ -  
	provides a summary of all of the classes in the `android.test` package.
	
	`Instrumentation test runner <http://developer.android.com/reference/android/test/InstrumentationTestRunner.html>`_ -  
	contains references to running instrumentation tests.
	
	